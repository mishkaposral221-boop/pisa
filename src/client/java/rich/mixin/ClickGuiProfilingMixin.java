package rich.mixin;

import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.modules.module.category.ModuleCategory;
import rich.screens.clickgui.ClickGui;
import rich.util.profiler.FrameProfiler;

@Mixin(ClickGui.class)
public abstract class ClickGuiProfilingMixin {
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

   @Inject(method = "renderCategoryBar", at = @At("HEAD"), require = 0)
   private void richProfileCategoryBarStart(float x, float y, ModuleCategory category, float alpha, float mouseX, float mouseY, CallbackInfo ci) {
      this.richProfileBegin("ClickGui/categoryBar");
   }

   @Inject(method = "renderCategoryBar", at = @At("RETURN"), require = 0)
   private void richProfileCategoryBarEnd(float x, float y, ModuleCategory category, float alpha, float mouseX, float mouseY, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderSearchBar", at = @At("HEAD"), require = 0)
   private void richProfileSearchBarStart(float x, float y, float alpha, float mouseX, float mouseY, CallbackInfo ci) {
      this.richProfileBegin("ClickGui/searchBar");
   }

   @Inject(method = "renderSearchBar", at = @At("RETURN"), require = 0)
   private void richProfileSearchBarEnd(float x, float y, float alpha, float mouseX, float mouseY, CallbackInfo ci) {
      this.richProfileEnd();
   }

   @Inject(method = "renderSettingsPanelExternal", at = @At("HEAD"), require = 0)
   private void richProfileSettingsPanelStart(DrawContext context, float x, float y, float mouseX, float mouseY, float delta, float alpha, CallbackInfo ci) {
      this.richProfileBegin("ClickGui/settingsPanelExternal");
   }

   @Inject(method = "renderSettingsPanelExternal", at = @At("RETURN"), require = 0)
   private void richProfileSettingsPanelEnd(DrawContext context, float x, float y, float mouseX, float mouseY, float delta, float alpha, CallbackInfo ci) {
      this.richProfileEnd();
   }
}
