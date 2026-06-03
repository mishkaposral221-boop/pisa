package rich.util.animations;

public class EaseInOutQuad extends Animation {
   @Override
   public double calculation(double var1) {
      double var3 = var1 / this.ms;
      return var3 < 0.5 ? 2.0 * var3 * var3 : 1.0 - Math.pow(-2.0 * var3 + 2.0, 2.0) / 2.0;
   }
}
