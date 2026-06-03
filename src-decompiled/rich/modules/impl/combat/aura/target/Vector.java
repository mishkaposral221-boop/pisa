package rich.modules.impl.combat.aura.target;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import rich.IMinecraft;
import rich.util.timer.StopWatch;

public class Vector {
   private static final Random random = new Random();
   private static final StopWatch pointTimer = new StopWatch();
   private static final StopWatch updateTimer = new StopWatch();
   private static List<class_243> cachedPoints = new ArrayList<>();
   private static int currentPointIndex = 0;

   public static class_243 hitbox(class_1297 var0, float var1, float var2, float var3, float var4) {
      double var5 = var0.method_17681() / var4;
      double var7 = class_3532.method_15350(var0.method_23320() - var0.method_23318(), 0.0, var0.method_17682());
      double var9 = class_3532.method_15350(IMinecraft.mc.field_1724.method_23317() - var0.method_23317(), -var5, var5);
      double var11 = class_3532.method_15350(IMinecraft.mc.field_1724.method_23321() - var0.method_23321(), -var5, var5);
      return new class_243(var0.method_23317() + var9 / var1, var0.method_23318() + var7 / var2, var0.method_23321() + var11 / var3);
   }

   public static class_243 brain(class_1297 var0, float var1, float var2) {
      double var3 = IMinecraft.mc.field_1724.method_73189().method_1022(var0.method_73189());
      double var5 = class_3532.method_15350((var3 - var1) / (var2 - var1), 0.0, 1.0);
      double var7 = var5;
      double var9 = 0.2;
      double var11 = 0.8;
      double var13 = var9 + (var11 - var9) * var7;
      double var15 = var0.method_23318() + var0.method_17682() * var13;
      return new class_243(var0.method_23317(), var15, var0.method_23321());
   }

   public static class_243 custom(class_1297 var0, int var1, float var2) {
      if (updateTimer.every(1000.0) || cachedPoints.isEmpty()) {
         generateRandomPoints(var0, var1);
         currentPointIndex = 0;
         pointTimer.reset();
      }

      if (pointTimer.finished(var2)) {
         currentPointIndex = (currentPointIndex + 1) % cachedPoints.size();
         pointTimer.reset();
      }

      return cachedPoints.isEmpty() ? var0.method_73189() : cachedPoints.get(currentPointIndex);
   }

   private static void generateRandomPoints(class_1297 var0, int var1) {
      cachedPoints.clear();
      double var2 = var0.method_17681();
      double var4 = var0.method_17682();
      class_243 var6 = var0.method_73189();

      for (int var7 = 0; var7 < var1; var7++) {
         double var8 = var6.field_1352 + (random.nextDouble() - 0.5) * var2;
         double var10 = var6.field_1351 + random.nextDouble() * var4;
         double var12 = var6.field_1350 + (random.nextDouble() - 0.5) * var2;
         cachedPoints.add(new class_243(var8, var10, var12));
      }
   }

   public static List<class_243> getAllCachedPoints() {
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

   public static void forceUpdate(class_1297 var0, int var1) {
      generateRandomPoints(var0, var1);
      currentPointIndex = 0;
      pointTimer.reset();
      updateTimer.reset();
   }
}
