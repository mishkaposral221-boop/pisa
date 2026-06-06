package rich.mixin;

import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.theme.ThemeRenderer;
import rich.util.profiler.FrameProfiler;

@Mixin(ThemeRenderer.class)
public abstract class ThemeRendererProfilingMixin {
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
   private void richProfileThemeRendererStart(DrawContext context, float x, float y, float width, float height, float mouseX, float mouseY, float alpha, CallbackInfo ci) {
      this.richProfileBegin("ClickGui/themeRenderer");
   }

   @Inject(method = "render", at = @At("RETURN"), require = 0)
   private void richProfileThemeRendererEnd(DrawContext context, float x, float y, float width, float height, float mouseX, float mouseY, float alpha, CallbackInfo ci) {
      this.richProfileEnd();
   }
}
