package rich.settings;

import rich.modules.module.setting.Setting;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.TextSetting;

public class CustomItemSetting extends Setting {
   public final TextSetting name;
   public final ColorSetting color;

   public CustomItemSetting(String var1, int var2) {
      super("Custom Item", "");
      this.name = new TextSetting("Item Name", var1);
      this.color = new ColorSetting("Item Color", "Цвет для предмета").value(var2);
   }

   @Override
   public boolean isVisible() {
      return true;
   }
}
