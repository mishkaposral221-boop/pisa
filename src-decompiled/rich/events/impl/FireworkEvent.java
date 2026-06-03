package rich.events.impl;

import net.minecraft.class_243;
import rich.events.api.events.Event;

public class FireworkEvent implements Event {
   public class_243 vector;

   public FireworkEvent(class_243 var1) {
      this.vector = var1;
   }

   public class_243 getVector() {
      return this.vector;
   }

   public void setVector(class_243 var1) {
      this.vector = var1;
   }
}
