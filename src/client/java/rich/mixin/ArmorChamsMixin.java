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
// and each slot resolves its base texture through layerTextures.apply(...). We capture that
// resolved Identifier and feed it to CHAMS_ENTITY for the matching submitModel call.
//
// IMPORTANT: rich$lastEquipTexture is an instance field that lives ACROSS render() calls. The
// leggings slot (layer_2 / inner leg model) was rendering black because it could inherit the
// PREVIOUS slot's texture if its own apply() result was not captured in time. We now clear the
// captured texture at the HEAD of every render() call, so a slot can only ever use its OWN base
// texture - and if it somehow resolves nothing, it falls back to the normal layer (visible) instead
// of being drawn black/invisible.
@Mixin(EquipmentRenderer.class)
public class ArmorChamsMixin {
   private static final String RICH$RENDER = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/util/Identifier;II)V";

   // Base armor texture for the layer about to be submitted. Reset on every render() call and
   // overwritten on every apply() so each submitModel uses its own texture.
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
      // Every armor piece (helmet / chestplate / leggings / boots) gets its own per-layer base
      // texture, so leggings render correctly through walls instead of black or invisible.
      return ClientPipelines.CHAMS_ENTITY.apply(tex);
   }
}
