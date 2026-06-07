package rich.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.world.ClientWorld.Properties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.IMinecraft;
import rich.events.api.EventManager;
import rich.events.impl.EntitySpawnEvent;
import rich.events.impl.WorldLoadEvent;
import rich.modules.impl.render.Ambience;
import rich.util.string.PlayerInteractionHelper;

@Mixin(ClientWorld.class)
public class ClientWorldMixin implements IMinecraft {
   @Shadow
   @Final
   private net.minecraft.client.world.ClientWorld.Properties clientWorldProperties;

   @Inject(method = "<init>", at = @At("RETURN"))
   public void initHook(CallbackInfo var1) {
      EventManager.callEvent(new WorldLoadEvent());
   }

   @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
   public void addEntityHook(Entity var1, CallbackInfo var2) {
      if (!PlayerInteractionHelper.nullCheck()) {
         EntitySpawnEvent var3 = new EntitySpawnEvent(var1);
         EventManager.callEvent(var3);
         if (var3.isCancelled()) {
            var2.cancel();
         }
      }
   }

   @Inject(method = "tickTime", at = @At("HEAD"), cancellable = true)
   private void onTickTime(CallbackInfo var1) {
      Ambience var2 = Ambience.getInstance();
      if (var2 != null && var2.isState()) {
         this.clientWorldProperties.setTimeOfDay(var2.getCustomTime());
         var1.cancel();
      }
   }
}
