package rich.mixin;

import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.modules.module.category.ModuleCategory;
import rich.screens.clickgui.impl.configs.ConfigsRenderer;
import rich.util.profiler.FrameProfiler;

@Mixin(ConfigsRenderer.class)
public abstract class ConfigsRendererProfilingMixin {
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
   private void richProfileConfigsRendererStart(DrawContext context, float x, float y, float mouseX, float mouseY, float delta, int scale, float alpha, ModuleCategory category, CallbackInfo ci) {
      this.richProfileBegin("ClickGui/configsRenderer");
   }

   @Inject(method = "render", at = @At("RETURN"), require = 0)
   private void richProfileConfigsRendererEnd(DrawContext context, float x, float y, float mouseX, float mouseY, float delta, int scale, float alpha, ModuleCategory category, CallbackInfo ci) {
      this.richProfileEnd();
   }
}
