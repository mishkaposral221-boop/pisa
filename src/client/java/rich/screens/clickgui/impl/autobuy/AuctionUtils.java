package rich.screens.clickgui.impl.autobuy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;

public class AuctionUtils {
   public static final Pattern funTimePricePattern = Pattern.compile("\\$([\\d]+(?:[\\s,][\\d]{3})*(?:\\.[\\d]{2})?)");
   private static final Pattern digitPattern = Pattern.compile("([\\d][\\d\\s,.]*)");

   public static int getPrice(ItemStack var0) {
      String var1 = null;
      LoreComponent var2 = (LoreComponent)var0.get(DataComponentTypes.LORE);
      if (var2 != null && !var2.lines().isEmpty()) {
         for (Text var4 : var2.lines()) {
            String var5 = var4.getString();
            if (var5.contains("$") || var5.toLowerCase().contains("цена")) {
               Matcher var6 = funTimePricePattern.matcher(var5);
               if (var6.find()) {
                  var1 = var6.group(1);
                  break;
               }

               var6 = digitPattern.matcher(var5);
               if (var6.find()) {
                  var1 = var6.group(1);
                  break;
               }
            }
         }
      }

      if (var1 == null || var1.isEmpty()) {
         String var9 = var0.getName().getString();
         if (var9 != null) {
            Matcher var11 = funTimePricePattern.matcher(var9);
            if (var11.find()) {
               var1 = var11.group(1);
            }
         }
      }

      if (var1 == null || var1.isEmpty()) {
         ComponentMap var10 = var0.getComponents();
         if (var10 != null) {
            String var12 = var10.toString();
            if (var12.contains("$")) {
               Matcher var13 = funTimePricePattern.matcher(var12);
               if (var13.find()) {
                  var1 = var13.group(1);
               }
            }
         }
      }

      if (var1 != null && !var1.isEmpty()) {
         try {
            var1 = var1.replaceAll("[\\s,.$]", "").trim();
            return var1.isEmpty() ? -1 : Integer.parseInt(var1);
         } catch (NumberFormatException var7) {
            return -1;
         }
      } else {
         return -1;
      }
   }

   private static String cleanString(String var0) {
      return var0 == null ? "" : var0.toLowerCase().trim().replaceAll("§.", "").replaceAll("[^a-zа-яё0-9\\s\\[\\]★⚒+()]", "").replaceAll("\\s+", " ");
   }

   private static List<String> getLoreStrings(ItemStack var0) {
      LoreComponent var1 = (LoreComponent)var0.get(DataComponentTypes.LORE);
      return var1 != null && !var1.lines().isEmpty()
         ? var1.lines().stream().map(var0x -> var0x.getString().toLowerCase()).collect(Collectors.toList())
         : List.of();
   }

   private static boolean loreContains(ItemStack var0, String var1) {
      List<String> var2 = getLoreStrings(var0);
      String var3 = var1.toLowerCase();

      for (String var5 : var2) {
         if (var5.contains(var3)) {
            return true;
         }
      }

      return false;
   }

   private static boolean loreContainsAny(ItemStack var0, String... var1) {
      List<String> var2 = getLoreStrings(var0);

      for (String var6 : var1) {
         String var7 = var6.toLowerCase();

         for (String var9 : var2) {
            if (var9.contains(var7)) {
               return true;
            }
         }
      }

      return false;
   }

   private static String extractChunkLoaderSize(ItemStack var0) {
      for (String var3 : getLoreStrings(var0)) {
         if (!var3.contains("(1x1)") && !var3.contains("области (1x1)")) {
            if (!var3.contains("(3x3)") && !var3.contains("области (3x3)")) {
               if (!var3.contains("(5x5)") && !var3.contains("области (5x5)")) {
                  continue;
               }

               return "5x5";
            }

            return "3x3";
         }

         return "1x1";
      }

      return null;
   }

   private static boolean isTntBlackType(ItemStack var0) {
      return loreContains(var0, "обсидиан") || loreContains(var0, "способен взорвать обсидиан");
   }

   private static String getLockpickType(ItemStack var0) {
      for (String var3 : getLoreStrings(var0)) {
         if (!var3.contains("с сферами") && !var3.contains("сферами")) {
            if (!var3.contains("с ключами") && !var3.contains("ключами")) {
               if (!var3.contains("с монетами") && !var3.contains("монетами")) {
                  continue;
               }

               return "coins";
            }

            return "keys";
         }

         return "spheres";
      }

      return "unknown";
   }

