package rich.command.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_5250;
import net.minecraft.class_2558.class_10609;
import net.minecraft.class_2568.class_10613;
import rich.IMinecraft;
import rich.command.Command;
import rich.command.CommandManager;
import rich.command.helpers.Paginator;
import rich.command.helpers.TabCompleteHelper;
import rich.util.config.impl.way.WayConfig;
import rich.util.repository.way.Way;
import rich.util.repository.way.WayRepository;

public class WayCommand extends Command implements IMinecraft {
   public WayCommand() {
      super("way", "Управление точками на карте", "waypoint", "wp");
   }

   @Override
   public void execute(String var1, String[] var2) {
      CommandManager var3 = CommandManager.getInstance();
      WayRepository var4 = WayRepository.getInstance();
      if (mc.field_1724 == null) {
         this.logDirect("Вы должны быть в игре!", class_124.field_1061);
      } else {
         String var5 = var2.length > 0 ? var2[0].toLowerCase(Locale.US) : "list";
         switch (var5) {
            case "add":
               if (var2.length < 2) {
                  this.logDirect("Использование: way add <name> [x] [y] [z]", class_124.field_1061);
                  return;
               }

               String var18 = var2[1];
               class_2338 var20;
               if (var2.length >= 5) {
                  try {
                     int var22 = Integer.parseInt(var2[2]);
                     int var25 = Integer.parseInt(var2[3]);
                     int var26 = Integer.parseInt(var2[4]);
                     var20 = new class_2338(var22, var25, var26);
                  } catch (NumberFormatException var14) {
                     this.logDirect("Неверные координаты!", class_124.field_1061);
                     return;
                  }
               } else {
                  var20 = mc.field_1724.method_24515();
               }

               String var23 = var4.getCurrentServer();
               if (var23.isEmpty()) {
                  this.logDirect("Не удалось определить сервер!", class_124.field_1061);
                  return;
               }

               if (var4.hasWay(var18)) {
                  this.logDirect(String.format("Точка с именем %s уже существует!", var18), class_124.field_1061);
                  return;
               }

               var4.addWayAndSave(var18, var20, var23);
               this.logDirect(
                  String.format("§aТочка §f%s §aдобавлена на координатах §f%d %d %d", var18, var20.method_10263(), var20.method_10264(), var20.method_10260()),
                  class_124.field_1060
               );
               break;
            case "remove":
            case "del":
            case "delete":
               if (var2.length < 2) {
                  this.logDirect("Использование: way remove <name>", class_124.field_1061);
                  return;
               }

               String var17 = var2[1];
               if (!var4.hasWay(var17)) {
                  this.logDirect(String.format("Точка %s не найдена!", var17), class_124.field_1061);
                  return;
               }

               var4.deleteWayAndSave(var17);
               this.logDirect(String.format("Точка %s удалена!", var17), class_124.field_1060);
               break;
            case "clear":
               String var16 = var4.getCurrentServer();
               int var19 = 0;

               for (Way var12 : var4.getWayList().stream().filter(var1x -> var1x.server().equalsIgnoreCase(var16)).toList()) {
                  var4.deleteWay(var12.name());
                  var19++;
               }

               if (var19 > 0) {
                  WayConfig.getInstance().save();
               }

               this.logDirect(String.format("Удалено точек для этого сервера: %d", var19), class_124.field_1060);
               break;
            case "clearall":
               int var15 = var4.size();
               var4.clearListAndSave();
               this.logDirect(String.format("Все точки удалены! Удалено: %d", var15), class_124.field_1060);
               break;
            case "list":
               int var8 = 1;
               if (var2.length > 1) {
                  try {
                     var8 = Integer.parseInt(var2[1]);
                  } catch (NumberFormatException var13) {
                  }
               }

               String var9 = var4.getCurrentServer();
               List var10 = var4.getWayList().stream().filter(var1x -> var1x.server().equalsIgnoreCase(var9)).toList();
               if (var10.isEmpty()) {
                  this.logDirect("Нет точек для этого сервера!", class_124.field_1061);
                  return;
               }

               Paginator var11 = new Paginator(var10);
               var11.setPage(var8);
               var11.display(
                  () -> {
                     this.logDirectRaw(class_2561.method_43470(HelpCommand.getLine()));
                     this.logDirect("§f§lТОЧКИ §7(" + var10.size() + ")");
                     this.logDirectRaw(class_2561.method_43470(HelpCommand.getLine()));
                  },
                  var1x -> {
                     String var2x = var1x.name();
                     class_2338 var3x = var1x.pos();
                     double var4x = mc.field_1724.method_73189().method_1022(var3x.method_46558());
                     class_5250 var6 = class_2561.method_43470("  §d● §f" + var2x)
                        .method_10852(
                           class_2561.method_43470(String.format(" §8[§7%d %d %d§8]", var3x.method_10263(), var3x.method_10264(), var3x.method_10260()))
                        )
                        .method_10852(class_2561.method_43470(String.format(" §8(§7%.1fm§8)", var4x)));
                     class_5250 var7 = class_2561.method_43470("§7Нажмите чтобы удалить точку");
                     String var8x = var3.getPrefix() + "way remove " + var2x;
                     var6.method_10862(var6.method_10866().method_10949(new class_10613(var7)).method_10958(new class_10609(var8x)));
                     return var6;
                  },
                  var3.getPrefix() + var1 + " list"
               );
               break;
            default:
               this.logDirectRaw(class_2561.method_43470(HelpCommand.getLine()));
               this.logDirect("§f§lУПРАВЛЕНИЕ ТОЧКАМИ");
               this.logDirectRaw(class_2561.method_43470(HelpCommand.getLine()));
               this.logDirect("§7> way add <name> [x y z] §8- §fДобавить точку");
               this.logDirect("§7> way remove <name> §8- §fУдалить точку");
               this.logDirect("§7> way list §8- §fПоказать список точек");
               this.logDirect("§7> way clear §8- §fУдалить точки для этого сервера");
               this.logDirect("§7> way clearall §8- §fУдалить все точки");
               this.logDirectRaw(class_2561.method_43470(HelpCommand.getLine()));
         }
      }
   }

   @Override
   public Stream<String> tabComplete(String var1, String[] var2) {
      WayRepository var3 = WayRepository.getInstance();
      if (var2.length == 1) {
         return new TabCompleteHelper().append("add", "remove", "list", "clear", "clearall").sortAlphabetically().filterPrefix(var2[0]).stream();
      }

      if (var2.length == 2) {
         String var4 = var2[0].toLowerCase();
         if (var4.equals("remove") || var4.equals("del") || var4.equals("delete")) {
            String var5 = var3.getCurrentServer();
            return new TabCompleteHelper().append(var3.getWayNamesForServer(var5).toArray(new String[0])).filterPrefix(var2[1]).stream();
         }
      }

      return Stream.empty();
   }

   @Override
   public String getShortDesc() {
      return "Управление точками на карте";
   }

   @Override
   public List<String> getLongDesc() {
      return Arrays.asList(
         "Команда для управления waypoints (точками на карте)",
         "Точки отображаются на экране с расстоянием до них",
         "Использование:",
         "> way add <name> [x y z] - Добавить точку (без координат - текущая позиция)",
         "> way remove <name> - Удалить точку",
         "> way list - Показать список точек для текущего сервера",
         "> way clear - Удалить все точки для текущего сервера",
         "> way clearall - Удалить все точки"
      );
   }
}
