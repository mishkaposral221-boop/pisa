package rich.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import rich.modules.impl.render.Chams;
import rich.util.render.clientpipeline.ClientPipelines;

// AUTHORITATIVE APPROACH (yarn 1.21.11 javadoc verified).
//
// Armor does NOT resolve its layer through Model.getLayer (that is why the three
// previous attempts silently did nothing). Instead EquipmentRenderer.render
// (the worker overload that also takes the @Nullable Identifier textureId) builds
// the RenderLayer itself and passes it into queue.submitModel(... RenderLayer ...).
//
// So we intercept the RenderLayer ARGUMENT of that submitModel call. This is
// independent of how the layer was created. While the current player is flagged as
// a Chams target (set by LivingEntityRendererMixin around the target's render(),
// which is synchronous and also covers the armor feature), we swap the layer for
// the no-depth CHAMS_ENTITY layer built from the same equipment texture -- exactly
// the swap that already makes the body show through walls.
//
// target = "submitModel" is intentionally owner/descriptor-less so it matches the
// call regardless of which submitModel overload / receiver type the worker uses.
@Mixin(EquipmentRenderer.class)
public class ArmorChamsMixin {
   @ModifyArg(
      method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/util/Identifier;II)V",
      at = @At(value = "INVOKE", target = "submitModel")
   )
   private RenderLayer richArmorChamsLayer(RenderLayer original, @Local(argsOnly = true) Identifier textureId) {
      if (textureId == null) {
         return original;
      }
      Chams chams = Chams.getInstance();
      if (chams != null && chams.isState() && Chams.RICH$EQUIPMENT_TARGET) {
         return ClientPipelines.CHAMS_ENTITY.apply(textureId);
      }
      return original;
   }
}
