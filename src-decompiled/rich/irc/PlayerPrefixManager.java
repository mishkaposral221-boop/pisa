package rich.irc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import rich.util.b;

public class PlayerPrefixManager {
   private static final String API_URL = "http://5.189.159.34:8080";
   private static final PlayerPrefixManager INSTANCE = new PlayerPrefixManager();
   private final Map<String, String> prefixes = new HashMap<>();

   public static PlayerPrefixManager getInstance() {
      return INSTANCE;
   }

   public String getPrefix(String var1) {
      String var2 = b.a();
      return !var2.isEmpty() && this.prefixes.containsKey(var2) ? this.prefixes.get(var2) : this.prefixes.getOrDefault(var1, "");
   }

   public void loadPrefixes() {
      new Thread(() -> {
         try {
            HttpURLConnection var1 = (HttpURLConnection)new URL("http://5.189.159.34:8080/prefixes").openConnection();
            var1.setRequestMethod("GET");
            var1.setConnectTimeout(5000);
            var1.setReadTimeout(5000);
            if (var1.getResponseCode() != 200) {
               var1.disconnect();
               return;
            }

            StringBuilder var2 = new StringBuilder();

            String var4;
            try (BufferedReader var3 = new BufferedReader(new InputStreamReader(var1.getInputStream(), StandardCharsets.UTF_8))) {
               while ((var4 = var3.readLine()) != null) {
                  var2.append(var4);
               }
            }

            var1.disconnect();
            this.parsePrefixes(var2.toString());
         } catch (Exception var8) {
         }
      }, "prefix-load").start();
   }

   private void parsePrefixes(String var1) {
      this.prefixes.clear();
      int var2 = 0;

      while (var2 < var1.length()) {
         int var3 = var1.indexOf(34, var2);
         if (var3 == -1) {
            break;
         }

         int var4 = this.findStringEnd(var1, var3 + 1);
         if (var4 == -1) {
            break;
         }

         String var5 = this.decodeJsonString(var1, var3 + 1, var4);
         int var6 = var1.indexOf(58, var4 + 1);
         if (var6 == -1) {
            break;
         }

         int var7 = var6 + 1;

         while (var7 < var1.length() && var1.charAt(var7) == ' ') {
            var7++;
         }

         if (var7 < var1.length() && var1.charAt(var7) == '"') {
            int var8 = this.findStringEnd(var1, var7 + 1);
            if (var8 == -1) {
               break;
            }

            String var9 = this.decodeJsonString(var1, var7 + 1, var8);
            this.prefixes.put(var5, var9);
            var2 = var8 + 1;
         } else {
            var2 = var6 + 1;
         }
      }
   }

   private int findStringEnd(String var1, int var2) {
      int var3 = var2;

      while (var3 < var1.length()) {
         char var4 = var1.charAt(var3);
         if (var4 == '\\') {
            var3 += 2;
         } else {
            if (var4 == '"') {
               return var3;
            }

            var3++;
         }
      }

      return -1;
   }

   private String decodeJsonString(String var1, int var2, int var3) {
      StringBuilder var4 = new StringBuilder();
      int var5 = var2;

      while (var5 < var3) {
         char var6 = var1.charAt(var5);
         if (var6 == '\\' && var5 + 1 < var3) {
            char var7 = var1.charAt(var5 + 1);
            if (var7 == '"') {
               var4.append('"');
               var5 += 2;
            } else if (var7 == '\\') {
               var4.append('\\');
               var5 += 2;
            } else if (var7 == 'n') {
               var4.append('\n');
               var5 += 2;
            } else if (var7 == 'r') {
               var4.append('\r');
               var5 += 2;
            } else if (var7 == 't') {
               var4.append('\t');
               var5 += 2;
            } else {
               if (var7 == 'u' && var5 + 5 <= var3) {
                  try {
                     int var8 = Integer.parseInt(var1.substring(var5 + 2, var5 + 6), 16);
                     var4.append((char)var8);
                     var5 += 6;
                     continue;
                  } catch (NumberFormatException var9) {
                  }
               }

               var4.append(var7);
               var5 += 2;
            }
         } else {
            var4.append(var6);
            var5++;
         }
      }

      return var4.toString();
   }
}
