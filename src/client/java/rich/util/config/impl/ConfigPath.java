package rich.util.config.impl;

import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public class ConfigPath {
   private static final String ROOT_DIR = "RunTime Visuals";
   private static final String CONFIG_DIR = "configs";
   private static final String AUTO_DIR = "autocfg";
   private static final String CONFIG_FILE = "autoconfig.json";

   public static void init() {
   }

   public static Path getConfigDirectory() {
      return FabricLoader.getInstance().getConfigDir().resolve(ROOT_DIR).resolve(CONFIG_DIR).resolve(AUTO_DIR);
   }

   public static Path getConfigFile() {
      return getConfigDirectory().resolve(CONFIG_FILE);
   }
}

