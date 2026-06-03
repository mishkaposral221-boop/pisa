package rich.util.animations;

public class InOutCirc extends Animation {
   @Override
   public double calculation(double var1) {
      double var3 = var1 / this.ms;
      return var3 < 0.5 ? (1.0 - Math.sqrt(1.0 - Math.pow(2.0 * var3, 2.0))) / 2.0 : (Math.sqrt(1.0 - Math.pow(-2.0 * var3 + 2.0, 2.0)) + 1.0) / 2.0;
   }
}
