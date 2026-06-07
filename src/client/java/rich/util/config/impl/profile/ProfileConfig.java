package rich.util.config.impl.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProfileConfig {
   private static ProfileConfig instance;
   private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
   private final Path configPath;
   private String avatarPath = "";
   private String bannerPath = "";

   private ProfileConfig() {
      Path var1 = Paths.get("RunTime Visuals", "configs");

      try {
         Files.createDirectories(var1);
      } catch (IOException var3) {
      }

      this.configPath = var1.resolve("profile.json");
      this.load();
   }

   public static ProfileConfig getInstance() {
      if (instance == null) {
         instance = new ProfileConfig();
      }

      return instance;
   }

   public String getAvatarPath() {
      return this.avatarPath;
   }

   public String getBannerPath() {
      return this.bannerPath;
   }

   public void setAvatarPath(String var1) {
      this.avatarPath = var1 != null ? var1 : "";
      this.save();
   }

   public void setBannerPath(String var1) {
      this.bannerPath = var1 != null ? var1 : "";
      this.save();
   }

   public boolean hasCustomAvatar() {
      return !this.avatarPath.isEmpty() && Files.exists(Paths.get(this.avatarPath));
   }

   public boolean hasCustomBanner() {
      return !this.bannerPath.isEmpty() && Files.exists(Paths.get(this.bannerPath));
   }

   public void save() {
      try {
         JsonObject var1 = new JsonObject();
         var1.addProperty("avatarPath", this.avatarPath);
         var1.addProperty("bannerPath", this.bannerPath);
         Files.writeString(this.configPath, this.gson.toJson(var1), StandardCharsets.UTF_8);
      } catch (IOException var2) {
      }
   }

   public void load() {
      try {
         if (!Files.exists(this.configPath)) {
            return;
         }

         String var1 = Files.readString(this.configPath, StandardCharsets.UTF_8);
         JsonObject var2 = JsonParser.parseString(var1).getAsJsonObject();
         if (var2.has("avatarPath")) {
            this.avatarPath = var2.get("avatarPath").getAsString();
         }

         if (var2.has("bannerPath")) {
            this.bannerPath = var2.get("bannerPath").getAsString();
         }
      } catch (Exception var3) {
      }
   }
}
