package rich.screens.clickgui.impl.autobuy.settings;

import net.minecraft.class_1792;
import rich.util.config.impl.autobuyconfig.AutoBuyConfig;

public class AutoBuyItemSettings {
   private int buyBelow;
   private int minQuantity = 1;
   private boolean canHaveQuantity = false;
   private class_1792 material;
   private String displayName;

   public AutoBuyItemSettings(int var1, class_1792 var2, String var3) {
      this.buyBelow = var1;
      this.material = var2;
      this.displayName = var3;
      this.loadFromConfig();
   }

   public AutoBuyItemSettings(int var1, class_1792 var2, String var3, boolean var4) {
      this.buyBelow = var1;
      this.material = var2;
      this.displayName = var3;
      this.canHaveQuantity = var4;
      this.loadFromConfig();
   }

   private void loadFromConfig() {
      AutoBuyConfig var1 = AutoBuyConfig.getInstance();
      if (var1.hasItemConfig(this.displayName)) {
         AutoBuyConfig.ItemConfig var2 = var1.getItemConfig(this.displayName);
         this.buyBelow = var2.getBuyBelow();
         this.minQuantity = var2.getMinQuantity();
      } else {
         var1.loadItemSettings(this.displayName, this.buyBelow);
      }
   }

   public void saveToConfig() {
      AutoBuyConfig var1 = AutoBuyConfig.getInstance();
      var1.setItemBuyBelow(this.displayName, this.buyBelow);
      var1.setItemMinQuantity(this.displayName, this.minQuantity);
   }

   public int getBuyBelow() {
      return this.buyBelow;
   }

   public int getMinQuantity() {
      return this.minQuantity;
   }

   public boolean isCanHaveQuantity() {
      return this.canHaveQuantity;
   }

   public class_1792 getMaterial() {
      return this.material;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public void setBuyBelow(int var1) {
      this.buyBelow = var1;
   }

   public void setMinQuantity(int var1) {
      this.minQuantity = var1;
   }

   public void setCanHaveQuantity(boolean var1) {
      this.canHaveQuantity = var1;
   }

   public void setMaterial(class_1792 var1) {
      this.material = var1;
   }

   public void setDisplayName(String var1) {
      this.displayName = var1;
   }
}
