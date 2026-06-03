package rich.screens.clickgui.impl.autobuy;

import net.minecraft.class_1799;
import rich.screens.clickgui.impl.autobuy.settings.AutoBuyItemSettings;

public interface AutoBuyableItem {
   String getDisplayName();

   class_1799 createItemStack();

   int getPrice();

   boolean isEnabled();

   void setEnabled(boolean var1);

   AutoBuyItemSettings getSettings();
}
