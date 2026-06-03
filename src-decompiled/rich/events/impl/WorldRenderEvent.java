package rich.events.impl;

import net.minecraft.class_4587;
import rich.events.api.events.Event;

public class WorldRenderEvent implements Event {
   private class_4587 stack;
   private float partialTicks;

   public WorldRenderEvent(class_4587 var1, float var2) {
      this.stack = var1;
      this.partialTicks = var2;
   }

   public class_4587 getStack() {
      return this.stack;
   }

   public float getPartialTicks() {
      return this.partialTicks;
   }
}
