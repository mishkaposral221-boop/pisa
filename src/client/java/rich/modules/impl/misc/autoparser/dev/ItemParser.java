package rich.modules.impl.misc.autoparser.dev;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.component.DataComponentTypes;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.util.string.chat.ChatMessage;

public class ItemParser extends ModuleStructure {
   private static ItemParser instance;
   private final BooleanSetting showInChat = new BooleanSetting("Показывать в чате", "").setValue(true);
   private final BooleanSetting saveToFile = new BooleanSetting("Сохранять в файл", "").setValue(true);
   private int parseCounter = 0;
   private static final Set<String> IGNORED_ITEMS = Set.of(
      "minecraft:glass_pane",
      "minecraft:white_stained_glass_pane",
      "minecraft:orange_stained_glass_pane",
      "minecraft:magenta_stained_glass_pane",
      "minecraft:light_blue_stained_glass_pane",
      "minecraft:yellow_stained_glass_pane",
      "minecraft:lime_stained_glass_pane",
      "minecraft:pink_stained_glass_pane",
      "minecraft:gray_stained_glass_pane",
      "minecraft:light_gray_stained_glass_pane",
      "minecraft:cyan_stained_glass_pane",
      "minecraft:purple_stained_glass_pane",
      "minecraft:blue_stained_glass_pane",
      "minecraft:brown_stained_glass_pane",
      "minecraft:green_stained_glass_pane",
      "minecraft:red_stained_glass_pane",
      "minecraft:black_stained_glass_pane",
      "minecraft:glass",
      "minecraft:white_stained_glass",
      "minecraft:orange_stained_glass",
      "minecraft:magenta_stained_glass",
      "minecraft:light_blue_stained_glass",
      "minecraft:yellow_stained_glass",
      "minecraft:lime_stained_glass",
      "minecraft:pink_stained_glass",
      "minecraft:gray_stained_glass",
      "minecraft:light_gray_stained_glass",
      "minecraft:cyan_stained_glass",
      "minecraft:purple_stained_glass",
      "minecraft:blue_stained_glass",
      "minecraft:brown_stained_glass",
      "minecraft:green_stained_glass",
      "minecraft:red_stained_glass",
      "minecraft:black_stained_glass",
      "minecraft:air",
      "minecraft:barrier"
   );
   private static final Map<String, String> EFFECT_TO_STATUSEFFECTS = Map.ofEntries(
      Map.entry("speed", "StatusEffects.SPEED"),
      Map.entry("slowness", "StatusEffects.SLOWNESS"),
      Map.entry("haste", "StatusEffects.HASTE"),
      Map.entry("mining_fatigue", "StatusEffects.MINING_FATIGUE"),
      Map.entry("strength", "StatusEffects.STRENGTH"),
      Map.entry("instant_health", "StatusEffects.INSTANT_HEALTH"),
      Map.entry("instant_damage", "StatusEffects.INSTANT_DAMAGE"),
      Map.entry("jump_boost", "StatusEffects.JUMP_BOOST"),
      Map.entry("nausea", "StatusEffects.NAUSEA"),
      Map.entry("regeneration", "StatusEffects.REGENERATION"),
      Map.entry("resistance", "StatusEffects.RESISTANCE"),
      Map.entry("fire_resistance", "StatusEffects.FIRE_RESISTANCE"),
      Map.entry("water_breathing", "StatusEffects.WATER_BREATHING"),
      Map.entry("invisibility", "StatusEffects.INVISIBILITY"),
      Map.entry("blindness", "StatusEffects.BLINDNESS"),
      Map.entry("night_vision", "StatusEffects.NIGHT_VISION"),
      Map.entry("hunger", "StatusEffects.HUNGER"),
      Map.entry("weakness", "StatusEffects.WEAKNESS"),
      Map.entry("poison", "StatusEffects.POISON"),
      Map.entry("wither", "StatusEffects.WITHER"),
      Map.entry("health_boost", "StatusEffects.HEALTH_BOOST"),
      Map.entry("absorption", "StatusEffects.ABSORPTION"),
      Map.entry("saturation", "StatusEffects.SATURATION"),
      Map.entry("glowing", "StatusEffects.GLOWING"),
      Map.entry("levitation", "StatusEffects.LEVITATION"),
      Map.entry("luck", "StatusEffects.LUCK"),
      Map.entry("unluck", "StatusEffects.UNLUCK"),
      Map.entry("slow_falling", "StatusEffects.SLOW_FALLING"),
      Map.entry("conduit_power", "StatusEffects.CONDUIT_POWER"),
      Map.entry("dolphins_grace", "StatusEffects.DOLPHINS_GRACE"),
      Map.entry("bad_omen", "StatusEffects.BAD_OMEN"),
      Map.entry("hero_of_the_village", "StatusEffects.HERO_OF_THE_VILLAGE"),
      Map.entry("darkness", "StatusEffects.DARKNESS")
   );

   public ItemParser() {
      super("Item Parser", "Парсинг информации о предметах", ModuleCategory.UTILITIES);
      instance = this;
      this.settings(this.showInChat, this.saveToFile);
   }

