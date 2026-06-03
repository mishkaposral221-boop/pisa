/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package com.example.client.menu.layout;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class MenuLayout {
    public float menuScale = 1.0f;
    public static final float MIN_SCALE = 0.5f;
    public static final float MAX_SCALE = 1.6f;
    public static final int COLS = 5;
    public static final int BASE_COL_W = 140;
    public static final int BASE_COL_H = 280;
    public static final float BASE_RADIUS = 6.0f;
    public static final int BASE_PAD = 10;
    public static final int BASE_HEADER_H = 26;
    public static final int BASE_ITEM_H = 18;
    public static final int BASE_GAP = 6;
    public static final int BASE_EXPAND_H = 110;
    public static final int BASE_ACCENT_BAR_W = 0;
    public static final int BASE_TOGGLE_W = 13;
    public static final int BASE_TOGGLE_H = 8;
    public static final int BASE_TOGGLE_R = 3;
    public static final int BASE_TOGGLE_CIRCLE_R = 3;
    public static final int BASE_TOGGLE_PAD = 6;
    public static final float BASE_SEP_H = 0.5f;
    public final float[] scrollOffset = new float[5];

    public int CW() {
        return (int)(140.0f * this.menuScale);
    }

    public int CH() {
        return (int)(280.0f * this.menuScale);
    }

    public float R() {
        return 6.0f * this.menuScale;
    }

    public int P() {
        return (int)(10.0f * this.menuScale);
    }

    public int HH() {
        return (int)(26.0f * this.menuScale);
    }

    public int IH() {
        return (int)(18.0f * this.menuScale);
    }

    public int GAP() {
        return (int)(6.0f * this.menuScale);
    }

    public int EXPH() {
        return (int)(110.0f * this.menuScale);
    }

    public int expandH(int settingsCount) {
        if (settingsCount <= 0) {
            return 0;
        }
        int rowH = (int)(18.0f * this.menuScale);
        int topPad = (int)(3.0f * this.menuScale);
        int botPad = (int)(3.0f * this.menuScale);
        return topPad + settingsCount * rowH + botPad;
    }

    public float SEP() {
        return 0.5f * this.menuScale;
    }

    public int TOTAL_W() {
        return 5 * this.CW() + 4 * this.GAP();
    }

    public int ABW() {
        return (int)(0.0f * this.menuScale);
    }

    public int TW() {
        return (int)(13.0f * this.menuScale);
    }

    public int TH() {
        return (int)(8.0f * this.menuScale);
    }

    public float TR() {
        return 3.0f * this.menuScale;
    }

    public int TCR() {
        return (int)(3.0f * this.menuScale);
    }

    public int TPAD() {
        return (int)(6.0f * this.menuScale);
    }

    public void scroll(int col, float delta) {
        if (col < 0 || col >= 5) {
            return;
        }
        int n = col;
        this.scrollOffset[n] = this.scrollOffset[n] + delta;
        if (this.scrollOffset[col] > 0.0f) {
            this.scrollOffset[col] = 0.0f;
        }
    }

    public void clampScroll(int col, int contentH) {
        int maxVisible = this.CH() - this.HH();
        float minScroll = Math.min(0, maxVisible - contentH);
        if (this.scrollOffset[col] < minScroll) {
            this.scrollOffset[col] = minScroll;
        }
        if (this.scrollOffset[col] > 0.0f) {
            this.scrollOffset[col] = 0.0f;
        }
    }

    public void adjustScale(float delta) {
        this.menuScale = Math.max(0.5f, Math.min(1.6f, this.menuScale + delta));
    }
}

