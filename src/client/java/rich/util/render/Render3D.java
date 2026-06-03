package rich.util.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.Pair;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import rich.IMinecraft;
import rich.events.impl.WorldRenderEvent;
import rich.util.color.ColorUtil;
import rich.util.math.MathUtils;
import rich.util.render.clientpipeline.ClientPipelines;

public final class Render3D implements IMinecraft {
   private static final Map<VoxelShape, Pair<List<Box>, List<Render3D.Line>>> SHAPE_OUTLINES = new HashMap<>();
   private static final Map<VoxelShape, List<Box>> SHAPE_BOXES = new HashMap<>();
   public static final List<Render3D.Line> LINE_DEPTH = new ArrayList<>();
   public static final List<Render3D.Line> LINE = new ArrayList<>();
   public static final List<Render3D.Quad> QUAD_DEPTH = new ArrayList<>();
   public static final List<Render3D.Quad> QUAD = new ArrayList<>();
   public static final List<Render3D.GradientQuad> GRADIENT_QUAD = new ArrayList<>();
   public static final List<Render3D.GradientQuad> GRADIENT_QUAD_DEPTH = new ArrayList<>();
   public static final Matrix4f lastProjMat = new Matrix4f();
   public static final Matrix4f lastModMat = new Matrix4f();
   public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();
   public static net.minecraft.client.util.math.MatrixStack.Entry lastWorldSpaceEntry = new MatrixStack().peek();
   public static float lastTickDelta = 1.0F;
   public static Vec3d lastCameraPos = Vec3d.ZERO;
   public static Quaternionf lastCameraRotation = new Quaternionf();
   private static float espValue = 1.0F;
   private static float espSpeed = 1.0F;
   private static float prevEspValue;
   private static float circleStep;
   private static boolean flipSpeed;
   private static double smoothY = 0.0;
   private static double smoothY2 = 0.0;

   public static void updateTargetEsp(float var0) {
      prevEspValue = espValue;
      espValue = espValue + espSpeed * var0;
      if (espSpeed > 25.0F) {
         flipSpeed = true;
      }

      if (espSpeed < -25.0F) {
         flipSpeed = false;
      }

      espSpeed = flipSpeed ? espSpeed - 0.5F * var0 : espSpeed + 0.5F * var0;
      circleStep += 0.06F * var0;
   }

   public static void updateTargetEsp() {
      updateTargetEsp(1.0F);
   }

   public static float getEspValue() {
      return espValue;
   }

   public static float getPrevEspValue() {
      return prevEspValue;
   }

   public static float getCircleStep() {
      return circleStep;
   }

   private static double easeInOutSine(double var0) {
      return -(Math.cos(Math.PI * var0) - 1.0) / 2.0;
   }

   private static double smoothSinAnimation(double var0) {
      double var2 = (Math.sin(var0) + 1.0) / 2.0;
      return easeInOutSine(var2);
   }

   public static void onWorldRender(WorldRenderEvent var0) {
      if (mc.world != null && mc.player != null) {
         MatrixStack var1 = var0.getStack();
         net.minecraft.client.render.VertexConsumerProvider.Immediate var2 = mc.getBufferBuilders().getEntityVertexConsumers();
         Vec3d var3 = lastCameraPos;
         renderGradientQuads(var1, var2, var3);
         renderQuads(var1, var2, var3);
         renderLines(var1, var2, var3);
         var2.draw();
      }
   }

   private static void renderLines(MatrixStack var0, net.minecraft.client.render.VertexConsumerProvider.Immediate var1, Vec3d var2) {
      if (!LINE.isEmpty() || !LINE_DEPTH.isEmpty()) {
         if (!LINE_DEPTH.isEmpty()) {
            VertexConsumer var3 = var1.getBuffer(RenderLayers.lines());

            for (Render3D.Line var5 : LINE_DEPTH) {
               drawLineVertex(var0, var3, var5, var2);
            }

            var1.draw(RenderLayers.lines());
         }

         if (!LINE.isEmpty()) {
            VertexConsumer var6 = var1.getBuffer(ClientPipelines.LINES_NO_DEPTH);

            for (Render3D.Line var8 : LINE) {
               drawLineVertex(var0, var6, var8, var2);
            }

            var1.draw(ClientPipelines.LINES_NO_DEPTH);
         }

         LINE.clear();
         LINE_DEPTH.clear();
      }
   }

   private static void drawLineVertex(MatrixStack var0, VertexConsumer var1, Render3D.Line var2, Vec3d var3) {
      net.minecraft.client.util.math.MatrixStack.Entry var4 = var0.peek();
      Vector3f var5 = getNormal(var2.start.toVector3f(), var2.end.toVector3f());
      float var6 = (float)(var2.start.x - var3.x);
      float var7 = (float)(var2.start.y - var3.y);
      float var8 = (float)(var2.start.z - var3.z);
      float var9 = (float)(var2.end.x - var3.x);
      float var10 = (float)(var2.end.y - var3.y);
      float var11 = (float)(var2.end.z - var3.z);
      var1.vertex(var4, var6, var7, var8).color(var2.colorStart).normal(var4, var5).lineWidth(var2.width);
      var1.vertex(var4, var9, var10, var11).color(var2.colorEnd).normal(var4, var5).lineWidth(var2.width);
   }

