package rich.util.profiler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Detailed FPS profiler for finding real causes of drops.
 *
 * What it logs:
 * - frame time: avg / p95 / p99 / worst
 * - FPS calculated from frame time
 * - top hot sections: event handlers, HUD/world render modules, named sections
 * - call counts, max single call, max per-frame cost
 * - GC and memory stats
 * - world/entity snapshot
 * - separate spike log with top sections for the exact bad frame
 *
 * Commands:
 * - .profiler on [autoDumpSeconds]
 * - .profiler off
 * - .profiler reset
 * - .profiler top [n]
 * - .profiler dump
 * - .profiler interval <seconds>
 * - .profiler spike <ms>
 * - .profiler status
 */
public final class FrameProfiler {
   private static final FrameProfiler INSTANCE = new FrameProfiler();

   public static FrameProfiler getInstance() {
      return INSTANCE;
   }

   private volatile boolean enabled = false;
   private volatile long autoDumpIntervalMs = 5_000L;
   private volatile long spikeMinMs = 45L;
   private volatile double spikeFactor = 2.25;
   private volatile long spikeLogMinGapMs = 150L;

   private static final int FRAME_WINDOW = 2400;
   private static final String LOG_DIR = "logs";
   private static final String REPORT_FILE = "rich-fps-profile.log";
   private static final String SPIKE_FILE = "rich-fps-spikes.log";

   private static final class Section {
      final String name;
      long frameNanos;
      int frameCalls;
      long totalNanos;
      long totalCalls;
      long maxCallNanos;
      long maxFrameNanos;
      long framesPresent;

      Section(String name) {
         this.name = name;
      }
   }

   private final Map<String, Section> sections = new ConcurrentHashMap<>();
   private final ThreadLocal<ArrayDeque<Object[]>> stack = ThreadLocal.withInitial(ArrayDeque::new);

   private long renderStartNano = 0L;
   private long lastFrameEndNano = 0L;
   private final long[] frameTimes = new long[FRAME_WINDOW];
   private int frameTimeIdx = 0;
   private int frameTimeCount = 0;
   private long windowFrameCount = 0L;
   private long worstFrameNanos = 0L;

   private boolean frameFocused = true;
   private long skippedUnfocusedFrames = 0L;

   private long windowStartMs = 0L;
   private long lastDumpMs = 0L;
   private long lastSpikeLogMs = 0L;

   private long gcBaseCount = 0L;
   private long gcBaseTime = 0L;

