package com.example.client.menu;

import com.example.client.menu.ColorPickerRenderer;
import com.example.client.menu.MenuRenderer;
import com.example.client.menu.MenuTextRenderer;
import com.example.client.menu.ModMenu;
import com.example.client.menu.animation.AnimationController;
import com.example.client.menu.data.MenuData;
import com.example.client.menu.input.MenuInputHandler;
import com.example.client.menu.layout.MenuLayout;
import net.minecraft.client.util.Window;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.Click;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gui.screen.Screen;

public class MenuScreen
extends Screen {
    private final ModMenu menu;
    private final MenuLayout layout;
    private final AnimationController anim;
    private final MenuInputHandler input;
    private final MenuRenderer renderer;
    private final MenuTextRenderer textRenderer;
    private final ColorPickerRenderer colorPicker;

    protected MenuScreen(ModMenu menu, MenuLayout layout, AnimationController anim, MenuInputHandler input, MenuRenderer renderer, MenuTextRenderer textRenderer, ColorPickerRenderer colorPicker) {
        super((Text)Text.literal((String)"ModMenu"));
        this.menu = menu;
        this.layout = layout;
        this.anim = anim;
        this.input = input;
        this.renderer = renderer;
        this.textRenderer = textRenderer;
        this.colorPicker = colorPicker;
    }

    public boolean shouldCloseOnEsc() {
        return true;
    }

    public void close() {
        this.menu.setOpen(false);
        super.close();
    }

    public void renderBackground(DrawContext g, int mouseX, int mouseY, float partialTick) {
    }

    public boolean mouseClicked(Click event, boolean doubleClick) {
        int btn = event.button();
        int baseX = this.renderer.getMenuX();
        int baseY = this.renderer.getMenuY();
        if (btn == 0 && this.colorPicker.handleClick(event.x(), event.y())) {
            return true;
        }
        if (btn == 0) {
            this.input.handleLeftClick(event.x(), event.y(), baseX, baseY);
        } else if (btn == 1) {
            this.input.handleRightClick(event.x(), event.y(), baseX, baseY);
        } else if (btn == 2) {
            this.input.handleMiddleClick(event.x(), event.y(), baseX, baseY);
        }
        return true;
    }

    public void render(DrawContext g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        double mx = this.getGuiMouseX();
        double my = this.getGuiMouseY();
        int baseX = this.renderer.getMenuX();
        int baseY = this.renderer.getMenuY();
        this.colorPicker.handleDrag(mx, my);
        this.input.handleDrag(mx, my, baseX, baseY);
    }

    public void tick() {
    }

    private double getGuiMouseX() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.mouse.getX() * (double)client.getWindow().getScaledWidth() / (double)client.getWindow().getWidth();
    }

    private double getGuiMouseY() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.mouse.getY() * (double)client.getWindow().getScaledHeight() / (double)client.getWindow().getHeight();
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double hDelta, double vDelta) {
        boolean ctrl;
        boolean bl = ctrl = InputUtil.isKeyPressed((Window)MinecraftClient.getInstance().getWindow(), (int)341) || InputUtil.isKeyPressed((Window)MinecraftClient.getInstance().getWindow(), (int)345);
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

    public boolean mouseReleased(Click event) {
        this.colorPicker.releaseDrag();
        this.input.releaseDrag();
        return true;
    }

    public boolean shouldPause() {
        return false;
    }

    public boolean keyPressed(KeyInput event) {
        if (this.input.isListening()) {
            int keyCode = event.key();
            if (keyCode == 256 || keyCode == 261) {
                this.input.cancelListening();
            } else {
                this.input.setKeybind(keyCode);
            }
            return true;
        }
        return super.keyPressed(event);
    }
}

