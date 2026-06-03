package rich.mixin;

import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.modules.impl.render.chinahat.ChinaHatFeatureRenderer;

@Mixin(PlayerEntityRenderer.class)
public class MixinPlayerEntityRenderer {
   @Inject(method = "<init>", at = @At("TAIL"))
   private void onInit(Context var1, boolean var2, CallbackInfo var3) {
      PlayerEntityRenderer var4 = (PlayerEntityRenderer)this;
      var4.addFeature(new ChinaHatFeatureRenderer(var4));
   }
}
