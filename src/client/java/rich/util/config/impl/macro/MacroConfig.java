package rich.util.config.impl.macro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import rich.util.config.impl.consolelogger.Logger;
import rich.util.repository.macro.Macro;
import rich.util.repository.macro.MacroRepository;

public class MacroConfig {
   private static MacroConfig instance;
   private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
   private final Path configPath;

   private MacroConfig() {
      Path var1 = Paths.get("RunTime Visuals", "configs");

      try {
         Files.createDirectories(var1);
      } catch (IOException var3) {
      }

      this.configPath = var1.resolve("macros.json");
   }

   public static MacroConfig getInstance() {
      if (instance == null) {
         instance = new MacroConfig();
      }

      return instance;
   }

   public void save() {
      try {
         JsonArray var1 = new JsonArray();

         for (Macro var3 : MacroRepository.getInstance().getMacroList()) {
            JsonObject var4 = new JsonObject();
            var4.addProperty("name", var3.name());
            var4.addProperty("message", var3.message());
            var4.addProperty("key", var3.key());
            var1.add(var4);
         }

         Files.writeString(this.configPath, this.gson.toJson(var1), StandardCharsets.UTF_8);
      } catch (IOException var5) {
         Logger.error("MacroConfig: Save failed! " + var5.getMessage());
      }
   }

   public void load() {
      try {
         if (!Files.exists(this.configPath)) {
            return;
         }

         String var1 = Files.readString(this.configPath, StandardCharsets.UTF_8);
         JsonArray var2 = JsonParser.parseString(var1).getAsJsonArray();
         ArrayList var3 = new ArrayList();
         var2.forEach(var1x -> {
            JsonObject var2x = var1x.getAsJsonObject();
            String var3x = var2x.get("name").getAsString();
            String var4x = var2x.get("message").getAsString();
            int var5 = var2x.get("key").getAsInt();
            var3.add(new Macro(var3x, var4x, var5));
         });
         MacroRepository.getInstance().setMacros(var3);
         Logger.success("MacroConfig: macros.json loaded successfully!");
      } catch (Exception var4) {
         Logger.error("MacroConfig: Load failed! " + var4.getMessage());
      }
   }
}
