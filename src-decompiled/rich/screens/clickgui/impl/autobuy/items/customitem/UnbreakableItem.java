package rich.screens.clickgui.impl.autobuy.items.customitem;

import java.util.List;
import java.util.Optional;
import net.minecraft.class_124;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1893;
import net.minecraft.class_2487;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_5455;
import net.minecraft.class_6880;
import net.minecraft.class_7924;
import net.minecraft.class_9279;
import net.minecraft.class_9290;
import net.minecraft.class_9304;
import net.minecraft.class_9334;
import net.minecraft.class_7225.class_7226;
import net.minecraft.class_9304.class_9305;
import rich.screens.clickgui.impl.autobuy.AutoBuyableItem;
import rich.screens.clickgui.impl.autobuy.settings.AutoBuyItemSettings;
import rich.util.config.impl.autobuyconfig.AutoBuyConfig;

public class UnbreakableItem implements AutoBuyableItem {
   private final String displayName;
   private final class_1792 material;
   private final int price;
   private final List<class_2561> loreTexts;
   private final AutoBuyItemSettings settings;
   private boolean enabled;

   public UnbreakableItem(String var1, class_1792 var2, int var3, List<class_2561> var4) {
      this.displayName = var1;
      this.material = var2;
      this.price = var3;
      this.loreTexts = var4;
      this.settings = new AutoBuyItemSettings(var3, var2, var1);
      AutoBuyConfig var5 = AutoBuyConfig.getInstance();
      if (var5.hasItemConfig(var1)) {
         this.enabled = var5.isItemEnabled(var1);
      } else {
         this.enabled = true;
         var5.loadItemSettings(var1, var3);
      }
   }

   @Override
   public String getDisplayName() {
      return this.displayName;
   }

   @Override
   public class_1799 createItemStack() {
      class_1799 var1 = new class_1799(this.material);
      var1.method_57379(class_9334.field_49631, class_2561.method_43470(this.displayName).method_27692(class_124.field_1076));
      class_2487 var2 = new class_2487();
      var2.method_10569("HideFlags", 127);
      var2.method_10556("Unbreakable", true);
      var1.method_57379(class_9334.field_49628, class_9279.method_57456(var2));
      class_310 var3 = class_310.method_1551();
      if (var3.field_1687 != null) {
         try {
            class_5455 var4 = var3.field_1687.method_30349();
            class_7226 var5 = var4.method_46762(class_7924.field_41265);
            class_9305 var6 = new class_9305((class_9304)var1.method_58695(class_9334.field_49633, class_9304.field_49385));
            Optional var7 = var5.method_46746(class_1893.field_9109);
            if (var7.isPresent()) {
               var6.method_57550((class_6880)var7.get(), 1);
            }

            var1.method_57379(class_9334.field_49633, var6.method_57549());
         } catch (Exception var8) {
         }
      }

      if (this.loreTexts != null && !this.loreTexts.isEmpty()) {
         var1.method_57379(class_9334.field_49632, new class_9290(this.loreTexts));
      }

      var1.method_57379(class_9334.field_49641, true);
      return var1;
   }

   @Override
   public int getPrice() {
      return this.price;
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
