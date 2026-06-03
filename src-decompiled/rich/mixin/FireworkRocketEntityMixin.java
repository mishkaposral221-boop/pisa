package rich.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.class_1309;
import net.minecraft.class_1671;
import net.minecraft.class_243;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import rich.IMinecraft;
import rich.events.api.EventManager;
import rich.events.impl.FireworkEvent;
import rich.modules.impl.combat.aura.AngleConnection;

@Mixin(class_1671.class)
public class FireworkRocketEntityMixin implements IMinecraft {
   @Shadow
   @Nullable
   private class_1309 field_7616;

   @WrapOperation(method = "method_5773", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1309;method_5720()Lnet/minecraft/class_243;"))
   public class_243 getRotationVectorHook(class_1309 var1, Operation<class_243> var2) {
      return this.field_7616 == mc.field_1724 && this.field_7616.method_6128()
         ? AngleConnection.INSTANCE.getMoveRotation().toVector()
         : (class_243)var2.call(new Object[]{var1});
   }

   @WrapOperation(method = "method_5773", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1309;method_18799(Lnet/minecraft/class_243;)V"))
   public void setVelocityHook(class_1309 var1, class_243 var2, Operation<Void> var3) {
      if (this.field_7616 == mc.field_1724 && this.field_7616.method_6128()) {
         FireworkEvent var4 = new FireworkEvent(var2);
         EventManager.callEvent(var4);
         var3.call(new Object[]{var1, var4.getVector()});
      } else {
         var3.call(new Object[]{var1, var2});
      }
   }
}
