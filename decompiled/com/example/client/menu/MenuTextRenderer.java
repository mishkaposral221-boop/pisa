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
package com.example.client.menu;

import com.example.client.menu.animation.AnimationController;
import com.example.client.menu.data.Category;
import com.example.client.menu.data.MenuData;
import com.example.client.menu.data.ModModule;
import com.example.client.menu.data.Setting;
import com.example.client.menu.input.MenuInputHandler;
import com.example.client.menu.layout.MenuLayout;
import com.example.client.menu.util.ColorUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;

@Environment(value=EnvType.CLIENT)
public class MenuTextRenderer {
    private final MenuLayout layout;
    private final AnimationController anim;
    private final MenuInputHandler input;

    public MenuTextRenderer(MenuLayout layout, AnimationController anim, MenuInputHandler input) {
        this.layout = layout;
        this.anim = anim;
        this.input = input;
    }

    public void render(class_332 guiGraphics, int baseX, int baseY) {
        if (!this.anim.shouldRender()) {
            return;
        }
        class_327 font = class_310.method_1551().field_1772;
        float alpha = this.anim.getAlpha();
        int cw = this.layout.CW();
        int p = this.layout.P();
        int hh = this.layout.HH();
        int ih = this.layout.IH();
        int gap = this.layout.GAP();
        for (int col = 0; col < MenuData.CATEGORIES.length; ++col) {
            int colX = baseX + col * (cw + gap);
            Category cat = MenuData.CATEGORIES[col];
            boolean isHoveredCol = col == this.input.hoveredCol;
            int headerColor = ColorUtil.applyAlpha(-1, alpha);
            int headerTextY = baseY + (hh - 8) / 2;
            guiGraphics.method_51433(font, cat.name, colX + p, headerTextY, headerColor, false);
            int bodyTop = (int)((float)(baseY + hh) + 4.0f * this.layout.menuScale);
            guiGraphics.method_44379(colX, bodyTop, colX + cw, baseY + this.layout.CH());
            int scrollY = (int)this.layout.scrollOffset[col];
            int yOff = 0;
            for (int i = 0; i < cat.modules.length; ++i) {
                String kbName;
                ModModule mod = cat.modules[i];
                int itemH = ih + (int)((float)this.layout.expandH(mod.settings.length) * mod.expandAnim);
                int itemY = bodyTop + yOff + scrollY;
                boolean hovered = isHoveredCol && i == this.input.hoveredMod;
                float glow = mod.toggleAnim;
                int nameColor = mod.enabled ? ColorUtil.applyAlpha(-1, alpha) : (hovered ? ColorUtil.applyAlpha(-2236963, alpha) : ColorUtil.applyAlpha(-4473925, alpha));
                int nameY = itemY + (ih - 8) / 2;
                guiGraphics.method_51433(font, mod.name, colX + p, nameY, nameColor, false);
                if (mod.enabled) {
                    int checkColor = ColorUtil.applyAlpha(-1, alpha);
                    String check = "\u2714";
                    int checkX = colX + cw - p - font.method_1727(check);
                    guiGraphics.method_51433(font, check, checkX, nameY, checkColor, false);
                }
                if (!(kbName = mod.getKeybindName()).isEmpty()) {
                    int kbColor = mod.isListening() ? ColorUtil.applyAlpha(-256, alpha) : ColorUtil.applyAlpha(0x66FFFFFF, alpha);
                    int kbX = colX + cw - p - (mod.enabled ? font.method_1727("\u2714") + (int)(4.0f * this.layout.menuScale) : 0) - font.method_1727(kbName);
                    guiGraphics.method_51433(font, kbName, kbX, nameY, kbColor, false);
                }
                if (mod.expandAnim > 0.05f) {
                    int expH = (int)((float)this.layout.expandH(mod.settings.length) * mod.expandAnim);
                    int rowH = (int)(18.0f * this.layout.menuScale);
                    int sliderW = cw - p * 2;
                    float setAlpha = alpha * mod.expandAnim;
                    for (int si = 0; si < mod.settings.length; ++si) {
                        int textY;
                        Setting set = mod.settings[si];
                        int setY = itemY + ih + (int)(3.0f * this.layout.menuScale) + si * rowH;
                        if (setY + rowH > itemY + ih + expH) break;
                        int setX = colX + p;
                        int setColor = ColorUtil.applyAlpha(-1073741825, setAlpha);
                        int valColor = ColorUtil.applyAlpha(-1, setAlpha);
                        if (set.type == Setting.Type.SLIDER) {
                            textY = setY + 1;
                            guiGraphics.method_51433(font, set.name, setX, textY, setColor, false);
                            String valStr = set.formatValue();
                            guiGraphics.method_51433(font, valStr, setX + sliderW - font.method_1727(valStr), textY, valColor, false);
                            continue;
                        }
                        if (set.type != Setting.Type.TOGGLE) continue;
                        textY = setY + (rowH - 8) / 2;
                        guiGraphics.method_51433(font, set.name, setX, textY, setColor, false);
                    }
                }
                yOff += itemH;
            }
            guiGraphics.method_44380();
        }
    }
}

