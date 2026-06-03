package rich.util.string.chat.helper;

import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
import rich.util.color.ColorUtil;

public class TextHelper {
   public static Text applyGradient(String var0, TextHelper.GradientStyle var1, int var2, int var3, boolean var4) {
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
            return Text.literal(var0).styled(var2x -> var2x.withColor(var2).withBold(var4));
      }
   }

   private static Text halfSplitGradient(String var0, int var1, int var2, boolean var3) {
      MutableText var4 = Text.literal("");
      int var5 = var0.length() / 2;

      for (int var6 = 0; var6 < var0.length(); var6++) {
         int var7 = var6 < var5 ? var1 : var2;
         var4.append(Text.literal(String.valueOf(var0.charAt(var6))).styled(var2x -> var2x.withColor(var7).withBold(var3)));
      }

      return var4;
   }

   private static Text fullGradient(String var0, int var1, int var2, boolean var3) {
      MutableText var4 = Text.literal("");

      for (int var5 = 0; var5 < var0.length(); var5++) {
         float var6 = (float)var5 / (var0.length() - 1);
         int var7 = ColorUtil.interpolate(var1, var2, var6);
         var4.append(Text.literal(String.valueOf(var0.charAt(var5))).styled(var2x -> var2x.withColor(var7).withBold(var3)));
      }

      return var4;
   }

   private static Text astolfoGradient(String var0, boolean var1) {
      MutableText var2 = Text.literal("");

      for (int var3 = 0; var3 < var0.length(); var3++) {
         int var4 = ColorUtil.astolfo(10, var3, 0.7F, 0.7F, 1.0F);
         var2.append(Text.literal(String.valueOf(var0.charAt(var3))).styled(var2x -> var2x.withColor(var4).withBold(var1)));
      }

      return var2;
   }

   private static Text twoColorFade(String var0, int var1, int var2, boolean var3) {
      MutableText var4 = Text.literal("");

      for (int var5 = 0; var5 < var0.length(); var5++) {
         float var6 = (float)var5 / (var0.length() - 1);
         int var7 = ColorUtil.interpolateColor(var1, var2, var6);
         var4.append(Text.literal(String.valueOf(var0.charAt(var5))).styled(var2x -> var2x.withColor(var7).withBold(var3)));
      }

      return var4;
   }

   public static Text applyPredefinedGradient(String var0, String var1, boolean var2) {
      switch (var1.toLowerCase()) {
         case "red_blue":
            return applyGradient(var0, TextHelper.GradientStyle.HALF_SPLIT, -49088, ColorUtil.toColor("#0000FF"), var2);
         case "green_purple":
            return applyGradient(var0, TextHelper.GradientStyle.HALF_SPLIT, -12517568, ColorUtil.toColor("#800080"), var2);
         case "yellow_cyan":
            return applyGradient(var0, TextHelper.GradientStyle.FULL_GRADIENT, -192, ColorUtil.toColor("#00FFFF"), var2);
         case "orange_magenta":
            return applyGradient(var0, TextHelper.GradientStyle.FULL_GRADIENT, -32736, ColorUtil.toColor("#FF00FF"), var2);
         case "astolfo":
            return applyGradient(var0, TextHelper.GradientStyle.ASTOLFO, 0, 0, var2);
         case "blue_green_fade":
            return applyGradient(var0, TextHelper.GradientStyle.TWO_COLOR_FADE, ColorUtil.toColor("#0000FF"), -12517568, var2);
         case "purple_red_fade":
            return applyGradient(var0, TextHelper.GradientStyle.TWO_COLOR_FADE, ColorUtil.toColor("#800080"), -49088, var2);
         case "cyan_orange_fade":
            return applyGradient(var0, TextHelper.GradientStyle.TWO_COLOR_FADE, ColorUtil.toColor("#00FFFF"), -32736, var2);
         case "white_black":
            return applyGradient(var0, TextHelper.GradientStyle.FULL_GRADIENT, ColorUtil.colorForTextWhite$(), ColorUtil.colorForRectsBlack$(), var2);
         case "custom_purple":
            return applyGradient(var0, TextHelper.GradientStyle.FULL_GRADIENT, ColorUtil.colorForTextCustom$(), ColorUtil.colorForRectsCustom$(), var2);
         case "black_light_purple":
            return applyGradient(var0, TextHelper.GradientStyle.FULL_GRADIENT, ColorUtil.colorForRectsBlack$(), ColorUtil.toColor("#DA70D6"), var2);
         case "dark_red_bright_red":
            return applyGradient(var0, TextHelper.GradientStyle.FULL_GRADIENT, ColorUtil.toColor("#8B0000"), -49088, var2);
         case "dark_red":
            return applyGradient(var0, TextHelper.GradientStyle.HALF_SPLIT, ColorUtil.toColor("#8B0000"), ColorUtil.toColor("#8B0000"), var2);
         case "red_white":
            return applyGradient(var0, TextHelper.GradientStyle.HALF_SPLIT, -49088, ColorUtil.colorForTextWhite$(), var2);
         case "purple_bright_pink":
            return applyGradient(var0, TextHelper.GradientStyle.FULL_GRADIENT, ColorUtil.toColor("#800080"), ColorUtil.toColor("#FF69B4"), var2);
         case "pink_dark_pink":
            return applyGradient(var0, TextHelper.GradientStyle.FULL_GRADIENT, ColorUtil.toColor("#FFC1CC"), ColorUtil.toColor("#C71585"), var2);
         case "bright_red":
            return applyGradient(var0, TextHelper.GradientStyle.HALF_SPLIT, -49088, -49088, var2);
         case "dark_green_bright_green":
            return applyGradient(var0, TextHelper.GradientStyle.FULL_GRADIENT, ColorUtil.toColor("#006400"), -12517568, var2);
         case "red_orange":
            return applyGradient(var0, TextHelper.GradientStyle.FULL_GRADIENT, -49088, -32736, var2);
         case "turquoise_blue":
            return applyGradient(var0, TextHelper.GradientStyle.FULL_GRADIENT, ColorUtil.toColor("#40E0D0"), ColorUtil.toColor("#0000FF"), var2);
         default:
            return Text.literal(var0).styled(var1x -> var1x.withColor(ColorUtil.colorForTextWhite$()).withBold(var2));
      }
   }

   public enum GradientStyle {
      HALF_SPLIT,
      FULL_GRADIENT,
      ASTOLFO,
      TWO_COLOR_FADE;
   }
}