   private static void renderQuads(MatrixStack var0, net.minecraft.client.render.VertexConsumerProvider.Immediate var1, Vec3d var2) {
      if (!QUAD.isEmpty() || !QUAD_DEPTH.isEmpty()) {
         if (!QUAD_DEPTH.isEmpty()) {
            VertexConsumer var3 = var1.getBuffer(RenderLayers.debugFilledBox());

            for (Render3D.Quad var5 : QUAD_DEPTH) {
               drawQuadVertex(var0, var3, var5, var2);
            }

            var1.draw(RenderLayers.debugFilledBox());
         }

         if (!QUAD.isEmpty()) {
            VertexConsumer var6 = var1.getBuffer(ClientPipelines.QUADS_NO_DEPTH);

            for (Render3D.Quad var8 : QUAD) {
               drawQuadVertex(var0, var6, var8, var2);
            }

            var1.draw(ClientPipelines.QUADS_NO_DEPTH);
         }

         QUAD.clear();
         QUAD_DEPTH.clear();
      }
   }

   private static void drawQuadVertex(MatrixStack var0, VertexConsumer var1, Render3D.Quad var2, Vec3d var3) {
      net.minecraft.client.util.math.MatrixStack.Entry var4 = var0.peek();
      float var5 = (float)(var2.x.x - var3.x);
      float var6 = (float)(var2.x.y - var3.y);
      float var7 = (float)(var2.x.z - var3.z);
      float var8 = (float)(var2.y.x - var3.x);
      float var9 = (float)(var2.y.y - var3.y);
      float var10 = (float)(var2.y.z - var3.z);
      float var11 = (float)(var2.w.x - var3.x);
      float var12 = (float)(var2.w.y - var3.y);
      float var13 = (float)(var2.w.z - var3.z);
      float var14 = (float)(var2.z.x - var3.x);
      float var15 = (float)(var2.z.y - var3.y);
      float var16 = (float)(var2.z.z - var3.z);
      var1.vertex(var4, var5, var6, var7).color(var2.color);
      var1.vertex(var4, var8, var9, var10).color(var2.color);
      var1.vertex(var4, var11, var12, var13).color(var2.color);
      var1.vertex(var4, var14, var15, var16).color(var2.color);
   }

   private static void renderGradientQuads(MatrixStack var0, net.minecraft.client.render.VertexConsumerProvider.Immediate var1, Vec3d var2) {
      if (!GRADIENT_QUAD.isEmpty() || !GRADIENT_QUAD_DEPTH.isEmpty()) {
         VertexConsumer var3 = var1.getBuffer(RenderLayers.debugFilledBox());

         for (Render3D.GradientQuad var5 : GRADIENT_QUAD) {
            drawGradientQuadVertex(var0, var3, var5, var2);
         }

         for (Render3D.GradientQuad var7 : GRADIENT_QUAD_DEPTH) {
            drawGradientQuadVertex(var0, var3, var7, var2);
         }

         GRADIENT_QUAD.clear();
         GRADIENT_QUAD_DEPTH.clear();
      }
   }

   private static void drawGradientQuadVertex(MatrixStack var0, VertexConsumer var1, Render3D.GradientQuad var2, Vec3d var3) {
      net.minecraft.client.util.math.MatrixStack.Entry var4 = var0.peek();
      float var5 = (float)(var2.p1.x - var3.x);
      float var6 = (float)(var2.p1.y - var3.y);
      float var7 = (float)(var2.p1.z - var3.z);
      float var8 = (float)(var2.p2.x - var3.x);
      float var9 = (float)(var2.p2.y - var3.y);
      float var10 = (float)(var2.p2.z - var3.z);
      float var11 = (float)(var2.p3.x - var3.x);
      float var12 = (float)(var2.p3.y - var3.y);
      float var13 = (float)(var2.p3.z - var3.z);
      float var14 = (float)(var2.p4.x - var3.x);
      float var15 = (float)(var2.p4.y - var3.y);
      float var16 = (float)(var2.p4.z - var3.z);
      var1.vertex(var4, var5, var6, var7).color(var2.c1);
      var1.vertex(var4, var8, var9, var10).color(var2.c2);
      var1.vertex(var4, var11, var12, var13).color(var2.c3);
      var1.vertex(var4, var14, var15, var16).color(var2.c4);
   }

