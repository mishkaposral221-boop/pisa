/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_10017
 *  net.minecraft.class_1007
 *  net.minecraft.class_11659
 *  net.minecraft.class_12075
 *  net.minecraft.class_4587
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.example.client.mixin;

import com.example.client.module.NoRender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10017;
import net.minecraft.class_1007;
import net.minecraft.class_11659;
import net.minecraft.class_12075;
import net.minecraft.class_4587;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_1007.class})
public class EntityRendererMixin {
    @Inject(method={"method_3926"}, at={@At(value="HEAD")}, cancellable=true)
    protected void onSubmitNameTag(class_10017 state, class_4587 poseStack, class_11659 collector, class_12075 cameraState, CallbackInfo ci) {
        if (NoRender.getInstance() != null && NoRender.getInstance().noNametag()) {
            ci.cancel();
        }
    }
}