   private static boolean isDragonSkin(ItemStack var0) {
      return loreContains(var0, "драконий скин");
   }

   private static String getSkinType(ItemStack var0) {
      for (String var3 : getLoreStrings(var0)) {
         if (var3.contains("драконий скин")) {
            return "dragon";
         }

         if (var3.contains("ледяной скин")) {
            return "ice";
         }

         if (var3.contains("огненный скин")) {
            return "fire";
         }
      }

      return "unknown";
   }

   private static boolean isValidTrap(ItemStack var0) {
      return loreContains(var0, "нерушимая клетка");
   }

   private static String getTrapSkinType(ItemStack var0) {
      if (!isValidTrap(var0)) {
         return "invalid";
      }

      for (String var3 : getLoreStrings(var0)) {
         if (!var3.contains("драконий") && !var3.contains("dragon")) {
            if (!var3.contains("ледяной") && !var3.contains("ледян") && !var3.contains("ice")) {
               if (!var3.contains("огненный") && !var3.contains("fire")) {
                  continue;
               }

               return "fire";
            }

            return "ice";
         }

         return "dragon";
      }

      return "standard";
   }

   private static String getSignalFireLootLevel(ItemStack var0) {
      for (String var3 : getLoreStrings(var0)) {
         if (var3.contains("уровень лута:") || var3.contains("уровень лута")) {
            if (var3.contains("легендарный")) {
               return "legendary";
            }

            if (var3.contains("богатый")) {
               return "rich";
            }

            if (var3.contains("обычный")) {
               return "ordinary";
            }

            if (var3.contains("случайный")) {
               return "random";
            }
         }
      }

      return "unknown";
   }

   private static boolean isSignalFire(ItemStack var0) {
      return var0.getItem() == Items.CAMPFIRE || var0.getItem() == Items.SOUL_CAMPFIRE;
   }

   private static boolean isValidSignalFire(ItemStack var0) {
      return isSignalFire(var0) && loreContains(var0, "мистический сундук");
   }

   private static boolean isValidLockpick(ItemStack var0) {
      return loreContainsAny(var0, "открыть хранилище", "этой отмычкой можно");
   }

   private static boolean isValidExperienceBottle(ItemStack var0) {
      return loreContainsAny(var0, "содержит", "ур опыта", "ур. опыта");
   }

   private static boolean isValidTnt(ItemStack var0) {
      return loreContains(var0, "динамит взрывается");
   }

   private static boolean isValidDragonSkin(ItemStack var0) {
      return loreContains(var0, "драконий скин");
   }

   private static boolean isValidChunkLoader(ItemStack var0) {
      return loreContains(var0, "прогружает чанк");
   }

   public static boolean isArmorItem(ItemStack var0) {
      return var0.getItem() == Items.NETHERITE_HELMET
         || var0.getItem() == Items.NETHERITE_CHESTPLATE
         || var0.getItem() == Items.NETHERITE_LEGGINGS
         || var0.getItem() == Items.NETHERITE_BOOTS
         || var0.getItem() == Items.DIAMOND_HELMET
         || var0.getItem() == Items.DIAMOND_CHESTPLATE
         || var0.getItem() == Items.DIAMOND_LEGGINGS
         || var0.getItem() == Items.DIAMOND_BOOTS
         || var0.getItem() == Items.IRON_HELMET
         || var0.getItem() == Items.IRON_CHESTPLATE
         || var0.getItem() == Items.IRON_LEGGINGS
         || var0.getItem() == Items.IRON_BOOTS
         || var0.getItem() == Items.GOLDEN_HELMET
         || var0.getItem() == Items.GOLDEN_CHESTPLATE
         || var0.getItem() == Items.GOLDEN_LEGGINGS
         || var0.getItem() == Items.GOLDEN_BOOTS
         || var0.getItem() == Items.CHAINMAIL_HELMET
         || var0.getItem() == Items.CHAINMAIL_CHESTPLATE
         || var0.getItem() == Items.CHAINMAIL_LEGGINGS
         || var0.getItem() == Items.CHAINMAIL_BOOTS
         || var0.getItem() == Items.LEATHER_HELMET
         || var0.getItem() == Items.LEATHER_CHESTPLATE
         || var0.getItem() == Items.LEATHER_LEGGINGS
         || var0.getItem() == Items.LEATHER_BOOTS
         || var0.getItem() == Items.TURTLE_HELMET;
   }

