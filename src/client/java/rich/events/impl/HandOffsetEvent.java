package rich.events.impl;

import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;
import net.minecraft.client.util.math.MatrixStack;
import rich.events.api.events.callables.EventCancellable;

public class HandOffsetEvent extends EventCancellable {
   private MatrixStack matrices;
   private ItemStack stack;
   private Hand hand;
   private float scale;

   public HandOffsetEvent(MatrixStack var1, ItemStack var2, Hand var3) {
      this.matrices = var1;
      this.stack = var2;
      this.hand = var3;
      this.scale = 1.0F;
   }

   public MatrixStack getMatrices() {
      return this.matrices;
   }

   public ItemStack getStack() {
      return this.stack;
   }

   public Hand getHand() {
      return this.hand;
   }

   public float getScale() {
      return this.scale;
   }

   public void setMatrices(MatrixStack var1) {
      this.matrices = var1;
   }

   public void setStack(ItemStack var1) {
      this.stack = var1;
   }

   public void setHand(Hand var1) {
      this.hand = var1;
   }

   public void setScale(float var1) {
      this.scale = var1;
   }
}
