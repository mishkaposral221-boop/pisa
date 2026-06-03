/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.ferra13671.cometrenderer.minecraft.CRMInstance
 *  com.ferra13671.cometrenderer.minecraft.event.AfterInitializeCallback
 *  com.ferra13671.cometrenderer.minecraft.event.RenderHudCallback
 *  com.ferra13671.cometrenderer.minecraft.event.RenderWorldCallback
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
 *  net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
 *  net.minecraft.class_1041
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_3675
 */
package com.example.client;

import com.example.client.config.ConfigManager;
import com.example.client.hud.HudManager;
import com.example.client.hud.widgets.ActiveModsWidget;
import com.example.client.hud.widgets.HotkeysWidget;
import com.example.client.hud.widgets.InfoBarWidget;
import com.example.client.hud.widgets.PotionsWidget;
import com.example.client.hud.widgets.TargetHudWidget;
import com.example.client.hud.widgets.TopBarWidget;
import com.example.client.menu.ModMenu;
import com.example.client.menu.data.Category;
import com.example.client.menu.data.MenuData;
import com.example.client.menu.data.ModModule;
import com.example.client.module.AimAssist;
import com.example.client.module.AspectRatio;
import com.example.client.module.AutoSprint;
import com.example.client.module.AutoSwap;
import com.example.client.module.Fullbright;
import com.example.client.module.GlowESP;
import com.example.client.module.Nametags;
import com.example.client.module.NoRender;
import com.example.client.module.Prediction;
import com.example.client.module.Triggerbot;
import com.ferra13671.cometrenderer.minecraft.CRMInstance;
import com.ferra13671.cometrenderer.minecraft.event.AfterInitializeCallback;
import com.ferra13671.cometrenderer.minecraft.event.RenderHudCallback;
import com.ferra13671.cometrenderer.minecraft.event.RenderWorldCallback;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.class_1041;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3675;

