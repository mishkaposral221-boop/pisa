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
 * Intercepts armor-related render layer methods in {@link RenderLayers} and
 * substitutes the unified CHAMS pipeline when rendering a chams target.
 *
 * Methods targeted:
 *   - armorCutoutNoCull(Identifier)  — primary armor layer (all pieces incl. leggings)
 *   - getEntityCutoutNoCull(Identifier, boolean) — optional fallback, require=0 so it
 *     silently skips if the method doesn't exist in this MC version.
 *
 * NOTE: dyeableArmorCutoutNoCull was REMOVED — it does NOT exist in MC 1.21.11
 * RenderLayers and caused a hard crash at bootstrap.
 *
 * Why CHAMS and not CHAMS_ENTITY/CHAMS_ARMOR:
 *   The old pipelines used ENTITY_SNIPPET which applies lightmap multiplication.
 *   For HUMANOID_LEGGINGS, UV2=(0,0) at draw time -> lightmap sample = black ->
 *   leggings appear black.  The new CHAMS pipeline uses custom flat shaders that
 *   skip the lightmap, rendering at full brightness for all geometry.
 */
@Mixin(RenderLayers.class)
public class ArmorChamsMixin {

    // ── armorCutoutNoCull ─────────────────────────────────────────────────────
    // Primary path for ALL armor pieces in MC 1.21.11

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

    // ── getEntityCutoutNoCull(Identifier, boolean) ────────────────────────────
    // Optional fallback — some MC 1.21.x builds route leggings through this.
    // require=0: silently skip if method doesn't exist in this MC version.

    @Inject(
        method = "getEntityCutoutNoCull(Lnet/minecraft/util/Identifier;Z)Lnet/minecraft/client/render/RenderLayer;",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
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
