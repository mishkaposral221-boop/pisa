package rich.util.config.impl.prefix;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import rich.command.CommandManager;
import rich.util.config.impl.consolelogger.Logger;

public class PrefixConfig {
   private static PrefixConfig instance;
   private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
   private final Path configPath;
   private String prefix = ".";

   private PrefixConfig() {
      Path var1 = Paths.get("RunTime Visuals", "configs");

      try {
         Files.createDirectories(var1);
      } catch (IOException var3) {
      }

      this.configPath = var1.resolve("prefix.json");
   }

   public static PrefixConfig getInstance() {
      if (instance == null) {
         instance = new PrefixConfig();
      }

      return instance;
   }

   public void setPrefix(String var1) {
      this.prefix = var1;
      if (CommandManager.getInstance() != null) {
         CommandManager.getInstance().setPrefix(var1);
      }
   }

   public void setPrefixAndSave(String var1) {
      this.setPrefix(var1);
      this.save();
   }

   public void save() {
      try {
         JsonObject var1 = new JsonObject();
         var1.addProperty("prefix", this.prefix);
         Files.writeString(this.configPath, this.gson.toJson(var1), StandardCharsets.UTF_8);
      } catch (IOException var2) {
         Logger.error("PrefixConfig: Save failed! " + var2.getMessage());
      }
   }

   public void load() {
      try {
         if (!Files.exists(this.configPath)) {
            return;
         }

         String var1 = Files.readString(this.configPath, StandardCharsets.UTF_8);
         JsonObject var2 = JsonParser.parseString(var1).getAsJsonObject();
         if (var2.has("prefix")) {
            String var3 = var2.get("prefix").getAsString();
            if (!var3.isEmpty()) {
               this.prefix = var3;
               if (CommandManager.getInstance() != null) {
                  CommandManager.getInstance().setPrefix(var3);
               }
            }
         }

         Logger.success("PrefixConfig: prefix.json loaded successfully!");
      } catch (Exception var4) {
         Logger.error("PrefixConfig: Load failed! " + var4.getMessage());
      }
   }

   public String getPrefix() {
      return this.prefix;
   }
}
