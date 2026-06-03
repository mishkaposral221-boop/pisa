package rich.events.impl;

import net.minecraft.util.PlayerInput;
import rich.events.api.events.callables.EventCancellable;

public class InputEvent extends EventCancellable {
   private PlayerInput input;

   public void setJumping(boolean var1) {
      this.input = new PlayerInput(
         this.input.forward(), this.input.backward(), this.input.left(), this.input.right(), var1, this.input.sneak(), this.input.sprint()
      );
   }

   public void setSprinting(boolean var1) {
      this.input = new PlayerInput(
         this.input.forward(), this.input.backward(), this.input.left(), this.input.right(), this.input.jump(), this.input.sneak(), var1
      );
   }

   public void setDirectional(boolean var1, boolean var2, boolean var3, boolean var4, boolean var5, boolean var6, boolean var7) {
      this.input = new PlayerInput(var1, var2, var3, var4, var7, var5, var6);
   }

   public void setDirectionalLow(boolean var1, boolean var2, boolean var3, boolean var4) {
      this.input = new PlayerInput(var1, var2, var3, var4, this.input.jump(), this.input.sneak(), this.input.sprint());
   }

   public void inputNone() {
      this.input = new PlayerInput(false, false, false, false, false, false, false);
   }

   public int forward() {
      return this.input.forward() ? 1 : (this.input.backward() ? -1 : 0);
   }

   public float sideways() {
      return this.input.left() ? 1.0F : (this.input.right() ? -1.0F : 0.0F);
   }

   public PlayerInput getInput() {
      return this.input;
   }

   public void setInput(PlayerInput var1) {
      this.input = var1;
   }

   public InputEvent(PlayerInput var1) {
      this.input = var1;
   }
}
