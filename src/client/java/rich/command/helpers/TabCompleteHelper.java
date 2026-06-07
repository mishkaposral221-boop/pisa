package rich.command.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import rich.command.Command;
import rich.command.CommandManager;

public class TabCompleteHelper {
   private final List<String> completions;
   private String prefix = "";
   private boolean sorted = false;

   public TabCompleteHelper() {
      this.completions = new ArrayList<>();
   }

   public TabCompleteHelper append(String... var1) {
      this.completions.addAll(Arrays.asList(var1));
      return this;
   }

   public TabCompleteHelper addCommands(CommandManager var1) {
      String var2 = this.prefix.toLowerCase();

      for (Command var4 : var1.getCommands()) {
         String var5 = var4.getName();
         if (var2.isEmpty()) {
            this.completions.add(var5);
         } else if (var5.toLowerCase().startsWith(var2)) {
            this.completions.add(var5);
         } else {
            for (String var7 : var4.getAliases()) {
               if (var7.toLowerCase().startsWith(var2)) {
                  this.completions.add(var7);
                  break;
               }
            }
         }
      }

      return this;
   }

   public TabCompleteHelper addCommands(List<Command> var1) {
      String var2 = this.prefix.toLowerCase();

      for (Command var4 : var1) {
         String var5 = var4.getName();
         if (var2.isEmpty()) {
            this.completions.add(var5);
         } else if (var5.toLowerCase().startsWith(var2)) {
            this.completions.add(var5);
         } else {
            for (String var7 : var4.getAliases()) {
               if (var7.toLowerCase().startsWith(var2)) {
                  this.completions.add(var7);
                  break;
               }
            }
         }
      }

      return this;
   }

   public TabCompleteHelper filterPrefix(String var1) {
      this.prefix = var1.toLowerCase();
      return this;
   }

   public TabCompleteHelper sortAlphabetically() {
      this.sorted = true;
      return this;
   }

   public TabCompleteHelper prepend(String... var1) {
      ArrayList var2 = new ArrayList<>(Arrays.asList(var1));
      var2.addAll(this.completions);
      this.completions.clear();
      this.completions.addAll(var2);
      return this;
   }

   public Stream<String> stream() {
      Stream var1 = this.completions.stream().filter(var1x -> var1x.toLowerCase().startsWith(this.prefix));
      if (this.sorted) {
         var1 = var1.sorted();
      }

      return var1;
   }

   public List<String> toList() {
      return this.stream().toList();
   }

   public String[] toArray() {
      return this.stream().toArray(String[]::new);
   }
}