   public static void drawCircle(MatrixStack var0, LivingEntity var1, float var2, float var3, int var4, int var5) {
      double var6 = MathUtils.interpolate(circleStep - 0.17, circleStep);
      Vec3d var8 = MathUtils.interpolate(var1);
      boolean var9 = mc.player != null && mc.player.canSee(var1);
      float var10 = Math.min(var3 * 2.0F, 1.0F);
      float var11 = 1.0F + (float)Math.sin(var10 * Math.PI) * 0.18F;
      byte var12 = 64;
      float var13 = var1.getWidth() * var11;
      float var14 = var1.getHeight();
      double var15 = smoothSinAnimation(var6) * var14;
      double var17 = smoothSinAnimation(var6 - 0.35) * var14;
      smoothY = lerp(smoothY, var15, 0.12);
      smoothY2 = lerp(smoothY2, var17, 0.1);
      int var19 = ColorUtil.i(var4, 1.0F + var3 * 125.0F);
      int var20 = ColorUtil.i(var5, 1.0F + var3 * 125.0F);

      for (int var21 = 0; var21 < var12; var21++) {
         float var22 = (float)var21 / var12;
         float var23 = (float)((var21 + 1) % var12) / var12;
         float var24 = (float)(0.5 - 0.5 * Math.cos(var22 * Math.PI * 2.0));
         float var25 = (float)(0.5 - 0.5 * Math.cos(var23 * Math.PI * 2.0));
         int var26 = ColorUtil.b(var19, var20, var24);
         int var27 = ColorUtil.b(var19, var20, var25);
         int var28 = ColorUtil.d(var26, 0.8F * var2);
         int var29 = ColorUtil.d(var27, 0.8F * var2);
         int var30 = ColorUtil.d(var26, 0.0F);
         int var31 = ColorUtil.d(var27, 0.0F);
         Vec3d var32 = MathUtils.cosSin(var21, var12, var13);
         Vec3d var33 = MathUtils.cosSin((var21 + 1) % var12, var12, var13);
         Vec3d var34 = var8.add(var32.x, smoothY, var32.z);
         Vec3d var35 = var8.add(var32.x, smoothY2, var32.z);
         Vec3d var36 = var8.add(var33.x, smoothY, var33.z);
         Vec3d var37 = var8.add(var33.x, smoothY2, var33.z);
         drawGradientQuad(var34, var36, var37, var35, var28, var29, var31, var30, var9);
         drawGradientQuad(var35, var37, var36, var34, var30, var31, var29, var28, var9);
         int var38 = ColorUtil.d(var26, 0.15F * var2);
         int var39 = ColorUtil.d(var26, 0.0F);
         drawLineGradient(var34, var35, var38, var39, 6.0F, var9);
         int var40 = ColorUtil.d(var26, 1.0F * var2);
         int var41 = ColorUtil.d(var27, 1.0F * var2);
         drawLineGradient(var34, var36, var40, var41, 2.0F, var9);
      }
   }

   public static void drawRadiusCircle(Vec3d var0, float var1, int var2) {
      if (mc.player != null) {
         double var3 = var0.y;
         int var5 = ColorUtil.d(var2, 0.25F);
         int var6 = (int)Math.ceil(var1) + 1;

         for (int var7 = -var6; var7 <= var6; var7++) {
            for (int var8 = -var6; var8 <= var6; var8++) {
               boolean var9 = false;
               boolean var10 = false;

               for (double var11 = -0.5; var11 <= 0.5; var11++) {
                  for (double var13 = -0.5; var13 <= 0.5; var13++) {
                     double var15 = Math.sqrt((var7 + var11) * (var7 + var11) + (var8 + var13) * (var8 + var13));
                     if (var15 <= var1) {
                        var9 = true;
                     } else {
                        var10 = true;
                     }
                  }
               }

               if (var9 && var10) {
                  double var17 = var0.x + var7;
                  double var18 = var0.z + var8;
                  Box var19 = new Box(var17 - 0.5, var3, var18 - 0.5, var17 + 0.5, var3 + 1.0, var18 + 0.5);
                  drawBoxWithCross(var19, var2, var5, 2.0F);
               }
            }
         }
      }
   }

