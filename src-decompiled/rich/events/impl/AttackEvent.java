package rich.events.impl;

import net.minecraft.class_1297;
import rich.events.api.events.Event;

public class AttackEvent implements Event {
   private final class_1297 target;
   private final boolean isCrit;

   public AttackEvent(class_1297 var1, boolean var2) {
      this.target = var1;
      this.isCrit = var2;
   }

   public class_1297 getTarget() {
      return this.target;
   }

   public boolean isCrit() {
      return this.isCrit;
   }
}
