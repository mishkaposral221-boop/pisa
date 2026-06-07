package rich.util.config.impl.way;

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
import net.minecraft.util.math.BlockPos;
import rich.util.config.impl.consolelogger.Logger;
import rich.util.repository.way.Way;
import rich.util.repository.way.WayRepository;

public class WayConfig {
   private static WayConfig instance;
   private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
   private final Path configPath;

   private WayConfig() {
      Path var1 = Paths.get("RunTime Visuals", "configs");

      try {
         Files.createDirectories(var1);
      } catch (IOException var3) {
      }

      this.configPath = var1.resolve("waypoints.json");
   }

   public static WayConfig getInstance() {
      if (instance == null) {
         instance = new WayConfig();
      }

      return instance;
   }

   public void save() {
      try {
         JsonArray var1 = new JsonArray();

         for (Way var3 : WayRepository.getInstance().getWayList()) {
            JsonObject var4 = new JsonObject();
            var4.addProperty("name", var3.name());
            var4.addProperty("x", var3.pos().getX());
            var4.addProperty("y", var3.pos().getY());
            var4.addProperty("z", var3.pos().getZ());
            var4.addProperty("server", var3.server());
            var1.add(var4);
         }

         Files.writeString(this.configPath, this.gson.toJson(var1), StandardCharsets.UTF_8);
      } catch (IOException var5) {
         Logger.error("WayConfig: Save failed! " + var5.getMessage());
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
            int var4x = var2x.get("x").getAsInt();
            int var5 = var2x.get("y").getAsInt();
            int var6 = var2x.get("z").getAsInt();
            String var7 = var2x.get("server").getAsString();
            var3.add(new Way(var3x, new BlockPos(var4x, var5, var6), var7));
         });
         WayRepository.getInstance().setWays(var3);
         Logger.success("WayConfig: waypoints.json loaded successfully!");
      } catch (Exception var4) {
         Logger.error("WayConfig: Load failed! " + var4.getMessage());
      }
   }
}
