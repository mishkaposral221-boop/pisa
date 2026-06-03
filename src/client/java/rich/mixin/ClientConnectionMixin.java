package rich.mixin;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import java.net.InetSocketAddress;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.handler.PacketSizeLogger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.events.api.EventManager;
import rich.events.impl.PacketEvent;
import rich.util.config.impl.proxy.ProxyConfig;
import rich.util.proxy.Proxy;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
   @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
   private static <T extends PacketListener> void handlePacketPre(Packet<T> var0, PacketListener var1, CallbackInfo var2) {
      PacketEvent var3 = new PacketEvent(var0, PacketEvent.Type.RECEIVE);
      EventManager.callEvent(var3);
      if (var3.isCancelled()) {
         var2.cancel();
      }
   }

   @Inject(method = "send(Lnet/minecraft/Packet;)V", at = @At("HEAD"), cancellable = true)
   private void sendPre(Packet<?> var1, CallbackInfo var2) {
      PacketEvent var3 = new PacketEvent(var1, PacketEvent.Type.SEND);
      EventManager.callEvent(var3);
      if (var3.isCancelled()) {
         var2.cancel();
      }
   }

   @Inject(method = "addHandlers", at = @At("RETURN"))
   private static void addHandlersHook(ChannelPipeline var0, NetworkSide var1, boolean var2, PacketSizeLogger var3, CallbackInfo var4) {
      ProxyConfig var5 = ProxyConfig.getInstance();
      Proxy var6 = var5.getDefaultProxy();
      if (var6 != null && var5.isProxyEnabled() && !var6.isEmpty() && var1 == NetworkSide.CLIENTBOUND && !var2) {
         InetSocketAddress var7 = new InetSocketAddress(var6.getIp(), var6.getPort());
         if (var6.type == Proxy.ProxyType.SOCKS4) {
            var0.addFirst("rich_socks4_proxy", new Socks4ProxyHandler(var7, var6.username));
         } else {
            var0.addFirst("rich_socks5_proxy", new Socks5ProxyHandler(var7, var6.username, var6.password));
         }

         var5.setLastUsedProxy(new Proxy(var6));
      }
   }
}
