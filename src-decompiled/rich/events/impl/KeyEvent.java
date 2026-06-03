package rich.events.impl;

import net.minecraft.class_437;
import net.minecraft.class_3675.class_307;
import rich.IMinecraft;
import rich.events.api.events.callables.EventCancellable;

public class KeyEvent extends EventCancellable implements IMinecraft {
   private class_437 screen;
   private class_307 type;
   private int key;
   private int action;

   public boolean isKeyDown(int var1) {
      return this.isKeyDown(var1, mc.field_1755 == null);
   }

   public boolean isKeyDown(int var1, boolean var2) {
      return this.key == var1 && this.action == 1 && var2;
   }

   public boolean isKeyReleased(int var1) {
      return this.isKeyReleased(var1, mc.field_1755 == null);
   }

   public boolean isKeyReleased(int var1, boolean var2) {
      return this.key == var1 && this.action == 0 && var2;
   }

   public class_437 getScreen() {
      return this.screen;
   }

   public class_307 getType() {
      return this.type;
   }

   public int getKey() {
      return this.key;
   }

   public int getAction() {
      return this.action;
   }

   public void setScreen(class_437 var1) {
      this.screen = var1;
   }

   public void setType(class_307 var1) {
      this.type = var1;
   }

   public void setKey(int var1) {
      this.key = var1;
   }

   public void setAction(int var1) {
      this.action = var1;
   }

   public KeyEvent(class_437 var1, class_307 var2, int var3, int var4) {
      this.screen = var1;
      this.type = var2;
      this.key = var3;
      this.action = var4;
   }
}
