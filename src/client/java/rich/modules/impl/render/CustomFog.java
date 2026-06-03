package rich.modules.impl.render;

import java.awt.Color;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.c;

public class CustomFog extends ModuleStructure {
   public ColorSetting color = new ColorSetting("Цвет", "Цвет тумана");
   public SliderSettings distance = new SliderSettings("Дистанция", "Расстояние тумана").range(0, 1000).setValue(100.0F);

   public static CustomFog getInstance() {
      return c.a(CustomFog.class);
   }

   public CustomFog() {
      super("CustomFog", "Позволяет изменять туман", ModuleCategory.VISUALS);
      this.color.setColor(new Color(255, 255, 255, 255).getRGB());
      this.settings(this.color, this.distance);
   }
}