   public static void drawBoxWithCross(Box var0, int var1, int var2, float var3) {
      double var4 = var0.minX;
      double var6 = var0.minY;
      double var8 = var0.minZ;
      double var10 = var0.maxX;
      double var12 = var0.maxY;
      double var14 = var0.maxZ;
      drawQuad(
         new Vec3d(var4, var6, var8), new Vec3d(var10, var6, var8), new Vec3d(var10, var6, var14), new Vec3d(var4, var6, var14), var2, false
      );
      drawQuad(
         new Vec3d(var4, var6, var8), new Vec3d(var4, var12, var8), new Vec3d(var10, var12, var8), new Vec3d(var10, var6, var8), var2, false
      );
      drawQuad(
         new Vec3d(var10, var6, var8),
         new Vec3d(var10, var12, var8),
         new Vec3d(var10, var12, var14),
         new Vec3d(var10, var6, var14),
         var2,
         false
      );
      drawQuad(
         new Vec3d(var4, var6, var14),
         new Vec3d(var10, var6, var14),
         new Vec3d(var10, var12, var14),
         new Vec3d(var4, var12, var14),
         var2,
         false
      );
      drawQuad(
         new Vec3d(var4, var6, var8), new Vec3d(var4, var6, var14), new Vec3d(var4, var12, var14), new Vec3d(var4, var12, var8), var2, false
      );
      drawQuad(
         new Vec3d(var4, var12, var8),
         new Vec3d(var4, var12, var14),
         new Vec3d(var10, var12, var14),
         new Vec3d(var10, var12, var8),
         var2,
         false
      );
      drawLine(new Vec3d(var4, var6, var8), new Vec3d(var10, var6, var8), var1, var3, false);
      drawLine(new Vec3d(var10, var6, var8), new Vec3d(var10, var6, var14), var1, var3, false);
      drawLine(new Vec3d(var10, var6, var14), new Vec3d(var4, var6, var14), var1, var3, false);
      drawLine(new Vec3d(var4, var6, var14), new Vec3d(var4, var6, var8), var1, var3, false);
      drawLine(new Vec3d(var4, var6, var14), new Vec3d(var4, var12, var14), var1, var3, false);
      drawLine(new Vec3d(var4, var6, var8), new Vec3d(var4, var12, var8), var1, var3, false);
      drawLine(new Vec3d(var10, var6, var14), new Vec3d(var10, var12, var14), var1, var3, false);
      drawLine(new Vec3d(var10, var6, var8), new Vec3d(var10, var12, var8), var1, var3, false);
      drawLine(new Vec3d(var4, var12, var8), new Vec3d(var10, var12, var8), var1, var3, false);
      drawLine(new Vec3d(var10, var12, var8), new Vec3d(var10, var12, var14), var1, var3, false);
      drawLine(new Vec3d(var10, var12, var14), new Vec3d(var4, var12, var14), var1, var3, false);
      drawLine(new Vec3d(var4, var12, var14), new Vec3d(var4, var12, var8), var1, var3, false);
      int var16 = ColorUtil.d(var1, 0.6F);
      float var17 = var3 * 0.8F;
      drawLine(new Vec3d(var4, var6, var8), new Vec3d(var10, var6, var14), var16, var17, false);
      drawLine(new Vec3d(var10, var6, var8), new Vec3d(var4, var6, var14), var16, var17, false);
      drawLine(new Vec3d(var4, var12, var8), new Vec3d(var10, var12, var14), var16, var17, false);
      drawLine(new Vec3d(var10, var12, var8), new Vec3d(var4, var12, var14), var16, var17, false);
      drawLine(new Vec3d(var4, var6, var8), new Vec3d(var10, var12, var8), var16, var17, false);
      drawLine(new Vec3d(var10, var6, var8), new Vec3d(var4, var12, var8), var16, var17, false);
      drawLine(new Vec3d(var4, var6, var14), new Vec3d(var10, var12, var14), var16, var17, false);
      drawLine(new Vec3d(var10, var6, var14), new Vec3d(var4, var12, var14), var16, var17, false);
      drawLine(new Vec3d(var4, var6, var8), new Vec3d(var4, var12, var14), var16, var17, false);
      drawLine(new Vec3d(var4, var6, var14), new Vec3d(var4, var12, var8), var16, var17, false);
      drawLine(new Vec3d(var10, var6, var8), new Vec3d(var10, var12, var14), var16, var17, false);
      drawLine(new Vec3d(var10, var6, var14), new Vec3d(var10, var12, var8), var16, var17, false);
   }

   public static void drawBoxWithCrossFull(Box var0, int var1, int var2, float var3) {
      double var4 = var0.minX;
      double var6 = var0.minY;
      double var8 = var0.minZ;
      double var10 = var0.maxX;
      double var12 = var0.maxY;
      double var14 = var0.maxZ;
      drawQuad(
         new Vec3d(var4, var6, var8), new Vec3d(var10, var6, var8), new Vec3d(var10, var6, var14), new Vec3d(var4, var6, var14), var2, false
      );
      drawQuad(
         new Vec3d(var4, var6, var8), new Vec3d(var4, var12, var8), new Vec3d(var10, var12, var8), new Vec3d(var10, var6, var8), var2, false
      );
      drawQuad(
         new Vec3d(var10, var6, var8),
         new Vec3d(var10, var12, var8),
         new Vec3d(var10, var12, var14),
         new Vec3d(var10, var6, var14),
         var2,
         false
      );
      drawQuad(
         new Vec3d(var4, var6, var14),
         new Vec3d(var10, var6, var14),
         new Vec3d(var10, var12, var14),
         new Vec3d(var4, var12, var14),
         var2,
         false
      );
      drawQuad(
         new Vec3d(var4, var6, var8), new Vec3d(var4, var6, var14), new Vec3d(var4, var12, var14), new Vec3d(var4, var12, var8), var2, false
      );
      drawQuad(
         new Vec3d(var4, var12, var8),
         new Vec3d(var4, var12, var14),
         new Vec3d(var10, var12, var14),
         new Vec3d(var10, var12, var8),
         var2,
         false
      );
      drawQuad(
         new Vec3d(var4, var6, var14), new Vec3d(var10, var6, var14), new Vec3d(var10, var6, var8), new Vec3d(var4, var6, var8), var2, false
      );
      drawQuad(
         new Vec3d(var10, var6, var8), new Vec3d(var10, var12, var8), new Vec3d(var4, var12, var8), new Vec3d(var4, var6, var8), var2, false
      );
      drawQuad(
         new Vec3d(var10, var6, var14),
         new Vec3d(var10, var12, var14),
         new Vec3d(var10, var12, var8),
         new Vec3d(var10, var6, var8),
         var2,
         false
      );
      drawQuad(
         new Vec3d(var4, var12, var14),
         new Vec3d(var10, var12, var14),
         new Vec3d(var10, var6, var14),
         new Vec3d(var4, var6, var14),
         var2,
         false
      );
      drawQuad(
         new Vec3d(var4, var12, var8), new Vec3d(var4, var12, var14), new Vec3d(var4, var6, var14), new Vec3d(var4, var6, var8), var2, false
      );
      drawQuad(
         new Vec3d(var10, var12, var8),
         new Vec3d(var10, var12, var14),
         new Vec3d(var4, var12, var14),
         new Vec3d(var4, var12, var8),
         var2,
         false
      );
      drawLine(new Vec3d(var4, var6, var8), new Vec3d(var10, var6, var8), var1, var3, false);
      drawLine(new Vec3d(var10, var6, var8), new Vec3d(var10, var6, var14), var1, var3, false);
      drawLine(new Vec3d(var10, var6, var14), new Vec3d(var4, var6, var14), var1, var3, false);
      drawLine(new Vec3d(var4, var6, var14), new Vec3d(var4, var6, var8), var1, var3, false);
      drawLine(new Vec3d(var4, var6, var14), new Vec3d(var4, var12, var14), var1, var3, false);
      drawLine(new Vec3d(var4, var6, var8), new Vec3d(var4, var12, var8), var1, var3, false);
      drawLine(new Vec3d(var10, var6, var14), new Vec3d(var10, var12, var14), var1, var3, false);
      drawLine(new Vec3d(var10, var6, var8), new Vec3d(var10, var12, var8), var1, var3, false);
      drawLine(new Vec3d(var4, var12, var8), new Vec3d(var10, var12, var8), var1, var3, false);
      drawLine(new Vec3d(var10, var12, var8), new Vec3d(var10, var12, var14), var1, var3, false);
      drawLine(new Vec3d(var10, var12, var14), new Vec3d(var4, var12, var14), var1, var3, false);
      drawLine(new Vec3d(var4, var12, var14), new Vec3d(var4, var12, var8), var1, var3, false);
      int var16 = ColorUtil.d(var1, 0.6F);
      float var17 = var3 * 0.8F;
      drawLine(new Vec3d(var4, var6, var8), new Vec3d(var10, var6, var14), var16, var17, false);
      drawLine(new Vec3d(var10, var6, var8), new Vec3d(var4, var6, var14), var16, var17, false);
      drawLine(new Vec3d(var4, var12, var8), new Vec3d(var10, var12, var14), var16, var17, false);
      drawLine(new Vec3d(var10, var12, var8), new Vec3d(var4, var12, var14), var16, var17, false);
      drawLine(new Vec3d(var4, var6, var8), new Vec3d(var10, var12, var8), var16, var17, false);
      drawLine(new Vec3d(var10, var6, var8), new Vec3d(var4, var12, var8), var16, var17, false);
      drawLine(new Vec3d(var4, var6, var14), new Vec3d(var10, var12, var14), var16, var17, false);
      drawLine(new Vec3d(var10, var6, var14), new Vec3d(var4, var12, var14), var16, var17, false);
      drawLine(new Vec3d(var4, var6, var8), new Vec3d(var4, var12, var14), var16, var17, false);
      drawLine(new Vec3d(var4, var6, var14), new Vec3d(var4, var12, var8), var16, var17, false);
      drawLine(new Vec3d(var10, var6, var8), new Vec3d(var10, var12, var14), var16, var17, false);
      drawLine(new Vec3d(var10, var6, var14), new Vec3d(var10, var12, var8), var16, var17, false);
   }

