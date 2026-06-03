package rich;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import rich.manager.Manager;
import rich.online.OnlineTracker;
import rich.update.UpdateChecker;
import rich.util.d;

public class Initialization implements ClientModInitializer {
   private static Initialization instance;
   private Manager manager;
   private static final int AUTOSAVE_TICK_INTERVAL = 1200;
   private static int saveCounter;

   public static Initialization getInstance() {
      if (instance == null) {
         instance = new Initialization();
      }
      return instance;
   }

   public Manager getManager() {
      return this.manager;
   }

   public void onInitializeClient() {
      d.a();
      this.registerClientTickEvents();
      this.registerHudRenderCallbacks();
      this.registerClientLifecycleEvents();
   }

   private void registerClientTickEvents() {
      ClientTickEvents.END_CLIENT_TICK.register(client -> {
         if (client.player == null) {
            return;
         }

         if (this.manager != null && this.manager.getModuleRepository() != null) {
            for (var module : this.manager.getModuleRepository().modules()) {
               String name = module.getName();
               if ("AutoSprint".equals(name)) {
                  if (module instanceof rich.modules.impl.movement.AutoSprint autoSprint) {
                     autoSprint.tick(client);
                  }
               } else if ("Fullbright".equals(name)) {
                  if (module instanceof rich.modules.impl.render.FullBright fullbright) {
                     fullbright.tick(client);
                  }
               }
            }
         }

         Entity targetedEntity = client.targetedEntity;
         if (targetedEntity instanceof LivingEntity living && living != client.player) {
            if (client.player.getAttackCooldownProgress(0.0f) < 0.5f) {
               if (this.manager != null && this.manager.getHudManager() != null) {
                  var hudMgr = this.manager.getHudManager();
                  hudMgr.modules().stream()
                     .filter(mod -> "TargetHud".equals(mod.getName()))
                     .findFirst()
                     .ifPresent(mod -> {
                        if (mod instanceof rich.modules.impl.render.hud.TargetHudModule targetHud) {
                           targetHud.setTarget(living);
                        }
                     });
               }
            }
         }

         if (++saveCounter >= AUTOSAVE_TICK_INTERVAL) {
            saveCounter = 0;
            if (this.manager != null && this.manager.getConfigSystem() != null) {
               this.manager.getConfigSystem().saveAll();
            }
         }
      });
   }

   private void registerHudRenderCallbacks() {
      HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player == null || this.manager == null) {
            return;
         }

         if (!this.manager.getClickgui().isOpen()) {
            if (this.manager.getHudManager() != null) {
               this.manager.getHudManager().renderText(guiGraphics, 1.0f);
            }
         }
      });
   }

   private void registerClientLifecycleEvents() {
      ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
         if (this.manager != null && this.manager.getConfigSystem() != null) {
            this.manager.getConfigSystem().saveAll();
         }
      });
   }

   public void init() {
      this.manager = new Manager();
      this.manager.init();
      UpdateChecker.getInstance().start();
      OnlineTracker.getInstance().start();
   }
}
