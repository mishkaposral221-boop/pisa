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

// Armor through walls.
//
// The body chams works by swapping the entity RenderLayer to the no-depth CHAMS_ENTITY layer
// inside LivingEntityRenderer.render. Armor is submitted within that same render() call by
// EquipmentRenderer, so the RICH$EQUIPMENT_TARGET flag is set and we can swap the armor's
// RenderLayer for the no-depth CHAMS_ENTITY layer built from the armor texture.
//
// History / why this is simple now:
//   - A previous attempt tried to CAPTURE the base texture from layerTextures.apply() and prefer
//     it over the texture argument. That capture grabbed the wrong Identifier on some pieces,
//     which corrupted their models (the "model bug") AND still left the leggings black. So the
//     capture is gone entirely - we use ONLY the texture argument the renderer already resolved.
//   - The leggings (the HUMANOID_LEGGINGS / *_layer_2 layer) render solid black under the
//     translucent no-depth chams pipeline, so for that layer we keep the ORIGINAL armor layer:
//     the leggings then render NORMALLY (correct, just not through walls) instead of black.
@Mixin(EquipmentRenderer.class)
public class ArmorChamsMixin {
   @ModifyArg(
      method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/util/Identifier;II)V",
      at = @At(value = "INVOKE", target = "submitModel")
   )
   private RenderLayer rich$armorChamsLayer(RenderLayer original, @Local(argsOnly = true) Identifier textureArg) {
      Chams chams = Chams.getInstance();
      if (chams == null || !chams.isState() || !Chams.RICH$EQUIPMENT_TARGET) {
         return original;
      }
      if (textureArg == null) {
         return original;
      }
      // Leg armor (leggings / layer 2) samples as solid black under the chams pipeline; keep its
      // original layer so it renders correctly instead of black.
      String path = textureArg.getPath();
      if (path != null && (path.contains("leggings") || path.contains("layer_2"))) {
         return original;
      }
      return ClientPipelines.CHAMS_ENTITY.apply(textureArg);
   }
}