   public static void drawPlastShape(BlockPos var0, Vec3d var1, int var2, int var3) {
      if (mc.player != null) {
         float var4 = MathHelper.wrapDegrees(mc.player.getYaw());
         if (Math.abs(mc.player.getPitch()) > 60.0F) {
            BlockPos var5 = var0.up().offset(mc.player.getFacing(), 3);
            Vec3d var6 = Vec3d.of(var5.east(3).south(3).down()).add(var1);
            Vec3d var7 = Vec3d.of(var5.west(2).north(2).up()).add(var1);
            drawBoxWithCrossFull(new Box(var6, var7), var2, var3, 3.0F);
         } else if (var4 <= -157.5F || var4 >= 157.5F) {
            BlockPos var11 = var0.north(3).up();
            Vec3d var15 = Vec3d.of(var11.down(2).east(3)).add(var1);
            Vec3d var19 = Vec3d.of(var11.up(3).west(2).south(2)).add(var1);
            drawBoxWithCrossFull(new Box(var15, var19), var2, var3, 3.0F);
         } else if (var4 <= -112.5F) {
            drawSidePlast(var0.east(5).south().down(), var1, var2, var3, -1, true);
         } else if (var4 <= -67.5F) {
            BlockPos var8 = var0.east(2).up();
            Vec3d var12 = Vec3d.of(var8.down(2).south(3)).add(var1);
            Vec3d var16 = Vec3d.of(var8.up(3).north(2).east(2)).add(var1);
            drawBoxWithCrossFull(new Box(var12, var16), var2, var3, 3.0F);
         } else if (var4 <= -22.5F) {
            drawSidePlast(var0.east(5).down(), var1, var2, var3, 1, false);
         } else if (var4 >= -22.5 && var4 <= 22.5) {
            BlockPos var10 = var0.south(2).up();
            Vec3d var14 = Vec3d.of(var10.down(2).east(3)).add(var1);
            Vec3d var18 = Vec3d.of(var10.up(3).west(2).south(2)).add(var1);
            drawBoxWithCrossFull(new Box(var14, var18), var2, var3, 3.0F);
         } else if (var4 <= 67.5F) {
            drawSidePlast(var0.west(4).down(), var1, var2, var3, 1, true);
         } else if (var4 <= 112.5F) {
            BlockPos var9 = var0.west(3).up();
            Vec3d var13 = Vec3d.of(var9.down(2).south(3)).add(var1);
            Vec3d var17 = Vec3d.of(var9.up(3).north(2).east(2)).add(var1);
            drawBoxWithCrossFull(new Box(var13, var17), var2, var3, 3.0F);
         } else if (var4 <= 157.5F) {
            drawSidePlast(var0.west(4).south().down(), var1, var2, var3, -1, false);
         }
      }
   }

