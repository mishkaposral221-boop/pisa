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
import rich.util.timer.StopWatch;

public class SPAngle extends RotateConstructor {
   private final SecureRandom random = new SecureRandom();
   private float currentJitterYaw = 0.0F;
   private float currentJitterPitch = 0.0F;
   private float targetJitterYaw = 0.0F;
   private float targetJitterPitch = 0.0F;
   private float circlePhase = 0.0F;
   private float circleRadius = 0.0F;
   private float targetCircleRadius = 0.0F;
   private float currentSpeed = 0.0F;

   public SPAngle() {
      super("SpookyTime");
   }

   @Override
   public Angle limitAngleChange(Angle var1, Angle var2, Vec3d var3, Entity var4) {
      StrikeManager var5 = Initialization.getInstance().getManager().getAttackPerpetrator();
      StopWatch var6 = var5.getAttackTimer();
      int var7 = var5.getCount();
      boolean var8 = var4 != null && var5.canAttack(null, 0);
      if (var4 != null && var8) {
         Vec3d var9 = Vector.hitbox(var4, 1.0F, var4.isOnGround() ? 1.0F : 1.256F, 1.0F, 2.0F);
         var2 = MathAngle.calculateAngle(var9);
      }

      Angle var31 = MathAngle.calculateDelta(var1, var2);
      float var10 = var31.getYaw();
      float var11 = var31.getPitch();
      float var12 = (float)Math.hypot(Math.abs(var10), Math.abs(var11));
      if (var12 < 0.01F) {
         var12 = 1.0F;
      }

      boolean var13 = false;
      if (var4 != null && !var8) {
         var13 = RaycastAngle.rayTrace(AngleConnection.INSTANCE.getRotation().toVector(), 4.0, var4.getBoundingBox());
      }

      float var14 = 0.75F;
      this.circlePhase = this.circlePhase + var14 * this.randomLerp(7.5F, 12.5F);
      if (this.circlePhase > Math.PI * 2) {
         this.circlePhase = (float)(this.circlePhase - (Math.PI * 2));
      }

      if (var8) {
         this.targetCircleRadius = this.randomLerp(0.5F, 4.5F);
      } else if (var13) {
         this.targetCircleRadius = this.randomLerp(12.0F, 12.0F);
      } else {
         this.targetCircleRadius = this.randomLerp(8.0F, 12.0F);
      }

      this.circleRadius = this.circleRadius + (this.targetCircleRadius - this.circleRadius) * 0.18F;
      float var15 = (float)(Math.cos(this.circlePhase) * this.circleRadius);
      float var16 = (float)(Math.sin(this.circlePhase * 11.3F) * this.circleRadius * 0.4F);
      float var17 = (float)var6.elapsedTime() / 100.0F + var7 % 5;
      int var18 = var7 % 4;

      Angle var19 = switch (var18) {
         case 0 -> new Angle((float)Math.cos(var17), (float)Math.sin(var17));
         case 1 -> new Angle((float)Math.sin(var17 * 2.2F), (float)Math.cos(var17 * 0.6F));
         case 2 -> new Angle((float)Math.sin(var17), (float)(-Math.cos(var17)));
         default -> new Angle((float)(-Math.cos(var17 * 0.5F)), (float)Math.sin(var17 * 2.1F));
      };
      float var20 = var8 ? 0.5F : (var13 ? 0.6F : 1.0F);
      this.targetJitterYaw = this.randomLerp(35.0F, 32.0F) * var19.getYaw() * var20;
      this.targetJitterPitch = this.randomLerp(5.0F, 2.0F) * var19.getPitch() * var20;
      float var21 = 0.15F;
      this.currentJitterYaw = this.currentJitterYaw + (this.targetJitterYaw - this.currentJitterYaw) * var21;
      this.currentJitterPitch = this.currentJitterPitch + (this.targetJitterPitch - this.currentJitterPitch) * var21;
      float var22;
      if (var8) {
         var22 = this.randomLerp(1.0F, 1.0F);
      } else if (var13) {
         var22 = this.randomLerp(0.35F, 0.15F);
      } else if (var4 != null) {
         float var23 = MathHelper.clamp(var12 / 30.0F, 0.1F, 1.0F);
         var22 = this.randomLerp(0.45F, 0.25F) * var23;
      } else {
         var22 = !var6.finished(600.0) ? 0.53F : this.randomLerp(0.2F, 0.35F);
      }

      this.currentSpeed = this.currentSpeed + (var22 - this.currentSpeed) * 0.65F;
      float var32 = Math.abs(var10 / var12) * 180.0F;
      float var24 = Math.abs(var11 / var12) * 90.0F;
      float var25 = MathHelper.clamp(var10, -var32, var32);
      float var26 = MathHelper.clamp(var11, -var24, var24);
      float var27 = this.currentJitterYaw + var15;
      float var28 = this.currentJitterPitch + var16;
      if (var4 == null && var6.finished(800.0)) {
         var27 *= 0.3F;
         var28 *= 0.3F;
      }

      float var29 = MathHelper.lerp(this.currentSpeed, var1.getYaw(), var1.getYaw() + var25) + var27;
      float var30 = MathHelper.lerp(this.currentSpeed, var1.getPitch(), var1.getPitch() + var26) + var28;
      return new Angle(var29, MathHelper.clamp(var30, -90.0F, 90.0F));
   }

   private float randomLerp(float var1, float var2) {
      return MathHelper.lerp(this.random.nextFloat(), var1, var2);
   }

   @Override
   public Vec3d randomValue() {
      return Vec3d.ZERO;
   }
}
