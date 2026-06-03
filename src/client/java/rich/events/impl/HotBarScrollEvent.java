package rich.events.impl;

import rich.events.api.events.callables.EventCancellable;

public class HotBarScrollEvent extends EventCancellable {
   private double horizontal;
   private double vertical;

   public double getHorizontal() {
      return this.horizontal;
   }

   public double getVertical() {
      return this.vertical;
   }

   public void setHorizontal(double var1) {
      this.horizontal = var1;
   }

   public void setVertical(double var1) {
      this.vertical = var1;
   }

   public HotBarScrollEvent(double var1, double var3) {
      this.horizontal = var1;
      this.vertical = var3;
   }
}
