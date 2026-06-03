package rich.command.helpers;

import java.util.List;
import java.util.function.Function;
import net.minecraft.class_2561;
import net.minecraft.class_5250;
import net.minecraft.class_2558.class_10609;
import net.minecraft.class_2568.class_10613;
import rich.command.CommandManager;
import rich.command.impl.HelpCommand;

public class Paginator<T> {
   private final List<T> items;
   private final int itemsPerPage;
   private int currentPage;

   public Paginator(List<T> var1) {
      this(var1, 8);
   }

   public Paginator(List<T> var1, int var2) {
      this.items = var1;
      this.itemsPerPage = var2;
      this.currentPage = 1;
   }

   public int getTotalPages() {
      return Math.max(1, (int)Math.ceil((double)this.items.size() / this.itemsPerPage));
   }

   public List<T> getCurrentPageItems() {
      int var1 = (this.currentPage - 1) * this.itemsPerPage;
      int var2 = Math.min(var1 + this.itemsPerPage, this.items.size());
      return this.items.subList(var1, var2);
   }

   public void setPage(int var1) {
      this.currentPage = Math.max(1, Math.min(var1, this.getTotalPages()));
   }

   public void display(Runnable var1, Function<T, class_5250> var2, String var3) {
      CommandManager var4 = CommandManager.getInstance();
      if (var1 != null) {
         var1.run();
      }

      for (Object var6 : this.getCurrentPageItems()) {
         class_5250 var7 = (class_5250)var2.apply(var6);
         var4.sendRaw(var7);
      }

      if (this.getTotalPages() > 1) {
         this.displayNavigation(var4, var3);
      } else {
         var4.sendRaw(class_2561.method_43470(HelpCommand.getLine()));
      }
   }

   private void displayNavigation(CommandManager var1, String var2) {
      var1.sendRaw(class_2561.method_43470(HelpCommand.getLine()));
      class_5250 var3 = class_2561.method_43470("");
      if (this.currentPage > 1) {
         class_5250 var4 = class_2561.method_43470("§8[§b◄ Назад§8]");
         String var5 = var2 + " " + (this.currentPage - 1);
         var4.method_10862(
            var4.method_10866()
               .method_10949(new class_10613(class_2561.method_43470("§7Страница " + (this.currentPage - 1))))
               .method_10958(new class_10609(var5))
         );
         var3.method_10852(var4);
      } else {
         var3.method_10852(class_2561.method_43470("§8[§7◄ Назад§8]"));
      }

      var3.method_10852(class_2561.method_43470(" §7Страница §b" + this.currentPage + "§7/§b" + this.getTotalPages() + " "));
      if (this.currentPage < this.getTotalPages()) {
         class_5250 var6 = class_2561.method_43470("§8[§bВперёд ►§8]");
         String var7 = var2 + " " + (this.currentPage + 1);
         var6.method_10862(
            var6.method_10866()
               .method_10949(new class_10613(class_2561.method_43470("§7Страница " + (this.currentPage + 1))))
               .method_10958(new class_10609(var7))
         );
         var3.method_10852(var6);
      } else {
         var3.method_10852(class_2561.method_43470("§8[§7Вперёд ►§8]"));
      }

      var1.sendRaw(var3);
   }

   public static <T> void paginate(String[] var0, Paginator<T> var1, Runnable var2, Function<T, class_5250> var3, String var4) {
      if (var0.length > 0) {
         try {
            int var5 = Integer.parseInt(var0[0]);
            var1.setPage(var5);
         } catch (NumberFormatException var6) {
         }
      }

      var1.display(var2, var3, var4);
   }
}
