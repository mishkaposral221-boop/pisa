package rich.events.impl;

import net.minecraft.util.math.Vec3d;
import rich.events.api.events.Event;

public class CameraPositionEvent implements Event {
   private Vec3d pos;

   public Vec3d getPos() {
      return this.pos;
   }

   public void setPos(Vec3d var1) {
      this.pos = var1;
   }

   public CameraPositionEvent(Vec3d var1) {
      this.pos = var1;
   }
}
