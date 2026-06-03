package com.example.client.menu;

import com.example.client.ModMenuInitializer;
import com.example.client.config.ConfigManager;
import com.example.client.menu.ColorPickerRenderer;
import com.example.client.menu.MenuRenderer;
import com.example.client.menu.MenuScreen;
import com.example.client.menu.MenuTextRenderer;
import com.example.client.menu.animation.AnimationController;
import com.example.client.menu.data.Category;
import com.example.client.menu.data.MenuData;
import com.example.client.menu.data.ModModule;
import com.example.client.menu.input.MenuInputHandler;
import com.example.client.menu.layout.MenuLayout;
import com.example.client.module.AimAssist;
import com.example.client.module.AspectRatio;
import com.example.client.module.AutoSprint;
import com.example.client.module.Fullbright;
import com.example.client.module.GlowESP;
import com.example.client.module.Nametags;
import com.example.client.module.NoRender;
import com.example.client.module.Prediction;
import com.example.client.module.Triggerbot;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.util.Identifier;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gui.screen.Screen;

public class ModMenu {
    private boolean open = false;
    private KeyBinding menuKey;
    private static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(Identifier.of("modid", "menu"));
    private final MenuLayout layout = new MenuLayout();
    private final AnimationController anim = new AnimationController();
    private final MenuInputHandler input = new MenuInputHandler(this.layout, this.anim);
    private final MenuRenderer renderer = new MenuRenderer(this.layout, this.anim);
    private final MenuTextRenderer textRenderer = new MenuTextRenderer(this.layout, this.anim, this.input);
    private final ColorPickerRenderer colorPicker = new ColorPickerRenderer(this.layout, this.renderer);
    private MenuScreen menuScreen;
    private boolean modulesLinked = false;

    public void registerKeybind() {
        this.menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.modid.menu", InputUtil.Type.KEYSYM, 344, CATEGORY));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!this.modulesLinked) {
                this.linkModules();
                this.modulesLinked = true;
            }
            while (this.menuKey.wasPressed()) {
                boolean bl = this.open = !this.open;
                if (this.open) {
                    if (this.menuScreen == null) {
                        this.menuScreen = new MenuScreen(this, this.layout, this.anim, this.input, this.renderer, this.textRenderer, this.colorPicker);
                    }
                    client.setScreen((Screen)this.menuScreen);
                    this.anim.resetAnim();
                    continue;
                }
                client.setScreen(null);
                ConfigManager.save();
            }
            if (this.open && client.currentScreen == null) {
                this.open = false;
            }
            if (this.open) {
                int baseX = this.renderer.getMenuX();
                int baseY = this.renderer.getMenuY();
                this.input.updateHover(client, baseX, baseY);
                this.anim.updateModuleAnimations();
            }
        });
    }

    public boolean isOpen() {
        return this.open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public ColorPickerRenderer getColorPicker() {
        return this.colorPicker;
    }

    public void renderShapes() {
        this.renderer.renderShapes();
        this.colorPicker.renderShapes(this.anim.getAlpha());
    }

    public void renderText(DrawContext guiGraphics) {
        if (!this.anim.shouldRender()) {
            return;
        }
        this.textRenderer.render(guiGraphics, this.renderer.getMenuX(), this.renderer.getMenuY());
        this.colorPicker.renderText(guiGraphics, this.anim.getAlpha());
    }

    private void linkModules() {
        AimAssist aim = ModMenuInitializer.AIM_ASSIST;
        NoRender noRender = ModMenuInitializer.NO_RENDER;
        Triggerbot triggerbot = ModMenuInitializer.TRIGGERBOT;
        AutoSprint autoSprint = ModMenuInitializer.AUTO_SPRINT;
        Fullbright fullbright = ModMenuInitializer.FULLBRIGHT;
        AspectRatio aspectRatio = ModMenuInitializer.ASPECT_RATIO;
        GlowESP glowESP = ModMenuInitializer.GLOW_ESP;
        Nametags nametags = ModMenuInitializer.NAMETAGS;
        Prediction prediction = ModMenuInitializer.PREDICTION;
        for (Category cat : MenuData.CATEGORIES) {
            for (ModModule mod : cat.modules) {
                if ("AimAssist".equals(mod.name)) {
                    aim.setModuleRef(mod);
                }
                if ("NoRender".equals(mod.name)) {
                    noRender.setModuleRef(mod);
                }
                if ("Triggerbot".equals(mod.name)) {
                    triggerbot.setModuleRef(mod);
                }
                if ("AutoSprint".equals(mod.name)) {
                    autoSprint.setModuleRef(mod);
                }
                if ("Fullbright".equals(mod.name)) {
                    fullbright.setModuleRef(mod);
                }
                if ("AspectRatio".equals(mod.name)) {
                    aspectRatio.setModuleRef(mod);
                }
                if ("GlowESP".equals(mod.name)) {
                    glowESP.setModuleRef(mod);
                }
                if ("Nametags".equals(mod.name)) {
                    nametags.setModuleRef(mod);
                }
                if ("Prediction".equals(mod.name)) {
                    prediction.setModuleRef(mod);
                }
                if (!"AutoSwap".equals(mod.name)) continue;
                ModMenuInitializer.AUTO_SWAP.setModuleRef(mod);
            }
        }
    }
}

