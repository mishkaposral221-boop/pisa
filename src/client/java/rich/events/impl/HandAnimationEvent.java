package rich.events.impl;

import net.minecraft.util.Hand;
import net.minecraft.client.util.math.MatrixStack;
import rich.events.api.events.callables.EventCancellable;

public class HandAnimationEvent extends EventCancellable {
   private MatrixStack matrices;
   private Hand hand;
   private float swingProgress;

   public HandAnimationEvent(MatrixStack var1, Hand var2, float var3) {
      this.matrices = var1;
      this.hand = var2;
      this.swingProgress = var3;
   }

   public MatrixStack getMatrices() {
      return this.matrices;
   }

   public Hand getHand() {
      return this.hand;
   }

   public float getSwingProgress() {
      return this.swingProgress;
   }

   public void setMatrices(MatrixStack var1) {
      this.matrices = var1;
   }

   public void setHand(Hand var1) {
      this.hand = var1;
   }

   public void setSwingProgress(float var1) {
      this.swingProgress = var1;
   }
}
