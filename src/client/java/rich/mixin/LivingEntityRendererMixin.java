package rich.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.IMinecraft;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.impl.render.Chams;
import rich.modules.impl.render.HitEffect;
import rich.util.render.clientpipeline.ClientPipelines;

/**
 * Chams rendering for enemy players.
 *
 * <h3>Architecture</h3>
 * <ol>
 *   <li>Body (skin): {@link #rich$chamsBodyLayer} redirects {@code getRenderLayer()}
 *       inside {@code render()} to return the unified {@code CHAMS} pipeline layer
 *       for enemy player states.  This gives the skin a NO_DEPTH_TEST draw call
 *       that renders through walls.</li>
 *   <li>Armor: {@link ArmorChamsMixin} intercepts {@code RenderLayers.armorCutoutNoCull()}
 *       (and related methods) when {@code RICH$EQUIPMENT_TARGET == true}.  The
 *       flag is set at HEAD of {@code render()} and cleared at RETURN, so armor
 *       feature renderers that run inside {@code render()} see it as {@code true}.</li>
 * </ol>
 *
 * <h3>Why the unified CHAMS pipeline instead of CHAMS_ENTITY / CHAMS_ARMOR</h3>
 * See {@link ClientPipelines#CHAMS} for the full explanation.  Short version:
 * the old ENTITY_SNIPPET shader applies lightmap multiplication which blackens
 * leggings.  The new custom chams shader skips the lightmap entirely.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<S extends LivingEntityRenderState, M extends EntityModel<? super S>>
        implements IMinecraft {

    // Map render states -> entity IDs so we can check "is this the local player?".
    // WeakHashMap: entries are collected once the render state is no longer referenced.
    @Unique
    private static final Map<LivingEntityRenderState, Integer> RICH$STATE_ID =
            Collections.synchronizedMap(new WeakHashMap<>());

    @Shadow
    @Nullable
    protected abstract RenderLayer getRenderLayer(S state, boolean visible, boolean glowing, boolean translucent);

    @Shadow
    public abstract Identifier getTexture(S state);

    // ---------------------------------------------------------------------------
    // 1) Entity-ID tracking: store the entity ID in the render state so we can
    //    check later whether a state belongs to the local player.
    // ---------------------------------------------------------------------------
    @Inject(
        method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
        at = @At("TAIL")
    )
    private void rich$trackEntityId(LivingEntity entity, S state, float tickDelta, CallbackInfo ci) {
        RICH$STATE_ID.put(state, entity.getId());
    }

    // ---------------------------------------------------------------------------
    // 2) Set RICH$EQUIPMENT_TARGET at HEAD of render() so that ArmorChamsMixin
    //    can see the flag while processing armor feature renderers inside render().
    //    Clear it at RETURN so it does not bleed into the next entity render.
    // ---------------------------------------------------------------------------
    @Inject(
        method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
        at = @At("HEAD")
    )
    private void rich$chamsBegin(
            LivingEntityRenderState state, MatrixStack matrices,
            OrderedRenderCommandQueue queue, CameraRenderState camera, CallbackInfo ci) {
        Chams.RICH$EQUIPMENT_TARGET = rich$isChamsTarget(state);
    }

    @Inject(
        method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
        at = @At("RETURN")
    )
    private void rich$chamsEnd(
            LivingEntityRenderState state, MatrixStack matrices,
            OrderedRenderCommandQueue queue, CameraRenderState camera, CallbackInfo ci) {
        Chams.RICH$EQUIPMENT_TARGET = false;
    }

    // ---------------------------------------------------------------------------
    // 3) Body (skin) through-wall rendering.
    //    Redirect getRenderLayer() -> CHAMS pipeline for chams targets.
    //    CHAMS uses NO_DEPTH_TEST + custom flat shader (full brightness, no lightmap).
    // ---------------------------------------------------------------------------
    @Redirect(
        method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getRenderLayer(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/render/RenderLayer;"
        )
    )
    private RenderLayer rich$chamsBodyLayer(
            LivingEntityRenderer<?, ?, ?> self,
            LivingEntityRenderState state, boolean visible, boolean glowing, boolean translucent) {

        Integer entityId = RICH$STATE_ID.get(state);
        Chams chams = Chams.getInstance();

        // Draw enemy players through walls with their real skin texture.
        // Use the unified CHAMS pipeline (no lightmap → no dark skin areas).
        if (chams != null && chams.isState() && state instanceof PlayerEntityRenderState) {
            boolean isSelf = (entityId != null && mc.player != null && entityId == mc.player.getId());
            if (!isSelf) {
                Identifier texture = this.getTexture((S) state);
                if (texture != null) {
                    return ClientPipelines.CHAMS.apply(texture);
                }
            }
        }

        // HitEffect tint: force glowing=true so vanilla applies the tint color.
        HitEffect hitEffect = HitEffect.getInstance();
        if (!glowing && entityId != null && hitEffect != null && hitEffect.shouldTintEntity(entityId)) {
            glowing = true;
        }

        return this.getRenderLayer((S) state, visible, glowing, translucent);
    }

    // ---------------------------------------------------------------------------
    // 4) Smooth rotation for the local player (silent aim / angle connection).
    // ---------------------------------------------------------------------------
    @ModifyExpressionValue(
        method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerpAngleDegrees(FFF)F")
    )
    private float rich$lerpYaw(float original,
            @Local(ordinal = 0, argsOnly = true) LivingEntity entity,
            @Local(ordinal = 0, argsOnly = true) float tickDelta) {
        AngleConnection ac = AngleConnection.INSTANCE;
        if (entity.equals(mc.player) && ac.getCurrentAngle() != null
                && !(mc.currentScreen instanceof HandledScreen)) {
            return MathHelper.lerpAngleDegrees(
                tickDelta,
                ac.getPreviousRotation().getYaw(),
                ac.getRotation().getYaw());
        }
        return original;
    }

    @ModifyExpressionValue(
        method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getLerpedPitch(F)F")
    )
    private float rich$lerpPitch(float original,
            @Local(ordinal = 0, argsOnly = true) LivingEntity entity,
            @Local(ordinal = 0, argsOnly = true) float tickDelta) {
        AngleConnection ac = AngleConnection.INSTANCE;
        if (entity.equals(mc.player) && ac.getCurrentAngle() != null
                && !(mc.currentScreen instanceof HandledScreen)) {
            return MathHelper.lerp(
                tickDelta,
                ac.getPreviousRotation().getPitch(),
                ac.getRotation().getPitch());
        }
        return original;
    }

    // ---------------------------------------------------------------------------
    // 5) HitEffect color tint blending.
    // ---------------------------------------------------------------------------
    @ModifyReturnValue(
        method = "getMixColor(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;)I",
        at = @At("RETURN")
    )
    private int rich$mixColor(int original, @Local(argsOnly = true) S state) {
        Integer entityId = RICH$STATE_ID.get(state);
        HitEffect hitEffect = HitEffect.getInstance();
        if (entityId == null || hitEffect == null || !hitEffect.shouldTintEntity(entityId)) {
            return original;
        }
        int tint  = hitEffect.getEntityTintColor();
        float alpha = ((tint >> 24) & 0xFF) / 255.0F;
        int tr = (tint >> 16) & 0xFF;
        int tg = (tint >>  8) & 0xFF;
        int tb =  tint        & 0xFF;
        int or = (original >> 16) & 0xFF;
        int og = (original >>  8) & 0xFF;
        int ob =  original        & 0xFF;
        int oa = (original >> 24) & 0xFF;
        int nr = Math.min(255, Math.max(0, (int)(or + (or * tr / 255.0F - or) * alpha)));
        int ng = Math.min(255, Math.max(0, (int)(og + (og * tg / 255.0F - og) * alpha)));
        int nb = Math.min(255, Math.max(0, (int)(ob + (ob * tb / 255.0F - ob) * alpha)));
        return (oa << 24) | (nr << 16) | (ng << 8) | nb;
    }

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------
    @Unique
    private boolean rich$isChamsTarget(LivingEntityRenderState state) {
        Chams chams = Chams.getInstance();
        if (chams == null || !chams.isState()) return false;
        if (!(state instanceof PlayerEntityRenderState)) return false;
        Integer id = RICH$STATE_ID.get(state);
        return id != null && mc.player != null && id != mc.player.getId();
    }
}
