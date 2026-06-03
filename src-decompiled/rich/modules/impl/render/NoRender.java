package rich.modules.impl.render;

import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.MultiSelectSetting;
import rich.util.c;

public class NoRender extends ModuleStructure {
   public final MultiSelectSetting modeSetting = new MultiSelectSetting("Элементы", "Выберите элементы для игнорирования")
      .value("Fire", "Bad Effects", "Block Overlay", "Darkness", "Damage", "Nausea", "Scoreboard", "BossBar")
      .selected("Fire", "Bad Effects", "Block Overlay", "Darkness", "Damage", "Nausea");

   public static NoRender getInstance() {
      return c.a(NoRender.class);
   }

   public NoRender() {
      super("NoRender", "No Render", ModuleCategory.VISUALS);
      this.settings(this.modeSetting);
   }
}
