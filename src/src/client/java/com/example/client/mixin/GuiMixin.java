package com.example.client.mixin;

import com.example.client.module.NoRender;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={InGameOverlayRenderer.class})
public class GuiMixin {
    @Inject(method={"renderFireOverlay"}, at={@At(value="HEAD")}, cancellable=true)
    private static void onRenderFire(MatrixStack poseStack, VertexConsumerProvider bufferSource, Sprite sprite, CallbackInfo ci) {
        NoRender nr = NoRender.getInstance();
        if (nr != null && nr.noFire()) {
            ci.cancel();
        }
    }
}

