package rich.modules.impl.render.hud;

import rich.modules.impl.render.Hud;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;

public abstract class HudModuleBase extends ModuleStructure {
   private final String elementName;

   public HudModuleBase(String var1, String var2, String var3) {
      super(var1, var2, ModuleCategory.HUD);
      this.elementName = var3;
   }

   @Override
   public void setState(boolean var1) {
      super.setState(var1);
      Hud var2 = Hud.getInstance();
      if (var2 != null) {
         if (var1) {
            var2.interfaceSettings.select(this.elementName);
         } else {
            var2.interfaceSettings.deselect(this.elementName);
         }
      }
   }

   public void syncFromHud() {
      Hud var1 = Hud.getInstance();
      if (var1 != null) {
         boolean var2 = var1.interfaceSettings.isSelected(this.elementName);
         if (var2 != this.isState()) {
            super.setState(var2);
         }
      }
   }
}
