package rich.modules.impl.combat.aura.rotations;

import java.security.SecureRandom;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import rich.Initialization;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.impl.combat.aura.attack.StrikeManager;
import rich.modules.impl.combat.aura.impl.RotateConstructor;
import rich.util.timer.StopWatch;

public class FTAngle extends RotateConstructor {
   private final SecureRandom random = new SecureRandom();
   private static int lastCount = -1;
   private static int hitsAfterMiss = 0;
   private static long missEndTime = 0L;
   private static int swingsDone = 0;
   private float currentJitterYaw = 0.0F;
   private float currentJitterPitch = 0.0F;
   private float targetJitterYaw = 0.0F;
   private float targetJitterPitch = 0.0F;

   public FTAngle() {
      super("FunTime");
   }

   @Override
   public Angle limitAngleChange(Angle var1, Angle var2, Vec3d var3, Entity var4) {
      StrikeManager var5 = Initialization.getInstance().getManager().getAttackPerpetrator();
      StopWatch var6 = var5.getAttackTimer();
      int var7 = var5.getCount();
      long var8 = System.currentTimeMillis();
      if (var7 != lastCount) {
         hitsAfterMiss++;
         lastCount = var7;
      }

      if (hitsAfterMiss >= 40 && missEndTime == 0L) {
         missEndTime = var8 + 350L;
         hitsAfterMiss = 0;
         swingsDone = 0;
      }

      if (missEndTime != 0L) {
         if (var8 < missEndTime) {
            long var28 = var8 - (missEndTime - 350L);
            if (swingsDone == 0 && var28 >= 50L) {
               mc.player.swingHand(Hand.MAIN_HAND);
               swingsDone = 1;
            } else if (swingsDone == 1 && var28 >= 180L) {
               mc.player.swingHand(Hand.MAIN_HAND);
               swingsDone = 2;
            }

            return new Angle(var1.getYaw() + this.random.nextFloat() * 6.0F - 3.0F, -80.0F);
         }

         missEndTime = 0L;
      }

      Angle var10 = MathAngle.calculateDelta(var1, var2);
      float var11 = var10.getYaw();
      float var12 = var10.getPitch();
      float var13 = (float)Math.hypot(Math.abs(var11), Math.abs(var12));
      if (var13 < 0.01F) {
         var13 = 1.0F;
      }

      int var14 = var7 % 3;
      float var15 = (float)var6.elapsedTime() / 80.0F + var7 % 6;

      Angle var16 = switch (var14) {
         case 0 -> new Angle((float)Math.cos(var15), (float)Math.sin(var15));
         case 1 -> new Angle((float)Math.sin(var15), (float)Math.cos(var15));
         case 2 -> new Angle((float)Math.sin(var15), (float)(-Math.cos(var15)));
         default -> new Angle((float)(-Math.cos(var15)), (float)Math.sin(var15));
      };
      this.targetJitterYaw = this.randomLerp(11.0F, 20.0F) * var16.getYaw();
      this.targetJitterPitch = this.randomLerp(1.0F, 6.0F) * var16.getPitch()
         + this.randomLerp(2.0F, 1.0F) * (float)Math.cos(System.currentTimeMillis() / 8000.0);
      float var17 = 1.0F;
      this.currentJitterYaw = this.currentJitterYaw + (this.targetJitterYaw - this.currentJitterYaw) * var17;
      this.currentJitterPitch = this.currentJitterPitch + (this.targetJitterPitch - this.currentJitterPitch) * var17;
      if (var4 != null) {
         float var29 = var5.canAttack(null, 0) ? 0.9F : (this.random.nextBoolean() ? 0.1F : 0.2F);
         float var30 = Math.abs(var11 / var13) * 180.0F;
         float var31 = Math.abs(var12 / var13) * 180.0F;
         float var32 = MathHelper.clamp(var11, -var30, var30);
         float var33 = MathHelper.clamp(var12, -var31, var31);
         float var34 = this.randomLerp(var29, var29 + 0.6F);
         float var35 = MathHelper.lerp(var34, var1.getYaw(), var1.getYaw() + var32) + this.currentJitterYaw;
         float var36 = MathHelper.lerp(var34, var1.getPitch(), var1.getPitch() + var33) + this.currentJitterPitch;
         return new Angle(var35, MathHelper.clamp(var36, -90.0F, 90.0F));
      } else {
         float var18 = var6.finished(650.0) ? (this.random.nextBoolean() ? 0.85F : 0.2F) : -0.2F;
         float var19 = !var6.finished(2000.0) ? this.currentJitterYaw : 0.0F;
         float var20 = !var6.finished(2000.0) ? this.currentJitterPitch : 0.0F;
         float var21 = Math.abs(var11 / var13) * 180.0F;
         float var22 = Math.abs(var12 / var13) * 180.0F;
         float var23 = MathHelper.clamp(var11, -var21, var21);
         float var24 = MathHelper.clamp(var12, -var22, var22);
         float var25 = Math.clamp(this.randomLerp(var18, var18 + 0.2F), 0.0F, 1.0F);
         float var26 = MathHelper.lerp(var25, var1.getYaw(), var1.getYaw() + var23) + var19;
         float var27 = MathHelper.lerp(var25, var1.getPitch(), var1.getPitch() + var24) + var20;
         return new Angle(var26, MathHelper.clamp(var27, -90.0F, 90.0F));
      }
   }

   private float randomLerp(float var1, float var2) {
      return MathHelper.lerp(this.random.nextFloat(), var1, var2);
   }

   @Override
   public Vec3d randomValue() {
      return Vec3d.ZERO;
   }
}
