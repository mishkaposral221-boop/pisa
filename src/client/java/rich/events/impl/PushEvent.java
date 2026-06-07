package rich.events.impl;

import rich.events.api.events.callables.EventCancellable;

public class PushEvent extends EventCancellable {
   private PushEvent.Type type;

   public PushEvent.Type getType() {
      return this.type;
   }

   public PushEvent(PushEvent.Type var1) {
      this.type = var1;
   }

   public enum Type {
      COLLISION,
      BLOCK,
      WATER;
   }
}
