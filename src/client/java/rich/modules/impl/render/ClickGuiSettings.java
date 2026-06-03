package rich.modules.impl.render;

import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.c;

public class ClickGuiSettings extends ModuleStructure {
   public final SliderSettings opacity = new SliderSettings("Прозрачность", "Прозрачность фона ClickGui").range(0.1F, 1.0F).setValue(1.0F);
   public final BooleanSetting blur = new BooleanSetting("Блюр", "Размытие фона ClickGui").setValue(true);
   public final SliderSettings blurStrength = new SliderSettings("Сила блюра", "Сила размытия фона")
      .range(1.0F, 20.0F)
      .setValue(8.0F)
      .visible(() -> this.blur.isValue());

   public static ClickGuiSettings getInstance() {
      return c.a(ClickGuiSettings.class);
   }

   public ClickGuiSettings() {
      super("ClickGui", "Настройки ClickGui", ModuleCategory.VISUALS);
      this.settings(this.opacity, this.blur, this.blurStrength);
   }
}
