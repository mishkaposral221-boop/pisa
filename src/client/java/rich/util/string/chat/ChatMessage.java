package rich.util.string.chat;

import net.minecraft.util.Formatting;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import rich.util.string.chat.helper.TextHelper;

public class ChatMessage {
   public static MutableText brandmessage() {
      return (MutableText)TextHelper.applyPredefinedGradient("RunTime Visuals", "black_light_purple", true);
   }

   public static MutableText blockesp() {
      return (MutableText)TextHelper.applyPredefinedGradient("Block Esp", "black_light_purple", true);
   }

   public static MutableText autobuy() {
      return (MutableText)TextHelper.applyPredefinedGradient("Auto Buy", "black_light_purple", true);
   }

   public static MutableText autobuiparcer() {
      return (MutableText)TextHelper.applyPredefinedGradient("Parce price", "black_light_purple", true);
   }

   public static void brandmessage(String var0) {
      if (MinecraftClient.getInstance().player != null) {
         Text var1 = TextHelper.applyPredefinedGradient("RunTime Visuals -> ", "black_light_purple", true);
         MutableText var2 = var1.copy().append(Text.literal(var0));
         MinecraftClient.getInstance().player.sendMessage(var2, false);
      }
   }

   public static void autobuymessage(String var0) {
      if (MinecraftClient.getInstance().player != null) {
         Text var1 = TextHelper.applyPredefinedGradient("AutoBuy -> ", "black_light_purple", true);
         MutableText var2 = var1.copy().append(Text.literal(var0));
         MinecraftClient.getInstance().player.sendMessage(var2, false);
      }
   }

