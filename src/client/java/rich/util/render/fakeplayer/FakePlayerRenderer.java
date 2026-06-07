package rich.util.render.fakeplayer;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import org.joml.Matrix4f;
import rich.IMinecraft;
import rich.util.color.ColorUtil;
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

   public static void render(Vec3d var0, float var1) {
      if (mc.player != null && !(var1 <= 0.001F)) {
         currentAlpha = (int)(Math.min(1.0F, Math.max(0.0F, var1)) * 255.0F);
         Vec3d var2 = mc.gameRenderer.getCamera().getCameraPos();
         net.minecraft.client.render.VertexConsumerProvider.Immediate var3 = mc.getBufferBuilders().getEntityVertexConsumers();
         MatrixStack var4 = new MatrixStack();
         GlStateManager._disableCull();
         GlStateManager._enableBlend();
         GlStateManager._blendFuncSeparate(770, 771, 1, 0);
         var4.push();
         var4.translate(var0.x - var2.x, var0.y - var2.y, var0.z - var2.z);
         renderPlayerModel(var4, var3);
         var4.pop();
         var3.draw();
         GlStateManager._disableBlend();
         GlStateManager._enableCull();
      }
   }

   private static void renderPlayerModel(MatrixStack var0, net.minecraft.client.render.VertexConsumerProvider.Immediate var1) {
      Box var2 = new Box(-0.25, 0.0, -0.125, 0.0, 0.75, 0.125);
      Box var3 = new Box(0.0, 0.0, -0.125, 0.25, 0.75, 0.125);
      Box var4 = new Box(-0.25, 0.75, -0.125, 0.25, 1.5, 0.125);
      Box var5 = new Box(-0.25, 1.5, -0.25, 0.25, 2.0, 0.25);
      Box var6 = new Box(-0.5, 0.75, -0.125, -0.25, 1.5, 0.125);
      Box var7 = new Box(0.25, 0.75, -0.125, 0.5, 1.5, 0.125);
      Box[] var8 = new Box[]{var2, var3, var4, var5, var6, var7};
      VertexConsumer var9 = var1.getBuffer(ClientPipelines.CRYSTAL_FILLED);
      Matrix4f var10 = var0.peek().getPositionMatrix();
      float var11 = 0.0F;
      float var12 = 1.125F;
      float var13 = 0.0F;
      float var14 = 1.0F;

      for (Box var18 : var8) {
         drawBoxWithVignette(var10, var9, var18, var11, var12, var13, var14);
      }
   }

   private static void drawBoxWithVignette(Matrix4f var0, VertexConsumer var1, Box var2, float var3, float var4, float var5, float var6) {
      float var7 = (float)var2.minX;
      float var8 = (float)var2.minY;
      float var9 = (float)var2.minZ;
      float var10 = (float)var2.maxX;
      float var11 = (float)var2.maxY;
      float var12 = (float)var2.maxZ;
      drawQuadVignette(var0, var1, var7, var8, var9, var10, var8, var9, var10, var8, var12, var7, var8, var12, var3, var4, var5, var6);
      drawQuadVignette(var0, var1, var7, var11, var12, var10, var11, var12, var10, var11, var9, var7, var11, var9, var3, var4, var5, var6);
      drawQuadVignette(var0, var1, var7, var8, var9, var7, var11, var9, var10, var11, var9, var10, var8, var9, var3, var4, var5, var6);
      drawQuadVignette(var0, var1, var10, var8, var12, var10, var11, var12, var7, var11, var12, var7, var8, var12, var3, var4, var5, var6);
      drawQuadVignette(var0, var1, var7, var8, var12, var7, var11, var12, var7, var11, var9, var7, var8, var9, var3, var4, var5, var6);
      drawQuadVignette(var0, var1, var10, var8, var9, var10, var11, var9, var10, var11, var12, var10, var8, var12, var3, var4, var5, var6);
   }

   private static void drawQuadVignette(
      Matrix4f var0,
      VertexConsumer var1,
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
      var1.vertex(var0, var2, var3, var4).color(getVignetteColor(var2, var3, var4, var14, var15, var16, var17));
      var1.vertex(var0, var5, var6, var7).color(getVignetteColor(var5, var6, var7, var14, var15, var16, var17));
      var1.vertex(var0, var8, var9, var10).color(getVignetteColor(var8, var9, var10, var14, var15, var16, var17));
      var1.vertex(var0, var11, var12, var13).color(getVignetteColor(var11, var12, var13, var14, var15, var16, var17));
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
      int var15 = ColorUtil.b(100, 180, 255, currentAlpha);
      int var16 = ColorUtil.b(255, 255, 255, currentAlpha);
      return ColorUtil.a(var16, var15, var13);
   }

   public static void renderFromBox(Box var0, float var1) {
      double var2 = (var0.minX + var0.maxX) / 2.0;
      double var4 = var0.minY;
      double var6 = (var0.minZ + var0.maxZ) / 2.0;
      render(new Vec3d(var2, var4, var6), var1);
   }

   private FakePlayerRenderer() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
