package rich.command.helpers;

import java.util.List;
import java.util.function.Function;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
import net.minecraft.text.ClickEvent.RunCommand;
import net.minecraft.text.HoverEvent.ShowText;
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

   public void display(Runnable var1, Function<T, MutableText> var2, String var3) {
      CommandManager var4 = CommandManager.getInstance();
      if (var1 != null) {
         var1.run();
      }

      for (T var6 : this.getCurrentPageItems()) {
         MutableText var7 = var2.apply(var6);
         var4.sendRaw(var7);
      }

      if (this.getTotalPages() > 1) {
         this.displayNavigation(var4, var3);
      } else {
         var4.sendRaw(Text.literal(HelpCommand.getLine()));
      }
   }

   private void displayNavigation(CommandManager var1, String var2) {
      var1.sendRaw(Text.literal(HelpCommand.getLine()));
      MutableText var3 = Text.literal("");
      if (this.currentPage > 1) {
         MutableText var4 = Text.literal("§8[§b◄ Назад§8]");
         String var5 = var2 + " " + (this.currentPage - 1);
         var4.setStyle(
            var4.getStyle()
               .withHoverEvent(new net.minecraft.text.HoverEvent.ShowText(Text.literal("§7Страница " + (this.currentPage - 1))))
               .withClickEvent(new net.minecraft.text.ClickEvent.RunCommand(var5))
         );
         var3.append(var4);
      } else {
         var3.append(Text.literal("§8[§7◄ Назад§8]"));
      }

      var3.append(Text.literal(" §7Страница §b" + this.currentPage + "§7/§b" + this.getTotalPages() + " "));
      if (this.currentPage < this.getTotalPages()) {
         MutableText var6 = Text.literal("§8[§bВперёд ►§8]");
         String var7 = var2 + " " + (this.currentPage + 1);
         var6.setStyle(
            var6.getStyle()
               .withHoverEvent(new net.minecraft.text.HoverEvent.ShowText(Text.literal("§7Страница " + (this.currentPage + 1))))
               .withClickEvent(new net.minecraft.text.ClickEvent.RunCommand(var7))
         );
         var3.append(var6);
      } else {
         var3.append(Text.literal("§8[§7Вперёд ►§8]"));
      }

      var1.sendRaw(var3);
   }

   public static <T> void paginate(String[] var0, Paginator<T> var1, Runnable var2, Function<T, MutableText> var3, String var4) {
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
