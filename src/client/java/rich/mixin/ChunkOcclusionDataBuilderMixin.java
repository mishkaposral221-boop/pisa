package rich.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.events.api.EventManager;
import rich.events.impl.ChunkOcclusionEvent;

@Mixin(ChunkOcclusionDataBuilder.class)
public abstract class ChunkOcclusionDataBuilderMixin {
   @Inject(method = "markClosed", at = @At("HEAD"), cancellable = true)
   private void onMarkClosed(BlockPos var1, CallbackInfo var2) {
      ChunkOcclusionEvent var3 = ChunkOcclusionEvent.get();
      EventManager.callEvent(var3);
      if (var3.isCancelled()) {
         var2.cancel();
      }
   }
}
