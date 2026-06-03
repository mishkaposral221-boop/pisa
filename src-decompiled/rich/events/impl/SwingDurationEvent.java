package rich.events.impl;

import rich.events.api.events.callables.EventCancellable;

public class SwingDurationEvent extends EventCancellable {
   private float animation;

   public float getAnimation() {
      return this.animation;
   }

   public void setAnimation(float var1) {
      this.animation = var1;
   }
}
