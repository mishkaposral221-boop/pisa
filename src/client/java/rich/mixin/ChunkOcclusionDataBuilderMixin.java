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
   private static final long RICH_OCCLUSION_EVENT_CACHE_NS = 50_000_000L;
   @Unique
   private static long richLastOcclusionEventCheckNs = 0L;
   @Unique
   private static boolean richCachedOcclusionCancelled = false;
   @Unique
   private boolean richProfileBuildOpen = false;

   @Inject(method = "markClosed", at = @At("HEAD"), cancellable = true, require = 0)
   private void onMarkClosed(BlockPos var1, CallbackInfo var2) {
      if (this.richIsOcclusionCancelledCached()) {
         var2.cancel();
      }
   }

   @Unique
   private boolean richIsOcclusionCancelledCached() {
      long now = System.nanoTime();
      if (now - richLastOcclusionEventCheckNs < RICH_OCCLUSION_EVENT_CACHE_NS) {
         return richCachedOcclusionCancelled;
      }

      FrameProfiler profiler = FrameProfiler.getInstance();
      boolean prof = profiler.isEnabled();
      if (prof) {
         profiler.begin("Minecraft/chunkOcclusion/eventRefresh");
      }
      try {
         ChunkOcclusionEvent event = ChunkOcclusionEvent.get();
         EventManager.callEvent(event);
         richCachedOcclusionCancelled = event.isCancelled();
         richLastOcclusionEventCheckNs = now;
         return richCachedOcclusionCancelled;
      } finally {
         if (prof) {
            profiler.end();
         }
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
