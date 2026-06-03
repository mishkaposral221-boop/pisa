package rich.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.class_1657;
import net.minecraft.class_243;
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

@Mixin(class_1657.class)
public abstract class PlayerEntityMixin implements IMinecraft {
   @Inject(method = "method_5675", at = @At("HEAD"), cancellable = true)
   public void isPushedByFluids(CallbackInfoReturnable<Boolean> var1) {
      PushEvent var2 = new PushEvent(PushEvent.Type.WATER);
      EventManager.callEvent(var2);
      if (var2.isCancelled()) {
         var1.setReturnValue(false);
      }
   }

   @ModifyExpressionValue(method = "method_75122", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1657;method_36454()F"))
   private float hookKnockbackRotation(float var1) {
      return this == mc.field_1724 && AngleConnection.INSTANCE.getMoveRotation() != null ? AngleConnection.INSTANCE.getMoveRotation().getYaw() : var1;
   }

   @ModifyExpressionValue(method = "method_7263", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1657;method_36454()F"))
   private float hookSweepRotation(float var1) {
      return this == mc.field_1724 && AngleConnection.INSTANCE.getMoveRotation() != null ? AngleConnection.INSTANCE.getMoveRotation().getYaw() : var1;
   }

   @Inject(method = "method_6091", at = @At("HEAD"), cancellable = true)
   private void onTravelPre(class_243 var1, CallbackInfo var2) {
      if (mc.field_1724 != null) {
         PlayerTravelEvent var3 = new PlayerTravelEvent(var1, true);
         EventManager.callEvent(var3);
         if (var3.isCancelled()) {
            var2.cancel();
         }
      }
   }

   @ModifyExpressionValue(method = "method_6091", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1657;method_5720()Lnet/minecraft/class_243;"))
   public class_243 travelHook(class_243 var1) {
      SwimmingEvent var2 = new SwimmingEvent(var1);
      EventManager.callEvent(var2);
      return var2.getVector();
   }

   @Inject(method = "method_6091", at = @At("RETURN"))
   private void onTravelPost(class_243 var1, CallbackInfo var2) {
      if (mc.field_1724 != null) {
         PlayerTravelEvent var3 = new PlayerTravelEvent(var1, false);
         EventManager.callEvent(var3);
      }
   }
}
