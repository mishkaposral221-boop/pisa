package rich.screens.clickgui.impl.autobuy.util;

import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_9334;
import rich.screens.clickgui.impl.autobuy.AutoBuyableItem;
import rich.screens.clickgui.impl.autobuy.settings.AutoBuyItemSettings;
import rich.util.config.impl.autobuyconfig.AutoBuyConfig;

public class KrushItem implements AutoBuyableItem {
   private final String displayName;
   private final class_1792 material;
   private final class_1799 displayStack;
   private final int defaultPrice;
   private final AutoBuyItemSettings settings;
   private final boolean isKrushItem;
   private boolean enabled;

   public KrushItem(String var1, class_1792 var2, class_1799 var3, int var4) {
      this(var1, var2, var3, var4, true, false);
   }

   public KrushItem(String var1, class_1792 var2, class_1799 var3, int var4, boolean var5) {
      this(var1, var2, var3, var4, var5, false);
   }

   public KrushItem(String var1, class_1792 var2, class_1799 var3, int var4, boolean var5, boolean var6) {
      this.displayName = var1;
      this.material = var2;
      this.displayStack = var3;
      this.defaultPrice = var4;
      this.isKrushItem = var6;
      this.settings = new AutoBuyItemSettings(var4, var2, var1, var5);
      AutoBuyConfig var7 = AutoBuyConfig.getInstance();
      if (var7.hasItemConfig(var1)) {
         this.enabled = var7.isItemEnabled(var1);
      } else {
         this.enabled = true;
         var7.loadItemSettings(var1, var4);
      }
   }

   private boolean shouldHaveGlint() {
      return !this.isKrushItem
         ? false
         : this.material == class_1802.field_8288
            || this.material == class_1802.field_22027
            || this.material == class_1802.field_22028
            || this.material == class_1802.field_22029
            || this.material == class_1802.field_22030
            || this.material == class_1802.field_22022
            || this.material == class_1802.field_22024
            || this.material == class_1802.field_22025
            || this.material == class_1802.field_22023
            || this.material == class_1802.field_22026
            || this.material == class_1802.field_8805
            || this.material == class_1802.field_8058
            || this.material == class_1802.field_8348
            || this.material == class_1802.field_8285
            || this.material == class_1802.field_8802
            || this.material == class_1802.field_8377
            || this.material == class_1802.field_8556
            || this.material == class_1802.field_8250
            || this.material == class_1802.field_8527
            || this.material == class_1802.field_8743
            || this.material == class_1802.field_8523
            || this.material == class_1802.field_8396
            || this.material == class_1802.field_8660
            || this.material == class_1802.field_8371
            || this.material == class_1802.field_8403
            || this.material == class_1802.field_8475
            || this.material == class_1802.field_8699
            || this.material == class_1802.field_8609
            || this.material == class_1802.field_8862
            || this.material == class_1802.field_8678
            || this.material == class_1802.field_8416
            || this.material == class_1802.field_8753
            || this.material == class_1802.field_8845
            || this.material == class_1802.field_8335
            || this.material == class_1802.field_8825
            || this.material == class_1802.field_8322
            || this.material == class_1802.field_8303
            || this.material == class_1802.field_8102
            || this.material == class_1802.field_8399
            || this.material == class_1802.field_8547
            || this.material == class_1802.field_49814
            || this.material == class_1802.field_8833
            || this.material == class_1802.field_8255
            || this.material == class_1802.field_8378;
   }

   @Override
   public String getDisplayName() {
      return this.displayName;
   }

   @Override
   public class_1799 createItemStack() {
      class_1799 var1 = this.displayStack.method_7972();
      if (this.shouldHaveGlint()) {
         var1.method_57379(class_9334.field_49641, true);
      }

      return var1;
   }

   @Override
   public int getPrice() {
      return this.settings.getBuyBelow();
   }

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   @Override
   public void setEnabled(boolean var1) {
      this.enabled = var1;
   }

   @Override
   public AutoBuyItemSettings getSettings() {
      return this.settings;
   }
}
