package rich.modules.impl.misc;

import rich.irc.IrcManager;
import rich.irc.PlayerPrefixManager;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;

public class IrcModule extends ModuleStructure {
   private static IrcModule instance;

   public IrcModule() {
      super("IRC", "IRC чат между игроками RunTime", ModuleCategory.UTILITIES);
      instance = this;
   }

   public static IrcModule getInstance() {
      return instance;
   }

   @Override
   public void activate() {
      IrcManager.getInstance().start();
      PlayerPrefixManager.getInstance().loadPrefixes();
   }

   @Override
   public void deactivate() {
      IrcManager.getInstance().stop();
   }
}
