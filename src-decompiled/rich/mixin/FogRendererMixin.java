package rich.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.class_11398;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import rich.modules.impl.render.CustomFog;

@Mixin(class_11398.class)
public class FogRendererMixin {
   @ModifyReturnValue(method = "method_71654", at = @At("RETURN"))
   private int onGetFogColor(int var1) {
      CustomFog var2 = CustomFog.getInstance();
      return var2 != null && var2.isState() ? var2.color.getColorNoAlpha() | 0xFF000000 : var1;
   }
}