@Environment(value=EnvType.CLIENT)
public class ModMenuInitializer
implements PreLaunchEntrypoint {
    public static CRMInstance crmInstance;
    public static final ModMenu MENU;
    public static final AimAssist AIM_ASSIST;
    public static final NoRender NO_RENDER;
    public static final Triggerbot TRIGGERBOT;
    public static final AutoSprint AUTO_SPRINT;
    public static final Fullbright FULLBRIGHT;
    public static final AspectRatio ASPECT_RATIO;
    public static final GlowESP GLOW_ESP;
    public static final Nametags NAMETAGS;
    public static final Prediction PREDICTION;
    public static final AutoSwap AUTO_SWAP;
    public static final HudManager HUD;
    public static final TargetHudWidget TARGET_HUD;
    private static int saveCounter;

    public void onPreLaunch() {
        AfterInitializeCallback.EVENT.register(() -> {
            crmInstance = new CRMInstance(() -> class_310.method_1551().method_22683().method_4495());
            ConfigManager.load();
        });
        RenderHudCallback.EVENT.register(() -> {
            try {
                crmInstance.setupUIMatrix();
                if (MENU.isOpen()) {
                    MENU.renderShapes();
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                crmInstance.restoreUIMatrix();
            }
            catch (Exception exception) {
                // empty catch block
            }
        });
        HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
            if (MENU.isOpen()) {
                MENU.renderText(guiGraphics);
            }
            AIM_ASSIST.getEngine().onFrame(class_310.method_1551(), 1.0f);
            NAMETAGS.render(guiGraphics, deltaTracker.method_60637(false));
            if (AIM_ASSIST.isEnabled()) {
                ModMenuInitializer.renderFovCircle(guiGraphics, AIM_ASSIST.fov());
            }
            if (!MENU.isOpen() && class_310.method_1551().field_1724 != null) {
                HUD.renderText(guiGraphics, 1.0f);
            }
        });
        ClientTickEvents.START_CLIENT_TICK.register(client -> TRIGGERBOT.tick(client));
        RenderWorldCallback.EVENT.register(() -> {
            try {
                GLOW_ESP.renderWorld();
                PREDICTION.renderWorld();
            }
            catch (Exception exception) {
                // empty catch block
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            class_1297 patt0$temp;
            AUTO_SPRINT.tick(client);
            FULLBRIGHT.tick(client);
            if (client.field_1724 != null && (patt0$temp = client.field_1692) instanceof class_1309) {
                class_1309 living = (class_1309)patt0$temp;
                if (client.field_1724.method_7261(0.0f) < 0.5f && living != client.field_1724) {
                    TARGET_HUD.setTarget(living);
                }
            }
            ModMenuInitializer.processKeybinds(client);
            HUD.tick();
            if (++saveCounter >= 100) {
                saveCounter = 0;
                ConfigManager.save();
            }
        });
        ModMenuInitializer.initHud();
    }

    private static void initHud() {
        HUD.addWidget(new TopBarWidget(2.0f, 2.0f));
        HUD.addWidget(new InfoBarWidget(2.0f, 18.0f));
        HUD.addWidget(new PotionsWidget(2.0f, 100.0f));
        HUD.addWidget(new HotkeysWidget(2.0f, 160.0f));
        HUD.addWidget(TARGET_HUD);
        HUD.addWidget(new ActiveModsWidget());
    }

    private static void renderFovCircle(class_332 g, float fovDeg) {
        class_310 client = class_310.method_1551();
        if (client.field_1724 == null) {
            return;
        }
        int sw = client.method_22683().method_4486();
        int sh = client.method_22683().method_4502();
        float cx = (float)sw / 2.0f;
        float cy = (float)sh / 2.0f;
        float mcFov = ((Integer)client.field_1690.method_41808().method_41753()).intValue();
        float pixelsPerDeg = (float)sh / 2.0f / (mcFov / 2.0f);
        float radius = fovDeg * pixelsPerDeg;
        int segments = 48;
        int dotSize = 2;
        int color = 0x30FFFFFF;
        for (int i = 0; i < segments; ++i) {
            double angle = Math.PI * 2 * (double)i / (double)segments;
            int px = (int)((double)cx + (double)radius * Math.cos(angle) - (double)((float)dotSize / 2.0f));
            int py = (int)((double)cy + (double)radius * Math.sin(angle) - (double)((float)dotSize / 2.0f));
            g.method_25294(px, py, px + dotSize, py + dotSize, color);
        }
    }

    private static void processKeybinds(class_310 client) {
        if (client.field_1755 != null) {
            return;
        }
        class_1041 window = client.method_22683();
        for (Category cat : MenuData.CATEGORIES) {
            for (ModModule mod : cat.modules) {
                if (mod.keybind >= 0 && class_3675.method_15987((class_1041)window, (int)mod.keybind)) {
                    if (mod._keybindWasDown) continue;
                    if ("AutoSwap".equals(mod.name)) {
                        AUTO_SWAP.onKeybindPress(client);
                    } else {
                        mod.enabled = !mod.enabled;
                    }
                    mod._keybindWasDown = true;
                    continue;
                }
                mod._keybindWasDown = false;
            }
        }
    }

    static {
        MENU = new ModMenu();
        AIM_ASSIST = new AimAssist();
        NO_RENDER = new NoRender();
        TRIGGERBOT = new Triggerbot();
        AUTO_SPRINT = new AutoSprint();
        FULLBRIGHT = new Fullbright();
        ASPECT_RATIO = new AspectRatio();
        GLOW_ESP = new GlowESP();
        NAMETAGS = new Nametags();
        PREDICTION = new Prediction();
        AUTO_SWAP = new AutoSwap();
        HUD = new HudManager();
        TARGET_HUD = new TargetHudWidget(100.0f, 100.0f);
        saveCounter = 0;
    }
}

