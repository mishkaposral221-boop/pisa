package rich.events.impl;

import net.minecraft.class_2596;
import rich.events.api.events.callables.EventCancellable;

public class PacketEvent extends EventCancellable {
   private class_2596<?> packet;
   private PacketEvent.Type type;

   public boolean isSend() {
      return this.type.equals(PacketEvent.Type.SEND);
   }

   public class_2596<?> getPacket() {
      return this.packet;
   }

   public PacketEvent.Type getType() {
      return this.type;
   }

   public void setPacket(class_2596<?> var1) {
      this.packet = var1;
   }

   public void setType(PacketEvent.Type var1) {
      this.type = var1;
   }

   public PacketEvent(class_2596<?> var1, PacketEvent.Type var2) {
      this.packet = var1;
      this.type = var2;
   }

   public enum Type {
      SEND,
      RECEIVE;
   }
}
