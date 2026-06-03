package rich.mixin;

import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.modules.impl.render.NoRender;

@Mixin(InGameOverlayRenderer.class)
public class InGameOverlayRendererMixin {
   @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
   private static void renderFireOverlayHook(MatrixStack var0, VertexConsumerProvider var1, Sprite var2, CallbackInfo var3) {
      NoRender var4 = NoRender.getInstance();
      if (var4.isState() && var4.modeSetting.isSelected("Fire")) {
         var3.cancel();
      }
   }

   @Inject(method = "renderInWallOverlay", at = @At("HEAD"), cancellable = true)
   private static void renderInWallOverlayHook(Sprite var0, MatrixStack var1, VertexConsumerProvider var2, CallbackInfo var3) {
      NoRender var4 = NoRender.getInstance();
      if (var4.isState() && var4.modeSetting.isSelected("Block Overlay")) {
         var3.cancel();
      }
   }
}
