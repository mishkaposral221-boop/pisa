package rich.util.config.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import rich.Initialization;
import rich.modules.module.ModuleRepository;
import rich.modules.module.ModuleStructure;
import rich.modules.module.setting.Setting;
import rich.modules.module.setting.implement.BindSetting;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.GroupSetting;
import rich.modules.module.setting.implement.MultiSelectSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.modules.module.setting.implement.TextSetting;
import rich.theme.ClientTheme;
import rich.util.config.impl.autobuyconfig.AutoBuyConfig;
import rich.util.config.impl.consolelogger.Logger;

public class ConfigSerializer {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

   public String serialize() {
      JsonObject var1 = new JsonObject();
      JsonObject var2 = new JsonObject();
      ModuleRepository var3 = this.getModuleRepository();
      if (var3 != null) {
         for (ModuleStructure var5 : var3.allModules()) {
            JsonObject var6 = this.serializeModule(var5);
            var2.add(var5.getName(), var6);
         }
      }

      var1.add("modules", var2);
      var1.add("autobuy", this.serializeAutoBuy());
      var1.addProperty("theme", ClientTheme.get().name());
      var1.addProperty("version", "2.0");
      var1.addProperty("timestamp", System.currentTimeMillis());
      var1.addProperty("client", "RunTime Visuals");
      return GSON.toJson(var1);
   }

   private JsonObject serializeAutoBuy() {
      JsonObject var1 = new JsonObject();
      AutoBuyConfig var2 = AutoBuyConfig.getInstance();
      var1.addProperty("globalEnabled", var2.isGlobalEnabled());
      JsonObject var3 = new JsonObject();
      Map<String, AutoBuyConfig.ItemConfig> var4 = var2.getAllItemConfigs();

      for (Map.Entry<String, AutoBuyConfig.ItemConfig> var6 : var4.entrySet()) {
         JsonObject var7 = new JsonObject();
         AutoBuyConfig.ItemConfig var8 = (AutoBuyConfig.ItemConfig)var6.getValue();
         var7.addProperty("enabled", var8.isEnabled());
         var7.addProperty("buyBelow", var8.getBuyBelow());
         var7.addProperty("minQuantity", var8.getMinQuantity());
         var3.add((String)var6.getKey(), var7);
      }

      var1.add("items", var3);
      return var1;
   }

   private JsonObject serializeModule(ModuleStructure var1) {
      JsonObject var2 = new JsonObject();
      var2.addProperty("enabled", var1.isState());
      var2.addProperty("key", var1.getKey());
      var2.addProperty("type", var1.getType());
      var2.addProperty("favorite", var1.isFavorite());
      JsonObject var3 = new JsonObject();

      for (Setting var5 : var1.settings()) {
         JsonElement var6 = this.serializeSetting(var5);
         if (var6 != null) {
            var3.add(var5.getName(), var6);
         }
      }

      var2.add("settings", var3);
      return var2;
   }

   private JsonElement serializeSetting(Setting var1) {
      if (var1 instanceof BooleanSetting var14) {
         return new JsonPrimitive(var14.isValue());
      } else if (var1 instanceof SliderSettings var13) {
         return new JsonPrimitive(var13.getValue());
      } else if (var1 instanceof BindSetting var12) {
         JsonObject var17 = new JsonObject();
         var17.addProperty("key", var12.getKey());
         var17.addProperty("type", var12.getType());
         return var17;
      } else if (var1 instanceof TextSetting var11) {
         return new JsonPrimitive(var11.getText() != null ? var11.getText() : "");
      } else if (var1 instanceof SelectSetting var10) {
         return new JsonPrimitive(var10.getSelected());
      } else if (var1 instanceof ColorSetting var9) {
         JsonObject var16 = new JsonObject();
         var16.addProperty("hue", var9.getHue());
         var16.addProperty("saturation", var9.getSaturation());
         var16.addProperty("brightness", var9.getBrightness());
         var16.addProperty("alpha", var9.getAlpha());
         return var16;
      } else if (!(var1 instanceof MultiSelectSetting var2)) {
         if (var1 instanceof GroupSetting var8) {
            JsonObject var15 = new JsonObject();
            var15.addProperty("value", var8.isValue());
            JsonObject var18 = new JsonObject();

            for (Setting var6 : var8.getSubSettings()) {
               JsonElement var7 = this.serializeSetting(var6);
               if (var7 != null) {
                  var18.add(var6.getName(), var7);
               }
            }

            var15.add("subSettings", var18);
            return var15;
         } else {
            return null;
         }
      } else {
         JsonArray var3 = new JsonArray();

         for (String var5 : var2.getSelected()) {
            var3.add(var5);
         }

         return var3;
      }
   }

   public void deserialize(String var1) {
      try {
         JsonObject var2 = JsonParser.parseString(var1).getAsJsonObject();
         if (var2.has("theme")) {
            try {
               ClientTheme.setQuiet(ClientTheme.Theme.valueOf(var2.get("theme").getAsString()));
            } catch (IllegalArgumentException var7) {
            }
         }

         if (var2.has("modules")) {
            JsonObject var3 = var2.getAsJsonObject("modules");
            ModuleRepository var4 = this.getModuleRepository();
            if (var4 != null) {
               for (ModuleStructure var6 : var4.allModules()) {
                  if (var3.has(var6.getName())) {
                     this.deserializeModule(var6, var3.getAsJsonObject(var6.getName()));
                  }
               }
            }
         }

         if (var2.has("autobuy")) {
            this.deserializeAutoBuy(var2.getAsJsonObject("autobuy"));
         }
      } catch (JsonSyntaxException var8) {
         Logger.error("AutoConfiguration: JSON syntax error!");
      }
   }

