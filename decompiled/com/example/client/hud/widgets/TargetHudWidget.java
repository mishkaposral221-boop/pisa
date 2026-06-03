/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1297
 *  net.minecraft.class_1304
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_640
 *  net.minecraft.class_7532
 *  net.minecraft.class_8685
 */
package com.example.client.hud.widgets;

import com.example.client.hud.HudWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_640;
import net.minecraft.class_7532;
import net.minecraft.class_8685;

@Environment(value=EnvType.CLIENT)
public class TargetHudWidget
extends HudWidget {
    private class_1309 target = null;
    private int showTicks = 0;
    private static final int FADE_TICKS = 60;

    public TargetHudWidget(float x, float y) {
        super(x, y, 120, 42);
    }

    public void setTarget(class_1309 entity) {
        this.target = entity;
        this.showTicks = 60;
    }

    @Override
    public void tick() {
        class_1297 class_12972;
        if (this.showTicks > 0) {
            --this.showTicks;
        }
        if (this.showTicks <= 0) {
            this.target = null;
        }
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 != null && (class_12972 = mc.field_1692) instanceof class_1309) {
            class_1309 living = (class_1309)class_12972;
            if (mc.field_1724.method_7261(0.0f) < 0.9f && living != mc.field_1724) {
                this.setTarget(living);
            }
        }
        if (this.target != null && (!this.target.method_5805() || this.target.method_31481())) {
            this.target = null;
            this.showTicks = 0;
        }
        this.visible = this.target != null && this.showTicks > 0;
    }

    @Override
    public void renderBackground(float alpha) {
    }

    @Override
    public void renderText(class_332 g, float alpha) {
        Object name;
        class_1309 class_13092;
        float absorb;
        int b;
        int gr;
        int r;
        if (this.target == null) {
            return;
        }
        float a = alpha * Math.min(1.0f, (float)this.showTicks / 10.0f);
        class_327 font = class_310.method_1551().field_1772;
        int ix = (int)this.x;
        int iy = (int)this.y;
        int iAlpha = (int)(180.0f * a);
        g.method_25294(ix, iy, ix + this.width, iy + this.height, iAlpha << 24 | 0x140A28);
        int barX = ix + 4;
        int barY = iy + this.height - 8;
        int barW = this.width - 8;
        int barH = 4;
        g.method_25294(barX, barY, barX + barW, barY + barH, (int)(180.0f * a) << 24 | 0x14141E);
        float hp = this.target.method_6032();
        float maxHp = this.target.method_6063();
        float pct = Math.min(1.0f, hp / maxHp);
        int fillW = (int)((float)barW * pct);
        if (pct > 0.5f) {
            r = (int)(255.0f * (1.0f - pct) * 2.0f);
            gr = 255;
            b = 50;
        } else if (pct > 0.25f) {
            r = 255;
            gr = (int)(255.0f * (pct - 0.25f) * 4.0f);
            b = 50;
        } else {
            r = 255;
            gr = 50;
            b = 50;
        }
        if (fillW > 0) {
            g.method_25294(barX, barY, barX + fillW, barY + barH, (int)(220.0f * a) << 24 | r << 16 | gr << 8 | b);
        }
        if ((absorb = this.target.method_6067()) > 0.0f) {
            int absW = (int)((float)barW * Math.min(1.0f, absorb / maxHp));
            g.method_25294(barX, barY - 2, barX + absW, barY, (int)(180.0f * a) << 24 | 0xFFDC32);
        }
        if ((class_13092 = this.target) instanceof class_1657) {
            class_640 info;
            class_1657 player = (class_1657)class_13092;
            class_310 mc = class_310.method_1551();
            class_640 class_6402 = info = mc.method_1562() != null ? mc.method_1562().method_2871(player.method_5667()) : null;
            if (info != null) {
                class_7532.method_52722((class_332)g, (class_8685)info.method_52810(), (int)(ix + 4), (int)(iy + 4), (int)19);
            }
        }
        if (((String)(name = this.target.method_5477().getString())).length() > 14) {
            name = ((String)name).substring(0, 14) + "..";
        }
        g.method_51433(font, (String)name, ix + 26, iy + 3, TargetHudWidget.applyA(-1, a), true);
        String hpText = String.format("%.1f", Float.valueOf(this.target.method_6032())) + " HP";
        int hpColor = this.target.method_6032() > this.target.method_6063() * 0.5f ? -11141291 : -43691;
        g.method_51433(font, hpText, ix + this.width - 4 - font.method_1727(hpText), iy + 3, TargetHudWidget.applyA(hpColor, a), true);
        class_1309 class_13093 = this.target;
        if (class_13093 instanceof class_1657) {
            class_1657 player = (class_1657)class_13093;
            class_1799[] armor = new class_1799[]{player.method_6118(class_1304.field_6169), player.method_6118(class_1304.field_6174), player.method_6118(class_1304.field_6172), player.method_6118(class_1304.field_6166), player.method_6118(class_1304.field_6173), player.method_6118(class_1304.field_6171)};
            int ax = ix + 4;
            int ay = iy + 16;
            for (class_1799 stack : armor) {
                if (stack.method_7960()) continue;
                g.method_51427(stack, ax, ay);
                ax += 18;
            }
        }
    }

    private static int applyA(int c, float a) {
        return (int)((float)(c >> 24 & 0xFF) * a) << 24 | c & 0xFFFFFF;
    }
}

