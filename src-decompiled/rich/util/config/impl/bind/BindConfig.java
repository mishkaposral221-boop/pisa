package rich.util.config.impl.bind;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import rich.util.config.impl.consolelogger.Logger;

public class BindConfig {
   private static BindConfig instance;
   private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
   private final Path configPath;
   private int BindKey = 344;

   private BindConfig() {
      Path var1 = Paths.get("RunTime Visuals", "configs");

      try {
         Files.createDirectories(var1);
      } catch (IOException var3) {
      }

      this.configPath = var1.resolve("Bind.json");
      this.load();
   }

   public static BindConfig getInstance() {
      if (instance == null) {
         instance = new BindConfig();
      }

      return instance;
   }

   public void setKey(int var1) {
      this.BindKey = var1;
   }

   public void setKeyAndSave(int var1) {
      this.setKey(var1);
      this.save();
   }

   public void save() {
      try {
         JsonObject var1 = new JsonObject();
         var1.addProperty("BindKey", this.BindKey);
         Files.writeString(this.configPath, this.gson.toJson(var1), StandardCharsets.UTF_8);
      } catch (IOException var2) {
         Logger.error("BindConfig: Save failed! " + var2.getMessage());
      }
   }

   public void load() {
      try {
         if (!Files.exists(this.configPath)) {
            return;
         }

         String var1 = Files.readString(this.configPath, StandardCharsets.UTF_8);
         JsonObject var2 = JsonParser.parseString(var1).getAsJsonObject();
         if (var2.has("BindKey")) {
            this.BindKey = var2.get("BindKey").getAsInt();
         }

         Logger.success("BindConfig: Bind.json loaded successfully!");
      } catch (Exception var3) {
         Logger.error("BindConfig: Load failed! " + var3.getMessage());
      }
   }

   public int getBindKey() {
      return this.BindKey;
   }
}
