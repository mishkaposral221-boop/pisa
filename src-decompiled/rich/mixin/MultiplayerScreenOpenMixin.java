package rich.mixin;

import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_4185;
import net.minecraft.class_500;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.util.config.impl.proxy.ProxyConfig;
import rich.util.proxy.GuiProxy;
import rich.util.proxy.ProxyServer;

@Mixin(class_500.class)
public class MultiplayerScreenOpenMixin {
   @Inject(method = "method_25426()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_500;method_20121()V"))
   public void multiplayerGuiOpen(CallbackInfo var1) {
      class_500 var2 = (class_500)this;
      class_310 var3 = class_310.method_1551();
      ProxyConfig var4 = ProxyConfig.getInstance();
      String var5;
      if (var4.isProxyEnabled() && !var4.getDefaultProxy().isEmpty()) {
         var5 = "§aПрокси: Активен";
      } else {
         var5 = "§7Proxy";
      }

      ProxyServer.proxyMenuButton = class_4185.method_46430(class_2561.method_43470(var5), var1x -> class_310.method_1551().method_1507(new GuiProxy(var2)))
         .method_46434(5, 5, 100, 20)
         .method_46431();
      IScreen var6 = (IScreen)var2;
      var6.getDrawables().add(ProxyServer.proxyMenuButton);
      var6.getSelectables().add(ProxyServer.proxyMenuButton);
      var6.getChildren().add(ProxyServer.proxyMenuButton);
   }
}
