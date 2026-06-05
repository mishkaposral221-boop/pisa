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

/**
 * Lightweight per-frame CPU profiler for the client.
 *
 * <p>Goal: find out WHAT is causing FPS drops by measuring how much time each
 * module / event handler / named section consumes per rendered frame, and dump
 * full statistics to a log file (and/or chat).
 *
 * <p>Design notes:
 * <ul>
 *   <li>{@link #record(String, long)} accumulates a measured duration for a named
 *       section. {@link #begin(String)}/{@link #end()} measure a (possibly nested)
 *       section on the current thread.</li>
 *   <li>{@link #frameEnd()} is called once per rendered frame. It computes frame
 *       time / FPS, detects spikes, folds the per-frame section totals into rolling
 *       aggregates and clears the per-frame counters.</li>
 *   <li>Hot-path work is allocation free; reports allocate.</li>
 *   <li>Disabled by default; when disabled begin/end/record are cheap no-ops.</li>
 * </ul>
 */
public final class FrameProfiler {
   private static final FrameProfiler INSTANCE = new FrameProfiler();

   public static FrameProfiler getInstance() {
      return INSTANCE;
   }

   // ---- configuration ----
   private volatile boolean enabled = false;
   /** How often (ms) to auto-dump a report to the log file. 0 disables auto-dump. */
   private volatile long autoDumpIntervalMs = 10_000L;
   /** How many recent frames to keep for frame-time percentiles. */
   private static final int FRAME_WINDOW = 1200;
   /** Absolute frame-time (ms) above which a frame is treated as a spike. */
   private static final long SPIKE_MIN_MS = 50L;
   /** Relative threshold: frame slower than avg * factor is a spike. */
   private static final double SPIKE_FACTOR = 3.0;
   /** Minimum gap (ms) between two spike log lines to avoid spamming. */
   private static final long SPIKE_LOG_MIN_GAP_MS = 200L;

   private static final String LOG_DIR = "logs";
   private static final String REPORT_FILE = "rich-fps-profile.log";
   private static final String SPIKE_FILE = "rich-fps-spikes.log";

   private static final class Section {
      final String name;
      // accumulated within the current (not yet ended) frame
      long frameNanos;
      int frameCalls;
      // rolling totals across all frames since last reset
      long totalNanos;
      long totalCalls;
      long maxCallNanos;  // worst single invocation
      long maxFrameNanos; // worst single-frame total
      long framesPresent; // number of frames in which this section ran

      Section(String name) {
         this.name = name;
      }
   }

   private final Map<String, Section> sections = new ConcurrentHashMap<>();
   private final ThreadLocal<ArrayDeque<Object[]>> stack = ThreadLocal.withInitial(ArrayDeque::new);

   // frame timing
   private long renderStartNano = 0L;
   private long lastFrameEndNano = 0L;
   private final long[] frameTimes = new long[FRAME_WINDOW];
   private int frameTimeIdx = 0;
   private int frameTimeCount = 0;
   private long windowFrameCount = 0L;
   private long worstFrameNanos = 0L;

   // window bookkeeping
   private long windowStartMs = 0L;
   private long lastDumpMs = 0L;
   private long lastSpikeLogMs = 0L;

   // gc baseline
   private long gcBaseCount = 0L;
   private long gcBaseTime = 0L;

