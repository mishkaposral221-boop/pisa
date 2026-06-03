/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1041
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_3675
 *  net.minecraft.class_437
 */
package com.example.client.menu;

import com.example.client.menu.ColorPickerRenderer;
import com.example.client.menu.MenuRenderer;
import com.example.client.menu.MenuTextRenderer;
import com.example.client.menu.ModMenu;
import com.example.client.menu.animation.AnimationController;
import com.example.client.menu.data.MenuData;
import com.example.client.menu.input.MenuInputHandler;
import com.example.client.menu.layout.MenuLayout;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1041;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3675;
import net.minecraft.class_437;

@Environment(value=EnvType.CLIENT)
public class MenuScreen
extends class_437 {
    private final ModMenu menu;
    private final MenuLayout layout;
    private final AnimationController anim;
    private final MenuInputHandler input;
    private final MenuRenderer renderer;
    private final MenuTextRenderer textRenderer;
    private final ColorPickerRenderer colorPicker;

    protected MenuScreen(ModMenu menu, MenuLayout layout, AnimationController anim, MenuInputHandler input, MenuRenderer renderer, MenuTextRenderer textRenderer, ColorPickerRenderer colorPicker) {
        super((class_2561)class_2561.method_43470((String)"ModMenu"));
        this.menu = menu;
        this.layout = layout;
        this.anim = anim;
        this.input = input;
        this.renderer = renderer;
        this.textRenderer = textRenderer;
        this.colorPicker = colorPicker;
    }

    public boolean method_25422() {
        return true;
    }

    public void method_25419() {
        this.menu.setOpen(false);
        super.method_25419();
    }

    public void method_25420(class_332 g, int mouseX, int mouseY, float partialTick) {
    }

    public boolean method_25402(class_11909 event, boolean doubleClick) {
        int btn = event.method_74245();
        int baseX = this.renderer.getMenuX();
        int baseY = this.renderer.getMenuY();
        if (btn == 0 && this.colorPicker.handleClick(event.comp_4798(), event.comp_4799())) {
            return true;
        }
        if (btn == 0) {
            this.input.handleLeftClick(event.comp_4798(), event.comp_4799(), baseX, baseY);
        } else if (btn == 1) {
            this.input.handleRightClick(event.comp_4798(), event.comp_4799(), baseX, baseY);
        } else if (btn == 2) {
            this.input.handleMiddleClick(event.comp_4798(), event.comp_4799(), baseX, baseY);
        }
        return true;
    }

    public void method_25394(class_332 g, int mouseX, int mouseY, float partialTick) {
        super.method_25394(g, mouseX, mouseY, partialTick);
        double mx = this.getGuiMouseX();
        double my = this.getGuiMouseY();
        int baseX = this.renderer.getMenuX();
        int baseY = this.renderer.getMenuY();
        this.colorPicker.handleDrag(mx, my);
        this.input.handleDrag(mx, my, baseX, baseY);
    }

    public void method_25393() {
    }

    private double getGuiMouseX() {
        class_310 client = class_310.method_1551();
        return client.field_1729.method_1603() * (double)client.method_22683().method_4486() / (double)client.method_22683().method_4480();
    }

    private double getGuiMouseY() {
        class_310 client = class_310.method_1551();
        return client.field_1729.method_1604() * (double)client.method_22683().method_4502() / (double)client.method_22683().method_4507();
    }

    public boolean method_25401(double mouseX, double mouseY, double hDelta, double vDelta) {
        boolean ctrl;
        boolean bl = ctrl = class_3675.method_15987((class_1041)class_310.method_1551().method_22683(), (int)341) || class_3675.method_15987((class_1041)class_310.method_1551().method_22683(), (int)345);
        if (vDelta != 0.0 && ctrl) {
            this.layout.adjustScale((float)vDelta * 0.05f);
        } else if (vDelta != 0.0) {
            double mx = this.getGuiMouseX();
            int baseX = this.renderer.getMenuX();
            int cw = this.layout.CW();
            int gap = this.layout.GAP();
            for (int col = 0; col < MenuData.CATEGORIES.length; ++col) {
                int colX = baseX + col * (cw + gap);
                if (!(mx >= (double)colX) || !(mx <= (double)(colX + cw))) continue;
                this.layout.scroll(col, (float)vDelta * (float)this.layout.IH());
                break;
            }
        }
        return true;
    }

    public boolean method_25406(class_11909 event) {
        this.colorPicker.releaseDrag();
        this.input.releaseDrag();
        return true;
    }

    public boolean method_25421() {
        return false;
    }

    public boolean method_25404(class_11908 event) {
        if (this.input.isListening()) {
            int keyCode = event.comp_4795();
            if (keyCode == 256 || keyCode == 261) {
                this.input.cancelListening();
            } else {
                this.input.setKeybind(keyCode);
            }
            return true;
        }
        return super.method_25404(event);
    }
}

