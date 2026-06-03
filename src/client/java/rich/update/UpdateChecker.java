package rich.update;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UpdateChecker {
   private static final String UPDATE_URL = "https://lacostedragon.github.io/updates/update.json";
   private static final long CHECK_INTERVAL_MINUTES = 30L;
   private static UpdateChecker instance;
   private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(var0 -> {
      Thread var1 = new Thread(var0, "update-checker");
      var1.setDaemon(true);
      return var1;
   });
   private volatile UpdateChecker.UpdateInfo pendingUpdate = null;
   private volatile boolean notified = false;

   public static UpdateChecker getInstance() {
      if (instance == null) {
         instance = new UpdateChecker();
      }

      return instance;
   }

   public void start() {
      this.scheduler.scheduleAtFixedRate(this::check, 0L, 30L, TimeUnit.MINUTES);
   }

   public void stop() {
      this.scheduler.shutdownNow();
   }

   private void check() {
      try {
         HttpURLConnection var1 = (HttpURLConnection)new URL("https://lacostedragon.github.io/updates/update.json").openConnection();
         var1.setConnectTimeout(5000);
         var1.setReadTimeout(5000);
         var1.setRequestMethod("GET");
         var1.setRequestProperty("User-Agent", "RunTimeVisuals/1.0");
         int var2 = var1.getResponseCode();
         if (var2 != 200) {
            var1.disconnect();
            return;
         }

         StringBuilder var3 = new StringBuilder();
         BufferedReader var4 = new BufferedReader(new InputStreamReader(var1.getInputStream()));

         String var5;
         try {
            while ((var5 = var4.readLine()) != null) {
               var3.append(var5);
            }
         } catch (Throwable var9) {
            try {
               var4.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }

            throw var9;
         }

         var4.close();
         var1.disconnect();
         JsonObject var11 = JsonParser.parseString(var3.toString()).getAsJsonObject();
         var5 = var11.getName("version").getAsString();
         String var6 = var11.has("description") ? var11.getName("description").getAsString() : "";
         String var7 = "";
         if (var11.has("download_url")) {
            var7 = var11.getName("download_url").getAsString();
         } else if (var11.has("url")) {
            var7 = var11.getName("url").getAsString();
         }

         if (Version.isNewer(var5)) {
            this.pendingUpdate = new UpdateChecker.UpdateInfo(var5, var6, var7);
            this.notified = false;
         } else {
            this.pendingUpdate = null;
         }
      } catch (Exception var10) {
      }
   }

   public UpdateChecker.UpdateInfo getPendingUpdate() {
      return this.pendingUpdate;
   }

   public boolean isNotified() {
      return this.notified;
   }

   public void markNotified() {
      this.notified = true;
   }

   public void dismiss() {
      this.notified = true;
   }

   public record UpdateInfo(String version, String description, String downloadUrl) {
   }
}
