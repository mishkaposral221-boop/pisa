package rich.util.math;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix3x2fStack;
import org.joml.Vector3d;
import rich.IMinecraft;

public final class MathUtils implements IMinecraft {
   public static double PI2 = Math.PI * 2;
   private static float contextAlpha = 1.0F;

   public static boolean isHovered(double var0, double var2, double var4, double var6, double var8, double var10) {
      return var0 >= var4 && var0 <= var4 + var8 && var2 >= var6 && var2 <= var6 + var10;
   }

   public static float clamp(float var0, float var1, float var2) {
      return var0 < var1 ? var1 : Math.min(var0, var2);
   }

   public static double computeGcd() {
      return Math.pow((Double)mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0) * 1.2;
   }

   public static int getRandom(int var0, int var1) {
      return (int)getRandom(var0, var1 + 1.0F);
   }

   public static float getRandom(float var0, float var1) {
      return (float)getRandom((double)var0, (double)var1);
   }

   public static double getRandom(double var0, double var2) {
      if (var0 == var2) {
         return var0;
      }

      if (var0 > var2) {
         double var4 = var0;
         var0 = var2;
         var2 = var4;
      }

      return ThreadLocalRandom.current().nextDouble(var0, var2);
   }

   public static void scale(Matrix3x2fStack var0, float var1, float var2, float var3, float var4, Runnable var5) {
      float var6 = var3 * var4;
      if (var6 != 1.0F && var6 > 0.0F) {
         float var7 = contextAlpha;
         contextAlpha = var6;
         var0.pushMatrix();
         var0.translate(var1, var2);
         var0.scale(var3, var4);
         var0.translate(-var1, -var2);
         var5.run();
         var0.popMatrix();
         contextAlpha = var7;
      } else if (var6 >= 1.0F) {
         var5.run();
      }
   }

   public static float textScrolling(float var0) {
      int var1 = (int)(var0 * 75.0F);
      return (float)MathHelper.clamp(System.currentTimeMillis() % var1 * Math.PI / var1, 0.0, 1.0) * var0;
   }

   public static double round(double var0, double var2) {
      double var4 = Math.round(var0 / var2) * var2;
      return Math.round(var4 * 100.0) / 100.0;
   }

   public static int floorNearestMulN(int var0, int var1) {
      return var1 * (int)Math.floor((double)var0 / var1);
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

   public static int applyOpacity(int var0, float var1) {
      return ColorHelper.getArgb((int)(getAlpha(var0) * var1 / 255.0F), getRed(var0), getGreen(var0), getBlue(var0));
   }

   public static int applyContextAlpha(int var0) {
      int var1 = (int)(getAlpha(var0) * contextAlpha);
      return ColorHelper.getArgb(var1, getRed(var0), getGreen(var0), getBlue(var0));
   }

   public static Vec3d cosSin(int var0, int var1, double var2) {
      int var4 = Math.min(var0, var1);
      float var5 = (float)(Math.cos(var4 * PI2 / var1) * var2);
      float var6 = (float)(-Math.sin(var4 * PI2 / var1) * var2);
      return new Vec3d(var5, 0.0, var6);
   }

   public static double absSinAnimation(double var0) {
      return Math.abs(1.0 + Math.sin(var0)) / 2.0;
   }

   public static Vector3d interpolate(Vector3d var0, Vector3d var1) {
      return new Vector3d(interpolate(var0.x, var1.x), interpolate(var0.y, var1.y), interpolate(var0.z, var1.z));
   }

   public static float interpolate(float var0, float var1, float var2) {
      return var0 + (var1 - var0) * var2;
   }

   public static Vec3d interpolate(Vec3d var0, Vec3d var1) {
      return new Vec3d(
         interpolate(var0.x, var1.x), interpolate(var0.y, var1.y), interpolate(var0.z, var1.z)
      );
   }

   public static Vec3d interpolate(Entity var0) {
      return var0 == null
         ? Vec3d.ZERO
         : new Vec3d(
            interpolate(var0.lastX, var0.getX()),
            interpolate(var0.lastY, var0.getY()),
            interpolate(var0.lastZ, var0.getZ())
         );
   }

   public static float interpolate(float var0, float var1) {
      return MathHelper.lerp(tickCounter.getTickProgress(false), var0, var1);
   }

   public static double interpolate(double var0, double var2) {
      return MathHelper.lerp(tickCounter.getTickProgress(false), var0, var2);
   }

   public static float interpolateSmooth(double var0, float var2, float var3) {
      return (float)MathHelper.lerp(tickCounter.getFixedDeltaTicks() / var0, var2, var3);
   }

   private MathUtils() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static float getContextAlpha() {
      return contextAlpha;
   }
}
