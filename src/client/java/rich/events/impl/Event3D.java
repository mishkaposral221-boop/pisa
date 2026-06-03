package rich.events.impl;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import rich.events.api.events.Event;

public class Event3D implements Event {
   public MatrixStack stack;
   public VertexConsumerProvider buffer;

   public Event3D(MatrixStack var1, VertexConsumerProvider var2) {
      this.stack = var1;
      this.buffer = var2;
   }
}
