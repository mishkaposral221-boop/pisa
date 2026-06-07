package rich.events.api.types;

import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
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
         case ClientCommandC2SPacket var4:
            serverSprint = switch (var4.getMode()) {
               case START_SPRINTING -> true;
               case STOP_SPRINTING -> false;
               default -> serverSprint;
            };
            break;
         case UpdateSelectedSlotC2SPacket var5:
            selectedSlot = var5.getSelectedSlot();
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
