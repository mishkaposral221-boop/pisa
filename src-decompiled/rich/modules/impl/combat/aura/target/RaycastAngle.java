package rich.modules.impl.combat.aura.target;

import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.class_1297;
import net.minecraft.class_1675;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import rich.IMinecraft;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.impl.combat.aura.attack.StrikerConstructor;

public final class RaycastAngle implements IMinecraft {
   public static class_3965 raycast(double var0, Angle var2, boolean var3) {
      return raycast(Objects.requireNonNull(mc.field_1724).method_5836(1.0F), var0, var2, var3);
   }

   public static class_3965 raycast(class_243 var0, double var1, Angle var3, boolean var4) {
      class_1297 var5 = mc.method_1560();
      if (var5 == null) {
         return null;
      }

      class_243 var6 = var3.toVector();
      class_243 var7 = var0.method_1031(var6.field_1352 * var1, var6.field_1351 * var1, var6.field_1350 * var1);
      class_638 var8 = mc.field_1687;
      if (var8 == null) {
         return null;
      }

      class_242 var9 = var4 ? class_242.field_1347 : class_242.field_1348;
      class_3959 var10 = new class_3959(var0, var7, class_3960.field_17559, var9, var5);
      return var8.method_17742(var10);
   }

   public static class_3965 raycast(class_243 var0, class_243 var1, class_3960 var2) {
      return raycast(var0, var1, var2, mc.field_1724);
   }

   public static class_3965 raycast(class_243 var0, class_243 var1, class_3960 var2, class_1297 var3) {
      return raycast(var0, var1, var2, class_242.field_1348, var3);
   }

   public static class_3965 raycast(class_243 var0, class_243 var1, class_3960 var2, class_242 var3, class_1297 var4) {
      return mc.field_1687.method_17742(new class_3959(var0, var1, var2, var3, var4));
   }

   public static class_3966 raytraceEntity(double var0, Angle var2, Predicate<class_1297> var3) {
      class_746 var4 = mc.field_1724;
      if (var4 == null) {
         return null;
      }

      class_243 var5 = var4.method_5836(1.0F);
      class_243 var6 = var2.toVector();
      class_243 var7 = var5.method_1031(var6.field_1352 * var0, var6.field_1351 * var0, var6.field_1350 * var0);
      class_238 var8 = var4.method_5829().method_18804(var6.method_1021(var0)).method_1009(1.0, 1.0, 1.0);
      return class_1675.method_18075(var4, var5, var7, var8, var1 -> !var1.method_7325() && var3.test(var1), var0 * var0);
   }

   public static boolean rayTrace(StrikerConstructor.AttackPerpetratorConfigurable var0) {
      boolean var1 = mc.field_1724.method_6128() && var0.getTarget().method_6128();
      return var1 ? true : rayTrace(AngleConnection.INSTANCE.getRotation().toVector(), var0.getMaximumRange(), var0.getBox());
   }

   public static boolean rayTrace(double var0, class_238 var2) {
      return rayTrace(AngleConnection.INSTANCE.getRotation().toVector(), var0, var2);
   }

   public static boolean rayTrace(class_243 var0, double var1, class_238 var3) {
      class_243 var4 = Objects.requireNonNull(mc.field_1724).method_33571();
      class_238 var5 = var3.method_1014(0.15);
      return var5.method_1006(var4) || var5.method_992(var4, var4.method_1019(var0.method_1021(var1))).isPresent();
   }

   private RaycastAngle() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
