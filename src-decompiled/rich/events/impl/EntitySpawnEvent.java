package rich.events.impl;

import net.minecraft.class_1297;
import rich.events.api.events.callables.EventCancellable;

public class EntitySpawnEvent extends EventCancellable {
   private class_1297 entity;

   public EntitySpawnEvent(class_1297 var1) {
      this.entity = var1;
   }

   public class_1297 getEntity() {
      return this.entity;
   }

   public void setEntity(class_1297 var1) {
      this.entity = var1;
   }
}
