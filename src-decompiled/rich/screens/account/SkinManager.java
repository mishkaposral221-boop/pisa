package rich.screens.account;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import net.minecraft.class_1071;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_8685;

public class SkinManager {
   private static final Map<String, class_2960> SKIN_CACHE = new ConcurrentHashMap<>();
   private static final Map<String, Boolean> LOADING = new ConcurrentHashMap<>();
   private static final class_2960 STEVE_SKIN = class_2960.method_60655("minecraft", "textures/entity/player/wide/steve.png");
   private static final class_2960 ALEX_SKIN = class_2960.method_60655("minecraft", "textures/entity/player/wide/alex.png");
   private static final Executor EXECUTOR = Executors.newFixedThreadPool(3, var0 -> {
      Thread var1 = new Thread(var0, "SkinLoader");
      var1.setDaemon(true);
      return var1;
   });

   public static class_2960 getSkin(String var0) {
      if (var0 != null && !var0.isEmpty()) {
         String var1 = var0.toLowerCase();
         class_2960 var2 = SKIN_CACHE.get(var1);
         if (var2 != null) {
            return var2;
         }

         if (!LOADING.containsKey(var1)) {
            LOADING.put(var1, true);
            loadSkinAsync(var0);
         }

         return getDefaultSkin(var0);
      } else {
         return STEVE_SKIN;
      }
   }

   private static class_2960 getDefaultSkin(String var0) {
      UUID var1 = UUID.nameUUIDFromBytes(("OfflinePlayer:" + var0).getBytes());
      return (var1.hashCode() & 1) == 0 ? STEVE_SKIN : ALEX_SKIN;
   }

   private static void loadSkinAsync(String var0) {
      String var1 = var0.toLowerCase();
      CompletableFuture.runAsync(() -> {
         try {
            UUID var2 = fetchUUID(var0);
            if (var2 == null) {
               LOADING.remove(var1);
               return;
            }

            class_310 var3 = class_310.method_1551();
            if (var3 != null) {
               GameProfile var4 = new GameProfile(var2, var0);
               class_1071 var5 = var3.method_1582();
               CompletableFuture var6 = var5.method_52863(var4);
               Optional var7 = (Optional)var6.join();
               if (var7.isPresent()) {
                  class_8685 var8 = (class_8685)var7.get();
                  if (var8.comp_1626() != null) {
                     class_2960 var9 = var8.comp_1626().comp_3627();
                     if (var9 != null) {
                        SKIN_CACHE.put(var1, var9);
                     }

                     return;
                  }
               }

               return;
            }

            LOADING.remove(var1);
         } catch (Exception var13) {
            return;
         } finally {
            LOADING.remove(var1);
         }
      }, EXECUTOR);
   }

   private static UUID fetchUUID(String var0) {
      try {
         URL var1 = new URL("https://api.mojang.com/users/profiles/minecraft/" + var0);
         HttpURLConnection var2 = (HttpURLConnection)var1.openConnection();
         var2.setRequestMethod("GET");
         var2.setConnectTimeout(5000);
         var2.setReadTimeout(5000);
         var2.setRequestProperty("User-Agent", "Mozilla/5.0");
         int var3 = var2.getResponseCode();
         if (var3 != 200) {
            var2.disconnect();
            return null;
         }

         try (InputStreamReader var4 = new InputStreamReader(var2.getInputStream())) {
            JsonObject var5 = JsonParser.parseReader(var4).getAsJsonObject();
            if (var5.has("id")) {
               String var6 = var5.get("id").getAsString();
               var2.disconnect();
               return parseUUID(var6);
            }
         }

         var2.disconnect();
      } catch (Exception var10) {
      }

      return null;
   }

   private static UUID parseUUID(String var0) {
      try {
         if (var0.length() == 32) {
            var0 = var0.substring(0, 8) + "-" + var0.substring(8, 12) + "-" + var0.substring(12, 16) + "-" + var0.substring(16, 20) + "-" + var0.substring(20);
         }

         return UUID.fromString(var0);
      } catch (Exception var2) {
         return null;
      }
   }

   public static void clearCache() {
      SKIN_CACHE.clear();
      LOADING.clear();
   }

   public static void removeSkin(String var0) {
      if (var0 != null) {
         SKIN_CACHE.remove(var0.toLowerCase());
         LOADING.remove(var0.toLowerCase());
      }
   }

   public static void reloadSkin(String var0) {
      removeSkin(var0);
      getSkin(var0);
   }
}
