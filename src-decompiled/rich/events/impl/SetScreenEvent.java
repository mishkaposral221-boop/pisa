package rich.events.impl;

import net.minecraft.class_437;
import rich.events.api.events.Event;

public class SetScreenEvent implements Event {
   public class_437 screen;

   public class_437 getScreen() {
      return this.screen;
   }

   public void setScreen(class_437 var1) {
      this.screen = var1;
   }

   public SetScreenEvent(class_437 var1) {
      this.screen = var1;
   }
}
