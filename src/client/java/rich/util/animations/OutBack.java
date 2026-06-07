package rich.util.animations;

public class OutBack extends Animation {
   @Override
   public double calculation(double var1) {
      double var3 = var1 / this.ms;
      double var5 = 1.70158;
      double var7 = var5 + 1.0;
      return 1.0 + var7 * Math.pow(var3 - 1.0, 3.0) + var5 * Math.pow(var3 - 1.0, 2.0);
   }
}
