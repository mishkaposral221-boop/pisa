package rich.modules.impl.render;

import rich.events.api.EventHandler;
import rich.events.impl.FovEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SliderSettings;

public class DynamicFov extends ModuleStructure {
   private final BooleanSetting lockFov = new BooleanSetting("Lock FOV", "Prevent potions/speed from changing FOV").setValue(true);
   private final SliderSettings fovSetting = new SliderSettings("Field of View", "Custom FOV (0 = use game setting)").setValue(0.0F).range(0.0F, 110.0F);

   public DynamicFov() {
      super("DynamicFov", "Dynamic FOV", ModuleCategory.UTILITIES);
      this.settings(this.lockFov, this.fovSetting);
   }

   @EventHandler
   public void onFov(FovEvent var1) {
      float var2 = this.fovSetting.getValue();
      if (var2 > 0.0F) {
         var1.setFov((int)var2);
         var1.cancel();
      } else if (this.lockFov.isValue()) {
         var1.setFov((Integer)mc.options.getFov().getValue());
         var1.cancel();
      }
   }
}
