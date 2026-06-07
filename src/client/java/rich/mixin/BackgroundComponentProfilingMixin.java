package rich.mixin;

import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.modules.module.category.ModuleCategory;
import rich.screens.clickgui.impl.background.BackgroundComponent;
import rich.util.profiler.FrameProfiler;

@Mixin(BackgroundComponent.class)
public abstract class BackgroundComponentProfilingMixin {
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
   private void richProfileBackgroundStart(DrawContext context, float x, float y, ModuleCategory category, float delta, float alpha, CallbackInfo ci) {
      this.richProfileBegin("ClickGui/background/render");
   }

   @Inject(method = "render", at = @At("RETURN"), require = 0)
   private void richProfileBackgroundEnd(DrawContext context, float x, float y, ModuleCategory category, float delta, float alpha, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderSearchResults", at = @At("HEAD"), require = 0)
   private void richProfileSearchResultsStart(DrawContext context, float x, float y, float mouseX, float mouseY, int scale, float alpha, CallbackInfo ci) {
      this.richProfileBegin("ClickGui/searchResults");
   }

   @Inject(method = "renderSearchResults", at = @At("RETURN"), require = 0)
   private void richProfileSearchResultsEnd(DrawContext context, float x, float y, float mouseX, float mouseY, int scale, float alpha, CallbackInfo ci) {
      this.richProfileEnd();
   }
}
