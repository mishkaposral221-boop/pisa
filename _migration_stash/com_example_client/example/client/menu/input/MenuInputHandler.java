package com.example.client.menu.input;

import com.example.client.menu.animation.AnimationController;
import com.example.client.menu.data.MenuData;
import com.example.client.menu.data.ModModule;
import com.example.client.menu.data.Setting;
import com.example.client.menu.layout.MenuLayout;
import net.minecraft.client.MinecraftClient;

public class MenuInputHandler {
    private final MenuLayout layout;
    private final AnimationController anim;
    public int hoveredCol = -1;
    public int hoveredMod = -1;
    private Setting draggingSetting = null;
    private int draggingCol = -1;
    public int listeningCol = -1;
    public int listeningMod = -1;

    public MenuInputHandler(MenuLayout layout, AnimationController anim) {
        this.layout = layout;
        this.anim = anim;
    }

    public void handleLeftClick(double mx, double my, int baseX, int baseY) {
        int cw = this.layout.CW();
        int p = this.layout.P();
        int hh = this.layout.HH();
        int ih = this.layout.IH();
        int gap = this.layout.GAP();
        float s = this.layout.menuScale;
        int bodyTop = (int)((float)(baseY + hh) + 4.0f * s);
        for (int col = 0; col < MenuData.CATEGORIES.length; ++col) {
            int colX = baseX + col * (cw + gap);
            if (mx < (double)colX || mx > (double)(colX + cw)) continue;
            int scrollY = (int)this.layout.scrollOffset[col];
            int yOffset = 0;
            for (int i = 0; i < MenuData.CATEGORIES[col].modules.length; ++i) {
                ModModule mod = MenuData.CATEGORIES[col].modules[i];
                int itemH = ih + (int)((float)this.layout.expandH(mod.settings.length) * mod.expandAnim);
                int itemY = bodyTop + yOffset + scrollY;
                if (mx >= (double)(colX + p) && mx <= (double)(colX + cw - p) && my >= (double)itemY && my <= (double)(itemY + ih)) {
                    mod.enabled = !mod.enabled;
                    return;
                }
                if (mod.expandAnim > 0.05f && my >= (double)(itemY + ih) && my <= (double)(itemY + itemH)) {
                    int expH = (int)((float)this.layout.expandH(mod.settings.length) * mod.expandAnim);
                    int rowH = (int)(18.0f * s);
                    int sliderW = cw - p * 2;
                    int tw = this.layout.TW();
                    int th = this.layout.TH();
                    for (int si = 0; si < mod.settings.length; ++si) {
                        Setting set = mod.settings[si];
                        int setY = itemY + ih + (int)(3.0f * s) + si * rowH;
                        if (setY + rowH > itemY + ih + expH) break;
                        int setX = colX + p;
                        if (set.type == Setting.Type.SLIDER) {
                            if (!(mx >= (double)setX) || !(mx <= (double)(setX + sliderW)) || !(my >= (double)setY) || !(my <= (double)(setY + rowH))) continue;
                            float t = Math.max(0.0f, Math.min(1.0f, (float)((mx - (double)setX) / (double)sliderW)));
                            set.setNormalized(t);
                            this.draggingSetting = set;
                            this.draggingCol = col;
                            return;
                        }
                        if (set.type != Setting.Type.TOGGLE || !(mx >= (double)setX) || !(mx <= (double)(setX + sliderW)) || !(my >= (double)setY) || !(my <= (double)(setY + rowH))) continue;
                        set.boolValue = !set.boolValue;
                        return;
                    }
                }
                yOffset += itemH;
            }
            return;
        }
    }

    public boolean handleDrag(double mx, double my, int baseX, int baseY) {
        if (this.draggingSetting == null || this.draggingSetting.type != Setting.Type.SLIDER || this.draggingCol < 0) {
            return false;
        }
        int cw = this.layout.CW();
        int p = this.layout.P();
        int gap = this.layout.GAP();
        int sliderW = cw - p * 2;
        int colX = baseX + this.draggingCol * (cw + gap);
        int setX = colX + p;
        float t = Math.max(0.0f, Math.min(1.0f, (float)((mx - (double)setX) / (double)sliderW)));
        this.draggingSetting.setNormalized(t);
        return true;
    }

    public void releaseDrag() {
        if (this.draggingSetting != null) {
            this.draggingSetting.dragging = false;
        }
        this.draggingSetting = null;
        this.draggingCol = -1;
    }