   private static void drawSidePlast(BlockPos var0, Vec3d var1, int var2, int var3, int var4, boolean var5) {
      Vec3d var6 = Vec3d.of(var0).add(var1);
      int var7 = ColorUtil.d(var2, 0.6F);
      ArrayList var8 = new ArrayList();
      float var9 = var5 ? var4 : -var4;
      Vec3d var10 = var6;
      var8.add(var10);
      var10 = var10.add(var9, 0.0, 0.0);
      var8.add(var10);

      for (int var11 = 0; var11 < 4; var11++) {
         Vec3d var17 = var10.add(0.0, 0.0, var4);
         var8.add(var17);
         var10 = var17.add(var9, 0.0, 0.0);
         var8.add(var10);
      }

      var10 = var10.add(0.0, 0.0, var4);
      var8.add(var10);
      var10 = var10.add(var9 * -2.0F, 0.0, 0.0);
      var8.add(var10);

      for (int var26 = 0; var26 < 3; var26++) {
         Vec3d var20 = var10.add(0.0, 0.0, var4 * -1);
         var8.add(var20);
         var10 = var20.add(var9 * -1.0F, 0.0, 0.0);
         var8.add(var10);
      }

      var10 = var10.add(0.0, 0.0, var4 * -2);
      var8.add(var10);

      for (int var27 = 0; var27 < var8.size() - 1; var27++) {
         Vec3d var12 = (Vec3d)var8.get(var27);
         Vec3d var13 = (Vec3d)var8.get(var27 + 1);
         drawLine(var12, var13, var2, 2.0F, false);
         drawLine(var12.add(0.0, 5.0, 0.0), var13.add(0.0, 5.0, 0.0), var2, 2.0F, false);
      }

      for (Vec3d var32 : var8) {
         drawLine(var32, var32.add(0.0, 5.0, 0.0), var2, 2.0F, false);
      }

      for (int var29 = 0; var29 < var8.size() - 1; var29++) {
         Vec3d var33 = (Vec3d)var8.get(var29);
         Vec3d var34 = (Vec3d)var8.get(var29 + 1);
         Vec3d var14 = var33.add(0.0, 5.0, 0.0);
         Vec3d var15 = var34.add(0.0, 5.0, 0.0);
         drawQuad(var33, var34, var15, var14, var3, false);
         drawQuad(var14, var15, var34, var33, var3, false);
         drawLine(var33, var15, var7, 1.6F, false);
         drawLine(var34, var14, var7, 1.6F, false);
      }

      var10 = var6;
      drawQuad(var10, var10.add(var9, 0.0, 0.0), var10.add(var9, 0.0, var4 * 2), var10.add(0.0, 0.0, var4 * 2), var3, false);
      drawQuad(var10.add(0.0, 0.0, var4 * 2), var10.add(var9, 0.0, var4 * 2), var10.add(var9, 0.0, 0.0), var10, var3, false);
      drawLine(var10, var10.add(var9, 0.0, var4 * 2), var7, 1.6F, false);
      drawLine(var10.add(var9, 0.0, 0.0), var10.add(0.0, 0.0, var4 * 2), var7, 1.6F, false);

      for (int var30 = 0; var30 < 3; var30++) {
         var10 = var10.add(var9, 0.0, var4);
         drawQuad(var10, var10.add(var9, 0.0, 0.0), var10.add(var9, 0.0, var4 * 2), var10.add(0.0, 0.0, var4 * 2), var3, false);
         drawQuad(var10.add(0.0, 0.0, var4 * 2), var10.add(var9, 0.0, var4 * 2), var10.add(var9, 0.0, 0.0), var10, var3, false);
         drawLine(var10, var10.add(var9, 0.0, var4 * 2), var7, 1.6F, false);
         drawLine(var10.add(var9, 0.0, 0.0), var10.add(0.0, 0.0, var4 * 2), var7, 1.6F, false);
      }

      var10 = var10.add(var9, 0.0, var4);
      drawQuad(var10, var10.add(var9, 0.0, 0.0), var10.add(var9, 0.0, var4), var10.add(0.0, 0.0, var4), var3, false);
      drawQuad(var10.add(0.0, 0.0, var4), var10.add(var9, 0.0, var4), var10.add(var9, 0.0, 0.0), var10, var3, false);
      drawLine(var10, var10.add(var9, 0.0, var4), var7, 1.6F, false);
      drawLine(var10.add(var9, 0.0, 0.0), var10.add(0.0, 0.0, var4), var7, 1.6F, false);
      var10 = var6.add(0.0, 5.0, 0.0);
      drawQuad(var10, var10.add(0.0, 0.0, var4 * 2), var10.add(var9, 0.0, var4 * 2), var10.add(var9, 0.0, 0.0), var3, false);
      drawQuad(var10.add(var9, 0.0, 0.0), var10.add(var9, 0.0, var4 * 2), var10.add(0.0, 0.0, var4 * 2), var10, var3, false);
      drawLine(var10, var10.add(var9, 0.0, var4 * 2), var7, 1.6F, false);
      drawLine(var10.add(var9, 0.0, 0.0), var10.add(0.0, 0.0, var4 * 2), var7, 1.6F, false);

      for (int var31 = 0; var31 < 3; var31++) {
         var10 = var10.add(var9, 0.0, var4);
         drawQuad(var10, var10.add(0.0, 0.0, var4 * 2), var10.add(var9, 0.0, var4 * 2), var10.add(var9, 0.0, 0.0), var3, false);
         drawQuad(var10.add(var9, 0.0, 0.0), var10.add(var9, 0.0, var4 * 2), var10.add(0.0, 0.0, var4 * 2), var10, var3, false);
         drawLine(var10, var10.add(var9, 0.0, var4 * 2), var7, 1.6F, false);
         drawLine(var10.add(var9, 0.0, 0.0), var10.add(0.0, 0.0, var4 * 2), var7, 1.6F, false);
      }

      var10 = var10.add(var9, 0.0, var4);
      drawQuad(var10, var10.add(0.0, 0.0, var4), var10.add(var9, 0.0, var4), var10.add(var9, 0.0, 0.0), var3, false);
      drawQuad(var10.add(var9, 0.0, 0.0), var10.add(var9, 0.0, var4), var10.add(0.0, 0.0, var4), var10, var3, false);
      drawLine(var10, var10.add(var9, 0.0, var4), var7, 1.6F, false);
      drawLine(var10.add(var9, 0.0, 0.0), var10.add(0.0, 0.0, var4), var7, 1.6F, false);
   }

