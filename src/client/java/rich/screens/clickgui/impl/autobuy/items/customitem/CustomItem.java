package rich.screens.clickgui.impl.autobuy.items.customitem;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.component.DataComponentTypes;
import rich.screens.clickgui.impl.autobuy.AutoBuyableItem;
import rich.screens.clickgui.impl.autobuy.settings.AutoBuyItemSettings;
import rich.util.config.impl.autobuyconfig.AutoBuyConfig;

public class CustomItem implements AutoBuyableItem {
   private final String displayName;
   private final NbtCompound nbt;
   private final Item material;
   private final int price;
   private final PotionContentsComponent potionContents;
   private final List<Text> loreTexts;
   private final AutoBuyItemSettings settings;
   private final boolean hasGlint;
   private boolean enabled;

   public CustomItem(String var1, NbtCompound var2, Item var3, int var4, PotionContentsComponent var5, List<Text> var6) {
      this(var1, var2, var3, var4, var5, var6, shouldHaveGlint(var3, var1), false);
   }

   public CustomItem(String var1, NbtCompound var2, Item var3, int var4, PotionContentsComponent var5, List<Text> var6, boolean var7) {
      this(var1, var2, var3, var4, var5, var6, var7, false);
   }

   public CustomItem(String var1, NbtCompound var2, Item var3, int var4, PotionContentsComponent var5, List<Text> var6, boolean var7, boolean var8) {
      this.displayName = var1;
      this.nbt = var2;
      this.material = var3;
      this.price = var4;
      this.potionContents = var5;
      this.loreTexts = var6;
      this.hasGlint = var7;
      this.settings = new AutoBuyItemSettings(var4, var3, var1, var8);
      AutoBuyConfig var9 = AutoBuyConfig.getInstance();
      if (var9.hasItemConfig(var1)) {
         this.enabled = var9.isItemEnabled(var1);
      } else {
         this.enabled = true;
         var9.loadItemSettings(var1, var4);
      }
   }

   public CustomItem(String var1, NbtCompound var2, Item var3, int var4) {
      this(var1, var2, var3, var4, null, null);
   }

   public CustomItem(String var1, NbtCompound var2, Item var3, int var4, boolean var5) {
      this(var1, var2, var3, var4, null, null, shouldHaveGlint(var3, var1), var5);
   }

   public CustomItem(String var1, NbtCompound var2, Item var3, int var4, PotionContentsComponent var5, List<Text> var6, int var7) {
      this(var1, var2, var3, var4, var5, var6, shouldHaveGlint(var3, var1), true);
   }

   private static boolean shouldHaveGlint(Item var0, String var1) {
      if (var0 == Items.TOTEM_OF_UNDYING || var0 == Items.ELYTRA) {
         return false;
      } else {
         return var0 != Items.NETHERITE_HELMET
               && var0 != Items.NETHERITE_CHESTPLATE
               && var0 != Items.NETHERITE_LEGGINGS
               && var0 != Items.NETHERITE_BOOTS
               && var0 != Items.NETHERITE_SWORD
               && var0 != Items.NETHERITE_PICKAXE
               && var0 != Items.NETHERITE_AXE
               && var0 != Items.NETHERITE_SHOVEL
               && var0 != Items.NETHERITE_HOE
               && var0 != Items.DIAMOND_HELMET
               && var0 != Items.DIAMOND_CHESTPLATE
               && var0 != Items.DIAMOND_LEGGINGS
               && var0 != Items.DIAMOND_BOOTS
               && var0 != Items.DIAMOND_SWORD
               && var0 != Items.DIAMOND_PICKAXE
               && var0 != Items.DIAMOND_AXE
               && var0 != Items.DIAMOND_SHOVEL
               && var0 != Items.DIAMOND_HOE
               && var0 != Items.IRON_HELMET
               && var0 != Items.IRON_CHESTPLATE
               && var0 != Items.IRON_LEGGINGS
               && var0 != Items.IRON_BOOTS
               && var0 != Items.IRON_SWORD
               && var0 != Items.IRON_PICKAXE
               && var0 != Items.IRON_AXE
               && var0 != Items.IRON_SHOVEL
               && var0 != Items.IRON_HOE
               && var0 != Items.GOLDEN_HELMET
               && var0 != Items.GOLDEN_CHESTPLATE
               && var0 != Items.GOLDEN_LEGGINGS
               && var0 != Items.GOLDEN_BOOTS
               && var0 != Items.GOLDEN_SWORD
               && var0 != Items.GOLDEN_PICKAXE
               && var0 != Items.GOLDEN_AXE
               && var0 != Items.GOLDEN_SHOVEL
               && var0 != Items.GOLDEN_HOE
               && var0 != Items.BOW
               && var0 != Items.CROSSBOW
               && var0 != Items.TRIDENT
               && var0 != Items.MACE
               && var0 != Items.SHIELD
               && var0 != Items.FISHING_ROD
            ? var1 != null && var1.contains("[★]") && (var0 == Items.POTION || var0 == Items.SPLASH_POTION || var0 == Items.LINGERING_POTION)
            : true;
      }
   }

