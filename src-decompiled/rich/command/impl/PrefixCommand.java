package rich.command.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import rich.command.Command;
import rich.command.CommandManager;
import rich.util.config.impl.prefix.PrefixConfig;

public class PrefixCommand extends Command {
   public PrefixCommand() {
      super("prefix", "Изменение префикса команд");
   }

   @Override
   public void execute(String var1, String[] var2) {
      CommandManager var3 = CommandManager.getInstance();
      if (var2.length == 0) {
         this.logDirectRaw(class_2561.method_43470(HelpCommand.getLine()));
         this.logDirect("§f§lПРЕФИКС КОМАНД");
         this.logDirectRaw(class_2561.method_43470(HelpCommand.getLine()));
         this.logDirect("§7Текущий префикс: §f" + var3.getPrefix());
         this.logDirect("§7> prefix set <symbol> §8- §fУстановить новый префикс");
         this.logDirectRaw(class_2561.method_43470(HelpCommand.getLine()));
      } else {
         String var4 = var2[0].toLowerCase();
         if (var4.equals("set")) {
            if (var2.length < 2) {
               this.logDirect("Использование: prefix set <symbol>", class_124.field_1061);
               return;
            }

            String var5 = var2[1];
            if (var5.length() > 3) {
               this.logDirect("Префикс не может быть длиннее 3 символов!", class_124.field_1061);
               return;
            }

            if (var5.contains(" ")) {
               this.logDirect("Префикс не может содержать пробелы!", class_124.field_1061);
               return;
            }

            PrefixConfig.getInstance().setPrefixAndSave(var5);
            this.logDirect(String.format("§aПрефикс изменен на: §f%s", var5), class_124.field_1060);
            this.logDirect(String.format("§7Теперь команды вводятся как: §f%shelp", var5), class_124.field_1060);
         } else {
            this.logDirect("Использование: prefix set <symbol>", class_124.field_1061);
         }
      }
   }

   @Override
   public Stream<String> tabComplete(String var1, String[] var2) {
      if (var2.length == 1) {
         return Stream.of("set").filter(var1x -> var1x.startsWith(var2[0].toLowerCase()));
      } else {
         return var2.length == 2 && var2[0].equalsIgnoreCase("set")
            ? Stream.of(".", "!", "$", "#", "-", "/").filter(var1x -> var1x.startsWith(var2[1]))
            : Stream.empty();
      }
   }

   @Override
   public String getShortDesc() {
      return "Изменение префикса команд";
   }

   @Override
   public List<String> getLongDesc() {
      return Arrays.asList(
         "Команда для изменения префикса команд в визуалах",
         "Использование:",
         "> prefix - Показать текущий префикс",
         "> prefix set <symbol> - Установить новый префикс"
      );
   }
}
