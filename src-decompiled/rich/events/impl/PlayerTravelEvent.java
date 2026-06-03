package rich.events.impl;

import net.minecraft.class_243;
import rich.events.api.events.callables.EventCancellable;

public class PlayerTravelEvent extends EventCancellable {
   private class_243 motion;
   private final boolean pre;

   public PlayerTravelEvent(class_243 var1, boolean var2) {
      this.motion = var1;
      this.pre = var2;
   }

   public class_243 getMotion() {
      return this.motion;
   }

   public void setMotion(class_243 var1) {
      this.motion = var1;
   }

   public boolean isPre() {
      return this.pre;
   }
}
