package rich.mixin;

import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.screens.clickgui.impl.module.ModuleComponent;
import rich.util.profiler.FrameProfiler;

@Mixin(ModuleComponent.class)
public abstract class ModuleComponentProfilingMixin {
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

   @Inject(method = "updateScroll", at = @At("HEAD"), require = 0)
   private void richProfileUpdateScrollStart(float delta, float speed, CallbackInfo ci) {
      this.richProfileBegin("ClickGui/module/updateScroll");
   }

   @Inject(method = "updateScroll", at = @At("RETURN"), require = 0)
   private void richProfileUpdateScrollEnd(float delta, float speed, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "updateScrollFades", at = @At("HEAD"), require = 0)
   private void richProfileUpdateScrollFadesStart(float delta, float speed, float listHeight, float settingHeight, CallbackInfo ci) {
      this.richProfileBegin("ClickGui/module/updateScrollFades");
   }

   @Inject(method = "updateScrollFades", at = @At("RETURN"), require = 0)
   private void richProfileUpdateScrollFadesEnd(float delta, float speed, float listHeight, float settingHeight, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderModuleList", at = @At("HEAD"), require = 0)
   private void richProfileModuleListStart(DrawContext context, float x, float y, float width, float height, float mouseX, float mouseY, int scale, float alpha, CallbackInfo ci) {
      this.richProfileBegin("ClickGui/moduleList");
   }

   @Inject(method = "renderModuleList", at = @At("RETURN"), require = 0)
   private void richProfileModuleListEnd(DrawContext context, float x, float y, float width, float height, float mouseX, float mouseY, int scale, float alpha, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderSettingsPanel", at = @At("HEAD"), require = 0)
   private void richProfileSettingsPanelStart(DrawContext context, float x, float y, float width, float height, float mouseX, float mouseY, float delta, int scale, float alpha, CallbackInfo ci) {
      this.richProfileBegin("ClickGui/settingsPanel/renderContents");
   }

   @Inject(method = "renderSettingsPanel", at = @At("RETURN"), require = 0)
   private void richProfileSettingsPanelEnd(DrawContext context, float x, float y, float width, float height, float mouseX, float mouseY, float delta, int scale, float alpha, CallbackInfo ci) {
      this.richProfileEnd();
   }
}
