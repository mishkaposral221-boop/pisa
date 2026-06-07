package rich.util.animations;

public class InOutBack extends Animation {
   @Override
   public double calculation(double var1) {
      double var3 = var1 / this.ms;
      double var5 = 1.70158;
      double var7 = var5 * 1.525;
      return var3 < 0.5
         ? Math.pow(2.0 * var3, 2.0) * ((var7 + 1.0) * 2.0 * var3 - var7) / 2.0
         : (Math.pow(2.0 * var3 - 2.0, 2.0) * ((var7 + 1.0) * (var3 * 2.0 - 2.0) + var7) + 2.0) / 2.0;
   }
}
