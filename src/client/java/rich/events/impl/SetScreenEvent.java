package rich.events.impl;

import net.minecraft.client.gui.screen.Screen;
import rich.events.api.events.Event;

public class SetScreenEvent implements Event {
   public Screen screen;

   public Screen getScreen() {
      return this.screen;
   }

   public void setScreen(Screen var1) {
      this.screen = var1;
   }

   public SetScreenEvent(Screen var1) {
      this.screen = var1;
   }
}
