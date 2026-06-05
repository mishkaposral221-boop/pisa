package rich.mixin;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.modules.impl.render.Chams;
import rich.util.render.clientpipeline.ClientPipelines;

/**
 * Makes items held in other players' hands render through walls (chams).
 *
 * <p>{@link ItemRenderState.LayerRenderState#render} submits the item geometry to
 * {@code OrderedRenderCommandQueue#submitItem} using its {@code renderLayer} field. That
 * layer carries the depth-test state. While {@link Chams#RICH$EQUIPMENT_TARGET} is active
 * (set by {@code LivingEntityRendererMixin} around non-local player rendering), we swap the
 * layer for {@link ClientPipelines#CHAMS_ENTITY} bound to the block atlas, which uses the
 * same entity vertex format (lightmap + overlay) but {@code NO_DEPTH_TEST}, so the item is
 * visible through geometry.</p>
 *
 * <p>The field is rebuilt every frame by the item model update, so this per-frame overwrite
 * is transient. Special-model items (shields, tridents, banners, etc.) take the
 * {@code specialModelType} branch where {@code renderLayer} is null and are intentionally
 * left untouched.</p>
 */
@Mixin(ItemRenderState.LayerRenderState.class)
public abstract class HeldItemChamsMixin {

	@Shadow
	private RenderLayer renderLayer;

	@Inject(method = "render", at = @At("HEAD"))
	private void rich$heldItemChams(
		MatrixStack matrices,
		OrderedRenderCommandQueue queue,
		int light,
		int overlay,
		int i,
		CallbackInfo ci
	) {
		if (this.renderLayer != null && Chams.RICH$EQUIPMENT_TARGET) {
			this.renderLayer = ClientPipelines.CHAMS_ENTITY.apply(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
		}
	}
}
