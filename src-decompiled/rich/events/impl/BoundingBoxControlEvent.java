package rich.events.impl;

import net.minecraft.class_1297;
import net.minecraft.class_238;
import rich.events.api.events.callables.EventCancellable;

public class BoundingBoxControlEvent extends EventCancellable {
   public class_238 box;
   public class_1297 entity;

   public class_238 getBox() {
      return this.box;
   }

   public class_1297 getEntity() {
      return this.entity;
   }

   public void setBox(class_238 var1) {
      this.box = var1;
   }

   public void setEntity(class_1297 var1) {
      this.entity = var1;
   }

   public BoundingBoxControlEvent(class_238 var1, class_1297 var2) {
      this.box = var1;
      this.entity = var2;
   }
}
