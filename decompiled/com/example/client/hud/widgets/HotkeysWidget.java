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
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;

@Environment(value=EnvType.CLIENT)
public class HotkeysWidget
extends HudWidget {
    private static final int HEADER_H = 18;
    private static final int ROW_H = 18;
    private final List<HotkeyEntry> entries = new ArrayList<HotkeyEntry>();

    public HotkeysWidget(float x, float y) {
        super(x, y, 92, 18);
    }

    @Override
    public void tick() {
        this.entries.clear();
        for (Category cat : MenuData.CATEGORIES) {
            for (ModModule mod : cat.modules) {
                if (!mod.enabled || mod.keybind <= 0) continue;
                this.entries.add(new HotkeyEntry(mod.name, mod.getKeybindName()));
            }
        }
        class_327 f = class_310.method_1551().field_1772;
        int maxW = f.method_1727("Keybinds") + 14;
        for (HotkeyEntry e : this.entries) {
            maxW = Math.max(maxW, f.method_1727(e.name) + 14 + f.method_1727(e.key));
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
        g.method_51433(f, "Keybinds", ix + 7, iy + 5, HotkeysWidget.applyA(-1, alpha), false);
        int rowStart = iy + 18 + 4;
        for (int i = 0; i < this.entries.size(); ++i) {
            HotkeyEntry e = this.entries.get(i);
            int ry = rowStart + i * 18 + 5;
            g.method_51433(f, e.name, ix + 7, ry, HotkeysWidget.applyA(-1, alpha), false);
            g.method_51433(f, e.key, ix + this.width - 7 - f.method_1727(e.key), ry, HotkeysWidget.applyA(-1073741825, alpha), false);
        }
    }

    private static int applyA(int c, float a) {
        return (int)((float)(c >> 24 & 0xFF) * a) << 24 | c & 0xFFFFFF;
    }

    @Environment(value=EnvType.CLIENT)
    private record HotkeyEntry(String name, String key) {
    }
}

