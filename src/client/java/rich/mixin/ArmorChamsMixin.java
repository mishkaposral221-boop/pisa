package rich.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.function.Function;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import rich.modules.impl.render.Chams;
import rich.util.render.clientpipeline.ClientPipelines;

// In 1.21.11 the armor RenderLayer factories on RenderLayers are private
// Function<Identifier, RenderLayer> fields, invoked via .apply(id) inside
// EquipmentRenderer.render(...). There are no public armorCutoutNoCull(Identifier)
// methods to hook anymore, so instead we wrap the generic Function.apply(Object)
// call site. We run the original first, then -- only when this entity is a Chams
// target (flagged by LivingEntityRendererMixin around render()) AND the call was
// the armor-layer factory (Identifier in -> RenderLayer out) -- we swap the
// returned layer for the no-depth CHAMS_ENTITY layer so armor draws through walls
// with its vanilla texture. The type guards keep every other Function.apply in
// render() (trim sprites, model lookups, etc.) untouched. require=0 keeps the
// build green even if the call site shifts in a future mapping update.
@Mixin(EquipmentRenderer.class)
public class ArmorChamsMixin {
   @WrapOperation(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;"
      ),
      require = 0
   )
   private Object richArmorLayerApply(Function instance, Object arg, Operation<Object> original) {
      Object result = original.call(instance, arg);
      Chams chams = Chams.getInstance();
      if (chams != null
            && chams.isState()
            && Chams.RICH$EQUIPMENT_TARGET
            && arg instanceof Identifier
            && result instanceof RenderLayer) {
         return ClientPipelines.CHAMS_ENTITY.apply((Identifier) arg);
      }
      return result;
   }
}
