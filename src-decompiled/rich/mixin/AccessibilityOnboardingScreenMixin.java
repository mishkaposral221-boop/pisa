package rich.mixin;

import net.minecraft.class_310;
import net.minecraft.class_8032;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.screens.menu.MainMenuScreen;

@Mixin(class_8032.class)
public class AccessibilityOnboardingScreenMixin {
   @Inject(method = "method_25426", at = @At("HEAD"), cancellable = true)
   private void onInit(CallbackInfo var1) {
      var1.cancel();
      class_310.method_1551().method_1507(new MainMenuScreen());
   }
}
