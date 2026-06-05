package rich.mixin;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import rich.modules.impl.render.Chams;
import rich.util.render.clientpipeline.ClientPipelines;

/**
 * Makes items held in other players' hands render through walls (chams).
 *
 * Respects the {@link Chams#showItems} setting: if disabled, items
 * are not swapped and render with normal depth (NOT through walls).
 */
@Mixin(targets = "net.minecraft.client.render.command.OrderedRenderCommandQueueImpl")
public class HeldItemChamsMixin {

	@ModifyVariable(method = "submitItem", at = @At("HEAD"), argsOnly = true)
	private RenderLayer rich$heldItemChams(RenderLayer renderLayer) {
		if (renderLayer != null && Chams.RICH$EQUIPMENT_TARGET) {
			Chams chams = Chams.getInstance();
			if (chams != null && chams.showItems.isValue()) {
				return ClientPipelines.CHAMS_ENTITY.apply(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
			}
		}
		return renderLayer;
	}
}
