package rich.mixin;

import net.minecraft.class_12074;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_761;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.modules.impl.render.BlockOverlay;

@Mixin(class_761.class)
public class BlockOutlineMixin {
   @Inject(method = "method_22712", at = @At("HEAD"), cancellable = true)
   private void onDrawBlockOutline(
      class_4587 var1, class_4588 var2, double var3, double var5, double var7, class_12074 var9, int var10, float var11, CallbackInfo var12
   ) {
      BlockOverlay var13 = BlockOverlay.getInstance();
      if (var13 != null && var13.isState()) {
         var12.cancel();
      }
   }
}
