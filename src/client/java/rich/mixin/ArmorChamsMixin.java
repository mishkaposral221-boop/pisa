package rich.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import rich.modules.impl.render.Chams;
import rich.util.render.clientpipeline.ClientPipelines;

// EXPERIMENTAL / PARKED: route the armor render layer to the no-depth CHAMS_ENTITY
// layer so equipped armor renders through walls while Chams is active.
//
// NOTE: RenderLayer.getArmorCutoutNoCull(Identifier) was REMOVED in this MC version,
// so we use @WrapOperation (which calls the original via Operation) instead of naming
// the method in Java code -> this compiles cleanly.
//
// require = 0 -> if the INVOKE target below is not present in ArmorFeatureRenderer#render
// at apply time, the hook is skipped silently and the client still launches (no crash).
// The exact target string will be corrected once the decompiled render() body is known.
@Mixin(ArmorFeatureRenderer.class)
public class ArmorChamsMixin {
   @WrapOperation(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/RenderLayer;getArmorCutoutNoCull(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"
      ),
      require = 0
   )
   private RenderLayer richArmorChamsLayer(Identifier id, Operation<RenderLayer> original) {
      Chams chams = Chams.getInstance();
      if (chams != null && chams.isState() && id != null) {
         return ClientPipelines.CHAMS_ENTITY.apply(id);
      }
      return original.call(id);
   }
}
