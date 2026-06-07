package rich.modules.impl.render;

import java.awt.Color;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.ColorSetting;
import rich.util.c;

public class CustomSky extends ModuleStructure {
   public ColorSetting color = new ColorSetting("Цвет", "Цвет неба");

   public static CustomSky getInstance() {
      return c.a(CustomSky.class);
   }

   public CustomSky() {
      super("CustomSky", "Позволяет изменять цвет неба", ModuleCategory.VISUALS);
      this.color.setColor(new Color(135, 206, 235, 255).getRGB());
      this.settings(this.color);
   }
}
