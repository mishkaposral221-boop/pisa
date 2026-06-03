package rich.command.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.class_2561;
import rich.command.Command;
import rich.irc.IrcManager;
import rich.util.b;

public class IrcCommand extends Command {
   public IrcCommand() {
      super("irc", "Отправить сообщение в IRC чат");
   }

   @Override
   public void execute(String var1, String[] var2) {
      if (var2.length == 0) {
         this.logDirectRaw(class_2561.method_43470(HelpCommand.getLine()));
         this.logDirect("§f§lIRC ЧАТ");
         this.logDirectRaw(class_2561.method_43470(HelpCommand.getLine()));
         this.logDirect("§7> irc <сообщение> §8- §fОтправить сообщение в IRC");
         this.logDirect("§7HWID: §f" + b.a());
         this.logDirectRaw(class_2561.method_43470(HelpCommand.getLine()));
      } else {
         if (!IrcManager.getInstance().isRunning()) {
            IrcManager.getInstance().start();
         }

         String var3 = String.join(" ", var2);
         IrcManager.getInstance().sendMessage(var3);
      }
   }

   @Override
   public Stream<String> tabComplete(String var1, String[] var2) {
      return Stream.empty();
   }

   @Override
   public String getShortDesc() {
      return "Отправить сообщение в IRC чат";
   }

   @Override
   public List<String> getLongDesc() {
      return Arrays.asList(
         "Отправляет сообщение в общий IRC чат RunTime Visuals",
         "Использование:",
         "> irc <сообщение> - Отправить сообщение",
         "",
         "Требует включённый модуль IRC в утилитах."
      );
   }
}
