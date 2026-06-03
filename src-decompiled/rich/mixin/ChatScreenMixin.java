package rich.mixin;

import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_408;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.Initialization;
import rich.client.draggables.Drag;

@Mixin(class_408.class)
public abstract class ChatScreenMixin extends class_437 {
   protected ChatScreenMixin(class_2561 var1) {
      super(var1);
   }

   @Inject(method = "method_25394", at = @At("TAIL"))
   private void onRender(class_332 var1, int var2, int var3, float var4, CallbackInfo var5) {
      Drag.onDraw(var1, var2, var3, var4, true);
   }

   @Inject(method = "method_25402", at = @At("HEAD"), cancellable = true)
   private void onMouseClicked(class_11909 var1, boolean var2, CallbackInfoReturnable<Boolean> var3) {
      int var4 = (int)var1.comp_4798();
      int var5 = (int)var1.comp_4799();
      int var6 = var1.method_74245();
      if (Initialization.getInstance() != null
         && Initialization.getInstance().getManager() != null
         && Initialization.getInstance().getManager().getHudManager() != null
         && Initialization.getInstance().getManager().getHudManager().mouseClicked(var4, var5, var6)) {
         var3.setReturnValue(true);
      } else {
         Drag.onMouseClick(var1);
         if (Drag.isDragging()) {
            var3.setReturnValue(true);
         }
      }
   }

   public boolean method_25406(class_11909 var1) {
      Drag.onMouseRelease(var1);
      return super.method_25406(var1);
   }

   public boolean method_25403(class_11909 var1, double var2, double var4) {
      return super.method_25403(var1, var2, var4);
   }

   public void method_25432() {
      Drag.resetDragging();
      super.method_25432();
   }

   public void method_25419() {
      Drag.resetDragging();
      super.method_25419();
   }
}
