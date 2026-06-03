package rich.mixin;

import net.minecraft.class_1291;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_5636;
import net.minecraft.class_6880;
import net.minecraft.class_7286;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.modules.impl.render.NoRender;

@Mixin(class_7286.class)
public abstract class StatusEffectFogModifierMixin {
   @Shadow
   public abstract class_6880<class_1291> method_42590();

   @Inject(method = "method_42593", at = @At("HEAD"), cancellable = true)
   private void onShouldApply(@Nullable class_5636 var1, class_1297 var2, CallbackInfoReturnable<Boolean> var3) {
      NoRender var4 = NoRender.getInstance();
      if (var4.isState()) {
         class_6880 var5 = this.method_42590();
         if (var4.modeSetting.isSelected("Bad Effects") && var5 == class_1294.field_5919) {
            var3.setReturnValue(false);
         }

         if (var4.modeSetting.isSelected("Darkness") && var5 == class_1294.field_38092) {
            var3.setReturnValue(false);
         }
      }
   }
}
