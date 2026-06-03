package rich.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.Camera;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos.Mutable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.events.api.EventManager;
import rich.events.impl.CameraEvent;
import rich.events.impl.CameraPositionEvent;
import rich.modules.impl.combat.aura.Angle;

@Mixin(Camera.class)
public abstract class CameraMixin {
   @Shadow
   private Vec3d pos;
   @Shadow
   @Final
   private net.minecraft.util.math.BlockPos.Mutable blockPos;
   @Shadow
   private float yaw;
   @Shadow
   private float pitch;

   @Shadow
   public abstract void setRotation(float var1, float var2);

   @Shadow
   protected abstract void moveBy(float var1, float var2, float var3);

   @Shadow
   protected abstract float clipToSpace(float var1);

   @Inject(
      method = "update",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/Camera;setPos(DDD)V", shift = Shift.AFTER),
      cancellable = true
   )
   private void updateHook(World var1, Entity var2, boolean var3, boolean var4, float var5, CallbackInfo var6) {
      CameraEvent var7 = new CameraEvent(false, 4.0F, new Angle(this.yaw, this.pitch));
      EventManager.callEvent(var7);
      Angle var8 = var7.getAngle();
      if (var7.isCancelled() && var2 instanceof ClientPlayerEntity var9 && !var9.isSleeping() && var3) {
         float var11 = var4 ? -var8.getPitch() : var8.getPitch();
         float var12 = var8.getYaw() - (var4 ? 180 : 0);
         float var13 = var7.getDistance();
         this.setRotation(var12, var11);
         this.moveBy(var7.isCameraClip() ? -var13 : -this.clipToSpace(var13), 0.0F, 0.0F);
         var6.cancel();
      } else if (var7.isCancelled() && var2 instanceof ClientPlayerEntity var10 && !var10.isSleeping() && !var3) {
         this.setRotation(var8.getYaw(), var8.getPitch());
         var6.cancel();
      }
   }

   @Inject(method = "setPos(Lnet/minecraft/Vec3d;)V", at = @At("HEAD"), cancellable = true)
   private void posHook(Vec3d var1, CallbackInfo var2) {
      CameraPositionEvent var3 = new CameraPositionEvent(var1);
      EventManager.callEvent(var3);
      this.pos = var1 = var3.getPos();
      this.blockPos.set(var1.x, var1.y, var1.z);
      var2.cancel();
   }
}
