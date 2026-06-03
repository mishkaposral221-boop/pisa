package rich.modules.impl.combat.aura.impl;

import net.minecraft.class_1297;
import net.minecraft.class_243;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.MathAngle;

public class LinearConstructor extends RotateConstructor {
   public static final LinearConstructor INSTANCE = new LinearConstructor();

   public LinearConstructor() {
      super("Linear");
   }

   @Override
   public Angle limitAngleChange(Angle var1, Angle var2, class_243 var3, class_1297 var4) {
      Angle var5 = MathAngle.calculateDelta(var1, var2);
      float var6 = var5.getYaw();
      float var7 = var5.getPitch();
      float var8 = (float)Math.hypot(var6, var7);
      float var9 = Math.abs(var6 / var8) * 360.0F;
      float var10 = Math.abs(var7 / var8) * 360.0F;
      float var11 = var1.getYaw() + Math.min(Math.max(var6, -var9), var9);
      float var12 = var1.getPitch() + Math.min(Math.max(var7, -var10), var10);
      return new Angle(var11, var12);
   }

   @Override
   public class_243 randomValue() {
      return new class_243(0.0, 0.0, 0.0);
   }
}
