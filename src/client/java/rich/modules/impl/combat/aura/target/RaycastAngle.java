package rich.modules.impl.combat.aura.target;

import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import rich.IMinecraft;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.impl.combat.aura.attack.StrikerConstructor;

public final class RaycastAngle implements IMinecraft {
   public static BlockHitResult raycast(double var0, Angle var2, boolean var3) {
      return raycast(Objects.requireNonNull(mc.player).getCameraPosVec(1.0F), var0, var2, var3);
   }

   public static BlockHitResult raycast(Vec3d var0, double var1, Angle var3, boolean var4) {
      Entity var5 = mc.getCameraEntity();
      if (var5 == null) {
         return null;
      }

      Vec3d var6 = var3.toVector();
      Vec3d var7 = var0.add(var6.x * var1, var6.y * var1, var6.z * var1);
      ClientWorld var8 = mc.world;
      if (var8 == null) {
         return null;
      }

      net.minecraft.world.RaycastContext.FluidHandling var9 = var4 ? net.minecraft.world.RaycastContext.FluidHandling.ANY : net.minecraft.world.RaycastContext.FluidHandling.NONE;
      RaycastContext var10 = new RaycastContext(var0, var7, net.minecraft.world.RaycastContext.ShapeType.OUTLINE, var9, var5);
      return var8.raycast(var10);
   }

   public static BlockHitResult raycast(Vec3d var0, Vec3d var1, net.minecraft.world.RaycastContext.ShapeType var2) {
      return raycast(var0, var1, var2, mc.player);
   }

   public static BlockHitResult raycast(Vec3d var0, Vec3d var1, net.minecraft.world.RaycastContext.ShapeType var2, Entity var3) {
      return raycast(var0, var1, var2, net.minecraft.world.RaycastContext.FluidHandling.NONE, var3);
   }

   public static BlockHitResult raycast(Vec3d var0, Vec3d var1, net.minecraft.world.RaycastContext.ShapeType var2, net.minecraft.world.RaycastContext.FluidHandling var3, Entity var4) {
      return mc.world.raycast(new RaycastContext(var0, var1, var2, var3, var4));
   }

   public static EntityHitResult raytraceEntity(double var0, Angle var2, Predicate<Entity> var3) {
      ClientPlayerEntity var4 = mc.player;
      if (var4 == null) {
         return null;
      }

      Vec3d var5 = var4.getCameraPosVec(1.0F);
      Vec3d var6 = var2.toVector();
      Vec3d var7 = var5.add(var6.x * var0, var6.y * var0, var6.z * var0);
      Box var8 = var4.getBoundingBox().stretch(var6.multiply(var0)).expand(1.0, 1.0, 1.0);
      return ProjectileUtil.raycast(var4, var5, var7, var8, var1 -> !var1.isSpectator() && var3.test(var1), var0 * var0);
   }

   public static boolean rayTrace(StrikerConstructor.AttackPerpetratorConfigurable var0) {
      boolean var1 = mc.player.isGliding() && var0.getTarget().isGliding();
      return var1 ? true : rayTrace(AngleConnection.INSTANCE.getRotation().toVector(), var0.getMaximumRange(), var0.getBox());
   }

   public static boolean rayTrace(double var0, Box var2) {
      return rayTrace(AngleConnection.INSTANCE.getRotation().toVector(), var0, var2);
   }

   public static boolean rayTrace(Vec3d var0, double var1, Box var3) {
      Vec3d var4 = Objects.requireNonNull(mc.player).getEyePos();
      Box var5 = var3.expand(0.15);
      return var5.contains(var4) || var5.raycast(var4, var4.add(var0.multiply(var1))).isPresent();
   }

   private RaycastAngle() {
      throw new UnsupportedOperationException("This is keyCodec utility class and cannot be instantiated");
   }
}
