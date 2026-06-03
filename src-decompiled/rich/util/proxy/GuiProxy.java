package rich.util.proxy;

import net.minecraft.class_11908;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_4185;
import net.minecraft.class_4286;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.minecraft.class_500;
import net.minecraft.class_4286.class_8929;
import org.apache.commons.lang3.StringUtils;
import rich.util.config.impl.proxy.ProxyConfig;

public class GuiProxy extends class_437 {
   private boolean isSocks4 = false;
   private class_342 ipPort;
   private class_342 username;
   private class_342 password;
   private class_4286 enabledCheck;
   private class_437 parentScreen;
   private String msg = "";
   private int[] positionY;
   private int positionX;
   private static String text_proxy = class_2561.method_43471("PROXY").getString();

   public GuiProxy(class_437 var1) {
      super(class_2561.method_43470(text_proxy));
      this.parentScreen = var1;
   }

   private static boolean isValidIpPort(String var0) {
      if (var0 != null && !var0.isEmpty()) {
         String[] var1 = var0.split(":");
         if (var1.length <= 1) {
            return false;
         }

         if (!StringUtils.isNumeric(var1[1])) {
            return false;
         }

         try {
            int var2 = Integer.parseInt(var1[1]);
            return var2 >= 0 && var2 <= 65535;
         } catch (NumberFormatException var3) {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean checkProxy() {
      if (!isValidIpPort(this.ipPort.method_1882())) {
         this.ipPort.method_25365(true);
         return false;
      } else {
         return true;
      }
   }

   private void centerButtons(int var1, int var2, int var3) {
      this.positionX = this.field_22789 / 2 - var2 / 2;
      this.positionY = new int[var1];
      int var4 = (this.field_22790 + var1 * var3) / 2;
      int var5 = var4 - var1 * var3;

      for (int var6 = 0; var6 != var1; var6++) {
         this.positionY[var6] = var5 + var3 * var6;
      }
   }

   public boolean method_25404(class_11908 var1) {
      if (var1.method_74231()) {
         class_310.method_1551().method_1507(this.parentScreen);
         return true;
      } else {
         super.method_25404(var1);
         this.msg = "";
         return true;
      }
   }

   public void method_25394(class_332 var1, int var2, int var3, float var4) {
      super.method_25394(var1, var2, var3, var4);
      if (this.enabledCheck.method_20372() && !isValidIpPort(this.ipPort.method_1882())) {
         this.enabledCheck.method_25306(null);
      }

      var1.method_25303(
         this.field_22793,
         class_2561.method_43471("Введите айпи адрес и порт. Пример ниже").getString(),
         this.field_22789 / 2 - 106,
         this.positionY[3] - 15,
         10526880
      );
      var1.method_25303(this.field_22793, class_2561.method_43471("Айпи:Порт ▸").getString(), this.field_22789 / 2 - 140, this.positionY[3] + 15, 10526880);
      this.ipPort.method_25394(var1, var2, var3, var4);
      var1.method_25303(this.field_22793, class_2561.method_43471("Никнейм ▸").getString(), this.field_22789 / 2 - 131, this.positionY[4] + 15, 10526880);
      var1.method_25303(this.field_22793, class_2561.method_43471("Пароль ▸").getString(), this.field_22789 / 2 - 126, this.positionY[5] + 15, 10526880);
      this.username.method_25394(var1, var2, var3, var4);
      this.password.method_25394(var1, var2, var3, var4);
      var1.method_25300(this.field_22793, this.msg, this.field_22789 / 2, this.positionY[6] + 5, 10526880);
   }

   public void method_25426() {
      short var1 = 160;
      this.centerButtons(10, var1, 26);
      ProxyConfig var2 = ProxyConfig.getInstance();
      Proxy var3 = var2.getDefaultProxy();
      this.isSocks4 = var3.type == Proxy.ProxyType.SOCKS4;
      this.ipPort = new class_342(this.field_22793, this.positionX, this.positionY[3] + 10, var1, 20, class_2561.method_43470(""));
      this.ipPort.method_1852(var3.ipPort);
      this.ipPort.method_1880(1024);
      this.ipPort.method_25365(true);
      this.method_25429(this.ipPort);
      this.username = new class_342(this.field_22793, this.positionX, this.positionY[4] + 10, var1, 20, class_2561.method_43470(""));
      this.username.method_1880(255);
      this.username.method_1852(var3.username);
      this.method_25429(this.username);
      this.password = new class_342(this.field_22793, this.positionX, this.positionY[5] + 10, var1, 20, class_2561.method_43470(""));
      this.password.method_1880(255);
      this.password.method_1852(var3.password);
      this.method_25429(this.password);
      int var4 = this.field_22789 / 2 - var1 / 2 * 3 / 2;
      class_4185 var5 = class_4185.method_46430(class_2561.method_43471("Применить"), var1x -> {
         ProxyConfig var2x = ProxyConfig.getInstance();
         if (this.enabledCheck.method_20372()) {
            if (this.checkProxy()) {
               Proxy var3x = new Proxy(this.isSocks4, this.ipPort.method_1882(), this.username.method_1882(), this.password.method_1882());
               var2x.setDefaultProxy(var3x);
               var2x.setProxyEnabled(true);
               var2x.save();
               class_310.method_1551().method_1507(new class_500(new class_442()));
            }
         } else {
            Proxy var4x = new Proxy(this.isSocks4, this.ipPort.method_1882(), this.username.method_1882(), this.password.method_1882());
            var2x.setDefaultProxy(var4x);
            var2x.setProxyEnabled(false);
            var2x.save();
            class_310.method_1551().method_1507(new class_500(new class_442()));
         }
      }).method_46434(var4 + (var1 / 2 - 62) * 2, this.positionY[7] - 10, var1 / 2 + 3, 20).method_46431();
      this.method_37063(var5);
      class_8929 var6 = class_4286.method_54787(class_2561.method_43471("Включить прокси"), this.field_22793);
      var6.method_54789(
         this.field_22789 / 2 - 34 - (13 + this.field_22793.method_27525(class_2561.method_43471("Включить прокси"))) / 2, this.positionY[7] + 15
      );
      if (var2.isProxyEnabled()) {
         var6.method_54794(true);
      }

      this.enabledCheck = var6.method_54788();
      this.method_37063(this.enabledCheck);
      class_4185 var7 = class_4185.method_46430(class_2561.method_43471("Отменить"), var1x -> class_310.method_1551().method_1507(this.parentScreen))
         .method_46434(var4 + (var1 / 2 - 16) * 2, this.positionY[7] - 10, var1 / 2 - 3, 20)
         .method_46431();
      this.method_37063(var7);
   }

   public void method_25419() {
      this.msg = "";
      class_310.method_1551().method_1507(this.parentScreen);
   }
}
