package com.example.client.menu;

import com.example.client.menu.MenuRenderer;
import com.example.client.menu.layout.MenuLayout;
import net.minecraft.client.gui.DrawContext;

public class ColorPickerRenderer {
    private final MenuLayout layout;
    private final MenuRenderer renderer;

    public ColorPickerRenderer(MenuLayout layout, MenuRenderer renderer) {
        this.layout = layout;
        this.renderer = renderer;
    }

    public void renderShapes(float alpha) {
    }

    public void renderText(DrawContext g, float alpha) {
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

