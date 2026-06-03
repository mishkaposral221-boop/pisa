package rich.mixin;

import net.minecraft.class_2558;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_2558.class_10609;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.command.CommandManager;
import rich.screens.clickgui.ClickGui;

@Mixin(class_437.class)
public class ScreenMixin {
   @Inject(method = "method_25420", at = @At("HEAD"), cancellable = true)
   private void disableBackgroundBlurAndDimming(class_332 var1, int var2, int var3, float var4, CallbackInfo var5) {
      if (this instanceof ClickGui) {
         var5.cancel();
      }
   }

   @Inject(method = "method_71999", at = @At("HEAD"), cancellable = true)
   private static void onHandleClickEvent(class_2558 var0, class_310 var1, class_437 var2, CallbackInfo var3) {
      if (var0 instanceof class_10609 var4) {
         String var5 = var4.comp_3506();
         CommandManager var6 = CommandManager.getInstance();
         if (var6 != null && var5 != null && var5.startsWith(var6.getPrefix())) {
            var6.execute(var5.substring(var6.getPrefix().length()));
            var3.cancel();
         }
      }
   }
}
