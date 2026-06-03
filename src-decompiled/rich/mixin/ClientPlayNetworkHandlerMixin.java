package rich.mixin;

import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2663;
import net.minecraft.class_2678;
import net.minecraft.class_2724;
import net.minecraft.class_3417;
import net.minecraft.class_634;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.IMinecraft;
import rich.events.api.EventManager;
import rich.events.impl.ChatEvent;
import rich.events.impl.EntityStatusEvent;
import rich.events.impl.GameLeftEvent;
import rich.events.impl.WorldChangeEvent;
import rich.modules.impl.render.Particles;

@Mixin(class_634.class)
public abstract class ClientPlayNetworkHandlerMixin implements IMinecraft {
   @Shadow
   private class_638 field_3699;
   @Unique
   private boolean worldNotNull;

   @Shadow
   private static class_1799 method_19691(class_1657 var0) {
      return null;
   }

   @Inject(method = "method_45729", at = @At("HEAD"), cancellable = true)
   private void onSendChatMessage(String var1, CallbackInfo var2) {
      ChatEvent var3 = new ChatEvent(var1);
      EventManager.callEvent(var3);
      if (var3.isCancelled()) {
         var2.cancel();
      }
   }

   @Inject(method = "method_11120", at = @At("HEAD"))
   private void onGameJoinHead(class_2678 var1, CallbackInfo var2) {
      this.worldNotNull = this.field_3699 != null;
   }

   @Inject(method = "method_11120", at = @At("TAIL"))
   private void onGameJoinTail(class_2678 var1, CallbackInfo var2) {
      if (this.worldNotNull) {
         EventManager.callEvent(GameLeftEvent.get());
      }
   }

   @Inject(method = "method_11120", at = @At("RETURN"))
   private void onGameJoin(class_2678 var1, CallbackInfo var2) {
      EventManager.callEvent(WorldChangeEvent.get());
   }

   @Inject(method = "method_11117", at = @At("RETURN"))
   private void onPlayerRespawn(class_2724 var1, CallbackInfo var2) {
      EventManager.callEvent(WorldChangeEvent.get());
   }

   @Inject(method = "method_11148", at = @At("HEAD"), cancellable = true)
   private void onEntityStatus(class_2663 var1, CallbackInfo var2) {
      if (mc.field_1687 != null) {
         class_1297 var3 = var1.method_11469(mc.field_1687);
         if (var3 != null) {
            EventManager.callEvent(new EntityStatusEvent(var3, var1.method_11470()));
         }

         if (var1.method_11470() == 35) {
            class_1297 var4 = var3;
            if (var4 != null) {
               Particles var5 = Particles.getInstance();
               if (var5 != null && var5.isState() && var5.triggers.isSelected("Тотем")) {
                  var5.onTotemPop(var4);
                  mc.field_1687
                     .method_8486(var4.method_23317(), var4.method_23318(), var4.method_23321(), class_3417.field_14931, var4.method_5634(), 1.0F, 1.0F, false);
                  if (var4 == mc.field_1724) {
                     mc.field_1773.method_3189(method_19691(mc.field_1724));
                  }

                  var2.cancel();
               }
            }
         }
      }
   }
}
