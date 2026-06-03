package rich.command.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_5250;
import net.minecraft.class_2558.class_10609;
import net.minecraft.class_2568.class_10613;
import rich.Initialization;
import rich.command.Command;
import rich.command.CommandManager;
import rich.command.helpers.Paginator;
import rich.command.helpers.TabCompleteHelper;
import rich.modules.module.ModuleRepository;
import rich.modules.module.ModuleStructure;
import rich.util.config.ConfigSystem;
import rich.util.config.impl.bind.BindConfig;
import rich.util.string.KeyHelper;

public class BindCommand extends Command {
   public BindCommand() {
      super("bind", "Управление биндами модулей", "b");
   }

   @Override
   public void execute(String var1, String[] var2) {
      CommandManager var3 = CommandManager.getInstance();
      ModuleRepository var4 = this.getModuleRepository();
      if (var4 == null) {
         this.logDirect("Репозиторий модулей не найден!", class_124.field_1061);
      } else {
         String var5 = var2.length > 0 ? var2[0].toLowerCase(Locale.US) : "list";
         switch (var5) {
            case "add":
               if (var2.length < 3) {
                  this.logDirect("Использование: bind add <module> <key>", class_124.field_1061);
                  return;
               }

               String var16 = var2[1];
               String var20 = var2[2];
               ModuleStructure var23 = this.findModule(var4, var16);
               if (var23 == null) {
                  this.logDirect(String.format("Модуль %s не найден!", var16), class_124.field_1061);
                  return;
               }

               int var11 = KeyHelper.getKeyCode(var20);
               if (var11 == -1) {
                  this.logDirect(String.format("Неизвестная клавиша: %s", var20), class_124.field_1061);
                  return;
               }

               var23.setKey(var11);
               ConfigSystem.getInstance().save();
               this.logDirect(
                  String.format("§aМодуль §f%s §aпривязан к клавише §f%s", var23.getName(), KeyHelper.getKeyName(var11).toLowerCase()), class_124.field_1060
               );
               break;
            case "remove":
            case "del":
            case "delete":
               if (var2.length < 2) {
                  this.logDirect("Использование: bind remove <module>", class_124.field_1061);
                  return;
               }

               String var15 = var2[1];
               ModuleStructure var19 = this.findModule(var4, var15);
               if (var19 == null) {
                  this.logDirect(String.format("Модуль %s не найден!", var15), class_124.field_1061);
                  return;
               }

               var19.setKey(-1);
               ConfigSystem.getInstance().save();
               this.logDirect(String.format("Бинд для модуля %s удален!", var19.getName()), class_124.field_1060);
               break;
            case "clear":
               int var14 = 0;

               for (ModuleStructure var22 : var4.modules()) {
                  if (var22.getKey() != -1) {
                     var22.setKey(-1);
                     var14++;
                  }
               }

               ConfigSystem.getInstance().save();
               this.logDirect(String.format("Все бинды модулей удалены! Удалено: %d", var14), class_124.field_1060);
               break;
            case "set":
               if (var2.length < 3) {
                  this.logDirect("Использование: bind set <target> <key>", class_124.field_1061);
                  this.logDirect("Доступные цели: Bind", class_124.field_1061);
                  return;
               }

               String var13 = var2[1].toLowerCase(Locale.US);
               String var17 = var2[2];
               int var21 = KeyHelper.getKeyCode(var17);
               if (var21 == -1) {
                  this.logDirect(String.format("Неизвестная клавиша: %s", var17), class_124.field_1061);
                  return;
               }

               if (var13.equals("Bind")) {
                  BindConfig.getInstance().setKeyAndSave(var21);
                  this.logDirect(String.format("§aКлавиша для Bind изменена на: §f%s", KeyHelper.getKeyName(var21).toLowerCase()), class_124.field_1060);
               } else {
                  this.logDirect(String.format("Неизвестная цель: %s", var13), class_124.field_1061);
               }
               break;
            case "list":
               int var8 = 1;
               if (var2.length > 1) {
                  try {
                     var8 = Integer.parseInt(var2[1]);
                  } catch (NumberFormatException var12) {
                  }
               }

               List var9 = var4.modules().stream().filter(var0 -> var0.getKey() != -1 && var0.getKey() != -1).collect(Collectors.toList());
               if (var9.isEmpty()) {
                  this.logDirect("Нет модулей с биндами!", class_124.field_1061);
                  return;
               }

               Paginator var10 = new Paginator(var9);
               var10.setPage(var8);
               var10.display(() -> {
                  this.logDirectRaw(class_2561.method_43470(HelpCommand.getLine()));
                  this.logDirect("§f§lСПИСОК БИНДОВ §7(" + var9.size() + ")");
                  this.logDirectRaw(class_2561.method_43470(HelpCommand.getLine()));
               }, var1x -> {
                  String var2x = var1x.getName();
                  String var3x = KeyHelper.getKeyName(var1x.getKey()).toLowerCase();
                  class_5250 var4x = class_2561.method_43470("  §b● §f" + var2x).method_10852(class_2561.method_43470(" §8[§7" + var3x + "§8]"));
                  class_5250 var5x = class_2561.method_43470("§7Нажмите чтобы удалить бинд для §f" + var2x);
                  String var6 = var3.getPrefix() + "bind remove " + var2x;
                  var4x.method_10862(var4x.method_10866().method_10949(new class_10613(var5x)).method_10958(new class_10609(var6)));
                  return var4x;
               }, var3.getPrefix() + var1 + " list");
               break;
            default:
               this.logDirectRaw(class_2561.method_43470(HelpCommand.getLine()));
               this.logDirect("§f§lУПРАВЛЕНИЕ БИНДАМИ");
               this.logDirectRaw(class_2561.method_43470(HelpCommand.getLine()));
               this.logDirect("§7> bind add <module> <key> §8- §fПривязать модуль к клавише");
               this.logDirect("§7> bind remove <module> §8- §fУдалить бинд модуля");
               this.logDirect("§7> bind list §8- §fПоказать список биндов");
               this.logDirect("§7> bind clear §8- §fУдалить все бинды");
               this.logDirect("§7> bind set Bind <key> §8- §fИзменить клавишу Bind");
               this.logDirectRaw(class_2561.method_43470(HelpCommand.getLine()));
         }
      }
   }

