package rich.events.impl;

import net.minecraft.class_1799;
import rich.events.api.events.Event;

public class HotbarItemRenderEvent implements Event {
   private class_1799 stack;
   private final int hotbarIndex;

   public HotbarItemRenderEvent(class_1799 var1, int var2) {
      this.stack = var1;
      this.hotbarIndex = var2;
   }

   public class_1799 getStack() {
      return this.stack;
   }

   public int getHotbarIndex() {
      return this.hotbarIndex;
   }

   public void setStack(class_1799 var1) {
      this.stack = var1;
   }
}
