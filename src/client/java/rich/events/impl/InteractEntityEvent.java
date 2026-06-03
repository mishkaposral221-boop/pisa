package rich.events.impl;

import net.minecraft.entity.Entity;
import rich.events.api.events.callables.EventCancellable;

public class InteractEntityEvent extends EventCancellable {
   private Entity entity;

   public InteractEntityEvent(Entity var1) {
      this.entity = var1;
   }

   public Entity getEntity() {
      return this.entity;
   }

   public void setEntity(Entity var1) {
      this.entity = var1;
   }
}
