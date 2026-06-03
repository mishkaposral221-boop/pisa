package rich.events.impl;

import net.minecraft.class_1657;
import rich.events.api.events.callables.EventCancellable;

public class JumpEvent extends EventCancellable {
   private class_1657 player;

   public class_1657 getPlayer() {
      return this.player;
   }

   public JumpEvent(class_1657 var1) {
      this.player = var1;
   }
}
