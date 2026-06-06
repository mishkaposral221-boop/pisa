package rich.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import rich.util.profiler.ProfilerFrameGraphHooks;

@Mixin(targets = "net.minecraft.client.render.FrameGraphBuilder$FramePassImpl")
public abstract class FramePassImplMixin {
   @ModifyVariable(method = "setRenderer", at = @At("HEAD"), argsOnly = true, require = 0)
   private Runnable richProfileFramePassRenderer(Runnable renderer) {
      return ProfilerFrameGraphHooks.wrapPassRenderer(this, renderer);
   }
}
