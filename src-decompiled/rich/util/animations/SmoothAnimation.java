package rich.util.animations;

public class SmoothAnimation {
   private long start;
   private double duration;
   private double fromValue;
   private double toValue;
   private double value;
   private double prevValue;
   private Easing easing = Easings.EXPO_OUT;
   private boolean finished = false;

   public SmoothAnimation run(double var1, double var3) {
      return this.run(var1, var3, Easings.EXPO_OUT, false);
   }

   public SmoothAnimation run(double var1, double var3, Easing var5) {
      return this.run(var1, var3, var5, false);
   }

   public SmoothAnimation run(double var1, double var3, boolean var5) {
      return this.run(var1, var3, Easings.EXPO_OUT, var5);
   }

   public SmoothAnimation run(double var1, double var3, Easing var5, boolean var6) {
      if (this.check(var6, var1)) {
         return this;
      }

      this.easing = var5;
      this.start = System.currentTimeMillis();
      this.duration = var3 * 1000.0;
      this.fromValue = this.value;
      this.toValue = var1;
      this.finished = this.fromValue == this.toValue;
      return this;
   }

   public boolean update() {
      this.prevValue = this.value;
      boolean var1 = this.isAlive();
      if (System.currentTimeMillis() - this.start > this.duration / 1.5) {
         this.finished = this.fromValue == this.toValue;
      }

      if (var1) {
         double var2 = Math.min(1.0, Math.max(0.0, this.calculatePart()));
         this.value = this.interpolate(this.fromValue, this.toValue, this.easing.ease(var2));
      } else {
         this.start = 0L;
         this.value = this.toValue;
      }

      return var1;
   }

   public boolean isAlive() {
      return !this.isFinished();
   }

   public boolean isFinished() {
      return this.calculatePart() >= 1.0;
   }

   public double calculatePart() {
      return this.duration <= 0.0 ? 1.0 : (System.currentTimeMillis() - this.start) / this.duration;
   }

   public boolean check(boolean var1, double var2) {
      return var1 && this.isAlive() && (var2 == this.fromValue || var2 == this.toValue || var2 == this.value);
   }

   public double interpolate(double var1, double var3, double var5) {
      return var1 + (var3 - var1) * var5;
   }

   public float get() {
      return (float)this.value;
   }

   public float getPrev() {
      return (float)this.prevValue;
   }

   public void set(double var1) {
      this.run(var1, 1.0E-4);
      this.update();
      this.value = var1;
   }

   public long getStart() {
      return this.start;
   }

   public double getDuration() {
      return this.duration;
   }

   public double getFromValue() {
      return this.fromValue;
   }

   public double getToValue() {
      return this.toValue;
   }

   public double getValue() {
      return this.value;
   }

   public double getPrevValue() {
      return this.prevValue;
   }

   public Easing getEasing() {
      return this.easing;
   }

   public void setValue(double var1) {
      this.value = var1;
   }
}
