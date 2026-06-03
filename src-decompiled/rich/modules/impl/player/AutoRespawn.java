package rich.modules.impl.player;

import antidaunleak.api.annotation.Native;
import rich.events.api.EventHandler;
import rich.events.impl.DeathScreenEvent;
import rich.events.impl.PacketEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SelectSetting;

public class AutoRespawn extends ModuleStructure {
   private final SelectSetting modeSetting = new SelectSetting("Режим", "Выберите, что будет использоваться").value("Default");

   public AutoRespawn() {
      super("AutoRespawn", "Auto Respawn", ModuleCategory.UTILITIES);
      this.settings(this.modeSetting);
   }

   @EventHandler
   public void onPacket(PacketEvent var1) {
   }

   @EventHandler
   @Native(type = Native.Type.VMProtectBeginMutation)
   public void onDeathScreen(DeathScreenEvent var1) {
      if (this.modeSetting.isSelected("Default")) {
         mc.field_1724.method_7331();
         mc.method_1507(null);
      }
   }
}
