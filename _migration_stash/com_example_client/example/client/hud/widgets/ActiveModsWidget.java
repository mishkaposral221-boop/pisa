package com.example.client.hud.widgets;

import com.example.client.hud.HudWidget;
import com.example.client.menu.data.Category;
import com.example.client.menu.data.MenuData;
import com.example.client.menu.data.ModModule;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

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
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        this.activeNames.sort(Comparator.comparingInt((String s) -> font.getWidth(s)).reversed());
        int maxW = 0;
        for (String n : this.activeNames) {
            maxW = Math.max(maxW, font.getWidth(n));
        }
        this.width = maxW + 6 + 2;
        this.height = this.activeNames.size() * 10 + 1;
        int sw = MinecraftClient.getInstance().getWindow().getScaledWidth();
        this.x = sw - this.width - 2;
    }

    @Override
    public void renderBackground(float alpha) {
        if (this.activeNames.isEmpty()) {
            return;
        }
    }

    @Override
    public void renderText(DrawContext g, float alpha) {
        if (this.activeNames.isEmpty()) {
            return;
        }
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        int iy = (int)this.y;
        for (int i = 0; i < this.activeNames.size(); ++i) {
            String name = this.activeNames.get(i);
            int nameW = font.getWidth(name);
            float rowW = nameW + 6;
            float rx = this.x + (float)this.width - rowW;
            float hue = (float)i / (float)Math.max(1, this.activeNames.size());
            int r = (int)(180.0 + 75.0 * Math.sin((double)hue * Math.PI * 2.0));
            int gr = (int)(140.0 + 60.0 * Math.sin((double)hue * Math.PI * 2.0 + 2.0));
            int b = 255;
            int color = ActiveModsWidget.applyA(0xFF000000 | r << 16 | gr << 8 | b, alpha);
            g.drawText(font, name, (int)(rx + 3.0f), iy + 1, color, true);
            iy += 10;
        }
    }

    private static int applyA(int c, float a) {
        return (int)((float)(c >> 24 & 0xFF) * a) << 24 | c & 0xFFFFFF;
    }
}

