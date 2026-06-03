package rich.command.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_5250;
import net.minecraft.class_2558.class_10609;
import net.minecraft.class_2568.class_10613;
import rich.command.Command;
import rich.command.CommandManager;
import rich.command.helpers.Paginator;
import rich.command.helpers.TabCompleteHelper;

public class HelpCommand extends Command {
   public HelpCommand() {
      super("help", "Показывает список всех доступных команд");
   }

   @Override
   public void execute(String var1, String[] var2) {
      CommandManager var3 = CommandManager.getInstance();
      if (var2.length != 0 && !this.isInteger(var2[0])) {
         String var10 = var2[0].toLowerCase();
         Command var11 = var3.getCommand(var10);
         if (var11 == null) {
            this.logDirect("Команда '" + var10 + "' не найдена!", class_124.field_1061);
            return;
         }

         this.logDirectRaw(class_2561.method_43470(getLine()));
         this.logDirect("§f§l" + var11.getName().toUpperCase());
         this.logDirectRaw(class_2561.method_43470(getLine()));
         List var12 = var11.getLongDesc();
         boolean var7 = true;

         for (String var9 : var12) {
            if (!var9.isEmpty()) {
               this.logDirect("§7" + var9);
               if (var7) {
                  this.logDirectRaw(class_2561.method_43470(getLine()));
                  var7 = false;
               }
            }
         }

         this.logDirectRaw(class_2561.method_43470(getLine()));
      } else {
         int var4 = 1;
         if (var2.length > 0 && this.isInteger(var2[0])) {
            var4 = Integer.parseInt(var2[0]);
         }

         List var5 = var3.getCommands().stream().filter(var0 -> !var0.hiddenFromHelp()).collect(Collectors.toList());
         Paginator var6 = new Paginator(var5);
         var6.setPage(var4);
         var6.display(() -> {
            this.logDirectRaw(class_2561.method_43470(getLine()));
            this.logDirect("§f§lДОСТУПНЫЕ КОМАНДЫ");
            this.logDirectRaw(class_2561.method_43470(getLine()));
         }, var2x -> {
            String var3x = var2x.getName();
            String var4x = var3.getPrefix() + var3x;
            class_5250 var5x = class_2561.method_43470(" §8- §7" + var2x.getShortDesc());
            class_5250 var6x = class_2561.method_43470("");
            var6x.method_10862(var6x.method_10866().method_10977(class_124.field_1080));
            var6x.method_10852(class_2561.method_43470(var4x).method_27692(class_124.field_1068));
            var6x.method_27693("\n§7" + var2x.getShortDesc());
            var6x.method_27693("\n\n§8Нажмите, чтобы просмотреть полную справку о команде");
            String var7x = var3.getPrefix() + String.format("%s %s", var1, var3x);
            class_5250 var8 = class_2561.method_43470("§f" + var4x);
            var8.method_10852(var5x);
            var8.method_10862(var8.method_10866().method_10949(new class_10613(var6x)).method_10958(new class_10609(var7x)));
            return var8;
         }, var3.getPrefix() + var1);
      }
   }

   @Override
   public Stream<String> tabComplete(String var1, String[] var2) {
      return var2.length == 1 ? new TabCompleteHelper().filterPrefix(var2[0]).addCommands(CommandManager.getInstance()).stream() : Stream.empty();
   }

   @Override
   public String getShortDesc() {
      return "Просмотр всех доступных команд";
   }

   @Override
   public List<String> getLongDesc() {
      return Arrays.asList(
         "С помощью этой команды можно просмотреть подробную справочную информацию о том, как использовать определенные команды",
         "Использование:",
         "> help - Перечисляет все команды и их краткие описания.",
         "> help <command> - Отображение справочной информации по конкретной команде."
      );
   }

   private boolean isInteger(String var1) {
      try {
         Integer.parseInt(var1);
         return true;
      } catch (NumberFormatException var3) {
         return false;
      }
   }

   public static String getLine() {
      class_310 var0 = class_310.method_1551();
      if (var0 != null && var0.field_1772 != null) {
         int var1 = ((Double)var0.field_1690.method_42556().method_41753()).intValue();
         int var2 = var1 * 280 + 40;
         int var3 = var0.field_1772.method_1727("-");
         if (var3 <= 0) {
            var3 = 4;
         }

         int var4 = var2 / var3 - 2;
         var4 = Math.max(10, Math.min(var4, 80));
         StringBuilder var5 = new StringBuilder("§8§m");

         for (int var6 = 0; var6 < var4; var6++) {
            var5.append("-");
         }

         return var5.toString();
      } else {
         return "§8§m                    ";
      }
   }
}
