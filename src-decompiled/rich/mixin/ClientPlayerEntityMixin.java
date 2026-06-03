package rich.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.class_1313;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_638;
import net.minecraft.class_742;
import net.minecraft.class_744;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.IMinecraft;
import rich.events.api.EventManager;
import rich.events.impl.CloseScreenEvent;
import rich.events.impl.MoveEvent;
import rich.events.impl.PlayerTravelEvent;
import rich.events.impl.PushEvent;
import rich.events.impl.TickEvent;
import rich.events.impl.UsingItemEvent;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.util.move.MoveUtil;

@Mixin(class_746.class)
public abstract class ClientPlayerEntityMixin extends class_742 {
   @Final
   @Shadow
   protected class_310 field_3937;
   @Shadow
   public class_744 field_3913;
   private double prevX = 0.0;
   private double prevZ = 0.0;
   private float prevBodyYaw = 0.0F;

   @Shadow
   protected abstract void method_3148(float var1, float var2);

   @Shadow
   public abstract boolean method_6115();

   public ClientPlayerEntityMixin(class_638 var1, GameProfile var2) {
      super(var1, var2);
   }

   @Inject(method = "method_5773", at = @At("HEAD"))
   public void tick(CallbackInfo var1) {
      if (this.field_3937.field_1724 != null && this.field_3937.field_1687 != null) {
         EventManager.callEvent(new TickEvent());
      }
   }

   @Inject(method = "method_6007", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_744;method_3129()V", shift = Shift.AFTER))
   private void onInputTick(CallbackInfo var1) {
      if (IMinecraft.mc.field_1724 != null) {
         PlayerTravelEvent var2 = new PlayerTravelEvent(class_243.field_1353, false);
         EventManager.callEvent(var2);
      }
   }

   @Redirect(method = "method_67270", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_241;method_35582(F)Lnet/minecraft/class_241;", ordinal = 1))
   private class_241 cancelItemSlowdown(class_241 var1, float var2) {
      UsingItemEvent var3 = new UsingItemEvent((byte)1);
      EventManager.callEvent(var3);
      return var3.isCancelled() && this.method_6115() && !this.method_5765() ? var1.method_35582(1.0F) : var1.method_35582(var2);
   }

   @Inject(method = "method_7346", at = @At("HEAD"), cancellable = true)
   private void closeHandledScreenHook(CallbackInfo var1) {
      CloseScreenEvent var2 = new CloseScreenEvent(this.field_3937.field_1755);
      EventManager.callEvent(var2);
      if (var2.isCancelled()) {
         var1.cancel();
      }
   }

   @Inject(method = "method_30673", at = @At("HEAD"), cancellable = true)
   public void pushOutOfBlocks(double var1, double var3, CallbackInfo var5) {
      PushEvent var6 = new PushEvent(PushEvent.Type.BLOCK);
      EventManager.callEvent(var6);
      if (var6.isCancelled()) {
         var5.cancel();
      }
   }

   @Inject(
      method = "method_5784",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_742;method_5784(Lnet/minecraft/class_1313;Lnet/minecraft/class_243;)V"),
      cancellable = true
   )
   public void onMoveHook(class_1313 var1, class_243 var2, CallbackInfo var3) {
      MoveEvent var4 = new MoveEvent(var2);
      EventManager.callEvent(var4);
      double var5 = this.method_23317();
      double var7 = this.method_23321();
      super.method_5784(var1, var4.getMovement());
      this.method_3148((float)(this.method_23317() - var5), (float)(this.method_23321() - var7));
      var3.cancel();
   }

   @ModifyExpressionValue(method = {"method_3136", "method_5773"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/class_746;method_36454()F"))
   private float hookSilentRotationYaw(float var1) {
      if (IMinecraft.mc.field_1724 != null && AngleConnection.INSTANCE.getRotation() != null) {
         float var2 = AngleConnection.INSTANCE.getRotation().getYaw();
         float var3 = MoveUtil.calculateBodyYaw(
            var2,
            this.prevBodyYaw,
            this.prevX,
            this.prevZ,
            IMinecraft.mc.field_1724.method_23317(),
            IMinecraft.mc.field_1724.method_23321(),
            IMinecraft.mc.field_1724.field_6251
         );
         this.prevBodyYaw = var3;
         this.prevX = IMinecraft.mc.field_1724.method_23317();
         this.prevZ = IMinecraft.mc.field_1724.method_23321();
         IMinecraft.mc.field_1724.method_5636(var3);
         return var2;
      } else {
         return var1;
      }
   }

   @ModifyExpressionValue(method = {"method_3136", "method_5773"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/class_746;method_36455()F"))
   private float hookSilentRotationPitch(float var1) {
      return AngleConnection.INSTANCE.getRotation() != null ? AngleConnection.INSTANCE.getRotation().getPitch() : var1;
   }
}
