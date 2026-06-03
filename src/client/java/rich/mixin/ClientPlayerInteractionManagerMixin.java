package rich.mixin;

import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.ActionResult.Success;
import net.minecraft.util.ActionResult.SwingSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.events.api.EventManager;
import rich.events.impl.BlockBreakingEvent;
import rich.events.impl.ClickSlotEvent;
import rich.events.impl.UsingItemEvent;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
   @Inject(method = "interactItem", at = @At("RETURN"))
   public void interactItemHook(PlayerEntity var1, Hand var2, CallbackInfoReturnable<ActionResult> var3) {
      if (var3.getReturnValue() instanceof net.minecraft.util.ActionResult.Success var4 && !var4.swingSource().equals(net.minecraft.util.ActionResult.SwingSource.CLIENT)) {
         UsingItemEvent var6 = new UsingItemEvent((byte)0);
         EventManager.callEvent(var6);
      }
   }

   @Inject(method = "stopUsingItem", at = @At("HEAD"), cancellable = true)
   public void stopUsingItemHook(CallbackInfo var1) {
      UsingItemEvent var2 = new UsingItemEvent((byte)2);
      EventManager.callEvent(var2);
   }

   @Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
   private void gameModeHook(PlayerEntity var1, Hand var2, CallbackInfoReturnable<ActionResult> var3) {
      UsingItemEvent var4 = new UsingItemEvent((byte)-1);
      EventManager.callEvent(var4);
      if (var4.isCancelled()) {
         var3.setReturnValue(ActionResult.PASS);
      }
   }

   @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"))
   private void injectBlockBreaking(BlockPos var1, Direction var2, CallbackInfoReturnable<Boolean> var3) {
      EventManager.callEvent(new BlockBreakingEvent(var1, var2));
   }

   @Inject(method = "clickSlot", at = @At("HEAD"), cancellable = true)
   public void clickSlotHook(int var1, int var2, int var3, SlotActionType var4, PlayerEntity var5, CallbackInfo var6) {
      ClickSlotEvent var7 = new ClickSlotEvent(var1, var2, var3, var4);
      EventManager.callEvent(var7);
      if (var7.isCancelled()) {
         var6.cancel();
      }
   }
}
