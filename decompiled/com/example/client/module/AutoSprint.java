/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_310
 */
package com.example.client.module;

import com.example.client.menu.data.ModModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;

@Environment(value=EnvType.CLIENT)
public class AutoSprint {
    private ModModule moduleRef;
    private static AutoSprint INSTANCE;

    public AutoSprint() {
        INSTANCE = this;
    }

    public static AutoSprint getInstance() {
        return INSTANCE;
    }

    public void setModuleRef(ModModule ref) {
        this.moduleRef = ref;
    }

    public boolean isEnabled() {
        return this.moduleRef != null && this.moduleRef.enabled;
    }

    public void tick(class_310 client) {
        if (client.field_1724 == null) {
            return;
        }
        if (!this.isEnabled() || client.field_1755 != null) {
            return;
        }
        client.field_1690.field_1867.method_23481(true);
    }
}

