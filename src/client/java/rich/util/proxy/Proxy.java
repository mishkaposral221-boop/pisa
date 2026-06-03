package rich.util.proxy;

import com.google.gson.annotations.SerializedName;

public class Proxy {
   @SerializedName("IP:PORT")
   public String ipPort = "";
   public Proxy.ProxyType type = Proxy.ProxyType.SOCKS5;
   public String username = "";
   public String password = "";

   public Proxy() {
   }

   public Proxy(boolean var1, String var2, String var3, String var4) {
      this.type = var1 ? Proxy.ProxyType.SOCKS4 : Proxy.ProxyType.SOCKS5;
      this.ipPort = var2;
      this.username = var3;
      this.password = var4;
   }

   public Proxy(Proxy var1) {
      this.ipPort = var1.ipPort;
      this.type = var1.type;
      this.username = var1.username;
      this.password = var1.password;
   }

   public int getPort() {
      if (this.ipPort != null && !this.ipPort.isEmpty() && this.ipPort.contains(":")) {
         try {
            return Integer.parseInt(this.ipPort.split(":")[1]);
         } catch (NumberFormatException | ArrayIndexOutOfBoundsException var2) {
            return 0;
         }
      } else {
         return 0;
      }
   }

   public String getIp() {
      return this.ipPort != null && !this.ipPort.isEmpty() && this.ipPort.contains(":") ? this.ipPort.split(":")[0] : "";
   }

   public boolean isEmpty() {
      return this.ipPort == null || this.ipPort.isEmpty();
   }

   public enum ProxyType {
      SOCKS4,
      SOCKS5;
   }
}
