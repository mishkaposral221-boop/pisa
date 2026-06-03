package rich.mixin;

import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4011;
import net.minecraft.class_4071;
import net.minecraft.class_425;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_425.class)
public abstract class SplashOverlayMixin {
   @Shadow
   @Final
   private class_310 field_18217;
   @Shadow
   @Final
   private class_4011 field_17767;
   @Shadow
   @Final
   private boolean field_18219;

   @Inject(method = "method_25394", at = @At("HEAD"), cancellable = true)
   private void onRender(class_332 var1, int var2, int var3, float var4, CallbackInfo var5) {
      if (!this.field_18219) {
         var5.cancel();
         if (this.field_17767.method_18787()) {
            this.field_18217.method_18502((class_4071)null);
         }
      }
   }
}
