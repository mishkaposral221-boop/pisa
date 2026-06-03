package rich.screens.clickgui.impl.autobuy.items;

import java.util.ArrayList;
import java.util.List;
import rich.screens.clickgui.impl.autobuy.AutoBuyableItem;
import rich.screens.clickgui.impl.autobuy.items.defaults.MiscProvider;
import rich.screens.clickgui.impl.autobuy.originalitems.DonatorProvider;
import rich.screens.clickgui.impl.autobuy.originalitems.PotionProvider;
import rich.screens.clickgui.impl.autobuy.originalitems.SphereProvider;
import rich.screens.clickgui.impl.autobuy.originalitems.TalismanProvider;
import rich.screens.clickgui.impl.autobuy.util.KrushProvider;
import rich.util.config.impl.autobuyconfig.AutoBuyConfig;

public class ItemRegistry {
   private static List<AutoBuyableItem> allItems = null;
   private static List<AutoBuyableItem> krushItems = null;
   private static List<AutoBuyableItem> talismanItems = null;
   private static List<AutoBuyableItem> sphereItems = null;
   private static List<AutoBuyableItem> miscItems = null;
   private static List<AutoBuyableItem> donatorItems = null;
   private static List<AutoBuyableItem> potionItems = null;
   private static boolean initialized = false;

   public static void ensureSettingsLoaded() {
      if (!initialized) {
         getAllItems();
         initialized = true;
      }
   }

   public static List<AutoBuyableItem> getAllItems() {
      if (allItems == null) {
         allItems = new ArrayList<>();
         allItems.addAll(getKrush());
         allItems.addAll(getTalismans());
         allItems.addAll(getSpheres());
         allItems.addAll(getMisc());
         allItems.addAll(getDonator());
         allItems.addAll(getPotions());
         initializeAllItemsDisabled();
         loadSavedSettings();
      }

      return allItems;
   }

   private static void initializeAllItemsDisabled() {
      for (AutoBuyableItem var1 : allItems) {
         var1.setEnabled(false);
      }
   }

   private static void loadSavedSettings() {
      AutoBuyConfig var0 = AutoBuyConfig.getInstance();

      for (AutoBuyableItem var2 : allItems) {
         if (var0.hasItemConfig(var2.getDisplayName())) {
            AutoBuyConfig.ItemConfig var3 = var0.getItemConfigOrNull(var2.getDisplayName());
            if (var3 != null) {
               var2.getSettings().setBuyBelow(var3.getBuyBelow());
               var2.getSettings().setMinQuantity(var3.getMinQuantity());
               var2.setEnabled(var3.isEnabled());
            }
         }
      }
   }

   public static void reloadSettings() {
      if (allItems != null) {
         initializeAllItemsDisabled();
         loadSavedSettings();
      }
   }

   public static void saveItemState(AutoBuyableItem var0) {
      var0.setEnabled(!var0.isEnabled());
      AutoBuyConfig var1 = AutoBuyConfig.getInstance();
      var1.setItemEnabled(var0.getDisplayName(), var0.isEnabled());
      var1.setItemBuyBelow(var0.getDisplayName(), var0.getSettings().getBuyBelow());
      var1.setItemMinQuantity(var0.getDisplayName(), var0.getSettings().getMinQuantity());
      var1.save();
   }

   public static void saveItemSettings(AutoBuyableItem var0) {
      AutoBuyConfig var1 = AutoBuyConfig.getInstance();
      var1.setItemEnabled(var0.getDisplayName(), var0.isEnabled());
      var1.setItemBuyBelow(var0.getDisplayName(), var0.getSettings().getBuyBelow());
      var1.setItemMinQuantity(var0.getDisplayName(), var0.getSettings().getMinQuantity());
      var1.save();
   }

   public static List<AutoBuyableItem> getKrush() {
      if (krushItems == null) {
         krushItems = KrushProvider.getKrush();
      }

      return krushItems;
   }

   public static List<AutoBuyableItem> getTalismans() {
      if (talismanItems == null) {
         talismanItems = TalismanProvider.getTalismans();
      }

      return talismanItems;
   }

   public static List<AutoBuyableItem> getSpheres() {
      if (sphereItems == null) {
         sphereItems = SphereProvider.getSpheres();
      }

      return sphereItems;
   }

   public static List<AutoBuyableItem> getMisc() {
      if (miscItems == null) {
         miscItems = MiscProvider.getMisc();
      }

      return miscItems;
   }

   public static List<AutoBuyableItem> getDonator() {
      if (donatorItems == null) {
         donatorItems = DonatorProvider.getDonator();
      }

      return donatorItems;
   }

   public static List<AutoBuyableItem> getPotions() {
      if (potionItems == null) {
         potionItems = PotionProvider.getPotions();
      }

      return potionItems;
   }

   public static void clearCache() {
      allItems = null;
      krushItems = null;
      talismanItems = null;
      sphereItems = null;
      miscItems = null;
      donatorItems = null;
      potionItems = null;
      initialized = false;
   }
}
