package rich.events.impl;

import net.minecraft.class_1735;
import net.minecraft.class_332;
import rich.events.api.events.Event;

public class HandledScreenEvent implements Event {
   private class_332 drawContext;
   private class_1735 slotHover;
   private int backgroundWidth;
   private int backgroundHeight;

   public class_332 getDrawContext() {
      return this.drawContext;
   }

   public class_1735 getSlotHover() {
      return this.slotHover;
   }

   public int getBackgroundWidth() {
      return this.backgroundWidth;
   }

   public int getBackgroundHeight() {
      return this.backgroundHeight;
   }

   public HandledScreenEvent(class_332 var1, class_1735 var2, int var3, int var4) {
      this.drawContext = var1;
      this.slotHover = var2;
      this.backgroundWidth = var3;
      this.backgroundHeight = var4;
   }
}
