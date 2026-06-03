package rich.events.impl;

import net.minecraft.class_10185;
import rich.events.api.events.callables.EventCancellable;

public class InputEvent extends EventCancellable {
   private class_10185 input;

   public void setJumping(boolean var1) {
      this.input = new class_10185(
         this.input.comp_3159(), this.input.comp_3160(), this.input.comp_3161(), this.input.comp_3162(), var1, this.input.comp_3164(), this.input.comp_3165()
      );
   }

   public void setSprinting(boolean var1) {
      this.input = new class_10185(
         this.input.comp_3159(), this.input.comp_3160(), this.input.comp_3161(), this.input.comp_3162(), this.input.comp_3163(), this.input.comp_3164(), var1
      );
   }

   public void setDirectional(boolean var1, boolean var2, boolean var3, boolean var4, boolean var5, boolean var6, boolean var7) {
      this.input = new class_10185(var1, var2, var3, var4, var7, var5, var6);
   }

   public void setDirectionalLow(boolean var1, boolean var2, boolean var3, boolean var4) {
      this.input = new class_10185(var1, var2, var3, var4, this.input.comp_3163(), this.input.comp_3164(), this.input.comp_3165());
   }

   public void inputNone() {
      this.input = new class_10185(false, false, false, false, false, false, false);
   }

   public int forward() {
      return this.input.comp_3159() ? 1 : (this.input.comp_3160() ? -1 : 0);
   }

   public float sideways() {
      return this.input.comp_3161() ? 1.0F : (this.input.comp_3162() ? -1.0F : 0.0F);
   }

   public class_10185 getInput() {
      return this.input;
   }

   public void setInput(class_10185 var1) {
      this.input = var1;
   }

   public InputEvent(class_10185 var1) {
      this.input = var1;
   }
}
