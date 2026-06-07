package rich.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.util.profiler.FrameProfiler;

@Mixin(InGameHud.class)
public abstract class InGameHudProfilingMixin {
   @Unique
   private void richProfileBegin(String name) {
      FrameProfiler profiler = FrameProfiler.getInstance();
      if (profiler.isEnabled()) {
         profiler.begin(name);
      }
   }

   @Unique
   private void richProfileEnd() {
      FrameProfiler profiler = FrameProfiler.getInstance();
      if (profiler.isEnabled()) {
         profiler.end();
      }
   }

   @Inject(method = "render", at = @At("HEAD"), require = 0)
   private void richProfileRenderStart(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/InGameHud.render/total");
   }

   @Inject(method = "render", at = @At("RETURN"), require = 0)
   private void richProfileRenderEnd(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderMiscOverlays", at = @At("HEAD"), require = 0)
   private void richProfileMiscOverlaysStart(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/InGameHud/renderMiscOverlays");
   }

   @Inject(method = "renderMiscOverlays", at = @At("RETURN"), require = 0)
   private void richProfileMiscOverlaysEnd(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderCrosshair", at = @At("HEAD"), require = 0)
   private void richProfileCrosshairStart(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/InGameHud/renderCrosshair");
   }

   @Inject(method = "renderCrosshair", at = @At("RETURN"), require = 0)
   private void richProfileCrosshairEnd(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderMainHud", at = @At("HEAD"), require = 0)
   private void richProfileMainHudStart(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/InGameHud/renderMainHud");
   }

   @Inject(method = "renderMainHud", at = @At("RETURN"), require = 0)
   private void richProfileMainHudEnd(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderHotbar", at = @At("HEAD"), require = 0)
   private void richProfileHotbarStart(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/InGameHud/renderHotbar");
   }

   @Inject(method = "renderHotbar", at = @At("RETURN"), require = 0)
   private void richProfileHotbarEnd(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderStatusBars", at = @At("HEAD"), require = 0)
   private void richProfileStatusBarsStart(DrawContext context, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/InGameHud/renderStatusBars");
   }

   @Inject(method = "renderStatusBars", at = @At("RETURN"), require = 0)
   private void richProfileStatusBarsEnd(DrawContext context, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderMountHealth", at = @At("HEAD"), require = 0)
   private void richProfileMountHealthStart(DrawContext context, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/InGameHud/renderMountHealth");
   }

   @Inject(method = "renderMountHealth", at = @At("RETURN"), require = 0)
   private void richProfileMountHealthEnd(DrawContext context, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderHeldItemTooltip", at = @At("HEAD"), require = 0)
   private void richProfileHeldItemTooltipStart(DrawContext context, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/InGameHud/renderHeldItemTooltip");
   }

   @Inject(method = "renderHeldItemTooltip", at = @At("RETURN"), require = 0)
   private void richProfileHeldItemTooltipEnd(DrawContext context, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), require = 0)
   private void richProfileStatusEffectOverlayStart(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/InGameHud/renderStatusEffectOverlay");
   }

   @Inject(method = "renderStatusEffectOverlay", at = @At("RETURN"), require = 0)
   private void richProfileStatusEffectOverlayEnd(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderBossBarHud", at = @At("HEAD"), require = 0)
   private void richProfileBossBarStart(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/InGameHud/renderBossBarHud");
   }

   @Inject(method = "renderBossBarHud", at = @At("RETURN"), require = 0)
   private void richProfileBossBarEnd(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderSleepOverlay", at = @At("HEAD"), require = 0)
   private void richProfileSleepOverlayStart(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/InGameHud/renderSleepOverlay");
   }

   @Inject(method = "renderSleepOverlay", at = @At("RETURN"), require = 0)
   private void richProfileSleepOverlayEnd(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderDemoTimer", at = @At("HEAD"), require = 0)
   private void richProfileDemoTimerStart(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/InGameHud/renderDemoTimer");
   }

   @Inject(method = "renderDemoTimer", at = @At("RETURN"), require = 0)
   private void richProfileDemoTimerEnd(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", at = @At("HEAD"), require = 0)
   private void richProfileScoreboardStart(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/InGameHud/renderScoreboardSidebar");
   }

   @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", at = @At("RETURN"), require = 0)
   private void richProfileScoreboardEnd(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderOverlayMessage", at = @At("HEAD"), require = 0)
   private void richProfileOverlayMessageStart(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/InGameHud/renderOverlayMessage");
   }

   @Inject(method = "renderOverlayMessage", at = @At("RETURN"), require = 0)
   private void richProfileOverlayMessageEnd(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderTitleAndSubtitle", at = @At("HEAD"), require = 0)
   private void richProfileTitleStart(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/InGameHud/renderTitleAndSubtitle");
   }

   @Inject(method = "renderTitleAndSubtitle", at = @At("RETURN"), require = 0)
   private void richProfileTitleEnd(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderChat", at = @At("HEAD"), require = 0)
   private void richProfileChatStart(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/InGameHud/renderChat");
   }

   @Inject(method = "renderChat", at = @At("RETURN"), require = 0)
   private void richProfileChatEnd(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderPlayerList", at = @At("HEAD"), require = 0)
   private void richProfilePlayerListStart(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/InGameHud/renderPlayerList");
   }

   @Inject(method = "renderPlayerList", at = @At("RETURN"), require = 0)
   private void richProfilePlayerListEnd(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderSubtitlesHud", at = @At("HEAD"), require = 0)
   private void richProfileSubtitlesStart(DrawContext context, boolean defer, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/InGameHud/renderSubtitlesHud");
   }

   @Inject(method = "renderSubtitlesHud", at = @At("RETURN"), require = 0)
   private void richProfileSubtitlesEnd(DrawContext context, boolean defer, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderDebugHud", at = @At("HEAD"), require = 0)
   private void richProfileDebugHudStart(DrawContext context, CallbackInfo ci) {
      this.richProfileBegin("Minecraft/InGameHud/renderDebugHud");
   }

   @Inject(method = "renderDebugHud", at = @At("RETURN"), require = 0)
   private void richProfileDebugHudEnd(DrawContext context, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderDeferredSubtitles", at = @At("HEAD"), require = 0)
   private void richProfileDeferredSubtitlesStart(CallbackInfo ci) {
      this.richProfileBegin("Minecraft/InGameHud/renderDeferredSubtitles");
   }

   @Inject(method = "renderDeferredSubtitles", at = @At("RETURN"), require = 0)
   private void richProfileDeferredSubtitlesEnd(CallbackInfo ci) {
      this.richProfileEnd();
   }
}
