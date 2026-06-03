package rich.modules.module;

import net.minecraft.client.MinecraftClient;
import rich.IMinecraft;
import rich.Initialization;
import rich.events.api.EventManager;
import rich.events.impl.ModuleToggleEvent;
import rich.modules.impl.render.Hud;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.SettingRepository;
import rich.screens.hud.Notifications;
import rich.util.animations.Animation;
import rich.util.animations.Decelerate;
import rich.util.animations.Direction;

public class ModuleStructure extends SettingRepository implements IMinecraft {
   private final String name;
   private final String description;
   private final ModuleCategory category;
   private final Animation animation = new Decelerate().setMs(175).setValue(1.0);
   private int key = -1;
   private int type = 1;
   public boolean state;
   public boolean favorite;

   public ModuleStructure(String var1, ModuleCategory var2) {
      this.name = var1;
      this.category = var2;
      this.description = "";
   }

   public ModuleStructure(String var1, String var2, ModuleCategory var3) {
      this.name = var1;
      this.description = var2;
      this.category = var3;
   }

   public void switchState() {
      this.setState(!this.state);
   }

   public void setState(boolean var1) {
      this.animation.setDirection(var1 ? Direction.FORWARDS : Direction.BACKWARDS);
      if (var1 != this.state) {
         this.state = var1;
         this.handleStateChange();
      }
   }

   public void switchFavorite() {
      this.setFavorite(!this.favorite);
   }

   public void setFavorite(boolean var1) {
      this.favorite = var1;
   }

   private void handleStateChange() {
      MinecraftClient var1 = MinecraftClient.getInstance();
      if (var1.player != null && var1.world != null) {
         Hud var2 = Hud.getInstance();
         Notifications var3 = Notifications.getInstance();
         if (var2 != null && var2.isState() && var3 != null && var2.interfaceSettings.isSelected("Notifications")) {
            if (this.state) {
               var3.addNotification("Feature " + this.name + " - enabled!", 2000L);
            } else {
               var3.addNotification("Feature " + this.name + " - disabled!", 2000L);
            }
         }

         if (this.state) {
            this.activate();
         } else {
            this.deactivate();
         }
      }

      this.toggleSilent(this.state);
      ModuleToggleEvent var4 = new ModuleToggleEvent(this, this.state);
      EventManager.callEvent(var4);
   }

   private void toggleSilent(boolean var1) {
      EventManager var2 = Initialization.getInstance().getManager().getEventManager();
      if (var1) {
         EventManager.register(this);
      } else {
         EventManager.unregister(this);
      }
   }

   public void activate() {
   }

   public void deactivate() {
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public ModuleCategory getCategory() {
      return this.category;
   }

   public Animation getAnimation() {
      return this.animation;
   }

   public int getKey() {
      return this.key;
   }

   public int getType() {
      return this.type;
   }

   public boolean isState() {
      return this.state;
   }

   public boolean isFavorite() {
      return this.favorite;
   }

   public void setKey(int var1) {
      this.key = var1;
   }

   public void setType(int var1) {
      this.type = var1;
   }
}
