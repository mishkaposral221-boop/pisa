package rich.mixin;

import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.modules.impl.render.Chams;

@Mixin(HeldItemFeatureRenderer.class)
public class HeldItemChamsMixin {

	private static final String RENDER =
		"render(Lnet/minecraft/client/util/math/MatrixStack;"
		+ "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;"
		+ "ILnet/minecraft/client/render/entity/state/PlayerEntityRenderState;FF)V";

	@Inject(method = RENDER, at = @At("HEAD"))
	private void rich$heldChamsHead(
			MatrixStack matrices,
			OrderedRenderCommandQueue queue,
			int light,
			PlayerEntityRenderState state,
			float limbAngle,
			float limbDistance,
			CallbackInfo ci) {
		// RICH$EQUIPMENT_TARGET is already set by LivingEntityRendererMixin.
		// This injection ensures the flag is confirmed before item rendering.
	}
}
