package rich.client.draggables;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_332;
import rich.events.impl.PacketEvent;
import rich.modules.impl.render.Hud;
import rich.screens.hud.ArmorHud;
import rich.screens.hud.CoolDowns;
import rich.screens.hud.HotKeys;
import rich.screens.hud.Info;
import rich.screens.hud.Inventory;
import rich.screens.hud.InventoryHUD;
import rich.screens.hud.Notifications;
import rich.screens.hud.Potions;
import rich.screens.hud.TargetHud;
import rich.screens.hud.Watermark;
import rich.util.config.impl.drag.DragConfig;

public class HudManager {
   private final List<HudElement> elements = new ArrayList<>();
   private boolean initialized = false;
   private Hud cachedHud = null;
   private long lastHudCheckTime = 0L;
   private static final long HUD_CACHE_TIME = 100L;

   public void initElements() {
      if (!this.initialized) {
         this.register(new Watermark());
         this.register(new HotKeys());
         this.register(new Notifications());
         this.register(new Potions());
         this.register(new CoolDowns());
         this.register(new TargetHud());
         this.register(new ArmorHud());
         this.register(new InventoryHUD());
         this.register(new Info());
         this.register(new Inventory());
         this.initialized = true;
         DragConfig.getInstance().load();
      }
   }

   public void register(HudElement var1) {
      this.elements.add(var1);
   }

   public void onPacket(PacketEvent var1) {
      for (HudElement var3 : this.elements) {
         var3.onPacket(var1);
      }
   }

   public void render(class_332 var1, float var2, int var3, int var4) {
      Hud var5 = this.getCachedHud();
      if (var5 != null && var5.isState()) {
         for (HudElement var7 : this.elements) {
            if (var5.interfaceSettings.isSelected(var7.getName()) && var7.visible()) {
               var7.render(var1, var2);
            }
         }
      }
   }

   public void tick() {
      Hud var1 = this.getCachedHud();
      if (var1 != null && var1.isState()) {
         for (HudElement var3 : this.elements) {
            if (var1.interfaceSettings.isSelected(var3.getName())) {
               var3.tick();
            }
         }
      }
   }

   private Hud getCachedHud() {
      long var1 = System.currentTimeMillis();
      if (var1 - this.lastHudCheckTime > 100L) {
         this.cachedHud = Hud.getInstance();
         this.lastHudCheckTime = var1;
      }

      return this.cachedHud;
   }

   private boolean isElementEnabled(HudElement var1) {
      Hud var2 = this.getCachedHud();
      return var2 != null && var2.isState() ? var2.interfaceSettings.isSelected(var1.getName()) : false;
   }

   public HudElement getElementAt(double var1, double var3) {
      for (int var5 = this.elements.size() - 1; var5 >= 0; var5--) {
         HudElement var6 = this.elements.get(var5);
         if (this.isElementEnabled(var6)
            && var6.visible()
            && var1 >= var6.getX()
            && var1 <= var6.getX() + var6.getWidth()
            && var3 >= var6.getY()
            && var3 <= var6.getY() + var6.getHeight()) {
            return var6;
         }
      }

      return null;
   }

   public boolean mouseClicked(double var1, double var3, int var5) {
      for (HudElement var7 : this.elements) {
         if (this.isElementEnabled(var7) && var7.mouseClicked(var1, var3, var5)) {
            return true;
         }
      }

      return false;
   }

   public void saveConfig() {
      DragConfig.getInstance().save();
   }

   public void loadConfig() {
      DragConfig.getInstance().load();
   }

   public List<HudElement> getElements() {
      return this.elements;
   }

   public List<HudElement> getEnabledElements() {
      ArrayList var1 = new ArrayList();

      for (HudElement var3 : this.elements) {
         if (this.isElementEnabled(var3)) {
            var1.add(var3);
         }
      }

      return var1;
   }

   public boolean isInitialized() {
      return this.initialized;
   }
}