   public static void autobuymessageSuccess(String var0) {
      if (MinecraftClient.getInstance().player != null) {
         Text var1 = TextHelper.applyPredefinedGradient("AutoBuy -> ", "black_light_purple", true);
         MutableText var2 = var1.copy()
            .append(Text.literal(var0).setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
         MinecraftClient.getInstance().player.sendMessage(var2, false);
      }
   }

   public static void autobuymessageError(String var0) {
      if (MinecraftClient.getInstance().player != null) {
         Text var1 = TextHelper.applyPredefinedGradient("AutoBuy -> ", "black_light_purple", true);
         MutableText var2 = var1.copy()
            .append(Text.literal(var0).setStyle(Style.EMPTY.withColor(Formatting.RED)));
         MinecraftClient.getInstance().player.sendMessage(var2, false);
      }
   }

   public static void autobuymessageWarning(String var0) {
      if (MinecraftClient.getInstance().player != null) {
         Text var1 = TextHelper.applyPredefinedGradient("AutoBuy -> ", "black_light_purple", true);
         MutableText var2 = var1.copy()
            .append(Text.literal(var0).setStyle(Style.EMPTY.withColor(Formatting.YELLOW)));
         MinecraftClient.getInstance().player.sendMessage(var2, false);
      }
   }

   public static void ancientmessage(String var0) {
      if (MinecraftClient.getInstance().player != null) {
         Text var1 = TextHelper.applyPredefinedGradient("Ancient Xray -> ", "black_light_purple", true);
         MutableText var2 = var1.copy().append(Text.literal(var0));
         MinecraftClient.getInstance().player.sendMessage(var2, false);
      }
   }

   public static void helpmessage(String var0) {
      if (MinecraftClient.getInstance().player != null) {
         Text var1 = TextHelper.applyPredefinedGradient("Help -> ", "black_light_purple", true);
         MutableText var2 = var1.copy().append(Text.literal(var0));
         MinecraftClient.getInstance().player.sendMessage(var2, false);
      }
   }

   public static void swapmessage(String var0) {
      if (MinecraftClient.getInstance().player != null) {
         Text var1 = TextHelper.applyPredefinedGradient("AutoSwap -> ", "black_light_purple", true);
         MutableText var2 = var1.copy().append(Text.literal(var0));
         MinecraftClient.getInstance().player.sendMessage(var2, false);
      }
   }

   public static void ircmessage(String var0) {
      if (MinecraftClient.getInstance().player != null) {
         Text var1 = TextHelper.applyPredefinedGradient("[IRC] ", "black_light_purple", true);
         MutableText var2 = var1.copy().append(Text.literal(var0));
         MinecraftClient.getInstance().player.sendMessage(var2, false);
      }
   }

   public static void ircmessageWithGreen(String var0) {
      if (MinecraftClient.getInstance().player != null) {
         Text var1 = TextHelper.applyPredefinedGradient("[IRC] ", "black_light_purple", true);
         MutableText var2 = var1.copy()
            .append(Text.literal(var0).setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
         MinecraftClient.getInstance().player.sendMessage(var2, false);
      }
   }

   public static void ircmessageWithRed(String var0) {
      if (MinecraftClient.getInstance().player != null) {
         Text var1 = TextHelper.applyPredefinedGradient("[IRC] ", "black_light_purple", true);
         MutableText var2 = var1.copy()
            .append(Text.literal(var0).setStyle(Style.EMPTY.withColor(Formatting.RED)));
         MinecraftClient.getInstance().player.sendMessage(var2, false);
      }
   }

   public static Text ircprefixDeveloper(String var0) {
      Text var1 = TextHelper.applyPredefinedGradient("Developer ", "dark_red_bright_red", false);
      return var1.copy().append(Text.literal(var0));
   }

   public static Text ircprefixCurator(String var0) {
      Text var1 = TextHelper.applyPredefinedGradient("Куратор ", "dark_red", false);
      return var1.copy().append(Text.literal(var0));
   }

   public static Text ircprefixYouTube(String var0) {
      Text var1 = TextHelper.applyPredefinedGradient("YouTube ", "red_white", false);
      return var1.copy().append(Text.literal(var0));
   }

   public static Text ircprefixPikmi(String var0) {
      Text var1 = TextHelper.applyPredefinedGradient("Пикми ", "purple_bright_pink", false);
      return var1.copy().append(Text.literal(var0));
   }

   public static Text ircprefixLabuba(String var0) {
      Text var1 = TextHelper.applyPredefinedGradient("Лабуба ", "pink_dark_pink", false);
      return var1.copy().append(Text.literal(var0));
   }

   public static Text ircprefixZapen(String var0) {
      Text var1 = TextHelper.applyPredefinedGradient("Запен ", "bright_red", false);
      return var1.copy().append(Text.literal(var0));
   }

   public static Text ircprefixBoost(String var0) {
      Text var1 = TextHelper.applyPredefinedGradient("Буст ", "dark_green_bright_green", false);
      return var1.copy().append(Text.literal(var0));
   }

   public static Text ircprefixRich(String var0) {
      Text var1 = TextHelper.applyPredefinedGradient("Рич ", "red_orange", false);
      return var1.copy().append(Text.literal(var0));
   }

   public static Text ircprefixPanda(String var0) {
      Text var1 = TextHelper.applyPredefinedGradient("Панда ", "white_black", false);
      return var1.copy().append(Text.literal(var0));
   }

   public static Text ircprefixSmiley(String var0) {
      Text var1 = TextHelper.applyPredefinedGradient("(●'◡'●) ", "turquoise_blue", true);
      return var1.copy().append(Text.literal(var0));
   }

   public static Text ircprefixBibi(String var0) {
      Text var1 = TextHelper.applyPredefinedGradient("Биби...! ", "cyan_orange_fade", false);
      return var1.copy().append(Text.literal(var0));
   }

   public static Text ircprefixBenena(String var0) {
      Text var1 = TextHelper.applyPredefinedGradient("Бэнена ", "yellow_cyan", false);
      return var1.copy().append(Text.literal(var0));
   }

   public static Text ircprefixBlyabuba(String var0) {
      Text var1 = TextHelper.applyPredefinedGradient("Блябуба ", "purple_red_fade", false);
      return var1.copy().append(Text.literal(var0));
   }
}
