/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.ferra13671.cometrenderer.minecraft.RenderColor
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package com.example.client.menu.util;

import com.ferra13671.cometrenderer.minecraft.RenderColor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class ColorUtil {
    public static int blendColor(int c1, int c2, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        int a1 = c1 >> 24 & 0xFF;
        int r1 = c1 >> 16 & 0xFF;
        int g1 = c1 >> 8 & 0xFF;
        int b1 = c1 & 0xFF;
        int a2 = c2 >> 24 & 0xFF;
        int r2 = c2 >> 16 & 0xFF;
        int g2 = c2 >> 8 & 0xFF;
        int b2 = c2 & 0xFF;
        int a = (int)((float)a1 + (float)(a2 - a1) * t);
        int r = (int)((float)r1 + (float)(r2 - r1) * t);
        int g = (int)((float)g1 + (float)(g2 - g1) * t);
        int b = (int)((float)b1 + (float)(b2 - b1) * t);
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static int applyAlpha(int argb, float alphaMult) {
        int a = argb >> 24 & 0xFF;
        int newA = Math.max(0, Math.min(255, (int)((float)a * alphaMult)));
        return newA << 24 | argb & 0xFFFFFF;
    }

    public static RenderColor mulAlpha(RenderColor color, float alphaMult) {
        float[] c = color.getColor();
        return RenderColor.of((float)c[0], (float)c[1], (float)c[2], (float)(c[3] * alphaMult));
    }

    public static RenderColor blendRenderColor(RenderColor a, RenderColor b, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        float[] ca = a.getColor();
        float[] cb = b.getColor();
        return RenderColor.of((float)(ca[0] + (cb[0] - ca[0]) * t), (float)(ca[1] + (cb[1] - ca[1]) * t), (float)(ca[2] + (cb[2] - ca[2]) * t), (float)(ca[3] + (cb[3] - ca[3]) * t));
    }
}

