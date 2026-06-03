package rich.events.impl;

import net.minecraft.block.Block;
import rich.events.api.events.callables.EventCancellable;

public class PlayerCollisionEvent extends EventCancellable {
   private Block block;

   public void setBlock(Block var1) {
      this.block = var1;
   }

   public Block getBlock() {
      return this.block;
   }

   public PlayerCollisionEvent(Block var1) {
      this.block = var1;
   }
}
