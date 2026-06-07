package rich.mixin;

import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.Initialization;
import rich.modules.impl.render.FullBright;
import rich.modules.impl.render.NoRender;

@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {
   @Redirect(method = "update", at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F", ordinal = 1))
   private float leet$getValue(Double var1) {
      return Initialization.getInstance().getManager().getModuleProvider().get(FullBright.class).isState() ? 200.0F : var1.floatValue();
   }

   @Inject(method = "getDarkness", at = @At("HEAD"), cancellable = true)
   private void removeDarknessEffect(CallbackInfoReturnable<Float> var1) {
      NoRender var2 = NoRender.getInstance();
      if (var2 != null && var2.isState() && var2.modeSetting.isSelected("Darkness")) {
         var1.setReturnValue(0.0F);
      }
   }
}
