package com.example.client.hud;

import net.minecraft.client.gui.DrawContext;

public abstract class HudWidget {
    public float x;
    public float y;
    public int width;
    public int height;
    public boolean visible = true;
    public boolean dragging = false;
    private float dragOffX;
    private float dragOffY;

    public HudWidget(float x, float y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void renderBackground(float var1);

    public abstract void renderText(DrawContext var1, float var2);

    public abstract void tick();

    public boolean isHovered(double mx, double my) {
        return mx >= (double)this.x && mx <= (double)(this.x + (float)this.width) && my >= (double)this.y && my <= (double)(this.y + (float)this.height);
    }

    public void startDrag(double mx, double my) {
        this.dragging = true;
        this.dragOffX = (float)(mx - (double)this.x);
        this.dragOffY = (float)(my - (double)this.y);
    }

    public void updateDrag(double mx, double my) {
        if (this.dragging) {
            this.x = (float)(mx - (double)this.dragOffX);
            this.y = (float)(my - (double)this.dragOffY);
        }
    }

    public void stopDrag() {
        this.dragging = false;
    }
}

