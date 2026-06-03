package rich.util.math;

import net.minecraft.class_1297;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4604;
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4d;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import rich.IMinecraft;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.MathAngle;
import rich.util.render.Render3D;

public final class Projection implements IMinecraft {
   private static final double NEAR_PLANE = 0.05;

   public static class_243 worldSpaceToScreenSpace(class_243 var0) {
      class_4184 var1 = mc.method_1561().field_4686;
      if (var1 == null) {
         return class_243.field_1353;
      }

      int var2 = mc.method_22683().method_4507();
      int[] var3 = new int[4];
      GL11.glGetIntegerv(2978, var3);
      Vector3f var4 = new Vector3f();
      double var5 = var0.field_1352 - var1.method_71156().field_1352;
      double var7 = var0.field_1351 - var1.method_71156().field_1351;
      double var9 = var0.field_1350 - var1.method_71156().field_1350;
      Vector4f var11 = new Vector4f((float)var5, (float)var7, (float)var9, 1.0F);
      var11.mul(Render3D.lastWorldSpaceMatrix);
      Matrix4f var12 = new Matrix4f(Render3D.lastProjMat);
      Matrix4f var13 = new Matrix4f(Render3D.lastModMat);
      var12.mul(var13).project(var11.x(), var11.y(), var11.z(), var3, var4);
      return new class_243(var4.x / mc.method_22683().method_4495(), (var2 - var4.y) / mc.method_22683().method_4495(), var4.z);
   }

   private static Matrix4d toMatrix4d(Matrix4f var0) {
      Matrix4d var1 = new Matrix4d();
      var1.m00(var0.m00()).m01(var0.m01()).m02(var0.m02()).m03(var0.m03());
      var1.m10(var0.m10()).m11(var0.m11()).m12(var0.m12()).m13(var0.m13());
      var1.m20(var0.m20()).m21(var0.m21()).m22(var0.m22()).m23(var0.m23());
      var1.m30(var0.m30()).m31(var0.m31()).m32(var0.m32()).m33(var0.m33());
      return var1;
   }

   private static double getViewZ(class_243 var0, class_243 var1) {
      double var2 = var0.field_1352 - var1.field_1352;
      double var4 = var0.field_1351 - var1.field_1351;
      double var6 = var0.field_1350 - var1.field_1350;
      Matrix4d var8 = toMatrix4d(Render3D.lastWorldSpaceMatrix);
      Vector4d var9 = new Vector4d(var2, var4, var6, 1.0);
      var8.transform(var9);
      return -var9.z;
   }

   private static Projection.ClipResult worldSpaceToClipSpaceDouble(class_243 var0, class_243 var1) {
      double var2 = var0.field_1352 - var1.field_1352;
      double var4 = var0.field_1351 - var1.field_1351;
      double var6 = var0.field_1350 - var1.field_1350;
      Matrix4d var8 = toMatrix4d(Render3D.lastWorldSpaceMatrix);
      Vector4d var9 = new Vector4d(var2, var4, var6, 1.0);
      var8.transform(var9);
      Matrix4d var10 = toMatrix4d(Render3D.lastProjMat);
      Matrix4d var11 = toMatrix4d(Render3D.lastModMat);
      Matrix4d var12 = new Matrix4d(var10).mul(var11);
      double var13 = var12.m00() * var9.x + var12.m10() * var9.y + var12.m20() * var9.z + var12.m30() * var9.w;
      double var15 = var12.m01() * var9.x + var12.m11() * var9.y + var12.m21() * var9.z + var12.m31() * var9.w;
      double var17 = var12.m02() * var9.x + var12.m12() * var9.y + var12.m22() * var9.z + var12.m32() * var9.w;
      double var19 = var12.m03() * var9.x + var12.m13() * var9.y + var12.m23() * var9.z + var12.m33() * var9.w;
      return new Projection.ClipResult(var13, var15, var17, var19, -var9.z);
   }

   private static class_243 clipToScreenDouble(Projection.ClipResult var0, int[] var1, int var2, double var3) {
      double var5 = var0.w;
      if (Math.abs(var5) < 1.0E-14) {
         return new class_243(var1[2] / var3 / 2.0, var2 / var3 / 2.0, 0.0);
      }

      double var7 = 1.0 / var5;
      double var9 = var0.x * var7;
      double var11 = var0.y * var7;
      double var13 = var0.z * var7;
      double var15 = (var9 * 0.5 + 0.5) * var1[2] / var3;
      double var17 = (var2 - (var11 * 0.5 + 0.5) * var1[3]) / var3;
      double var19 = var13 * 0.5 + 0.5;
      return new class_243(var15, var17, var5 > 0.0 ? var19 : -1.0);
   }

