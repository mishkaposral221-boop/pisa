package rich.events.impl;

import net.minecraft.util.math.Vec3d;
import rich.events.api.events.callables.EventCancellable;

public class SwimmingEvent extends EventCancellable {
   Vec3d vector;

   public void setVector(Vec3d var1) {
      this.vector = var1;
   }

   public Vec3d getVector() {
      return this.vector;
   }

   public SwimmingEvent(Vec3d var1) {
      this.vector = var1;
   }
}
