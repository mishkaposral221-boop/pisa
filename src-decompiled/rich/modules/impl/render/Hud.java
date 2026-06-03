package rich.modules.impl.render;

import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.MultiSelectSetting;
import rich.util.c;

public class Hud extends ModuleStructure {
   public final MultiSelectSetting interfaceSettings = new MultiSelectSetting("Elements", "Interface elements settings")
      .value("Watermark", "HotKeys", "Potions", "TargetHud", "ArmorHud", "InventoryHUD", "Info", "Notifications")
      .selected("Watermark", "HotKeys", "Potions", "TargetHud", "ArmorHud", "InventoryHUD", "Info", "Notifications");
   public final BooleanSetting showBps = new BooleanSetting("Show BPS", "Show blocks per second")
      .setValue(true)
      .visible(() -> this.interfaceSettings.isSelected("Info"));
   public final BooleanSetting showTps = new BooleanSetting("Show TPS", "Show TPS in Watermark")
      .setValue(true)
      .visible(() -> this.interfaceSettings.isSelected("Watermark"));

   public static Hud getInstance() {
      return c.a(Hud.class);
   }

   public Hud() {
      super("Hud", ModuleCategory.HUD);
      this.settings(this.interfaceSettings, this.showBps, this.showTps);
      this.setState(true);
   }
}
