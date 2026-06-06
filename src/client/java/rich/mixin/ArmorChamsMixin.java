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
 * Intercepts ALL armor-related render layer methods in {@link RenderLayers} and
 * substitutes the chams pipeline when we are rendering a chams target.
 *
 * <h3>Why we intercept multiple methods</h3>
 * <ul>
 *   <li>{@code armorCutoutNoCull}  – used by {@code EquipmentRenderer} for all
 *       humanoid armor pieces (helmet, chestplate, leggings, boots) in most
 *       MC versions.</li>
 *   <li>{@code dyeableArmorCutoutNoCull} – used for leather armor (which can
 *       be dyed) so it gets the same through-wall treatment.</li>
 *   <li>{@code getEntityCutoutNoCull(Identifier, boolean)} – fallback; some
 *       MC 1.21.x EquipmentRenderer variants route inner-layer (leggings)
 *       through this method instead of {@code armorCutoutNoCull}.</li>
 * </ul>
 *
 * <h3>Why CHAMS instead of old CHAMS_ARMOR / CHAMS_ENTITY</h3>
 * The old {@code CHAMS_ARMOR} pipeline used {@code ENTITY_SNIPPET} which runs
 * the vanilla entity shader. That shader multiplies texture color by the lightmap
 * sample: {@code color *= vertexColor * texelFetch(Sampler2, UV2, 0)}.
 * For HUMANOID_LEGGINGS (inner layer / layer_2), UV2 is (0,0) at draw time,
 * so the lightmap sample is (0,0,0,1) = black → leggings appear black.
 * The new {@code CHAMS} pipeline uses custom shaders that skip the lightmap
 * entirely, rendering at full brightness regardless of UV2.
 *
 * <h3>Bootstrap guard</h3>
 * {@code RenderLayers.armorCutoutNoCull()} is called during
 * {@code TexturedRenderLayers.<clinit>} (MC bootstrap) before our Manager is
 * initialised.  We check {@code getManager() != null} before touching any
 * mod state to avoid NPEs.
 */
@Mixin(RenderLayers.class)
public class ArmorChamsMixin {

    // ── armorCutoutNoCull ─────────────────────────────────────────────────────

    @Inject(
        method = "armorCutoutNoCull",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void rich$chamsArmor(
            Identifier texture,
            CallbackInfoReturnable<RenderLayer> cir) {
        if (rich$shouldChams()) {
            cir.setReturnValue(ClientPipelines.CHAMS.apply(texture));
        }
    }

    // ── dyeableArmorCutoutNoCull (leather armor) ──────────────────────────────

    @Inject(
        method = "dyeableArmorCutoutNoCull",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void rich$chamsDyeable(
            Identifier texture,
            CallbackInfoReturnable<RenderLayer> cir) {
        if (rich$shouldChams()) {
            cir.setReturnValue(ClientPipelines.CHAMS.apply(texture));
        }
    }

    // ── getEntityCutoutNoCull(Identifier, boolean) ────────────────────────────
    // Fallback: some MC 1.21.x builds route leggings through this method.

    @Inject(
        method = "getEntityCutoutNoCull(Lnet/minecraft/util/Identifier;Z)Lnet/minecraft/client/render/RenderLayer;",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void rich$chamsEntityCutout(
            Identifier texture,
            boolean outline,
            CallbackInfoReturnable<RenderLayer> cir) {
        if (rich$shouldChams()) {
            cir.setReturnValue(ClientPipelines.CHAMS.apply(texture));
        }
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private static boolean rich$shouldChams() {
        // Bootstrap guard: Manager is null during TexturedRenderLayers.<clinit>
        if (Initialization.getInstance().getManager() == null) return false;
        Chams chams = Chams.getInstance();
        return chams != null
            && chams.isState()
            && Chams.RICH$EQUIPMENT_TARGET
            && chams.showArmor.isValue();
    }
}
