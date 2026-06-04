package rich.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import rich.modules.impl.render.Chams;
import rich.util.render.clientpipeline.ClientPipelines;

// 1.21.11 reality (verified against yarn 1.21.11+build.3):
//   EquipmentRenderer has NO method/Function that returns a RenderLayer -- its
//   only Functions are layerTextures (-> Identifier) and trimSprites (-> Sprite).
//   The armor RenderLayer is built inline and passed as the 4th argument (index 3)
//   to RenderCommandQueue.submitModel(...) inside the long render worker
//   (method_64078, the overload that takes the @Nullable Identifier textureId).
//   So we @ModifyArg that RenderLayer argument: when this entity is a Chams target
//   (flagged by LivingEntityRendererMixin around render()) and we know the armor
//   texture, we swap in the no-depth CHAMS_ENTITY layer so armor draws through
//   walls with its vanilla texture -- mirroring exactly how body chams works via
//   the getRenderLayer redirect.
//   We target the submitModel call by name+descriptor only (no owner) so it matches
//   whether the call site is typed RenderCommandQueue or OrderedRenderCommandQueue,
//   and we cover both submitModel overloads (with Sprite / without). require=0 keeps
//   the build green if one overload isn't used.
@Mixin(EquipmentRenderer.class)
public class ArmorChamsMixin {
   @Unique
   private static final String RENDER_WORKER =
      "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/util/Identifier;II)V";

   @ModifyArg(
      method = RENDER_WORKER,
      at = @At(
         value = "INVOKE",
         target = "submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/RenderLayer;IIILnet/minecraft/client/texture/Sprite;ILnet/minecraft/client/render/command/ModelCommandRenderer$CrumblingOverlayCommand;)V"
      ),
      index = 3,
      require = 0
   )
   private RenderLayer richArmorLayerWithSprite(RenderLayer original, @Local(argsOnly = true) @Nullable Identifier textureId) {
      return this.richArmorLayer(original, textureId);
   }

   @ModifyArg(
      method = RENDER_WORKER,
      at = @At(
         value = "INVOKE",
         target = "submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/RenderLayer;IIILnet/minecraft/client/render/command/ModelCommandRenderer$CrumblingOverlayCommand;)V"
      ),
      index = 3,
      require = 0
   )
   private RenderLayer richArmorLayerNoSprite(RenderLayer original, @Local(argsOnly = true) @Nullable Identifier textureId) {
      return this.richArmorLayer(original, textureId);
   }

   @Unique
   private RenderLayer richArmorLayer(RenderLayer original, @Nullable Identifier textureId) {
      Chams chams = Chams.getInstance();
      if (chams != null && chams.isState() && Chams.RICH$EQUIPMENT_TARGET && textureId != null) {
         return ClientPipelines.CHAMS_ENTITY.apply(textureId);
      }
      return original;
   }
}