   private void deserializeAutoBuy(JsonObject var1) {
      AutoBuyConfig var2 = AutoBuyConfig.getInstance();
      if (var1.has("globalEnabled")) {
         var2.setGlobalEnabled(var1.get("globalEnabled").getAsBoolean());
      }

      if (var1.has("items")) {
         JsonObject var3 = var1.getAsJsonObject("items");

         for (Entry var5 : var3.entrySet()) {
            String var6 = (String)var5.getKey();
            JsonObject var7 = ((JsonElement)var5.getValue()).getAsJsonObject();
            boolean var8 = var7.has("enabled") && var7.get("enabled").getAsBoolean();
            int var9 = var7.has("buyBelow") ? var7.get("buyBelow").getAsInt() : 1000;
            int var10 = var7.has("minQuantity") ? var7.get("minQuantity").getAsInt() : 1;
            AutoBuyConfig.ItemConfig var11 = new AutoBuyConfig.ItemConfig(var8, var9, var10);
            var2.setItemConfig(var6, var11);
         }
      }
   }

   private void deserializeModule(ModuleStructure var1, JsonObject var2) {
      if (var2.has("enabled")) {
         boolean var3 = var2.get("enabled").getAsBoolean();
         if (var3) {
            var1.setState(true);
         }
      }

      if (var2.has("key")) {
         var1.setKey(var2.get("key").getAsInt());
      }

      if (var2.has("type")) {
         var1.setType(var2.get("type").getAsInt());
      }

      if (var2.has("favorite")) {
         var1.setFavorite(var2.get("favorite").getAsBoolean());
      }

      if (var2.has("settings")) {
         JsonObject var6 = var2.getAsJsonObject("settings");

         for (Setting var5 : var1.settings()) {
            if (var6.has(var5.getName())) {
               this.deserializeSetting(var5, var6.get(var5.getName()));
            }
         }
      }
   }

   private void deserializeSetting(Setting var1, JsonElement var2) {
      try {
         if (var1 instanceof BooleanSetting var3) {
            var3.setValue(var2.getAsBoolean());
         } else if (var1 instanceof SliderSettings var4) {
            var4.setValue((float)var2.getAsDouble());
         } else if (var1 instanceof BindSetting var5) {
            if (var2.isJsonObject()) {
               JsonObject var11 = var2.getAsJsonObject();
               if (var11.has("key")) {
                  var5.setKey(var11.get("key").getAsInt());
               }

               if (var11.has("type")) {
                  var5.setType(var11.get("type").getAsInt());
               }
            } else {
               var5.setKey(var2.getAsInt());
            }
         } else if (var1 instanceof TextSetting var6) {
            var6.setText(var2.getAsString());
         } else if (var1 instanceof SelectSetting var7) {
            var7.setSelected(var2.getAsString());
         } else if (var1 instanceof ColorSetting var8) {
            if (var2.isJsonObject()) {
               JsonObject var16 = var2.getAsJsonObject();
               if (var16.has("hue")) {
                  var8.setHue(var16.get("hue").getAsFloat());
               }

               if (var16.has("saturation")) {
                  var8.setSaturation(var16.get("saturation").getAsFloat());
               }

               if (var16.has("brightness")) {
                  var8.setBrightness(var16.get("brightness").getAsFloat());
               }

               if (var16.has("alpha")) {
                  var8.setAlpha(var16.get("alpha").getAsFloat());
               }

               if (var16.has("brightness")) {
                  var8.setBrightness(var16.get("brightness").getAsFloat());
               }

               if (var16.has("alpha")) {
                  var8.setAlpha(var16.get("alpha").getAsFloat());
               }
            } else {
               var8.setColor(var2.getAsInt());
            }
         } else if (var1 instanceof MultiSelectSetting var9) {
            if (var2.isJsonArray()) {
               JsonArray var17 = var2.getAsJsonArray();
               ArrayList var12 = new ArrayList();

               for (JsonElement var14 : var17) {
                  var12.add(var14.getAsString());
               }

               var9.setSelected(var12);
            }
         } else if (var1 instanceof GroupSetting var10 && var2.isJsonObject()) {
            JsonObject var18 = var2.getAsJsonObject();
            if (var18.has("value")) {
               var10.setValue(var18.get("value").getAsBoolean());
            }

            if (var18.has("subSettings")) {
               JsonObject var19 = var18.getAsJsonObject("subSettings");

               for (Setting var21 : var10.getSubSettings()) {
                  if (var19.has(var21.getName())) {
                     this.deserializeSetting(var21, var19.get(var21.getName()));
                  }
               }
            }
         }
      } catch (Exception var15) {
      }
   }

   private ModuleRepository getModuleRepository() {
      Initialization var1 = Initialization.getInstance();
      return var1 != null && var1.getManager() != null ? var1.getManager().getModuleRepository() : null;
   }
}
