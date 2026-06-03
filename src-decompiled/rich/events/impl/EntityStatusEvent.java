package rich.events.impl;

import net.minecraft.class_1297;
import rich.events.api.events.Event;

public class EntityStatusEvent implements Event {
   private final class_1297 entity;
   private final byte status;

   public class_1297 getEntity() {
      return this.entity;
   }

   public byte getStatus() {
      return this.status;
   }

   public EntityStatusEvent(class_1297 var1, byte var2) {
      this.entity = var1;
      this.status = var2;
   }
}
