package rich.util.modules;

import java.util.List;
import rich.IMinecraft;
import rich.events.api.EventHandler;
import rich.events.api.EventManager;
import rich.events.impl.KeyEvent;
import rich.modules.module.ModuleStructure;

public class ModuleSwitcher implements IMinecraft {
   private final List<ModuleStructure> moduleStructures;

   public ModuleSwitcher(List<ModuleStructure> var1, EventManager var2) {
      this.moduleStructures = var1;
      EventManager.register(this);
   }

   @EventHandler
   public void onKey(KeyEvent var1) {
      for (ModuleStructure var3 : this.moduleStructures) {
         if (var1.getKey() == var3.getKey() && mc.currentScreen == null) {
            try {
               this.handleModuleState(var3, var1.getAction());
            } catch (Exception var5) {
            }
         }
      }
   }

   private void handleModuleState(ModuleStructure var1, int var2) {
      if (var1.getType() == 1 && var2 == 1) {
         var1.switchState();
      }
   }
}
