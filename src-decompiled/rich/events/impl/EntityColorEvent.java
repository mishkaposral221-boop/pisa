package rich.events.impl;

import rich.events.api.events.callables.EventCancellable;

public class EntityColorEvent extends EventCancellable {
   private int color;

   public int getColor() {
      return this.color;
   }

   public void setColor(int var1) {
      this.color = var1;
   }

   public EntityColorEvent(int var1) {
      this.color = var1;
   }
}
