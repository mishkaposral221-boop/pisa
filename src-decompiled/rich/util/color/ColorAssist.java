package rich.util.color;

import java.awt.Color;
import net.minecraft.class_3532;
import net.minecraft.class_9848;
import rich.util.math.MathUtils;

public final class ColorAssist {
   public static final int green = -12517568;
   public static final int yellow = -192;
   public static final int orange = -32736;
   public static final int red = -49088;

   public static int colorForRectsCustom$() {
      return -10797100;
   }

   public static int colorForRectsBlack$() {
      return -15066598;
   }

   public static int colorForTextWhite$() {
      return -1;
   }

   public static int colorForTextCustom$() {
      return -8231726;
   }

   public static int red(int var0) {
      return var0 >> 16 & 0xFF;
   }

   public static int green(int var0) {
      return var0 >> 8 & 0xFF;
   }

   public static int blue(int var0) {
      return var0 & 0xFF;
   }

   public static int alpha(int var0) {
      return var0 >> 24 & 0xFF;
   }

   public static float redf(int var0) {
      return red(var0) / 255.0F;
   }

   public static float greenf(int var0) {
      return green(var0) / 255.0F;
   }

   public static float bluef(int var0) {
      return blue(var0) / 255.0F;
   }

   public static float alphaf(int var0) {
      return alpha(var0) / 255.0F;
   }

   public static int[] getRGB(int var0) {
      return new int[]{red(var0), green(var0), blue(var0)};
   }

   public static int getColor(int var0, int var1) {
      return getColor(var0, var0, var0, var1);
   }

   public static int getColor(int var0, float var1) {
      return getColor(var0, Math.round(var1 * 255.0F));
   }

   public static int getColor(int var0) {
      return getColor(var0, var0, var0);
   }

   public static int applyOpacity(int var0, float var1) {
      return class_9848.method_61324(
         (int)(class_9848.method_61320(var0) * (var1 / 255.0F)), class_9848.method_61327(var0), class_9848.method_61329(var0), class_9848.method_61331(var0)
      );
   }

   public static int calculateHuyDegrees(int var0, int var1) {
      return (int)((System.currentTimeMillis() / var0 + var1) % 360L);
   }

   public static int reAlphaInt(int var0, int var1) {
      return Math.clamp(var1, 0, 255) << 24 | var0 & 16777215;
   }

   public static int astolfo(int var0, int var1, float var2, float var3, float var4) {
      float var5 = (calculateHuyDegrees(var0, var1) + var1 * 90.0F) % 360.0F / 360.0F;
      int var6 = Color.HSBtoRGB(var5, Math.clamp(var2, 0.0F, 1.0F), Math.clamp(var3, 0.0F, 1.0F));
      return reAlphaInt(var6, Math.clamp((int)(var4 * 255.0F), 0, 255));
   }

   public static int toColor(String var0) {
      return setAlpha(Integer.parseInt(var0.substring(1), 16), 255);
   }

   public static int setAlpha(int var0, int var1) {
      return var0 & 16777215 | var1 << 24;
   }

   public static int interpolateColor(int var0, int var1, float var2) {
      var2 = Math.min(1.0F, Math.max(0.0F, var2));
      return getColor(
         interpolateInt(getRed(var0), getRed(var1), var2),
         interpolateInt(getGreen(var0), getGreen(var1), var2),
         interpolateInt(getBlue(var0), getBlue(var1), var2),
         interpolateInt(getAlpha(var0), getAlpha(var1), var2)
      );
   }

   public static Double interpolateD(double var0, double var2, double var4) {
      return var0 + (var2 - var0) * var4;
   }

   public static int interpolateInt(int var0, int var1, double var2) {
      return (int)(var0 + (var1 - var0) * var2);
   }

   public static int getRed(int var0) {
      return var0 >> 16 & 0xFF;
   }

   public static int getGreen(int var0) {
      return var0 >> 8 & 0xFF;
   }

   public static int getBlue(int var0) {
      return var0 & 0xFF;
   }

   public static int getAlpha(int var0) {
      return var0 >> 24 & 0xFF;
   }

   public static int overCol(int var0, int var1, float var2) {
      float var3 = class_3532.method_15363(var2, 0.0F, 1.0F);
      return getColor(
         class_3532.method_48781(var3, red(var0), red(var1)),
         class_3532.method_48781(var3, green(var0), green(var1)),
         class_3532.method_48781(var3, blue(var0), blue(var1)),
         class_3532.method_48781(var3, alpha(var0), alpha(var1))
      );
   }

   public static int multRedAndAlpha(int var0, float var1, float var2) {
      return getColor(red(var0), Math.min(255, Math.round(green(var0) / var1)), Math.min(255, Math.round(blue(var0) / var1)), Math.round(alpha(var0) * var2));
   }

   public static int rgba(int var0, int var1, int var2, int var3) {
      return getColor(var0, var1, var2, var3);
   }

   public static int[] genGradientForText(int var0, int var1, int var2) {
      int[] var3 = new int[var2];

      for (int var4 = 0; var4 < var2; var4++) {
         var3[var4] = overCol(var0, var1, (float)var4 / (var2 - 1));
      }

      return var3;
   }

   public static float[] rgba(int var0) {
      return new float[]{(var0 >> 16 & 0xFF) / 255.0F, (var0 >> 8 & 0xFF) / 255.0F, (var0 & 0xFF) / 255.0F, (var0 >> 24 & 0xFF) / 255.0F};
   }

   public static int interpolate(int var0, int var1, float var2) {
      float[] var3 = rgba(var0);
      float[] var4 = rgba(var1);
      return rgba(
         (int)MathUtils.interpolate(var3[0] * 255.0F, var4[0] * 255.0F, var2),
         (int)MathUtils.interpolate(var3[1] * 255.0F, var4[1] * 255.0F, var2),
         (int)MathUtils.interpolate(var3[2] * 255.0F, var4[2] * 255.0F, var2),
         (int)MathUtils.interpolate(var3[3] * 255.0F, var4[3] * 255.0F, var2)
      );
   }

   public static int getColor(int var0, int var1, int var2, int var3) {
      return class_3532.method_15340(var3, 0, 255) << 24
         | class_3532.method_15340(var0, 0, 255) << 16
         | class_3532.method_15340(var1, 0, 255) << 8
         | class_3532.method_15340(var2, 0, 255);
   }

   public static int getColor(int var0, int var1, int var2) {
      return getColor(var0, var1, var2, 255);
   }

   public static String formatting(int var0) {
      return "⏏" + var0 + "⏏";
   }

   private ColorAssist() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
