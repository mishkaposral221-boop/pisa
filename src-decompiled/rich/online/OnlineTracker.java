package rich.online;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import rich.util.a.b;

public class OnlineTracker {
   private static final String API_URL = b.a(new long[]{4179650672494529044L, 8954376986468044824L, 8954944338930785553L});
   private static final int HEARTBEAT_SEC = 30;
   private static final OnlineTracker INSTANCE = new OnlineTracker();
   private final AtomicInteger cachedOnline = new AtomicInteger(0);
   private final String uuid = UUID.randomUUID().toString();
   private volatile String username = "Unknown";
   private volatile boolean running = false;
   private volatile Thread thread = null;
   private final Set<String> runtimeUsers = Collections.synchronizedSet(new HashSet<>());

   public static OnlineTracker getInstance() {
      return INSTANCE;
   }

   public void setUsername(String var1) {
      this.username = var1;
   }

   public int getOnline() {
      return this.cachedOnline.get();
   }

   public boolean isRuntimeUser(String var1) {
      return this.runtimeUsers.contains(var1);
   }

   public void start() {
      if (!this.running) {
         this.running = true;
         this.thread = new Thread(this::loop, "online-tracker");
         this.thread.setDaemon(true);
         this.thread.start();
      }
   }

   public void stop() {
      this.running = false;
      if (this.thread != null) {
         this.thread.interrupt();
      }
   }

   private void loop() {
      while (this.running) {
         try {
            this.sendHeartbeat();
         } catch (Exception var2) {
         }

         try {
            Thread.sleep(30000L);
         } catch (InterruptedException var3) {
            break;
         }
      }
   }

   private void sendHeartbeat() throws Exception {
      String var1 = rich.util.b.a();
      String var2 = "{\""
         + b.a(new long[]{2810254071987791137L})
         + "\":\""
         + this.uuid
         + "\",\""
         + b.a(new long[]{2811947275294345284L})
         + "\":\""
         + this.username
         + "\",\""
         + b.a(new long[]{4179911308661843233L})
         + "\":\""
         + var1
         + "\"}";
      HttpURLConnection var3 = (HttpURLConnection)new URL(API_URL + b.a(new long[]{9015374351781398340L, 3675340079736120609L})).openConnection();
      var3.setConnectTimeout(5000);
      var3.setReadTimeout(5000);
      var3.setRequestMethod(b.a(new long[]{160507808703145249L}));
      var3.setRequestProperty(b.a(new long[]{1232297585679733004L, 435756461519103265L}), b.a(new long[]{3676377896685798485L, 4258716925965503055L}));
      var3.setDoOutput(true);

      try (OutputStream var4 = var3.getOutputStream()) {
         var4.write(var2.getBytes());
      }

      int var13 = var3.getResponseCode();
      if (var13 == 200) {
         StringBuilder var5 = new StringBuilder();

         String var7;
         try (BufferedReader var6 = new BufferedReader(new InputStreamReader(var3.getInputStream()))) {
            while ((var7 = var6.readLine()) != null) {
               var5.append(var7);
            }
         }

         this.parseResponse(var5.toString());
      }

      var3.disconnect();
   }

   private void parseResponse(String var1) {
      int var2 = var1.indexOf(b.a(new long[]{8077769122160771075L}));
      if (var2 != -1) {
         int var3 = var1.indexOf(58, var2);
         if (var3 != -1) {
            int var4 = var3 + 1;

            while (var4 < var1.length() && var1.charAt(var4) == ' ') {
               var4++;
            }

            int var5 = var4;

            while (var5 < var1.length() && Character.isDigit(var1.charAt(var5))) {
               var5++;
            }

            if (var5 > var4) {
               try {
                  this.cachedOnline.set(Integer.parseInt(var1.substring(var4, var5)));
               } catch (NumberFormatException var13) {
               }
            }
         }
      }

      HashSet var14 = new HashSet();
      int var15 = var1.indexOf(b.a(new long[]{8070482628993500961L}));
      if (var15 != -1) {
         int var16 = var1.indexOf(91, var15);
         int var6 = var1.indexOf(93, var16);
         if (var16 != -1 && var6 != -1) {
            String var7 = var1.substring(var16 + 1, var6);

            for (String var11 : var7.split(",")) {
               String var12 = var11.trim().replace("\"", "");
               if (!var12.isEmpty()) {
                  var14.add(var12);
               }
            }
         }
      }

      this.runtimeUsers.clear();
      this.runtimeUsers.addAll(var14);
   }
}
