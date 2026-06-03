package com.example.client.mixin;

import com.example.client.module.AspectRatio;
import com.example.client.module.NoRender;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={GameRenderer.class})
public class GameRendererMixin {
    @Shadow
    private float fovMultiplier;
    @Shadow
    private float lastFovMultiplier;
    @Shadow
    private float nauseaEffectTime;
    @Shadow
    private float nauseaEffectSpeed;

    @Inject(method={"tick"}, at={@At(value="TAIL")})
    private void onTick(CallbackInfo ci) {
        NoRender nr = NoRender.getInstance();
        if (nr != null && nr.noNausea()) {
            this.nauseaEffectTime = 0.0f;
            this.nauseaEffectSpeed = 0.0f;
        }
    }

    @Inject(method={"renderWorld"}, at={@At(value="HEAD")})
    private void onRenderLevel(RenderTickCounter deltaTracker, CallbackInfo ci) {
        NoRender nr = NoRender.getInstance();
        if (nr != null && nr.noNausea()) {
            this.nauseaEffectTime = 0.0f;
            this.nauseaEffectSpeed = 0.0f;
        }
    }

    @Inject(method={"onCameraEntitySet"}, at={@At(value="TAIL")})
    private void afterCheckEntityPostEffect(Entity entity, CallbackInfo ci) {
        LivingEntity living;
        NoRender nr = NoRender.getInstance();
        if (nr != null && nr.noNausea() && entity instanceof LivingEntity && (living = (LivingEntity)entity).hasStatusEffect(StatusEffects.NAUSEA)) {
            ((GameRenderer)(Object)this).clearPostProcessor();
        }
    }

    @Inject(method={"tiltViewWhenHurt"}, at={@At(value="HEAD")}, cancellable=true)
    private void onBobHurt(MatrixStack poseStack, float partialTick, CallbackInfo ci) {
        NoRender nr = NoRender.getInstance();
        if (nr != null && nr.noHurtCam()) {
            ci.cancel();
        }
    }

    @Inject(method={"bobView"}, at={@At(value="HEAD")}, cancellable=true)
    private void onBobView(MatrixStack poseStack, float partialTick, CallbackInfo ci) {
        NoRender nr = NoRender.getInstance();
        if (nr != null && nr.noSpeedFX()) {
            ci.cancel();
        }
    }

    @Inject(method={"getSkyDarkness"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetDarkenWorldAmount(float partialTick, CallbackInfoReturnable<Float> cir) {
        NoRender nr = NoRender.getInstance();
        if (nr != null && nr.noDarkness()) {
            cir.setReturnValue(0.0f);
        }
    }

    @Inject(method={"updateFovMultiplier"}, at={@At(value="HEAD")}, cancellable=true)
    private void onTickFov(CallbackInfo ci) {
        NoRender nr = NoRender.getInstance();
        if (nr != null && nr.noSpeedFX()) {
            this.fovMultiplier = 1.0f;
            this.lastFovMultiplier = 1.0f;
            ci.cancel();
        }
    }

    @Inject(method={"getBasicProjectionMatrix"}, at={@At(value="RETURN")}, cancellable=true)
    private void onGetProjectionMatrix(CallbackInfoReturnable<Matrix4f> cir) {
        float customRatio;
        AspectRatio ar = AspectRatio.getInstance();
        if (ar != null && ar.isEnabled() && (customRatio = ar.getAspectRatio()) > 0.0f) {
            MinecraftClient client = MinecraftClient.getInstance();
            float realRatio = (float)client.getWindow().getFramebufferWidth() / (float)client.getWindow().getFramebufferHeight();
            float scaleX = realRatio / customRatio;
            Matrix4f matrix = (Matrix4f)cir.getReturnValue();
            matrix.scale(scaleX, 1.0f, 1.0f);
            cir.setReturnValue(matrix);
        }
    }
}

