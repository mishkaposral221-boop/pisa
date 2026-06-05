package rich.modules.impl.player;

import antidaunleak.api.annotation.Native;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.mixin.MinecraftClientAccessor;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.c;

public class NoDelay extends ModuleStructure {
   private final SelectSetting mode = new SelectSetting("Режим", "На что убирать задержку").value("Прыжок", "Блоки");
   private final SliderSettings delay = new SliderSettings("Задержка", "Задержка в тиках (0 = максимально быстро)").range(0, 20).setValue(0.0F);

   public static NoDelay getInstance() {
      return c.a(NoDelay.class);
   }

   public NoDelay() {
      super("NoDelay", "Быстрый прыжок / постановка блоков по удержанию клавиши", ModuleCategory.UTILITIES);
      this.settings(this.mode, this.delay);
   }

   @EventHandler
   @Native(type = Native.Type.VMProtectBeginUltra)
   public void onTick(TickEvent var1) {
      if (mc.player == null) {
         return;
      }

      int var2 = this.delay.getInt();
      if (this.mode.isSelected("Прыжок")) {
         if (mc.options.jumpKey.isPressed() && mc.player.jumpingCooldown > var2) {
            mc.player.jumpingCooldown = var2;
         }
      } else if (this.mode.isSelected("Блоки") && mc.options.useKey.isPressed()) {
         MinecraftClientAccessor var3 = (MinecraftClientAccessor)mc;
         if (var3.getItemUseCooldown() > var2) {
            var3.setItemUseCooldown(var2);
         }
      }
   }
}
