/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1294
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_310
 *  net.minecraft.class_4587
 *  net.minecraft.class_757
 *  net.minecraft.class_9779
 *  org.joml.Matrix4f
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.example.client.mixin;

import com.example.client.module.AspectRatio;
import com.example.client.module.NoRender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_757;
import net.minecraft.class_9779;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_757.class})
public class GameRendererMixin {
    @Shadow
    private float field_4019;
    @Shadow
    private float field_3999;
    @Shadow
    private float field_55871;
    @Shadow
    private float field_55872;

    @Inject(method={"method_3182"}, at={@At(value="TAIL")})
    private void onTick(CallbackInfo ci) {
        NoRender nr = NoRender.getInstance();
        if (nr != null && nr.noNausea()) {
            this.field_55871 = 0.0f;
            this.field_55872 = 0.0f;
        }
    }

    @Inject(method={"method_3188"}, at={@At(value="HEAD")})
    private void onRenderLevel(class_9779 deltaTracker, CallbackInfo ci) {
        NoRender nr = NoRender.getInstance();
        if (nr != null && nr.noNausea()) {
            this.field_55871 = 0.0f;
            this.field_55872 = 0.0f;
        }
    }

    @Inject(method={"method_3167"}, at={@At(value="TAIL")})
    private void afterCheckEntityPostEffect(class_1297 entity, CallbackInfo ci) {
        class_1309 living;
        NoRender nr = NoRender.getInstance();
        if (nr != null && nr.noNausea() && entity instanceof class_1309 && (living = (class_1309)entity).method_6059(class_1294.field_5916)) {
            ((class_757)this).method_62905();
        }
    }

    @Inject(method={"method_3198"}, at={@At(value="HEAD")}, cancellable=true)
    private void onBobHurt(class_4587 poseStack, float partialTick, CallbackInfo ci) {
        NoRender nr = NoRender.getInstance();
        if (nr != null && nr.noHurtCam()) {
            ci.cancel();
        }
    }

    @Inject(method={"method_3186"}, at={@At(value="HEAD")}, cancellable=true)
    private void onBobView(class_4587 poseStack, float partialTick, CallbackInfo ci) {
        NoRender nr = NoRender.getInstance();
        if (nr != null && nr.noSpeedFX()) {
            ci.cancel();
        }
    }

    @Inject(method={"method_3195"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetDarkenWorldAmount(float partialTick, CallbackInfoReturnable<Float> cir) {
        NoRender nr = NoRender.getInstance();
        if (nr != null && nr.noDarkness()) {
            cir.setReturnValue((Object)Float.valueOf(0.0f));
        }
    }

    @Inject(method={"method_3199"}, at={@At(value="HEAD")}, cancellable=true)
    private void onTickFov(CallbackInfo ci) {
        NoRender nr = NoRender.getInstance();
        if (nr != null && nr.noSpeedFX()) {
            this.field_4019 = 1.0f;
            this.field_3999 = 1.0f;
            ci.cancel();
        }
    }

    @Inject(method={"method_22973"}, at={@At(value="RETURN")}, cancellable=true)
    private void onGetProjectionMatrix(CallbackInfoReturnable<Matrix4f> cir) {
        float customRatio;
        AspectRatio ar = AspectRatio.getInstance();
        if (ar != null && ar.isEnabled() && (customRatio = ar.getAspectRatio()) > 0.0f) {
            class_310 client = class_310.method_1551();
            float realRatio = (float)client.method_22683().method_4489() / (float)client.method_22683().method_4506();
            float scaleX = realRatio / customRatio;
            Matrix4f matrix = (Matrix4f)cir.getReturnValue();
            matrix.scale(scaleX, 1.0f, 1.0f);
            cir.setReturnValue((Object)matrix);
        }
    }
}

