package rich.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import rich.modules.impl.render.Chams;
import rich.util.render.clientpipeline.ClientPipelines;

// In 1.21.11 the armor RenderLayer factories moved from RenderLayer onto RenderLayers,
// and armor/equipment is drawn by EquipmentRenderer (not the old ArmorFeatureRenderer call).
// We wrap whichever RenderLayers armor-layer factory the worker calls and, when the current
// entity is a Chams target (flagged by LivingEntityRendererMixin around render()), swap the
// returned layer for the no-depth CHAMS_ENTITY layer so armor draws through walls with its
// vanilla texture. require=0 on each so only the factory that actually exists is hooked;
// the others are skipped silently instead of failing the build.
@Mixin(EquipmentRenderer.class)
public class ArmorChamsMixin {
   @WrapOperation(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/RenderLayers;armorCutoutNoCull(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"
      ),
      require = 0
   )
   private RenderLayer richArmorCutoutNoCull(Identifier id, Operation<RenderLayer> original) {
      return this.richArmorLayer(id, original);
   }

   @WrapOperation(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/RenderLayers;getArmorCutoutNoCull(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"
      ),
      require = 0
   )
   private RenderLayer richGetArmorCutoutNoCull(Identifier id, Operation<RenderLayer> original) {
      return this.richArmorLayer(id, original);
   }

   @WrapOperation(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/RenderLayers;armorTranslucent(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"
      ),
      require = 0
   )
   private RenderLayer richArmorTranslucent(Identifier id, Operation<RenderLayer> original) {
      return this.richArmorLayer(id, original);
   }

   @Unique
   private RenderLayer richArmorLayer(Identifier id, Operation<RenderLayer> original) {
      Chams chams = Chams.getInstance();
      if (chams != null && chams.isState() && Chams.RICH$EQUIPMENT_TARGET && id != null) {
         return ClientPipelines.CHAMS_ENTITY.apply(id);
      }
      return original.call(id);
   }
}
