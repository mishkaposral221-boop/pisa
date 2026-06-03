package rich.util.config.impl.blockesp;

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
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import rich.util.config.impl.consolelogger.Logger;

public class BlockESPConfig {
   private static BlockESPConfig instance;
   private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
   private final Path configPath;
   private final Set<String> blocks = new CopyOnWriteArraySet<>();

   private BlockESPConfig() {
      Path var1 = Paths.get("RunTime Visuals", "configs");

      try {
         Files.createDirectories(var1);
      } catch (IOException var3) {
      }

      this.configPath = var1.resolve("blockesp.json");
   }

   public static BlockESPConfig getInstance() {
      if (instance == null) {
         instance = new BlockESPConfig();
      }

      return instance;
   }

   public Set<String> getBlocks() {
      return this.blocks;
   }

   public void addBlock(String var1) {
      this.blocks.add(var1);
   }

   public void addBlockAndSave(String var1) {
      this.addBlock(var1);
      this.save();
   }

   public boolean removeBlock(String var1) {
      return this.blocks.remove(var1);
   }

   public boolean removeBlockAndSave(String var1) {
      boolean var2 = this.removeBlock(var1);
      if (var2) {
         this.save();
      }

      return var2;
   }

   public boolean hasBlock(String var1) {
      return this.blocks.contains(var1);
   }

   public void clear() {
      this.blocks.clear();
   }

   public void clearAndSave() {
      this.clear();
      this.save();
   }

   public int size() {
      return this.blocks.size();
   }

   public List<String> getBlockList() {
      return new ArrayList<>(this.blocks);
   }

   public void save() {
      try {
         JsonArray var1 = new JsonArray();

         for (String var3 : this.blocks) {
            var1.add(var3);
         }

         Files.writeString(this.configPath, this.gson.toJson(var1), StandardCharsets.UTF_8);
      } catch (IOException var4) {
         Logger.error("BlockESPConfig: Save failed! " + var4.getMessage());
      }
   }

   public void load() {
      try {
         if (!Files.exists(this.configPath)) {
            return;
         }

         String var1 = Files.readString(this.configPath, StandardCharsets.UTF_8);
         JsonArray var2 = JsonParser.parseString(var1).getAsJsonArray();
         this.blocks.clear();
         var2.forEach(var1x -> this.blocks.add(var1x.getAsString()));
         Logger.success("BlockESPConfig: blockesp.json loaded successfully!");
      } catch (Exception var3) {
         Logger.error("BlockESPConfig: Load failed! " + var3.getMessage());
      }
   }
}
