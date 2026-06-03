package rich.util.animations;

public class FadeAnimation {
   private final long duration;
   private long startTime;
   private boolean forwards = true;
   private double value = 0.0;
   private Easing easing = Easings.EXPO_OUT;

   public FadeAnimation(long var1) {
      this.duration = var1;
      this.startTime = System.currentTimeMillis();
   }

   public FadeAnimation(long var1, Easing var3) {
      this.duration = var1;
      this.startTime = System.currentTimeMillis();
      this.easing = var3;
   }

   public void switchDirection(boolean var1) {
      if (this.forwards != var1) {
         long var2 = System.currentTimeMillis() - this.startTime;
         long var4 = this.duration - Math.min(var2, this.duration);
         this.startTime = System.currentTimeMillis() - var4;
         this.forwards = var1;
      }
   }

   public void setDirection(boolean var1) {
      this.forwards = var1;
   }

   public void reset() {
      this.startTime = System.currentTimeMillis();
      this.value = this.forwards ? 0.0 : 1.0;
   }

   public float get() {
      long var1 = System.currentTimeMillis() - this.startTime;
      double var3 = Math.min((double)var1 / this.duration, 1.0);
      double var5 = this.easing.ease(var3);
      if (this.forwards) {
         this.value = var5;
      } else {
         this.value = 1.0 - var5;
      }

      return (float)Math.max(0.0, Math.min(1.0, this.value));
   }

   public boolean isDone() {
      return System.currentTimeMillis() - this.startTime >= this.duration;
   }

   public boolean isFullyHidden() {
      return this.isDone() && !this.forwards;
   }

   public boolean isFullyVisible() {
      return this.isDone() && this.forwards;
   }

   public long getDuration() {
      return this.duration;
   }

   public long getStartTime() {
      return this.startTime;
   }

   public boolean isForwards() {
      return this.forwards;
   }

   public double getValue() {
      return this.value;
   }

   public Easing getEasing() {
      return this.easing;
   }
}
