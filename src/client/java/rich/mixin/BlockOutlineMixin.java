package rich.mixin;

import net.minecraft.client.render.state.OutlineRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.modules.impl.render.BlockOverlay;

@Mixin(WorldRenderer.class)
public class BlockOutlineMixin {
   @Inject(method = "drawBlockOutline", at = @At("HEAD"), cancellable = true)
   private void onDrawBlockOutline(
      MatrixStack var1, VertexConsumer var2, double var3, double var5, double var7, OutlineRenderState var9, int var10, float var11, CallbackInfo var12
   ) {
      BlockOverlay var13 = BlockOverlay.getInstance();
      if (var13 != null && var13.isState()) {
         var12.cancel();
      }
   }
}
