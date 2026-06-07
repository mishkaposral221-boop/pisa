package com.example.client.mixin;

import com.example.client.module.NoRender;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={PlayerEntityRenderer.class})
public class EntityRendererMixin {
    @Inject(method={"renderLabelIfPresent"}, at={@At(value="HEAD")}, cancellable=true)
    protected void onSubmitNameTag(PlayerEntityRenderState state, MatrixStack poseStack, OrderedRenderCommandQueue collector, CameraRenderState cameraState, CallbackInfo ci) {
        if (NoRender.getInstance() != null && NoRender.getInstance().noNametag()) {
            ci.cancel();
        }
    }
}

