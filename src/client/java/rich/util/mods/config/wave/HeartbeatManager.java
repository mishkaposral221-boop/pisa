package rich.util.mods.config.wave;

import antidaunleak.api.annotation.Native;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeartbeatManager {
   private static ScheduledExecutorService scheduler;
   private static String systemHwid;
   private static String profileHwid;
   private static String currentUsername;
   private static String currentUid;

   @Native(type = Native.Type.VMProtectBeginUltra)
   private static String g1() {
      char[] var0 = new char[]{
         'h', 't', 't', 'p', ':', '/', '/', '8', '7', '.', '1', '2', '0', '.', '1', '8', '6', '.', '1', '8', '6', ':', '3', '0', '0', '0'
      };
      StringBuilder var1 = new StringBuilder();

      for (char var5 : var0) {
         var1.append(var5);
      }

      return var1.toString();
   }

   @Native(type = Native.Type.VMProtectBeginUltra)
   private static String g2() {
      char[] var0 = new char[]{
         'V', 'M', '$', 'U', 'v', 'w', '9', 'u', '6', 'W', 'C', 'U', '6', '5', '9', '0', 'w', 'q', '6', 'u', 'j', 't', 'e', 'g', 's', 'a'
      };
      StringBuilder var1 = new StringBuilder();

      for (char var5 : var0) {
         var1.append(var5);
      }

      return var1.toString();
   }

   @Native(type = Native.Type.VMProtectBeginUltra)
   private static String g3() {
      char[] var0 = new char[]{'/', 'a', 'p', 'i', '/', 'r', 'e', 'g', 'i', 's', 't', 'e', 'r'};
      StringBuilder var1 = new StringBuilder();

      for (char var5 : var0) {
         var1.append(var5);
      }

      return var1.toString();
   }

   @Native(type = Native.Type.VMProtectBeginUltra)
   private static String g4() {
      char[] var0 = new char[]{'/', 'a', 'p', 'i', '/', 'h', 'e', 'a', 'r', 't', 'b', 'e', 'a', 't'};
      StringBuilder var1 = new StringBuilder();

      for (char var5 : var0) {
         var1.append(var5);
      }

      return var1.toString();
   }

   @Native(type = Native.Type.VMProtectBeginUltra)
   private static String g5() {
      char[] var0 = new char[]{'/', 'a', 'p', 'i', '/', 'o', 'f', 'f', 'l', 'i', 'n', 'e'};
      StringBuilder var1 = new StringBuilder();

      for (char var5 : var0) {
         var1.append(var5);
      }

      return var1.toString();
   }

   public static String getSystemHwid() {
      return systemHwid != null ? systemHwid : "";
   }

   @Native(type = Native.Type.VMProtectBeginUltra)
   public static void start(String var0, String var1, String var2, String var3) {
      systemHwid = var0;
      profileHwid = var1;
      currentUsername = var2;
      currentUid = var3;
      (new Thread(() -> {
         register();
         scheduler = Executors.newSingleThreadScheduledExecutor(var0x -> {
            Thread var1x = new Thread(var0x, "heartbeat-scheduler");
            var1x.setDaemon(true);
            return var1x;
         });
         scheduler.scheduleAtFixedRate(HeartbeatManager::heartbeat, 0L, 10L, TimeUnit.SECONDS);
      }, "heartbeat-init") {
         {
            this.setDaemon(true);
         }
      }).start();
      Runtime.getRuntime().addShutdownHook(new Thread(HeartbeatManager::offline));
   }

   @Native(type = Native.Type.VMProtectBeginUltra)
   private static void register() {
      try {
         String var0 = String.format(
            "{\"secret\":\"%s\",\"systemHwid\":\"%s\",\"profileHwid\":\"%s\",\"username\":\"%s\",\"uid\":\"%s\"}",
            g2(),
            escape(systemHwid),
            escape(profileHwid != null ? profileHwid : ""),
            escape(currentUsername),
            escape(currentUid)
         );
         sendPost(g1() + g3(), var0);
      } catch (Exception var1) {
      }
   }

   @Native(type = Native.Type.VMProtectBeginUltra)
   private static void heartbeat() {
      try {
         String var0 = String.format(
            "{\"secret\":\"%s\",\"systemHwid\":\"%s\",\"profileHwid\":\"%s\"}", g2(), escape(systemHwid), escape(profileHwid != null ? profileHwid : "")
         );
         String var1 = sendPost(g1() + g4(), var0);
         if (var1 != null && (var1.contains("\"kill\":true") || var1.contains("\"banned\":true"))) {
            shutdown();
         }
      } catch (Exception var2) {
      }
   }

   @Native(type = Native.Type.VMProtectBeginUltra)
   private static void offline() {
      try {
         String var0 = String.format(
            "{\"secret\":\"%s\",\"systemHwid\":\"%s\",\"profileHwid\":\"%s\"}", g2(), escape(systemHwid), escape(profileHwid != null ? profileHwid : "")
         );
         sendPost(g1() + g5(), var0);
      } catch (Exception var1) {
      }
   }

   @Native(type = Native.Type.VMProtectBeginUltra)
   private static String sendPost(String var0, String var1) {
      try {
         URL var2 = new URL(var0);
         HttpURLConnection var3 = (HttpURLConnection)var2.openConnection();
         var3.setRequestMethod(d("UE9TVA=="));
         var3.setRequestProperty(d("Q29udGVudC1UeXBl"), d("YXBwbGljYXRpb24vanNvbg=="));
         var3.setRequestProperty(d("VXNlci1BZ2VudA=="), d("UmljaENsaWVudC8yLjA="));
         var3.setDoOutput(true);
         var3.setConnectTimeout(5000);
         var3.setReadTimeout(5000);

         try (OutputStream var4 = var3.getOutputStream()) {
            var4.write(var1.getBytes(StandardCharsets.UTF_8));
         }

         int var14 = var3.getResponseCode();
         if (var14 == 200) {
            try (BufferedReader var5 = new BufferedReader(new InputStreamReader(var3.getInputStream(), StandardCharsets.UTF_8))) {
               StringBuilder var6 = new StringBuilder();

               String var7;
               while ((var7 = var5.readLine()) != null) {
                  var6.append(var7);
               }

               return var6.toString();
            }
         }
      } catch (Exception var13) {
      }

      return null;
   }

   @Native(type = Native.Type.VMProtectBeginUltra)
   private static String escape(String var0) {
      return var0 == null ? "" : var0.replace("\\", "\\\\").replace("\"", "\\\"");
   }

   @Native(type = Native.Type.VMProtectBeginUltra)
   private static void shutdown() {
      try {
         Runtime.getRuntime().halt(0);
      } catch (Throwable var1) {
         System.exit(0);
      }
   }

   @Native(type = Native.Type.VMProtectBeginUltra)
   private static String d(String var0) {
      try {
         return new String(Base64.getDecoder().decode(var0), StandardCharsets.UTF_8);
      } catch (Exception var2) {
         return "";
      }
   }
}