   private static double lerp(double var0, double var2, double var4) {
      return var0 + (var2 - var0) * var4;
   }

   public static void drawGradientQuad(Vec3d var0, Vec3d var1, Vec3d var2, Vec3d var3, int var4, int var5, int var6, int var7, boolean var8) {
      Render3D.GradientQuad var9 = new Render3D.GradientQuad(var0, var1, var2, var3, var4, var5, var6, var7);
      if (var8) {
         GRADIENT_QUAD_DEPTH.add(var9);
      } else {
         GRADIENT_QUAD.add(var9);
      }
   }

   public static void drawLineGradient(Vec3d var0, Vec3d var1, int var2, int var3, float var4, boolean var5) {
      Render3D.Line var6 = new Render3D.Line(null, var0, var1, var2, var3, var4);
      if (var5) {
         LINE_DEPTH.add(var6);
      } else {
         LINE.add(var6);
      }
   }

   public static Vector3f getNormal(Vector3f var0, Vector3f var1) {
      Vector3f var2 = new Vector3f(var0).sub(var1);
      float var3 = MathHelper.sqrt(var2.lengthSquared());
      return var3 < 1.0E-4F ? new Vector3f(0.0F, 1.0F, 0.0F) : var2.div(var3);
   }

   public static void drawShape(BlockPos var0, VoxelShape var1, int var2, float var3) {
      drawShape(var0, var1, var2, var3, true, false);
   }

   public static void drawShape(BlockPos var0, VoxelShape var1, int var2, float var3, boolean var4, boolean var5) {
      if (SHAPE_BOXES.containsKey(var1)) {
         SHAPE_BOXES.get(var1).forEach(var5x -> {
            Box var6 = var5x.offset(var0);
            drawBox(var6, var2, var3, true, var4, var5);
         });
      } else {
         SHAPE_BOXES.put(var1, var1.getBoundingBoxes());
      }
   }

   public static void drawShapeAlternative(BlockPos var0, VoxelShape var1, int var2, float var3, boolean var4, boolean var5) {
      Vec3d var6 = Vec3d.of(var0);
      if (SHAPE_OUTLINES.containsKey(var1)) {
         Pair var8 = SHAPE_OUTLINES.get(var1);
         if (var4) {
            ((List)var8.getLeft()).forEach(var4x -> drawBox(var4x.offset(var6), var2, var3, false, true, var5));
         }

         ((List)var8.getRight()).forEach(var4x -> drawLine(var4x.start.add(var6), var4x.end.add(var6), var2, var3, var5));
      } else {
         ArrayList var7 = new ArrayList();
         var1.forEachEdge(
            (var1x, var3x, var5x, var7x, var9, var11) -> var7.add(
               new Render3D.Line(null, new Vec3d(var1x, var3x, var5x), new Vec3d(var7x, var9, var11), 0, 0, 0.0F)
            )
         );
         SHAPE_OUTLINES.put(var1, new Pair(var1.getBoundingBoxes(), var7));
      }
   }

   public static void drawBox(Box var0, int var1, float var2) {
      drawBox(var0, var1, var2, true, true, false);
   }

   public static void drawBox(Box var0, int var1, float var2, boolean var3, boolean var4, boolean var5) {
      drawBox(null, var0, var1, var2, var3, var4, var5);
   }

