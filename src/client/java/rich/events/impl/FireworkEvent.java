package rich.events.impl;

import net.minecraft.util.math.Vec3d;
import rich.events.api.events.Event;

public class FireworkEvent implements Event {
   public Vec3d vector;

   public FireworkEvent(Vec3d var1) {
      this.vector = var1;
   }

   public Vec3d getVector() {
      return this.vector;
   }

   public void setVector(Vec3d var1) {
      this.vector = var1;
   }
}
