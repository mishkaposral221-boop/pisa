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
 * <p>Items are submitted via {@code OrderedRenderCommandQueueImpl#submitItem(..., RenderLayer
 * renderLayer, ItemRenderState.Glint glint)}; that {@code renderLayer} carries the depth-test
 * state used when the queued item geometry is finally drawn. While {@link Chams#RICH$EQUIPMENT_TARGET}
 * is active (set by {@code LivingEntityRendererMixin} only around non-local player rendering), we
 * swap that layer for {@link ClientPipelines#CHAMS_ENTITY} bound to the block atlas. It uses the same
 * entity vertex format (lightmap + overlay) but {@code NO_DEPTH_TEST}, so the item shows through walls.</p>
 *
 * <p>We target the queue implementation rather than the {@code ItemRenderState$LayerRenderState}
 * inner class so the hook is on a stable, top-level consumption point. The flag is only true during
 * other players' render, so the local player's own held item and all GUI item rendering are untouched.
 * Special-model items (shields, tridents, banners) pass {@code renderLayer == null} and are skipped.</p>
 */
@Mixin(targets = "net.minecraft.client.render.command.OrderedRenderCommandQueueImpl")
public class HeldItemChamsMixin {

	@ModifyVariable(method = "submitItem", at = @At("HEAD"), argsOnly = true)
	private RenderLayer rich$heldItemChams(RenderLayer renderLayer) {
		if (renderLayer != null && Chams.RICH$EQUIPMENT_TARGET) {
			return ClientPipelines.CHAMS_ENTITY.apply(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
		}
		return renderLayer;
	}
}
