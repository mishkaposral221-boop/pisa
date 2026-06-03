/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.ferra13671.cometrenderer.minecraft.RenderColor
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package com.example.client.menu.layout;

import com.ferra13671.cometrenderer.minecraft.RenderColor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class MenuColors {
    private static final int BG_TOP_R = 95;
    private static final int BG_TOP_G = 30;
    private static final int BG_TOP_B = 190;
    private static final float BG_TOP_ALPHA = 0.72f;
    private static final int BG_BOT_R = 50;
    private static final int BG_BOT_G = 15;
    private static final int BG_BOT_B = 140;
    private static final float BG_BOT_ALPHA = 0.8f;
    private static final int ACC_R = 151;
    private static final int ACC_G = 71;
    private static final int ACC_B = 255;
    private static final int SEP_R = 255;
    private static final int SEP_G = 255;
    private static final int SEP_B = 255;
    private static final float SEP_ALPHA = 0.08f;
    private static final int ADD_R = 24;
    private static final int ADD_G = 24;
    private static final int ADD_B = 27;
    public static final RenderColor SEPARATOR = RenderColor.of((int)255, (int)255, (int)255, (int)20);
    public static final RenderColor COLUMN_SEP = RenderColor.of((int)0, (int)0, (int)0, (int)0);
    public static final int TEXT_DISABLED = -1073741825;
    public static final int TEXT_HOVER = -1;
    public static final int TEXT_HEADER = -1;
    public static final int TEXT_SETTING = -1073741825;
    public static final int ARROW_DISABLED = 0x4DFFFFFF;

    public static RenderColor getOverlay() {
        return RenderColor.of((int)0, (int)0, (int)0, (int)0);
    }

    public static RenderColor getColumnTop() {
        return RenderColor.of((int)95, (int)30, (int)190, (int)183);
    }

    public static RenderColor getColumnBottom() {
        return RenderColor.of((int)50, (int)15, (int)140, (int)204);
    }

    public static RenderColor getAccentColor() {
        return RenderColor.of((int)151, (int)71, (int)255, (int)255);
    }

    public static RenderColor getAccentBar() {
        return MenuColors.getAccentColor();
    }

    public static RenderColor getToggleOnBg() {
        return RenderColor.of((int)151, (int)71, (int)255, (int)255);
    }

    public static RenderColor getToggleOffBg() {
        return RenderColor.of((int)60, (int)25, (int)110, (int)200);
    }

    public static RenderColor getToggleOnCircle() {
        return RenderColor.of((int)255, (int)255, (int)255, (int)255);
    }

    public static RenderColor getToggleOffCircle() {
        return RenderColor.of((int)255, (int)255, (int)255, (int)191);
    }

    public static RenderColor getExpandBg() {
        return RenderColor.of((int)0, (int)0, (int)0, (int)0);
    }

    public static RenderColor getHeaderSeparator() {
        return RenderColor.of((int)180, (int)100, (int)255, (int)30);
    }

    public static RenderColor getSliderTrack() {
        return RenderColor.of((int)60, (int)25, (int)110, (int)178);
    }

    public static RenderColor getSliderFill() {
        return MenuColors.getAccentColor();
    }

    public static RenderColor getSliderThumb() {
        return RenderColor.of((int)255, (int)255, (int)255, (int)255);
    }

    public static int getTextEnabled() {
        return -1;
    }

    public static int getArrowEnabled() {
        return MenuColors.getTextEnabled();
    }

    public static RenderColor getHudBackground() {
        return RenderColor.of((int)50, (int)15, (int)140, (int)165);
    }

    public static RenderColor getHudSeparator() {
        return RenderColor.of((int)255, (int)255, (int)255, (int)7);
    }
}

