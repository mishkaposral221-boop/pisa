package rich.util.animations;

import rich.util.timer.TimerUtil;

public class Animation implements AnimationCalculation {
   public final TimerUtil counter = new TimerUtil();
   protected int ms;
   protected double value;
   protected Direction direction = Direction.FORWARDS;

   public void reset() {
      this.counter.resetCounter();
   }

   public void update() {
   }

   public boolean isDone() {
      return this.counter.isReached(this.ms);
   }

   public boolean isFinished(Direction var1) {
      return this.direction == var1 && this.isDone();
   }

   public Direction getDirection() {
      return this.direction;
   }

   public void setDirection(Direction var1) {
      if (this.direction != var1) {
         this.direction = var1;
         this.adjustTimer();
      }
   }

   public boolean isDirection(Direction var1) {
      return this.direction == var1;
   }

   private void adjustTimer() {
      this.counter.setTime(System.currentTimeMillis() - (this.ms - Math.min(this.ms, this.counter.getTime())));
   }

   public Double getOutput() {
      double var1 = (1.0 - this.calculation(this.counter.getTime())) * this.value;
      return this.direction == Direction.FORWARDS ? this.endValue() : (this.isDone() ? 0.0 : var1);
   }

   protected double endValue() {
      return this.isDone() ? this.value : this.calculation(this.counter.getTime()) * this.value;
   }

   public Animation setMs(int var1) {
      this.ms = var1;
      return this;
   }

   public Animation setValue(double var1) {
      this.value = var1;
      return this;
   }
}
