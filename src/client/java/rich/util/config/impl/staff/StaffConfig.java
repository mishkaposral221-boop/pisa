package rich.util.config.impl.staff;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import rich.util.config.impl.consolelogger.Logger;
import rich.util.repository.staff.StaffUtils;

public class StaffConfig {
   private static StaffConfig instance;
   private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
   private final Path configPath;

   private StaffConfig() {
      Path var1 = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("RunTime Visuals").resolve("configs");

      try {
         Files.createDirectories(var1);
      } catch (IOException var3) {
      }

      this.configPath = var1.resolve("staff.json");
   }

   public static StaffConfig getInstance() {
      if (instance == null) {
         instance = new StaffConfig();
      }

      return instance;
   }

   public void save() {
      try {
         JsonArray var1 = new JsonArray();

         for (String var3 : StaffUtils.getStaffNames()) {
            var1.add(var3);
         }

         Files.writeString(this.configPath, this.gson.toJson(var1), StandardCharsets.UTF_8);
      } catch (IOException var4) {
         Logger.error("StaffConfig: Save failed! " + var4.getMessage());
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
         var2.forEach(var1x -> var3.add(var1x.getAsString()));
         StaffUtils.setStaff(var3);
         Logger.success("StaffConfig: staff.json loaded successfully!");
      } catch (Exception var4) {
         Logger.error("StaffConfig: Load failed! " + var4.getMessage());
      }
   }
}
