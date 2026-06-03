package rich.events.impl;

import rich.events.api.events.Event;
import rich.modules.module.ModuleStructure;

public class ModuleToggleEvent implements Event {
   private final ModuleStructure module;
   private final boolean enabled;

   public ModuleStructure getModule() {
      return this.module;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public ModuleToggleEvent(ModuleStructure var1, boolean var2) {
      this.module = var1;
      this.enabled = var2;
   }
}
