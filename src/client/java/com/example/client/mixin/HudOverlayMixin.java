package com.example.client.mixin;

import com.example.client.module.NoRender;
import net.minecraft.entity.Entity;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={InGameHud.class})
public class HudOverlayMixin {
    @Inject(method={"renderNauseaOverlay"}, at={@At(value="HEAD")}, cancellable=true)
    private void onConfusion(DrawContext g, float f, CallbackInfo ci) {
        NoRender nr = NoRender.getInstance();
        if (nr != null && nr.noNausea()) {
            ci.cancel();
        }
    }

    @Inject(method={"renderPortalOverlay"}, at={@At(value="HEAD")}, cancellable=true)
    private void onPortal(DrawContext g, float f, CallbackInfo ci) {
        NoRender nr = NoRender.getInstance();
        if (nr != null && nr.noNausea()) {
            ci.cancel();
        }
    }

    @Inject(method={"renderVignetteOverlay"}, at={@At(value="HEAD")}, cancellable=true)
    private void onVignette(DrawContext g, Entity entity, CallbackInfo ci) {
        NoRender nr = NoRender.getInstance();
        if (nr != null && nr.noDarkness()) {
            ci.cancel();
        }
    }
}

