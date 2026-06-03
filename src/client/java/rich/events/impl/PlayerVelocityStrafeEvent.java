package rich.events.impl;

import net.minecraft.util.math.Vec3d;
import rich.events.api.events.Event;

public class PlayerVelocityStrafeEvent implements Event {
   private final Vec3d movementInput;
   private final float speed;
   private final float yaw;
   private Vec3d velocity;

   public Vec3d getMovementInput() {
      return this.movementInput;
   }

   public float getSpeed() {
      return this.speed;
   }

   public float getYaw() {
      return this.yaw;
   }

   public Vec3d getVelocity() {
      return this.velocity;
   }

   public void setVelocity(Vec3d var1) {
      this.velocity = var1;
   }

   public PlayerVelocityStrafeEvent(Vec3d var1, float var2, float var3, Vec3d var4) {
      this.movementInput = var1;
      this.speed = var2;
      this.yaw = var3;
      this.velocity = var4;
   }
}
