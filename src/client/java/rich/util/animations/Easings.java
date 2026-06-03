package rich.util.animations;

public final class Easings {
   public static final Easing LINEAR = var0 -> var0;
   public static final Easing QUAD_OUT = var0 -> 1.0 - Math.pow(1.0 - var0, 2.0);
   public static final Easing CUBIC_OUT = var0 -> 1.0 - Math.pow(1.0 - var0, 3.0);
   public static final Easing EXPO_IN = var0 -> var0 == 0.0 ? 0.0 : Math.pow(2.0, 10.0 * var0 - 10.0);
   public static final Easing EXPO_OUT = var0 -> var0 == 1.0 ? 1.0 : 1.0 - Math.pow(2.0, -10.0 * var0);
   public static final Easing EXPO_IN_OUT = var0 -> {
      if (var0 != 0.0 && var0 != 1.0) {
         return var0 < 0.5 ? Math.pow(2.0, 20.0 * var0 - 10.0) / 2.0 : (2.0 - Math.pow(2.0, -20.0 * var0 + 10.0)) / 2.0;
      } else {
         return var0;
      }
   };
   public static final Easing SINE_OUT = var0 -> Math.sin(var0 * Math.PI / 2.0);
   public static final Easing BACK_OUT = var0 -> {
      double var2 = 1.70158;
      double var4 = var2 + 1.0;
      return 1.0 + var4 * Math.pow(var0 - 1.0, 3.0) + var2 * Math.pow(var0 - 1.0, 2.0);
   };

   private Easings() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
