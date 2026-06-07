package rich.util.timer;

public class StopWatch {
   private long startTime;

   public StopWatch() {
      this.reset();
   }

   public boolean finished(double var1) {
      return System.currentTimeMillis() - var1 >= this.startTime;
   }

   public boolean every(double var1) {
      boolean var3 = this.finished(var1);
      if (var3) {
         this.reset();
      }

      return var3;
   }

   public void reset() {
      this.startTime = System.currentTimeMillis();
   }

   public long elapsedTime() {
      return System.currentTimeMillis() - this.startTime;
   }

   public StopWatch setMs(long var1) {
      this.startTime = System.currentTimeMillis() - var1;
      return this;
   }

   public long getStartTime() {
      return this.startTime;
   }
}