   private FrameProfiler() {
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public synchronized void setEnabled(boolean value) {
      if (value && !this.enabled) {
         this.resetInternal();
      }
      this.enabled = value;
   }

   public synchronized void reset() {
      this.resetInternal();
   }

   private void resetInternal() {
      this.sections.clear();
      this.frameTimeIdx = 0;
      this.frameTimeCount = 0;
      this.windowFrameCount = 0L;
      this.worstFrameNanos = 0L;
      this.lastFrameEndNano = 0L;
      long now = System.currentTimeMillis();
      this.windowStartMs = now;
      this.lastDumpMs = now;
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

   /** Record a measured duration (nanos) for a named section. */
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

   /** Begin a (possibly nested) timed section on the current thread. */
   public void begin(String name) {
      if (!this.enabled || name == null) {
         return;
      }
      this.stack.get().push(new Object[]{name, System.nanoTime()});
   }

   /** End the most recently begun section on the current thread. */
   public void end() {
      if (!this.enabled) {
         return;
      }
      ArrayDeque<Object[]> st = this.stack.get();
      Object[] top = st.poll();
      if (top == null) {
         return;
      }
      long nanos = System.nanoTime() - (Long) top[1];
      this.record((String) top[0], nanos);
   }

   /** Called at render() HEAD. */
   public void beginFrame() {
      if (!this.enabled) {
         return;
      }
      this.renderStartNano = System.nanoTime();
   }

   /** Called at render() TAIL: rolls up the frame. */
   public synchronized void frameEnd() {
      if (!this.enabled) {
         return;
      }
      long now = System.nanoTime();
      if (this.renderStartNano > 0L) {
         this.record("Frame/render(total)", now - this.renderStartNano);
      }

      long frameNanos = this.lastFrameEndNano > 0L ? now - this.lastFrameEndNano : 0L;
      this.lastFrameEndNano = now;

      if (frameNanos > 0L) {
         double avgMs = this.averageFrameMs();
         double frameMs = frameNanos / 1_000_000.0;
         long gapMs = System.currentTimeMillis() - this.lastSpikeLogMs;
         boolean absSpike = frameMs >= SPIKE_MIN_MS;
         boolean relSpike = this.frameTimeCount > 30 && avgMs > 0.0 && frameMs >= avgMs * SPIKE_FACTOR;
         if (gapMs >= SPIKE_LOG_MIN_GAP_MS && (absSpike || relSpike)) {
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

      // fold per-frame section totals into rolling aggregates
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
      int idx = (int) Math.ceil(p / 100.0 * this.frameTimeCount) - 1;
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

   /** Top N sections formatted for chat output (Russian). */
   public synchronized List<String> topLines(int n) {
      List<String> out = new ArrayList<>();
      if (!this.enabled && this.sections.isEmpty()) {
         out.add("Профилировщик выключен и данных нет. Включите: .profiler on");
         return out;
      }
      double avgMs = this.averageFrameMs();
      double fps = avgMs > 0.0 ? 1000.0 / avgMs : 0.0;
      out.add(String.format("FPS avg=%.1f  кадр avg=%.2fms p95=%.2fms худший=%.1fms  кадров=%d",
            fps, avgMs, this.percentileFrameMs(95), this.worstFrameNanos / 1_000_000.0, this.windowFrameCount));
      List<Section> list = this.sortedSections();
      long denom = this.windowFrameCount > 0L ? this.windowFrameCount : 1L;
      int i = 0;
      for (Section s : list) {
         if (i >= n) {
            break;
         }
         i++;
         double perFrame = (s.totalNanos / (double) denom) / 1_000_000.0;
         double pct = avgMs > 0.0 ? perFrame / avgMs * 100.0 : 0.0;
         out.add(String.format("%2d. %s  %.2fms/кадр (%.0f%%) max=%.2fms",
               i, trim(s.name, 32), perFrame, pct, s.maxFrameNanos / 1_000_000.0));
      }
      if (list.isEmpty()) {
         out.add("Нет данных (поиграйте несколько секунд при включённом профайлере).");
      }
      return out;
   }

   private String buildReport() {
      StringBuilder sb = new StringBuilder();
      SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      double windowSec = (System.currentTimeMillis() - this.windowStartMs) / 1000.0;
      double avgMs = this.averageFrameMs();
      double fps = avgMs > 0.0 ? 1000.0 / avgMs : 0.0;
      Runtime rt = Runtime.getRuntime();
      long used = (rt.totalMemory() - rt.freeMemory()) / (1024L * 1024L);
      long max = rt.maxMemory() / (1024L * 1024L);
      long gcc = gcCount() - this.gcBaseCount;
      long gct = gcTime() - this.gcBaseTime;

      sb.append("==== RICH FPS PROFILE [").append(fmt.format(new Date())).append("] окно=")
            .append(String.format("%.1f", windowSec)).append("s кадров=").append(this.windowFrameCount).append(" ====\n");
      sb.append(String.format("FPS avg=%.1f  кадр: avg=%.2fms p95=%.2fms p99=%.2fms худший=%.1fms\n",
            fps, avgMs, this.percentileFrameMs(95), this.percentileFrameMs(99), this.worstFrameNanos / 1_000_000.0));
      sb.append(String.format("Память: %dMB / %dMB   GC за окно: count=%d time=%dms\n", used, max, gcc, gct));
      sb.append("-- Топ секций по суммарному CPU за окно (инклюзивно) --\n");

      List<Section> list = this.sortedSections();
      long denom = this.windowFrameCount > 0L ? this.windowFrameCount : 1L;
      int i = 0;
      for (Section s : list) {
         if (i >= 40) {
            break;
         }
         i++;
         double tot = s.totalNanos / 1_000_000.0;
         double perFrame = (s.totalNanos / (double) denom) / 1_000_000.0;
         double pct = avgMs > 0.0 ? perFrame / avgMs * 100.0 : 0.0;
         sb.append(String.format("%2d. %-38s tot=%8.1fms  avg=%6.3fms/f (%4.1f%%)  maxf=%6.2fms  maxcall=%6.3fms  calls=%d\n",
               i, trim(s.name, 38), tot, perFrame, pct, s.maxFrameNanos / 1_000_000.0, s.maxCallNanos / 1_000_000.0, s.totalCalls));
      }
      if (list.isEmpty()) {
         sb.append("(нет данных)\n");
      }
      sb.append("\n");
      return sb.toString();
   }

   /** Build a report and append it to the log file; returns the relative path. */
   public synchronized String dumpToFile() {
      writeReport(this.buildReport());
      return LOG_DIR + "/" + REPORT_FILE;
   }

   private void logSpike(double frameMs, double avgMs) {
      try {
         List<Section> list = new ArrayList<>();
         for (Section s : this.sections.values()) {
            if (s.frameNanos > 0L) {
               list.add(s);
            }
         }
         list.sort(Comparator.comparingLong((Section s) -> s.frameNanos).reversed());
         SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss.SSS");
         StringBuilder sb = new StringBuilder();
         sb.append(String.format("[%s] СПАЙК %.1fms (avg %.1fms) -> ", fmt.format(new Date()), frameMs, avgMs));
         int i = 0;
         for (Section s : list) {
            if (i >= 6) {
               break;
            }
            i++;
            sb.append(String.format("%s=%.2fms; ", trim(s.name, 30), s.frameNanos / 1_000_000.0));
         }
         sb.append("\n");
         File dir = new File(LOG_DIR);
         if (!dir.exists()) {
            dir.mkdirs();
         }
         try (PrintWriter w = new PrintWriter(new FileWriter(new File(dir, SPIKE_FILE), true))) {
            w.print(sb);
         }
      } catch (Throwable ignored) {
      }
   }

   private static void writeReport(String text) {
      try {
         File dir = new File(LOG_DIR);
         if (!dir.exists()) {
            dir.mkdirs();
         }
         try (PrintWriter w = new PrintWriter(new FileWriter(new File(dir, REPORT_FILE), true))) {
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
