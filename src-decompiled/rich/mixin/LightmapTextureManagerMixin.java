package rich.mixin;

import net.minecraft.class_765;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.Initialization;
import rich.modules.impl.render.FullBright;
import rich.modules.impl.render.NoRender;

@Mixin(class_765.class)
public class LightmapTextureManagerMixin {
   @Redirect(method = "method_3313", at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F", ordinal = 1))
   private float leet$getValue(Double var1) {
      return Initialization.getInstance().getManager().getModuleProvider().get(FullBright.class).isState() ? 200.0F : var1.floatValue();
   }

   @Inject(method = "method_42596", at = @At("HEAD"), cancellable = true)
   private void removeDarknessEffect(CallbackInfoReturnable<Float> var1) {
      NoRender var2 = NoRender.getInstance();
      if (var2 != null && var2.isState() && var2.modeSetting.isSelected("Darkness")) {
         var1.setReturnValue(0.0F);
      }
   }
}
