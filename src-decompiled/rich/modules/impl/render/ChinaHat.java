package rich.modules.impl.render;

import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.util.a;

public class ChinaHat extends ModuleStructure {
   private static ChinaHat instance;
   public final ColorSetting color1 = new ColorSetting("Color 1", "First gradient color").value(a.d(255, 50, 100, 255));
   public final ColorSetting color2 = new ColorSetting("Color 2", "Second gradient color").value(a.d(100, 50, 255, 255));
   public final ColorSetting color3 = new ColorSetting("Color 3", "Third gradient color").value(a.d(50, 255, 100, 255));
   public final ColorSetting color4 = new ColorSetting("Color 4", "Fourth gradient color").value(a.d(255, 200, 50, 255));
   public final SelectSetting colorMode = new SelectSetting("Color Mode", "Number of colors to use")
      .value("2 Colors", "1 Color", "2 Colors", "3 Colors", "4 Colors");
   public final SelectSetting style = new SelectSetting("Style", "Hat style").value("Обычный", "Обычный", "Сеточный");

   public static ChinaHat getInstance() {
      return instance;
   }

   public ChinaHat() {
      super("ChinaHat", "China Hat", ModuleCategory.VISUALS);
      instance = this;
      this.settings(this.style, this.colorMode, this.color1, this.color2, this.color3, this.color4);
   }
}
