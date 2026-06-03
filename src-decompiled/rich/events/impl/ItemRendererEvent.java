package rich.events.impl;

import net.minecraft.class_1268;
import net.minecraft.class_1799;
import net.minecraft.class_742;
import rich.events.api.events.Event;

public class ItemRendererEvent implements Event {
   private class_742 player;
   private class_1799 stack;
   private class_1268 hand;

   public class_742 getPlayer() {
      return this.player;
   }

   public class_1799 getStack() {
      return this.stack;
   }

   public class_1268 getHand() {
      return this.hand;
   }

   public void setPlayer(class_742 var1) {
      this.player = var1;
   }

   public void setStack(class_1799 var1) {
      this.stack = var1;
   }

   public void setHand(class_1268 var1) {
      this.hand = var1;
   }

   public ItemRendererEvent(class_742 var1, class_1799 var2, class_1268 var3) {
      this.player = var1;
      this.stack = var2;
      this.hand = var3;
   }
}
