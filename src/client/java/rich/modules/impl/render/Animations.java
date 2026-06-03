package rich.modules.impl.render;

import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.c;

public class Animations extends ModuleStructure {
   public final BooleanSetting chatAnim = new BooleanSetting("Чат", "Анимация открытия чата").setValue(true);
   public final BooleanSetting tabAnim = new BooleanSetting("Таб", "Анимация открытия таблицы игроков").setValue(true);
   public final BooleanSetting hotbarAnim = new BooleanSetting("Хотбар", "Анимация слотов хотбара").setValue(true);
   public final BooleanSetting inventoryAnim = new BooleanSetting("Инвентарь", "Анимация открытия инвентаря").setValue(true);
   public final SliderSettings speed = new SliderSettings("Скорость", "Скорость анимаций").range(1.0F, 20.0F).setValue(8.0F);
   public final SelectSetting easing = new SelectSetting("Тип", "Тип анимации").value("Ease Out", "Ease Out", "Ease In Out", "Linear");

   public static Animations getInstance() {
      return c.a(Animations.class);
   }

   public Animations() {
      super("Animations", "Анимации интерфейса", ModuleCategory.VISUALS);
      this.settings(this.chatAnim, this.tabAnim, this.hotbarAnim, this.inventoryAnim, this.speed, this.easing);
   }

   public float lerpFactor(float var1) {
      float var2 = this.speed.getValue();

      return switch (this.easing.getSelected()) {
         case "Ease In Out" -> (float)(1.0 - Math.pow(0.001, var1 * var2 * 0.5));
         case "Linear" -> Math.min(1.0F, var1 * var2 * 3.0F);
         default -> (float)(1.0 - Math.pow(0.001, var1 * var2));
      };
   }
}
