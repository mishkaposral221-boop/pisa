package rich.util.render.item;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_10444;
import net.minecraft.class_1058;
import net.minecraft.class_1747;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_5819;
import net.minecraft.class_811;
import org.joml.Matrix3x2fStack;
import rich.util.render.Render2D;

public class ItemRender {
   private static final class_310 mc = class_310.method_1551();
   private static final Map<String, ItemRender.CachedSprite> SPRITE_CACHE = new ConcurrentHashMap<>();
   private static final class_5819 RANDOM = class_5819.method_43047();
   private static final int FORCED_GUI_SCALE = 2;

   private static int getCurrentGuiScale() {
      int var0 = (Integer)mc.field_1690.method_42474().method_41753();
      if (var0 == 0) {
         var0 = mc.method_22683().method_4476(0, mc.method_1573());
      }

      return var0;
   }

   private static float getScaleCompensation() {
      return 2.0F / getCurrentGuiScale();
   }

   public static boolean isBlockItem(class_1799 var0) {
      return var0.method_7909() instanceof class_1747;
   }

   public static boolean isPotionItem(class_1799 var0) {
      return var0.method_7909() == class_1802.field_8574
         || var0.method_7909() == class_1802.field_8436
         || var0.method_7909() == class_1802.field_8150
         || var0.method_7909() == class_1802.field_8087;
   }

   public static boolean hasGlint(class_1799 var0) {
      return var0.method_7958();
   }

   public static boolean needsContextRender(class_1799 var0) {
      return isBlockItem(var0) || isPotionItem(var0) || hasGlint(var0);
   }

   public static void drawItem(class_1799 var0, float var1, float var2, float var3, float var4) {
      drawItem(var0, var1, var2, var3, var4, -1);
   }

   public static void drawItem(class_1799 var0, float var1, float var2, float var3, float var4, int var5) {
      if (!var0.method_7960() && !(var4 <= 0.01F)) {
         if (!needsContextRender(var0)) {
            class_1058 var6 = getSpriteForStack(var0);
            if (var6 != null) {
               int var7 = applyAlpha(var5, var4);
               float var8 = 16.0F * var3;
               Render2D.drawSprite(var6, var1, var2, var8, var8, var7, true);
            }
         }
      }
   }

   public static void drawBlockItem(class_332 var0, class_1799 var1, float var2, float var3, float var4, float var5) {
      if (!var1.method_7960() && !(var5 <= 0.01F)) {
         float var6 = getScaleCompensation();
         float var7 = var4 * var6;
         float var8 = 16.0F * var4;
         float var9 = var2 + var8 / 2.0F;
         float var10 = var3 + var8 / 2.0F;
         Matrix3x2fStack var11 = var0.method_51448();
         var11.pushMatrix();
         var11.translate(var9, var10);
         var11.scale(var7, var7);
         var11.translate(-8.0F, -8.0F);
         var0.method_51427(var1, 0, 0);
         var11.popMatrix();
      }
   }

   public static void drawItemWithContext(class_332 var0, class_1799 var1, float var2, float var3, float var4, float var5) {
      if (!var1.method_7960() && !(var5 <= 0.01F)) {
         float var6 = getScaleCompensation();
         float var7 = var4 * var6;
         float var8 = 16.0F * var4;
         float var9 = var2 + var8 / 2.0F;
         float var10 = var3 + var8 / 2.0F;
         Matrix3x2fStack var11 = var0.method_51448();
         var11.pushMatrix();
         var11.translate(var9, var10);
         var11.scale(var7, var7);
         var11.translate(-8.0F, -8.0F);
         var0.method_51427(var1, 0, 0);
         var11.popMatrix();
      }
   }

   public static void drawItemCentered(class_1799 var0, float var1, float var2, float var3, float var4) {
      float var5 = 16.0F * var3;
      float var6 = var1 - var5 / 2.0F;
      float var7 = var2 - var5 / 2.0F;
      drawItem(var0, var6, var7, var3, var4);
   }

   public static void drawItemCenteredWithContext(class_332 var0, class_1799 var1, float var2, float var3, float var4, float var5) {
      float var6 = 16.0F * var4;
      float var7 = var2 - var6 / 2.0F;
      float var8 = var3 - var6 / 2.0F;
      drawItemWithContext(var0, var1, var7, var8, var4, var5);
   }

   private static class_1058 getSpriteForStack(class_1799 var0) {
      String var1 = getCacheKey(var0);
      ItemRender.CachedSprite var2 = SPRITE_CACHE.get(var1);
      if (var2 != null) {
         return var2.sprite;
      }

      try {
         class_10444 var3 = new class_10444();
         mc.method_65386().method_65598(var3, var0, class_811.field_4317, mc.field_1687, null, 0);
         class_1058 var4 = var3.method_65603(RANDOM);
         if (var4 != null) {
            SPRITE_CACHE.put(var1, new ItemRender.CachedSprite(var4));
            return var4;
         }
      } catch (Exception var5) {
      }

      return null;
   }

   private static String getCacheKey(class_1799 var0) {
      return var0.method_7909().toString() + "_" + var0.method_57353().hashCode();
   }

   private static int applyAlpha(int var0, float var1) {
      int var2 = (int)((var0 >> 24 & 0xFF) * var1);
      return var2 << 24 | var0 & 16777215;
   }

   public static void clearCache() {
      SPRITE_CACHE.clear();
   }

   private record CachedSprite() {
      private final class_1058 sprite;

      private CachedSprite(class_1058 var1) {
         this.sprite = var1;
      }
   }
}
