package rich.modules.impl.player;

import antidaunleak.api.annotation.Native;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;

public class NoDelay extends ModuleStructure {
   public NoDelay() {
      super("NoDelay", "No Delay", ModuleCategory.UTILITIES);
   }

   @EventHandler
   @Native(type = Native.Type.VMProtectBeginUltra)
   public void onTick(TickEvent var1) {
      if (mc.field_1724 != null) {
         mc.field_1724.field_6228 = 0;
      }
   }
}
