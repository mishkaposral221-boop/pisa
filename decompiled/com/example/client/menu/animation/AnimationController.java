/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package com.example.client.menu.animation;

import com.example.client.menu.data.Category;
import com.example.client.menu.data.MenuData;
import com.example.client.menu.data.ModModule;
import com.example.client.menu.util.Easing;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class AnimationController {
    private float animProgress = 0.0f;
    private long lastFrameTime = System.currentTimeMillis();

    public void updateOpenAnimation(boolean open) {
        long now = System.currentTimeMillis();
        float dt = (float)(now - this.lastFrameTime) / 1000.0f;
        this.lastFrameTime = now;
        if (open && this.animProgress < 1.0f) {
            this.animProgress = Math.min(1.0f, this.animProgress + dt * 6.0f);
        } else if (!open && this.animProgress > 0.0f) {
            this.animProgress = Math.max(0.0f, this.animProgress - dt * 4.0f);
        }
    }

    public float getAlpha() {
        return Easing.easeOutCubic(this.animProgress);
    }

    public boolean shouldRender() {
        return this.animProgress > 0.01f;
    }

    public void resetAnim() {
        this.animProgress = 0.0f;
    }

    public void updateModuleAnimations() {
        for (Category cat : MenuData.CATEGORIES) {
            for (ModModule mod : cat.modules) {
                float targetExp = mod.expanded ? 1.0f : 0.0f;
                mod.expandAnim += (targetExp - mod.expandAnim) * 0.35f;
                if (Math.abs(mod.expandAnim - targetExp) < 0.01f) {
                    mod.expandAnim = targetExp;
                }
                float targetTog = mod.enabled ? 1.0f : 0.0f;
                mod.toggleAnim += (targetTog - mod.toggleAnim) * 0.35f;
                if (!(Math.abs(mod.toggleAnim - targetTog) < 0.01f)) continue;
                mod.toggleAnim = targetTog;
            }
        }
    }
}

