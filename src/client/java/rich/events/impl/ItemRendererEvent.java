package rich.events.impl;

import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import rich.events.api.events.Event;

public class ItemRendererEvent implements Event {
   private AbstractClientPlayerEntity player;
   private ItemStack stack;
   private Hand hand;

   public AbstractClientPlayerEntity getPlayer() {
      return this.player;
   }

   public ItemStack getStack() {
      return this.stack;
   }

   public Hand getHand() {
      return this.hand;
   }

   public void setPlayer(AbstractClientPlayerEntity var1) {
      this.player = var1;
   }

   public void setStack(ItemStack var1) {
      this.stack = var1;
   }

   public void setHand(Hand var1) {
      this.hand = var1;
   }

   public ItemRendererEvent(AbstractClientPlayerEntity var1, ItemStack var2, Hand var3) {
      this.player = var1;
      this.stack = var2;
      this.hand = var3;
   }
}
