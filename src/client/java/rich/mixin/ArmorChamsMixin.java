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
 * IMPORTANT: RenderLayers.armorCutoutNoCull() is called during MC bootstrap
 * (TexturedRenderLayers.<clinit>) BEFORE our mod's Manager is initialized.
 * We MUST check that the manager is ready before calling Chams.getInstance(),
 * otherwise we get NullPointerException -> ExceptionInInitializerError -> crash.
 *
 * Guard: Initialization.getInstance().getManager() != null
 * This is cheap (no allocation) and safe to call at any time.
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
            cir.setReturnValue(ClientPipelines.CHAMS_ENTITY.apply(texture));
        }
    }
}
