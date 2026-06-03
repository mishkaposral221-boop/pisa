package rich.events.impl;

import net.minecraft.class_2248;
import rich.events.api.events.callables.EventCancellable;

public class PlayerCollisionEvent extends EventCancellable {
   private class_2248 block;

   public void setBlock(class_2248 var1) {
      this.block = var1;
   }

   public class_2248 getBlock() {
      return this.block;
   }

   public PlayerCollisionEvent(class_2248 var1) {
      this.block = var1;
   }
}
