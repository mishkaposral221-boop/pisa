package rich.util.string.chat;

import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_310;
import net.minecraft.class_5250;
import rich.util.string.chat.helper.TextHelper;

public class ChatMessage {
   public static class_5250 brandmessage() {
      return (class_5250)TextHelper.applyPredefinedGradient("RunTime Visuals", "black_light_purple", true);
   }

   public static class_5250 blockesp() {
      return (class_5250)TextHelper.applyPredefinedGradient("Block Esp", "black_light_purple", true);
   }

   public static class_5250 autobuy() {
      return (class_5250)TextHelper.applyPredefinedGradient("Auto Buy", "black_light_purple", true);
   }

   public static class_5250 autobuiparcer() {
      return (class_5250)TextHelper.applyPredefinedGradient("Parce price", "black_light_purple", true);
   }

   public static void brandmessage(String var0) {
      if (class_310.method_1551().field_1724 != null) {
         class_2561 var1 = TextHelper.applyPredefinedGradient("RunTime Visuals -> ", "black_light_purple", true);
         class_5250 var2 = var1.method_27661().method_10852(class_2561.method_43470(var0));
         class_310.method_1551().field_1724.method_7353(var2, false);
      }
   }

   public static void autobuymessage(String var0) {
      if (class_310.method_1551().field_1724 != null) {
         class_2561 var1 = TextHelper.applyPredefinedGradient("AutoBuy -> ", "black_light_purple", true);
         class_5250 var2 = var1.method_27661().method_10852(class_2561.method_43470(var0));
         class_310.method_1551().field_1724.method_7353(var2, false);
      }
   }

   public static void autobuymessageSuccess(String var0) {
      if (class_310.method_1551().field_1724 != null) {
         class_2561 var1 = TextHelper.applyPredefinedGradient("AutoBuy -> ", "black_light_purple", true);
         class_5250 var2 = var1.method_27661()
            .method_10852(class_2561.method_43470(var0).method_10862(class_2583.field_24360.method_10977(class_124.field_1060)));
         class_310.method_1551().field_1724.method_7353(var2, false);
      }
   }

   public static void autobuymessageError(String var0) {
      if (class_310.method_1551().field_1724 != null) {
         class_2561 var1 = TextHelper.applyPredefinedGradient("AutoBuy -> ", "black_light_purple", true);
         class_5250 var2 = var1.method_27661()
            .method_10852(class_2561.method_43470(var0).method_10862(class_2583.field_24360.method_10977(class_124.field_1061)));
         class_310.method_1551().field_1724.method_7353(var2, false);
      }
   }

   public static void autobuymessageWarning(String var0) {
      if (class_310.method_1551().field_1724 != null) {
         class_2561 var1 = TextHelper.applyPredefinedGradient("AutoBuy -> ", "black_light_purple", true);
         class_5250 var2 = var1.method_27661()
            .method_10852(class_2561.method_43470(var0).method_10862(class_2583.field_24360.method_10977(class_124.field_1054)));
         class_310.method_1551().field_1724.method_7353(var2, false);
      }
   }

   public static void ancientmessage(String var0) {
      if (class_310.method_1551().field_1724 != null) {
         class_2561 var1 = TextHelper.applyPredefinedGradient("Ancient Xray -> ", "black_light_purple", true);
         class_5250 var2 = var1.method_27661().method_10852(class_2561.method_43470(var0));
         class_310.method_1551().field_1724.method_7353(var2, false);
      }
   }

   public static void helpmessage(String var0) {
      if (class_310.method_1551().field_1724 != null) {
         class_2561 var1 = TextHelper.applyPredefinedGradient("Help -> ", "black_light_purple", true);
         class_5250 var2 = var1.method_27661().method_10852(class_2561.method_43470(var0));
         class_310.method_1551().field_1724.method_7353(var2, false);
      }
   }

   public static void swapmessage(String var0) {
      if (class_310.method_1551().field_1724 != null) {
         class_2561 var1 = TextHelper.applyPredefinedGradient("AutoSwap -> ", "black_light_purple", true);
         class_5250 var2 = var1.method_27661().method_10852(class_2561.method_43470(var0));
         class_310.method_1551().field_1724.method_7353(var2, false);
      }
   }

   public static void ircmessage(String var0) {
      if (class_310.method_1551().field_1724 != null) {
         class_2561 var1 = TextHelper.applyPredefinedGradient("[IRC] ", "black_light_purple", true);
         class_5250 var2 = var1.method_27661().method_10852(class_2561.method_43470(var0));
         class_310.method_1551().field_1724.method_7353(var2, false);
      }
   }