    public void handleRightClick(double mx, double my, int baseX, int baseY) {
        int cw = this.layout.CW();
        int p = this.layout.P();
        int hh = this.layout.HH();
        int ih = this.layout.IH();
        int gap = this.layout.GAP();
        float s = this.layout.menuScale;
        int bodyTop = (int)((float)(baseY + hh) + 4.0f * s);
        for (int col = 0; col < MenuData.CATEGORIES.length; ++col) {
            int colX = baseX + col * (cw + gap);
            if (mx < (double)colX || mx > (double)(colX + cw)) continue;
            int scrollY = (int)this.layout.scrollOffset[col];
            int yOffset = 0;
            for (int i = 0; i < MenuData.CATEGORIES[col].modules.length; ++i) {
                ModModule mod = MenuData.CATEGORIES[col].modules[i];
                int itemH = ih + (int)((float)this.layout.expandH(mod.settings.length) * mod.expandAnim);
                int itemY = bodyTop + yOffset + scrollY;
                if (mx >= (double)(colX + p) && mx <= (double)(colX + cw - p) && my >= (double)itemY && my <= (double)(itemY + ih)) {
                    mod.expanded = !mod.expanded;
                    return;
                }
                yOffset += itemH;
            }
            return;
        }
    }

    public void handleMiddleClick(double mx, double my, int baseX, int baseY) {
        int cw = this.layout.CW();
        int p = this.layout.P();
        int hh = this.layout.HH();
        int ih = this.layout.IH();
        int gap = this.layout.GAP();
        float s = this.layout.menuScale;
        int bodyTop = (int)((float)(baseY + hh) + 4.0f * s);
        for (int col = 0; col < MenuData.CATEGORIES.length; ++col) {
            int colX = baseX + col * (cw + gap);
            if (mx < (double)colX || mx > (double)(colX + cw)) continue;
            int scrollY = (int)this.layout.scrollOffset[col];
            int yOffset = 0;
            for (int i = 0; i < MenuData.CATEGORIES[col].modules.length; ++i) {
                ModModule mod = MenuData.CATEGORIES[col].modules[i];
                int itemH = ih + (int)((float)this.layout.expandH(mod.settings.length) * mod.expandAnim);
                int itemY = bodyTop + yOffset + scrollY;
                if (mx >= (double)(colX + p) && mx <= (double)(colX + cw - p) && my >= (double)itemY && my <= (double)(itemY + ih)) {
                    mod.keybind = -2;
                    this.listeningCol = col;
                    this.listeningMod = i;
                    return;
                }
                yOffset += itemH;
            }
            return;
        }
    }

    public boolean isListening() {
        return this.listeningCol >= 0 && this.listeningMod >= 0;
    }

    public void cancelListening() {
        if (this.listeningCol >= 0 && this.listeningMod >= 0) {
            MenuData.CATEGORIES[this.listeningCol].modules[this.listeningMod].keybind = -1;
        }
        this.listeningCol = -1;
        this.listeningMod = -1;
    }

    public void setKeybind(int key) {
        if (this.listeningCol >= 0 && this.listeningMod >= 0) {
            MenuData.CATEGORIES[this.listeningCol].modules[this.listeningMod].keybind = key;
        }
        this.listeningCol = -1;
        this.listeningMod = -1;
    }

    public void updateHover(MinecraftClient client, int baseX, int baseY) {
        double mx = client.mouse.getX() * (double)client.getWindow().getScaledWidth() / (double)client.getWindow().getWidth();
        double my = client.mouse.getY() * (double)client.getWindow().getScaledHeight() / (double)client.getWindow().getHeight();
        int cw = this.layout.CW();
        int p = this.layout.P();
        int hh = this.layout.HH();
        int ih = this.layout.IH();
        int gap = this.layout.GAP();
        this.hoveredCol = -1;
        this.hoveredMod = -1;
        float s = this.layout.menuScale;
        int bodyTop = (int)((float)(baseY + hh) + 4.0f * s);
        block0: for (int col = 0; col < MenuData.CATEGORIES.length; ++col) {
            int colX = baseX + col * (cw + gap);
            if (mx < (double)colX || mx > (double)(colX + cw)) continue;
            this.hoveredCol = col;
            int scrollY = (int)this.layout.scrollOffset[col];
            int yOffset = 0;
            for (int i = 0; i < MenuData.CATEGORIES[col].modules.length; ++i) {
                ModModule mod = MenuData.CATEGORIES[col].modules[i];
                int itemH = ih + (int)((float)this.layout.expandH(mod.settings.length) * mod.expandAnim);
                int itemY = bodyTop + yOffset + scrollY;
                if (mx >= (double)(colX + p) && mx <= (double)(colX + cw - p) && my >= (double)itemY && my <= (double)(itemY + ih)) {
                    this.hoveredMod = i;
                    break block0;
                }
                yOffset += itemH;
            }
            break;
        }
    }
}

