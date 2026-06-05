package rich.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.modules.impl.render.Chams;
import rich.util.render.clientpipeline.ClientPipelines;

/**
 * Renders other players' armor through walls (chams).
 *
 * Respects the {@link Chams#showArmor} setting: if disabled, armor is
 * not swapped and renders with normal depth (NOT through walls).
 */
@Mixin(EquipmentRenderer.class)
public class ArmorChamsMixin {

	private static final String RICH$RENDER = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/util/Identifier;II)V";

	@Unique
	private Identifier rich$lastEquipTexture;

	@Inject(method = RICH$RENDER, at = @At("HEAD"))
	private void rich$resetCapture(CallbackInfo ci) {
		this.rich$lastEquipTexture = null;
	}

	@ModifyExpressionValue(
		method = RICH$RENDER,
		at = @At(value = "INVOKE", target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;")
	)
	private Object rich$captureBaseTexture(Object result) {
		if (result instanceof Identifier) {
			this.rich$lastEquipTexture = (Identifier) result;
		}
		return result;
	}

	/**
	 * Swap the base armor layer to the no-depth chams pipeline.
	 * Only active when Chams is enabled AND showArmor is true.
	 */
	@ModifyExpressionValue(
		method = RICH$RENDER,
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayers;armorCutoutNoCull(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;")
	)
	private RenderLayer rich$armorChamsLayer(RenderLayer original, @Local(argsOnly = true) Identifier textureId) {
		Chams chams = Chams.getInstance();
		if (chams == null || !chams.isState() || !Chams.RICH$EQUIPMENT_TARGET || !chams.showArmor.isValue()) {
			return original;
		}
		Identifier tex = this.rich$lastEquipTexture != null ? this.rich$lastEquipTexture : textureId;
		if (tex == null) {
			return original;
		}
		return ClientPipelines.CHAMS_ENTITY.apply(tex);
	}
}