   public static void ircmessageWithGreen(String var0) {
      if (class_310.method_1551().field_1724 != null) {
         class_2561 var1 = TextHelper.applyPredefinedGradient("[IRC] ", "black_light_purple", true);
         class_5250 var2 = var1.method_27661()
            .method_10852(class_2561.method_43470(var0).method_10862(class_2583.field_24360.method_10977(class_124.field_1060)));
         class_310.method_1551().field_1724.method_7353(var2, false);
      }
   }

   public static void ircmessageWithRed(String var0) {
      if (class_310.method_1551().field_1724 != null) {
         class_2561 var1 = TextHelper.applyPredefinedGradient("[IRC] ", "black_light_purple", true);
         class_5250 var2 = var1.method_27661()
            .method_10852(class_2561.method_43470(var0).method_10862(class_2583.field_24360.method_10977(class_124.field_1061)));
         class_310.method_1551().field_1724.method_7353(var2, false);
      }
   }

   public static class_2561 ircprefixDeveloper(String var0) {
      class_2561 var1 = TextHelper.applyPredefinedGradient("Developer ", "dark_red_bright_red", false);
      return var1.method_27661().method_10852(class_2561.method_43470(var0));
   }

   public static class_2561 ircprefixCurator(String var0) {
      class_2561 var1 = TextHelper.applyPredefinedGradient("Куратор ", "dark_red", false);
      return var1.method_27661().method_10852(class_2561.method_43470(var0));
   }

   public static class_2561 ircprefixYouTube(String var0) {
      class_2561 var1 = TextHelper.applyPredefinedGradient("YouTube ", "red_white", false);
      return var1.method_27661().method_10852(class_2561.method_43470(var0));
   }

   public static class_2561 ircprefixPikmi(String var0) {
      class_2561 var1 = TextHelper.applyPredefinedGradient("Пикми ", "purple_bright_pink", false);
      return var1.method_27661().method_10852(class_2561.method_43470(var0));
   }

   public static class_2561 ircprefixLabuba(String var0) {
      class_2561 var1 = TextHelper.applyPredefinedGradient("Лабуба ", "pink_dark_pink", false);
      return var1.method_27661().method_10852(class_2561.method_43470(var0));
   }

   public static class_2561 ircprefixZapen(String var0) {
      class_2561 var1 = TextHelper.applyPredefinedGradient("Запен ", "bright_red", false);
      return var1.method_27661().method_10852(class_2561.method_43470(var0));
   }

   public static class_2561 ircprefixBoost(String var0) {
      class_2561 var1 = TextHelper.applyPredefinedGradient("Буст ", "dark_green_bright_green", false);
      return var1.method_27661().method_10852(class_2561.method_43470(var0));
   }

   public static class_2561 ircprefixRich(String var0) {
      class_2561 var1 = TextHelper.applyPredefinedGradient("Рич ", "red_orange", false);
      return var1.method_27661().method_10852(class_2561.method_43470(var0));
   }

   public static class_2561 ircprefixPanda(String var0) {
      class_2561 var1 = TextHelper.applyPredefinedGradient("Панда ", "white_black", false);
      return var1.method_27661().method_10852(class_2561.method_43470(var0));
   }

   public static class_2561 ircprefixSmiley(String var0) {
      class_2561 var1 = TextHelper.applyPredefinedGradient("(●'◡'●) ", "turquoise_blue", true);
      return var1.method_27661().method_10852(class_2561.method_43470(var0));
   }

   public static class_2561 ircprefixBibi(String var0) {
      class_2561 var1 = TextHelper.applyPredefinedGradient("Биби...! ", "cyan_orange_fade", false);
      return var1.method_27661().method_10852(class_2561.method_43470(var0));
   }

   public static class_2561 ircprefixBenena(String var0) {
      class_2561 var1 = TextHelper.applyPredefinedGradient("Бэнена ", "yellow_cyan", false);
      return var1.method_27661().method_10852(class_2561.method_43470(var0));
   }

   public static class_2561 ircprefixBlyabuba(String var0) {
      class_2561 var1 = TextHelper.applyPredefinedGradient("Блябуба ", "purple_red_fade", false);
      return var1.method_27661().method_10852(class_2561.method_43470(var0));
   }
}
