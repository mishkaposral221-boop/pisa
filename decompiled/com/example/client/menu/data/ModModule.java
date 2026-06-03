/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.lwjgl.glfw.GLFW
 */
package com.example.client.menu.data;

import com.example.client.menu.data.Setting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.glfw.GLFW;

@Environment(value=EnvType.CLIENT)
public class ModModule {
    public final String name;
    public final Setting[] settings;
    public boolean enabled;
    public boolean expanded;
    public float expandAnim = 0.0f;
    public float toggleAnim = 0.0f;
    public int keybind = -1;
    public boolean _keybindWasDown = false;

    public ModModule(String name, Setting[] settings, boolean enabled) {
        this.name = name;
        this.settings = settings;
        this.enabled = enabled;
    }

    public boolean hasKeybind() {
        return this.keybind >= 0;
    }

    public boolean isListening() {
        return this.keybind == -2;
    }

    public String getKeybindName() {
        if (this.keybind == -1) {
            return "";
        }
        if (this.keybind == -2) {
            return "...";
        }
        String name = GLFW.glfwGetKeyName((int)this.keybind, (int)0);
        if (name != null) {
            return name.toUpperCase();
        }
        return "KEY" + this.keybind;
    }
}

