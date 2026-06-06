package rich.mixin;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.Initialization;
import rich.modules.impl.render.Chams;
import rich.util.render.clientpipeline.ClientPipelines;

/**
 * Renders armor through walls by intercepting RenderLayers.armorCutoutNoCull().
 *
 * WHY CHAMS_ARMOR (cull=false) INSTEAD OF CHAMS_ENTITY (cull=true):
 *
 * The original CHAMS_ENTITY_PIPELINE has withCull(true), which is correct for
 * the player body model (all faces point outward). However, the leggings inner
 * layer (HUMANOID_LEGGINGS / layer_2) has geometry with back-facing polygons.
 * With backface culling ON, those polygons are discarded -> leggings render black.
 *
 * CHAMS_ARMOR_PIPELINE is identical to CHAMS_ENTITY but with withCull(false),
 * so ALL armor faces render correctly including leggings.
 *
 * IMPORTANT: RenderLayers.armorCutoutNoCull() is called during MC bootstrap
 * (TexturedRenderLayers.<clinit>) BEFORE our mod's Manager is initialized.
 * Guard: check getManager() != null before calling Chams.getInstance().
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

        // Guard: manager is null during bootstrap - skip entirely
        if (Initialization.getInstance().getManager() == null) return;
        if (texture == null) return;

        Chams chams = Chams.getInstance();
        if (chams != null
                && chams.isState()
                && Chams.RICH$EQUIPMENT_TARGET
                && chams.showArmor.isValue()) {
            // CHAMS_ARMOR uses cull=false so leggings (back-facing geometry) render correctly
            cir.setReturnValue(ClientPipelines.CHAMS_ARMOR.apply(texture));
        }
    }
}
