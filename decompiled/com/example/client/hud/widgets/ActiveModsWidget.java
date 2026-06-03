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
import com.example.client.menu.data.Category;
import com.example.client.menu.data.MenuData;
import com.example.client.menu.data.ModModule;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;

@Environment(value=EnvType.CLIENT)
public class ActiveModsWidget
extends HudWidget {
    private static final int ROW_H = 10;
    private static final int PAD = 3;
    private final List<String> activeNames = new ArrayList<String>();

    public ActiveModsWidget() {
        super(0.0f, 2.0f, 60, 10);
    }

    @Override
    public void tick() {
        this.activeNames.clear();
        for (Category cat : MenuData.CATEGORIES) {
            for (ModModule mod : cat.modules) {
                if (!mod.enabled) continue;
                this.activeNames.add(mod.name);
            }
        }
        class_327 font = class_310.method_1551().field_1772;
        this.activeNames.sort(Comparator.comparingInt(s -> font.method_1727(s)).reversed());
        int maxW = 0;
        for (String n : this.activeNames) {
            maxW = Math.max(maxW, font.method_1727(n));
        }
        this.width = maxW + 6 + 2;
        this.height = this.activeNames.size() * 10 + 1;
        int sw = class_310.method_1551().method_22683().method_4486();
        this.x = sw - this.width - 2;
    }

    @Override
    public void renderBackground(float alpha) {
        if (this.activeNames.isEmpty()) {
            return;
        }
    }

    @Override
    public void renderText(class_332 g, float alpha) {
        if (this.activeNames.isEmpty()) {
            return;
        }
        class_327 font = class_310.method_1551().field_1772;
        int iy = (int)this.y;
        for (int i = 0; i < this.activeNames.size(); ++i) {
            String name = this.activeNames.get(i);
            int nameW = font.method_1727(name);
            float rowW = nameW + 6;
            float rx = this.x + (float)this.width - rowW;
            float hue = (float)i / (float)Math.max(1, this.activeNames.size());
            int r = (int)(180.0 + 75.0 * Math.sin((double)hue * Math.PI * 2.0));
            int gr = (int)(140.0 + 60.0 * Math.sin((double)hue * Math.PI * 2.0 + 2.0));
            int b = 255;
            int color = ActiveModsWidget.applyA(0xFF000000 | r << 16 | gr << 8 | b, alpha);
            g.method_51433(font, name, (int)(rx + 3.0f), iy + 1, color, true);
            iy += 10;
        }
    }

    private static int applyA(int c, float a) {
        return (int)((float)(c >> 24 & 0xFF) * a) << 24 | c & 0xFFFFFF;
    }
}

