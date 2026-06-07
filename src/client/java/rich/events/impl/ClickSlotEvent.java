package rich.events.impl;

import net.minecraft.screen.slot.SlotActionType;
import rich.events.api.events.callables.EventCancellable;

public class ClickSlotEvent extends EventCancellable {
   private int windowId;
   private int slotId;
   private int button;
   private SlotActionType actionType;

   public int getWindowId() {
      return this.windowId;
   }

   public int getSlotId() {
      return this.slotId;
   }

   public int getButton() {
      return this.button;
   }

   public SlotActionType getActionType() {
      return this.actionType;
   }

   public void setWindowId(int var1) {
      this.windowId = var1;
   }

   public void setSlotId(int var1) {
      this.slotId = var1;
   }

   public void setButton(int var1) {
      this.button = var1;
   }

   public void setActionType(SlotActionType var1) {
      this.actionType = var1;
   }

   public ClickSlotEvent(int var1, int var2, int var3, SlotActionType var4) {
      this.windowId = var1;
      this.slotId = var2;
      this.button = var3;
      this.actionType = var4;
   }
}
