package rich.modules.impl.combat.aura.target;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import rich.IMinecraft;
import rich.util.timer.StopWatch;

public class Vector {
   private static final Random random = new Random();
   private static final StopWatch pointTimer = new StopWatch();
   private static final StopWatch updateTimer = new StopWatch();
   private static List<Vec3d> cachedPoints = new ArrayList<>();
   private static int currentPointIndex = 0;

   public static Vec3d hitbox(Entity var0, float var1, float var2, float var3, float var4) {
      double var5 = var0.getWidth() / var4;
      double var7 = MathHelper.clamp(var0.getEyeY() - var0.getY(), 0.0, var0.getHeight());
      double var9 = MathHelper.clamp(IMinecraft.mc.player.getX() - var0.getX(), -var5, var5);
      double var11 = MathHelper.clamp(IMinecraft.mc.player.getZ() - var0.getZ(), -var5, var5);
      return new Vec3d(var0.getX() + var9 / var1, var0.getY() + var7 / var2, var0.getZ() + var11 / var3);
   }

   public static Vec3d brain(Entity var0, float var1, float var2) {
      double var3 = IMinecraft.mc.player.getEntityPos().distanceTo(var0.getEntityPos());
      double var5 = MathHelper.clamp((var3 - var1) / (var2 - var1), 0.0, 1.0);
      double var7 = var5;
      double var9 = 0.2;
      double var11 = 0.8;
      double var13 = var9 + (var11 - var9) * var7;
      double var15 = var0.getY() + var0.getHeight() * var13;
      return new Vec3d(var0.getX(), var15, var0.getZ());
   }

   public static Vec3d custom(Entity var0, int var1, float var2) {
      if (updateTimer.every(1000.0) || cachedPoints.isEmpty()) {
         generateRandomPoints(var0, var1);
         currentPointIndex = 0;
         pointTimer.reset();
      }

      if (pointTimer.finished(var2)) {
         currentPointIndex = (currentPointIndex + 1) % cachedPoints.size();
         pointTimer.reset();
      }

      return cachedPoints.isEmpty() ? var0.getEntityPos() : cachedPoints.get(currentPointIndex);
   }

   private static void generateRandomPoints(Entity var0, int var1) {
      cachedPoints.clear();
      double var2 = var0.getWidth();
      double var4 = var0.getHeight();
      Vec3d var6 = var0.getEntityPos();

      for (int var7 = 0; var7 < var1; var7++) {
         double var8 = var6.x + (random.nextDouble() - 0.5) * var2;
         double var10 = var6.y + random.nextDouble() * var4;
         double var12 = var6.z + (random.nextDouble() - 0.5) * var2;
         cachedPoints.add(new Vec3d(var8, var10, var12));
      }
   }

   public static List<Vec3d> getAllCachedPoints() {
      return new ArrayList<>(cachedPoints);
   }

   public static int getCurrentPointIndex() {
      return currentPointIndex;
   }

   public static long getTimeSinceLastSwitch() {
      return pointTimer.elapsedTime();
   }

   public static void clearCache() {
      cachedPoints.clear();
      currentPointIndex = 0;
      pointTimer.reset();
      updateTimer.reset();
   }

   public static void forceUpdate(Entity var0, int var1) {
      generateRandomPoints(var0, var1);
      currentPointIndex = 0;
      pointTimer.reset();
      updateTimer.reset();
   }
}
