package rich.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Lightweight micro-profiler for in-game performance debugging.
 *
 * Goals:
 * 1) Periodic summary (avg/max) for instrumented sections.
 * 2) Immediate "spike" logging when a single frame/callback is slow.
 *
 * Enable:
 *   -Drich.perfStats=true
 *
 * Optional:
 *   -Drich.perfStats.everyN=200           (periodic dump, default 200)
 *   -Drich.perfStats.spikeMs=22           (log any callback/frame slower than this, default 22ms ~ 45 FPS)
 *   -Drich.perfStats.top=8               (how many sections to show in spike report, default 8)
 *
 * Output goes to stdout with prefix [perf].
 */
public final class PerfStats {
    private static final boolean ENABLED = Boolean.parseBoolean(System.getProperty("rich.perfStats", "false"));
    private static final int EVERY_N = Integer.parseInt(System.getProperty("rich.perfStats.everyN", "200"));
    private static final double SPIKE_MS = Double.parseDouble(System.getProperty("rich.perfStats.spikeMs", "22"));
    private static final int TOP = Integer.parseInt(System.getProperty("rich.perfStats.top", "8"));

    private static int counter = 0;

    // Periodic summary (avg/max).
    private static final Map<String, Long> totalNs = new LinkedHashMap<>();
    private static final Map<String, Long> maxNs = new LinkedHashMap<>();
    private static final Map<String, Long> count = new LinkedHashMap<>();

    // "Current callback" breakdown (for spike printing).
    private static final Map<String, Long> currentTotalNs = new LinkedHashMap<>();
    private static long currentRootStartNs = 0L;
    private static String currentRootName = null;

    private PerfStats() {}

    public static boolean enabled() {
        return ENABLED;
    }

    public static long begin() {
        return ENABLED ? System.nanoTime() : 0L;
    }

    public static void end(String key, long startNs) {
        if (!ENABLED) return;
        long dt = System.nanoTime() - startNs;
        add(key, dt);
    }

    public static void add(String key, long dtNs) {
        if (!ENABLED) return;
        totalNs.put(key, totalNs.getOrDefault(key, 0L) + dtNs);
        count.put(key, count.getOrDefault(key, 0L) + 1L);
        long prevMax = maxNs.getOrDefault(key, 0L);
        if (dtNs > prevMax) maxNs.put(key, dtNs);
    }

    /**
     * Start measuring one "root" callback/frame.
     * Use together with rootEnd().
     */
    public static void rootBegin(String name) {
        if (!ENABLED) return;
        currentRootName = name;
        currentRootStartNs = System.nanoTime();
        currentTotalNs.clear();
    }

    /**
     * Add a section timing to the current root callback.
     */
    public static void section(String key, long dtNs) {
        if (!ENABLED) return;
        currentTotalNs.put(key, currentTotalNs.getOrDefault(key, 0L) + dtNs);
    }

    /**
     * Finish the current root callback. If it is a spike, print an immediate breakdown.
     */
    public static void rootEnd() {
        if (!ENABLED || currentRootName == null) return;
        long dtNs = System.nanoTime() - currentRootStartNs;
        double dtMs = dtNs / 1_000_000.0;

        // Always feed periodic stats for the root.
        add(currentRootName, dtNs);

        if (dtMs >= SPIKE_MS) {
            System.out.println(formatSpike(dtMs));
        }

        currentRootName = null;
        currentRootStartNs = 0L;
        currentTotalNs.clear();
    }

    private static String formatSpike(double rootMs) {
        // Sort sections by time desc.
        List<Map.Entry<String, Long>> entries = new ArrayList<>(currentTotalNs.entrySet());
        entries.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

        StringBuilder sb = new StringBuilder(512);
        sb.append("[perf] SPIKE ")
          .append(currentRootName)
          .append(" ")
          .append(String.format(Locale.ROOT, "%.3f", rootMs))
          .append("ms >= ")
          .append(String.format(Locale.ROOT, "%.3f", SPIKE_MS))
          .append("ms; top=")
          .append(TOP)
          .append(":");

        double accounted = 0.0;
        int shown = 0;
        for (Map.Entry<String, Long> e : entries) {
            if (shown >= TOP) break;
            double ms = e.getValue() / 1_000_000.0;
            accounted += ms;
            sb.append(" ")
              .append(e.getKey())
              .append("=")
              .append(String.format(Locale.ROOT, "%.3f", ms))
              .append("ms;");
            shown++;
        }

        double other = Math.max(0.0, rootMs - accounted);
        sb.append(" other=")
          .append(String.format(Locale.ROOT, "%.3f", other))
          .append("ms;");

        return sb.toString();
    }

    /**
     * Call once per frame/callback to periodically dump aggregated stats.
     */
    public static void tickAndMaybeDump() {
        if (!ENABLED) return;
        counter++;
        if (counter < EVERY_N) return;
        counter = 0;

        StringBuilder sb = new StringBuilder(512);
        sb.append("[perf] last ").append(EVERY_N).append(" calls:");

        for (String k : totalNs.keySet()) {
            long c = count.getOrDefault(k, 0L);
            long t = totalNs.getOrDefault(k, 0L);
            long m = maxNs.getOrDefault(k, 0L);
            if (c == 0) continue;

            double avgMs = (t / 1_000_000.0) / c;
            double maxMs = m / 1_000_000.0;

            sb.append(" ")
              .append(k)
              .append(" avg=")
              .append(String.format(Locale.ROOT, "%.3f", avgMs))
              .append("ms")
              .append(" max=")
              .append(String.format(Locale.ROOT, "%.3f", maxMs))
              .append("ms")
              .append(" n=")
              .append(c)
              .append(";");
        }

        System.out.println(sb);

        totalNs.clear();
        maxNs.clear();
        count.clear();
    }
}
