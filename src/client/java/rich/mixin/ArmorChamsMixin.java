package rich.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import rich.modules.impl.render.Chams;
import rich.util.render.clientpipeline.ClientPipelines;

// Armor through walls.
//
// Body chams swaps the entity RenderLayer to the no-depth CHAMS_ENTITY layer inside
// LivingEntityRenderer.render. Armor is submitted within that same render() call by
// EquipmentRenderer, so RICH$EQUIPMENT_TARGET is set and we can swap the armor layer too.
//
// Why the capture is back:
//   The trailing Identifier argument of render() is the dye/overlay decal - it is NULL for normal
//   armor. The previous commit used ONLY that argument, so normal armor got CHAMS_ENTITY.apply(null)
//   and rendered NOTHING through walls. We must instead use the BASE texture that layerTextures.apply()
//   resolves for each layer.
//
//   The earlier capture attempt grabbed the FIRST apply() Identifier and reused it for every layer,
//   which put the wrong texture on later pieces (the "model bug"). The fix is to OVERWRITE the
//   captured texture on EVERY apply(): submitModel is always called right after its layer's
//   layerTextures.apply(), so each piece gets its OWN base texture.
@Mixin(EquipmentRenderer.class)
public class ArmorChamsMixin {
   private static final String RICH$RENDER = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/util/Identifier;II)V";

   // Base armor texture for the layer about to be submitted. Overwritten on every apply() (no
   // first-only guard) so each submitModel uses its own texture.
   @Unique
   private Identifier rich$lastEquipTexture;

   @ModifyExpressionValue(
      method = RICH$RENDER,
      at = @At(value = "INVOKE", target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;"),
      require = 0
   )
   private Object rich$captureBaseTexture(Object result) {
      if (result instanceof Identifier) {
         this.rich$lastEquipTexture = (Identifier)result;
      }
      return result;
   }

   @ModifyArg(
      method = RICH$RENDER,
      at = @At(value = "INVOKE", target = "submitModel")
   )
   private RenderLayer rich$armorChamsLayer(RenderLayer original, @Local(argsOnly = true) Identifier textureArg) {
      Chams chams = Chams.getInstance();
      if (chams == null || !chams.isState() || !Chams.RICH$EQUIPMENT_TARGET) {
         return original;
      }
      Identifier tex = this.rich$lastEquipTexture != null ? this.rich$lastEquipTexture : textureArg;
      if (tex == null) {
         return original;
      }
      // Leg armor (leggings / layer 2) samples as solid black under the chams pipeline; keep its
      // original layer so it renders correctly instead of black.
      String path = tex.getPath();
      if (path != null && (path.contains("leggings") || path.contains("layer_2"))) {
         return original;
      }
      return ClientPipelines.CHAMS_ENTITY.apply(tex);
   }
}
