package rich.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.util.profiler.FrameProfiler;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientRenderBreakdownMixin {
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

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;executePendingTasks()V"), require = 0)
   private void richPendingTasksStart(boolean tick, CallbackInfo ci) {
      this.richBeginSection("Minecraft/MinecraftClient.executePendingTasks");
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;executePendingTasks()V", shift = Shift.AFTER), require = 0)
   private void richPendingTasksEnd(boolean tick, CallbackInfo ci) {
      this.richEndSection();
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sound/SoundManager;updateListenerPosition(Lnet/minecraft/client/render/Camera;)V"), require = 0)
   private void richSoundListenerStart(boolean tick, CallbackInfo ci) {
      this.richBeginSection("Minecraft/MinecraftClient.soundListener");
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sound/SoundManager;updateListenerPosition(Lnet/minecraft/client/render/Camera;)V", shift = Shift.AFTER), require = 0)
   private void richSoundListenerEnd(boolean tick, CallbackInfo ci) {
      this.richEndSection();
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/toast/ToastManager;update()V"), require = 0)
   private void richToastUpdateStart(boolean tick, CallbackInfo ci) {
      this.richBeginSection("Minecraft/MinecraftClient.toastUpdate");
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/toast/ToastManager;update()V", shift = Shift.AFTER), require = 0)
   private void richToastUpdateEnd(boolean tick, CallbackInfo ci) {
      this.richEndSection();
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Mouse;tick()V"), require = 0)
   private void richMouseTickStart(boolean tick, CallbackInfo ci) {
      this.richBeginSection("Minecraft/MinecraftClient.mouseTick");
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Mouse;tick()V", shift = Shift.AFTER), require = 0)
   private void richMouseTickEnd(boolean tick, CallbackInfo ci) {
      this.richEndSection();
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;render(Lnet/minecraft/client/render/RenderTickCounter;Z)V"), require = 0)
   private void richGameRendererCallStart(boolean tick, CallbackInfo ci) {
      this.richBeginSection("Minecraft/MinecraftClient.gameRenderer.renderCall");
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;render(Lnet/minecraft/client/render/RenderTickCounter;Z)V", shift = Shift.AFTER), require = 0)
   private void richGameRendererCallEnd(boolean tick, CallbackInfo ci) {
      this.richEndSection();
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/Framebuffer;blitToScreen()V"), require = 0)
   private void richBlitToScreenStart(boolean tick, CallbackInfo ci) {
      this.richBeginSection("Minecraft/MinecraftClient.framebuffer.blitToScreen");
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/Framebuffer;blitToScreen()V", shift = Shift.AFTER), require = 0)
   private void richBlitToScreenEnd(boolean tick, CallbackInfo ci) {
      this.richEndSection();
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;swapBuffers(Lnet/minecraft/client/util/tracy/TracyFrameCapturer;)V"), require = 0)
   private void richSwapBuffersStart(boolean tick, CallbackInfo ci) {
      this.richBeginSection("Minecraft/MinecraftClient.window.swapBuffers");
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;swapBuffers(Lnet/minecraft/client/util/tracy/TracyFrameCapturer;)V", shift = Shift.AFTER), require = 0)
   private void richSwapBuffersEnd(boolean tick, CallbackInfo ci) {
      this.richEndSection();
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;limitDisplayFPS(I)V"), require = 0)
   private void richLimitFpsStart(boolean tick, CallbackInfo ci) {
      this.richBeginSection("Minecraft/MinecraftClient.limitDisplayFPS");
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;limitDisplayFPS(I)V", shift = Shift.AFTER), require = 0)
   private void richLimitFpsEnd(boolean tick, CallbackInfo ci) {
      this.richEndSection();
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;yield()V"), require = 0)
   private void richThreadYieldStart(boolean tick, CallbackInfo ci) {
      this.richBeginSection("Minecraft/MinecraftClient.threadYield");
   }

   @Inject(method = "render", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;yield()V", shift = Shift.AFTER), require = 0)
   private void richThreadYieldEnd(boolean tick, CallbackInfo ci) {
      this.richEndSection();
   }
}
