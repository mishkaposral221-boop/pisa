package rich.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.modules.impl.render.Chams;
import rich.util.render.clientpipeline.ClientPipelines;

// Armor through walls.
//
// Body chams swaps the entity RenderLayer to the no-depth CHAMS_ENTITY layer inside
// LivingEntityRenderer.render. Armor is submitted within that same render() call by
// EquipmentRenderer, so RICH$EQUIPMENT_TARGET is set and we can swap the armor layer too.
//
// EquipmentRenderer.render is called ONCE PER ARMOR SLOT (helmet / chestplate / leggings / boots),
// and each slot resolves textures through layerTextures.apply(...). A single slot may resolve
// MULTIPLE textures within one render() call: the base armor texture first, then optional
// overlay/trim textures. We only want the BASE texture for CHAMS_ENTITY.
//
// IMPORTANT: leggings (layer_2 / inner leg model) were rendering BLACK because a later overlay/trim
// apply() result overwrote the captured Identifier, and CHAMS_ENTITY then sampled that empty/wrong
// texture. The fix: keep the FIRST apply() result per render() call (the base texture) and ignore the
// rest. rich$lastEquipTexture is reset at the HEAD of every render() call, so each slot can only ever
// use its OWN base texture - and if nothing is captured it falls back to the normal layer (visible)
// instead of being drawn black.
@Mixin(EquipmentRenderer.class)
public class ArmorChamsMixin {
   private static final String RICH$RENDER = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/util/Identifier;II)V";

   // Base armor texture for the layer about to be submitted. Reset on every render() call and
   // set ONLY from the first apply() so each submitModel uses its slot's base texture.
   @Unique
   private Identifier rich$lastEquipTexture;

   @Inject(method = RICH$RENDER, at = @At("HEAD"))
   private void rich$resetCapture(CallbackInfo ci) {
      // New slot: forget the previous slot's texture so leggings can't be drawn with it.
      this.rich$lastEquipTexture = null;
   }

   @ModifyExpressionValue(
      method = RICH$RENDER,
      at = @At(value = "INVOKE", target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;"),
      require = 0
   )
   private Object rich$captureBaseTexture(Object result) {
      // FIRST apply() per render() call wins: that is the slot's BASE texture
      // (helmet / chestplate / leggings / boots). Later apply() results in the same call are
      // overlay/trim lookups (e.g. the leggings layer_2 overlay) whose texture, when fed to
      // CHAMS_ENTITY, sampled empty/black. Ignoring them keeps leggings on their real base texture.
      if (result instanceof Identifier && this.rich$lastEquipTexture == null) {
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
         // Nothing reliable to sample: keep the normal layer (visible with depth) rather than
         // forcing a wrong texture that would render black.
         return original;
      }
      // Every armor piece (helmet / chestplate / leggings / boots) gets its own per-layer base
      // texture, so leggings render correctly through walls instead of black or invisible.
      return ClientPipelines.CHAMS_ENTITY.apply(tex);
   }
}
