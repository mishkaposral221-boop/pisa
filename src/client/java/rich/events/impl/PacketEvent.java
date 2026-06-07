package rich.events.impl;

import net.minecraft.network.packet.Packet;
import rich.events.api.events.callables.EventCancellable;

public class PacketEvent extends EventCancellable {
   private Packet<?> packet;
   private PacketEvent.Type type;

   public boolean isSend() {
      return this.type.equals(PacketEvent.Type.SEND);
   }

   public Packet<?> getPacket() {
      return this.packet;
   }

   public PacketEvent.Type getType() {
      return this.type;
   }

   public void setPacket(Packet<?> var1) {
      this.packet = var1;
   }

   public void setType(PacketEvent.Type var1) {
      this.type = var1;
   }

   public PacketEvent(Packet<?> var1, PacketEvent.Type var2) {
      this.packet = var1;
      this.type = var2;
   }

   public enum Type {
      SEND,
      RECEIVE;
   }
}
