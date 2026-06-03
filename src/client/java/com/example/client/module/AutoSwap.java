package com.example.client.module;

import com.example.client.menu.data.ModModule;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

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

    public void onKeybindPress(MinecraftClient client) {
        if (!this.isEnabled() || client.player == null) {
            return;
        }
        KeyBinding.onKeyPressed((InputUtil.Key)client.options.swapHandsKey.getDefaultKey());
    }
}

