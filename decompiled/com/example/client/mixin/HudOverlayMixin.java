/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1297
 *  net.minecraft.class_329
 *  net.minecraft.class_332
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.example.client.mixin;

import com.example.client.module.NoRender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_329;
import net.minecraft.class_332;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_329.class})
public class HudOverlayMixin {
    @Inject(method={"method_61980"}, at={@At(value="HEAD")}, cancellable=true)
    private void onConfusion(class_332 g, float f, CallbackInfo ci) {
        NoRender nr = NoRender.getInstance();
        if (nr != null && nr.noNausea()) {
            ci.cancel();
        }
    }

    @Inject(method={"method_1746"}, at={@At(value="HEAD")}, cancellable=true)
    private void onPortal(class_332 g, float f, CallbackInfo ci) {
        NoRender nr = NoRender.getInstance();
        if (nr != null && nr.noNausea()) {
            ci.cancel();
        }
    }

    @Inject(method={"method_1735"}, at={@At(value="HEAD")}, cancellable=true)
    private void onVignette(class_332 g, class_1297 entity, CallbackInfo ci) {
        NoRender nr = NoRender.getInstance();
        if (nr != null && nr.noDarkness()) {
            ci.cancel();
        }
    }
}

