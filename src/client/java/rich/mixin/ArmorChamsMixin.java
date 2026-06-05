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
 * Uses a simple @Redirect on armorCutoutNoCull which is called for EVERY
 * armor layer including the inner layer (leggings, layer_2.png).
 * This avoids the null-texture issue that caused leggings to appear black.
 */
@Mixin(EquipmentRenderer.class)
public class ArmorChamsMixin {

	private static final String RICH$RENDER =
		"render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;"
		+ "Lnet/minecraft/registry/RegistryKey;"
		+ "Lnet/minecraft/client/model/Model;"
		+ "Ljava/lang/Object;"
		+ "Lnet/minecraft/item/ItemStack;"
		+ "Lnet/minecraft/client/util/math/MatrixStack;"
		+ "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;"
		+ "I"
		+ "Lnet/minecraft/util/Identifier;"
		+ "II)V";

	/**
	 * Redirect armorCutoutNoCull for both outer (layer_1) and inner (layer_2 = leggings)
	 * armor layers. The textureId is the ACTUAL armor texture that Minecraft resolved,
	 * so passing it directly to CHAMS_ENTITY avoids any null-texture issue.
	 */
	@Redirect(
		method = RICH$RENDER,
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/RenderLayers;armorCutoutNoCull(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"
		)
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
