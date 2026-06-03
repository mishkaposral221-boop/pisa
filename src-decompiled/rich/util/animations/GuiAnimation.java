package rich.util.animations;

import rich.util.timer.TimerUtil;

public class GuiAnimation {
   public final TimerUtil counter = new TimerUtil();
   protected int ms = 250;
   protected double value = 1.0;
   protected Direction direction = Direction.FORWARDS;

   public GuiAnimation reset() {
      this.counter.resetCounter();
      return this;
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

   public GuiAnimation setDirection(Direction var1) {
      if (this.direction != var1) {
         this.direction = var1;
      }

      return this;
   }

   public Double getOutput() {
      double var1 = Math.min(1.0, (double)this.counter.getTime() / this.ms);
      double var3 = this.easeOutQuart(var1);
      return this.direction == Direction.FORWARDS ? var3 * this.value : (1.0 - var3) * this.value;
   }

   private double easeOutQuart(double var1) {
      return 1.0 - Math.pow(1.0 - var1, 4.0);
   }

   public GuiAnimation setMs(int var1) {
      this.ms = var1;
      return this;
   }

   public GuiAnimation setValue(double var1) {
      this.value = var1;
      return this;
   }
}
