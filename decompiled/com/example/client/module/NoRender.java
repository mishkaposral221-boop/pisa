/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package com.example.client.module;

import com.example.client.menu.data.ModModule;
import com.example.client.menu.data.Setting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class NoRender {
    private ModModule moduleRef;
    private static NoRender INSTANCE;

    public NoRender() {
        INSTANCE = this;
    }

    public static NoRender getInstance() {
        return INSTANCE;
    }

    public void setModuleRef(ModModule ref) {
        this.moduleRef = ref;
    }

    public boolean isEnabled() {
        return this.moduleRef != null && this.moduleRef.enabled;
    }

    public boolean noHurtCam() {
        return this.isEnabled() && this.getSetting(0).getBool();
    }

    public boolean noFire() {
        return this.isEnabled() && this.getSetting(1).getBool();
    }

    public boolean noNausea() {
        return this.isEnabled() && this.getSetting(2).getBool();
    }

    public boolean noDarkness() {
        return this.isEnabled() && this.getSetting(3).getBool();
    }

    public boolean noSpeedFX() {
        return this.isEnabled() && this.getSetting(4).getBool();
    }

    public boolean noNametag() {
        return this.isEnabled() && this.getSetting(5).getBool();
    }

    private Setting getSetting(int index) {
        if (this.moduleRef == null || index >= this.moduleRef.settings.length) {
            return new Setting("", true);
        }
        return this.moduleRef.settings[index];
    }
}

