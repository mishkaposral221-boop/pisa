/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_11909
 *  net.minecraft.class_408
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.example.client.mixin;

import com.example.client.hud.HudManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11909;
import net.minecraft.class_408;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_408.class})
public class ChatScreenMixin {
    @Inject(method={"method_25402"}, at={@At(value="HEAD")}, cancellable=true)
    private void onMouseClicked(class_11909 event, boolean consumed, CallbackInfoReturnable<Boolean> cir) {
        HudManager hud = HudManager.getInstance();
        if (hud != null && event.comp_4800().comp_4801() == 0 && hud.mouseClicked(event.comp_4798(), event.comp_4799(), 0)) {
            cir.setReturnValue((Object)true);
        }
    }
}

