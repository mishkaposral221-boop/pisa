package rich.events.impl;

import net.minecraft.client.util.math.MatrixStack;
import rich.events.api.events.Event;

public class WorldRenderEvent implements Event {
   private MatrixStack stack;
   private float partialTicks;

   public WorldRenderEvent(MatrixStack var1, float var2) {
      this.stack = var1;
      this.partialTicks = var2;
   }

   public MatrixStack getStack() {
      return this.stack;
   }

   public float getPartialTicks() {
      return this.partialTicks;
   }
}