   private FrameProfiler() {
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public long getAutoDumpIntervalMs() {
      return this.autoDumpIntervalMs;
   }

   public long getSpikeMinMs() {
      return this.spikeMinMs;
   }

   public double getSpikeFactor() {
      return this.spikeFactor;
   }

   public synchronized void setEnabled(boolean value) {
      if (value && !this.enabled) {
         this.resetInternal();
         this.appendLine(REPORT_FILE, "==== PROFILER START " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " ====");
      }
      this.enabled = value;
      if (!value) {
         this.appendLine(REPORT_FILE, "==== PROFILER STOP " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " ====");
      }
   }

   public synchronized void setAutoDumpIntervalSeconds(long seconds) {
      if (seconds <= 0L) {
         this.autoDumpIntervalMs = 0L;
      } else {
         this.autoDumpIntervalMs = Math.max(1000L, seconds * 1000L);
      }
   }

   public synchronized void setSpikeMinMs(long ms) {
      this.spikeMinMs = Math.max(5L, ms);
   }

   public synchronized void setSpikeFactor(double factor) {
      this.spikeFactor = Math.max(1.1, factor);
   }

   public synchronized void reset() {
      this.resetInternal();
   }

   private void resetInternal() {
      this.sections.clear();
      this.stack.remove();
      this.frameTimeIdx = 0;
      this.frameTimeCount = 0;
      this.windowFrameCount = 0L;
      this.worstFrameNanos = 0L;
      this.lastFrameEndNano = 0L;
      this.renderStartNano = 0L;
      this.skippedUnfocusedFrames = 0L;
      long now = System.currentTimeMillis();
      this.windowStartMs = now;
      this.lastDumpMs = now;
      this.lastSpikeLogMs = 0L;
      this.gcBaseCount = gcCount();
      this.gcBaseTime = gcTime();
   }

   private Section section(String name) {
      Section s = this.sections.get(name);
      if (s == null) {
         s = new Section(name);
         Section prev = this.sections.putIfAbsent(name, s);
         if (prev != null) {
            s = prev;
         }
      }
      return s;
   }

   public void record(String name, long nanos) {
      if (!this.enabled || nanos < 0L || name == null) {
         return;
      }
      Section s = this.section(name);
      synchronized (s) {
         s.frameNanos += nanos;
         s.frameCalls++;
         if (nanos > s.maxCallNanos) {
            s.maxCallNanos = nanos;
         }
      }
   }

   public void begin(String name) {
      if (!this.enabled || name == null) {
         return;
      }
      this.stack.get().push(new Object[]{name, System.nanoTime()});
   }

   public void end() {
      if (!this.enabled) {
         return;
      }
      ArrayDeque<Object[]> st = this.stack.get();
      Object[] top = st.poll();
      if (top == null) {
         return;
      }
      this.record((String) top[0], System.nanoTime() - (Long) top[1]);
   }

   public void beginFrame() {
      if (!this.enabled) {
         return;
      }
      this.renderStartNano = System.nanoTime();
      boolean focused = true;
      try {
         MinecraftClient mc = MinecraftClient.getInstance();
         focused = mc != null && mc.isWindowFocused();
      } catch (Throwable ignored) {
      }
      this.frameFocused = focused;
   }

   public synchronized void frameEnd() {
      if (!this.enabled) {
         return;
      }

      long now = System.nanoTime();
      if (!this.frameFocused) {
         this.clearFrameAccumulators();
         this.lastFrameEndNano = now;
         this.renderStartNano = 0L;
         this.skippedUnfocusedFrames++;
         return;
      }

      if (this.renderStartNano > 0L) {
         this.record("Frame/render(total)", now - this.renderStartNano);
      }

      long frameNanos = this.lastFrameEndNano > 0L ? now - this.lastFrameEndNano : 0L;
      this.lastFrameEndNano = now;

      if (frameNanos > 0L) {
         double avgMs = this.averageFrameMs();
         double frameMs = frameNanos / 1_000_000.0;
         long gapMs = System.currentTimeMillis() - this.lastSpikeLogMs;
         boolean absSpike = frameMs >= this.spikeMinMs;
         boolean relSpike = this.frameTimeCount > 30 && avgMs > 0.0 && frameMs >= avgMs * this.spikeFactor;
         if (gapMs >= this.spikeLogMinGapMs && (absSpike || relSpike)) {
            this.logSpike(frameMs, avgMs);
            this.lastSpikeLogMs = System.currentTimeMillis();
         }

         this.frameTimes[this.frameTimeIdx] = frameNanos;
         this.frameTimeIdx = (this.frameTimeIdx + 1) % FRAME_WINDOW;
         if (this.frameTimeCount < FRAME_WINDOW) {
            this.frameTimeCount++;
         }
         if (frameNanos > this.worstFrameNanos) {
            this.worstFrameNanos = frameNanos;
         }
         this.windowFrameCount++;
      }

      for (Section s : this.sections.values()) {
         synchronized (s) {
            if (s.frameCalls > 0) {
               s.totalNanos += s.frameNanos;
               s.totalCalls += s.frameCalls;
               s.framesPresent++;
               if (s.frameNanos > s.maxFrameNanos) {
                  s.maxFrameNanos = s.frameNanos;
               }
               s.frameNanos = 0L;
               s.frameCalls = 0;
            }
         }
      }

      long nowMs = System.currentTimeMillis();
      if (this.autoDumpIntervalMs > 0L && nowMs - this.lastDumpMs >= this.autoDumpIntervalMs && this.windowFrameCount > 0L) {
         try {
            writeReport(this.buildReport());
         } catch (Throwable ignored) {
         }
         this.resetInternal();
      }
   }

   private void clearFrameAccumulators() {
      for (Section s : this.sections.values()) {
         synchronized (s) {
            s.frameNanos = 0L;
            s.frameCalls = 0;
         }
      }
   }

   private double averageFrameMs() {
      if (this.frameTimeCount == 0) {
         return 0.0;
      }
      long sum = 0L;
      for (int i = 0; i < this.frameTimeCount; i++) {
         sum += this.frameTimes[i];
      }
      return (sum / (double) this.frameTimeCount) / 1_000_000.0;
   }

   private double percentileFrameMs(int p) {
      if (this.frameTimeCount == 0) {
         return 0.0;
      }
      long[] copy = new long[this.frameTimeCount];
      System.arraycopy(this.frameTimes, 0, copy, 0, this.frameTimeCount);
      Arrays.sort(copy);
      int idx = (int)Math.ceil(p / 100.0 * this.frameTimeCount) - 1;
      if (idx < 0) {
         idx = 0;
      }
      if (idx >= this.frameTimeCount) {
         idx = this.frameTimeCount - 1;
      }
      return copy[idx] / 1_000_000.0;
   }

   private List<Section> sortedSections() {
      List<Section> list = new ArrayList<>(this.sections.values());
      list.sort(Comparator.comparingLong((Section s) -> s.totalNanos).reversed());
      return list;
   }

   public synchronized List<String> topLines(int n) {
      List<String> out = new ArrayList<>();
      if (!this.enabled && this.sections.isEmpty()) {
         out.add("Профилировщик выключен и данных нет. Включить: .profiler on");
         return out;
      }

      double avgMs = this.averageFrameMs();
      double fps = avgMs > 0.0 ? 1000.0 / avgMs : 0.0;
      out.add(String.format("FPS avg=%.1f | frame avg=%.2fms p95=%.2f p99=%.2f worst=%.1fms | кадров=%d",
            fps, avgMs, this.percentileFrameMs(95), this.percentileFrameMs(99), this.worstFrameNanos / 1_000_000.0, this.windowFrameCount));

      List<Section> list = this.sortedSections();
      long denom = this.windowFrameCount > 0L ? this.windowFrameCount : 1L;
      int i = 0;
      for (Section s : list) {
         if (i >= n) {
            break;
         }
         i++;
         double perFrame = (s.totalNanos / (double)denom) / 1_000_000.0;
         double pct = avgMs > 0.0 ? perFrame / avgMs * 100.0 : 0.0;
         double callsPerFrame = s.totalCalls / (double)denom;
         out.add(String.format("%2d. %s | %.3fms/f %.1f%% | maxF %.2f | maxCall %.3f | calls/f %.1f",
               i, trim(s.name, 42), perFrame, pct, s.maxFrameNanos / 1_000_000.0, s.maxCallNanos / 1_000_000.0, callsPerFrame));
      }

      if (list.isEmpty()) {
         out.add("Нет данных: включите профайлер и поиграйте 10-20 секунд.");
      }
      return out;
   }

   public synchronized String statusLine() {
      double avgMs = this.averageFrameMs();
      double fps = avgMs > 0.0 ? 1000.0 / avgMs : 0.0;
      return String.format("enabled=%s | FPS avg=%.1f | avg=%.2fms p95=%.2fms worst=%.1fms | autoDump=%sms | spike=%sms factor=%.2f | frames=%d",
            this.enabled, fps, avgMs, this.percentileFrameMs(95), this.worstFrameNanos / 1_000_000.0,
            this.autoDumpIntervalMs, this.spikeMinMs, this.spikeFactor, this.windowFrameCount);
   }

   private String buildReport() {
      StringBuilder sb = new StringBuilder();
      SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      double windowSec = (System.currentTimeMillis() - this.windowStartMs) / 1000.0;
      double avgMs = this.averageFrameMs();
      double fps = avgMs > 0.0 ? 1000.0 / avgMs : 0.0;
      Runtime rt = Runtime.getRuntime();
      long used = (rt.totalMemory() - rt.freeMemory()) / (1024L * 1024L);
      long total = rt.totalMemory() / (1024L * 1024L);
      long max = rt.maxMemory() / (1024L * 1024L);
      long gcc = gcCount() - this.gcBaseCount;
      long gct = gcTime() - this.gcBaseTime;

      sb.append("==== RICH FPS PROFILE [").append(fmt.format(new Date())).append("] window=")
            .append(String.format("%.1f", windowSec)).append("s frames=").append(this.windowFrameCount).append(" ====\n");
      sb.append(String.format("FPS avg=%.1f | frame avg=%.2fms p50=%.2fms p95=%.2fms p99=%.2fms worst=%.1fms\n",
            fps, avgMs, this.percentileFrameMs(50), this.percentileFrameMs(95), this.percentileFrameMs(99), this.worstFrameNanos / 1_000_000.0));
      sb.append(String.format("Memory used=%dMB total=%dMB max=%dMB | GC count=%d time=%dms | skipped unfocused=%d\n",
            used, total, max, gcc, gct, this.skippedUnfocusedFrames));
      sb.append("World: ").append(this.worldSnapshot()).append("\n");
      sb.append(String.format("Config: autoDump=%dms spikeMin=%dms spikeFactor=%.2f\n",
            this.autoDumpIntervalMs, this.spikeMinMs, this.spikeFactor));
      sb.append("-- TOP SECTIONS (inclusive CPU time) --\n");
      sb.append(" #  section                                      totalMs  ms/frame   %frame  maxFrame  maxCall  calls  calls/f  frames%\n");

      List<Section> list = this.sortedSections();
      long denom = this.windowFrameCount > 0L ? this.windowFrameCount : 1L;
      int i = 0;
      for (Section s : list) {
         if (i >= 80) {
            break;
         }
         i++;
         double tot = s.totalNanos / 1_000_000.0;
         double perFrame = (s.totalNanos / (double)denom) / 1_000_000.0;
         double pct = avgMs > 0.0 ? perFrame / avgMs * 100.0 : 0.0;
         double callsPerFrame = s.totalCalls / (double)denom;
         double framesPct = s.framesPresent * 100.0 / (double)denom;
         sb.append(String.format("%2d. %-42s %8.1f %8.3f %7.1f %9.2f %8.3f %6d %8.2f %7.1f\n",
               i, trim(s.name, 42), tot, perFrame, pct, s.maxFrameNanos / 1_000_000.0,
               s.maxCallNanos / 1_000_000.0, s.totalCalls, callsPerFrame, framesPct));
      }

      if (list.isEmpty()) {
         sb.append("(no data)\n");
      }

      sb.append("\n");
      return sb.toString();
   }

   public synchronized String dumpToFile() {
      writeReport(this.buildReport());
      return LOG_DIR + "/" + REPORT_FILE;
   }

   private String worldSnapshot() {
      try {
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc == null) {
            return "mc=null";
         }
         if (mc.world == null) {
            return "world=null screen=" + (mc.currentScreen == null ? "null" : mc.currentScreen.getClass().getSimpleName());
         }

         int entities = 0;
         int living = 0;
         int players = 0;
         int visibleLiving = 0;
         for (Entity e : mc.world.getEntities()) {
            entities++;
            if (e instanceof LivingEntity) {
               living++;
               if (!e.isInvisible()) {
                  visibleLiving++;
               }
            }
            if (e instanceof PlayerEntity) {
               players++;
            }
         }

         String screen = mc.currentScreen == null ? "null" : mc.currentScreen.getClass().getSimpleName();
         String player = mc.player == null ? "null" : mc.player.getName().getString();
         return "entities=" + entities + " living=" + living + " visibleLiving=" + visibleLiving + " players=" + players + " screen=" + screen + " player=" + player;
      } catch (Throwable t) {
         return "snapshot-error=" + t.getClass().getSimpleName();
      }
   }

