package rich.events.impl;

import net.minecraft.class_437;
import rich.events.api.events.callables.EventCancellable;

public class CloseScreenEvent extends EventCancellable {
   private class_437 screen;

   @Override
   public boolean equals(Object var1) {
      if (var1 == this) {
         return true;
      } else if (!(var1 instanceof CloseScreenEvent var2)) {
         return false;
      } else {
         if (!var2.canEqual(this)) {
            return false;
         }

         if (!super.equals(var1)) {
            return false;
         }

         class_437 var3 = this.getScreen();
         class_437 var4 = var2.getScreen();
         return var3 == null ? var4 == null : var3.equals(var4);
      }
   }

   protected boolean canEqual(Object var1) {
      return var1 instanceof CloseScreenEvent;
   }

   @Override
   public int hashCode() {
      byte var1 = 59;
      int var2 = super.hashCode();
      class_437 var3 = this.getScreen();
      return var2 * 59 + (var3 == null ? 43 : var3.hashCode());
   }

   public class_437 getScreen() {
      return this.screen;
   }

   public void setScreen(class_437 var1) {
      this.screen = var1;
   }

   @Override
   public String toString() {
      return "CloseScreenEvent(screen=" + this.getScreen() + ")";
   }

   public CloseScreenEvent(class_437 var1) {
      this.screen = var1;
   }
}
