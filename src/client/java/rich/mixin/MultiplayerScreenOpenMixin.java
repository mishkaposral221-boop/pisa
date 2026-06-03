package rich.mixin;

import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.util.config.impl.proxy.ProxyConfig;
import rich.util.proxy.GuiProxy;
import rich.util.proxy.ProxyServer;

@Mixin(MultiplayerScreen.class)
public class MultiplayerScreenOpenMixin {
   @Inject(method = "init()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/MultiplayerScreen;updateButtonActivationStates()V"))
   public void multiplayerGuiOpen(CallbackInfo var1) {
      MultiplayerScreen var2 = (MultiplayerScreen)(Object)this;
      MinecraftClient var3 = MinecraftClient.getInstance();
      ProxyConfig var4 = ProxyConfig.getInstance();
      String var5;
      if (var4.isProxyEnabled() && !var4.getDefaultProxy().isEmpty()) {
         var5 = "§aПрокси: Активен";
      } else {
         var5 = "§7Proxy";
      }

      ProxyServer.proxyMenuButton = ButtonWidget.builder(Text.literal(var5), var1x -> MinecraftClient.getInstance().setScreen(new GuiProxy(var2)))
         .dimensions(5, 5, 100, 20)
         .build();
      IScreen var6 = (IScreen)var2;
      var6.getDrawables().add(ProxyServer.proxyMenuButton);
      var6.getSelectables().add(ProxyServer.proxyMenuButton);
      var6.getChildren().add(ProxyServer.proxyMenuButton);
   }
}
