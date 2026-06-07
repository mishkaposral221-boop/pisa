package rich.mixin;

import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.modules.impl.render.NoRender;

@Mixin(PlayerEntityRenderer.class)
public class EntityRendererMixin {

   @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
   protected void onSubmitNameTag(PlayerEntityRenderState state, MatrixStack poseStack, OrderedRenderCommandQueue collector, CameraRenderState cameraState, CallbackInfo ci) {
      NoRender noRender = NoRender.getInstance();
      if (noRender != null && noRender.isState() && noRender.modeSetting.isSelected("Nametags")) {
         ci.cancel();
      }
   }
}