   public static boolean hasThornsEnchantment(ItemStack var0) {
      ItemEnchantmentsComponent var1 = (ItemEnchantmentsComponent)var0.get(DataComponentTypes.ENCHANTMENTS);
      if (var1 != null && !var1.isEmpty()) {
         for (RegistryEntry var3 : var1.getEnchantments()) {
            String var4 = var3.getIdAsString();
            if (var4 != null) {
               String var5 = var4.toLowerCase();
               if (var5.contains("thorns") || var5.contains("шип") || var5.contains("шипы")) {
                  return true;
               }
            }
         }
      }

      LoreComponent var6 = (LoreComponent)var0.get(DataComponentTypes.LORE);
      if (var6 != null) {
         for (Text var8 : var6.lines()) {
            String var9 = var8.getString().toLowerCase();
            if (var9.contains("thorns") || var9.contains("шип") || var9.contains("шипы")) {
               return true;
            }
         }
      }

      return false;
   }

   public static boolean hasVanishingCurse(ItemStack var0) {
      ItemEnchantmentsComponent var1 = (ItemEnchantmentsComponent)var0.get(DataComponentTypes.ENCHANTMENTS);
      if (var1 != null && !var1.isEmpty()) {
         for (RegistryEntry var3 : var1.getEnchantments()) {
            String var4 = var3.getIdAsString();
            if (var4 != null && var4.toLowerCase().contains("vanishing")) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean isUnbreakableItem(ItemStack var0) {
      NbtComponent var1 = (NbtComponent)var0.get(DataComponentTypes.CUSTOM_DATA);
      if (var1 != null) {
         NbtCompound var2 = var1.copyNbt();
         if (var2.getBoolean("Unbreakable", false)) {
            return true;
         }
      }

      String var3 = var0.getName().getString().toLowerCase();
      return var3.contains("нерушим") || var3.contains("[⚒]");
   }

   public static boolean isSplashPotion(ItemStack var0) {
      return var0.getItem() == Items.SPLASH_POTION || var0.getItem() == Items.LINGERING_POTION;
   }

   public static Map<RegistryEntry<StatusEffect>, AuctionUtils.EffectData> getPotionEffects(ItemStack var0) {
      HashMap var1 = new HashMap();
      PotionContentsComponent var2 = (PotionContentsComponent)var0.get(DataComponentTypes.POTION_CONTENTS);
      if (var2 == null) {
         return var1;
      }

      for (StatusEffectInstance var4 : var2.customEffects()) {
         var1.put(var4.getEffectType(), new AuctionUtils.EffectData(var4.getAmplifier(), var4.getDuration()));
      }

      return var1;
   }

   public static boolean hasEffect(ItemStack var0, RegistryEntry<StatusEffect> var1, int var2) {
      Map var3 = getPotionEffects(var0);
      AuctionUtils.EffectData var4 = (AuctionUtils.EffectData)var3.get(var1);
      return var4 != null && var4.amplifier >= var2;
   }

   public static boolean matchesPotionEffects(ItemStack var0, List<AuctionUtils.PotionEffectRequirement> var1) {
      if (!isSplashPotion(var0)) {
         return false;
      }

      Map var2 = getPotionEffects(var0);
      if (var2.isEmpty()) {
         return false;
      }

      for (AuctionUtils.PotionEffectRequirement var4 : var1) {
         AuctionUtils.EffectData var5 = (AuctionUtils.EffectData)var2.get(var4.effect);
         if (var5 == null) {
            return false;
         }

         if (var5.amplifier < var4.minAmplifier) {
            return false;
         }
      }

      return true;
   }

   public static boolean compareItem(ItemStack var0, ItemStack var1) {
      if (var0.getItem() == var1.getItem() || isSignalFire(var0) && isSignalFire(var1)) {
         if (isArmorItem(var0) && hasThornsEnchantment(var0)) {
            return false;
         }

         if (var0.getItem() == Items.NETHERITE_SCRAP) {
            if (!isValidTrap(var0)) {
               return false;
            }

            if (!isValidTrap(var1)) {
               return true;
            }

            String var35 = getTrapSkinType(var0);
            String var42 = getTrapSkinType(var1);
            return !var42.equals("standard")
               ? var35.equals(var42)
               : var35.equals("standard") || var35.equals("dragon") || var35.equals("ice") || var35.equals("fire");
         } else if (isSignalFire(var0) && isSignalFire(var1)) {
            if (!isValidSignalFire(var0)) {
               return false;
            }

            String var34 = getSignalFireLootLevel(var0);
            String var41 = getSignalFireLootLevel(var1);
            return var41.equals("unknown") || var34.equals(var41);
         } else if (var0.getItem() == Items.STRUCTURE_BLOCK) {
            if (!isValidChunkLoader(var0)) {
               return false;
            }

            String var33 = extractChunkLoaderSize(var0);
            String var40 = extractChunkLoaderSize(var1);
            return var40 == null || var33 != null && var33.equals(var40);
         } else if (var0.getItem() == Items.TNT) {
            if (!isValidTnt(var0)) {
               return false;
            }

            boolean var32 = isTntBlackType(var0);
            boolean var39 = isTntBlackType(var1);
            return var32 == var39;
         } else if (var0.getItem() == Items.TRIPWIRE_HOOK) {
            if (!isValidLockpick(var0)) {
               return false;
            }

            String var31 = getLockpickType(var0);
            String var38 = getLockpickType(var1);
            return var38.equals("unknown") || var31.equals(var38);
         } else if (var0.getItem() == Items.PAPER) {
            boolean var30 = isDragonSkin(var1);
            if (var30 && !isValidDragonSkin(var0)) {
               return false;
            }

            String var37 = getSkinType(var0);
            String var44 = getSkinType(var1);
            return var44.equals("unknown") || var37.equals(var44);
         } else if (var0.getItem() == Items.EXPERIENCE_BOTTLE) {
            LoreComponent var29 = (LoreComponent)var1.get(DataComponentTypes.LORE);
            if (var29 != null && !var29.lines().isEmpty()) {
               if (!isValidExperienceBottle(var0)) {
                  return false;
               }

               String var36 = getExperienceLevel(var0);
               String var43 = getExperienceLevel(var1);
               if (!var43.equals("unknown") && !var36.equals(var43)) {
                  return false;
               }
            }

            return true;
         } else {
            String var2 = var0.getName().getString();
            var2 = funTimePricePattern.matcher(var2).replaceAll("").trim();
            String var3 = var1.getName().getString();
            String var4 = cleanString(var2);
            String var5 = cleanString(var3);
            if (var5.contains("⚒") || var5.contains("нерушим")) {
               if (!isUnbreakableItem(var0) && !hasVanishingCurse(var0)) {
                  return false;
               }

               if (var4.contains("нерушим") && var5.contains("нерушим")) {
                  return var4.contains("элитр") && var5.contains("элитр");
               }
            }

            LoreComponent var6 = (LoreComponent)var0.get(DataComponentTypes.LORE);
            LoreComponent var7 = (LoreComponent)var1.get(DataComponentTypes.LORE);
            boolean var8 = var7 != null && !var7.lines().isEmpty();
            if (isSplashPotion(var0) && isSplashPotion(var1)) {
               return comparePotionsByEffects(var0, var1);
            }

            if (!var8) {
               if (!var4.contains(var5) && !var5.contains(var4)) {
                  return false;
               }
            } else {
               List<Text> var9 = var7.lines();
               if (var6 == null || var6.lines().isEmpty()) {
                  return false;
               }

               List<String> var10 = var6.lines()
                  .stream()
                  .map(var0x -> cleanString(var0x.getString()))
                  .filter(var0x -> !var0x.isEmpty())
                  .collect(Collectors.toList());
               String var11 = String.join(" ", var10);
               List<String> var12 = List.of(
                  "с сферами",
                  "сферами",
                  "драконий скин",
                  "обсидиан",
                  "способен взорвать обсидиан",
                  "области (1x1)",
                  "области (3x3)",
                  "области (5x5)",
                  "нерушимая клетка",
                  "tier black",
                  "tier white",
                  "уровень лута легендарный",
                  "уровень лута богатый",
                  "уровень лута обычный",
                  "уровень лута случайный",
                  "мистический сундук",
                  "прогружает чанк",
                  "динамит взрывается",
                  "открыть хранилище"
               );

               for (Text var14 : var9) {
                  String var15 = cleanString(var14.getString());
                  if (!var15.isEmpty()) {
                     for (String var17 : var12) {
                        if (var15.contains(var17)) {
                           boolean var18 = false;

                           for (String var20 : var10) {
                              if (var20.contains(var17)) {
                                 var18 = true;
                                 break;
                              }
                           }

                           if (!var18 && !var11.contains(var17)) {
                              return false;
                           }
                        }
                     }
                  }
               }

               boolean var45 = false;
               boolean var46 = false;

               for (String var49 : var10) {
                  if (var49.contains("оригинальный предмет") || var49.contains("★")) {
                     var45 = true;
                  }

                  if (var49.contains("нерушим") || var49.contains("⚒")) {
                     var46 = true;
                  }
               }

               int var48 = 0;
               int var50 = 0;

               for (Text var53 : var9) {
                  String var54 = cleanString(var53.getString());
                  if (!var54.isEmpty()) {
                     boolean var56 = var54.contains("оригинальный предмет") || var54.contains("★");
                     boolean var21 = var54.contains("нерушим") || var54.contains("⚒");
                     if (var56) {
                        if (!var45) {
                           return false;
                        }

                        var48++;
                        var50++;
                     } else if (var21) {
                        if (!var46 && !isUnbreakableItem(var0) && !hasVanishingCurse(var0)) {
                           return false;
                        }

                        var48++;
                        var50++;
                     } else {
                        var50++;
                        boolean var22 = false;

                        for (String var24 : var10) {
                           if (var24.contains(var54) || var54.contains(var24)) {
                              var22 = true;
                              break;
                           }
                        }

                        if (!var22 && var11.contains(var54)) {
                           var22 = true;
                        }

                        if (var22) {
                           var48++;
                        }
                     }
                  }
               }

               double var52 = var50 > 0 ? (double)var48 / var50 : 1.0;
               if (var52 < 0.7) {
                  return false;
               }

               if (var45) {
                  ItemEnchantmentsComponent var55 = (ItemEnchantmentsComponent)var0.get(DataComponentTypes.ENCHANTMENTS);
                  ItemEnchantmentsComponent var57 = (ItemEnchantmentsComponent)var1.get(DataComponentTypes.ENCHANTMENTS);
                  if (var57 != null && !var57.isEmpty()) {
                     if (var55 == null || var55.isEmpty()) {
                        return false;
                     }

                     HashMap<String, Integer> var58 = new HashMap<>();

                     for (RegistryEntry var61 : var55.getEnchantments()) {
                        String var64 = var61.getIdAsString();
                        if (var64 != null) {
                           String var25 = var64.replace("minecraft:", "").toLowerCase();
                           int var26 = var55.getLevel(var61);
                           var58.put(var25, var26);
                        }
                     }

                     HashMap<String, Integer> var60 = new HashMap<>();

                     for (RegistryEntry var65 : var57.getEnchantments()) {
                        String var68 = var65.getIdAsString();
                        if (var68 != null) {
                           String var70 = var68.replace("minecraft:", "").toLowerCase();
                           int var27 = var57.getLevel(var65);
                           var60.put(var70, var27);
                        }
                     }

                     if (var60.isEmpty()) {
                        return true;
                     }

                     int var63 = 0;

                     for (Map.Entry<String, Integer> var69 : var60.entrySet()) {
                        String var71 = var69.getKey();
                        Integer var72 = var58.get(var71);
                        if (var72 != null && var72 >= 1) {
                           var63++;
                        }
                     }

                     double var67 = (double)var63 / var60.size();
                     if (var67 < 1.0) {
                        return false;
                     }
                  }
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   private static String getExperienceLevel(ItemStack var0) {
      for (String var3 : getLoreStrings(var0)) {
         if (var3.contains("15")) {
            return "15";
         }

         if (var3.contains("30")) {
            return "30";
         }

         if (var3.contains("50")) {
            return "50";
         }
      }

      return "unknown";
   }

   private static boolean comparePotionsByEffects(ItemStack var0, ItemStack var1) {
      Map<RegistryEntry<StatusEffect>, AuctionUtils.EffectData> var2 = getPotionEffects(var0);
      Map<RegistryEntry<StatusEffect>, AuctionUtils.EffectData> var3 = getPotionEffects(var1);
      if (var3.isEmpty()) {
         return false;
      }

      if (var2.isEmpty()) {
         return false;
      }

      for (Map.Entry<RegistryEntry<StatusEffect>, AuctionUtils.EffectData> var5 : var3.entrySet()) {
         RegistryEntry<StatusEffect> var6 = var5.getKey();
         int var7 = var5.getValue().amplifier;
         AuctionUtils.EffectData var8 = var2.get(var6);
         if (var8 == null) {
            return false;
         }

         if (var8.amplifier < var7) {
            return false;
         }
      }

      return true;
   }

   public static class EffectData {
      public final int amplifier;
      public final int duration;

      public EffectData(int var1, int var2) {
         this.amplifier = var1;
         this.duration = var2;
      }
   }

   public static class PotionEffectRequirement {
      public final RegistryEntry<StatusEffect> effect;
      public final int minAmplifier;

      public PotionEffectRequirement(RegistryEntry<StatusEffect> var1, int var2) {
         this.effect = var1;
         this.minAmplifier = var2;
      }
   }
}
