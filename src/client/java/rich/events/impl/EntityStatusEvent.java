package rich.events.impl;

import net.minecraft.entity.Entity;
import rich.events.api.events.Event;

public class EntityStatusEvent implements Event {
   private final Entity entity;
   private final byte status;

   public Entity getEntity() {
      return this.entity;
   }

   public byte getStatus() {
      return this.status;
   }

   public EntityStatusEvent(Entity var1, byte var2) {
      this.entity = var1;
      this.status = var2;
   }
}
