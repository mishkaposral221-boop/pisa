package rich.screens.clickgui.impl.autobuy.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Formatting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.registry.RegistryWrapper.Impl;
import net.minecraft.component.type.ItemEnchantmentsComponent.Builder;

public class KrushItems {
   public static ItemStack getHelmet() {
      ItemStack var0 = new ItemStack(Items.NETHERITE_HELMET);
      ArrayList var1 = new ArrayList();
      var1.add(new KrushItems.EnchantmentData(Enchantments.FIRE_PROTECTION, 5));
      var1.add(new KrushItems.EnchantmentData(Enchantments.PROTECTION, 5));
      var1.add(new KrushItems.EnchantmentData(Enchantments.RESPIRATION, 3));
      var1.add(new KrushItems.EnchantmentData(Enchantments.PROJECTILE_PROTECTION, 5));
      var1.add(new KrushItems.EnchantmentData(Enchantments.MENDING, 1));
      var1.add(new KrushItems.EnchantmentData(Enchantments.UNBREAKING, 5));
      var1.add(new KrushItems.EnchantmentData(Enchantments.BLAST_PROTECTION, 5));
      var1.add(new KrushItems.EnchantmentData(Enchantments.AQUA_AFFINITY, 1));
      addEnchantments(var0, var1);
      setupItem(var0, createStyledName("Шлем Крушителя"), List.of(Text.literal("[★] Оригинальный предмет").formatted(Formatting.GRAY)));
      return var0;
   }

   public static ItemStack getChestplate() {
      ItemStack var0 = new ItemStack(Items.NETHERITE_CHESTPLATE);
      ArrayList var1 = new ArrayList();
      var1.add(new KrushItems.EnchantmentData(Enchantments.PROTECTION, 5));
      var1.add(new KrushItems.EnchantmentData(Enchantments.BLAST_PROTECTION, 5));
      var1.add(new KrushItems.EnchantmentData(Enchantments.FIRE_PROTECTION, 5));
      var1.add(new KrushItems.EnchantmentData(Enchantments.PROJECTILE_PROTECTION, 5));
      var1.add(new KrushItems.EnchantmentData(Enchantments.UNBREAKING, 5));
      var1.add(new KrushItems.EnchantmentData(Enchantments.MENDING, 1));
      addEnchantments(var0, var1);
      setupItem(var0, createStyledName("Нагрудник Крушителя"), List.of(Text.literal("[★] Оригинальный предмет").formatted(Formatting.GRAY)));
      return var0;
   }

   public static ItemStack getLeggings() {
      ItemStack var0 = new ItemStack(Items.NETHERITE_LEGGINGS);
      ArrayList var1 = new ArrayList();
      var1.add(new KrushItems.EnchantmentData(Enchantments.FIRE_PROTECTION, 5));
      var1.add(new KrushItems.EnchantmentData(Enchantments.PROJECTILE_PROTECTION, 5));
      var1.add(new KrushItems.EnchantmentData(Enchantments.PROTECTION, 5));
      var1.add(new KrushItems.EnchantmentData(Enchantments.MENDING, 1));
      var1.add(new KrushItems.EnchantmentData(Enchantments.UNBREAKING, 5));
      var1.add(new KrushItems.EnchantmentData(Enchantments.BLAST_PROTECTION, 5));
      addEnchantments(var0, var1);
      setupItem(var0, createStyledName("Поножи Крушителя"), List.of(Text.literal("[★] Оригинальный предмет").formatted(Formatting.GRAY)));
      return var0;
   }

