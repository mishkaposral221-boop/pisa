package rich.util.render.font;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rich.Initialization;

public class FontInitializer {
   private static final Logger LOGGER = LoggerFactory.getLogger("rich/FontInitializer");
   private static boolean registered = false;
   private static boolean initialized = false;

   public static void register() {
      if (!registered) {
         registered = true;
         ClientTickEvents.END_CLIENT_TICK.register((EndTick)var0 -> {
            if (!initialized && var0.getResourceManager() != null && var0.getWindow() != null) {
               try {
                  FontRenderer var1 = Initialization.getInstance().getManager().getRenderCore().getFontRenderer();
                  if (var1 != null && !var1.isInitialized()) {
                     var1.initialize();
                     initialized = true;
                     LOGGER.info("Fonts initialized successfully");
                  }
               } catch (Exception var2) {
                  LOGGER.error("Failed to initialize fonts", var2);
               }
            }
         });
      }
   }

   public static boolean isInitialized() {
      return initialized;
   }
}
