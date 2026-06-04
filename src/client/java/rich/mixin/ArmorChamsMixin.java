package rich.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.model.Model;
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
//   EquipmentRenderer has NO Function returning a RenderLayer (only layerTextures
//   -> Identifier and trimSprites -> Sprite). The armor RenderLayer is obtained via
//   Model.getLayer(Identifier) -- "Returns the render layer for the corresponding
//   texture" -- inside the long render worker (method_64078, the overload taking
//   @Nullable Identifier textureId), then passed to RenderCommandQueue.submitModel.
//
//   PRIMARY hook: @WrapOperation on Model.getLayer(...) -> swap the returned layer
//   for the no-depth CHAMS_ENTITY layer when this entity is a Chams target (flagged
//   by LivingEntityRendererMixin around render()). This mirrors exactly how body
//   chams works via the getRenderLayer redirect, and is independent of which submit
//   overload is used.
//   FALLBACK hooks: @ModifyArg on both submitModel overloads (with / without Sprite),
//   index 3 = the RenderLayer arg, in case the layer is built without going through
//   Model.getLayer. All require=0 so the build stays green regardless.
@Mixin(EquipmentRenderer.class)
public class ArmorChamsMixin {
   @Unique
   private static final String RENDER_WORKER =
      "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/util/Identifier;II)V";

   @WrapOperation(
      method = RENDER_WORKER,
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/model/Model;getLayer(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"
      ),
      require = 0
   )
   private RenderLayer richArmorGetLayer(Model model, Identifier texture, Operation<RenderLayer> original) {
      RenderLayer layer = original.call(model, texture);
      if (this.richShouldChams() && texture != null) {
         return ClientPipelines.CHAMS_ENTITY.apply(texture);
      }
      return layer;
   }

   @ModifyArg(
      method = RENDER_WORKER,
      at = @At(
         value = "INVOKE",
         target = "submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/RenderLayer;IIILnet/minecraft/client/texture/Sprite;ILnet/minecraft/client/render/command/ModelCommandRenderer$CrumblingOverlayCommand;)V"
      ),
      index = 3,
      require = 0
   )
   private RenderLayer richArmorSubmitWithSprite(RenderLayer original, @Local(argsOnly = true) @Nullable Identifier textureId) {
      if (this.richShouldChams() && textureId != null) {
         return ClientPipelines.CHAMS_ENTITY.apply(textureId);
      }
      return original;
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
   private RenderLayer richArmorSubmitNoSprite(RenderLayer original, @Local(argsOnly = true) @Nullable Identifier textureId) {
      if (this.richShouldChams() && textureId != null) {
         return ClientPipelines.CHAMS_ENTITY.apply(textureId);
      }
      return original;
   }

   @Unique
   private boolean richShouldChams() {
      Chams chams = Chams.getInstance();
      return chams != null && chams.isState() && Chams.RICH$EQUIPMENT_TARGET;
   }
}
