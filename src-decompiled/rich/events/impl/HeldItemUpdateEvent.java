package rich.events.impl;

import net.minecraft.class_1799;
import rich.events.api.events.Event;

public class HeldItemUpdateEvent implements Event {
   private class_1799 mainHand;
   private class_1799 offHand;

   public class_1799 getMainHand() {
      return this.mainHand;
   }

   public class_1799 getOffHand() {
      return this.offHand;
   }

   public void setMainHand(class_1799 var1) {
      this.mainHand = var1;
   }

   public void setOffHand(class_1799 var1) {
      this.offHand = var1;
   }

   public HeldItemUpdateEvent(class_1799 var1, class_1799 var2) {
      this.mainHand = var1;
      this.offHand = var2;
   }
}
