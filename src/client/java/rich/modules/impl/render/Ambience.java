package rich.modules.impl.render;

import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import rich.events.api.EventHandler;
import rich.events.impl.PacketEvent;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.c;

public class Ambience extends ModuleStructure {
   public SliderSettings time = new SliderSettings("Время", "Время суток (0-24000)").range(0, 24000).setValue(1000.0F);
   private double animatedTime = 1000.0;

   public static Ambience getInstance() {
      return c.a(Ambience.class);
   }

   public Ambience() {
      super("Ambience", "Изменяет время мира", ModuleCategory.VISUALS);
      this.settings(this.time);
   }

   @Override
   public void activate() {
      this.animatedTime = this.time.getValue();
   }

   @EventHandler
   public void onTick(TickEvent var1) {
      double var2 = this.time.getValue();
      double var4 = 0.15;
      double var6 = var2 - this.animatedTime;
      this.animatedTime += var6 * var4;
   }

   @EventHandler
   public void onPacket(PacketEvent var1) {
      if (var1.getPacket() instanceof WorldTimeUpdateS2CPacket) {
         var1.cancel();
      }
   }

   public long getCustomTime() {
      return (long)this.animatedTime;
   }
}