   public static ItemStack getBoots() {
      ItemStack var0 = new ItemStack(Items.NETHERITE_BOOTS);
      ArrayList var1 = new ArrayList();
      var1.add(new KrushItems.EnchantmentData(Enchantments.FIRE_PROTECTION, 5));
      var1.add(new KrushItems.EnchantmentData(Enchantments.PROTECTION, 5));
      var1.add(new KrushItems.EnchantmentData(Enchantments.SOUL_SPEED, 3));
      var1.add(new KrushItems.EnchantmentData(Enchantments.FEATHER_FALLING, 4));
      var1.add(new KrushItems.EnchantmentData(Enchantments.DEPTH_STRIDER, 3));
      var1.add(new KrushItems.EnchantmentData(Enchantments.PROJECTILE_PROTECTION, 5));
      var1.add(new KrushItems.EnchantmentData(Enchantments.MENDING, 1));
      var1.add(new KrushItems.EnchantmentData(Enchantments.UNBREAKING, 5));
      var1.add(new KrushItems.EnchantmentData(Enchantments.BLAST_PROTECTION, 5));
      addEnchantments(var0, var1);
      setupItem(var0, createStyledName("Ботинки Крушителя"), List.of(Text.literal("[★] Оригинальный предмет").formatted(Formatting.GRAY)));
      return var0;
   }

   public static ItemStack getSword() {
      ItemStack var0 = new ItemStack(Items.NETHERITE_SWORD);
      ArrayList var1 = new ArrayList();
      var1.add(new KrushItems.EnchantmentData(Enchantments.SHARPNESS, 7));
      var1.add(new KrushItems.EnchantmentData(Enchantments.BANE_OF_ARTHROPODS, 7));
      var1.add(new KrushItems.EnchantmentData(Enchantments.FIRE_ASPECT, 2));
      var1.add(new KrushItems.EnchantmentData(Enchantments.SWEEPING_EDGE, 3));
      var1.add(new KrushItems.EnchantmentData(Enchantments.MENDING, 1));
      var1.add(new KrushItems.EnchantmentData(Enchantments.LOOTING, 5));
      var1.add(new KrushItems.EnchantmentData(Enchantments.SMITE, 7));
      var1.add(new KrushItems.EnchantmentData(Enchantments.UNBREAKING, 5));
      addEnchantments(var0, var1);
      setupItem(
         var0,
         createStyledName("Меч Крушителя"),
         List.of(
            Text.literal("Опытный III").formatted(Formatting.GRAY),
            Text.literal("Вампиризм II").formatted(Formatting.GRAY),
            Text.literal("Окисление II").formatted(Formatting.GRAY),
            Text.literal("Яд III").formatted(Formatting.GRAY),
            Text.literal("Детекция III").formatted(Formatting.GRAY),
            Text.literal("[★] Оригинальный предмет").formatted(Formatting.GRAY)
         )
      );
      return var0;
   }

   public static ItemStack getPickaxe() {
      ItemStack var0 = new ItemStack(Items.NETHERITE_PICKAXE);
      ArrayList var1 = new ArrayList();
      var1.add(new KrushItems.EnchantmentData(Enchantments.EFFICIENCY, 10));
      var1.add(new KrushItems.EnchantmentData(Enchantments.MENDING, 1));
      var1.add(new KrushItems.EnchantmentData(Enchantments.FORTUNE, 5));
      var1.add(new KrushItems.EnchantmentData(Enchantments.UNBREAKING, 5));
      addEnchantments(var0, var1);
      setupItem(
         var0,
         createStyledName("Кирка Крушителя"),
         List.of(
            Text.literal("Бульдозер II").formatted(Formatting.GRAY),
            Text.literal("Опытный III").formatted(Formatting.GRAY),
            Text.literal("Магнит").formatted(Formatting.GRAY),
            Text.literal("Авто-Плавка").formatted(Formatting.GRAY),
            Text.literal("Паутина").formatted(Formatting.GRAY),
            Text.literal("Пингер").formatted(Formatting.GRAY),
            Text.literal("[★] Оригинальный предмет").formatted(Formatting.GRAY)
         )
      );
      return var0;
   }

