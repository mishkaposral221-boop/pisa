package rich.modules.impl.misc;

import antidaunleak.api.annotation.Native;
import rich.events.api.EventHandler;
import rich.events.impl.ModuleToggleEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.c;
import rich.util.sounds.SoundManager;

public class ClientSounds extends ModuleStructure {
   private final SelectSetting soundType = new SelectSetting("Тип звука", "Select sound type").value("New", "Old").selected("New");
   private final SliderSettings volume = new SliderSettings("Громкость", "Set volume").range(0.1F, 2.0F).setValue(1.0F);

   public static ClientSounds getInstance() {
      return c.a(ClientSounds.class);
   }

   public ClientSounds() {
      super("ClientSounds", ModuleCategory.UTILITIES);
      this.settings(this.soundType, this.volume);
   }

   @EventHandler
   @Native(type = Native.Type.VMProtectBeginMutation)
   public void onModuleToggle(ModuleToggleEvent var1) {
      if (mc.player != null && mc.world != null) {
         if (var1.getModule() != this) {
            this.playToggleSound(var1.isEnabled());
         }
      }
   }

   @Native(type = Native.Type.VMProtectBeginMutation)
   private void playToggleSound(boolean var1) {
      float var2 = this.volume.getValue();
      if (var1) {
         if (this.soundType.isSelected("New")) {
            SoundManager.playSound(SoundManager.MODULE_ENABLE, var2, 1.0F);
         } else {
            SoundManager.playSound(SoundManager.ON, var2, 1.0F);
         }
      } else if (this.soundType.isSelected("New")) {
         SoundManager.playSound(SoundManager.MODULE_DISABLE, var2, 1.0F);
      } else {
         SoundManager.playSound(SoundManager.OFF, var2, 1.0F);
      }
   }
}
