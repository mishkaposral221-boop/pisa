package rich.mixin;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import net.minecraft.class_4267;
import net.minecraft.class_4267.class_4270;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_4267.class)
public class MultiplayerServerListWidgetMixin {
   @Mutable
   @Final
   @Shadow
   static ThreadPoolExecutor field_19105;
   @Unique
   private static final int PINGER_THREAD_COUNT_OVERHEAD = 5;
   @Final
   @Shadow
   private List<class_4270> field_19109;
   @Unique
   private static boolean threadpoolInitialized = false;

   @Inject(method = "method_20131", at = @At("HEAD"))
   private void updateEntriesInject(CallbackInfo var1) {
      if (!threadpoolInitialized) {
         threadpoolInitialized = true;
         this.clearServerPingerThreadPool();
      }

      if (field_19105.getActiveCount() >= 5) {
         this.clearServerPingerThreadPool();
      }
   }

   @Unique
   private void clearServerPingerThreadPool() {
      field_19105.shutdownNow();
      field_19105 = new ScheduledThreadPoolExecutor(
         this.field_19109.size() + 5, new ThreadFactoryBuilder().setNameFormat("Server Pinger #%d").setDaemon(true).build()
      );
   }
}