   public static ItemStack getCrossbow() {
      ItemStack var0 = new ItemStack(Items.CROSSBOW);
      ArrayList var1 = new ArrayList();
      var1.add(new KrushItems.EnchantmentData(Enchantments.MULTISHOT, 1));
      var1.add(new KrushItems.EnchantmentData(Enchantments.MENDING, 1));
      var1.add(new KrushItems.EnchantmentData(Enchantments.PIERCING, 5));
      var1.add(new KrushItems.EnchantmentData(Enchantments.UNBREAKING, 3));
      var1.add(new KrushItems.EnchantmentData(Enchantments.QUICK_CHARGE, 3));
      addEnchantments(var0, var1);
      setupItem(var0, createStyledName("Арбалет Крушителя"), List.of(Text.literal("[★] Оригинальный предмет").formatted(Formatting.GRAY)));
      return var0;
   }

   public static ItemStack getBow() {
      ItemStack var0 = new ItemStack(Items.BOW);
      ArrayList var1 = new ArrayList();
      var1.add(new KrushItems.EnchantmentData(Enchantments.POWER, 7));
      var1.add(new KrushItems.EnchantmentData(Enchantments.PUNCH, 3));
      var1.add(new KrushItems.EnchantmentData(Enchantments.FLAME, 1));
      var1.add(new KrushItems.EnchantmentData(Enchantments.INFINITY, 1));
      var1.add(new KrushItems.EnchantmentData(Enchantments.UNBREAKING, 5));
      var1.add(new KrushItems.EnchantmentData(Enchantments.MENDING, 1));
      addEnchantments(var0, var1);
      setupItem(
         var0,
         createStyledName("Лук Крушителя"),
         List.of(
            Text.literal("Снайпер II").formatted(Formatting.GRAY),
            Text.literal("Подрывник").formatted(Formatting.GRAY),
            Text.literal("[★] Оригинальный предмет").formatted(Formatting.GRAY)
         )
      );
      return var0;
   }

   public static ItemStack getTrident() {
      ItemStack var0 = new ItemStack(Items.TRIDENT);
      ArrayList var1 = new ArrayList();
      var1.add(new KrushItems.EnchantmentData(Enchantments.CHANNELING, 1));
      var1.add(new KrushItems.EnchantmentData(Enchantments.SHARPNESS, 7));
      var1.add(new KrushItems.EnchantmentData(Enchantments.FIRE_ASPECT, 2));
      var1.add(new KrushItems.EnchantmentData(Enchantments.MENDING, 1));
      var1.add(new KrushItems.EnchantmentData(Enchantments.UNBREAKING, 5));
      var1.add(new KrushItems.EnchantmentData(Enchantments.LOYALTY, 3));
      var1.add(new KrushItems.EnchantmentData(Enchantments.IMPALING, 5));
      addEnchantments(var0, var1);
      setupItem(
         var0,
         createStyledName("Трезубец Крушителя"),
         List.of(
            Text.literal("Скаут III").formatted(Formatting.GRAY),
            Text.literal("Опытный III").formatted(Formatting.GRAY),
            Text.literal("Вампиризм II").formatted(Formatting.GRAY),
            Text.literal("Ступор III").formatted(Formatting.GRAY),
            Text.literal("Притяжение II").formatted(Formatting.GRAY),
            Text.literal("Окисление II").formatted(Formatting.GRAY),
            Text.literal("Возвращение").formatted(Formatting.GRAY),
            Text.literal("Подрывник").formatted(Formatting.GRAY),
            Text.literal("Яд III").formatted(Formatting.GRAY),
            Text.literal("Детекция III").formatted(Formatting.GRAY),
            Text.literal("[★] Оригинальный предмет").formatted(Formatting.GRAY)
         )
      );
      return var0;
   }

