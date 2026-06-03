package rich.events.impl;

import rich.events.api.events.callables.EventCancellable;

public class UsingItemEvent extends EventCancellable {
   byte type;

   public byte getType() {
      return this.type;
   }

   public void setType(byte var1) {
      this.type = var1;
   }

   public UsingItemEvent(byte var1) {
      this.type = var1;
   }
}
