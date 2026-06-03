package rich.command.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.class_310;
import rich.command.Command;
import rich.modules.impl.render.GpsArrow;

public class GpsCommand extends Command {
   public GpsCommand() {
      super("gps", "GPS навигация к координатам");
   }

   @Override
   public void execute(String var1, String[] var2) {
      if (var2.length == 0) {
         this.logDirect("§7Использование: §f.gps set <x> <z> §7или §f.gps off");
      } else {
         switch (var2[0].toLowerCase()) {
            case "set":
               if (var2.length < 3) {
                  this.logDirect("§7Использование: §f.gps set <x> <z>");
                  return;
               }

               try {
                  double var5 = Double.parseDouble(var2[1]);
                  double var7 = Double.parseDouble(var2[2]);
                  double var9 = class_310.method_1551().field_1724 != null ? class_310.method_1551().field_1724.method_23318() : 64.0;
                  GpsArrow.setTarget(var5, var9, var7);
                  this.logDirect("§aGPS установлен: §f" + (int)var5 + " " + (int)var7);
               } catch (NumberFormatException var11) {
                  this.logDirect("§cНеверные координаты!");
               }
               break;
            case "off":
               GpsArrow.clearTarget();
               this.logDirect("§cGPS отключён");
               break;
            default:
               this.logDirect("§7Использование: §f.gps set <x> <z> §7или §f.gps off");
         }
      }
   }

   @Override
   public Stream<String> tabComplete(String var1, String[] var2) {
      return var2.length == 1 ? Stream.of("set", "off").filter(var1x -> var1x.startsWith(var2[0].toLowerCase())) : Stream.empty();
   }

   @Override
   public String getShortDesc() {
      return "GPS навигация к координатам";
   }

   @Override
   public List<String> getLongDesc() {
      return Arrays.asList(
         "Показывает стрелку и расстояние до заданных координат.", "Использование:", "> .gps set <x> <y> <z> - Установить цель", "> .gps off - Убрать GPS"
      );
   }
}
