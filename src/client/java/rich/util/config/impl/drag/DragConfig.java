package rich.util.config.impl.drag;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import rich.Initialization;
import rich.client.draggables.AbstractHudElement;
import rich.client.draggables.HudElement;
import rich.client.draggables.HudManager;
import rich.util.config.impl.consolelogger.Logger;

public class DragConfig {
   private static DragConfig instance;
   private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
   private final Path configPath;

   private DragConfig() {
      Path var1 = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("RunTime Visuals").resolve("configs");

      try {
         Files.createDirectories(var1);
      } catch (IOException var3) {
      }

      this.configPath = var1.resolve("draggables.json");
   }

   public static DragConfig getInstance() {
      if (instance == null) {
         instance = new DragConfig();
      }

      return instance;
   }

   public void save() {
      try {
         HudManager var1 = this.getHudManager();
         if (var1 == null || !var1.isInitialized()) {
            return;
         }

         JsonObject var2 = new JsonObject();

         for (HudElement var4 : var1.getElements()) {
            JsonObject var5 = new JsonObject();
            var5.addProperty("x", var4.getX());
            var5.addProperty("y", var4.getY());
            var5.addProperty("width", var4.getWidth());
            var5.addProperty("height", var4.getHeight());
            var5.addProperty("scale", var4.getScale());
            var2.add(var4.getName(), var5);
         }

         Files.writeString(this.configPath, this.gson.toJson(var2), StandardCharsets.UTF_8);
         Logger.success("DragConfig: draggables.json saved successfully!");
      } catch (IOException var6) {
         Logger.error("DragConfig: Save failed! " + var6.getMessage());
      }
   }

   public void load() {
      try {
         if (!Files.exists(this.configPath)) {
            Logger.info("DragConfig: No config file found, using defaults.");
            return;
         }

         HudManager var1 = this.getHudManager();
         if (var1 == null) {
            Logger.error("DragConfig: HudManager is null, cannot load.");
            return;
         }

         String var2 = Files.readString(this.configPath, StandardCharsets.UTF_8);
         if (var2 == null || var2.trim().isEmpty()) {
            Logger.error("DragConfig: Config file is empty.");
            return;
         }

         JsonObject var3 = JsonParser.parseString(var2).getAsJsonObject();

         for (HudElement var5 : var1.getElements()) {
            if (var3.has(var5.getName())) {
               JsonObject var6 = var3.getAsJsonObject(var5.getName());
               if (var6.has("x")) {
                  var5.setX(var6.get("x").getAsInt());
               }

               if (var6.has("y")) {
                  var5.setY(var6.get("y").getAsInt());
               }

               if (var6.has("width")) {
                  var5.setWidth(var6.get("width").getAsInt());
               }

               if (var6.has("height")) {
                  var5.setHeight(var6.get("height").getAsInt());
               }

               if (var6.has("scale") && var5 instanceof AbstractHudElement var7) {
                  var7.setScale(var6.get("scale").getAsFloat());
               }
            }
         }

         Logger.success("DragConfig: draggables.json loaded successfully!");
      } catch (Exception var7) {
         Logger.error("DragConfig: Load failed! " + var7.getMessage());
      }
   }

   private HudManager getHudManager() {
      if (Initialization.getInstance() == null) {
         return null;
      } else {
         return Initialization.getInstance().getManager() == null ? null : Initialization.getInstance().getManager().getHudManager();
      }
   }
}
