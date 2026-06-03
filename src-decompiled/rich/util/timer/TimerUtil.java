package rich.util.timer;

import java.time.Instant;

public class TimerUtil {
   private long lastMS = System.currentTimeMillis();
   private long startTime;

   public void reset() {
      this.lastMS = Instant.now().toEpochMilli();
   }

   public TimerUtil() {
      this.resetCounter();
   }

   public static TimerUtil create() {
      return new TimerUtil();
   }

   public void resetCounter() {
      this.lastMS = System.currentTimeMillis();
   }

   public boolean isReached(long var1) {
      return System.currentTimeMillis() - this.lastMS > var1;
   }

   public void setLastMS(long var1) {
      this.lastMS = System.currentTimeMillis() + var1;
   }

   public void setTime(long var1) {
      this.lastMS = var1;
   }

   public long getTime() {
      return System.currentTimeMillis() - this.lastMS;
   }

   public boolean isRunning() {
      return System.currentTimeMillis() - this.lastMS <= 0L;
   }

   public boolean hasTimeElapsed(long var1) {
      return System.currentTimeMillis() - this.lastMS > var1;
   }

   public boolean finished(double var1) {
      return System.currentTimeMillis() - var1 >= this.startTime;
   }

   public boolean hasTimeElapsed() {
      return this.lastMS < System.currentTimeMillis();
   }

   public long getLastMS() {
      return this.lastMS;
   }

   public long getStartTime() {
      return this.startTime;
   }
}
