/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_310
 *  net.minecraft.class_7172
 */
package com.example.client.module;

import com.example.client.menu.data.ModModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_7172;

@Environment(value=EnvType.CLIENT)
public class Fullbright {
    private ModModule moduleRef;
    private static Fullbright INSTANCE;

    public Fullbright() {
        INSTANCE = this;
    }

    public static Fullbright getInstance() {
        return INSTANCE;
    }

    public void setModuleRef(ModModule ref) {
        this.moduleRef = ref;
    }

    public boolean isEnabled() {
        return this.moduleRef != null && this.moduleRef.enabled;
    }

    public void tick(class_310 client) {
        if (client.field_1690 == null) {
            return;
        }
        class_7172 gammaOpt = client.field_1690.method_42473();
        if (gammaOpt == null) {
            return;
        }
        if (this.isEnabled()) {
            if ((Double)gammaOpt.method_41753() < 9999.0) {
                gammaOpt.method_41748((Object)9999.0);
            }
        } else if ((Double)gammaOpt.method_41753() >= 9999.0) {
            gammaOpt.method_41748((Object)1.0);
        }
    }
}

