/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_634
 *  net.minecraft.class_640
 */
package com.example.client.hud.widgets;

import com.example.client.hud.HudWidget;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_634;
import net.minecraft.class_640;

@Environment(value=EnvType.CLIENT)
public class TopBarWidget
extends HudWidget {
    private String fpsStr = "";
    private String pingStr = "";
    private String time = "";
    private static final int TC = -267521;
    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    public TopBarWidget(float x, float y) {
        super(x, y, 10, 14);
    }

    @Override
    public void tick() {
        class_310 mc = class_310.method_1551();
        this.fpsStr = mc.method_47599() + " Fps";
        class_634 net = mc.method_1562();
        if (net != null && mc.field_1724 != null) {
            class_640 info = net.method_2871(mc.field_1724.method_5667());
            this.pingStr = (info != null ? info.method_2959() : 0) + " Ping";
        }
        this.time = LocalTime.now().format(TF);
        class_327 f = mc.field_1772;
        int pad = 6;
        int sep = 8;
        this.width = pad + f.method_1727("Luminas") + sep + f.method_1727(this.fpsStr) + sep + f.method_1727(this.pingStr) + sep + f.method_1727(this.time) + pad;
        this.height = 14;
    }

    @Override
    public void renderBackground(float alpha) {
    }

    @Override
    public void renderText(class_332 g, float alpha) {
        class_327 f = class_310.method_1551().field_1772;
        int ix = (int)this.x;
        int iy = (int)this.y;
        int col = TopBarWidget.applyA(-267521, alpha);
        int pad = 6;
        int sep = 8;
        int ty = iy + 3;
        int cx = ix + pad;
        g.method_51433(f, "Luminas", cx, ty, TopBarWidget.applyA(-38401, alpha), false);
        g.method_51433(f, this.fpsStr, cx += f.method_1727("Luminas") + sep, ty, col, false);
        g.method_51433(f, this.pingStr, cx += f.method_1727(this.fpsStr) + sep, ty, col, false);
        g.method_51433(f, this.time, cx += f.method_1727(this.pingStr) + sep, ty, col, false);
    }

    private static int applyA(int c, float a) {
        return (int)((float)(c >> 24 & 0xFF) * a) << 24 | c & 0xFFFFFF;
    }
}

