package rich.modules.impl.combat.aura;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.math.Box;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.jetbrains.annotations.NotNull;
import rich.IMinecraft;

public final class MathAngle implements IMinecraft {
   public static Angle fromVec2f(Vec2f var0) {
      return new Angle(var0.y, var0.x);
   }

   public static float computeAngleDifference(float var0, float var1) {
      return MathHelper.wrapDegrees(var0 - var1);
   }

   public static Angle fromVec3d(Vec3d var0) {
      return new Angle(
         (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(var0.z, var0.x)) - 90.0),
         (float)MathHelper.wrapDegrees(Math.toDegrees(-Math.atan2(var0.y, Math.hypot(var0.x, var0.z))))
      );
   }

   public static Angle calculateDelta(Angle var0, Angle var1) {
      float var2 = MathHelper.wrapDegrees(var1.getYaw() - var0.getYaw());
      float var3 = MathHelper.wrapDegrees(var1.getPitch() - var0.getPitch());
      return new Angle(var2, var3);
   }

   public static Angle calculateAngle(Vec3d var0) {
      return fromVec3d(var0.subtract(mc.player.getEyePos()));
   }

   public static Angle pitch(float var0) {
      return new Angle(mc.player.getYaw(), var0);
   }

   public static Angle cameraAngle() {
      return new Angle(mc.player.getYaw(), mc.player.getPitch());
   }

   public static boolean rayTrace(float var0, float var1, float var2, float var3, Entity var4) {
      HitResult var5 = rayTrace(var2, var0, var1);
      Vec3d var6 = mc.player.getEntityPos().add(0.0, mc.player.getEyeHeight(mc.player.getPose()), 0.0);
      double var7 = Math.pow(var2, 2.0);
      if (var5 != null) {
         var7 = var6.squaredDistanceTo(var5.getPos());
      }

      Vec3d var9 = getRotationVector(var1, var0).multiply(var2);
      Vec3d var10 = var6.add(var9);
      Box var11 = mc.player.getBoundingBox().stretch(var9).expand(1.0, 1.0, 1.0);
      double var13 = Math.max(var7, Math.pow(var3, 2.0));
      EntityHitResult var12 = ProjectileUtil.raycast(
         mc.player, var6, var10, var11, var1x -> !var1x.isSpectator() && var1x.canHit() && var1x == var4, var13
      );
      if (var12 != null) {
         boolean var15 = var6.squaredDistanceTo(var12.getPos()) <= Math.pow(var3, 2.0);
         boolean var16 = var5 == null;
         boolean var17 = var6.squaredDistanceTo(var12.getPos()) < var7;
         if (var6.squaredDistanceTo(var12.getPos()) <= Math.pow(var2, 2.0)) {
            double var18 = var4.getY();
            double var20 = var4.getHeight();
            double var22 = var18 + var20 * 0.3;
            if (var12.getPos().y >= var22) {
               return var12.getEntity() == var4;
            }
         }
      }

      return false;
   }

   public static HitResult rayTrace(double var0, float var2, float var3) {
      Vec3d var4 = mc.player.getCameraPosVec(1.0F);
      Vec3d var5 = getRotationVector(var3, var2);
      Vec3d var6 = var4.add(var5.x * var0, var5.y * var0, var5.z * var0);
      return mc.world.raycast(new RaycastContext(var4, var6, net.minecraft.world.RaycastContext.ShapeType.OUTLINE, net.minecraft.world.RaycastContext.FluidHandling.NONE, mc.player));
   }

   @NotNull
   public static Vec3d getRotationVector(float var0, float var1) {
      return new Vec3d(
         MathHelper.sin(-var1 * (float) (Math.PI / 180.0)) * MathHelper.cos(var0 * (float) (Math.PI / 180.0)),
         -MathHelper.sin(var0 * (float) (Math.PI / 180.0)),
         MathHelper.cos(-var1 * (float) (Math.PI / 180.0)) * MathHelper.cos(var0 * (float) (Math.PI / 180.0))
      );
   }

   private MathAngle() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
