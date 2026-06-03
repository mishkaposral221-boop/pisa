package com.example.client.hud.widgets;

import com.example.client.hud.HudWidget;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

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
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }
        for (StatusEffectInstance e : mc.player.getStatusEffects()) {
            Object name = ((StatusEffect)e.getEffectType().value()).getTranslationKey();
            int dot = ((String)name).lastIndexOf(46);
            if (dot >= 0) {
                name = ((String)name).substring(dot + 1);
            }
            name = ((String)name).substring(0, 1).toUpperCase() + ((String)name).substring(1);
            int amp = e.getAmplifier();
            if (amp > 0) {
                name = (String)name + " " + (amp + 1);
            }
            int dur = e.getDuration();
            int totalSec = dur / 20;
            String time = dur > 0 && dur < 655340 ? String.format("%02d:%02d", totalSec / 60, totalSec % 60) : "**:**";
            this.entries.add(new PotionEntry((String)name, time));
        }
        TextRenderer f = mc.textRenderer;
        int maxW = f.getWidth("Effects") + 14;
        for (PotionEntry en : this.entries) {
            maxW = Math.max(maxW, f.getWidth(en.name) + 14 + f.getWidth(en.time) + 14);
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
    public void renderText(DrawContext g, float alpha) {
        if (this.entries.isEmpty()) {
            return;
        }
        int ix = (int)this.x;
        int iy = (int)this.y;
        int bgAlpha = (int)(170.0f * alpha);
        g.fill(ix, iy, ix + this.width, iy + this.height, bgAlpha << 24 | 0x140A28);
        int sepAlpha = (int)(30.0f * alpha);
        g.fill(ix, iy + 18, ix + this.width, iy + 18 + 1, sepAlpha << 24 | 0xB464FF);
        for (int i = 1; i < this.entries.size(); ++i) {
            int ry = iy + 18 + 4 + i * 18;
            int rowSepAlpha = (int)(18.0f * alpha);
            g.fill(ix, ry, ix + this.width, ry + 1, rowSepAlpha << 24 | 0xB464FF);
        }
        TextRenderer f = MinecraftClient.getInstance().textRenderer;
        g.drawText(f, "Effects", ix + 7, iy + 5, PotionsWidget.applyA(-1, alpha), false);
        int rowStart = iy + 18 + 4;
        for (int i = 0; i < this.entries.size(); ++i) {
            PotionEntry e = this.entries.get(i);
            int ry = rowStart + i * 18 + 5;
            g.drawText(f, e.name, ix + 7, ry, PotionsWidget.applyA(-1, alpha), false);
            g.drawText(f, e.time, ix + this.width - 7 - f.getWidth(e.time), ry, PotionsWidget.applyA(-1073741825, alpha), false);
        }
    }

    private static int applyA(int c, float a) {
        return (int)((float)(c >> 24 & 0xFF) * a) << 24 | c & 0xFFFFFF;
    }

        private record PotionEntry(String name, String time) {
    }
}

