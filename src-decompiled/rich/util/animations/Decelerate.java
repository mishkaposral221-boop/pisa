package rich.util.animations;

public class Decelerate extends Animation {
   @Override
   public double calculation(double var1) {
      double var3 = var1 / this.ms;
      return 1.0 - (var3 - 1.0) * (var3 - 1.0);
   }
}
