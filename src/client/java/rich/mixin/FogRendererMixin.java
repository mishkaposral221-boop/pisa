package rich.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.render.fog.AtmosphericFogModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import rich.modules.impl.render.CustomFog;

@Mixin(AtmosphericFogModifier.class)
public class FogRendererMixin {
   @ModifyReturnValue(method = "getFogColor", at = @At("RETURN"))
   private int onGetFogColor(int var1) {
      CustomFog var2 = CustomFog.getInstance();
      return var2 != null && var2.isState() ? var2.color.getColorNoAlpha() | 0xFF000000 : var1;
   }
}