   public static void drawBox(net.minecraft.client.util.math.MatrixStack.Entry var0, Box var1, int var2, float var3, boolean var4, boolean var5, boolean var6) {
      double var7 = var1.minX;
      double var9 = var1.minY;
      double var11 = var1.minZ;
      double var13 = var1.maxX;
      double var15 = var1.maxY;
      double var17 = var1.maxZ;
      if (var5) {
         int var19 = ColorUtil.d(var2, 0.3F);
         drawQuad(
            var0,
            new Vec3d(var7, var9, var11),
            new Vec3d(var13, var9, var11),
            new Vec3d(var13, var9, var17),
            new Vec3d(var7, var9, var17),
            var19,
            var6
         );
         drawQuad(
            var0,
            new Vec3d(var7, var9, var11),
            new Vec3d(var7, var15, var11),
            new Vec3d(var13, var15, var11),
            new Vec3d(var13, var9, var11),
            var19,
            var6
         );
         drawQuad(
            var0,
            new Vec3d(var13, var9, var11),
            new Vec3d(var13, var15, var11),
            new Vec3d(var13, var15, var17),
            new Vec3d(var13, var9, var17),
            var19,
            var6
         );
         drawQuad(
            var0,
            new Vec3d(var7, var9, var17),
            new Vec3d(var13, var9, var17),
            new Vec3d(var13, var15, var17),
            new Vec3d(var7, var15, var17),
            var19,
            var6
         );
         drawQuad(
            var0,
            new Vec3d(var7, var9, var11),
            new Vec3d(var7, var9, var17),
            new Vec3d(var7, var15, var17),
            new Vec3d(var7, var15, var11),
            var19,
            var6
         );
         drawQuad(
            var0,
            new Vec3d(var7, var15, var11),
            new Vec3d(var7, var15, var17),
            new Vec3d(var13, var15, var17),
            new Vec3d(var13, var15, var11),
            var19,
            var6
         );
      }

      if (var4) {
         drawLine(var0, var7, var9, var11, var13, var9, var11, var2, var3, var6);
         drawLine(var0, var13, var9, var11, var13, var9, var17, var2, var3, var6);
         drawLine(var0, var13, var9, var17, var7, var9, var17, var2, var3, var6);
         drawLine(var0, var7, var9, var17, var7, var9, var11, var2, var3, var6);
         drawLine(var0, var7, var9, var17, var7, var15, var17, var2, var3, var6);
         drawLine(var0, var7, var9, var11, var7, var15, var11, var2, var3, var6);
         drawLine(var0, var13, var9, var17, var13, var15, var17, var2, var3, var6);
         drawLine(var0, var13, var9, var11, var13, var15, var11, var2, var3, var6);
         drawLine(var0, var7, var15, var11, var13, var15, var11, var2, var3, var6);
         drawLine(var0, var13, var15, var11, var13, var15, var17, var2, var3, var6);
         drawLine(var0, var13, var15, var17, var7, var15, var17, var2, var3, var6);
         drawLine(var0, var7, var15, var17, var7, var15, var11, var2, var3, var6);
      }
   }

   public static void drawLine(
      net.minecraft.client.util.math.MatrixStack.Entry var0, double var1, double var3, double var5, double var7, double var9, double var11, int var13, float var14, boolean var15
   ) {
      drawLine(var0, new Vec3d(var1, var3, var5), new Vec3d(var7, var9, var11), var13, var13, var14, var15);
   }

   public static void drawLine(Vec3d var0, Vec3d var1, int var2, float var3, boolean var4) {
      drawLine(null, var0, var1, var2, var2, var3, var4);
   }

   public static void drawLine(net.minecraft.client.util.math.MatrixStack.Entry var0, Vec3d var1, Vec3d var2, int var3, int var4, float var5, boolean var6) {
      Render3D.Line var7 = new Render3D.Line(var0, var1, var2, var3, var4, var5);
      if (var6) {
         LINE_DEPTH.add(var7);
      } else {
         LINE.add(var7);
      }
   }

   public static void drawQuad(Vec3d var0, Vec3d var1, Vec3d var2, Vec3d var3, int var4, boolean var5) {
      drawQuad(null, var0, var1, var2, var3, var4, var5);
   }

   public static void drawQuad(net.minecraft.client.util.math.MatrixStack.Entry var0, Vec3d var1, Vec3d var2, Vec3d var3, Vec3d var4, int var5, boolean var6) {
      Render3D.Quad var7 = new Render3D.Quad(var0, var1, var2, var3, var4, var5);
      if (var6) {
         QUAD_DEPTH.add(var7);
      } else {
         QUAD.add(var7);
      }
   }

   public static void resetCircleSmoothing() {
      smoothY = 0.0;
      smoothY2 = 0.0;
   }

   private Render3D() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static void setLastWorldSpaceEntry(net.minecraft.client.util.math.MatrixStack.Entry var0) {
      lastWorldSpaceEntry = var0;
   }

   public static void setLastTickDelta(float var0) {
      lastTickDelta = var0;
   }

   public static void setLastCameraPos(Vec3d var0) {
      lastCameraPos = var0;
   }

   public static void setLastCameraRotation(Quaternionf var0) {
      lastCameraRotation = var0;
   }

   public record GradientQuad(Vec3d p1, Vec3d p2, Vec3d p3, Vec3d p4, int c1, int c2, int c3, int c4) {
   }

   public record Line(net.minecraft.client.util.math.MatrixStack.Entry entry, Vec3d start, Vec3d end, int colorStart, int colorEnd, float width) {
   }

   public record Quad(net.minecraft.client.util.math.MatrixStack.Entry entry, Vec3d x, Vec3d y, Vec3d w, Vec3d z, int color) {
   }
}
