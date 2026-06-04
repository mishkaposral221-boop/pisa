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
// The body chams works by swapping the entity RenderLayer to the no-depth CHAMS_ENTITY
// layer inside LivingEntityRenderer.render (synchronous). Armor is submitted within that
// same render() call by EquipmentRenderer, so the RICH$EQUIPMENT_TARGET flag is set.
//
// EquipmentRenderer builds the armor RenderLayer itself and passes it into
// queue.submitModel(... RenderLayer ...). We swap THAT RenderLayer argument for the
// no-depth CHAMS_ENTITY layer built from the armor texture.
//
// Texture source: the worker render() resolves the BASE armor texture internally via
// this.layerTextures.apply(key) (returns an Identifier; the sibling trimSprites Function
// returns a Sprite, so an instanceof check picks the base texture). The trailing
// @Nullable Identifier ARGUMENT is the decal/overlay texture and is usually null - using
// it for the leggings (armor layer 2) is exactly what rendered them solid black. So we
// PREFER the internally-resolved base texture and only fall back to the arg, and we reset
// the captured texture at the head of every render() call so one piece never leaks its
// texture onto the next.
@Mixin(EquipmentRenderer.class)
public class ArmorChamsMixin {
   @Unique
   private Identifier rich$lastEquipTexture;

   @Unique
   private static final String RICH$RENDER = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/util/Identifier;II)V";

   // Clear the captured texture for each equipment piece so a stale texture from a previous
   // piece can never be applied (which would sample as garbage/black).
   @Inject(method = RICH$RENDER, at = @At("HEAD"))
   private void rich$resetEquipTexture(CallbackInfo ci) {
      this.rich$lastEquipTexture = null;
   }

   // Capture the FIRST resolved armor texture (the base layer). Later apply() calls within the
   // same piece can return overlay/decal textures; using one of those for leggings was what
   // produced the black model. require = 0: some builds pass the texture as the arg instead.
   @ModifyExpressionValue(
      method = RICH$RENDER,
      at = @At(value = "INVOKE", target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;"),
      require = 0
   )
   private Object rich$captureEquipTexture(Object result) {
      if (this.rich$lastEquipTexture == null && result instanceof Identifier id) {
         this.rich$lastEquipTexture = id;
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
      // Prefer the internally-resolved BASE texture; the textureArg is the decal/overlay and is
      // only a fallback. Using the arg for the leggings (layer 2) rendered them black.
      Identifier tex = this.rich$lastEquipTexture != null ? this.rich$lastEquipTexture : textureArg;
      return tex != null ? ClientPipelines.CHAMS_ENTITY.apply(tex) : original;
   }
}
