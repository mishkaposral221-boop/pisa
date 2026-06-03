package com.example.client.module;

import com.example.client.menu.data.ModModule;
import net.minecraft.client.MinecraftClient;

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

    public void tick(MinecraftClient client) {
        if (client.player == null) {
            return;
        }
        if (!this.isEnabled() || client.currentScreen != null) {
            return;
        }
        client.options.sprintKey.setPressed(true);
    }
}

