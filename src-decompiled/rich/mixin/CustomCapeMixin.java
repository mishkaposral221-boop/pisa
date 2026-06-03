package rich.mixin;

import net.minecraft.class_310;
import net.minecraft.class_742;
import net.minecraft.class_8685;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_742.class)
public class CustomCapeMixin {
   @Inject(method = "method_52814", at = @At("RETURN"), cancellable = true)
   private void replaceCape(CallbackInfoReturnable<class_8685> var1) {
      class_742 var2 = (class_742)this;
      class_310 var3 = class_310.method_1551();
      if (var3.field_1724 != null && var2.method_5667().equals(var3.field_1724.method_5667())) {
         class_8685 var4 = (class_8685)var1.getReturnValue();
         var1.setReturnValue(new class_8685(var4.comp_1626(), null, null, var4.comp_1629(), var4.comp_1630()));
      }
   }
}
