package rich.events.impl;

import rich.events.api.events.callables.EventCancellable;

public class FovEvent extends EventCancellable {
   private int fov;

   public int getFov() {
      return this.fov;
   }

   public void setFov(int var1) {
      this.fov = var1;
   }
}
