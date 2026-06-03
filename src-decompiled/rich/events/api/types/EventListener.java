package rich.events.api.types;

import net.minecraft.class_2848;
import net.minecraft.class_2868;
import rich.Initialization;
import rich.events.api.EventHandler;
import rich.events.impl.PacketEvent;
import rich.events.impl.TickEvent;
import rich.events.impl.UsingItemEvent;

public class EventListener implements Listener {
   public static boolean serverSprint;
   public static int selectedSlot;

   @EventHandler
   public void onTick(TickEvent var1) {
      Initialization.getInstance().getManager().getAttackPerpetrator().tick();
      if (Initialization.getInstance().getManager().getHudManager() != null) {
         Initialization.getInstance().getManager().getHudManager().tick();
      }
   }

   @EventHandler
   public void onPacket(PacketEvent var1) {
      switch (var1.getPacket()) {
         case class_2848 var4:
            serverSprint = switch (var4.method_12365()) {
               case field_12981 -> true;
               case field_12985 -> false;
               default -> serverSprint;
            };
            break;
         case class_2868 var5:
            selectedSlot = var5.method_12442();
            break;
         default:
      }

      Initialization.getInstance().getManager().getAttackPerpetrator().onPacket(var1);
      Initialization.getInstance().getManager().getHudManager().onPacket(var1);
   }

   @EventHandler
   public void onUsingItemEvent(UsingItemEvent var1) {
      Initialization.getInstance().getManager().getAttackPerpetrator().onUsingItem(var1);
   }
}
