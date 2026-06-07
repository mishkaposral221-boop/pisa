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

public class GuiLayoutConfig {
   private static GuiLayoutConfig instance;
   private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
   private final Path configPath;

   private GuiLayoutConfig() {
      Path var1 = Paths.get("RunTime Visuals", "configs");

      try {
         Files.createDirectories(var1);
      } catch (IOException var3) {
      }

      this.configPath = var1.resolve("guilayout.json");
   }

   public static GuiLayoutConfig getInstance() {
      if (instance == null) {
         instance = new GuiLayoutConfig();
      }

      return instance;
   }

   public void save(float var1, float var2, float var3, float var4, int var5) {
      try {
         JsonObject var6 = new JsonObject();
         var6.addProperty("sp_offset_x", var1);
         var6.addProperty("sp_offset_y", var2);
         var6.addProperty("cat_offset_x", var3);
         var6.addProperty("cat_offset_y", var4);
         var6.addProperty("cat_side", var5);
         Files.writeString(this.configPath, this.gson.toJson(var6), StandardCharsets.UTF_8);
      } catch (IOException var7) {
      }
   }

   public float[] load() {
      float[] var1 = new float[]{0.0F, 0.0F, 0.0F, 0.0F, 0.0F};

      try {
         if (!Files.exists(this.configPath)) {
            return var1;
         }

         String var2 = Files.readString(this.configPath, StandardCharsets.UTF_8);
         if (var2 == null || var2.isBlank()) {
            return var1;
         }

         JsonObject var3 = JsonParser.parseString(var2).getAsJsonObject();
         var1[0] = var3.has("sp_offset_x") ? var3.get("sp_offset_x").getAsFloat() : 0.0F;
         var1[1] = var3.has("sp_offset_y") ? var3.get("sp_offset_y").getAsFloat() : 0.0F;
         var1[2] = var3.has("cat_offset_x") ? var3.get("cat_offset_x").getAsFloat() : 0.0F;
         var1[3] = var3.has("cat_offset_y") ? var3.get("cat_offset_y").getAsFloat() : 0.0F;
         var1[4] = var3.has("cat_side") ? var3.get("cat_side").getAsFloat() : 0.0F;
      } catch (Exception var4) {
      }

      return var1;
   }
}
