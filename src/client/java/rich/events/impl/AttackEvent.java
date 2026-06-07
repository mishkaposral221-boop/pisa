package rich.events.impl;

import net.minecraft.entity.Entity;
import rich.events.api.events.Event;

public class AttackEvent implements Event {
   private final Entity target;
   private final boolean isCrit;

   public AttackEvent(Entity var1, boolean var2) {
      this.target = var1;
      this.isCrit = var2;
   }

   public Entity getTarget() {
      return this.target;
   }

   public boolean isCrit() {
      return this.isCrit;
   }
}