   @Override
   public String getDisplayName() {
      return this.displayName;
   }

   @Override
   public ItemStack createItemStack() {
      ItemStack var1 = new ItemStack(this.material);
      var1.set(DataComponentTypes.CUSTOM_NAME, Text.literal(this.displayName));
      if (this.isPotion(this.material)) {
         if (this.potionContents != null) {
            var1.set(DataComponentTypes.POTION_CONTENTS, this.potionContents);
         } else {
            int var2 = this.getPotionColorByName(this.displayName);
            var1.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(), Optional.of(var2), List.of(), Optional.empty()));
         }
      }

      if (this.loreTexts != null && !this.loreTexts.isEmpty()) {
         var1.set(DataComponentTypes.LORE, new LoreComponent(this.loreTexts));
      }

      if (this.hasGlint) {
         var1.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
      }

      if (this.nbt != null) {
         NbtCompound var13 = this.nbt.copy();
         if (this.material == Items.PLAYER_HEAD && var13.getCompound("SkullOwner").isPresent()) {
            NbtCompound var3 = (NbtCompound)var13.getCompound("SkullOwner").get();
            Optional var4 = var3.getIntArray("Id");
            UUID var5;
            if (var4.isPresent()) {
               int[] var6 = (int[])var4.get();
               var5 = uuidFromIntArray(var6);
            } else {
               Optional<String> var14 = var3.getString("Id");
               var5 = var14.map(UUID::fromString).orElse(UUID.randomUUID());
            }

            Builder var15 = ImmutableMultimap.builder();
            Optional var7 = var3.getCompound("Properties");
            if (var7.isPresent()) {
               NbtCompound var8 = (NbtCompound)var7.get();
               Optional var9 = var8.getList("textures");
               if (var9.isPresent()) {
                  NbtList var10 = (NbtList)var9.get();
                  if (!var10.isEmpty()) {
                     Optional var11 = var10.getCompound(0);
                     if (var11.isPresent()) {
                        Optional var12 = ((NbtCompound)var11.get()).getString("Value");
                        if (var12.isPresent()) {
                           var15.put("textures", new Property("textures", (String)var12.get()));
                        }
                     }
                  }
               }
            }

            PropertyMap var16 = new PropertyMap(var15.build());
            GameProfile var17 = new GameProfile(var5, "", var16);
            var1.set(DataComponentTypes.PROFILE, ProfileComponent.ofStatic(var17));
            var13.remove("SkullOwner");
         }

         if (!var13.isEmpty()) {
            var1.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(var13));
         }
      }

      return var1;
   }

   private boolean isPotion(Item var1) {
      return var1 == Items.POTION || var1 == Items.SPLASH_POTION || var1 == Items.LINGERING_POTION;
   }

   private int getPotionColorByName(String var1) {
      return switch (var1) {
         case "Зелье отрыжки" -> 16735488;
         case "Зелье серной кислоты" -> 49664;
         case "Зелье вспышки" -> 16777215;
         case "Зелье мочи Флеша" -> 6092799;
         case "Зелье победителя" -> 65280;
         case "Зелье агента" -> 16775936;
         case "Зелье медика" -> 16711902;
         case "Зелье киллера" -> 16711680;
         case "[★] Хлопушка" -> 16738740;
         case "[★] Святая вода" -> 16777215;
         case "[★] Зелье Гнева" -> 10040115;
         case "[★] Зелье Палладина" -> 65535;
         case "[★] Зелье Ассасина" -> 3355443;
         case "[★] Зелье Радиации" -> 3329330;
         case "[★] Снотворное" -> 4737096;
         case "[\ud83c\udf79] Мандариновый сок" -> 14077507;
         default -> 3694022;
      };
   }

   private static UUID uuidFromIntArray(int[] var0) {
      if (var0.length != 4) {
         return UUID.randomUUID();
      }

      long var1 = (long)var0[0] << 32 | var0[1] & 4294967295L;
      long var3 = (long)var0[2] << 32 | var0[3] & 4294967295L;
      return new UUID(var1, var3);
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
