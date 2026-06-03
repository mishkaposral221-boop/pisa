package rich.util;

import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.Enumeration;

public class b {
   private static String a = null;

   public static String a() {
      if (a != null) {
         return a;
      }

      try {
         StringBuilder var0 = new StringBuilder();
         Enumeration var1 = NetworkInterface.getNetworkInterfaces();
         if (var1 != null) {
            for (NetworkInterface var3 : Collections.list(var1)) {
               byte[] var4 = var3.getHardwareAddress();
               if (var4 != null && var4.length > 0) {
                  for (byte var8 : var4) {
                     var0.append(String.format("%02X", var8));
                  }
               }
            }
         }

         var0.append(System.getProperty("os.name", ""));
         var0.append(System.getProperty("os.arch", ""));
         var0.append(System.getProperty("user.name", ""));
         var0.append(Runtime.getRuntime().availableProcessors());
         MessageDigest var10 = MessageDigest.getInstance("SHA-256");
         byte[] var11 = var10.digest(var0.toString().getBytes(StandardCharsets.UTF_8));
         StringBuilder var12 = new StringBuilder();

         for (byte var17 : var11) {
            var12.append(String.format("%02X", var17));
         }

         String var14 = var12.toString();
         a = var14.substring(0, 8) + "-" + var14.substring(8, 16) + "-" + var14.substring(16, 24) + "-" + var14.substring(24, 32);
      } catch (Exception var9) {
         a = "UNKNOWN-HWID";
      }

      return a;
   }
}
