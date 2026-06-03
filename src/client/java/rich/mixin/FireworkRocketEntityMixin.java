package rich.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import rich.IMinecraft;
import rich.events.api.EventManager;
import rich.events.impl.FireworkEvent;
import rich.modules.impl.combat.aura.AngleConnection;

@Mixin(FireworkRocketEntity.class)
public class FireworkRocketEntityMixin implements IMinecraft {
   @Shadow
   @Nullable
   private LivingEntity shooter;

   @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/LivingEntity;getRotationVector()Lnet/minecraft/Vec3d;"))
   public Vec3d getRotationVectorHook(LivingEntity var1, Operation<Vec3d> var2) {
      return this.shooter == mc.player && this.shooter.isGliding()
         ? AngleConnection.INSTANCE.getMoveRotation().toVector()
         : (Vec3d)var2.call(new Object[]{var1});
   }

   @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/LivingEntity;setVelocity(Lnet/minecraft/Vec3d;)V"))
   public void setVelocityHook(LivingEntity var1, Vec3d var2, Operation<Void> var3) {
      if (this.shooter == mc.player && this.shooter.isGliding()) {
         FireworkEvent var4 = new FireworkEvent(var2);
         EventManager.callEvent(var4);
         var3.call(new Object[]{var1, var4.getVector()});
      } else {
         var3.call(new Object[]{var1, var2});
      }
   }
}
