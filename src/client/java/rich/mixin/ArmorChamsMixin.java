package rich.mixin;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.modules.impl.render.Chams;
import rich.util.render.clientpipeline.ClientPipelines;

/**
 * Renders armor through walls by intercepting RenderLayers.armorCutoutNoCull().
 *
 * WHY WE INJECT INTO RenderLayers INSTEAD OF EquipmentRenderer:
 *
 * EquipmentRenderer has multiple overloaded render() methods in MC 1.21, and
 * getting the exact method descriptor for each one is fragile across MC versions.
 * Using method = "render" without a descriptor fails if there are multiple overloads
 * (Mixin throws "Ambiguous method").
 *
 * By injecting into RenderLayers.armorCutoutNoCull() instead, we intercept at the
 * source. This works for ALL armor layers (helmet, chestplate, leggings, boots)
 * regardless of which EquipmentRenderer overload called it.
 *
 * RICH$EQUIPMENT_TARGET is set true at the HEAD of LivingEntityRenderer.render()
 * for enemy players, so we only apply chams for the right targets.
 */
@Mixin(RenderLayers.class)
public class ArmorChamsMixin {

    @Inject(
        method = "armorCutoutNoCull",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void rich$chamsArmorLayer(
            Identifier texture,
            CallbackInfoReturnable<RenderLayer> cir) {

        Chams chams = Chams.getInstance();
        if (chams != null
                && chams.isState()
                && Chams.RICH$EQUIPMENT_TARGET
                && chams.showArmor.isValue()
                && texture != null) {
            cir.setReturnValue(ClientPipelines.CHAMS_ENTITY.apply(texture));
        }
    }
}
