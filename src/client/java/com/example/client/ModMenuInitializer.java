package com.example.client;

import com.example.ExampleMod;
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
import com.ferra13671.cometrenderer.minecraft.CRM;
import com.ferra13671.cometrenderer.minecraft.CRMInstance;
import com.ferra13671.cometrenderer.minecraft.event.RenderHudCallback;
import com.ferra13671.cometrenderer.minecraft.event.RenderWorldCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.util.Window;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;

public class ModMenuInitializer {
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
    private static boolean setupDone;

    public static void setup() {
        if (setupDone) {
            return;
        }
        setupDone = true;

        MinecraftClient client = MinecraftClient.getInstance();
        crmInstance = new CRMInstance(() -> client.getWindow().getScaleFactor());

        RenderHudCallback.EVENT.register(() -> {
            if (crmInstance == null) {
                ExampleMod.LOGGER.warn("RenderHudCallback: crmInstance is null, skipping menu shapes");
                return;
            }
            if (CRM.getPrograms() == null) {
                ExampleMod.LOGGER.warn("RenderHudCallback: CometRenderer not initialized yet, skipping menu shapes");
                return;
            }
            try {
                crmInstance.setupUIMatrix();
                if (MENU.isOpen()) {
                    MENU.renderShapes();
                }
            } catch (Exception e) {
                ExampleMod.LOGGER.error("Failed to render menu shapes", e);
            }
            try {
                crmInstance.restoreUIMatrix();
            } catch (Exception e) {
                ExampleMod.LOGGER.error("Failed to restore UI matrix after menu shapes", e);
            }
        });
        HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
            if (MENU.isOpen()) {
                MENU.renderText(guiGraphics);
            }
            AIM_ASSIST.getEngine().onFrame(MinecraftClient.getInstance(), 1.0f);
            NAMETAGS.render(guiGraphics, deltaTracker.getTickProgress(false));
            if (AIM_ASSIST.isEnabled()) {
                ModMenuInitializer.renderFovCircle(guiGraphics, AIM_ASSIST.fov());
            }
            if (!MENU.isOpen() && MinecraftClient.getInstance().player != null) {
                HUD.renderText(guiGraphics, 1.0f);
            }
        });
        ClientTickEvents.START_CLIENT_TICK.register(c -> TRIGGERBOT.tick(c));
        RenderWorldCallback.EVENT.register(() -> {
            try {
                GLOW_ESP.renderWorld();
                PREDICTION.renderWorld();
            } catch (Exception e) {
                ExampleMod.LOGGER.error("Failed to render world overlays", e);
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(c -> {
            Entity patt0$temp;
            AUTO_SPRINT.tick(c);
            FULLBRIGHT.tick(c);
            if (c.player != null && (patt0$temp = c.targetedEntity) instanceof LivingEntity) {
                LivingEntity living = (LivingEntity)patt0$temp;
                if (c.player.getAttackCooldownProgress(0.0f) < 0.5f && living != c.player) {
                    TARGET_HUD.setTarget(living);
                }
            }
            ModMenuInitializer.processKeybinds(c);
            HUD.tick();
            if (++saveCounter >= 100) {
                saveCounter = 0;
                ConfigManager.save();
            }
        });
        ModMenuInitializer.initHud();
        ConfigManager.load();
    }

    private static void initHud() {
        HUD.addWidget(new TopBarWidget(2.0f, 2.0f));
        HUD.addWidget(new InfoBarWidget(2.0f, 18.0f));
        HUD.addWidget(new PotionsWidget(2.0f, 100.0f));
        HUD.addWidget(new HotkeysWidget(2.0f, 160.0f));
        HUD.addWidget(TARGET_HUD);
        HUD.addWidget(new ActiveModsWidget());
    }

    private static void renderFovCircle(DrawContext g, float fovDeg) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }
        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();
        float cx = (float)sw / 2.0f;
        float cy = (float)sh / 2.0f;
        float mcFov = ((Integer)client.options.getFov().getValue()).intValue();
        float pixelsPerDeg = (float)sh / 2.0f / (mcFov / 2.0f);
        float radius = fovDeg * pixelsPerDeg;
        int segments = 48;
        int dotSize = 2;
        int color = 0x30FFFFFF;
        for (int i = 0; i < segments; ++i) {
            double angle = Math.PI * 2 * (double)i / (double)segments;
            int px = (int)((double)cx + (double)radius * Math.cos(angle) - (double)((float)dotSize / 2.0f));
            int py = (int)((double)cy + (double)radius * Math.sin(angle) - (double)((float)dotSize / 2.0f));
            g.fill(px, py, px + dotSize, py + dotSize, color);
        }
    }

    private static void processKeybinds(MinecraftClient client) {
        if (client.currentScreen != null) {
            return;
        }
        Window window = client.getWindow();
        for (Category cat : MenuData.CATEGORIES) {
            for (ModModule mod : cat.modules) {
                if (mod.keybind >= 0 && InputUtil.isKeyPressed((Window)window, (int)mod.keybind)) {
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
