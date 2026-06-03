package rich.command.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_5250;
import net.minecraft.class_2558.class_10609;
import net.minecraft.class_2568.class_10613;
import rich.command.Command;
import rich.command.CommandManager;
import rich.command.helpers.Paginator;
import rich.command.helpers.TabCompleteHelper;
import rich.util.config.ConfigSystem;
import rich.util.config.impl.ConfigPath;

public class ConfigCommand extends Command {
   public ConfigCommand() {
      super("config", "Управление конфигурациями", "cfg");
   }

   @Override
   public void execute(String var1, String[] var2) {
      CommandManager var3 = CommandManager.getInstance();
      String var4 = var2.length > 0 ? var2[0].toLowerCase(Locale.US) : "list";
      switch (var4) {
         case "load":
            if (var2.length < 2) {
               this.logDirect("Использование: config load <name>", class_124.field_1061);
               return;
            }

            String var17 = var2[1];
            Path var20 = ConfigPath.getConfigDirectory();
            Path var23 = var20.resolve(var17 + ".json");
            if (Files.exists(var23)) {
               try {
                  ConfigSystem.getInstance().load();
                  this.logDirect(String.format("Конфигурация %s загружена!", var17));
               } catch (Exception var14) {
                  this.logDirect(String.format("Ошибка при загрузке конфига! Детали: %s", var14.getMessage()), class_124.field_1061);
               }
            } else {
               this.logDirect(String.format("Конфигурация %s не найдена!", var17), class_124.field_1061);
            }
            break;
         case "save":
            if (var2.length < 2) {
               ConfigSystem.getInstance().save();
               this.logDirect("Конфигурация сохранена!");
               return;
            }

            String var16 = var2[1];

            try {
               Path var19 = ConfigPath.getConfigDirectory();
               Path var22 = var19.resolve(var16 + ".json");
               ConfigSystem.getInstance().save();
               Path var10 = ConfigPath.getConfigFile();
               Files.copy(var10, var22);
               this.logDirect(String.format("Конфигурация %s сохранена!", var16));
            } catch (Exception var13) {
               this.logDirect(String.format("Ошибка при сохранении конфига! Детали: %s", var13.getMessage()), class_124.field_1061);
            }
            break;
         case "list":
            int var15 = 1;
            if (var2.length > 1) {
               try {
                  var15 = Integer.parseInt(var2[1]);
               } catch (NumberFormatException var12) {
               }
            }

            List var18 = this.getConfigs();
            if (var18.isEmpty()) {
               this.logDirect("Конфигурации не найдены!", class_124.field_1061);
               return;
            }

            Paginator var21 = new Paginator(var18);
            var21.setPage(var15);
            var21.display(() -> {
               this.logDirectRaw(class_2561.method_43470(HelpCommand.getLine()));
               this.logDirect("§f§lСПИСОК КОНФИГОВ");
               this.logDirectRaw(class_2561.method_43470(HelpCommand.getLine()));
            }, var1x -> {
               class_5250 var2x = class_2561.method_43470("  §b● §f" + var1x);
               class_5250 var3x = class_2561.method_43470("§7Нажмите чтобы загрузить конфиг §f" + var1x);
               String var4x = var3.getPrefix() + "config load " + var1x;
               var2x.method_10862(var2x.method_10866().method_10949(new class_10613(var3x)).method_10958(new class_10609(var4x)));
               return var2x;
            }, var3.getPrefix() + var1 + " list");
            break;
         case "dir":
            try {
               Path var7 = ConfigPath.getConfigDirectory();
               String var8 = System.getProperty("os.name").toLowerCase();
               ProcessBuilder var9;
               if (var8.contains("win")) {
                  var9 = new ProcessBuilder("explorer", var7.toAbsolutePath().toString());
               } else if (var8.contains("mac")) {
                  var9 = new ProcessBuilder("open", var7.toAbsolutePath().toString());
               } else {
                  var9 = new ProcessBuilder("xdg-open", var7.toAbsolutePath().toString());
               }

               var9.start();
               this.logDirect("Папка с конфигурациями открыта!");
            } catch (IOException var11) {
               this.logDirect("Папка с конфигурациями не найдена! " + var11.getMessage(), class_124.field_1061);
            }
            break;
         default:
            this.logDirectRaw(class_2561.method_43470(HelpCommand.getLine()));
            this.logDirect("§f§lИСПОЛЬЗОВАНИЕ");
            this.logDirectRaw(class_2561.method_43470(HelpCommand.getLine()));
            this.logDirect("§7> config load <name> §8- §fЗагружает конфиг.");
            this.logDirect("§7> config save <name> §8- §fСохраняет конфиг.");
            this.logDirect("§7> config list §8- §fВозвращает список конфигов");
            this.logDirect("§7> config dir §8- §fОткрывает папку с конфигами.");
            this.logDirectRaw(class_2561.method_43470(HelpCommand.getLine()));
      }
   }

   @Override
   public Stream<String> tabComplete(String var1, String[] var2) {
      if (var2.length == 1) {
         return new TabCompleteHelper().append("load", "save", "list", "dir").sortAlphabetically().filterPrefix(var2[0]).stream();
      }

      if (var2.length == 2) {
         String var3 = var2[0].toLowerCase();
         if (var3.equals("load") || var3.equals("save")) {
            return new TabCompleteHelper().append(this.getConfigs().toArray(new String[0])).filterPrefix(var2[1]).stream();
         }
      }

      return Stream.empty();
   }

   @Override
   public String getShortDesc() {
      return "Позволяет взаимодействовать с конфигами в визуалах";
   }

   @Override
   public List<String> getLongDesc() {
      return Arrays.asList(
         "С помощью этой команды можно загружать/сохранять конфиги",
         "Использование:",
         "> config load <name> - Загружает конфиг.",
         "> config save <name> - Сохраняет конфиг.",
         "> config list - Возвращает список конфигов",
         "> config dir - Открывает папку с конфигами."
      );
   }

   public List<String> getConfigs() {
      ArrayList var1 = new ArrayList();

      try {
         Path var2 = ConfigPath.getConfigDirectory();
         if (Files.exists(var2)) {
            Files.list(var2).filter(var0 -> var0.toString().endsWith(".json")).forEach(var1x -> {
               String var2x = var1x.getFileName().toString();
               var1.add(var2x.substring(0, var2x.length() - 5));
            });
         }
      } catch (IOException var3) {
      }

      return var1;
   }
}
