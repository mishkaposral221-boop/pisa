package rich.mixin;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import rich.modules.impl.render.Chams;
import rich.util.render.clientpipeline.ClientPipelines;

/**
 * Renders other players' armor through walls (chams).
 *
 * WHY THE OLD VERSION HAD BLACK LEGGINGS:
 * The previous @Redirect used a hard-coded method descriptor (RICH$RENDER string)
 * that matched only ONE overload of EquipmentRenderer.render().
 * In MC 1.21, leggings (HUMANOID_LEGGINGS layer type) are rendered via a
 * DIFFERENT overload whose descriptor didn't match, so the redirect never fired
 * for leggings. Leggings then used the default pipeline but the surrounding
 * CHAMS_ENTITY state caused them to render as solid black.
 *
 * FIX:
 * Use method = "render" WITHOUT a full descriptor.
 * Mixin will apply this @Redirect to ALL methods named "render" in the class,
 * capturing every call to armorCutoutNoCull() regardless of which overload
 * triggered it. allow = 10 / require = 0 prevents crashes if the number of
 * injection points varies across MC versions.
 */
@Mixin(EquipmentRenderer.class)
public class ArmorChamsMixin {

    /**
     * Redirect armorCutoutNoCull for EVERY armor layer.
     * This fires for: helmet, chestplate, boots (HUMANOID / layer_1)
     *                 leggings                  (HUMANOID_LEGGINGS / layer_2)
     *                 any future layer types
     *
     * No method descriptor = applies to all 'render' overloads in EquipmentRenderer.
     */
    @Redirect(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/RenderLayers;armorCutoutNoCull(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"
        ),
        require = 0,   // don't crash if inject point count changes
        allow   = 10   // allow up to 10 call sites (outer + inner + possible dyeable variants)
    )
    private RenderLayer rich$armorChams(Identifier textureId) {
        Chams chams = Chams.getInstance();
        if (chams != null && chams.isState() && Chams.RICH$EQUIPMENT_TARGET && chams.showArmor.isValue()) {
            if (textureId != null) {
                return ClientPipelines.CHAMS_ENTITY.apply(textureId);
            }
        }
        return RenderLayers.armorCutoutNoCull(textureId);
    }
}
