package rich.modules.impl.combat.aura.rotations;

import java.security.SecureRandom;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
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
   public Angle limitAngleChange(Angle var1, Angle var2, Vec3d var3, Entity var4) {
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
      float var16 = MathHelper.clamp(var7, -var14, var14);
      float var17 = MathHelper.clamp(var8, -var15, var15);
      Angle var18 = new Angle(var1.getYaw(), var1.getPitch());
      var18.setYaw(MathHelper.lerp(var13, var1.getYaw(), var1.getYaw() + var16));
      var18.setPitch(MathHelper.lerp(var13, var1.getPitch(), var1.getPitch() + var17));
      return var18;
   }

   private float randomLerp(float var1, float var2) {
      return MathHelper.lerp(this.random.nextFloat(), var1, var2);
   }

   @Override
   public Vec3d randomValue() {
      return Vec3d.ZERO;
   }
}
