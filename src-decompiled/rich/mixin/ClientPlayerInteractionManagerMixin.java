package rich.mixin;

import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_636;
import net.minecraft.class_1269.class_9860;
import net.minecraft.class_1269.class_9861;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.events.api.EventManager;
import rich.events.impl.BlockBreakingEvent;
import rich.events.impl.ClickSlotEvent;
import rich.events.impl.UsingItemEvent;

@Mixin(class_636.class)
public class ClientPlayerInteractionManagerMixin {
   @Inject(method = "method_2919", at = @At("RETURN"))
   public void interactItemHook(class_1657 var1, class_1268 var2, CallbackInfoReturnable<class_1269> var3) {
      if (var3.getReturnValue() instanceof class_9860 var4 && !var4.comp_2909().equals(class_9861.field_52427)) {
         UsingItemEvent var6 = new UsingItemEvent((byte)0);
         EventManager.callEvent(var6);
      }
   }

   @Inject(method = "method_2897", at = @At("HEAD"), cancellable = true)
   public void stopUsingItemHook(CallbackInfo var1) {
      UsingItemEvent var2 = new UsingItemEvent((byte)2);
      EventManager.callEvent(var2);
   }

   @Inject(method = "method_2919", at = @At("HEAD"), cancellable = true)
   private void gameModeHook(class_1657 var1, class_1268 var2, CallbackInfoReturnable<class_1269> var3) {
      UsingItemEvent var4 = new UsingItemEvent((byte)-1);
      EventManager.callEvent(var4);
      if (var4.isCancelled()) {
         var3.setReturnValue(class_1269.field_5811);
      }
   }

   @Inject(method = "method_2902", at = @At("HEAD"))
   private void injectBlockBreaking(class_2338 var1, class_2350 var2, CallbackInfoReturnable<Boolean> var3) {
      EventManager.callEvent(new BlockBreakingEvent(var1, var2));
   }

   @Inject(method = "method_2906", at = @At("HEAD"), cancellable = true)
   public void clickSlotHook(int var1, int var2, int var3, class_1713 var4, class_1657 var5, CallbackInfo var6) {
      ClickSlotEvent var7 = new ClickSlotEvent(var1, var2, var3, var4);
      EventManager.callEvent(var7);
      if (var7.isCancelled()) {
         var6.cancel();
      }
   }
}
