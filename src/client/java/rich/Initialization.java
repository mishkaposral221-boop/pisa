package rich;

import net.fabricmc.api.ClientModInitializer;
import rich.manager.Manager;
import rich.online.OnlineTracker;
import rich.update.UpdateChecker;
import rich.util.d;

public class Initialization implements ClientModInitializer {
   private static Initialization instance;
   private Manager manager;

   public static Initialization getInstance() {
      if (instance == null) {
         instance = new Initialization();
      }

      return instance;
   }

   public Manager getManager() {
      return this.manager;
   }

   public void onInitializeClient() {
      d.a();
   }

   public void init() {
      this.manager = new Manager();
      this.manager.init();
      UpdateChecker.getInstance().start();
      OnlineTracker.getInstance().start();
   }
}

