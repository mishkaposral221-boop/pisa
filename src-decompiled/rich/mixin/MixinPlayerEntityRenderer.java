package rich.mixin;

import net.minecraft.class_1007;
import net.minecraft.class_5617.class_5618;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.modules.impl.render.chinahat.ChinaHatFeatureRenderer;

@Mixin(class_1007.class)
public class MixinPlayerEntityRenderer {
   @Inject(method = "<init>", at = @At("TAIL"))
   private void onInit(class_5618 var1, boolean var2, CallbackInfo var3) {
      class_1007 var4 = (class_1007)this;
      var4.method_4046(new ChinaHatFeatureRenderer(var4));
   }
}
