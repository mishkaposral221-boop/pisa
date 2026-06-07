package rich.util.config.impl.autosaver;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import rich.util.config.impl.consolelogger.Logger;

public class ConfigAutoSaver {
   private static final long SAVE_INTERVAL_MS = 90000L;
   private static final long INITIAL_DELAY_MS = 90000L;
   private final ScheduledExecutorService executor;
   private final Runnable saveTask;
   private final AtomicBoolean running;
   private final AtomicLong lastSaveTime;
   private ScheduledFuture<?> scheduledTask;

   public ConfigAutoSaver(Runnable var1) {
      this.saveTask = var1;
      this.running = new AtomicBoolean(false);
      this.lastSaveTime = new AtomicLong(0L);
      this.executor = Executors.newSingleThreadScheduledExecutor(var0 -> {
         Thread var1x = new Thread(var0, "RunTimeVisuals-ConfigAutoSaver");
         var1x.setDaemon(true);
         var1x.setPriority(1);
         return var1x;
      });
   }

   public void start() {
      if (this.running.compareAndSet(false, true)) {
         this.scheduledTask = this.executor.scheduleAtFixedRate(this::executeSave, 90000L, 90000L, TimeUnit.MILLISECONDS);
         Logger.info("AutoConfiguration: AutoSaver started (interval: 90s)");
      }
   }

   private void executeSave() {
      if (this.running.get()) {
         try {
            this.saveTask.run();
            this.lastSaveTime.set(System.currentTimeMillis());
         } catch (Exception var2) {
            Logger.error("AutoConfiguration: AutoSave failed! " + var2.getMessage());
         }
      }
   }

   public void stop() {
      this.running.set(false);
      if (this.scheduledTask != null) {
         this.scheduledTask.cancel(false);
      }
   }

   public void shutdown() {
      this.stop();
      this.executor.shutdown();

      try {
         if (!this.executor.awaitTermination(3L, TimeUnit.SECONDS)) {
            this.executor.shutdownNow();
         }
      } catch (InterruptedException var2) {
         this.executor.shutdownNow();
         Thread.currentThread().interrupt();
      }
   }

   public long getLastSaveTime() {
      return this.lastSaveTime.get();
   }

   public boolean isRunning() {
      return this.running.get();
   }
}
