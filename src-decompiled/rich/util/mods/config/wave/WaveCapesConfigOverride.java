package rich.util.mods.config.wave;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class WaveCapesConfigOverride implements PreLaunchEntrypoint {
   private static final String CONFIG_CONTENT = "{\n  \"configVersion\": 2,\n  \"windMode\": \"WAVES\",\n  \"capeStyle\": \"SMOOTH\",\n  \"capeMovement\": \"BASIC_SIMULATION_3D\",\n  \"gravity\": 15,\n  \"heightMultiplier\": 5,\n  \"straveMultiplier\": 5\n}\n";

   public void onPreLaunch() {
      Path var1 = FabricLoader.getInstance().getConfigDir();
      Path var2 = var1.resolve("waveycapes.json");

      try {
         Files.writeString(
            var2,
            "{\n  \"configVersion\": 2,\n  \"windMode\": \"WAVES\",\n  \"capeStyle\": \"SMOOTH\",\n  \"capeMovement\": \"BASIC_SIMULATION_3D\",\n  \"gravity\": 15,\n  \"heightMultiplier\": 5,\n  \"straveMultiplier\": 5\n}\n"
         );
      } catch (IOException var4) {
         var4.printStackTrace();
      }
   }
}
