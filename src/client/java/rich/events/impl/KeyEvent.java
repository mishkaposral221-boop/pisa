package rich.events.impl;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil.Type;
import rich.IMinecraft;
import rich.events.api.events.callables.EventCancellable;

public class KeyEvent extends EventCancellable implements IMinecraft {
   private Screen screen;
   private net.minecraft.client.util.InputUtil.Type type;
   private int key;
   private int action;

   public boolean isKeyDown(int var1) {
      return this.isKeyDown(var1, mc.currentScreen == null);
   }

   public boolean isKeyDown(int var1, boolean var2) {
      return this.key == var1 && this.action == 1 && var2;
   }

   public boolean isKeyReleased(int var1) {
      return this.isKeyReleased(var1, mc.currentScreen == null);
   }

   public boolean isKeyReleased(int var1, boolean var2) {
      return this.key == var1 && this.action == 0 && var2;
   }

   public Screen getScreen() {
      return this.screen;
   }

   public net.minecraft.client.util.InputUtil.Type getType() {
      return this.type;
   }

   public int getKey() {
      return this.key;
   }

   public int getAction() {
      return this.action;
   }

   public void setScreen(Screen var1) {
      this.screen = var1;
   }

   public void setType(net.minecraft.client.util.InputUtil.Type var1) {
      this.type = var1;
   }

   public void setKey(int var1) {
      this.key = var1;
   }

   public void setAction(int var1) {
      this.action = var1;
   }

   public KeyEvent(Screen var1, net.minecraft.client.util.InputUtil.Type var2, int var3, int var4) {
      this.screen = var1;
      this.type = var2;
      this.key = var3;
      this.action = var4;
   }
}
