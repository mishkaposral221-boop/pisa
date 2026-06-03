package rich.modules.impl.combat.aura.rotations;

import java.security.SecureRandom;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import rich.Initialization;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.impl.combat.aura.attack.StrikeManager;
import rich.modules.impl.combat.aura.impl.RotateConstructor;
import rich.modules.impl.combat.aura.target.RaycastAngle;
import rich.modules.impl.combat.aura.target.Vector;
import rich.util.move.MoveUtil;
import rich.util.timer.StopWatch;

public class MatrixAngle extends RotateConstructor {
   public MatrixAngle() {
      super("Matrix");
   }

   @Override
   public Angle limitAngleChange(Angle var1, Angle var2, Vec3d var3, Entity var4) {
      StrikeManager var5 = Initialization.getInstance().getManager().getAttackPerpetrator();
      StopWatch var6 = var5.getAttackTimer();
      boolean var7 = var4 != null && var5.canAttack(null, 0);
      if (var4 != null && var7) {
         Vec3d var8 = Vector.hitbox(var4, 1.0F, var4.isOnGround() ? 0.9F : 1.4F, 1.0F, 2.0F);
         var2 = MathAngle.calculateAngle(var8);
      }

      Angle var25 = MathAngle.calculateDelta(var1, var2);
      float var9 = var25.getYaw();
      float var10 = var25.getPitch();
      float var11 = (float)Math.hypot(Math.abs(var9), Math.abs(var10));
      boolean var12 = false;
      if (var4 != null && !var7 && RaycastAngle.rayTrace(AngleConnection.INSTANCE.getRotation().toVector(), 4.0, var4.getBoundingBox())) {
         var12 = true;
      }

      float var13 = 1.0F;
      float var14 = var12 ? 0.06F : this.randomLerp(0.0F, 0.5F);
      float var15 = var7 ? var13 : var14;
      float var16 = Math.abs(var9 / var11) * 360.0F;
      float var17 = Math.abs(var10 / var11) * 180.0F;
      float var18 = var7 ? 0.0F : (MoveUtil.hasPlayerMovement() ? (float)(6.0 * Math.sin(System.currentTimeMillis() / 65.0)) : 0.0F);
      float var19 = var7 ? 0.0F : (MoveUtil.hasPlayerMovement() ? (float)(2.0 * Math.cos(System.currentTimeMillis() / 65.0)) : 0.0F);
      float var20 = var7 ? 0.0F : 13.0F;
      float var21 = var7 ? 0.0F : 8.0F;
      if (!var7 || var4 == null) {
         float var22 = MathHelper.clamp(1.0F - var11 / 180.0F, 0.1F, 1.0F);
         var15 = !var6.finished(550.0) ? 0.05F : 0.8F * var22;
         var18 = 0.0F;
         var21 = 0.0F;
         var20 = 0.0F;
         var19 = 0.0F;
      }

      float var26 = MathHelper.clamp(var9, -var16, var16) + var20;
      float var23 = MathHelper.clamp(var10, -var17, var17) + var21;
      Angle var24 = new Angle(var1.getYaw(), var1.getPitch());
      var24.setYaw(MathHelper.lerp(this.randomLerp(var15, var15), var1.getYaw(), var1.getYaw() + var26) + var18);
      var24.setPitch(MathHelper.lerp(this.randomLerp(var15, var15), var1.getPitch(), var1.getPitch() + var23) + var19);
      return var24;
   }

   private float randomLerp(float var1, float var2) {
      return MathHelper.lerp(new SecureRandom().nextFloat(), var1, var2);
   }

   @Override
   public Vec3d randomValue() {
      return new Vec3d(0.0, 0.0, 0.0);
   }
}
