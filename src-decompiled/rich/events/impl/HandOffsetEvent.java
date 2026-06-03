package rich.events.impl;

import net.minecraft.class_1268;
import net.minecraft.class_1799;
import net.minecraft.class_4587;
import rich.events.api.events.callables.EventCancellable;

public class HandOffsetEvent extends EventCancellable {
   private class_4587 matrices;
   private class_1799 stack;
   private class_1268 hand;
   private float scale;

   public HandOffsetEvent(class_4587 var1, class_1799 var2, class_1268 var3) {
      this.matrices = var1;
      this.stack = var2;
      this.hand = var3;
      this.scale = 1.0F;
   }

   public class_4587 getMatrices() {
      return this.matrices;
   }

   public class_1799 getStack() {
      return this.stack;
   }

   public class_1268 getHand() {
      return this.hand;
   }

   public float getScale() {
      return this.scale;
   }

   public void setMatrices(class_4587 var1) {
      this.matrices = var1;
   }

   public void setStack(class_1799 var1) {
      this.stack = var1;
   }

   public void setHand(class_1268 var1) {
      this.hand = var1;
   }

   public void setScale(float var1) {
      this.scale = var1;
   }
}
