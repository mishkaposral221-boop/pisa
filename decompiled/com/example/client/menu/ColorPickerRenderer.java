/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_332
 */
package com.example.client.menu;

import com.example.client.menu.MenuRenderer;
import com.example.client.menu.layout.MenuLayout;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_332;

@Environment(value=EnvType.CLIENT)
public class ColorPickerRenderer {
    private final MenuLayout layout;
    private final MenuRenderer renderer;

    public ColorPickerRenderer(MenuLayout layout, MenuRenderer renderer) {
        this.layout = layout;
        this.renderer = renderer;
    }

    public void renderShapes(float alpha) {
    }

    public void renderText(class_332 g, float alpha) {
    }

    public boolean handleClick(double mx, double my) {
        return false;
    }

    public boolean handleDrag(double mx, double my) {
        return false;
    }

    public void releaseDrag() {
    }
}

