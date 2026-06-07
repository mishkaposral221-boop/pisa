package rich.events.impl;

import net.minecraft.item.ItemStack;
import rich.events.api.events.Event;

public class HotbarItemRenderEvent implements Event {
   private ItemStack stack;
   private final int hotbarIndex;

   public HotbarItemRenderEvent(ItemStack var1, int var2) {
      this.stack = var1;
      this.hotbarIndex = var2;
   }

   public ItemStack getStack() {
      return this.stack;
   }

   public int getHotbarIndex() {
      return this.hotbarIndex;
   }

   public void setStack(ItemStack var1) {
      this.stack = var1;
   }
}
