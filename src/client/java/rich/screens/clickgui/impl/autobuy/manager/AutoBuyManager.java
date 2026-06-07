package rich.screens.clickgui.impl.autobuy.manager;

import java.util.ArrayList;
import java.util.List;
import rich.screens.clickgui.impl.autobuy.AutoBuyableItem;
import rich.screens.clickgui.impl.autobuy.items.ItemRegistry;
import rich.util.config.impl.autobuyconfig.AutoBuyConfig;

public class AutoBuyManager {
   private static AutoBuyManager instance;

   private AutoBuyManager() {
   }

   public static AutoBuyManager getInstance() {
      if (instance == null) {
         instance = new AutoBuyManager();
      }

      return instance;
   }

   public void setEnabled(boolean var1) {
      boolean var2 = AutoBuyConfig.getInstance().isGlobalEnabled();
      AutoBuyConfig.getInstance().setGlobalEnabled(var1);
   }

   public void setEnabledSilent(boolean var1) {
      AutoBuyConfig.getInstance().setGlobalEnabled(var1);
   }

   public boolean isEnabled() {
      return AutoBuyConfig.getInstance().isGlobalEnabled();
   }

   public List<AutoBuyableItem> getAllItems() {
      return ItemRegistry.getAllItems();
   }

   public List<AutoBuyableItem> getEnabledItems() {
      ArrayList var1 = new ArrayList();

      for (AutoBuyableItem var3 : this.getAllItems()) {
         if (var3.isEnabled()) {
            var1.add(var3);
         }
      }

      return var1;
   }

   public int getEnabledCount() {
      int var1 = 0;

      for (AutoBuyableItem var3 : this.getAllItems()) {
         if (var3.isEnabled()) {
            var1++;
         }
      }

      return var1;
   }

   public void toggleItem(AutoBuyableItem var1) {
      var1.setEnabled(!var1.isEnabled());
      ItemRegistry.saveItemSettings(var1);
   }

   public void enableAll() {
      for (AutoBuyableItem var2 : this.getAllItems()) {
         var2.setEnabled(true);
         ItemRegistry.saveItemSettings(var2);
      }
   }

   public void disableAll() {
      for (AutoBuyableItem var2 : this.getAllItems()) {
         var2.setEnabled(false);
         ItemRegistry.saveItemSettings(var2);
      }
   }
}
