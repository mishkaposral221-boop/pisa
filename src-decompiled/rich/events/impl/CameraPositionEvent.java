package rich.events.impl;

import net.minecraft.class_243;
import rich.events.api.events.Event;

public class CameraPositionEvent implements Event {
   private class_243 pos;

   public class_243 getPos() {
      return this.pos;
   }

   public void setPos(class_243 var1) {
      this.pos = var1;
   }

   public CameraPositionEvent(class_243 var1) {
      this.pos = var1;
   }
}
