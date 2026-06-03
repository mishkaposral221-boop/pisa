package rich.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.resource.ResourceReload;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.SplashOverlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashOverlay.class)
public abstract class SplashOverlayMixin {
   @Shadow
   @Final
   private MinecraftClient client;
   @Shadow
   @Final
   private ResourceReload reload;
   @Shadow
   @Final
   private boolean reloading;

   @Inject(method = "render", at = @At("HEAD"), cancellable = true)
   private void onRender(DrawContext var1, int var2, int var3, float var4, CallbackInfo var5) {
      if (!this.reloading) {
         var5.cancel();
         if (this.reload.isComplete()) {
            this.client.setOverlay((Overlay)null);
         }
      }
   }
}