   public static ItemParser getInstance() {
      return instance;
   }

   public void parseAllSlots(List<Slot> var1, int var2, String var3) {
      this.parseCounter++;
      StringBuilder var4 = new StringBuilder();
      String var5 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
      var4.append("// ПАРСИНГ #").append(this.parseCounter).append(" | ").append(var5).append("\n");
      var4.append("// Контейнер: ").append(var3).append("\n\n");
      int var6 = 0;

      for (int var7 = 0; var7 < var2 && var7 < var1.size(); var7++) {
         Slot var8 = (Slot)var1.get(var7);
         ItemStack var9 = var8.getStack();
         if (!var9.isEmpty()) {
            String var10 = Registries.ITEM.getId(var9.getItem()).toString();
            if (!IGNORED_ITEMS.contains(var10)) {
               var6++;
               var4.append("// --- СЛОТ ").append(var7).append(" ---\n");
               this.parseItemCompact(var9, var4);
               var4.append("\n");
            }
         }
      }

      var4.append("// ИТОГО: ").append(var6).append(" предметов\n");
      String var11 = var4.toString();
      if (this.showInChat.isValue()) {
         ChatMessage.autobuymessage("§6Парсинг #" + this.parseCounter + " | §bПредметов: §f" + var6);
      }

      if (this.saveToFile.isValue()) {
         this.saveToFile(var11, this.parseCounter);
         ChatMessage.autobuymessageSuccess("Файл: parse_" + this.parseCounter + ".txt");
      }
   }

   private void parseItemCompact(ItemStack var1, StringBuilder var2) {
      String var3 = Registries.ITEM.getId(var1.getItem()).toString();
      Text var4 = (Text)var1.get(DataComponentTypes.CUSTOM_NAME);
      String var5 = var4 != null ? var4.getString() : var1.getName().getString();
      var2.append("// ").append(var5).append(" (").append(var3).append(")\n");
      LoreComponent var6 = (LoreComponent)var1.get(DataComponentTypes.LORE);
      if (var6 != null && !var6.lines().isEmpty()) {
         var2.append("List<Text> lore = List.of(\n");

         for (int var7 = 0; var7 < var6.lines().size(); var7++) {
            String var8 = ((Text)var6.lines().get(var7)).getString();
            if (!var8.trim().isEmpty()) {
               var2.append("    Text.literal(\"").append(this.escapeString(var8)).append("\")");
               if (var7 < var6.lines().size() - 1) {
                  var2.append(",");
               }

               var2.append("\n");
            }
         }

         var2.append(");\n");
      }

      if (var1.getItem() == Items.PLAYER_HEAD) {
         this.generateHeadCode(var1, var5, var2);
      } else if (var1.getItem() == Items.TOTEM_OF_UNDYING) {
         this.generateTalismanCode(var5, var2);
      } else if (var1.getItem() != Items.POTION && var1.getItem() != Items.SPLASH_POTION && var1.getItem() != Items.LINGERING_POTION) {
         this.generateGenericCode(var1, var5, var2);
      } else {
         this.generatePotionCode(var1, var5, var2);
      }
   }

   private void generateHeadCode(ItemStack var1, String var2, StringBuilder var3) {
      ProfileComponent var4 = (ProfileComponent)var1.get(DataComponentTypes.PROFILE);
      if (var4 == null) {
         var3.append("// НЕТ ПРОФИЛЯ\n");
      } else {
         GameProfile var5 = var4.getGameProfile();
         String var6 = var5.id() != null ? var5.id().toString() : "unknown";
         String var7 = "";
         Collection var8 = var5.properties().get("textures");
         if (var8 != null && !var8.isEmpty()) {
            Iterator var9 = var8.iterator();
            if (var9.hasNext()) {
               Property var10 = (Property)var9.next();
               var7 = var10.value();
            }
         }

         String var11 = var2.replace("[★] ", "");
         var3.append("spheres.add(createSphere(\"")
            .append(var2)
            .append("\", \"")
            .append(var6)
            .append("\", \"")
            .append(var7)
            .append("\", ")
            .append("Defaultpricec.getPrice(\"")
            .append(var11)
            .append("\"), lore));\n");
      }
   }

   private void generateTalismanCode(String var1, StringBuilder var2) {
      String var3 = var1.replace("[★] ", "");
      var2.append("talismans.add(new CustomItem(\"")
         .append(var1)
         .append("\", null, Items.TOTEM_OF_UNDYING, Defaultpricec.getPrice(\"")
         .append(var3)
         .append("\"), null, lore));\n");
   }

