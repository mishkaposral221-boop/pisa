package rich.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_5250;
import rich.command.impl.BindCommand;
import rich.command.impl.ConfigCommand;
import rich.command.impl.FriendCommand;
import rich.command.impl.GpsCommand;
import rich.command.impl.HelpCommand;
import rich.command.impl.IrcCommand;
import rich.command.impl.PrefixCommand;
import rich.events.api.EventHandler;
import rich.events.api.EventManager;
import rich.events.impl.ChatEvent;
import rich.events.impl.TabCompleteEvent;
import rich.util.config.impl.prefix.PrefixConfig;
import rich.util.string.chat.ChatMessage;

public class CommandManager {
   private static CommandManager instance;
   private final List<Command> commands;
   private String prefix;

   public CommandManager() {
      instance = this;
      this.commands = new CopyOnWriteArrayList<>();
      this.prefix = ".";
   }

   public static CommandManager getInstance() {
      return instance;
   }

   public void init() {
      this.registerCommand(new HelpCommand());
      this.registerCommand(new ConfigCommand());
      this.registerCommand(new FriendCommand());
      this.registerCommand(new BindCommand());
      this.registerCommand(new PrefixCommand());
      this.registerCommand(new GpsCommand());
      this.registerCommand(new IrcCommand());
      EventManager.register(this);
      PrefixConfig var1 = PrefixConfig.getInstance();
      var1.load();
      this.prefix = var1.getPrefix();
   }

   public void registerCommand(Command var1) {
      this.commands.add(var1);
   }

   public void unregisterCommand(Command var1) {
      this.commands.remove(var1);
   }

   public Command getCommand(String var1) {
      return this.commands.stream().filter(var1x -> var1x.matches(var1)).findFirst().orElse(null);
   }

   public List<Command> getCommands() {
      return new ArrayList<>(this.commands);
   }

   public String getPrefix() {
      return this.prefix;
   }

   public void setPrefix(String var1) {
      this.prefix = var1;
   }

   @EventHandler
   public void onChat(ChatEvent var1) {
      String var2 = var1.getMessage();
      if (var2.startsWith(this.prefix)) {
         var1.cancel();
         String var3 = var2.substring(this.prefix.length());
         if (var3.trim().isEmpty()) {
            this.execute("help");
            return;
         }

         if (!this.execute(var3)) {
            this.sendError("Неизвестная команда. Используйте " + this.prefix + "help для списка команд.");
         }
      }
   }

   @EventHandler
   public void onTabComplete(TabCompleteEvent var1) {
      String var2 = var1.prefix;
      if (var2.startsWith(this.prefix)) {
         String var3 = var2.substring(this.prefix.length());
         Stream var4 = this.tabComplete(var3);
         String[] var5 = var3.split(" ", -1);
         if (var5.length <= 1) {
            var4 = var4.map(var1x -> this.prefix + var1x);
         }

         var1.completions = var4.toArray(String[]::new);
      }
   }

   public boolean execute(String var1) {
      if (var1 != null && !var1.trim().isEmpty()) {
         String[] var2 = var1.trim().split("\\s+", 2);
         String var3 = var2[0];
         String[] var4 = var2.length > 1 ? var2[1].split("\\s+") : new String[0];
         Command var5 = this.getCommand(var3);
         if (var5 != null) {
            try {
               var5.execute(var3, var4);
               return true;
            } catch (Exception var7) {
               this.sendError("Ошибка при выполнении команды: " + var7.getMessage());
               var7.printStackTrace();
            }
         }

         return false;
      } else {
         return this.execute("help");
      }
   }

   public Stream<String> tabComplete(String var1) {
      if (var1 == null) {
         var1 = "";
      }

      String[] var2 = var1.split("\\s+", -1);
      if (var2.length <= 1) {
         String var6 = var2.length == 0 ? "" : var2[0].toLowerCase();
         return this.getCommandSuggestions(var6);
      } else {
         String var3 = var2[0];
         Command var4 = this.getCommand(var3);
         if (var4 != null) {
            String[] var5 = Arrays.copyOfRange(var2, 1, var2.length);
            return var4.tabComplete(var3, var5);
         } else {
            return Stream.empty();
         }
      }
   }

   private Stream<String> getCommandSuggestions(String var1) {
      if (var1.isEmpty()) {
         return this.commands.stream().map(Command::getName).sorted();
      }

      LinkedHashSet var2 = new LinkedHashSet();

      for (Command var4 : this.commands) {
         String var5 = var4.getName();
         if (var5.toLowerCase().startsWith(var1)) {
            var2.add(var5);
         } else {
            for (String var7 : var4.getAliases()) {
               if (var7.toLowerCase().startsWith(var1)) {
                  var2.add(var7);
                  break;
               }
            }
         }
      }

      return var2.stream().sorted();
   }

   public void sendMessage(String var1) {
      ChatMessage.brandmessage(var1);
   }

   public void sendSuccess(String var1) {
      if (class_310.method_1551().field_1724 != null) {
         class_5250 var2 = ChatMessage.brandmessage();
         class_5250 var3 = var2.method_27661()
            .method_10852(class_2561.method_43470(" -> ").method_27692(class_124.field_1063))
            .method_10852(class_2561.method_43470(var1).method_27692(class_124.field_1060));
         class_310.method_1551().field_1724.method_7353(var3, false);
      }
   }

   public void sendError(String var1) {
      if (class_310.method_1551().field_1724 != null) {
         class_5250 var2 = ChatMessage.brandmessage();
         class_5250 var3 = var2.method_27661()
            .method_10852(class_2561.method_43470(" -> ").method_27692(class_124.field_1063))
            .method_10852(class_2561.method_43470(var1).method_27692(class_124.field_1061));
         class_310.method_1551().field_1724.method_7353(var3, false);
      }
   }

   public void sendRaw(class_2561 var1) {
      if (class_310.method_1551().field_1724 != null) {
         class_310.method_1551().field_1724.method_7353(var1, false);
      }
   }
}
