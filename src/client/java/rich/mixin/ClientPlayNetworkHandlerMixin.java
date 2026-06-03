package rich.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
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

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin implements IMinecraft {
   @Shadow
   private ClientWorld world;
   @Unique
   private boolean worldNotNull;

   @Shadow
   private static ItemStack getActiveDeathProtector(PlayerEntity var0) {
      return null;
   }

   @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
   private void onSendChatMessage(String var1, CallbackInfo var2) {
      ChatEvent var3 = new ChatEvent(var1);
      EventManager.callEvent(var3);
      if (var3.isCancelled()) {
         var2.cancel();
      }
   }

   @Inject(method = "onGameJoin", at = @At("HEAD"))
   private void onGameJoinHead(GameJoinS2CPacket var1, CallbackInfo var2) {
      this.worldNotNull = this.world != null;
   }

   @Inject(method = "onGameJoin", at = @At("TAIL"))
   private void onGameJoinTail(GameJoinS2CPacket var1, CallbackInfo var2) {
      if (this.worldNotNull) {
         EventManager.callEvent(GameLeftEvent.get());
      }
   }

   @Inject(method = "onGameJoin", at = @At("RETURN"))
   private void onGameJoin(GameJoinS2CPacket var1, CallbackInfo var2) {
      EventManager.callEvent(WorldChangeEvent.get());
   }

   @Inject(method = "onPlayerRespawn", at = @At("RETURN"))
   private void onPlayerRespawn(PlayerRespawnS2CPacket var1, CallbackInfo var2) {
      EventManager.callEvent(WorldChangeEvent.get());
   }

   @Inject(method = "onEntityStatus", at = @At("HEAD"), cancellable = true)
   private void onEntityStatus(EntityStatusS2CPacket var1, CallbackInfo var2) {
      if (mc.world != null) {
         Entity var3 = var1.getEntity(mc.world);
         if (var3 != null) {
            EventManager.callEvent(new EntityStatusEvent(var3, var1.getStatus()));
         }

         if (var1.getStatus() == 35) {
            Entity var4 = var3;
            if (var4 != null) {
               Particles var5 = Particles.getInstance();
               if (var5 != null && var5.isState() && var5.triggers.isSelected("Тотем")) {
                  var5.onTotemPop(var4);
                  mc.world
                     .playSoundClient(var4.getX(), var4.getY(), var4.getZ(), SoundEvents.ITEM_TOTEM_USE, var4.getSoundCategory(), 1.0F, 1.0F, false);
                  if (var4 == mc.player) {
                     mc.gameRenderer.showFloatingItem(getActiveDeathProtector(mc.player));
                  }

                  var2.cancel();
               }
            }
         }
      }
   }
}