   public static double getDistanceToGround() {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         for (double var0 = mc.field_1724.method_23318(); var0 > 0.0; var0 -= 0.1) {
            if (!mc.field_1687.method_8320(mc.field_1724.method_24515().method_10087((int)(mc.field_1724.method_23318() - var0 + 1.0))).method_26215()) {
               return mc.field_1724.method_23318() - var0;
            }
         }

         return 256.0;
      } else {
         return 256.0;
      }
   }

   public static class_243 interpolate(class_1297 var0) {
      float var1 = mc.method_61966().method_60637(true);
      return var0.method_30950(var1);
   }

   public static class_243 interpolate(class_1297 var0, float var1) {
      return var0.method_30950(var1);
   }

   public static Vector4d getVector4D(class_1297 var0) {
      return getVector4D(var0, mc.method_61966().method_60637(true));
   }

   public static Vector4d getVector4D(class_1297 var0, float var1) {
      if (var0 == null) {
         return null;
      }

      class_4184 var2 = mc.method_1561().field_4686;
      if (var2 == null) {
         return null;
      }

      class_243 var3 = var2.method_71156();
      class_243 var4 = var0.method_30950(var1);
      class_243 var5 = var0.method_73189();
      class_238 var6 = var0.method_5829().method_997(var4.method_1020(var5));
      class_243 var7 = var6.method_1005();
      double var8 = getViewZ(var7, var3);
      if (var8 < -5.0) {
         return null;
      }

      int var10 = mc.method_22683().method_4507();
      int[] var11 = new int[4];
      GL11.glGetIntegerv(2978, var11);
      double var12 = mc.method_22683().method_4495();
      double var14 = mc.method_22683().method_4486();
      double var16 = mc.method_22683().method_4502();
      class_243[] var18 = new class_243[]{
         new class_243(var6.field_1323, var6.field_1322, var6.field_1321),
         new class_243(var6.field_1323, var6.field_1322, var6.field_1324),
         new class_243(var6.field_1320, var6.field_1322, var6.field_1321),
         new class_243(var6.field_1320, var6.field_1322, var6.field_1324),
         new class_243(var6.field_1323, var6.field_1325, var6.field_1321),
         new class_243(var6.field_1323, var6.field_1325, var6.field_1324),
         new class_243(var6.field_1320, var6.field_1325, var6.field_1321),
         new class_243(var6.field_1320, var6.field_1325, var6.field_1324)
      };
      int[][] var19 = new int[][]{{0, 1}, {0, 2}, {1, 3}, {2, 3}, {4, 5}, {4, 6}, {5, 7}, {6, 7}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};
      Projection.ClipResult[] var20 = new Projection.ClipResult[8];

      for (int var21 = 0; var21 < 8; var21++) {
         var20[var21] = worldSpaceToClipSpaceDouble(var18[var21], var3);
         if (var20[var21] == null) {
            return null;
         }
      }

      double var48 = Double.MAX_VALUE;
      double var23 = Double.MAX_VALUE;
      double var25 = -Double.MAX_VALUE;
      double var27 = -Double.MAX_VALUE;
      int var29 = 0;

      for (int var30 = 0; var30 < 8; var30++) {
         Projection.ClipResult var31 = var20[var30];
         if (var31.viewZ > 0.05) {
            var29++;
            class_243 var32 = clipToScreenDouble(var31, var11, var10, var12);
            double var33 = clampScreenX(var32.field_1352, var14);
            double var35 = clampScreenY(var32.field_1351, var16);
            var48 = Math.min(var48, var33);
            var23 = Math.min(var23, var35);
            var25 = Math.max(var25, var33);
            var27 = Math.max(var27, var35);
         }
      }

      for (int[] var56 : var19) {
         Projection.ClipResult var34 = var20[var56[0]];
         Projection.ClipResult var57 = var20[var56[1]];
         boolean var36 = var34.viewZ > 0.05;
         boolean var37 = var57.viewZ > 0.05;
         if (var36 != var37) {
            double var38 = var57.viewZ - var34.viewZ;
            if (!(Math.abs(var38) < 1.0E-14)) {
               double var40 = (0.05 - var34.viewZ) / var38;
               var40 = Math.max(0.0, Math.min(1.0, var40));
               Projection.ClipResult var42 = new Projection.ClipResult(
                  var34.x + var40 * (var57.x - var34.x),
                  var34.y + var40 * (var57.y - var34.y),
                  var34.z + var40 * (var57.z - var34.z),
                  var34.w + var40 * (var57.w - var34.w),
                  0.05
               );
               class_243 var43 = clipToScreenDouble(var42, var11, var10, var12);
               double var44 = clampScreenX(var43.field_1352, var14);
               double var46 = clampScreenY(var43.field_1351, var16);
               var48 = Math.min(var48, var44);
               var23 = Math.min(var23, var46);
               var25 = Math.max(var25, var44);
               var27 = Math.max(var27, var46);
            }
         }
      }

      if (var29 == 0 && var48 == Double.MAX_VALUE) {
         return null;
      } else if (!(var25 <= var48) && !(var27 <= var23)) {
         var48 = Math.max(-var14, var48);
         var23 = Math.max(-var16, var23);
         var25 = Math.min(var14 * 2.0, var25);
         var27 = Math.min(var16 * 2.0, var27);
         return new Vector4d(var48, var23, var25, var27);
      } else {
         return null;
      }
   }

   private static double clampScreenX(double var0, double var2) {
      return Math.max(-var2 * 2.0, Math.min(var2 * 3.0, var0));
   }

   private static double clampScreenY(double var0, double var2) {
      return Math.max(-var2 * 2.0, Math.min(var2 * 3.0, var0));
   }

   private static boolean isPointInFrontDouble(class_243 var0, class_243 var1, class_4184 var2) {
      double var3 = var0.field_1352 - var1.field_1352;
      double var5 = var0.field_1351 - var1.field_1351;
      double var7 = var0.field_1350 - var1.field_1350;
      double var9 = var2.method_19330();
      double var11 = var2.method_19329();
      double var13 = Math.toRadians(var9);
      double var15 = Math.toRadians(var11);
      double var17 = Math.cos(var15);
      double var19 = -Math.sin(var13) * var17;
      double var21 = -Math.sin(var15);
      double var23 = Math.cos(var13) * var17;
      double var25 = var19 * var3 + var21 * var5 + var23 * var7;
      return var25 > -10.0;
   }

   public static boolean canSee(class_243 var0) {
      class_4184 var1 = mc.field_1773.method_19418();
      if (var1 == null) {
         return false;
      }

      Angle var2 = MathAngle.fromVec3d(var0.method_1020(var1.method_71156()));
      return Math.abs(class_3532.method_15393(var2.getYaw() - var1.method_19330())) < 90.0F
            && Math.abs(class_3532.method_15393(var2.getPitch() - var1.method_19329())) < 60.0F
         || canSee(new class_238(class_2338.method_49638(var0)));
   }

   public static boolean canSee(class_238 var0) {
      if (var0 != null && mc.field_1773 != null) {
         class_4184 var1 = mc.field_1773.method_19418();
         if (var1 != null && var1.method_19332()) {
            class_243 var2 = var1.method_71156();
            Matrix4f var3 = new Matrix4f().rotation(var1.method_23767());
            Matrix4f var4 = mc.field_1773.method_22973(((Integer)mc.field_1690.method_41808().method_41753()).floatValue());
            class_4604 var5 = new class_4604(var3, var4);
            var5.method_23088(var2.field_1352, var2.field_1351, var2.field_1350);
            return var5.method_23093(var0);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean cantSee(Vector4d var0) {
      if (var0 == null) {
         return true;
      } else {
         double var1 = mc.method_22683().method_4486();
         double var3 = mc.method_22683().method_4502();
         if (Double.isNaN(var0.x) || Double.isNaN(var0.y) || Double.isNaN(var0.z) || Double.isNaN(var0.w)) {
            return true;
         } else if (Double.isInfinite(var0.x) || Double.isInfinite(var0.y) || Double.isInfinite(var0.z) || Double.isInfinite(var0.w)) {
            return true;
         } else {
            return var0.z < -var1 || var0.x > var1 * 2.0 ? true : var0.w < -var3 || var0.y > var3 * 2.0;
         }
      }
   }

   public static double centerX(Vector4d var0) {
      return var0.x + (var0.z - var0.x) / 2.0;
   }

   public static boolean isInFrontOfCamera(class_243 var0) {
      class_4184 var1 = mc.field_1773.method_19418();
      return var1 != null && var1.method_19332() ? isPointInFrontDouble(var0, var1.method_71156(), var1) : false;
   }

   private Projection() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   private record ClipResult() {
      private final double x;
      private final double y;
      private final double z;
      private final double w;
      private final double viewZ;

      private ClipResult(double var1, double var3, double var5, double var7, double var9) {
         this.x = var1;
         this.y = var3;
         this.z = var5;
         this.w = var7;
         this.viewZ = var9;
      }
   }
}
