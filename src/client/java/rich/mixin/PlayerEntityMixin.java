package rich.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.IMinecraft;
import rich.events.api.EventManager;
import rich.events.impl.PlayerTravelEvent;
import rich.events.impl.PushEvent;
import rich.events.impl.SwimmingEvent;
import rich.modules.impl.combat.aura.AngleConnection;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements IMinecraft {
   @Inject(method = "isPushedByFluids", at = @At("HEAD"), cancellable = true)
   public void isPushedByFluids(CallbackInfoReturnable<Boolean> var1) {
      PushEvent var2 = new PushEvent(PushEvent.Type.WATER);
      EventManager.callEvent(var2);
      if (var2.isCancelled()) {
         var1.setReturnValue(false);
      }
   }

   @ModifyExpressionValue(method = "knockbackTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/PlayerEntity;getYaw()F"))
   private float hookKnockbackRotation(float var1) {
      return (Object)this == mc.player && AngleConnection.INSTANCE.getMoveRotation() != null ? AngleConnection.INSTANCE.getMoveRotation().getYaw() : var1;
   }

   @ModifyExpressionValue(method = "doSweepingAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/PlayerEntity;getYaw()F"))
   private float hookSweepRotation(float var1) {
      return (Object)this == mc.player && AngleConnection.INSTANCE.getMoveRotation() != null ? AngleConnection.INSTANCE.getMoveRotation().getYaw() : var1;
   }

   @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
   private void onTravelPre(Vec3d var1, CallbackInfo var2) {
      if (mc.player != null) {
         PlayerTravelEvent var3 = new PlayerTravelEvent(var1, true);
         EventManager.callEvent(var3);
         if (var3.isCancelled()) {
            var2.cancel();
         }
      }
   }

   @ModifyExpressionValue(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/PlayerEntity;getRotationVector()Lnet/minecraft/Vec3d;"))
   public Vec3d travelHook(Vec3d var1) {
      SwimmingEvent var2 = new SwimmingEvent(var1);
      EventManager.callEvent(var2);
      return var2.getVector();
   }

   @Inject(method = "travel", at = @At("RETURN"))
   private void onTravelPost(Vec3d var1, CallbackInfo var2) {
      if (mc.player != null) {
         PlayerTravelEvent var3 = new PlayerTravelEvent(var1, false);
         EventManager.callEvent(var3);
      }
   }
}
