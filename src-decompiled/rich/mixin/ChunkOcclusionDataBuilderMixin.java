package rich.mixin;

import net.minecraft.class_2338;
import net.minecraft.class_852;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.events.api.EventManager;
import rich.events.impl.ChunkOcclusionEvent;

@Mixin(class_852.class)
public abstract class ChunkOcclusionDataBuilderMixin {
   @Inject(method = "method_3682", at = @At("HEAD"), cancellable = true)
   private void onMarkClosed(class_2338 var1, CallbackInfo var2) {
      ChunkOcclusionEvent var3 = ChunkOcclusionEvent.get();
      EventManager.callEvent(var3);
      if (var3.isCancelled()) {
         var2.cancel();
      }
   }
}
