package rich.util.render.item;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.random.Random;
import net.minecraft.item.ItemDisplayContext;
import org.joml.Matrix3x2fStack;
import rich.util.render.Render2D;

public class ItemRender {
   private static final MinecraftClient mc = MinecraftClient.getInstance();
   private static final Map<String, ItemRender.CachedSprite> SPRITE_CACHE = new ConcurrentHashMap<>();
   private static final Random RANDOM = Random.create();
   private static final int FORCED_GUI_SCALE = 2;

   private static int getCurrentGuiScale() {
      int var0 = (Integer)mc.options.getGuiScale().getValue();
      if (var0 == 0) {
         var0 = mc.getWindow().calculateScaleFactor(0, mc.forcesUnicodeFont());
      }

      return var0;
   }

   private static float getScaleCompensation() {
      return 2.0F / getCurrentGuiScale();
   }

   public static boolean isBlockItem(ItemStack var0) {
      return var0.getItem() instanceof BlockItem;
   }

   public static boolean isPotionItem(ItemStack var0) {
      return var0.getItem() == Items.POTION
         || var0.getItem() == Items.SPLASH_POTION
         || var0.getItem() == Items.LINGERING_POTION
         || var0.getItem() == Items.TIPPED_ARROW;
   }

   public static boolean hasGlint(ItemStack var0) {
      return var0.hasGlint();
   }

   public static boolean needsContextRender(ItemStack var0) {
      return isBlockItem(var0) || isPotionItem(var0) || hasGlint(var0);
   }

   public static void drawItem(ItemStack var0, float var1, float var2, float var3, float var4) {
      drawItem(var0, var1, var2, var3, var4, -1);
   }

   public static void drawItem(ItemStack var0, float var1, float var2, float var3, float var4, int var5) {
      if (!var0.isEmpty() && !(var4 <= 0.01F)) {
         if (!needsContextRender(var0)) {
            Sprite var6 = getSpriteForStack(var0);
            if (var6 != null) {
               int var7 = applyAlpha(var5, var4);
               float var8 = 16.0F * var3;
               Render2D.drawSprite(var6, var1, var2, var8, var8, var7, true);
            }
         }
      }
   }

   public static void drawBlockItem(DrawContext var0, ItemStack var1, float var2, float var3, float var4, float var5) {
      if (!var1.isEmpty() && !(var5 <= 0.01F)) {
         float var6 = getScaleCompensation();
         float var7 = var4 * var6;
         float var8 = 16.0F * var4;
         float var9 = var2 + var8 / 2.0F;
         float var10 = var3 + var8 / 2.0F;
         Matrix3x2fStack var11 = var0.getMatrices();
         var11.pushMatrix();
         var11.translate(var9, var10);
         var11.scale(var7, var7);
         var11.translate(-8.0F, -8.0F);
         var0.drawItem(var1, 0, 0);
         var11.popMatrix();
      }
   }

   public static void drawItemWithContext(DrawContext var0, ItemStack var1, float var2, float var3, float var4, float var5) {
      if (!var1.isEmpty() && !(var5 <= 0.01F)) {
         float var6 = getScaleCompensation();
         float var7 = var4 * var6;
         float var8 = 16.0F * var4;
         float var9 = var2 + var8 / 2.0F;
         float var10 = var3 + var8 / 2.0F;
         Matrix3x2fStack var11 = var0.getMatrices();
         var11.pushMatrix();
         var11.translate(var9, var10);
         var11.scale(var7, var7);
         var11.translate(-8.0F, -8.0F);
         var0.drawItem(var1, 0, 0);
         var11.popMatrix();
      }
   }

   public static void drawItemCentered(ItemStack var0, float var1, float var2, float var3, float var4) {
      float var5 = 16.0F * var3;
      float var6 = var1 - var5 / 2.0F;
      float var7 = var2 - var5 / 2.0F;
      drawItem(var0, var6, var7, var3, var4);
   }

   public static void drawItemCenteredWithContext(DrawContext var0, ItemStack var1, float var2, float var3, float var4, float var5) {
      float var6 = 16.0F * var4;
      float var7 = var2 - var6 / 2.0F;
      float var8 = var3 - var6 / 2.0F;
      drawItemWithContext(var0, var1, var7, var8, var4, var5);
   }

   private static Sprite getSpriteForStack(ItemStack var0) {
      String var1 = getCacheKey(var0);
      ItemRender.CachedSprite var2 = SPRITE_CACHE.getName(var1);
      if (var2 != null) {
         return var2.sprite;
      }

      try {
         ItemRenderState var3 = new ItemRenderState();
         mc.getItemModelManager().clearAndUpdate(var3, var0, ItemDisplayContext.GUI, mc.world, null, 0);
         Sprite var4 = var3.getParticleSprite(RANDOM);
         if (var4 != null) {
            SPRITE_CACHE.put(var1, new ItemRender.CachedSprite(var4));
            return var4;
         }
      } catch (Exception var5) {
      }

      return null;
   }

   private static String getCacheKey(ItemStack var0) {
      return var0.getItem().toString() + "_" + var0.getComponents().hashCode();
   }

   private static int applyAlpha(int var0, float var1) {
      int var2 = (int)((var0 >> 24 & 0xFF) * var1);
      return var2 << 24 | var0 & 16777215;
   }

   public static void clearCache() {
      SPRITE_CACHE.clear();
   }

   private record CachedSprite(Sprite sprite) {
   }
}
