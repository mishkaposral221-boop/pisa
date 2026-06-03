package rich.events.impl;

import net.minecraft.class_1268;
import net.minecraft.class_4587;
import rich.events.api.events.callables.EventCancellable;

public class HandAnimationEvent extends EventCancellable {
   private class_4587 matrices;
   private class_1268 hand;
   private float swingProgress;

   public HandAnimationEvent(class_4587 var1, class_1268 var2, float var3) {
      this.matrices = var1;
      this.hand = var2;
      this.swingProgress = var3;
   }

   public class_4587 getMatrices() {
      return this.matrices;
   }

   public class_1268 getHand() {
      return this.hand;
   }

   public float getSwingProgress() {
      return this.swingProgress;
   }

   public void setMatrices(class_4587 var1) {
      this.matrices = var1;
   }

   public void setHand(class_1268 var1) {
      this.hand = var1;
   }

   public void setSwingProgress(float var1) {
      this.swingProgress = var1;
   }
}
