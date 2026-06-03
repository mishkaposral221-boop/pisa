package rich.events.impl;

import net.minecraft.class_4587;
import net.minecraft.class_4597;
import rich.events.api.events.Event;

public class Event3D implements Event {
   public class_4587 stack;
   public class_4597 buffer;

   public Event3D(class_4587 var1, class_4597 var2) {
      this.stack = var1;
      this.buffer = var2;
   }
}
