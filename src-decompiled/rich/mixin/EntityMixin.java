package rich.mixin;

import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1937;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.IMinecraft;
import rich.events.api.EventManager;
import rich.events.impl.BoundingBoxControlEvent;
import rich.events.impl.PlayerVelocityStrafeEvent;
import rich.modules.impl.combat.aura.AngleConnection;

@Mixin(class_1297.class)
public abstract class EntityMixin implements IMinecraft {
   @Shadow
   private class_238 field_6005;
   @Shadow
   public float field_6031;
   @Unique
   private boolean client$local;
   @Unique
   private final class_310 client = class_310.method_1551();

   @Inject(method = "<init>", at = @At("TAIL"))
   private void onInit(class_1299<?> var1, class_1937 var2, CallbackInfo var3) {
      this.client$local = (class_1297)this instanceof class_746;
   }

   @Inject(method = "method_5829", at = @At("HEAD"), cancellable = true)
   public final void getBoundingBox(CallbackInfoReturnable<class_238> var1) {
      BoundingBoxControlEvent var2 = new BoundingBoxControlEvent(this.field_6005, (class_1297)this);
      EventManager.callEvent(var2);
      var1.setReturnValue(var2.getBox());
   }

   @Redirect(
      method = "method_5724",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1297;method_18795(Lnet/minecraft/class_243;FF)Lnet/minecraft/class_243;")
   )
   public class_243 hookVelocity(class_243 var1, float var2, float var3) {
      if (this == mc.field_1724) {
         PlayerVelocityStrafeEvent var4 = new PlayerVelocityStrafeEvent(var1, var2, var3, class_1297.method_18795(var1, var2, var3));
         EventManager.callEvent(var4);
         return var4.getVelocity();
      } else {
         return class_1297.method_18795(var1, var2, var3);
      }
   }

   @ModifyVariable(method = "method_5631(FF)Lnet/minecraft/class_243;", at = @At("HEAD"), ordinal = 0, argsOnly = true)
   private float modifyPitch(float var1) {
      return this instanceof class_746 && AngleConnection.INSTANCE.getCurrentAngle() != null ? AngleConnection.INSTANCE.getCurrentAngle().getPitch() : var1;
   }
}
