package rich.screens.clickgui.impl.autobuy.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.component.DataComponentTypes;
import rich.screens.clickgui.impl.autobuy.AutoBuyableItem;
import rich.screens.clickgui.impl.autobuy.settings.AutoBuyItemSettings;
import rich.util.config.impl.autobuyconfig.AutoBuyConfig;

public class KrushItem implements AutoBuyableItem {
   private final String displayName;
   private final Item material;
   private final ItemStack displayStack;
   private final int defaultPrice;
   private final AutoBuyItemSettings settings;
   private final boolean isKrushItem;
   private boolean enabled;

   public KrushItem(String var1, Item var2, ItemStack var3, int var4) {
      this(var1, var2, var3, var4, true, false);
   }

   public KrushItem(String var1, Item var2, ItemStack var3, int var4, boolean var5) {
      this(var1, var2, var3, var4, var5, false);
   }

   public KrushItem(String var1, Item var2, ItemStack var3, int var4, boolean var5, boolean var6) {
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
         : this.material == Items.TOTEM_OF_UNDYING
            || this.material == Items.NETHERITE_HELMET
            || this.material == Items.NETHERITE_CHESTPLATE
            || this.material == Items.NETHERITE_LEGGINGS
            || this.material == Items.NETHERITE_BOOTS
            || this.material == Items.NETHERITE_SWORD
            || this.material == Items.NETHERITE_PICKAXE
            || this.material == Items.NETHERITE_AXE
            || this.material == Items.NETHERITE_SHOVEL
            || this.material == Items.NETHERITE_HOE
            || this.material == Items.DIAMOND_HELMET
            || this.material == Items.DIAMOND_CHESTPLATE
            || this.material == Items.DIAMOND_LEGGINGS
            || this.material == Items.DIAMOND_BOOTS
            || this.material == Items.DIAMOND_SWORD
            || this.material == Items.DIAMOND_PICKAXE
            || this.material == Items.DIAMOND_AXE
            || this.material == Items.DIAMOND_SHOVEL
            || this.material == Items.DIAMOND_HOE
            || this.material == Items.IRON_HELMET
            || this.material == Items.IRON_CHESTPLATE
            || this.material == Items.IRON_LEGGINGS
            || this.material == Items.IRON_BOOTS
            || this.material == Items.IRON_SWORD
            || this.material == Items.IRON_PICKAXE
            || this.material == Items.IRON_AXE
            || this.material == Items.IRON_SHOVEL
            || this.material == Items.IRON_HOE
            || this.material == Items.GOLDEN_HELMET
            || this.material == Items.GOLDEN_CHESTPLATE
            || this.material == Items.GOLDEN_LEGGINGS
            || this.material == Items.GOLDEN_BOOTS
            || this.material == Items.GOLDEN_SWORD
            || this.material == Items.GOLDEN_PICKAXE
            || this.material == Items.GOLDEN_AXE
            || this.material == Items.GOLDEN_SHOVEL
            || this.material == Items.GOLDEN_HOE
            || this.material == Items.BOW
            || this.material == Items.CROSSBOW
            || this.material == Items.TRIDENT
            || this.material == Items.MACE
            || this.material == Items.ELYTRA
            || this.material == Items.SHIELD
            || this.material == Items.FISHING_ROD;
   }

   @Override
   public String getDisplayName() {
      return this.displayName;
   }

   @Override
   public ItemStack createItemStack() {
      ItemStack var1 = this.displayStack.copy();
      if (this.shouldHaveGlint()) {
         var1.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
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
