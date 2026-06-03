package rich.events.impl;

import rich.events.api.events.Event;

public class RotationUpdateEvent implements Event {
   byte type;

   public byte getType() {
      return this.type;
   }

   public RotationUpdateEvent(byte var1) {
      this.type = var1;
   }
}
