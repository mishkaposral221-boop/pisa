package rich.mixin;

import net.minecraft.client.render.chunk.ChunkOcclusionData;
import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.events.api.EventManager;
import rich.events.impl.ChunkOcclusionEvent;
import rich.util.profiler.FrameProfiler;

@Mixin(ChunkOcclusionDataBuilder.class)
public abstract class ChunkOcclusionDataBuilderMixin {
   @Unique
   private boolean richProfileMarkClosedOpen = false;
   @Unique
   private boolean richProfileBuildOpen = false;

   @Inject(method = "markClosed", at = @At("HEAD"), cancellable = true, require = 0)
   private void onMarkClosed(BlockPos var1, CallbackInfo var2) {
      FrameProfiler profiler = FrameProfiler.getInstance();
      boolean prof = profiler.isEnabled();
      if (prof) {
         profiler.begin("Minecraft/chunkOcclusion/markClosed");
         this.richProfileMarkClosedOpen = true;
      }

      ChunkOcclusionEvent var3 = ChunkOcclusionEvent.get();
      EventManager.callEvent(var3);
      if (var3.isCancelled()) {
         if (prof && this.richProfileMarkClosedOpen) {
            profiler.end();
            this.richProfileMarkClosedOpen = false;
         }
         var2.cancel();
      }
   }

   @Inject(method = "markClosed", at = @At("RETURN"), require = 0)
   private void onMarkClosedReturn(BlockPos var1, CallbackInfo var2) {
      if (this.richProfileMarkClosedOpen) {
         FrameProfiler.getInstance().end();
         this.richProfileMarkClosedOpen = false;
      }
   }

   @Inject(method = "build", at = @At("HEAD"), require = 0)
   private void richProfileBuildStart(CallbackInfoReturnable<ChunkOcclusionData> cir) {
      FrameProfiler profiler = FrameProfiler.getInstance();
      if (profiler.isEnabled()) {
         profiler.begin("Minecraft/chunkOcclusion/build");
         this.richProfileBuildOpen = true;
      }
   }

   @Inject(method = "build", at = @At("RETURN"), require = 0)
   private void richProfileBuildEnd(CallbackInfoReturnable<ChunkOcclusionData> cir) {
      if (this.richProfileBuildOpen) {
         FrameProfiler.getInstance().end();
         this.richProfileBuildOpen = false;
      }
   }
}
