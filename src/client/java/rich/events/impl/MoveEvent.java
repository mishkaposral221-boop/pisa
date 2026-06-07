package rich.events.impl;

import net.minecraft.util.math.Vec3d;
import rich.events.api.events.Event;

public class MoveEvent implements Event {
   private Vec3d movement;

   public Vec3d getMovement() {
      return this.movement;
   }

   public void setMovement(Vec3d var1) {
      this.movement = var1;
   }

   @Override
   public boolean equals(Object var1) {
      if (var1 == this) {
         return true;
      } else if (!(var1 instanceof MoveEvent var2)) {
         return false;
      } else {
         if (!var2.canEqual(this)) {
            return false;
         }

         Vec3d var3 = this.getMovement();
         Vec3d var4 = var2.getMovement();
         return var3 == null ? var4 == null : var3.equals(var4);
      }
   }

   protected boolean canEqual(Object var1) {
      return var1 instanceof MoveEvent;
   }

   @Override
   public int hashCode() {
      byte var1 = 59;
      byte var2 = 1;
      Vec3d var3 = this.getMovement();
      return var2 * 59 + (var3 == null ? 43 : var3.hashCode());
   }

   @Override
   public String toString() {
      return "MoveEvent(movement=" + this.getMovement() + ")";
   }

   public MoveEvent(Vec3d var1) {
      this.movement = var1;
   }
}
