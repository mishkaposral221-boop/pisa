package rich.modules.impl.combat.aura.rotations;

import java.security.SecureRandom;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import rich.Initialization;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.impl.combat.aura.attack.StrikeManager;
import rich.modules.impl.combat.aura.impl.RotateConstructor;

public class SnapAngle extends RotateConstructor {
   private final SecureRandom random = new SecureRandom();
   private static long lastSnapTime = 0L;
   private static Angle snappedAngle = null;
   private static boolean holdingSnap = false;

   public SnapAngle() {
      super("Snap");
   }

   @Override
   public Angle limitAngleChange(Angle var1, Angle var2, class_243 var3, class_1297 var4) {
      StrikeManager var5 = Initialization.getInstance().getManager().getAttackPerpetrator();
      Angle var6 = MathAngle.calculateDelta(var1, var2);
      float var7 = var6.getYaw();
      float var8 = var6.getPitch();
      float var9 = (float)Math.hypot(Math.abs(var7), Math.abs(var8));
      boolean var10 = var4 != null && var5.canAttack(null, 0);
      float var11 = 1.0F;
      float var12 = 1.0F;
      float var13 = var10 ? var11 : var12;
      float var14 = Math.abs(var7 / var9) * 180.0F;
      float var15 = Math.abs(var8 / var9) * 180.0F;
      float var16 = class_3532.method_15363(var7, -var14, var14);
      float var17 = class_3532.method_15363(var8, -var15, var15);
      Angle var18 = new Angle(var1.getYaw(), var1.getPitch());
      var18.setYaw(class_3532.method_16439(var13, var1.getYaw(), var1.getYaw() + var16));
      var18.setPitch(class_3532.method_16439(var13, var1.getPitch(), var1.getPitch() + var17));
      return var18;
   }

   private float randomLerp(float var1, float var2) {
      return class_3532.method_16439(this.random.nextFloat(), var1, var2);
   }

   @Override
   public class_243 randomValue() {
      return class_243.field_1353;
   }
}
