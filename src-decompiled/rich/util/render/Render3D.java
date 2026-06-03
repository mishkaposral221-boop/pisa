package rich.util.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_12249;
import net.minecraft.class_1309;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_3532;
import net.minecraft.class_3545;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4587.class_4665;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import rich.IMinecraft;
import rich.events.impl.WorldRenderEvent;
import rich.util.a;
import rich.util.math.MathUtils;
import rich.util.render.clientpipeline.ClientPipelines;

public final class Render3D implements IMinecraft {
   private static final Map<class_265, class_3545<List<class_238>, List<Render3D.Line>>> SHAPE_OUTLINES = new HashMap<>();
   private static final Map<class_265, List<class_238>> SHAPE_BOXES = new HashMap<>();
   public static final List<Render3D.Line> LINE_DEPTH = new ArrayList<>();
   public static final List<Render3D.Line> LINE = new ArrayList<>();
   public static final List<Render3D.Quad> QUAD_DEPTH = new ArrayList<>();
   public static final List<Render3D.Quad> QUAD = new ArrayList<>();
   public static final List<Render3D.GradientQuad> GRADIENT_QUAD = new ArrayList<>();
   public static final List<Render3D.GradientQuad> GRADIENT_QUAD_DEPTH = new ArrayList<>();
   public static final Matrix4f lastProjMat = new Matrix4f();
   public static final Matrix4f lastModMat = new Matrix4f();
   public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();
   public static class_4665 lastWorldSpaceEntry = new class_4587().method_23760();
   public static float lastTickDelta = 1.0F;
   public static class_243 lastCameraPos = class_243.field_1353;
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
      if (mc.field_1687 != null && mc.field_1724 != null) {
         class_4587 var1 = var0.getStack();
         class_4598 var2 = mc.method_22940().method_23000();
         class_243 var3 = lastCameraPos;
         renderGradientQuads(var1, var2, var3);
         renderQuads(var1, var2, var3);
         renderLines(var1, var2, var3);
         var2.method_22993();
      }
   }

   private static void renderLines(class_4587 var0, class_4598 var1, class_243 var2) {
      if (!LINE.isEmpty() || !LINE_DEPTH.isEmpty()) {
         if (!LINE_DEPTH.isEmpty()) {
            class_4588 var3 = var1.method_73477(class_12249.method_76015());

            for (Render3D.Line var5 : LINE_DEPTH) {
               drawLineVertex(var0, var3, var5, var2);
            }

            var1.method_22994(class_12249.method_76015());
         }

         if (!LINE.isEmpty()) {
            class_4588 var6 = var1.method_73477(ClientPipelines.LINES_NO_DEPTH);

            for (Render3D.Line var8 : LINE) {
               drawLineVertex(var0, var6, var8, var2);
            }

            var1.method_22994(ClientPipelines.LINES_NO_DEPTH);
         }

         LINE.clear();
         LINE_DEPTH.clear();
      }
   }

   private static void drawLineVertex(class_4587 var0, class_4588 var1, Render3D.Line var2, class_243 var3) {
      class_4665 var4 = var0.method_23760();
      Vector3f var5 = getNormal(var2.start.method_46409(), var2.end.method_46409());
      float var6 = (float)(var2.start.field_1352 - var3.field_1352);
      float var7 = (float)(var2.start.field_1351 - var3.field_1351);
      float var8 = (float)(var2.start.field_1350 - var3.field_1350);
      float var9 = (float)(var2.end.field_1352 - var3.field_1352);
      float var10 = (float)(var2.end.field_1351 - var3.field_1351);
      float var11 = (float)(var2.end.field_1350 - var3.field_1350);
      var1.method_56824(var4, var6, var7, var8).method_39415(var2.colorStart).method_61959(var4, var5).method_75298(var2.width);
      var1.method_56824(var4, var9, var10, var11).method_39415(var2.colorEnd).method_61959(var4, var5).method_75298(var2.width);
   }

   private static void renderQuads(class_4587 var0, class_4598 var1, class_243 var2) {
      if (!QUAD.isEmpty() || !QUAD_DEPTH.isEmpty()) {
         if (!QUAD_DEPTH.isEmpty()) {
            class_4588 var3 = var1.method_73477(class_12249.method_76019());

            for (Render3D.Quad var5 : QUAD_DEPTH) {
               drawQuadVertex(var0, var3, var5, var2);
            }

            var1.method_22994(class_12249.method_76019());
         }

         if (!QUAD.isEmpty()) {
            class_4588 var6 = var1.method_73477(ClientPipelines.QUADS_NO_DEPTH);

            for (Render3D.Quad var8 : QUAD) {
               drawQuadVertex(var0, var6, var8, var2);
            }

            var1.method_22994(ClientPipelines.QUADS_NO_DEPTH);
         }

         QUAD.clear();
         QUAD_DEPTH.clear();
      }
   }

   private static void drawQuadVertex(class_4587 var0, class_4588 var1, Render3D.Quad var2, class_243 var3) {
      class_4665 var4 = var0.method_23760();
      float var5 = (float)(var2.x.field_1352 - var3.field_1352);
      float var6 = (float)(var2.x.field_1351 - var3.field_1351);
      float var7 = (float)(var2.x.field_1350 - var3.field_1350);
      float var8 = (float)(var2.y.field_1352 - var3.field_1352);
      float var9 = (float)(var2.y.field_1351 - var3.field_1351);
      float var10 = (float)(var2.y.field_1350 - var3.field_1350);
      float var11 = (float)(var2.w.field_1352 - var3.field_1352);
      float var12 = (float)(var2.w.field_1351 - var3.field_1351);
      float var13 = (float)(var2.w.field_1350 - var3.field_1350);
      float var14 = (float)(var2.z.field_1352 - var3.field_1352);
      float var15 = (float)(var2.z.field_1351 - var3.field_1351);
      float var16 = (float)(var2.z.field_1350 - var3.field_1350);
      var1.method_56824(var4, var5, var6, var7).method_39415(var2.color);
      var1.method_56824(var4, var8, var9, var10).method_39415(var2.color);
      var1.method_56824(var4, var11, var12, var13).method_39415(var2.color);
      var1.method_56824(var4, var14, var15, var16).method_39415(var2.color);
   }

   private static void renderGradientQuads(class_4587 var0, class_4598 var1, class_243 var2) {
      if (!GRADIENT_QUAD.isEmpty() || !GRADIENT_QUAD_DEPTH.isEmpty()) {
         class_4588 var3 = var1.method_73477(class_12249.method_76019());

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

   private static void drawGradientQuadVertex(class_4587 var0, class_4588 var1, Render3D.GradientQuad var2, class_243 var3) {
      class_4665 var4 = var0.method_23760();
      float var5 = (float)(var2.p1.field_1352 - var3.field_1352);
      float var6 = (float)(var2.p1.field_1351 - var3.field_1351);
      float var7 = (float)(var2.p1.field_1350 - var3.field_1350);
      float var8 = (float)(var2.p2.field_1352 - var3.field_1352);
      float var9 = (float)(var2.p2.field_1351 - var3.field_1351);
      float var10 = (float)(var2.p2.field_1350 - var3.field_1350);
      float var11 = (float)(var2.p3.field_1352 - var3.field_1352);
      float var12 = (float)(var2.p3.field_1351 - var3.field_1351);
      float var13 = (float)(var2.p3.field_1350 - var3.field_1350);
      float var14 = (float)(var2.p4.field_1352 - var3.field_1352);
      float var15 = (float)(var2.p4.field_1351 - var3.field_1351);
      float var16 = (float)(var2.p4.field_1350 - var3.field_1350);
      var1.method_56824(var4, var5, var6, var7).method_39415(var2.c1);
      var1.method_56824(var4, var8, var9, var10).method_39415(var2.c2);
      var1.method_56824(var4, var11, var12, var13).method_39415(var2.c3);
      var1.method_56824(var4, var14, var15, var16).method_39415(var2.c4);
   }

   public static void drawCircle(class_4587 var0, class_1309 var1, float var2, float var3, int var4, int var5) {
      double var6 = MathUtils.interpolate(circleStep - 0.17, circleStep);
      class_243 var8 = MathUtils.interpolate(var1);
      boolean var9 = mc.field_1724 != null && mc.field_1724.method_6057(var1);
      float var10 = Math.min(var3 * 2.0F, 1.0F);
      float var11 = 1.0F + (float)Math.sin(var10 * Math.PI) * 0.18F;
      byte var12 = 64;
      float var13 = var1.method_17681() * var11;
      float var14 = var1.method_17682();
      double var15 = smoothSinAnimation(var6) * var14;
      double var17 = smoothSinAnimation(var6 - 0.35) * var14;
      smoothY = lerp(smoothY, var15, 0.12);
      smoothY2 = lerp(smoothY2, var17, 0.1);
      int var19 = a.i(var4, 1.0F + var3 * 125.0F);
      int var20 = a.i(var5, 1.0F + var3 * 125.0F);

      for (int var21 = 0; var21 < var12; var21++) {
         float var22 = (float)var21 / var12;
         float var23 = (float)((var21 + 1) % var12) / var12;
         float var24 = (float)(0.5 - 0.5 * Math.cos(var22 * Math.PI * 2.0));
         float var25 = (float)(0.5 - 0.5 * Math.cos(var23 * Math.PI * 2.0));
         int var26 = a.b(var19, var20, var24);
         int var27 = a.b(var19, var20, var25);
         int var28 = a.d(var26, 0.8F * var2);
         int var29 = a.d(var27, 0.8F * var2);
         int var30 = a.d(var26, 0.0F);
         int var31 = a.d(var27, 0.0F);
         class_243 var32 = MathUtils.cosSin(var21, var12, var13);
         class_243 var33 = MathUtils.cosSin((var21 + 1) % var12, var12, var13);
         class_243 var34 = var8.method_1031(var32.field_1352, smoothY, var32.field_1350);
         class_243 var35 = var8.method_1031(var32.field_1352, smoothY2, var32.field_1350);
         class_243 var36 = var8.method_1031(var33.field_1352, smoothY, var33.field_1350);
         class_243 var37 = var8.method_1031(var33.field_1352, smoothY2, var33.field_1350);
         drawGradientQuad(var34, var36, var37, var35, var28, var29, var31, var30, var9);
         drawGradientQuad(var35, var37, var36, var34, var30, var31, var29, var28, var9);
         int var38 = a.d(var26, 0.15F * var2);
         int var39 = a.d(var26, 0.0F);
         drawLineGradient(var34, var35, var38, var39, 6.0F, var9);
         int var40 = a.d(var26, 1.0F * var2);
         int var41 = a.d(var27, 1.0F * var2);
         drawLineGradient(var34, var36, var40, var41, 2.0F, var9);
      }
   }

   public static void drawRadiusCircle(class_243 var0, float var1, int var2) {
      if (mc.field_1724 != null) {
         double var3 = var0.field_1351;
         int var5 = a.d(var2, 0.25F);
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
                  double var17 = var0.field_1352 + var7;
                  double var18 = var0.field_1350 + var8;
                  class_238 var19 = new class_238(var17 - 0.5, var3, var18 - 0.5, var17 + 0.5, var3 + 1.0, var18 + 0.5);
                  drawBoxWithCross(var19, var2, var5, 2.0F);
               }
            }
         }
      }
   }

   public static void drawBoxWithCross(class_238 var0, int var1, int var2, float var3) {
      double var4 = var0.field_1323;
      double var6 = var0.field_1322;
      double var8 = var0.field_1321;
      double var10 = var0.field_1320;
      double var12 = var0.field_1325;
      double var14 = var0.field_1324;
      drawQuad(
         new class_243(var4, var6, var8), new class_243(var10, var6, var8), new class_243(var10, var6, var14), new class_243(var4, var6, var14), var2, false
      );
      drawQuad(
         new class_243(var4, var6, var8), new class_243(var4, var12, var8), new class_243(var10, var12, var8), new class_243(var10, var6, var8), var2, false
      );
      drawQuad(
         new class_243(var10, var6, var8),
         new class_243(var10, var12, var8),
         new class_243(var10, var12, var14),
         new class_243(var10, var6, var14),
         var2,
         false
      );
      drawQuad(
         new class_243(var4, var6, var14),
         new class_243(var10, var6, var14),
         new class_243(var10, var12, var14),
         new class_243(var4, var12, var14),
         var2,
         false
      );
      drawQuad(
         new class_243(var4, var6, var8), new class_243(var4, var6, var14), new class_243(var4, var12, var14), new class_243(var4, var12, var8), var2, false
      );
      drawQuad(
         new class_243(var4, var12, var8),
         new class_243(var4, var12, var14),
         new class_243(var10, var12, var14),
         new class_243(var10, var12, var8),
         var2,
         false
      );
      drawLine(new class_243(var4, var6, var8), new class_243(var10, var6, var8), var1, var3, false);
      drawLine(new class_243(var10, var6, var8), new class_243(var10, var6, var14), var1, var3, false);
      drawLine(new class_243(var10, var6, var14), new class_243(var4, var6, var14), var1, var3, false);
      drawLine(new class_243(var4, var6, var14), new class_243(var4, var6, var8), var1, var3, false);
      drawLine(new class_243(var4, var6, var14), new class_243(var4, var12, var14), var1, var3, false);
      drawLine(new class_243(var4, var6, var8), new class_243(var4, var12, var8), var1, var3, false);
      drawLine(new class_243(var10, var6, var14), new class_243(var10, var12, var14), var1, var3, false);
      drawLine(new class_243(var10, var6, var8), new class_243(var10, var12, var8), var1, var3, false);
      drawLine(new class_243(var4, var12, var8), new class_243(var10, var12, var8), var1, var3, false);
      drawLine(new class_243(var10, var12, var8), new class_243(var10, var12, var14), var1, var3, false);
      drawLine(new class_243(var10, var12, var14), new class_243(var4, var12, var14), var1, var3, false);
      drawLine(new class_243(var4, var12, var14), new class_243(var4, var12, var8), var1, var3, false);
      int var16 = a.d(var1, 0.6F);
      float var17 = var3 * 0.8F;
      drawLine(new class_243(var4, var6, var8), new class_243(var10, var6, var14), var16, var17, false);
      drawLine(new class_243(var10, var6, var8), new class_243(var4, var6, var14), var16, var17, false);
      drawLine(new class_243(var4, var12, var8), new class_243(var10, var12, var14), var16, var17, false);
      drawLine(new class_243(var10, var12, var8), new class_243(var4, var12, var14), var16, var17, false);
      drawLine(new class_243(var4, var6, var8), new class_243(var10, var12, var8), var16, var17, false);
      drawLine(new class_243(var10, var6, var8), new class_243(var4, var12, var8), var16, var17, false);
      drawLine(new class_243(var4, var6, var14), new class_243(var10, var12, var14), var16, var17, false);
      drawLine(new class_243(var10, var6, var14), new class_243(var4, var12, var14), var16, var17, false);
      drawLine(new class_243(var4, var6, var8), new class_243(var4, var12, var14), var16, var17, false);
      drawLine(new class_243(var4, var6, var14), new class_243(var4, var12, var8), var16, var17, false);
      drawLine(new class_243(var10, var6, var8), new class_243(var10, var12, var14), var16, var17, false);
      drawLine(new class_243(var10, var6, var14), new class_243(var10, var12, var8), var16, var17, false);
   }

   public static void drawBoxWithCrossFull(class_238 var0, int var1, int var2, float var3) {
      double var4 = var0.field_1323;
      double var6 = var0.field_1322;
      double var8 = var0.field_1321;
      double var10 = var0.field_1320;
      double var12 = var0.field_1325;
      double var14 = var0.field_1324;
      drawQuad(
         new class_243(var4, var6, var8), new class_243(var10, var6, var8), new class_243(var10, var6, var14), new class_243(var4, var6, var14), var2, false
      );
      drawQuad(
         new class_243(var4, var6, var8), new class_243(var4, var12, var8), new class_243(var10, var12, var8), new class_243(var10, var6, var8), var2, false
      );
      drawQuad(
         new class_243(var10, var6, var8),
         new class_243(var10, var12, var8),
         new class_243(var10, var12, var14),
         new class_243(var10, var6, var14),
         var2,
         false
      );
      drawQuad(
         new class_243(var4, var6, var14),
         new class_243(var10, var6, var14),
         new class_243(var10, var12, var14),
         new class_243(var4, var12, var14),
         var2,
         false
      );
      drawQuad(
         new class_243(var4, var6, var8), new class_243(var4, var6, var14), new class_243(var4, var12, var14), new class_243(var4, var12, var8), var2, false
      );
      drawQuad(
         new class_243(var4, var12, var8),
         new class_243(var4, var12, var14),
         new class_243(var10, var12, var14),
         new class_243(var10, var12, var8),
         var2,
         false
      );
      drawQuad(
         new class_243(var4, var6, var14), new class_243(var10, var6, var14), new class_243(var10, var6, var8), new class_243(var4, var6, var8), var2, false
      );
      drawQuad(
         new class_243(var10, var6, var8), new class_243(var10, var12, var8), new class_243(var4, var12, var8), new class_243(var4, var6, var8), var2, false
      );
      drawQuad(
         new class_243(var10, var6, var14),
         new class_243(var10, var12, var14),
         new class_243(var10, var12, var8),
         new class_243(var10, var6, var8),
         var2,
         false
      );
      drawQuad(
         new class_243(var4, var12, var14),
         new class_243(var10, var12, var14),
         new class_243(var10, var6, var14),
         new class_243(var4, var6, var14),
         var2,
         false
      );
      drawQuad(
         new class_243(var4, var12, var8), new class_243(var4, var12, var14), new class_243(var4, var6, var14), new class_243(var4, var6, var8), var2, false
      );
      drawQuad(
         new class_243(var10, var12, var8),
         new class_243(var10, var12, var14),
         new class_243(var4, var12, var14),
         new class_243(var4, var12, var8),
         var2,
         false
      );
      drawLine(new class_243(var4, var6, var8), new class_243(var10, var6, var8), var1, var3, false);
      drawLine(new class_243(var10, var6, var8), new class_243(var10, var6, var14), var1, var3, false);
      drawLine(new class_243(var10, var6, var14), new class_243(var4, var6, var14), var1, var3, false);
      drawLine(new class_243(var4, var6, var14), new class_243(var4, var6, var8), var1, var3, false);
      drawLine(new class_243(var4, var6, var14), new class_243(var4, var12, var14), var1, var3, false);
      drawLine(new class_243(var4, var6, var8), new class_243(var4, var12, var8), var1, var3, false);
      drawLine(new class_243(var10, var6, var14), new class_243(var10, var12, var14), var1, var3, false);
      drawLine(new class_243(var10, var6, var8), new class_243(var10, var12, var8), var1, var3, false);
      drawLine(new class_243(var4, var12, var8), new class_243(var10, var12, var8), var1, var3, false);
      drawLine(new class_243(var10, var12, var8), new class_243(var10, var12, var14), var1, var3, false);
      drawLine(new class_243(var10, var12, var14), new class_243(var4, var12, var14), var1, var3, false);
      drawLine(new class_243(var4, var12, var14), new class_243(var4, var12, var8), var1, var3, false);
      int var16 = a.d(var1, 0.6F);
      float var17 = var3 * 0.8F;
      drawLine(new class_243(var4, var6, var8), new class_243(var10, var6, var14), var16, var17, false);
      drawLine(new class_243(var10, var6, var8), new class_243(var4, var6, var14), var16, var17, false);
      drawLine(new class_243(var4, var12, var8), new class_243(var10, var12, var14), var16, var17, false);
      drawLine(new class_243(var10, var12, var8), new class_243(var4, var12, var14), var16, var17, false);
      drawLine(new class_243(var4, var6, var8), new class_243(var10, var12, var8), var16, var17, false);
      drawLine(new class_243(var10, var6, var8), new class_243(var4, var12, var8), var16, var17, false);
      drawLine(new class_243(var4, var6, var14), new class_243(var10, var12, var14), var16, var17, false);
      drawLine(new class_243(var10, var6, var14), new class_243(var4, var12, var14), var16, var17, false);
      drawLine(new class_243(var4, var6, var8), new class_243(var4, var12, var14), var16, var17, false);
      drawLine(new class_243(var4, var6, var14), new class_243(var4, var12, var8), var16, var17, false);
      drawLine(new class_243(var10, var6, var8), new class_243(var10, var12, var14), var16, var17, false);
      drawLine(new class_243(var10, var6, var14), new class_243(var10, var12, var8), var16, var17, false);
   }

   public static void drawPlastShape(class_2338 var0, class_243 var1, int var2, int var3) {
      if (mc.field_1724 != null) {
         float var4 = class_3532.method_15393(mc.field_1724.method_36454());
         if (Math.abs(mc.field_1724.method_36455()) > 60.0F) {
            class_2338 var5 = var0.method_10084().method_10079(mc.field_1724.method_58149(), 3);
            class_243 var6 = class_243.method_24954(var5.method_10089(3).method_10077(3).method_10074()).method_1019(var1);
            class_243 var7 = class_243.method_24954(var5.method_10088(2).method_10076(2).method_10084()).method_1019(var1);
            drawBoxWithCrossFull(new class_238(var6, var7), var2, var3, 3.0F);
         } else if (var4 <= -157.5F || var4 >= 157.5F) {
            class_2338 var11 = var0.method_10076(3).method_10084();
            class_243 var15 = class_243.method_24954(var11.method_10087(2).method_10089(3)).method_1019(var1);
            class_243 var19 = class_243.method_24954(var11.method_10086(3).method_10088(2).method_10077(2)).method_1019(var1);
            drawBoxWithCrossFull(new class_238(var15, var19), var2, var3, 3.0F);
         } else if (var4 <= -112.5F) {
            drawSidePlast(var0.method_10089(5).method_10072().method_10074(), var1, var2, var3, -1, true);
         } else if (var4 <= -67.5F) {
            class_2338 var8 = var0.method_10089(2).method_10084();
            class_243 var12 = class_243.method_24954(var8.method_10087(2).method_10077(3)).method_1019(var1);
            class_243 var16 = class_243.method_24954(var8.method_10086(3).method_10076(2).method_10089(2)).method_1019(var1);
            drawBoxWithCrossFull(new class_238(var12, var16), var2, var3, 3.0F);
         } else if (var4 <= -22.5F) {
            drawSidePlast(var0.method_10089(5).method_10074(), var1, var2, var3, 1, false);
         } else if (var4 >= -22.5 && var4 <= 22.5) {
            class_2338 var10 = var0.method_10077(2).method_10084();
            class_243 var14 = class_243.method_24954(var10.method_10087(2).method_10089(3)).method_1019(var1);
            class_243 var18 = class_243.method_24954(var10.method_10086(3).method_10088(2).method_10077(2)).method_1019(var1);
            drawBoxWithCrossFull(new class_238(var14, var18), var2, var3, 3.0F);
         } else if (var4 <= 67.5F) {
            drawSidePlast(var0.method_10088(4).method_10074(), var1, var2, var3, 1, true);
         } else if (var4 <= 112.5F) {
            class_2338 var9 = var0.method_10088(3).method_10084();
            class_243 var13 = class_243.method_24954(var9.method_10087(2).method_10077(3)).method_1019(var1);
            class_243 var17 = class_243.method_24954(var9.method_10086(3).method_10076(2).method_10089(2)).method_1019(var1);
            drawBoxWithCrossFull(new class_238(var13, var17), var2, var3, 3.0F);
         } else if (var4 <= 157.5F) {
            drawSidePlast(var0.method_10088(4).method_10072().method_10074(), var1, var2, var3, -1, false);
         }
      }
   }

   private static void drawSidePlast(class_2338 var0, class_243 var1, int var2, int var3, int var4, boolean var5) {
      class_243 var6 = class_243.method_24954(var0).method_1019(var1);
      int var7 = a.d(var2, 0.6F);
      ArrayList var8 = new ArrayList();
      float var9 = var5 ? var4 : -var4;
      class_243 var10 = var6;
      var8.add(var10);
      var10 = var10.method_1031(var9, 0.0, 0.0);
      var8.add(var10);

      for (int var11 = 0; var11 < 4; var11++) {
         class_243 var17 = var10.method_1031(0.0, 0.0, var4);
         var8.add(var17);
         var10 = var17.method_1031(var9, 0.0, 0.0);
         var8.add(var10);
      }

      var10 = var10.method_1031(0.0, 0.0, var4);
      var8.add(var10);
      var10 = var10.method_1031(var9 * -2.0F, 0.0, 0.0);
      var8.add(var10);

      for (int var26 = 0; var26 < 3; var26++) {
         class_243 var20 = var10.method_1031(0.0, 0.0, var4 * -1);
         var8.add(var20);
         var10 = var20.method_1031(var9 * -1.0F, 0.0, 0.0);
         var8.add(var10);
      }

      var10 = var10.method_1031(0.0, 0.0, var4 * -2);
      var8.add(var10);

      for (int var27 = 0; var27 < var8.size() - 1; var27++) {
         class_243 var12 = (class_243)var8.get(var27);
         class_243 var13 = (class_243)var8.get(var27 + 1);
         drawLine(var12, var13, var2, 2.0F, false);
         drawLine(var12.method_1031(0.0, 5.0, 0.0), var13.method_1031(0.0, 5.0, 0.0), var2, 2.0F, false);
      }

      for (class_243 var32 : var8) {
         drawLine(var32, var32.method_1031(0.0, 5.0, 0.0), var2, 2.0F, false);
      }

      for (int var29 = 0; var29 < var8.size() - 1; var29++) {
         class_243 var33 = (class_243)var8.get(var29);
         class_243 var34 = (class_243)var8.get(var29 + 1);
         class_243 var14 = var33.method_1031(0.0, 5.0, 0.0);
         class_243 var15 = var34.method_1031(0.0, 5.0, 0.0);
         drawQuad(var33, var34, var15, var14, var3, false);
         drawQuad(var14, var15, var34, var33, var3, false);
         drawLine(var33, var15, var7, 1.6F, false);
         drawLine(var34, var14, var7, 1.6F, false);
      }

      var10 = var6;
      drawQuad(var10, var10.method_1031(var9, 0.0, 0.0), var10.method_1031(var9, 0.0, var4 * 2), var10.method_1031(0.0, 0.0, var4 * 2), var3, false);
      drawQuad(var10.method_1031(0.0, 0.0, var4 * 2), var10.method_1031(var9, 0.0, var4 * 2), var10.method_1031(var9, 0.0, 0.0), var10, var3, false);
      drawLine(var10, var10.method_1031(var9, 0.0, var4 * 2), var7, 1.6F, false);
      drawLine(var10.method_1031(var9, 0.0, 0.0), var10.method_1031(0.0, 0.0, var4 * 2), var7, 1.6F, false);

      for (int var30 = 0; var30 < 3; var30++) {
         var10 = var10.method_1031(var9, 0.0, var4);
         drawQuad(var10, var10.method_1031(var9, 0.0, 0.0), var10.method_1031(var9, 0.0, var4 * 2), var10.method_1031(0.0, 0.0, var4 * 2), var3, false);
         drawQuad(var10.method_1031(0.0, 0.0, var4 * 2), var10.method_1031(var9, 0.0, var4 * 2), var10.method_1031(var9, 0.0, 0.0), var10, var3, false);
         drawLine(var10, var10.method_1031(var9, 0.0, var4 * 2), var7, 1.6F, false);
         drawLine(var10.method_1031(var9, 0.0, 0.0), var10.method_1031(0.0, 0.0, var4 * 2), var7, 1.6F, false);
      }

      var10 = var10.method_1031(var9, 0.0, var4);
      drawQuad(var10, var10.method_1031(var9, 0.0, 0.0), var10.method_1031(var9, 0.0, var4), var10.method_1031(0.0, 0.0, var4), var3, false);
      drawQuad(var10.method_1031(0.0, 0.0, var4), var10.method_1031(var9, 0.0, var4), var10.method_1031(var9, 0.0, 0.0), var10, var3, false);
      drawLine(var10, var10.method_1031(var9, 0.0, var4), var7, 1.6F, false);
      drawLine(var10.method_1031(var9, 0.0, 0.0), var10.method_1031(0.0, 0.0, var4), var7, 1.6F, false);
      var10 = var6.method_1031(0.0, 5.0, 0.0);
      drawQuad(var10, var10.method_1031(0.0, 0.0, var4 * 2), var10.method_1031(var9, 0.0, var4 * 2), var10.method_1031(var9, 0.0, 0.0), var3, false);
      drawQuad(var10.method_1031(var9, 0.0, 0.0), var10.method_1031(var9, 0.0, var4 * 2), var10.method_1031(0.0, 0.0, var4 * 2), var10, var3, false);
      drawLine(var10, var10.method_1031(var9, 0.0, var4 * 2), var7, 1.6F, false);
      drawLine(var10.method_1031(var9, 0.0, 0.0), var10.method_1031(0.0, 0.0, var4 * 2), var7, 1.6F, false);

      for (int var31 = 0; var31 < 3; var31++) {
         var10 = var10.method_1031(var9, 0.0, var4);
         drawQuad(var10, var10.method_1031(0.0, 0.0, var4 * 2), var10.method_1031(var9, 0.0, var4 * 2), var10.method_1031(var9, 0.0, 0.0), var3, false);
         drawQuad(var10.method_1031(var9, 0.0, 0.0), var10.method_1031(var9, 0.0, var4 * 2), var10.method_1031(0.0, 0.0, var4 * 2), var10, var3, false);
         drawLine(var10, var10.method_1031(var9, 0.0, var4 * 2), var7, 1.6F, false);
         drawLine(var10.method_1031(var9, 0.0, 0.0), var10.method_1031(0.0, 0.0, var4 * 2), var7, 1.6F, false);
      }

      var10 = var10.method_1031(var9, 0.0, var4);
      drawQuad(var10, var10.method_1031(0.0, 0.0, var4), var10.method_1031(var9, 0.0, var4), var10.method_1031(var9, 0.0, 0.0), var3, false);
      drawQuad(var10.method_1031(var9, 0.0, 0.0), var10.method_1031(var9, 0.0, var4), var10.method_1031(0.0, 0.0, var4), var10, var3, false);
      drawLine(var10, var10.method_1031(var9, 0.0, var4), var7, 1.6F, false);
      drawLine(var10.method_1031(var9, 0.0, 0.0), var10.method_1031(0.0, 0.0, var4), var7, 1.6F, false);
   }

   private static double lerp(double var0, double var2, double var4) {
      return var0 + (var2 - var0) * var4;
   }

   public static void drawGradientQuad(class_243 var0, class_243 var1, class_243 var2, class_243 var3, int var4, int var5, int var6, int var7, boolean var8) {
      Render3D.GradientQuad var9 = new Render3D.GradientQuad(var0, var1, var2, var3, var4, var5, var6, var7);
      if (var8) {
         GRADIENT_QUAD_DEPTH.add(var9);
      } else {
         GRADIENT_QUAD.add(var9);
      }
   }

   public static void drawLineGradient(class_243 var0, class_243 var1, int var2, int var3, float var4, boolean var5) {
      Render3D.Line var6 = new Render3D.Line(null, var0, var1, var2, var3, var4);
      if (var5) {
         LINE_DEPTH.add(var6);
      } else {
         LINE.add(var6);
      }
   }

   public static Vector3f getNormal(Vector3f var0, Vector3f var1) {
      Vector3f var2 = new Vector3f(var0).sub(var1);
      float var3 = class_3532.method_15355(var2.lengthSquared());
      return var3 < 1.0E-4F ? new Vector3f(0.0F, 1.0F, 0.0F) : var2.div(var3);
   }

   public static void drawShape(class_2338 var0, class_265 var1, int var2, float var3) {
      drawShape(var0, var1, var2, var3, true, false);
   }

   public static void drawShape(class_2338 var0, class_265 var1, int var2, float var3, boolean var4, boolean var5) {
      if (SHAPE_BOXES.containsKey(var1)) {
         SHAPE_BOXES.get(var1).forEach(var5x -> {
            class_238 var6 = var5x.method_996(var0);
            drawBox(var6, var2, var3, true, var4, var5);
         });
      } else {
         SHAPE_BOXES.put(var1, var1.method_1090());
      }
   }

   public static void drawShapeAlternative(class_2338 var0, class_265 var1, int var2, float var3, boolean var4, boolean var5) {
      class_243 var6 = class_243.method_24954(var0);
      if (SHAPE_OUTLINES.containsKey(var1)) {
         class_3545 var8 = SHAPE_OUTLINES.get(var1);
         if (var4) {
            ((List)var8.method_15442()).forEach(var4x -> drawBox(var4x.method_997(var6), var2, var3, false, true, var5));
         }

         ((List)var8.method_15441()).forEach(var4x -> drawLine(var4x.start.method_1019(var6), var4x.end.method_1019(var6), var2, var3, var5));
      } else {
         ArrayList var7 = new ArrayList();
         var1.method_1104(
            (var1x, var3x, var5x, var7x, var9, var11) -> var7.add(
               new Render3D.Line(null, new class_243(var1x, var3x, var5x), new class_243(var7x, var9, var11), 0, 0, 0.0F)
            )
         );
         SHAPE_OUTLINES.put(var1, new class_3545(var1.method_1090(), var7));
      }
   }

   public static void drawBox(class_238 var0, int var1, float var2) {
      drawBox(var0, var1, var2, true, true, false);
   }

   public static void drawBox(class_238 var0, int var1, float var2, boolean var3, boolean var4, boolean var5) {
      drawBox(null, var0, var1, var2, var3, var4, var5);
   }

   public static void drawBox(class_4665 var0, class_238 var1, int var2, float var3, boolean var4, boolean var5, boolean var6) {
      double var7 = var1.field_1323;
      double var9 = var1.field_1322;
      double var11 = var1.field_1321;
      double var13 = var1.field_1320;
      double var15 = var1.field_1325;
      double var17 = var1.field_1324;
      if (var5) {
         int var19 = a.d(var2, 0.3F);
         drawQuad(
            var0,
            new class_243(var7, var9, var11),
            new class_243(var13, var9, var11),
            new class_243(var13, var9, var17),
            new class_243(var7, var9, var17),
            var19,
            var6
         );
         drawQuad(
            var0,
            new class_243(var7, var9, var11),
            new class_243(var7, var15, var11),
            new class_243(var13, var15, var11),
            new class_243(var13, var9, var11),
            var19,
            var6
         );
         drawQuad(
            var0,
            new class_243(var13, var9, var11),
            new class_243(var13, var15, var11),
            new class_243(var13, var15, var17),
            new class_243(var13, var9, var17),
            var19,
            var6
         );
         drawQuad(
            var0,
            new class_243(var7, var9, var17),
            new class_243(var13, var9, var17),
            new class_243(var13, var15, var17),
            new class_243(var7, var15, var17),
            var19,
            var6
         );
         drawQuad(
            var0,
            new class_243(var7, var9, var11),
            new class_243(var7, var9, var17),
            new class_243(var7, var15, var17),
            new class_243(var7, var15, var11),
            var19,
            var6
         );
         drawQuad(
            var0,
            new class_243(var7, var15, var11),
            new class_243(var7, var15, var17),
            new class_243(var13, var15, var17),
            new class_243(var13, var15, var11),
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
      class_4665 var0, double var1, double var3, double var5, double var7, double var9, double var11, int var13, float var14, boolean var15
   ) {
      drawLine(var0, new class_243(var1, var3, var5), new class_243(var7, var9, var11), var13, var13, var14, var15);
   }

   public static void drawLine(class_243 var0, class_243 var1, int var2, float var3, boolean var4) {
      drawLine(null, var0, var1, var2, var2, var3, var4);
   }

   public static void drawLine(class_4665 var0, class_243 var1, class_243 var2, int var3, int var4, float var5, boolean var6) {
      Render3D.Line var7 = new Render3D.Line(var0, var1, var2, var3, var4, var5);
      if (var6) {
         LINE_DEPTH.add(var7);
      } else {
         LINE.add(var7);
      }
   }

   public static void drawQuad(class_243 var0, class_243 var1, class_243 var2, class_243 var3, int var4, boolean var5) {
      drawQuad(null, var0, var1, var2, var3, var4, var5);
   }

   public static void drawQuad(class_4665 var0, class_243 var1, class_243 var2, class_243 var3, class_243 var4, int var5, boolean var6) {
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

   public static void setLastWorldSpaceEntry(class_4665 var0) {
      lastWorldSpaceEntry = var0;
   }

   public static void setLastTickDelta(float var0) {
      lastTickDelta = var0;
   }

   public static void setLastCameraPos(class_243 var0) {
      lastCameraPos = var0;
   }

   public static void setLastCameraRotation(Quaternionf var0) {
      lastCameraRotation = var0;
   }

   public record GradientQuad() {
      private final class_243 p1;
      private final class_243 p2;
      private final class_243 p3;
      private final class_243 p4;
      private final int c1;
      private final int c2;
      private final int c3;
      private final int c4;

      public GradientQuad(class_243 var1, class_243 var2, class_243 var3, class_243 var4, int var5, int var6, int var7, int var8) {
         this.p1 = var1;
         this.p2 = var2;
         this.p3 = var3;
         this.p4 = var4;
         this.c1 = var5;
         this.c2 = var6;
         this.c3 = var7;
         this.c4 = var8;
      }
   }

   public record Line() {
      private final class_4665 entry;
      private final class_243 start;
      private final class_243 end;
      private final int colorStart;
      private final int colorEnd;
      private final float width;

      public Line(class_4665 var1, class_243 var2, class_243 var3, int var4, int var5, float var6) {
         this.entry = var1;
         this.start = var2;
         this.end = var3;
         this.colorStart = var4;
         this.colorEnd = var5;
         this.width = var6;
      }
   }

   public record Quad() {
      private final class_4665 entry;
      private final class_243 x;
      private final class_243 y;
      private final class_243 w;
      private final class_243 z;
      private final int color;

      public Quad(class_4665 var1, class_243 var2, class_243 var3, class_243 var4, class_243 var5, int var6) {
         this.entry = var1;
         this.x = var2;
         this.y = var3;
         this.w = var4;
         this.z = var5;
         this.color = var6;
      }
   }
}
