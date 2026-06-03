package rich.screens.clickgui.impl.configs.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import rich.util.config.ConfigSystem;
import rich.util.config.impl.ConfigPath;

public class ConfigDataHandler {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneId.systemDefault());
   private final List<ConfigDataHandler.ConfigEntry> configs = new ArrayList<>();
   private final ConfigAnimationHandler animationHandler;
   private boolean isCreating = false;
   private String newConfigName = "";
   private String newConfigAuthor = "";
   private boolean authorFieldFocused = false;
   private String searchQuery = "";
   private boolean searchFocused = false;
   private double scrollOffset = 0.0;
   private double targetScrollOffset = 0.0;

   public ConfigDataHandler(ConfigAnimationHandler var1) {
      this.animationHandler = var1;
   }

   public List<ConfigDataHandler.ConfigEntry> getFilteredConfigs() {
      if (this.searchQuery.isEmpty()) {
         return this.configs;
      }

      ArrayList var1 = new ArrayList();
      String var2 = this.searchQuery.toLowerCase();

      for (ConfigDataHandler.ConfigEntry var4 : this.configs) {
         if (var4.name.toLowerCase().contains(var2)) {
            var1.add(var4);
         }
      }

      return var1;
   }

   public void refreshConfigs() {
      ArrayList var1 = new ArrayList();

      for (ConfigDataHandler.ConfigEntry var3 : this.configs) {
         var1.add(var3.name);
      }

      this.configs.clear();

      try {
         Path var5 = ConfigPath.getConfigDirectory();
         if (!Files.exists(var5)) {
            return;
         }

         Files.list(var5).filter(var0 -> var0.toString().endsWith(".json")).forEach(var1x -> {
            String var2 = var1x.getFileName().toString();
            String var3x = var2.substring(0, var2.length() - 5);
            if (!var3x.equalsIgnoreCase("autoconfig")) {
               String var4x = this.readAuthor(var1x);
               String var5x = this.readDate(var1x);
               this.configs.add(new ConfigDataHandler.ConfigEntry(var3x, var4x, var5x));
            }
         });
      } catch (IOException var4) {
      }

      for (ConfigDataHandler.ConfigEntry var7 : this.configs) {
         if (!var1.contains(var7.name)) {
            this.animationHandler.getItemAppearAnimations().put(var7.name, 0.0F);
         }
      }
   }

   private String readAuthor(Path var1) {
      try {
         String var2 = Files.readString(var1, StandardCharsets.UTF_8);
         JsonObject var3 = JsonParser.parseString(var2).getAsJsonObject();
         if (var3.has("author")) {
            return var3.get("author").getAsString();
         }
      } catch (Exception var4) {
      }

      return "Unknown";
   }

   private String readDate(Path var1) {
      try {
         BasicFileAttributes var2 = Files.readAttributes(var1, BasicFileAttributes.class);
         Instant var3 = var2.creationTime().toInstant();
         return DATE_FMT.format(var3);
      } catch (Exception var4) {
         return "—";
      }
   }

   public void updateScroll(float var1) {
      this.scrollOffset = this.scrollOffset + (this.targetScrollOffset - this.scrollOffset) * 12.0 * var1;
   }

   public void handleScroll(double var1, float var3) {
      float var4 = 38.0F;
      float var5 = this.getFilteredConfigs().size() * var4;
      float var6 = Math.max(0.0F, var5 - var3);
      this.targetScrollOffset += var1 * 25.0;
      this.targetScrollOffset = Math.max(-var6, Math.min(0.0, this.targetScrollOffset));
   }

   public boolean saveConfig(String var1, String var2) {
      if (var1.equalsIgnoreCase("autoconfig")) {
         return false;
      }

      try {
         Path var3 = ConfigPath.getConfigDirectory();
         Path var4 = var3.resolve(var1 + ".json");
         if (Files.exists(var4)) {
            return false;
         }

         ConfigSystem.getInstance().save();
         Path var5 = ConfigPath.getConfigFile();
         String var6 = Files.readString(var5, StandardCharsets.UTF_8);
         JsonObject var7 = JsonParser.parseString(var6).getAsJsonObject();
         var7.addProperty("author", var2.isEmpty() ? "Unknown" : var2);
         Files.writeString(var4, GSON.toJson(var7), StandardCharsets.UTF_8);
         this.refreshConfigs();
         return true;
      } catch (Exception var8) {
         return false;
      }
   }

   public boolean loadConfig(String var1) {
      try {
         Path var2 = ConfigPath.getConfigDirectory();
         Path var3 = var2.resolve(var1 + ".json");
         if (!Files.exists(var3)) {
            return false;
         }

         String var4 = Files.readString(var3, StandardCharsets.UTF_8);
         ConfigSystem.getInstance().getSerializer().deserialize(var4);
         return true;
      } catch (Exception var5) {
         return false;
      }
   }

   public boolean deleteConfig(String var1) {
      try {
         Path var2 = ConfigPath.getConfigDirectory();
         Path var3 = var2.resolve(var1 + ".json");
         if (Files.exists(var3)) {
            Files.delete(var3);
            this.refreshConfigs();
            return true;
         } else {
            return false;
         }
      } catch (Exception var4) {
         return false;
      }
   }

   public void toggleCreating() {
      this.isCreating = !this.isCreating;
      if (!this.isCreating) {
         this.newConfigName = "";
         this.newConfigAuthor = "";
         this.authorFieldFocused = false;
      }
   }

   public void appendChar(char var1) {
      if (this.authorFieldFocused) {
         if (this.newConfigAuthor.length() < 32) {
            this.newConfigAuthor = this.newConfigAuthor + var1;
         }
      } else if (this.newConfigName.length() < 32 && (Character.isLetterOrDigit(var1) || var1 == '_' || var1 == '-')) {
         this.newConfigName = this.newConfigName + var1;
      }
   }

   public void removeLastChar() {
      if (this.authorFieldFocused) {
         if (!this.newConfigAuthor.isEmpty()) {
            this.newConfigAuthor = this.newConfigAuthor.substring(0, this.newConfigAuthor.length() - 1);
         }
      } else if (!this.newConfigName.isEmpty()) {
         this.newConfigName = this.newConfigName.substring(0, this.newConfigName.length() - 1);
      }
   }

   public void appendSearchChar(char var1) {
      this.searchQuery = this.searchQuery + var1;
      this.targetScrollOffset = 0.0;
      this.scrollOffset = 0.0;
   }

   public void removeSearchChar() {
      if (!this.searchQuery.isEmpty()) {
         this.searchQuery = this.searchQuery.substring(0, this.searchQuery.length() - 1);
      }
   }

   public void clearNewConfig() {
      this.newConfigName = "";
      this.newConfigAuthor = "";
      this.authorFieldFocused = false;
   }

   public List<ConfigDataHandler.ConfigEntry> getConfigs() {
      return this.configs;
   }

   public ConfigAnimationHandler getAnimationHandler() {
      return this.animationHandler;
   }

   public boolean isCreating() {
      return this.isCreating;
   }

   public String getNewConfigName() {
      return this.newConfigName;
   }

   public String getNewConfigAuthor() {
      return this.newConfigAuthor;
   }

   public boolean isAuthorFieldFocused() {
      return this.authorFieldFocused;
   }

   public String getSearchQuery() {
      return this.searchQuery;
   }

   public boolean isSearchFocused() {
      return this.searchFocused;
   }

   public double getScrollOffset() {
      return this.scrollOffset;
   }

   public double getTargetScrollOffset() {
      return this.targetScrollOffset;
   }

   public void setCreating(boolean var1) {
      this.isCreating = var1;
   }

   public void setNewConfigName(String var1) {
      this.newConfigName = var1;
   }

   public void setNewConfigAuthor(String var1) {
      this.newConfigAuthor = var1;
   }

   public void setAuthorFieldFocused(boolean var1) {
      this.authorFieldFocused = var1;
   }

   public void setSearchQuery(String var1) {
      this.searchQuery = var1;
   }

   public void setSearchFocused(boolean var1) {
      this.searchFocused = var1;
   }

   public void setScrollOffset(double var1) {
      this.scrollOffset = var1;
   }

   public void setTargetScrollOffset(double var1) {
      this.targetScrollOffset = var1;
   }

   public static class ConfigEntry {
      public final String name;
      public final String author;
      public final String date;

      public ConfigEntry(String var1, String var2, String var3) {
         this.name = var1;
         this.author = var2;
         this.date = var3;
      }
   }
}
