/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1291
 *  net.minecraft.class_1293
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 */
package com.example.client.hud.widgets;

import com.example.client.hud.HudWidget;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;

@Environment(value=EnvType.CLIENT)
public class PotionsWidget
extends HudWidget {
    private static final int HEADER_H = 18;
    private static final int ROW_H = 18;
    private final List<PotionEntry> entries = new ArrayList<PotionEntry>();

    public PotionsWidget(float x, float y) {
        super(x, y, 92, 18);
    }

    @Override
    public void tick() {
        this.entries.clear();
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null) {
            return;
        }
        for (class_1293 e : mc.field_1724.method_6026()) {
            Object name = ((class_1291)e.method_5579().comp_349()).method_5567();
            int dot = ((String)name).lastIndexOf(46);
            if (dot >= 0) {
                name = ((String)name).substring(dot + 1);
            }
            name = ((String)name).substring(0, 1).toUpperCase() + ((String)name).substring(1);
            int amp = e.method_5578();
            if (amp > 0) {
                name = (String)name + " " + (amp + 1);
            }
            int dur = e.method_5584();
            int totalSec = dur / 20;
            String time = dur > 0 && dur < 655340 ? String.format("%02d:%02d", totalSec / 60, totalSec % 60) : "**:**";
            this.entries.add(new PotionEntry((String)name, time));
        }
        class_327 f = mc.field_1772;
        int maxW = f.method_1727("Effects") + 14;
        for (PotionEntry en : this.entries) {
            maxW = Math.max(maxW, f.method_1727(en.name) + 14 + f.method_1727(en.time) + 14);
        }
        this.width = maxW;
        this.height = 18;
        if (!this.entries.isEmpty()) {
            this.height += 5 + this.entries.size() * 18;
        }
    }

    @Override
    public void renderBackground(float alpha) {
    }

    @Override
    public void renderText(class_332 g, float alpha) {
        if (this.entries.isEmpty()) {
            return;
        }
        int ix = (int)this.x;
        int iy = (int)this.y;
        int bgAlpha = (int)(170.0f * alpha);
        g.method_25294(ix, iy, ix + this.width, iy + this.height, bgAlpha << 24 | 0x140A28);
        int sepAlpha = (int)(30.0f * alpha);
        g.method_25294(ix, iy + 18, ix + this.width, iy + 18 + 1, sepAlpha << 24 | 0xB464FF);
        for (int i = 1; i < this.entries.size(); ++i) {
            int ry = iy + 18 + 4 + i * 18;
            int rowSepAlpha = (int)(18.0f * alpha);
            g.method_25294(ix, ry, ix + this.width, ry + 1, rowSepAlpha << 24 | 0xB464FF);
        }
        class_327 f = class_310.method_1551().field_1772;
        g.method_51433(f, "Effects", ix + 7, iy + 5, PotionsWidget.applyA(-1, alpha), false);
        int rowStart = iy + 18 + 4;
        for (int i = 0; i < this.entries.size(); ++i) {
            PotionEntry e = this.entries.get(i);
            int ry = rowStart + i * 18 + 5;
            g.method_51433(f, e.name, ix + 7, ry, PotionsWidget.applyA(-1, alpha), false);
            g.method_51433(f, e.time, ix + this.width - 7 - f.method_1727(e.time), ry, PotionsWidget.applyA(-1073741825, alpha), false);
        }
    }

    private static int applyA(int c, float a) {
        return (int)((float)(c >> 24 & 0xFF) * a) << 24 | c & 0xFFFFFF;
    }

    @Environment(value=EnvType.CLIENT)
    private record PotionEntry(String name, String time) {
    }
}

