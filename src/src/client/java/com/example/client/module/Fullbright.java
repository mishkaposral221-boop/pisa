package com.example.client.module;

import com.example.client.menu.data.ModModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;

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

    public void tick(MinecraftClient client) {
        if (client.options == null) {
            return;
        }
        SimpleOption gammaOpt = client.options.getGamma();
        if (gammaOpt == null) {
            return;
        }
        if (this.isEnabled()) {
            if ((Double)gammaOpt.getValue() < 9999.0) {
                gammaOpt.setValue((Object)9999.0);
            }
        } else if ((Double)gammaOpt.getValue() >= 9999.0) {
            gammaOpt.setValue((Object)1.0);
        }
    }
}

