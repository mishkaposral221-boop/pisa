package rich.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lightweight micro-profiler for in-game performance debugging.
 *
 * Usage:
 * - Enable with JVM flag: -Drich.perfStats=true
 * - Optional: -Drich.perfStats.everyN=200 (default 200)
 *
 * Output goes to stdout with prefix [perf].
 */
public final class PerfStats {
    private static final boolean ENABLED = Boolean.parseBoolean(System.getProperty("rich.perfStats", "false"));
    private static final int EVERY_N = Integer.parseInt(System.getProperty("rich.perfStats.everyN", "200"));

    private static int counter = 0;

    // Keep insertion order so the log is stable.
    private static final Map<String, Long> totalNs = new LinkedHashMap<>();
    private static final Map<String, Long> maxNs = new LinkedHashMap<>();
    private static final Map<String, Long> count = new LinkedHashMap<>();

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
     * Call once per frame (or once per render callback) to periodically dump stats.
     */
    public static void tickAndMaybeDump() {
        if (!ENABLED) return;
        counter++;
        if (counter < EVERY_N) return;
        counter = 0;

        StringBuilder sb = new StringBuilder(256);
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
              .append(String.format(java.util.Locale.ROOT, "%.3f", avgMs))
              .append("ms")
              .append(" max=")
              .append(String.format(java.util.Locale.ROOT, "%.3f", maxMs))
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
