/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1297
 *  net.minecraft.class_5636
 *  net.minecraft.class_7286
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.example.client.mixin;

import com.example.client.module.NoRender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_5636;
import net.minecraft.class_7286;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_7286.class})
public class FogMixin {
    @Inject(method={"method_42593"}, at={@At(value="HEAD")}, cancellable=true)
    private void onIsApplicable(class_5636 fogType, class_1297 entity, CallbackInfoReturnable<Boolean> cir) {
        NoRender nr = NoRender.getInstance();
        if (nr != null && nr.noDarkness()) {
            cir.setReturnValue((Object)false);
        }
    }
}

