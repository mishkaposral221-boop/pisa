package rich.mixin;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import rich.modules.impl.render.Chams;
import rich.util.render.clientpipeline.ClientPipelines;

// EXPERIMENTAL: route the armor render layer to the no-depth CHAMS_ENTITY layer
// so equipped armor renders through walls while Chams is active.
// require = 0 -> if the INVOKE target is not found at apply time, the redirect is
// skipped silently and the client still launches (no crash).
@Mixin(ArmorFeatureRenderer.class)
public class ArmorChamsMixin {
   @Redirect(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/RenderLayer;getArmorCutoutNoCull(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"
      ),
      require = 0
   )
   private RenderLayer richArmorChamsLayer(Identifier id) {
      Chams chams = Chams.getInstance();
      if (chams != null && chams.isState() && id != null) {
         return ClientPipelines.CHAMS_ENTITY.apply(id);
      }
      return RenderLayer.getArmorCutoutNoCull(id);
   }
}
