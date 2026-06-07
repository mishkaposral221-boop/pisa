package rich.events.impl;

import net.minecraft.screen.slot.Slot;
import net.minecraft.client.gui.DrawContext;
import rich.events.api.events.Event;

public class HandledScreenEvent implements Event {
   private DrawContext drawContext;
   private Slot slotHover;
   private int backgroundWidth;
   private int backgroundHeight;

   public DrawContext getDrawContext() {
      return this.drawContext;
   }

   public Slot getSlotHover() {
      return this.slotHover;
   }

   public int getBackgroundWidth() {
      return this.backgroundWidth;
   }

   public int getBackgroundHeight() {
      return this.backgroundHeight;
   }

   public HandledScreenEvent(DrawContext var1, Slot var2, int var3, int var4) {
      this.drawContext = var1;
      this.slotHover = var2;
      this.backgroundWidth = var3;
      this.backgroundHeight = var4;
   }
}
