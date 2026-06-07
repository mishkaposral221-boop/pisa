package rich.events.impl;

import net.minecraft.item.ItemStack;
import rich.events.api.events.Event;

public class HeldItemUpdateEvent implements Event {
   private ItemStack mainHand;
   private ItemStack offHand;

   public ItemStack getMainHand() {
      return this.mainHand;
   }

   public ItemStack getOffHand() {
      return this.offHand;
   }

   public void setMainHand(ItemStack var1) {
      this.mainHand = var1;
   }

   public void setOffHand(ItemStack var1) {
      this.offHand = var1;
   }

   public HeldItemUpdateEvent(ItemStack var1, ItemStack var2) {
      this.mainHand = var1;
      this.offHand = var2;
   }
}
