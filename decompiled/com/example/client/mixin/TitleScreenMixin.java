/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_364
 *  net.minecraft.class_4185
 *  net.minecraft.class_437
 *  net.minecraft.class_442
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.example.client.mixin;

import com.example.client.account.AccountSwitcherScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_364;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_442;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value=EnvType.CLIENT)
@Mixin(value={class_442.class})
public abstract class TitleScreenMixin
extends class_437 {
    protected TitleScreenMixin(class_2561 title) {
        super(title);
    }

    @Inject(method={"method_25426"}, at={@At(value="TAIL")})
    private void onInit(CallbackInfo ci) {
        int x = this.field_22789 / 2 + 104;
        int y = this.field_22790 / 4 + 48 + 72;
        class_4185 accountsBtn = class_4185.method_46430((class_2561)class_2561.method_43470((String)"Accounts"), btn -> class_310.method_1551().method_1507((class_437)new AccountSwitcherScreen())).method_46434(x, y, 50, 20).method_46431();
        this.method_37063((class_364)accountsBtn);
    }
}