   private void logSpike(double frameMs, double avgMs) {
      try {
         List<Section> list = new ArrayList<>();
         for (Section s : this.sections.values()) {
            synchronized (s) {
               if (s.frameNanos > 0L) {
                  list.add(s);
               }
            }
         }
         list.sort(Comparator.comparingLong((Section s) -> s.frameNanos).reversed());

         SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss.SSS");
         StringBuilder sb = new StringBuilder();
         sb.append(String.format("[%s] SPIKE %.1fms (avg %.1fms, p95 %.1fms) | %s\n",
               fmt.format(new Date()), frameMs, avgMs, this.percentileFrameMs(95), this.worldSnapshot()));

         int i = 0;
         for (Section s : list) {
            if (i >= 12) {
               break;
            }
            i++;
            synchronized (s) {
               sb.append(String.format("  %2d. %-42s frame=%.3fms calls=%d maxCall=%.3fms\n",
                     i, trim(s.name, 42), s.frameNanos / 1_000_000.0, s.frameCalls, s.maxCallNanos / 1_000_000.0));
            }
         }
         sb.append("\n");
         appendText(SPIKE_FILE, sb.toString());
      } catch (Throwable ignored) {
      }
   }

   private static void writeReport(String text) {
      appendText(REPORT_FILE, text);
   }

   private void appendLine(String fileName, String line) {
      appendText(fileName, line + "\n");
   }

   private static void appendText(String fileName, String text) {
      try {
         File dir = new File(LOG_DIR);
         if (!dir.exists()) {
            dir.mkdirs();
         }
         try (PrintWriter w = new PrintWriter(new FileWriter(new File(dir, fileName), true))) {
            w.print(text);
         }
      } catch (IOException ignored) {
      }
   }

   private static long gcCount() {
      long c = 0L;
      for (GarbageCollectorMXBean b : ManagementFactory.getGarbageCollectorMXBeans()) {
         long v = b.getCollectionCount();
         if (v > 0L) {
            c += v;
         }
      }
      return c;
   }

   private static long gcTime() {
      long t = 0L;
      for (GarbageCollectorMXBean b : ManagementFactory.getGarbageCollectorMXBeans()) {
         long v = b.getCollectionTime();
         if (v > 0L) {
            t += v;
         }
      }
      return t;
   }

   private static String trim(String s, int len) {
      if (s == null) {
         return "";
      }
      return s.length() <= len ? s : s.substring(0, len);
   }
}