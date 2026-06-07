package rich.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.util.profiler.FrameProfiler;

@Mixin(GameRenderer.class)
public abstract class GameRendererRenderBreakdownMixin {
   @Unique
   private void richBeginSection(String name) {
      FrameProfiler profiler = FrameProfiler.getInstance();
      if (profiler.isEnabled()) {
         profiler.begin(name);
      }
   }

   @Unique
   private void richEndSection() {
      FrameProfiler profiler = FrameProfiler.getInstance();
      if (profiler.isEnabled()) {
         profiler.end();
      }
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;drawEntityOutlinesFramebuffer()V"), require = 0)
   private void richDrawEntityOutlinesStart(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richBeginSection("Minecraft/GameRenderer.drawEntityOutlinesFramebuffer");
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;drawEntityOutlinesFramebuffer()V", shift = Shift.AFTER), require = 0)
   private void richDrawEntityOutlinesEnd(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richEndSection();
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/PostEffectProcessor;render(Lnet/minecraft/client/gl/Framebuffer;Lnet/minecraft/client/util/Pool;)V"), require = 0)
   private void richPostProcessorStart(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richBeginSection("Minecraft/GameRenderer.postProcessorRender");
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/PostEffectProcessor;render(Lnet/minecraft/client/gl/Framebuffer;Lnet/minecraft/client/util/Pool;)V", shift = Shift.AFTER), require = 0)
   private void richPostProcessorEnd(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richEndSection();
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/fog/FogRenderer;rotate()V"), require = 0)
   private void richFogRotateStart(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richBeginSection("Minecraft/GameRenderer.fogRotate");
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/fog/FogRenderer;rotate()V", shift = Shift.AFTER), require = 0)
   private void richFogRotateEnd(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richEndSection();
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"), require = 0)
   private void richInGameHudRenderStart(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richBeginSection("Minecraft/GameRenderer.inGameHud.render");
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", shift = Shift.AFTER), require = 0)
   private void richInGameHudRenderEnd(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richEndSection();
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Overlay;render(Lnet/minecraft/client/gui/DrawContext;IIF)V"), require = 0)
   private void richOverlayRenderStart(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richBeginSection("Minecraft/GameRenderer.overlay.render");
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Overlay;render(Lnet/minecraft/client/gui/DrawContext;IIF)V", shift = Shift.AFTER), require = 0)
   private void richOverlayRenderEnd(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richEndSection();
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;renderWithTooltip(Lnet/minecraft/client/gui/DrawContext;IIF)V"), require = 0)
   private void richScreenRenderStart(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richBeginSection("Minecraft/GameRenderer.screen.renderWithTooltip");
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;renderWithTooltip(Lnet/minecraft/client/gui/DrawContext;IIF)V", shift = Shift.AFTER), require = 0)
   private void richScreenRenderEnd(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richEndSection();
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderAutosaveIndicator(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"), require = 0)
   private void richAutosaveRenderStart(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richBeginSection("Minecraft/GameRenderer.inGameHud.autosave");
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderAutosaveIndicator(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", shift = Shift.AFTER), require = 0)
   private void richAutosaveRenderEnd(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richEndSection();
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/toast/ToastManager;draw(Lnet/minecraft/client/gui/DrawContext;)V"), require = 0)
   private void richToastDrawStart(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richBeginSection("Minecraft/GameRenderer.toastManager.draw");
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/toast/ToastManager;draw(Lnet/minecraft/client/gui/DrawContext;)V", shift = Shift.AFTER), require = 0)
   private void richToastDrawEnd(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richEndSection();
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderDebugHud(Lnet/minecraft/client/gui/DrawContext;)V"), require = 0)
   private void richDebugHudStart(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richBeginSection("Minecraft/GameRenderer.inGameHud.debugHud");
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderDebugHud(Lnet/minecraft/client/gui/DrawContext;)V", shift = Shift.AFTER), require = 0)
   private void richDebugHudEnd(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richEndSection();
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderDeferredSubtitles()V"), require = 0)
   private void richDeferredSubtitlesStart(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richBeginSection("Minecraft/GameRenderer.inGameHud.deferredSubtitles");
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderDeferredSubtitles()V", shift = Shift.AFTER), require = 0)
   private void richDeferredSubtitlesEnd(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richEndSection();
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V"), require = 0)
   private void richGuiRendererStart(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richBeginSection("Minecraft/GameRenderer.guiRenderer.render");
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V", shift = Shift.AFTER), require = 0)
   private void richGuiRendererEnd(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richEndSection();
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;incrementFrame()V"), require = 0)
   private void richGuiIncrementStart(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richBeginSection("Minecraft/GameRenderer.guiRenderer.incrementFrame");
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;incrementFrame()V", shift = Shift.AFTER), require = 0)
   private void richGuiIncrementEnd(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richEndSection();
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;applyCursorTo(Lnet/minecraft/client/util/Window;)V"), require = 0)
   private void richApplyCursorStart(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richBeginSection("Minecraft/GameRenderer.drawContext.applyCursor");
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;applyCursorTo(Lnet/minecraft/client/util/Window;)V", shift = Shift.AFTER), require = 0)
   private void richApplyCursorEnd(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richEndSection();
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/command/OrderedRenderCommandQueueImpl;onNextFrame()V"), require = 0)
   private void richOrderedQueueNextFrameStart(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richBeginSection("Minecraft/GameRenderer.orderedQueue.onNextFrame");
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/command/OrderedRenderCommandQueueImpl;onNextFrame()V", shift = Shift.AFTER), require = 0)
   private void richOrderedQueueNextFrameEnd(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richEndSection();
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/command/RenderDispatcher;endLayeredCustoms()V"), require = 0)
   private void richEndLayeredCustomsStart(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richBeginSection("Minecraft/GameRenderer.entityDispatcher.endLayeredCustoms");
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/command/RenderDispatcher;endLayeredCustoms()V", shift = Shift.AFTER), require = 0)
   private void richEndLayeredCustomsEnd(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richEndSection();
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Pool;decrementLifespan()V"), require = 0)
   private void richPoolDecrementStart(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richBeginSection("Minecraft/GameRenderer.pool.decrementLifespan");
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Pool;decrementLifespan()V", shift = Shift.AFTER), require = 0)
   private void richPoolDecrementEnd(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      this.richEndSection();
   }
}
