package rich.mixin;

import net.minecraft.class_1058;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_4603;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.modules.impl.render.NoRender;

@Mixin(class_4603.class)
public class InGameOverlayRendererMixin {
   @Inject(method = "method_23070", at = @At("HEAD"), cancellable = true)
   private static void renderFireOverlayHook(class_4587 var0, class_4597 var1, class_1058 var2, CallbackInfo var3) {
      NoRender var4 = NoRender.getInstance();
      if (var4.isState() && var4.modeSetting.isSelected("Fire")) {
         var3.cancel();
      }
   }

   @Inject(method = "method_23068", at = @At("HEAD"), cancellable = true)
   private static void renderInWallOverlayHook(class_1058 var0, class_4587 var1, class_4597 var2, CallbackInfo var3) {
      NoRender var4 = NoRender.getInstance();
      if (var4.isState() && var4.modeSetting.isSelected("Block Overlay")) {
         var3.cancel();
      }
   }
}
