package rich.events.impl;

import net.minecraft.util.math.Vec3d;
import rich.events.api.events.callables.EventCancellable;

public class PlayerTravelEvent extends EventCancellable {
   private Vec3d motion;
   private final boolean pre;

   public PlayerTravelEvent(Vec3d var1, boolean var2) {
      this.motion = var1;
      this.pre = var2;
   }

   public Vec3d getMotion() {
      return this.motion;
   }

   public void setMotion(Vec3d var1) {
      this.motion = var1;
   }

   public boolean isPre() {
      return this.pre;
   }
}
