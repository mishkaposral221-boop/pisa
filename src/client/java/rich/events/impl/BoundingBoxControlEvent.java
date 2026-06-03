package rich.events.impl;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import rich.events.api.events.callables.EventCancellable;

public class BoundingBoxControlEvent extends EventCancellable {
   public Box box;
   public Entity entity;

   public Box getBox() {
      return this.box;
   }

   public Entity getEntity() {
      return this.entity;
   }

   public void setBox(Box var1) {
      this.box = var1;
   }

   public void setEntity(Entity var1) {
      this.entity = var1;
   }

   public BoundingBoxControlEvent(Box var1, Entity var2) {
      this.box = var1;
      this.entity = var2;
   }
}
