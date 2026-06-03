package rich.mixin;

import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.events.api.EventManager;
import rich.events.impl.AttackEvent;
import rich.events.impl.ClickSlotEvent;
import rich.events.impl.InteractEntityEvent;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {
   @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
   public void attackEntityHook(PlayerEntity var1, Entity var2, CallbackInfo var3) {
      InteractEntityEvent var4 = new InteractEntityEvent(var2);
      EventManager.callEvent(var4);
      if (var4.isCancelled()) {
         var3.cancel();
      }
   }

   @Inject(method = "attackEntity", at = @At("HEAD"))
   private void onAttackEntity(PlayerEntity var1, Entity var2, CallbackInfo var3) {
      boolean var4 = var1.fallDistance > 0.0
         && !var1.isOnGround()
         && !var1.isClimbing()
         && !var1.isTouchingWater()
         && !var1.hasStatusEffect(StatusEffects.BLINDNESS)
         && var1.getVehicle() == null;
      AttackEvent var5 = new AttackEvent(var2, var4);
      EventManager.callEvent(var5);
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
