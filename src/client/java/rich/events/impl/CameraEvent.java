package rich.events.impl;

import rich.events.api.events.callables.EventCancellable;
import rich.modules.impl.combat.aura.Angle;

public class CameraEvent extends EventCancellable {
   private boolean cameraClip;
   private float getDistance;
   private Angle angle;

   public boolean isCameraClip() {
      return this.cameraClip;
   }

   public float getDistance() {
      return this.getDistance;
   }

   public Angle getAngle() {
      return this.angle;
   }

   public void setCameraClip(boolean var1) {
      this.cameraClip = var1;
   }

   public void setDistance(float var1) {
      this.getDistance = var1;
   }

   public void setAngle(Angle var1) {
      this.angle = var1;
   }

   public CameraEvent(boolean var1, float var2, Angle var3) {
      this.cameraClip = var1;
      this.getDistance = var2;
      this.angle = var3;
   }
}
