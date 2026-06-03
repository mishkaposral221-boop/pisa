/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 */
package com.example.client.hud.widgets;

import com.example.client.hud.HudWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;

@Environment(value=EnvType.CLIENT)
public class InfoBarWidget
extends HudWidget {
    private String coords = "";
    private String bpsStr = "";
    private String ticksStr = "";
    private String playerName = "";
    private double lastX;
    private double lastY;
    private double lastZ;
    private static final int TC = -267521;

    public InfoBarWidget(float x, float y) {
        super(x, y, 10, 14);
    }

    @Override
    public void tick() {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 != null) {
            double dx = mc.field_1724.method_23317() - this.lastX;
            double dy = mc.field_1724.method_23318() - this.lastY;
            double dz = mc.field_1724.method_23321() - this.lastZ;
            double speed = Math.sqrt(dx * dx + dy * dy + dz * dz) * 20.0;
            this.bpsStr = String.format("%.1f Bps", speed);
            this.lastX = mc.field_1724.method_23317();
            this.lastY = mc.field_1724.method_23318();
            this.lastZ = mc.field_1724.method_23321();
            this.coords = String.format("%d %d %d", (int)mc.field_1724.method_23317(), (int)mc.field_1724.method_23318(), (int)mc.field_1724.method_23321());
            this.playerName = mc.field_1724.method_5477().getString();
            if (mc.field_1687 != null) {
                this.ticksStr = String.format("%.1f Ticks", Float.valueOf(mc.field_1687.method_54719().method_54748()));
            }
        }
        class_327 f = mc.field_1772;
        int p = 6;
        int s = 8;
        this.width = p + f.method_1727(this.coords) + s + f.method_1727(this.bpsStr) + s + f.method_1727(this.ticksStr) + s + f.method_1727(this.playerName) + p;
        this.height = 14;
    }

    @Override
    public void renderBackground(float alpha) {
    }

    @Override
    public void renderText(class_332 g, float alpha) {
        class_327 f = class_310.method_1551().field_1772;
        int col = InfoBarWidget.applyA(-267521, alpha);
        int ty = (int)this.y + 3;
        int cx = (int)this.x + 6;
        g.method_51433(f, this.coords, cx, ty, col, false);
        g.method_51433(f, this.bpsStr, cx += f.method_1727(this.coords) + 8, ty, col, false);
        g.method_51433(f, this.ticksStr, cx += f.method_1727(this.bpsStr) + 8, ty, col, false);
        g.method_51433(f, this.playerName, cx += f.method_1727(this.ticksStr) + 8, ty, col, false);
    }

    private static int applyA(int c, float a) {
        return (int)((float)(c >> 24 & 0xFF) * a) << 24 | c & 0xFFFFFF;
    }
}