   private void generatePotionCode(ItemStack var1, String var2, StringBuilder var3) {
      String var4;
      if (var1.getItem() == Items.SPLASH_POTION) {
         var4 = "SPLASH_POTION";
      } else if (var1.getItem() == Items.LINGERING_POTION) {
         var4 = "LINGERING_POTION";
      } else {
         var4 = "POTION";
      }

      PotionContentsComponent var5 = (PotionContentsComponent)var1.get(DataComponentTypes.POTION_CONTENTS);
      if (var5 != null) {
         ArrayList var6 = new ArrayList();

         for (StatusEffectInstance var8 : var5.getEffects()) {
            var6.add(var8);
         }

         int var16 = var5.getColor();
         String var17 = String.format("0x%06X", var16 & 16777215);
         if (!var6.isEmpty()) {
            var3.append("List<StatusEffectInstance> ").append(this.toVariableName(var2)).append("Effects = List.of(\n");

            for (int var9 = 0; var9 < var6.size(); var9++) {
               StatusEffectInstance var10 = (StatusEffectInstance)var6.get(var9);
               String var11 = var10.getEffectType().getIdAsString();
               if (var11 != null) {
                  var11 = var11.replace("minecraft:", "");
               }

               String var12 = EFFECT_TO_STATUSEFFECTS.getOrDefault(var11, "StatusEffects." + var11.toUpperCase());
               int var13 = var10.getDuration();
               int var14 = var10.getAmplifier();
               var3.append("        new StatusEffectInstance(").append(var12).append(", ").append(var13).append(", ").append(var14).append(")");
               if (var9 < var6.size() - 1) {
                  var3.append(",");
               }

               var3.append(" // ").append(var11).append(" lvl:").append(var14 + 1).append(" dur:").append(this.formatDuration(var13)).append("\n");
            }

            var3.append(");\n");
            String var18 = var2.replace("[★] ", "").replace("[\ud83c\udf79] ", "").replace("[$] ", "");
            var3.append("potions.add(new CustomItem(\"")
               .append(var2)
               .append("\", null, Items.")
               .append(var4)
               .append(", Defaultpricec.getPrice(\"")
               .append(var18)
               .append("\"),\n");
            var3.append("        new PotionContentsComponent(Optional.empty(), Optional.of(")
               .append(var17)
               .append("), ")
               .append(this.toVariableName(var2))
               .append("Effects, Optional.empty()), lore));\n");
         } else {
            var3.append("// Зелье без эффектов, цвет: ").append(var17).append("\n");
            String var19 = var2.replace("[★] ", "").replace("[\ud83c\udf79] ", "").replace("[$] ", "");
            var3.append("potions.add(new CustomItem(\"")
               .append(var2)
               .append("\", null, Items.")
               .append(var4)
               .append(", Defaultpricec.getPrice(\"")
               .append(var19)
               .append("\"), null, lore));\n");
         }
      } else {
         var3.append("// Нет PotionContentsComponent\n");
         String var15 = var2.replace("[★] ", "").replace("[\ud83c\udf79] ", "").replace("[$] ", "");
         var3.append("potions.add(new CustomItem(\"")
            .append(var2)
            .append("\", null, Items.")
            .append(var4)
            .append(", Defaultpricec.getPrice(\"")
            .append(var15)
            .append("\"), null, lore));\n");
      }
   }

   private void generateGenericCode(ItemStack var1, String var2, StringBuilder var3) {
      String var4 = Registries.ITEM.getId(var1.getItem()).getPath().toUpperCase();
      var3.append("items.add(new CustomItem(\"").append(var2).append("\", null, Items.").append(var4).append(", price, null, lore));\n");
   }

   private String toVariableName(String var1) {
      String var2 = var1.replace("[★] ", "").replace("[\ud83c\udf79] ", "").replace("[$] ", "").replace(" ", "").replace("-", "").replace(".", "");
      StringBuilder var3 = new StringBuilder();
      boolean var4 = false;

      for (int var5 = 0; var5 < var2.length(); var5++) {
         char var6 = var2.charAt(var5);
         if (Character.isLetterOrDigit(var6)) {
            if (var5 == 0) {
               var3.append(Character.toLowerCase(var6));
            } else if (var4) {
               var3.append(Character.toUpperCase(var6));
               var4 = false;
            } else {
               var3.append(var6);
            }
         } else {
            var4 = true;
         }
      }

      return var3.length() == 0 ? "potion" : var3.toString();
   }

   private String formatDuration(int var1) {
      int var2 = var1 / 20;
      int var3 = var2 / 60;
      var2 %= 60;
      return String.format("%d:%02d", var3, var2);
   }

   private String escapeString(String var1) {
      return var1.replace("\\", "\\\\").replace("\"", "\\\"");
   }

   private void saveToFile(String var1, int var2) {
      try {
         MinecraftClient var3 = MinecraftClient.getInstance();
         File var4 = new File(var3.runDirectory, "item_parser");
         if (!var4.exists()) {
            var4.mkdirs();
         }

         File var5 = new File(var4, "parse_" + var2 + ".txt");

         try (PrintWriter var6 = new PrintWriter(new FileWriter(var5))) {
            var6.print(var1);
         }
      } catch (IOException var11) {
         ChatMessage.autobuymessageError("Ошибка: " + var11.getMessage());
      }
   }

   public BooleanSetting getShowInChat() {
      return this.showInChat;
   }

   public BooleanSetting getSaveToFile() {
      return this.saveToFile;
   }

   public int getParseCounter() {
      return this.parseCounter;
   }
}
