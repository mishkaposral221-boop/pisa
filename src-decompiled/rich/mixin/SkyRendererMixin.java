package rich.mixin;

import net.minecraft.class_12076;
import net.minecraft.class_4184;
import net.minecraft.class_638;
import net.minecraft.class_9975;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.modules.impl.render.CustomSky;

@Mixin(class_9975.class)
public class SkyRendererMixin {
   @Inject(method = "method_74926", at = @At("RETURN"))
   private void onUpdateRenderState(class_638 var1, float var2, class_4184 var3, class_12076 var4, CallbackInfo var5) {
      CustomSky var6 = CustomSky.getInstance();
      if (var6 != null && var6.isState()) {
         int var7 = var6.color.getColorNoAlpha();
         var4.field_63097 = var7 & 16777215;
      }
   }
}
