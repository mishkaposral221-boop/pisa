package rich.events.impl;

import net.minecraft.entity.player.PlayerEntity;
import rich.events.api.events.callables.EventCancellable;

public class JumpEvent extends EventCancellable {
   private PlayerEntity player;

   public PlayerEntity getPlayer() {
      return this.player;
   }

   public JumpEvent(PlayerEntity var1) {
      this.player = var1;
   }
}
