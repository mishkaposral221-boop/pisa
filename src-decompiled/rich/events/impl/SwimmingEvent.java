package rich.events.impl;

import net.minecraft.class_243;
import rich.events.api.events.callables.EventCancellable;

public class SwimmingEvent extends EventCancellable {
   class_243 vector;

   public void setVector(class_243 var1) {
      this.vector = var1;
   }

   public class_243 getVector() {
      return this.vector;
   }

   public SwimmingEvent(class_243 var1) {
      this.vector = var1;
   }
}
