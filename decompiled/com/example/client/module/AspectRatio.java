/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package com.example.client.module;

import com.example.client.menu.data.ModModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class AspectRatio {
    private ModModule moduleRef;
    private static AspectRatio INSTANCE;

    public AspectRatio() {
        INSTANCE = this;
    }

    public static AspectRatio getInstance() {
        return INSTANCE;
    }

    public void setModuleRef(ModModule ref) {
        this.moduleRef = ref;
    }

    public boolean isEnabled() {
        return this.moduleRef != null && this.moduleRef.enabled;
    }

    public float getAspectRatio() {
        if (!this.isEnabled() || this.moduleRef == null || this.moduleRef.settings.length == 0) {
            return 0.0f;
        }
        return (float)this.moduleRef.settings[0].value / 100.0f;
    }
}

