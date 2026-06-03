package rich.util.proxy;

import net.minecraft.class_4185;
import rich.util.config.impl.proxy.ProxyConfig;

public class ProxyServer {
   public static class_4185 proxyMenuButton;

   public static boolean isProxyEnabled() {
      return ProxyConfig.getInstance().isProxyEnabled();
   }

   public static void setProxyEnabled(boolean var0) {
      ProxyConfig.getInstance().setProxyEnabled(var0);
   }

   public static Proxy getProxy() {
      return ProxyConfig.getInstance().getDefaultProxy();
   }

   public static void setProxy(Proxy var0) {
      ProxyConfig.getInstance().setDefaultProxy(var0);
   }

   public static Proxy getLastUsedProxy() {
      return ProxyConfig.getInstance().getLastUsedProxy();
   }

   public static void setLastUsedProxy(Proxy var0) {
      ProxyConfig.getInstance().setLastUsedProxy(var0);
   }

   public static String getLastUsedProxyIp() {
      Proxy var0 = getLastUsedProxy();
      return var0.isEmpty() ? "none" : var0.getIp();
   }

   public static void save() {
      ProxyConfig.getInstance().save();
   }

   public static void load() {
      ProxyConfig.getInstance().load();
   }
}
