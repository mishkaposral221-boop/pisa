package rich.modules.impl.render;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
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
         mc.options.getGamma().setValue(10.0);
      }
   }

   @Override
   public void deactivate() {
      mc.options.getGamma().setValue(1.0);
      if (mc.player != null && mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
         mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
      }
   }

   @EventHandler
   public void onTick(TickEvent var1) {
      if (this.isState() && mc.player != null) {
         if (this.mode.isSelected("Night Vision")) {
            mc.options.getGamma().setValue(1.0);
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 500, 0, false, false, false));
         } else {
            if (mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
               mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            }

            mc.options.getGamma().setValue(10.0);
         }
      }
   }
}
