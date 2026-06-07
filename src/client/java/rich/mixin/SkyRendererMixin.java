package rich.mixin;

import net.minecraft.client.render.state.SkyRenderState;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.render.SkyRendering;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.modules.impl.render.CustomSky;

@Mixin(SkyRendering.class)
public class SkyRendererMixin {
   @Inject(method = "updateRenderState", at = @At("RETURN"))
   private void onUpdateRenderState(ClientWorld var1, float var2, Camera var3, SkyRenderState var4, CallbackInfo var5) {
      CustomSky var6 = CustomSky.getInstance();
      if (var6 != null && var6.isState()) {
         int var7 = var6.color.getColorNoAlpha();
         var4.skyColor = var7 & 16777215;
      }
   }
}
