package rich.events.impl;

import net.minecraft.client.gui.DrawContext;
import rich.events.api.events.Event;
import rich.util.render.draw.DrawEngine;

public class DrawEvent implements Event {
   private DrawContext drawContext;
   private DrawEngine drawEngine;
   private float partialTicks;

   public DrawContext getDrawContext() {
      return this.drawContext;
   }

   public DrawEngine getDrawEngine() {
      return this.drawEngine;
   }

   public float getPartialTicks() {
      return this.partialTicks;
   }

   public DrawEvent(DrawContext var1, DrawEngine var2, float var3) {
      this.drawContext = var1;
      this.drawEngine = var2;
      this.partialTicks = var3;
   }
}
