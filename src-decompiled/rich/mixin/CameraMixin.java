package rich.mixin;

import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_4184;
import net.minecraft.class_746;
import net.minecraft.class_2338.class_2339;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.events.api.EventManager;
import rich.events.impl.CameraEvent;
import rich.events.impl.CameraPositionEvent;
import rich.modules.impl.combat.aura.Angle;

@Mixin(class_4184.class)
public abstract class CameraMixin {
   @Shadow
   private class_243 field_18712;
   @Shadow
   @Final
   private class_2339 field_18713;
   @Shadow
   private float field_18718;
   @Shadow
   private float field_18717;

   @Shadow
   public abstract void method_19325(float var1, float var2);

   @Shadow
   protected abstract void method_19324(float var1, float var2, float var3);

   @Shadow
   protected abstract float method_19318(float var1);

   @Inject(
      method = "method_19321",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4184;method_19327(DDD)V", shift = Shift.AFTER),
      cancellable = true
   )
   private void updateHook(class_1937 var1, class_1297 var2, boolean var3, boolean var4, float var5, CallbackInfo var6) {
      CameraEvent var7 = new CameraEvent(false, 4.0F, new Angle(this.field_18718, this.field_18717));
      EventManager.callEvent(var7);
      Angle var8 = var7.getAngle();
      if (var7.isCancelled() && var2 instanceof class_746 var9 && !var9.method_6113() && var3) {
         float var11 = var4 ? -var8.getPitch() : var8.getPitch();
         float var12 = var8.getYaw() - (var4 ? 180 : 0);
         float var13 = var7.getDistance();
         this.method_19325(var12, var11);
         this.method_19324(var7.isCameraClip() ? -var13 : -this.method_19318(var13), 0.0F, 0.0F);
         var6.cancel();
      } else if (var7.isCancelled() && var2 instanceof class_746 var10 && !var10.method_6113() && !var3) {
         this.method_19325(var8.getYaw(), var8.getPitch());
         var6.cancel();
      }
   }

   @Inject(method = "method_19322(Lnet/minecraft/class_243;)V", at = @At("HEAD"), cancellable = true)
   private void posHook(class_243 var1, CallbackInfo var2) {
      CameraPositionEvent var3 = new CameraPositionEvent(var1);
      EventManager.callEvent(var3);
      this.field_18712 = var1 = var3.getPos();
      this.field_18713.method_10102(var1.field_1352, var1.field_1351, var1.field_1350);
      var2.cancel();
   }
}
