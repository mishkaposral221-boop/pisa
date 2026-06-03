package rich.mixin;

import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_636;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.events.api.EventManager;
import rich.events.impl.AttackEvent;
import rich.events.impl.ClickSlotEvent;
import rich.events.impl.InteractEntityEvent;

@Mixin(class_636.class)
public class MixinClientPlayerInteractionManager {
   @Inject(method = "method_2918", at = @At("HEAD"), cancellable = true)
   public void attackEntityHook(class_1657 var1, class_1297 var2, CallbackInfo var3) {
      InteractEntityEvent var4 = new InteractEntityEvent(var2);
      EventManager.callEvent(var4);
      if (var4.isCancelled()) {
         var3.cancel();
      }
   }

   @Inject(method = "method_2918", at = @At("HEAD"))
   private void onAttackEntity(class_1657 var1, class_1297 var2, CallbackInfo var3) {
      boolean var4 = var1.field_6017 > 0.0
         && !var1.method_24828()
         && !var1.method_6101()
         && !var1.method_5799()
         && !var1.method_6059(class_1294.field_5919)
         && var1.method_5854() == null;
      AttackEvent var5 = new AttackEvent(var2, var4);
      EventManager.callEvent(var5);
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
