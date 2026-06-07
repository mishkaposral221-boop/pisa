package rich.events.impl;

import net.minecraft.client.util.math.MatrixStack;
import rich.events.api.events.callables.EventCancellable;

public class GlassHandsRenderEvent extends EventCancellable {
   private GlassHandsRenderEvent.Phase phase;
   private MatrixStack matrices;
   private float tickDelta;

   public GlassHandsRenderEvent(GlassHandsRenderEvent.Phase var1, MatrixStack var2, float var3) {
      this.phase = var1;
      this.matrices = var2;
      this.tickDelta = var3;
   }

   public GlassHandsRenderEvent.Phase getPhase() {
      return this.phase;
   }

   public MatrixStack getMatrices() {
      return this.matrices;
   }

   public float getTickDelta() {
      return this.tickDelta;
   }

   public void setPhase(GlassHandsRenderEvent.Phase var1) {
      this.phase = var1;
   }

   public void setMatrices(MatrixStack var1) {
      this.matrices = var1;
   }

   public void setTickDelta(float var1) {
      this.tickDelta = var1;
   }

   public enum Phase {
      PRE,
      POST;
   }
}
