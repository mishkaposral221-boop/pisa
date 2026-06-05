package rich.command.impl;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.util.Formatting;
import rich.command.Command;
import rich.util.profiler.FrameProfiler;

/**
 * Управление профилировщиком FPS.
 *
 * Использование:
 *   .profiler on      - включить замеры
 *   .profiler off     - выключить
 *   .profiler reset   - сбросить статистику
 *   .profiler dump    - записать полный отчёт в logs/rich-fps-profile.log и показать топ в чате
 *   .profiler top [n] - показать топ N самых тяжёлых секций (по умолчанию 10)
 */
public class ProfilerCommand extends Command {
   public ProfilerCommand() {
      super("profiler", "Профилировщик FPS: что нагружает кадр (логирование в logs/rich-fps-profile.log)", "fps", "prof");
   }

   @Override
   public void execute(String var1, String[] var2) {
      FrameProfiler p = FrameProfiler.getInstance();
      if (var2.length == 0) {
         if (p.isEnabled()) {
            this.logDirect("Профилировщик ВКЛЮЧЁН. Подкоманды: on/off/reset/dump/top <n>");
         } else {
            this.logDirect("Профилировщик ВЫКЛЮЧЕН. Включить: .profiler on");
         }
         return;
      }

      String sub = var2[0].toLowerCase();
      switch (sub) {
         case "on":
            p.setEnabled(true);
            this.logDirect("Профилировщик FPS включён. Лог: logs/rich-fps-profile.log, спайки: logs/rich-fps-spikes.log", Formatting.GREEN);
            break;
         case "off":
            p.setEnabled(false);
            this.logDirect("Профилировщик FPS выключен.", Formatting.GREEN);
            break;
         case "reset":
            p.reset();
            this.logDirect("Статистика профайлера сброшена.", Formatting.GREEN);
            break;
         case "dump": {
            for (String line : p.topLines(10)) {
               this.logDirect(line);
            }
            String path = p.dumpToFile();
            this.logDirect("Полный отчёт записан в " + path, Formatting.GREEN);
            break;
         }
         case "top": {
            int n = 10;
            if (var2.length > 1) {
               try {
                  n = Integer.parseInt(var2[1]);
               } catch (NumberFormatException ignored) {
               }
            }
            if (n < 1) {
               n = 1;
            }
            List<String> lines = p.topLines(n);
            for (String line : lines) {
               this.logDirect(line);
            }
            break;
         }
         default:
            this.logDirect("Неизвестная подкоманда. Доступно: on/off/reset/dump/top", Formatting.RED);
      }
   }

   @Override
   public Stream<String> tabComplete(String var1, String[] var2) {
      if (var2.length <= 1) {
         return Stream.of("on", "off", "reset", "dump", "top");
      }
      return Stream.empty();
   }
}
