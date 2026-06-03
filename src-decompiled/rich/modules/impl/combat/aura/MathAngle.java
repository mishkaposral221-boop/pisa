package rich.modules.impl.combat.aura;

import net.minecraft.class_1297;
import net.minecraft.class_1675;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3966;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import org.jetbrains.annotations.NotNull;
import rich.IMinecraft;

public final class MathAngle implements IMinecraft {
   public static Angle fromVec2f(class_241 var0) {
      return new Angle(var0.field_1342, var0.field_1343);
   }

   public static float computeAngleDifference(float var0, float var1) {
      return class_3532.method_15393(var0 - var1);
   }

   public static Angle fromVec3d(class_243 var0) {
      return new Angle(
         (float)class_3532.method_15338(Math.toDegrees(Math.atan2(var0.field_1350, var0.field_1352)) - 90.0),
         (float)class_3532.method_15338(Math.toDegrees(-Math.atan2(var0.field_1351, Math.hypot(var0.field_1352, var0.field_1350))))
      );
   }

   public static Angle calculateDelta(Angle var0, Angle var1) {
      float var2 = class_3532.method_15393(var1.getYaw() - var0.getYaw());
      float var3 = class_3532.method_15393(var1.getPitch() - var0.getPitch());
      return new Angle(var2, var3);
   }

   public static Angle calculateAngle(class_243 var0) {
      return fromVec3d(var0.method_1020(mc.field_1724.method_33571()));
   }

   public static Angle pitch(float var0) {
      return new Angle(mc.field_1724.method_36454(), var0);
   }

   public static Angle cameraAngle() {
      return new Angle(mc.field_1724.method_36454(), mc.field_1724.method_36455());
   }

   public static boolean rayTrace(float var0, float var1, float var2, float var3, class_1297 var4) {
      class_239 var5 = rayTrace(var2, var0, var1);
      class_243 var6 = mc.field_1724.method_73189().method_1031(0.0, mc.field_1724.method_18381(mc.field_1724.method_18376()), 0.0);
      double var7 = Math.pow(var2, 2.0);
      if (var5 != null) {
         var7 = var6.method_1025(var5.method_17784());
      }

      class_243 var9 = getRotationVector(var1, var0).method_1021(var2);
      class_243 var10 = var6.method_1019(var9);
      class_238 var11 = mc.field_1724.method_5829().method_18804(var9).method_1009(1.0, 1.0, 1.0);
      double var13 = Math.max(var7, Math.pow(var3, 2.0));
      class_3966 var12 = class_1675.method_18075(
         mc.field_1724, var6, var10, var11, var1x -> !var1x.method_7325() && var1x.method_5863() && var1x == var4, var13
      );
      if (var12 != null) {
         boolean var15 = var6.method_1025(var12.method_17784()) <= Math.pow(var3, 2.0);
         boolean var16 = var5 == null;
         boolean var17 = var6.method_1025(var12.method_17784()) < var7;
         if (var6.method_1025(var12.method_17784()) <= Math.pow(var2, 2.0)) {
            double var18 = var4.method_23318();
            double var20 = var4.method_17682();
            double var22 = var18 + var20 * 0.3;
            if (var12.method_17784().field_1351 >= var22) {
               return var12.method_17782() == var4;
            }
         }
      }

      return false;
   }

   public static class_239 rayTrace(double var0, float var2, float var3) {
      class_243 var4 = mc.field_1724.method_5836(1.0F);
      class_243 var5 = getRotationVector(var3, var2);
      class_243 var6 = var4.method_1031(var5.field_1352 * var0, var5.field_1351 * var0, var5.field_1350 * var0);
      return mc.field_1687.method_17742(new class_3959(var4, var6, class_3960.field_17559, class_242.field_1348, mc.field_1724));
   }

   @NotNull
   public static class_243 getRotationVector(float var0, float var1) {
      return new class_243(
         class_3532.method_15374(-var1 * (float) (Math.PI / 180.0)) * class_3532.method_15362(var0 * (float) (Math.PI / 180.0)),
         -class_3532.method_15374(var0 * (float) (Math.PI / 180.0)),
         class_3532.method_15362(-var1 * (float) (Math.PI / 180.0)) * class_3532.method_15362(var0 * (float) (Math.PI / 180.0))
      );
   }

   private MathAngle() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
