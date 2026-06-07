package rich.screens.clickgui.impl.autobuy.items.customitem;

import java.util.List;
import java.util.Optional;
import net.minecraft.util.Formatting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.registry.RegistryWrapper.Impl;
import net.minecraft.component.type.ItemEnchantmentsComponent.Builder;
import rich.screens.clickgui.impl.autobuy.AutoBuyableItem;
import rich.screens.clickgui.impl.autobuy.settings.AutoBuyItemSettings;
import rich.util.config.impl.autobuyconfig.AutoBuyConfig;

public class UnbreakableItem implements AutoBuyableItem {
   private final String displayName;
   private final Item material;
   private final int price;
   private final List<Text> loreTexts;
   private final AutoBuyItemSettings settings;
   private boolean enabled;

   public UnbreakableItem(String var1, Item var2, int var3, List<Text> var4) {
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
   public ItemStack createItemStack() {
      ItemStack var1 = new ItemStack(this.material);
      var1.set(DataComponentTypes.CUSTOM_NAME, Text.literal(this.displayName).formatted(Formatting.LIGHT_PURPLE));
      NbtCompound var2 = new NbtCompound();
      var2.putInt("HideFlags", 127);
      var2.putBoolean("Unbreakable", true);
      var1.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(var2));
      MinecraftClient var3 = MinecraftClient.getInstance();
      if (var3.world != null) {
         try {
            DynamicRegistryManager var4 = var3.world.getRegistryManager();
            net.minecraft.registry.RegistryWrapper.Impl var5 = var4.getOrThrow(RegistryKeys.ENCHANTMENT);
            net.minecraft.component.type.ItemEnchantmentsComponent.Builder var6 = new net.minecraft.component.type.ItemEnchantmentsComponent.Builder((ItemEnchantmentsComponent)var1.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT));
            Optional var7 = var5.getOptional(Enchantments.VANISHING_CURSE);
            if (var7.isPresent()) {
               var6.add((RegistryEntry)var7.get(), 1);
            }

            var1.set(DataComponentTypes.ENCHANTMENTS, var6.build());
         } catch (Exception var8) {
         }
      }

      if (this.loreTexts != null && !this.loreTexts.isEmpty()) {
         var1.set(DataComponentTypes.LORE, new LoreComponent(this.loreTexts));
      }

      var1.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
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
