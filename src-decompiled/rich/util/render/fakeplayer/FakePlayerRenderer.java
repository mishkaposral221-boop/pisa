package rich.util.render.fakeplayer;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import rich.IMinecraft;
import rich.util.a;
import rich.util.render.clientpipeline.ClientPipelines;

public final class FakePlayerRenderer implements IMinecraft {
   private static final float HEAD_SIZE = 0.5F;
   private static final float BODY_WIDTH = 0.5F;
   private static final float BODY_HEIGHT = 0.75F;
   private static final float BODY_DEPTH = 0.25F;
   private static final float ARM_WIDTH = 0.25F;
   private static final float ARM_HEIGHT = 0.75F;
   private static final float LEG_HEIGHT = 0.75F;
   private static final float MODEL_CENTER_Y = 1.125F;
   private static int currentAlpha = 255;

   public static void render(class_243 var0, float var1) {
      if (mc.field_1724 != null && !(var1 <= 0.001F)) {
         currentAlpha = (int)(Math.min(1.0F, Math.max(0.0F, var1)) * 255.0F);
         class_243 var2 = mc.field_1773.method_19418().method_71156();
         class_4598 var3 = mc.method_22940().method_23000();
         class_4587 var4 = new class_4587();
         GlStateManager._disableCull();
         GlStateManager._enableBlend();
         GlStateManager._blendFuncSeparate(770, 771, 1, 0);
         var4.method_22903();
         var4.method_22904(var0.field_1352 - var2.field_1352, var0.field_1351 - var2.field_1351, var0.field_1350 - var2.field_1350);
         renderPlayerModel(var4, var3);
         var4.method_22909();
         var3.method_22993();
         GlStateManager._disableBlend();
         GlStateManager._enableCull();
      }
   }

   private static void renderPlayerModel(class_4587 var0, class_4598 var1) {
      class_238 var2 = new class_238(-0.25, 0.0, -0.125, 0.0, 0.75, 0.125);
      class_238 var3 = new class_238(0.0, 0.0, -0.125, 0.25, 0.75, 0.125);
      class_238 var4 = new class_238(-0.25, 0.75, -0.125, 0.25, 1.5, 0.125);
      class_238 var5 = new class_238(-0.25, 1.5, -0.25, 0.25, 2.0, 0.25);
      class_238 var6 = new class_238(-0.5, 0.75, -0.125, -0.25, 1.5, 0.125);
      class_238 var7 = new class_238(0.25, 0.75, -0.125, 0.5, 1.5, 0.125);
      class_238[] var8 = new class_238[]{var2, var3, var4, var5, var6, var7};
      class_4588 var9 = var1.method_73477(ClientPipelines.CRYSTAL_FILLED);
      Matrix4f var10 = var0.method_23760().method_23761();
      float var11 = 0.0F;
      float var12 = 1.125F;
      float var13 = 0.0F;
      float var14 = 1.0F;

      for (class_238 var18 : var8) {
         drawBoxWithVignette(var10, var9, var18, var11, var12, var13, var14);
      }
   }

   private static void drawBoxWithVignette(Matrix4f var0, class_4588 var1, class_238 var2, float var3, float var4, float var5, float var6) {
      float var7 = (float)var2.field_1323;
      float var8 = (float)var2.field_1322;
      float var9 = (float)var2.field_1321;
      float var10 = (float)var2.field_1320;
      float var11 = (float)var2.field_1325;
      float var12 = (float)var2.field_1324;
      drawQuadVignette(var0, var1, var7, var8, var9, var10, var8, var9, var10, var8, var12, var7, var8, var12, var3, var4, var5, var6);
      drawQuadVignette(var0, var1, var7, var11, var12, var10, var11, var12, var10, var11, var9, var7, var11, var9, var3, var4, var5, var6);
      drawQuadVignette(var0, var1, var7, var8, var9, var7, var11, var9, var10, var11, var9, var10, var8, var9, var3, var4, var5, var6);
      drawQuadVignette(var0, var1, var10, var8, var12, var10, var11, var12, var7, var11, var12, var7, var8, var12, var3, var4, var5, var6);
      drawQuadVignette(var0, var1, var7, var8, var12, var7, var11, var12, var7, var11, var9, var7, var8, var9, var3, var4, var5, var6);
      drawQuadVignette(var0, var1, var10, var8, var9, var10, var11, var9, var10, var11, var12, var10, var8, var12, var3, var4, var5, var6);
   }

   private static void drawQuadVignette(
      Matrix4f var0,
      class_4588 var1,
      float var2,
      float var3,
      float var4,
      float var5,
      float var6,
      float var7,
      float var8,
      float var9,
      float var10,
      float var11,
      float var12,
      float var13,
      float var14,
      float var15,
      float var16,
      float var17
   ) {
      var1.method_22918(var0, var2, var3, var4).method_39415(getVignetteColor(var2, var3, var4, var14, var15, var16, var17));
      var1.method_22918(var0, var5, var6, var7).method_39415(getVignetteColor(var5, var6, var7, var14, var15, var16, var17));
      var1.method_22918(var0, var8, var9, var10).method_39415(getVignetteColor(var8, var9, var10, var14, var15, var16, var17));
      var1.method_22918(var0, var11, var12, var13).method_39415(getVignetteColor(var11, var12, var13, var14, var15, var16, var17));
   }

   private static int getVignetteColor(float var0, float var1, float var2, float var3, float var4, float var5, float var6) {
      float var7 = 0.7F;
      float var8 = 1.0F;
      float var9 = 1.0F;
      float var10 = Math.abs(var0 - var3) / var7;
      float var11 = Math.abs(var1 - var4) / var8;
      float var12 = Math.abs(var2 - var5) / var9;
      float var13 = Math.max(Math.max(var10, var11), var12);
      var13 = Math.min(1.0F, var13);
      float var14 = 0.6F;
      var13 *= var14;
      int var15 = a.b(100, 180, 255, currentAlpha);
      int var16 = a.b(255, 255, 255, currentAlpha);
      return a.a(var16, var15, var13);
   }

   public static void renderFromBox(class_238 var0, float var1) {
      double var2 = (var0.field_1323 + var0.field_1320) / 2.0;
      double var4 = var0.field_1322;
      double var6 = (var0.field_1321 + var0.field_1324) / 2.0;
      render(new class_243(var2, var4, var6), var1);
   }

   private FakePlayerRenderer() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
