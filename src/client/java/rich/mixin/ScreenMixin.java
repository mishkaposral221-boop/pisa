package rich.mixin;

import net.minecraft.text.ClickEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.ClickEvent.RunCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.command.CommandManager;
import rich.screens.clickgui.ClickGui;

@Mixin(Screen.class)
public class ScreenMixin {
   @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
   private void disableBackgroundBlurAndDimming(DrawContext var1, int var2, int var3, float var4, CallbackInfo var5) {
      if ((Object)this instanceof ClickGui) {
         var5.cancel();
      }
   }

   @Inject(method = "handleClickEvent", at = @At("HEAD"), cancellable = true)
   private static void onHandleClickEvent(ClickEvent var0, MinecraftClient var1, Screen var2, CallbackInfo var3) {
      if (var0 instanceof net.minecraft.text.ClickEvent.RunCommand var4) {
         String var5 = var4.command();
         CommandManager var6 = CommandManager.getInstance();
         if (var6 != null && var5 != null && var5.startsWith(var6.getPrefix())) {
            var6.execute(var5.substring(var6.getPrefix().length()));
            var3.cancel();
         }
      }
   }
}
