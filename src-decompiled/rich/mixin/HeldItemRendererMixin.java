package rich.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.class_11659;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1799;
import net.minecraft.class_4587;
import net.minecraft.class_742;
import net.minecraft.class_746;
import net.minecraft.class_759;
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

@Mixin(class_759.class)
public abstract class HeldItemRendererMixin {
   @Shadow
   private class_1799 field_4047;
   @Shadow
   private class_1799 field_4048;
   @Unique
   private boolean richCustomAnimation = false;

   @Inject(method = "method_3220", at = @At("TAIL"))
   private void onUpdateHeldItems(CallbackInfo var1) {
      HeldItemUpdateEvent var2 = new HeldItemUpdateEvent(this.field_4047, this.field_4048);
      EventManager.callEvent(var2);
      if (var2.getMainHand() != this.field_4047) {
         this.field_4047 = var2.getMainHand();
      }

      if (var2.getOffHand() != this.field_4048) {
         this.field_4048 = var2.getOffHand();
      }
   }

   @Inject(method = "method_22976(FLnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_746;I)V", at = @At("HEAD"))
   private void onRenderItemPre(float var1, class_4587 var2, class_11659 var3, class_746 var4, int var5, CallbackInfo var6) {
      GlassHands var7 = GlassHands.getInstance();
      if (var7 != null && var7.isState()) {
         GlassHandsRenderEvent var8 = new GlassHandsRenderEvent(GlassHandsRenderEvent.Phase.PRE, var2, var1);
         EventManager.callEvent(var8);
      }
   }

   @Inject(method = "method_22976(FLnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_746;I)V", at = @At("TAIL"))
   private void onRenderItemPost(float var1, class_4587 var2, class_11659 var3, class_746 var4, int var5, CallbackInfo var6) {
      GlassHands var7 = GlassHands.getInstance();
      if (var7 != null && var7.isState()) {
         GlassHandsRenderEvent var8 = new GlassHandsRenderEvent(GlassHandsRenderEvent.Phase.POST, var2, var1);
         EventManager.callEvent(var8);
      }
   }

   @WrapOperation(
      method = "method_22976(FLnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_746;I)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_759;method_3228(Lnet/minecraft/class_742;FFLnet/minecraft/class_1268;FLnet/minecraft/class_1799;FLnet/minecraft/class_4587;Lnet/minecraft/class_11659;I)V"
      )
   )
   private void itemRenderHook(
      class_759 var1,
      class_742 var2,
      float var3,
      float var4,
      class_1268 var5,
      float var6,
      class_1799 var7,
      float var8,
      class_4587 var9,
      class_11659 var10,
      int var11,
      Operation<Void> var12
   ) {
      ItemRendererEvent var13 = new ItemRendererEvent(var2, var7, var5);
      EventManager.callEvent(var13);
      var12.call(new Object[]{var1, var13.getPlayer(), var3, var4, var13.getHand(), var6, var13.getStack(), var8, var9, var10, var11});
   }

   @Inject(method = "method_3228", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4587;method_22903()V", shift = Shift.AFTER))
   private void renderFirstPersonItemHook(
      class_742 var1,
      float var2,
      float var3,
      class_1268 var4,
      float var5,
      class_1799 var6,
      float var7,
      class_4587 var8,
      class_11659 var9,
      int var10,
      CallbackInfo var11
   ) {
      HandOffsetEvent var12 = new HandOffsetEvent(var8, var6, var4);
      EventManager.callEvent(var12);
      float var13 = var12.getScale();
      if (var13 != 1.0F) {
         var8.method_22905(var13, var13, var13);
      }
   }

   @WrapOperation(
      method = "method_3228",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_759;method_3224(Lnet/minecraft/class_4587;Lnet/minecraft/class_1306;F)V")
   )
   private void wrapApplyEquipOffset(
      class_759 var1,
      class_4587 var2,
      class_1306 var3,
      float var4,
      Operation<Void> var5,
      @Local(ordinal = 0, argsOnly = true) class_742 var6,
      @Local(ordinal = 0, argsOnly = true) class_1268 var7,
      @Local(ordinal = 2, argsOnly = true) float var8,
      @Local(ordinal = 0, argsOnly = true) class_1799 var9
   ) {
      boolean var10 = var6.method_6115() && var6.method_6058() == var7;
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
      method = "method_3228",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_759;method_65816(FLnet/minecraft/class_4587;ILnet/minecraft/class_1306;)V")
   )
   private void wrapSwingArm(class_759 var1, float var2, class_4587 var3, int var4, class_1306 var5, Operation<Void> var6) {
      if (!this.richCustomAnimation) {
         var6.call(new Object[]{var1, var2, var3, var4, var5});
      }
   }
}
