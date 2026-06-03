package rich.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_5250;
import rich.util.string.chat.ChatMessage;

public abstract class Command {
   private final String name;
   private final String description;
   private final List<String> aliases;

   public Command(String var1, String var2, String... var3) {
      this.name = var1;
      this.description = var2;
      this.aliases = Arrays.asList(var3);
   }

   public abstract void execute(String var1, String[] var2);

   public Stream<String> tabComplete(String var1, String[] var2) {
      return Stream.empty();
   }

   public String getShortDesc() {
      return this.description;
   }

   public List<String> getLongDesc() {
      return Arrays.asList(this.description, "", "Использование:", "> " + this.name + " - " + this.description);
   }

   public boolean hiddenFromHelp() {
      return false;
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public List<String> getAliases() {
      return this.aliases;
   }

   public List<String> getAllNames() {
      ArrayList var1 = new ArrayList();
      var1.add(this.name);
      var1.addAll(this.aliases);
      return var1;
   }

   public boolean matches(String var1) {
      return this.name.equalsIgnoreCase(var1) || this.aliases.stream().anyMatch(var1x -> var1x.equalsIgnoreCase(var1));
   }

   protected void logDirect(String var1) {
      ChatMessage.brandmessage(var1);
   }

   protected void logDirect(String var1, class_124 var2) {
      CommandManager var3 = CommandManager.getInstance();
      if (var2 == class_124.field_1061) {
         var3.sendError(var1);
      } else if (var2 == class_124.field_1060) {
         var3.sendSuccess(var1);
      } else {
         var3.sendMessage(var1);
      }
   }

   protected void logDirect(class_2561 var1) {
      ChatMessage.brandmessage(var1.getString());
   }

   protected void logDirect(class_5250 var1) {
      ChatMessage.brandmessage(var1.getString());
   }

   protected void logDirectRaw(class_2561 var1) {
      CommandManager.getInstance().sendRaw(var1);
   }

   protected void logDirectRaw(class_5250 var1) {
      CommandManager.getInstance().sendRaw(var1);
   }
}
