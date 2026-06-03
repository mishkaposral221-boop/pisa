package rich.modules.impl.render;

import net.minecraft.class_1293;
import net.minecraft.class_1294;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SelectSetting;

public class FullBright extends ModuleStructure {
   private final SelectSetting mode = new SelectSetting("Режим", "Режим работы FullBright").value("Gamma", "Night Vision");

   public FullBright() {
      super("FullBright", "Убирает темноту в игре", ModuleCategory.VISUALS);
      this.settings(this.mode);
   }

   @Override
   public void activate() {
      if (this.mode.isSelected("Gamma")) {
         mc.field_1690.method_42473().method_41748(10.0);
      }
   }

   @Override
   public void deactivate() {
      mc.field_1690.method_42473().method_41748(1.0);
      if (mc.field_1724 != null && mc.field_1724.method_6059(class_1294.field_5925)) {
         mc.field_1724.method_6016(class_1294.field_5925);
      }
   }

   @EventHandler
   public void onTick(TickEvent var1) {
      if (this.isState() && mc.field_1724 != null) {
         if (this.mode.isSelected("Night Vision")) {
            mc.field_1690.method_42473().method_41748(1.0);
            mc.field_1724.method_6092(new class_1293(class_1294.field_5925, 500, 0, false, false, false));
         } else {
            if (mc.field_1724.method_6059(class_1294.field_5925)) {
               mc.field_1724.method_6016(class_1294.field_5925);
            }

            mc.field_1690.method_42473().method_41748(10.0);
         }
      }
   }
}
