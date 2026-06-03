package rich.util.string.chat.helper;

import net.minecraft.class_2561;
import net.minecraft.class_5250;
import rich.util.color.ColorAssist;

public class TextHelper {
   public static class_2561 applyGradient(String var0, TextHelper.GradientStyle var1, int var2, int var3, boolean var4) {
      switch (var1) {
         case HALF_SPLIT:
            return halfSplitGradient(var0, var2, var3, var4);
         case FULL_GRADIENT:
            return fullGradient(var0, var2, var3, var4);
         case ASTOLFO:
            return astolfoGradient(var0, var4);
         case TWO_COLOR_FADE:
            return twoColorFade(var0, var2, var3, var4);
         default:
            return class_2561.method_43470(var0).method_27694(var2x -> var2x.method_36139(var2).method_10982(var4));
      }
   }

   private static class_2561 halfSplitGradient(String var0, int var1, int var2, boolean var3) {
      class_5250 var4 = class_2561.method_43470("");
      int var5 = var0.length() / 2;

      for (int var6 = 0; var6 < var0.length(); var6++) {
         int var7 = var6 < var5 ? var1 : var2;
         var4.method_10852(class_2561.method_43470(String.valueOf(var0.charAt(var6))).method_27694(var2x -> var2x.method_36139(var7).method_10982(var3)));
      }

      return var4;
   }

   private static class_2561 fullGradient(String var0, int var1, int var2, boolean var3) {
      class_5250 var4 = class_2561.method_43470("");

      for (int var5 = 0; var5 < var0.length(); var5++) {
         float var6 = (float)var5 / (var0.length() - 1);
         int var7 = ColorAssist.interpolate(var1, var2, var6);
         var4.method_10852(class_2561.method_43470(String.valueOf(var0.charAt(var5))).method_27694(var2x -> var2x.method_36139(var7).method_10982(var3)));
      }

      return var4;
   }

   private static class_2561 astolfoGradient(String var0, boolean var1) {
      class_5250 var2 = class_2561.method_43470("");

      for (int var3 = 0; var3 < var0.length(); var3++) {
         int var4 = ColorAssist.astolfo(10, var3, 0.7F, 0.7F, 1.0F);
         var2.method_10852(class_2561.method_43470(String.valueOf(var0.charAt(var3))).method_27694(var2x -> var2x.method_36139(var4).method_10982(var1)));
      }

      return var2;
   }

   private static class_2561 twoColorFade(String var0, int var1, int var2, boolean var3) {
      class_5250 var4 = class_2561.method_43470("");

      for (int var5 = 0; var5 < var0.length(); var5++) {
         float var6 = (float)var5 / (var0.length() - 1);
         int var7 = ColorAssist.interpolateColor(var1, var2, var6);
         var4.method_10852(class_2561.method_43470(String.valueOf(var0.charAt(var5))).method_27694(var2x -> var2x.method_36139(var7).method_10982(var3)));
      }

      return var4;
   }

   public static class_2561 applyPredefinedGradient(String var0, String var1, boolean var2) {
      switch (var1.toLowerCase()) {
         case "red_blue":
            return applyGradient(var0, TextHelper.GradientStyle.HALF_SPLIT, -49088, ColorAssist.toColor("#0000FF"), var2);
         case "green_purple":
            return applyGradient(var0, TextHelper.GradientStyle.HALF_SPLIT, -12517568, ColorAssist.toColor("#800080"), var2);
         case "yellow_cyan":
            return applyGradient(var0, TextHelper.GradientStyle.FULL_GRADIENT, -192, ColorAssist.toColor("#00FFFF"), var2);
         case "orange_magenta":
            return applyGradient(var0, TextHelper.GradientStyle.FULL_GRADIENT, -32736, ColorAssist.toColor("#FF00FF"), var2);
         case "astolfo":
            return applyGradient(var0, TextHelper.GradientStyle.ASTOLFO, 0, 0, var2);
         case "blue_green_fade":
            return applyGradient(var0, TextHelper.GradientStyle.TWO_COLOR_FADE, ColorAssist.toColor("#0000FF"), -12517568, var2);
         case "purple_red_fade":
            return applyGradient(var0, TextHelper.GradientStyle.TWO_COLOR_FADE, ColorAssist.toColor("#800080"), -49088, var2);
         case "cyan_orange_fade":
            return applyGradient(var0, TextHelper.GradientStyle.TWO_COLOR_FADE, ColorAssist.toColor("#00FFFF"), -32736, var2);
         case "white_black":
            return applyGradient(var0, TextHelper.GradientStyle.FULL_GRADIENT, ColorAssist.colorForTextWhite$(), ColorAssist.colorForRectsBlack$(), var2);
         case "custom_purple":
            return applyGradient(var0, TextHelper.GradientStyle.FULL_GRADIENT, ColorAssist.colorForTextCustom$(), ColorAssist.colorForRectsCustom$(), var2);
         case "black_light_purple":
            return applyGradient(var0, TextHelper.GradientStyle.FULL_GRADIENT, ColorAssist.colorForRectsBlack$(), ColorAssist.toColor("#DA70D6"), var2);
         case "dark_red_bright_red":
            return applyGradient(var0, TextHelper.GradientStyle.FULL_GRADIENT, ColorAssist.toColor("#8B0000"), -49088, var2);
         case "dark_red":
            return applyGradient(var0, TextHelper.GradientStyle.HALF_SPLIT, ColorAssist.toColor("#8B0000"), ColorAssist.toColor("#8B0000"), var2);
         case "red_white":
            return applyGradient(var0, TextHelper.GradientStyle.HALF_SPLIT, -49088, ColorAssist.colorForTextWhite$(), var2);
         case "purple_bright_pink":
            return applyGradient(var0, TextHelper.GradientStyle.FULL_GRADIENT, ColorAssist.toColor("#800080"), ColorAssist.toColor("#FF69B4"), var2);
         case "pink_dark_pink":
            return applyGradient(var0, TextHelper.GradientStyle.FULL_GRADIENT, ColorAssist.toColor("#FFC1CC"), ColorAssist.toColor("#C71585"), var2);
         case "bright_red":
            return applyGradient(var0, TextHelper.GradientStyle.HALF_SPLIT, -49088, -49088, var2);
         case "dark_green_bright_green":
            return applyGradient(var0, TextHelper.GradientStyle.FULL_GRADIENT, ColorAssist.toColor("#006400"), -12517568, var2);
         case "red_orange":
            return applyGradient(var0, TextHelper.GradientStyle.FULL_GRADIENT, -49088, -32736, var2);
         case "turquoise_blue":
            return applyGradient(var0, TextHelper.GradientStyle.FULL_GRADIENT, ColorAssist.toColor("#40E0D0"), ColorAssist.toColor("#0000FF"), var2);
         default:
            return class_2561.method_43470(var0).method_27694(var1x -> var1x.method_36139(ColorAssist.colorForTextWhite$()).method_10982(var2));
      }
   }

   public enum GradientStyle {
      HALF_SPLIT,
      FULL_GRADIENT,
      ASTOLFO,
      TWO_COLOR_FADE;
   }
}
