package rich.mixin;

import net.minecraft.client.model.Model;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.modules.impl.render.Chams;
import rich.util.render.clientpipeline.ClientPipelines;

// NEW APPROACH (previous EquipmentRenderer hooks never fired - the worker method
// descriptor was a guess and the injections silently no-op'd with require=0).
//
// Every model (player body AND every armor/equipment piece) resolves its render
// layer through Model.getLayer(Identifier) -- "Returns the render layer for the
// corresponding texture". So we inject straight into Model.getLayer at HEAD and,
// while the current entity is flagged as a Chams target (set by
// LivingEntityRendererMixin around the target's render()), return the no-depth
// CHAMS_ENTITY layer for the same texture. This is independent of EquipmentRenderer
// internals / submit overloads and is exactly the layer-swap that already makes the
// body show through walls -- now applied to the armor too.
//
// The Chams.RICH$EQUIPMENT_TARGET gate keeps this scoped to the target player's
// synchronous render pass, so it does not recolor unrelated world models.
@Mixin(Model.class)
public class ArmorChamsMixin {
   @Inject(
      method = "getLayer(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
      at = @At("HEAD"),
      cancellable = true
   )
   private void richArmorChams(Identifier texture, CallbackInfoReturnable<RenderLayer> cir) {
      if (texture == null) {
         return;
      }
      Chams chams = Chams.getInstance();
      if (chams != null && chams.isState() && Chams.RICH$EQUIPMENT_TARGET) {
         cir.setReturnValue(ClientPipelines.CHAMS_ENTITY.apply(texture));
      }
   }
}
