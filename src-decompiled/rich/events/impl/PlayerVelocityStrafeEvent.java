package rich.events.impl;

import net.minecraft.class_243;
import rich.events.api.events.Event;

public class PlayerVelocityStrafeEvent implements Event {
   private final class_243 movementInput;
   private final float speed;
   private final float yaw;
   private class_243 velocity;

   public class_243 getMovementInput() {
      return this.movementInput;
   }

   public float getSpeed() {
      return this.speed;
   }

   public float getYaw() {
      return this.yaw;
   }

   public class_243 getVelocity() {
      return this.velocity;
   }

   public void setVelocity(class_243 var1) {
      this.velocity = var1;
   }

   public PlayerVelocityStrafeEvent(class_243 var1, float var2, float var3, class_243 var4) {
      this.movementInput = var1;
      this.speed = var2;
      this.yaw = var3;
      this.velocity = var4;
   }
}
