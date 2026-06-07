package rich.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.util.Hand;
import net.minecraft.util.Arm;
import net.minecraft.item.ItemStack;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.item.HeldItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.events.api.EventManager;
import rich.events.impl.GlassHandsRenderEvent;
import rich.events.impl.HandAnimationEvent;
import rich.events.impl.HandOffsetEvent;
import rich.events.impl.HeldItemUpdateEvent;
import rich.events.impl.ItemRendererEvent;
import rich.modules.impl.render.GlassHands;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
   @Shadow
   private ItemStack mainHand;
   @Shadow
   private ItemStack offHand;
   @Unique
   private boolean richCustomAnimation = false;

   @Inject(method = "updateHeldItems", at = @At("TAIL"))
   private void onUpdateHeldItems(CallbackInfo var1) {
      HeldItemUpdateEvent var2 = new HeldItemUpdateEvent(this.mainHand, this.offHand);
      EventManager.callEvent(var2);
      if (var2.getMainHand() != this.mainHand) {
         this.mainHand = var2.getMainHand();
      }

      if (var2.getOffHand() != this.offHand) {
         this.offHand = var2.getOffHand();
      }
   }

   @Inject(
      method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
      at = @At("HEAD")
   )
   private void onRenderItemPre(float var1, MatrixStack var2, OrderedRenderCommandQueue var3, ClientPlayerEntity var4, int var5, CallbackInfo var6) {
      GlassHands var7 = GlassHands.getInstance();
      if (var7 != null && var7.isState()) {
         GlassHandsRenderEvent var8 = new GlassHandsRenderEvent(GlassHandsRenderEvent.Phase.PRE, var2, var1);
         EventManager.callEvent(var8);
      }
   }

   @Inject(
      method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
      at = @At("TAIL")
   )
   private void onRenderItemPost(float var1, MatrixStack var2, OrderedRenderCommandQueue var3, ClientPlayerEntity var4, int var5, CallbackInfo var6) {
      GlassHands var7 = GlassHands.getInstance();
      if (var7 != null && var7.isState()) {
         GlassHandsRenderEvent var8 = new GlassHandsRenderEvent(GlassHandsRenderEvent.Phase.POST, var2, var1);
         EventManager.callEvent(var8);
      }
   }

   @WrapOperation(
      method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;I)V"
      )
   )
   private void itemRenderHook(
      HeldItemRenderer var1,
      AbstractClientPlayerEntity var2,
      float var3,
      float var4,
      Hand var5,
      float var6,
      ItemStack var7,
      float var8,
      MatrixStack var9,
      OrderedRenderCommandQueue var10,
      int var11,
      Operation<Void> var12
   ) {
      ItemRendererEvent var13 = new ItemRendererEvent(var2, var7, var5);
      EventManager.callEvent(var13);
      var12.call(new Object[]{var1, var13.getPlayer(), var3, var4, var13.getHand(), var6, var13.getStack(), var8, var9, var10, var11});
   }

   @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = Shift.AFTER))
   private void renderFirstPersonItemHook(
      AbstractClientPlayerEntity var1,
      float var2,
      float var3,
      Hand var4,
      float var5,
      ItemStack var6,
      float var7,
      MatrixStack var8,
      OrderedRenderCommandQueue var9,
      int var10,
      CallbackInfo var11
   ) {
      HandOffsetEvent var12 = new HandOffsetEvent(var8, var6, var4);
      EventManager.callEvent(var12);
      float var13 = var12.getScale();
      if (var13 != 1.0F) {
         var8.scale(var13, var13, var13);
      }
   }

   @WrapOperation(
      method = "renderFirstPersonItem",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applyEquipOffset(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V")
   )
   private void wrapApplyEquipOffset(
      HeldItemRenderer var1,
      MatrixStack var2,
      Arm var3,
      float var4,
      Operation<Void> var5,
      @Local(ordinal = 0, argsOnly = true) AbstractClientPlayerEntity var6,
      @Local(ordinal = 0, argsOnly = true) Hand var7,
      @Local(ordinal = 2, argsOnly = true) float var8,
      @Local(ordinal = 0, argsOnly = true) ItemStack var9
   ) {
      boolean var10 = var6.isUsingItem() && var6.getActiveHand() == var7;
      if (var10) {
         this.richCustomAnimation = false;
         var5.call(new Object[]{var1, var2, var3, var4});
      } else {
         HandAnimationEvent var11 = new HandAnimationEvent(var2, var7, var8);
         EventManager.callEvent(var11);
         if (var11.isCancelled()) {
            this.richCustomAnimation = true;
         } else {
            this.richCustomAnimation = false;
            var5.call(new Object[]{var1, var2, var3, var4});
         }
      }
   }

   @WrapOperation(
      method = "renderFirstPersonItem",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;swingArm(FLnet/minecraft/client/util/math/MatrixStack;ILnet/minecraft/util/Arm;)V")
   )
   private void wrapSwingArm(HeldItemRenderer var1, float var2, MatrixStack var3, int var4, Arm var5, Operation<Void> var6) {
      if (!this.richCustomAnimation) {
         var6.call(new Object[]{var1, var2, var3, var4, var5});
      }
   }
}
