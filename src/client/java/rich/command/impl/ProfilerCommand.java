package rich.command.impl;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.util.Formatting;
import rich.command.Command;
import rich.util.profiler.FrameProfiler;

/**
 * Управление подробным FPS-профайлером.
 *
 * Использование:
 *   .profiler on [сек]     - включить, автодамп каждые N секунд (по умолчанию 5)
 *   .profiler off          - выключить
 *   .profiler reset        - сбросить статистику
 *   .profiler dump         - записать полный отчёт в logs/rich-fps-profile.log
 *   .profiler top [n]      - показать топ N секций в чате
 *   .profiler interval <s> - поменять интервал автодампа; 0 отключает автодамп
 *   .profiler spike <ms>   - порог спайка кадра для logs/rich-fps-spikes.log
 *   .profiler factor <x>   - относительный порог спайка: frame >= avg * x
 *   .profiler status       - текущие настройки и краткая статистика
 */
public class ProfilerCommand extends Command {
   public ProfilerCommand() {
      super("profiler", "Подробный FPS-профайлер: что именно просаживает кадр", "fps", "prof");
   }

   @Override
   public void execute(String var1, String[] var2) {
      FrameProfiler p = FrameProfiler.getInstance();
      if (var2.length == 0) {
         this.printHelp();
         return;
      }

      String sub = var2[0].toLowerCase();
      switch (sub) {
         case "on":
         case "start": {
            if (var2.length > 1) {
               Long seconds = this.parseLong(var2[1]);
               if (seconds != null) {
                  p.setAutoDumpIntervalSeconds(seconds);
               }
            }
            p.setEnabled(true);
            this.logDirect("FPS profiler включён. Играй 30-60 секунд, потом .profiler dump. Логи: logs/rich-fps-profile.log и logs/rich-fps-spikes.log", Formatting.GREEN);
            this.logDirect("Автодамп: " + p.getAutoDumpIntervalMs() + "ms, порог спайка: " + p.getSpikeMinMs() + "ms");
            break;
         }
         case "off":
         case "stop":
            p.setEnabled(false);
            this.logDirect("FPS profiler выключен.", Formatting.GREEN);
            break;
         case "reset":
            p.reset();
            this.logDirect("Статистика профайлера сброшена.", Formatting.GREEN);
            break;
         case "dump": {
            for (String line : p.topLines(15)) {
               this.logDirect(line);
            }
            String path = p.dumpToFile();
            this.logDirect("Полный подробный отчёт записан в " + path, Formatting.GREEN);
            break;
         }
         case "top": {
            int n = 15;
            if (var2.length > 1) {
               Long parsed = this.parseLong(var2[1]);
               if (parsed != null) {
                  n = parsed.intValue();
               }
            }
            if (n < 1) {
               n = 1;
            }
            if (n > 50) {
               n = 50;
            }
            List<String> lines = p.topLines(n);
            for (String line : lines) {
               this.logDirect(line);
            }
            break;
         }
         case "interval": {
            if (var2.length < 2) {
               this.logDirect("Использование: .profiler interval <seconds>, 0 = отключить автодамп", Formatting.RED);
               return;
            }
            Long seconds = this.parseLong(var2[1]);
            if (seconds == null) {
               this.logDirect("Нужно число секунд.", Formatting.RED);
               return;
            }
            p.setAutoDumpIntervalSeconds(seconds);
            this.logDirect("Интервал автодампа: " + p.getAutoDumpIntervalMs() + "ms", Formatting.GREEN);
            break;
         }
         case "spike": {
            if (var2.length < 2) {
               this.logDirect("Использование: .profiler spike <milliseconds>", Formatting.RED);
               return;
            }
            Long ms = this.parseLong(var2[1]);
            if (ms == null) {
               this.logDirect("Нужно число миллисекунд.", Formatting.RED);
               return;
            }
            p.setSpikeMinMs(ms);
            this.logDirect("Порог спайка: " + p.getSpikeMinMs() + "ms", Formatting.GREEN);
            break;
         }
         case "factor": {
            if (var2.length < 2) {
               this.logDirect("Использование: .profiler factor <number>, например 2.0", Formatting.RED);
               return;
            }
            try {
               p.setSpikeFactor(Double.parseDouble(var2[1]));
               this.logDirect("Относительный порог спайка: avg * " + p.getSpikeFactor(), Formatting.GREEN);
            } catch (NumberFormatException e) {
               this.logDirect("Нужно число, например 2.0", Formatting.RED);
            }
            break;
         }
         case "status":
            this.logDirect(p.statusLine());
            break;
         case "help":
            this.printHelp();
            break;
         default:
            this.logDirect("Неизвестная подкоманда. Используй .profiler help", Formatting.RED);
      }
   }

   private void printHelp() {
      this.logDirect("FPS profiler команды:");
      this.logDirect(".profiler on 5  -> включить и писать подробный отчёт каждые 5 секунд");
      this.logDirect(".profiler dump  -> сохранить полный отчёт вручную");
      this.logDirect(".profiler top 20 -> показать топ тяжёлых секций");
      this.logDirect(".profiler spike 40 -> логировать кадры хуже 40ms в rich-fps-spikes.log");
      this.logDirect(".profiler off / reset / status");
   }

   private Long parseLong(String value) {
      try {
         return Long.parseLong(value);
      } catch (NumberFormatException e) {
         return null;
      }
   }

   @Override
   public Stream<String> tabComplete(String var1, String[] var2) {
      if (var2.length <= 1) {
         return Stream.of("on", "off", "reset", "dump", "top", "interval", "spike", "factor", "status", "help");
      }
      return Stream.empty();
   }
}