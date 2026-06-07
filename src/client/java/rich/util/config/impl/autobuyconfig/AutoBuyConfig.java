package rich.util.config.impl.autobuyconfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class AutoBuyConfig {
   private static AutoBuyConfig instance;
   private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
   private final Path configPath;
   private AutoBuyConfig.ConfigData data = new AutoBuyConfig.ConfigData();

   private AutoBuyConfig() {
      Path var1 = Paths.get("RunTime Visuals", "configs", "autobuy");
      this.configPath = var1.resolve("autobuy.json");
      this.load();
   }

   public static AutoBuyConfig getInstance() {
      if (instance == null) {
         instance = new AutoBuyConfig();
      }

      return instance;
   }

   public void load() {
      try {
         if (Files.exists(this.configPath)) {
            String var1 = Files.readString(this.configPath);
            AutoBuyConfig.ConfigData var2 = (AutoBuyConfig.ConfigData)this.gson.fromJson(var1, AutoBuyConfig.ConfigData.class);
            if (var2 != null) {
               this.data = var2;
               if (this.data.getItems() == null) {
                  this.data.setItems(new HashMap<>());
               }
            }
         }
      } catch (IOException var3) {
      }
   }

   public void save() {
      try {
         String var1 = this.gson.toJson(this.data);
         Files.writeString(this.configPath, var1);
      } catch (IOException var2) {
      }
   }

   public void reset() {
      this.data = new AutoBuyConfig.ConfigData();

      try {
         if (Files.exists(this.configPath)) {
            Files.delete(this.configPath);
         }
      } catch (IOException var2) {
      }

      this.save();
   }

   public boolean isGlobalEnabled() {
      return this.data.isGlobalEnabled();
   }

   public void setGlobalEnabled(boolean var1) {
      this.data.setGlobalEnabled(var1);
   }

   public void setGlobalEnabledAndSave(boolean var1) {
      this.data.setGlobalEnabled(var1);
      this.save();
   }

   public AutoBuyConfig.ItemConfig getItemConfig(String var1) {
      return this.data.getItems().computeIfAbsent(var1, var0 -> new AutoBuyConfig.ItemConfig());
   }

   public AutoBuyConfig.ItemConfig getItemConfigOrNull(String var1) {
      return this.data.getItems().get(var1);
   }

   public void setItemConfig(String var1, AutoBuyConfig.ItemConfig var2) {
      this.data.getItems().put(var1, var2);
   }

   public void setItemConfigAndSave(String var1, AutoBuyConfig.ItemConfig var2) {
      this.data.getItems().put(var1, var2);
      this.save();
   }

   public void setItemEnabled(String var1, boolean var2) {
      AutoBuyConfig.ItemConfig var3 = this.getItemConfig(var1);
      var3.setEnabled(var2);
   }

   public void setItemEnabledAndSave(String var1, boolean var2) {
      AutoBuyConfig.ItemConfig var3 = this.getItemConfig(var1);
      var3.setEnabled(var2);
      this.save();
   }

   public void setItemBuyBelow(String var1, int var2) {
      AutoBuyConfig.ItemConfig var3 = this.getItemConfig(var1);
      var3.setBuyBelow(var2);
   }

   public void setItemBuyBelowAndSave(String var1, int var2) {
      AutoBuyConfig.ItemConfig var3 = this.getItemConfig(var1);
      var3.setBuyBelow(var2);
      this.save();
   }

   public void setItemMinQuantity(String var1, int var2) {
      AutoBuyConfig.ItemConfig var3 = this.getItemConfig(var1);
      var3.setMinQuantity(var2);
   }

   public void setItemMinQuantityAndSave(String var1, int var2) {
      AutoBuyConfig.ItemConfig var3 = this.getItemConfig(var1);
      var3.setMinQuantity(var2);
      this.save();
   }

   public boolean isItemEnabled(String var1) {
      AutoBuyConfig.ItemConfig var2 = this.getItemConfigOrNull(var1);
      return var2 != null && var2.isEnabled();
   }

   public int getItemBuyBelow(String var1) {
      return this.getItemConfig(var1).getBuyBelow();
   }

   public int getItemMinQuantity(String var1) {
      return this.getItemConfig(var1).getMinQuantity();
   }

   public boolean hasItemConfig(String var1) {
      return this.data.getItems().containsKey(var1);
   }

   public void loadItemSettings(String var1, int var2) {
      if (!this.hasItemConfig(var1)) {
         AutoBuyConfig.ItemConfig var3 = new AutoBuyConfig.ItemConfig(false, var2, 1);
         this.data.getItems().put(var1, var3);
      }
   }

   public Map<String, AutoBuyConfig.ItemConfig> getAllItemConfigs() {
      return new HashMap<>(this.data.getItems());
   }

   public AutoBuyConfig.ConfigData getData() {
      return this.data;
   }

   public static class ConfigData {
      private boolean globalEnabled = false;
      private Map<String, AutoBuyConfig.ItemConfig> items = new HashMap<>();

      public boolean isGlobalEnabled() {
         return this.globalEnabled;
      }

      public Map<String, AutoBuyConfig.ItemConfig> getItems() {
         return this.items;
      }

      public void setGlobalEnabled(boolean var1) {
         this.globalEnabled = var1;
      }

      public void setItems(Map<String, AutoBuyConfig.ItemConfig> var1) {
         this.items = var1;
      }
   }

   public static class ItemConfig {
      private boolean enabled = false;
      private int buyBelow = 1000;
      private int minQuantity = 1;

      public ItemConfig() {
      }

      public ItemConfig(boolean var1, int var2, int var3) {
         this.enabled = var1;
         this.buyBelow = var2;
         this.minQuantity = var3;
      }

      public boolean isEnabled() {
         return this.enabled;
      }

      public int getBuyBelow() {
         return this.buyBelow;
      }

      public int getMinQuantity() {
         return this.minQuantity;
      }

      public void setEnabled(boolean var1) {
         this.enabled = var1;
      }

      public void setBuyBelow(int var1) {
         this.buyBelow = var1;
      }

      public void setMinQuantity(int var1) {
         this.minQuantity = var1;
      }
   }
}
