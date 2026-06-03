/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1058
 *  net.minecraft.class_4587
 *  net.minecraft.class_4597
 *  net.minecraft.class_4603
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.example.client.mixin;

import com.example.client.module.NoRender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1058;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_4603;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_4603.class})
public class GuiMixin {
    @Inject(method={"method_23070"}, at={@At(value="HEAD")}, cancellable=true)
    private static void onRenderFire(class_4587 poseStack, class_4597 bufferSource, class_1058 sprite, CallbackInfo ci) {
        NoRender nr = NoRender.getInstance();
        if (nr != null && nr.noFire()) {
            ci.cancel();
        }
    }
}

