package rich.events.impl;

import net.minecraft.class_332;
import rich.events.api.events.Event;
import rich.util.render.draw.DrawEngine;

public class DrawEvent implements Event {
   private class_332 drawContext;
   private DrawEngine drawEngine;
   private float partialTicks;

   public class_332 getDrawContext() {
      return this.drawContext;
   }

   public DrawEngine getDrawEngine() {
      return this.drawEngine;
   }

   public float getPartialTicks() {
      return this.partialTicks;
   }

   public DrawEvent(class_332 var1, DrawEngine var2, float var3) {
      this.drawContext = var1;
      this.drawEngine = var2;
      this.partialTicks = var3;
   }
}
