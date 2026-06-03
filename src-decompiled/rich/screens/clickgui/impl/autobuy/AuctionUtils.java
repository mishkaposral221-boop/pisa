package rich.screens.clickgui.impl.autobuy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_2487;
import net.minecraft.class_2561;
import net.minecraft.class_6880;
import net.minecraft.class_9279;
import net.minecraft.class_9290;
import net.minecraft.class_9304;
import net.minecraft.class_9323;
import net.minecraft.class_9334;

public class AuctionUtils {
   public static final Pattern funTimePricePattern = Pattern.compile("\\$([\\d]+(?:[\\s,][\\d]{3})*(?:\\.[\\d]{2})?)");
   private static final Pattern digitPattern = Pattern.compile("([\\d][\\d\\s,.]*)");

   public static int getPrice(class_1799 var0) {
      String var1 = null;
      class_9290 var2 = (class_9290)var0.method_58694(class_9334.field_49632);
      if (var2 != null && !var2.comp_2400().isEmpty()) {
         for (class_2561 var4 : var2.comp_2400()) {
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
         String var9 = var0.method_7964().getString();
         if (var9 != null) {
            Matcher var11 = funTimePricePattern.matcher(var9);
            if (var11.find()) {
               var1 = var11.group(1);
            }
         }
      }

      if (var1 == null || var1.isEmpty()) {
         class_9323 var10 = var0.method_57353();
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

   private static List<String> getLoreStrings(class_1799 var0) {
      class_9290 var1 = (class_9290)var0.method_58694(class_9334.field_49632);
      return var1 != null && !var1.comp_2400().isEmpty()
         ? var1.comp_2400().stream().map(var0x -> var0x.getString().toLowerCase()).collect(Collectors.toList())
         : List.of();
   }

   private static boolean loreContains(class_1799 var0, String var1) {
      List var2 = getLoreStrings(var0);
      String var3 = var1.toLowerCase();

      for (String var5 : var2) {
         if (var5.contains(var3)) {
            return true;
         }
      }

      return false;
   }

   private static boolean loreContainsAny(class_1799 var0, String... var1) {
      List var2 = getLoreStrings(var0);

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

   private static String extractChunkLoaderSize(class_1799 var0) {
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

   private static boolean isTntBlackType(class_1799 var0) {
      return loreContains(var0, "обсидиан") || loreContains(var0, "способен взорвать обсидиан");
   }

   private static String getLockpickType(class_1799 var0) {
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

   private static boolean isDragonSkin(class_1799 var0) {
      return loreContains(var0, "драконий скин");
   }

   private static String getSkinType(class_1799 var0) {
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

   private static boolean isValidTrap(class_1799 var0) {
      return loreContains(var0, "нерушимая клетка");
   }

   private static String getTrapSkinType(class_1799 var0) {
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

   private static String getSignalFireLootLevel(class_1799 var0) {
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

   private static boolean isSignalFire(class_1799 var0) {
      return var0.method_7909() == class_1802.field_17346 || var0.method_7909() == class_1802.field_23842;
   }

   private static boolean isValidSignalFire(class_1799 var0) {
      return isSignalFire(var0) && loreContains(var0, "мистический сундук");
   }

   private static boolean isValidLockpick(class_1799 var0) {
      return loreContainsAny(var0, "открыть хранилище", "этой отмычкой можно");
   }

   private static boolean isValidExperienceBottle(class_1799 var0) {
      return loreContainsAny(var0, "содержит", "ур опыта", "ур. опыта");
   }

   private static boolean isValidTnt(class_1799 var0) {
      return loreContains(var0, "динамит взрывается");
   }

   private static boolean isValidDragonSkin(class_1799 var0) {
      return loreContains(var0, "драконий скин");
   }

   private static boolean isValidChunkLoader(class_1799 var0) {
      return loreContains(var0, "прогружает чанк");
   }

   public static boolean isArmorItem(class_1799 var0) {
      return var0.method_7909() == class_1802.field_22027
         || var0.method_7909() == class_1802.field_22028
         || var0.method_7909() == class_1802.field_22029
         || var0.method_7909() == class_1802.field_22030
         || var0.method_7909() == class_1802.field_8805
         || var0.method_7909() == class_1802.field_8058
         || var0.method_7909() == class_1802.field_8348
         || var0.method_7909() == class_1802.field_8285
         || var0.method_7909() == class_1802.field_8743
         || var0.method_7909() == class_1802.field_8523
         || var0.method_7909() == class_1802.field_8396
         || var0.method_7909() == class_1802.field_8660
         || var0.method_7909() == class_1802.field_8862
         || var0.method_7909() == class_1802.field_8678
         || var0.method_7909() == class_1802.field_8416
         || var0.method_7909() == class_1802.field_8753
         || var0.method_7909() == class_1802.field_8283
         || var0.method_7909() == class_1802.field_8873
         || var0.method_7909() == class_1802.field_8218
         || var0.method_7909() == class_1802.field_8313
         || var0.method_7909() == class_1802.field_8267
         || var0.method_7909() == class_1802.field_8577
         || var0.method_7909() == class_1802.field_8570
         || var0.method_7909() == class_1802.field_8370
         || var0.method_7909() == class_1802.field_8090;
   }

   public static boolean hasThornsEnchantment(class_1799 var0) {
      class_9304 var1 = (class_9304)var0.method_58694(class_9334.field_49633);
      if (var1 != null && !var1.method_57543()) {
         for (class_6880 var3 : var1.method_57534()) {
            String var4 = var3.method_55840();
            if (var4 != null) {
               String var5 = var4.toLowerCase();
               if (var5.contains("thorns") || var5.contains("шип") || var5.contains("шипы")) {
                  return true;
               }
            }
         }
      }

      class_9290 var6 = (class_9290)var0.method_58694(class_9334.field_49632);
      if (var6 != null) {
         for (class_2561 var8 : var6.comp_2400()) {
            String var9 = var8.getString().toLowerCase();
            if (var9.contains("thorns") || var9.contains("шип") || var9.contains("шипы")) {
               return true;
            }
         }
      }

      return false;
   }

   public static boolean hasVanishingCurse(class_1799 var0) {
      class_9304 var1 = (class_9304)var0.method_58694(class_9334.field_49633);
      if (var1 != null && !var1.method_57543()) {
         for (class_6880 var3 : var1.method_57534()) {
            String var4 = var3.method_55840();
            if (var4 != null && var4.toLowerCase().contains("vanishing")) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean isUnbreakableItem(class_1799 var0) {
      class_9279 var1 = (class_9279)var0.method_58694(class_9334.field_49628);
      if (var1 != null) {
         class_2487 var2 = var1.method_57461();
         if (var2.method_68566("Unbreakable", false)) {
            return true;
         }
      }

      String var3 = var0.method_7964().getString().toLowerCase();
      return var3.contains("нерушим") || var3.contains("[⚒]");
   }

   public static boolean isSplashPotion(class_1799 var0) {
      return var0.method_7909() == class_1802.field_8436 || var0.method_7909() == class_1802.field_8150;
   }

   public static Map<class_6880<class_1291>, AuctionUtils.EffectData> getPotionEffects(class_1799 var0) {
      HashMap var1 = new HashMap();
      class_1844 var2 = (class_1844)var0.method_58694(class_9334.field_49651);
      if (var2 == null) {
         return var1;
      }

      for (class_1293 var4 : var2.comp_2380()) {
         var1.put(var4.method_5579(), new AuctionUtils.EffectData(var4.method_5578(), var4.method_5584()));
      }

      return var1;
   }

   public static boolean hasEffect(class_1799 var0, class_6880<class_1291> var1, int var2) {
      Map var3 = getPotionEffects(var0);
      AuctionUtils.EffectData var4 = (AuctionUtils.EffectData)var3.get(var1);
      return var4 != null && var4.amplifier >= var2;
   }

   public static boolean matchesPotionEffects(class_1799 var0, List<AuctionUtils.PotionEffectRequirement> var1) {
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

   public static boolean compareItem(class_1799 var0, class_1799 var1) {
      if (var0.method_7909() == var1.method_7909() || isSignalFire(var0) && isSignalFire(var1)) {
         if (isArmorItem(var0) && hasThornsEnchantment(var0)) {
            return false;
         }

         if (var0.method_7909() == class_1802.field_22021) {
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
         } else if (var0.method_7909() == class_1802.field_8238) {
            if (!isValidChunkLoader(var0)) {
               return false;
            }

            String var33 = extractChunkLoaderSize(var0);
            String var40 = extractChunkLoaderSize(var1);
            return var40 == null || var33 != null && var33.equals(var40);
         } else if (var0.method_7909() == class_1802.field_8626) {
            if (!isValidTnt(var0)) {
               return false;
            }

            boolean var32 = isTntBlackType(var0);
            boolean var39 = isTntBlackType(var1);
            return var32 == var39;
         } else if (var0.method_7909() == class_1802.field_8366) {
            if (!isValidLockpick(var0)) {
               return false;
            }

            String var31 = getLockpickType(var0);
            String var38 = getLockpickType(var1);
            return var38.equals("unknown") || var31.equals(var38);
         } else if (var0.method_7909() == class_1802.field_8407) {
            boolean var30 = isDragonSkin(var1);
            if (var30 && !isValidDragonSkin(var0)) {
               return false;
            }

            String var37 = getSkinType(var0);
            String var44 = getSkinType(var1);
            return var44.equals("unknown") || var37.equals(var44);
         } else if (var0.method_7909() == class_1802.field_8287) {
            class_9290 var29 = (class_9290)var1.method_58694(class_9334.field_49632);
            if (var29 != null && !var29.comp_2400().isEmpty()) {
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
            String var2 = var0.method_7964().getString();
            var2 = funTimePricePattern.matcher(var2).replaceAll("").trim();
            String var3 = var1.method_7964().getString();
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

            class_9290 var6 = (class_9290)var0.method_58694(class_9334.field_49632);
            class_9290 var7 = (class_9290)var1.method_58694(class_9334.field_49632);
            boolean var8 = var7 != null && !var7.comp_2400().isEmpty();
            if (isSplashPotion(var0) && isSplashPotion(var1)) {
               return comparePotionsByEffects(var0, var1);
            }

            if (!var8) {
               if (!var4.contains(var5) && !var5.contains(var4)) {
                  return false;
               }
            } else {
               List var9 = var7.comp_2400();
               if (var6 == null || var6.comp_2400().isEmpty()) {
                  return false;
               }

               List var10 = var6.comp_2400()
                  .stream()
                  .map(var0x -> cleanString(var0x.getString()))
                  .filter(var0x -> !var0x.isEmpty())
                  .collect(Collectors.toList());
               String var11 = String.join(" ", var10);
               List var12 = List.of(
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

               for (class_2561 var14 : var9) {
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

               for (class_2561 var53 : var9) {
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
                  class_9304 var55 = (class_9304)var0.method_58694(class_9334.field_49633);
                  class_9304 var57 = (class_9304)var1.method_58694(class_9334.field_49633);
                  if (var57 != null && !var57.method_57543()) {
                     if (var55 == null || var55.method_57543()) {
                        return false;
                     }

                     HashMap var58 = new HashMap();

                     for (class_6880 var61 : var55.method_57534()) {
                        String var64 = var61.method_55840();
                        if (var64 != null) {
                           String var25 = var64.replace("minecraft:", "").toLowerCase();
                           int var26 = var55.method_57536(var61);
                           var58.put(var25, var26);
                        }
                     }

                     HashMap var60 = new HashMap();

                     for (class_6880 var65 : var57.method_57534()) {
                        String var68 = var65.method_55840();
                        if (var68 != null) {
                           String var70 = var68.replace("minecraft:", "").toLowerCase();
                           int var27 = var57.method_57536(var65);
                           var60.put(var70, var27);
                        }
                     }

                     if (var60.isEmpty()) {
                        return true;
                     }

                     int var63 = 0;

                     for (Entry var69 : var60.entrySet()) {
                        String var71 = (String)var69.getKey();
                        Integer var72 = (Integer)var58.get(var71);
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

   private static String getExperienceLevel(class_1799 var0) {
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

   private static boolean comparePotionsByEffects(class_1799 var0, class_1799 var1) {
      Map var2 = getPotionEffects(var0);
      Map var3 = getPotionEffects(var1);
      if (var3.isEmpty()) {
         return false;
      }

      if (var2.isEmpty()) {
         return false;
      }

      for (Entry var5 : var3.entrySet()) {
         class_6880 var6 = (class_6880)var5.getKey();
         int var7 = ((AuctionUtils.EffectData)var5.getValue()).amplifier;
         AuctionUtils.EffectData var8 = (AuctionUtils.EffectData)var2.get(var6);
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
      public final class_6880<class_1291> effect;
      public final int minAmplifier;

      public PotionEffectRequirement(class_6880<class_1291> var1, int var2) {
         this.effect = var1;
         this.minAmplifier = var2;
      }
   }
}