   @Override
   public Stream<String> tabComplete(String var1, String[] var2) {
      ModuleRepository var3 = this.getModuleRepository();
      if (var2.length == 1) {
         return new TabCompleteHelper().append("add", "remove", "list", "clear", "set").sortAlphabetically().filterPrefix(var2[0]).stream();
      }

      if (var2.length == 2) {
         String var4 = var2[0].toLowerCase();
         if (var4.equals("add")) {
            return new TabCompleteHelper().append(this.getModuleNames(var3)).filterPrefix(var2[1]).stream();
         }

         if (var4.equals("remove") || var4.equals("del") || var4.equals("delete")) {
            return new TabCompleteHelper().append(this.getBoundModuleNames(var3)).filterPrefix(var2[1]).stream();
         }

         if (var4.equals("set")) {
            return new TabCompleteHelper().append("Bind").filterPrefix(var2[1]).stream();
         }
      }

      if (var2.length == 3) {
         String var5 = var2[0].toLowerCase();
         if (var5.equals("add") || var5.equals("set")) {
            return new TabCompleteHelper().append(KeyHelper.getAllKeyNames()).filterPrefix(var2[2]).stream();
         }
      }

      return Stream.empty();
   }

   @Override
   public String getShortDesc() {
      return "Управление биндами модулей";
   }

   @Override
   public List<String> getLongDesc() {
      return Arrays.asList(
         "Команда для управления биндами модулей и GUI",
         "Использование:",
         "> bind add <module> <key> - Привязать модуль к клавише",
         "> bind remove <module> - Удалить бинд модуля",
         "> bind list - Показать список биндов",
         "> bind clear - Удалить все бинды",
         "> bind set Bind <key> - Изменить клавишу Bind"
      );
   }

   private ModuleRepository getModuleRepository() {
      Initialization var1 = Initialization.getInstance();
      return var1 != null && var1.getManager() != null ? var1.getManager().getModuleRepository() : null;
   }

   private ModuleStructure findModule(ModuleRepository var1, String var2) {
      return var1.modules().stream().filter(var1x -> var1x.getName().equalsIgnoreCase(var2)).findFirst().orElse(null);
   }

   private String[] getModuleNames(ModuleRepository var1) {
      return var1 == null ? new String[0] : var1.modules().stream().map(ModuleStructure::getName).toArray(String[]::new);
   }

   private String[] getBoundModuleNames(ModuleRepository var1) {
      return var1 == null
         ? new String[0]
         : var1.modules().stream().filter(var0 -> var0.getKey() != -1 && var0.getKey() != -1).map(ModuleStructure::getName).toArray(String[]::new);
   }
}
