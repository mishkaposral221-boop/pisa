package rich.screens.clickgui.impl.module.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import rich.modules.module.ModuleStructure;

public class ModuleDisplayHelper {
   private final Set<ModuleStructure> modulesWithSettings = new HashSet<>();

   public void updateModulesWithSettings(List<ModuleStructure> var1) {
      this.modulesWithSettings.clear();

      for (ModuleStructure var3 : var1) {
         if (this.hasModuleSettings(var3)) {
            this.modulesWithSettings.add(var3);
         }
      }
   }

   public boolean hasModuleSettings(ModuleStructure var1) {
      if (var1 == null) {
         return false;
      }

      List var2 = var1.settings();
      return var2 != null && !var2.isEmpty();
   }

   public boolean hasSettings(ModuleStructure var1) {
      return this.modulesWithSettings.contains(var1);
   }
}
