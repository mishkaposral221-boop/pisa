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
// The body chams works by swapping the entity RenderLayer to the no-depth CHAMS_ENTITY
// layer inside LivingEntityRenderer.render (synchronous). Armor is submitted within that
// same render() call by EquipmentRenderer, so the RICH$EQUIPMENT_TARGET flag is set.
//
// EquipmentRenderer builds the armor RenderLayer itself and passes it into
// queue.submitModel(... RenderLayer ...). We swap THAT RenderLayer argument for the
// no-depth CHAMS_ENTITY layer built from the armor texture.
//
// The catch the previous attempts kept hitting: for the base armor layer the worker's
// @Nullable Identifier textureId ARGUMENT is null - the real texture is resolved inside
// the method via this.layerTextures.apply(key). So we obtain the texture from BOTH:
//   * the textureId arg (when the build does pass it), captured via @Local, and
//   * the result of layerTextures.apply(...) (the null-arg case). That Function returns
//     an Identifier, while the sibling trimSprites Function returns a Sprite, so an
//     instanceof check picks out the texture unambiguously without guessing ordinals.
@Mixin(EquipmentRenderer.class)
public class ArmorChamsMixin {
   @Unique
   private Identifier rich$lastEquipTexture;

   // Capture the internally-resolved armor texture. require = 0: if this build passes the
   // texture as the arg instead of resolving it here, there may be no such call and that
   // is fine - the @ModifyArg below still gets it from the arg.
   @ModifyExpressionValue(
      method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/util/Identifier;II)V",
      at = @At(value = "INVOKE", target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;"),
      require = 0
   )
   private Object rich$captureEquipTexture(Object result) {
      if (result instanceof Identifier id) {
         this.rich$lastEquipTexture = id;
      }
      return result;
   }

   @ModifyArg(
      method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/util/Identifier;II)V",
      at = @At(value = "INVOKE", target = "submitModel")
   )
   private RenderLayer rich$armorChamsLayer(RenderLayer original, @Local(argsOnly = true) Identifier textureArg) {
      Chams chams = Chams.getInstance();
      if (chams == null || !chams.isState() || !Chams.RICH$EQUIPMENT_TARGET) {
         return original;
      }
      Identifier tex = textureArg != null ? textureArg : this.rich$lastEquipTexture;
      return tex != null ? ClientPipelines.CHAMS_ENTITY.apply(tex) : original;
   }
}
