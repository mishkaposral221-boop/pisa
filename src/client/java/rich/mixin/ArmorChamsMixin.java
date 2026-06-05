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
 * <p>{@code EquipmentRenderer#render} draws each armor layer with
 * {@code RenderLayers.armorCutoutNoCull(identifier)} and may additionally draw an
 * enchantment glint ({@code armorEntityGlint()}) and an armor trim
 * ({@code TexturedRenderLayers.getArmorTrims(...)}) on top. We swap ONLY the base
 * armor layer to the no-depth {@link ClientPipelines#CHAMS_ENTITY} pipeline, using
 * the texture resolved for THAT specific layer. The glint and trim submissions are
 * intentionally left alone — previously swapping them with the (wrong) base texture
 * is what made dyed/enchanted leggings render solid black.</p>
 *
 * <p>The base texture for each layer is resolved through {@code this.layerTextures.apply(key)}
 * immediately before {@code armorCutoutNoCull} is called, so we capture the latest
 * {@link Identifier} produced by any {@code Function#apply} in the method and use it for the
 * very next layer swap. This guarantees the helmet / chestplate / leggings / boots each keep
 * their own texture.</p>
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

	/**
	 * Capture the texture resolved for the layer that is about to be submitted.
	 * {@code this.layerTextures.apply(...)} (and {@code this.trimSprites.apply(...)}) are the only
	 * {@code Function#apply} calls in render(); only the former returns an {@link Identifier},
	 * so the trim sprite lookup is ignored. We keep the LATEST identifier (per-layer), not the first.
	 */
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
	 * Swap ONLY the base armor layer to the no-depth chams pipeline, using this layer's own texture.
	 * Glint ({@code armorEntityGlint()}) and trim ({@code getArmorTrims(...)}) are not targeted here,
	 * so they keep their normal layers and no longer corrupt the leggings.
	 */
	@ModifyExpressionValue(
		method = RICH$RENDER,
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayers;armorCutoutNoCull(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;")
	)
	private RenderLayer rich$armorChamsLayer(RenderLayer original, @Local(argsOnly = true) Identifier textureId) {
		Chams chams = Chams.getInstance();
		if (chams == null || !chams.isState() || !Chams.RICH$EQUIPMENT_TARGET) {
			return original;
		}
		Identifier tex = this.rich$lastEquipTexture != null ? this.rich$lastEquipTexture : textureId;
		if (tex == null) {
			return original;
		}
		return ClientPipelines.CHAMS_ENTITY.apply(tex);
	}
}