   public static ItemStack getMace() {
      ItemStack var0 = new ItemStack(Items.MACE);
      ArrayList var1 = new ArrayList();
      var1.add(new KrushItems.EnchantmentData(Enchantments.SHARPNESS, 7));
      var1.add(new KrushItems.EnchantmentData(Enchantments.SMITE, 7));
      var1.add(new KrushItems.EnchantmentData(Enchantments.BANE_OF_ARTHROPODS, 7));
      var1.add(new KrushItems.EnchantmentData(Enchantments.DENSITY, 5));
      var1.add(new KrushItems.EnchantmentData(Enchantments.BREACH, 3));
      var1.add(new KrushItems.EnchantmentData(Enchantments.SWEEPING_EDGE, 3));
      var1.add(new KrushItems.EnchantmentData(Enchantments.FIRE_ASPECT, 2));
      var1.add(new KrushItems.EnchantmentData(Enchantments.LOOTING, 5));
      var1.add(new KrushItems.EnchantmentData(Enchantments.UNBREAKING, 5));
      var1.add(new KrushItems.EnchantmentData(Enchantments.MENDING, 1));
      addEnchantments(var0, var1);
      setupItem(
         var0,
         createStyledName("Булава Крушителя"),
         List.of(
            Text.literal("Опытный III").formatted(Formatting.GRAY),
            Text.literal("Вампиризм II").formatted(Formatting.GRAY),
            Text.literal("Окисление II").formatted(Formatting.GRAY),
            Text.literal("Яд III").formatted(Formatting.GRAY),
            Text.literal("Детекция III").formatted(Formatting.GRAY),
            Text.literal("[★] Оригинальный предмет").formatted(Formatting.GRAY)
         )
      );
      return var0;
   }

   public static ItemStack getElytra() {
      ItemStack var0 = new ItemStack(Items.ELYTRA);
      ArrayList var1 = new ArrayList();
      var1.add(new KrushItems.EnchantmentData(Enchantments.UNBREAKING, 5));
      var1.add(new KrushItems.EnchantmentData(Enchantments.MENDING, 1));
      addEnchantments(var0, var1);
      setupItem(var0, createStyledName("Элитры Крушителя"), List.of(Text.literal("[★] Оригинальный предмет").formatted(Formatting.GRAY)));
      return var0;
   }

   private static void addEnchantments(ItemStack var0, List<KrushItems.EnchantmentData> var1) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.world != null) {
         try {
            DynamicRegistryManager var3 = var2.world.getRegistryManager();
            net.minecraft.registry.RegistryWrapper.Impl var4 = var3.getOrThrow(RegistryKeys.ENCHANTMENT);
            net.minecraft.component.type.ItemEnchantmentsComponent.Builder var5 = new net.minecraft.component.type.ItemEnchantmentsComponent.Builder((ItemEnchantmentsComponent)var0.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT));

            for (KrushItems.EnchantmentData var7 : var1) {
               try {
                  Optional var8 = var4.getOptional(var7.key);
                  if (var8.isPresent()) {
                     var5.add((RegistryEntry)var8.get(), var7.level);
                  }
               } catch (Exception var9) {
               }
            }

            var0.set(DataComponentTypes.ENCHANTMENTS, var5.build());
         } catch (Exception var10) {
            var10.printStackTrace();
         }
      }
   }

   private static void setupItem(ItemStack var0, Text var1, List<Text> var2) {
      var0.set(DataComponentTypes.CUSTOM_NAME, var1);
      NbtCompound var3 = new NbtCompound();
      var3.putInt("HideFlags", 127);
      var3.putBoolean("Unbreakable", true);
      var0.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(var3));
      if (!var2.isEmpty()) {
         var0.set(DataComponentTypes.LORE, new LoreComponent(var2));
      }
   }

   private static Text createStyledName(String var0) {
      return Text.literal(var0).formatted(new Formatting[]{Formatting.BOLD, Formatting.DARK_RED});
   }

   private static class EnchantmentData {
      final RegistryKey<Enchantment> key;
      final int level;

      EnchantmentData(RegistryKey<Enchantment> var1, int var2) {
         this.key = var1;
         this.level = var2;
      }
   }
}
