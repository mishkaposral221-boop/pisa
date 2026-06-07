package rich.util.profiler;

/**
 * Automatically mirrors the manual profiling flow around server sessions:
 * - on server join: .profiler on 5
 * - on server disconnect: .profiler dump
 *
 * Kept separate from command parsing so it still works while disconnecting,
 * when chat/player objects may already be gone.
 */
public final class ProfilerAutoSession {
   private static boolean activeSession = false;
   private static long lastJoinMs = 0L;
   private static long lastDumpMs = 0L;

   private ProfilerAutoSession() {
   }

   public static synchronized void onServerJoin() {
      long now = System.currentTimeMillis();
      if (now - lastJoinMs < 1000L) {
         return;
      }
      lastJoinMs = now;

      FrameProfiler profiler = FrameProfiler.getInstance();
      profiler.setAutoDumpIntervalSeconds(5L);
      profiler.setEnabled(true);
      activeSession = true;
   }

   public static synchronized void onServerDisconnect() {
      if (!activeSession) {
         return;
      }

      long now = System.currentTimeMillis();
      if (now - lastDumpMs < 1000L) {
         return;
      }
      lastDumpMs = now;

      FrameProfiler profiler = FrameProfiler.getInstance();
      try {
         profiler.dumpToFile();
      } catch (Throwable ignored) {
      }
      activeSession = false;
   }
}
