/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_304
 *  net.minecraft.class_310
 *  net.minecraft.class_3675$class_306
 */
package com.example.client.module;

import com.example.client.menu.data.ModModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;

@Environment(value=EnvType.CLIENT)
public class AutoSwap {
    private ModModule moduleRef;
    private static AutoSwap INSTANCE;

    public AutoSwap() {
        INSTANCE = this;
    }

    public static AutoSwap getInstance() {
        return INSTANCE;
    }

    public void setModuleRef(ModModule ref) {
        this.moduleRef = ref;
    }

    public boolean isEnabled() {
        return this.moduleRef != null && this.moduleRef.enabled;
    }

    public void onKeybindPress(class_310 client) {
        if (!this.isEnabled() || client.field_1724 == null) {
            return;
        }
        class_304.method_1420((class_3675.class_306)client.field_1690.field_1831.method_1429());
    }
}

