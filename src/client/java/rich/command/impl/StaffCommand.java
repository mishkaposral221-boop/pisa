package rich.command.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.util.Formatting;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.ClickEvent.RunCommand;
import net.minecraft.text.HoverEvent.ShowText;
import rich.command.Command;
import rich.command.CommandManager;
import rich.command.helpers.Paginator;
import rich.command.helpers.TabCompleteHelper;
import rich.util.repository.staff.StaffUtils;

public class StaffCommand extends Command {
   public StaffCommand() {
      super("staff", "Управление списком персонала");
   }

   @Override
   public void execute(String var1, String[] var2) {
      CommandManager var3 = CommandManager.getInstance();
      String var4 = var2.length > 0 ? var2[0].toLowerCase(Locale.US) : "list";
      switch (var4) {
         case "add":
            if (var2.length < 2) {
               this.logDirect("Использование: staff add <name>", Formatting.RED);
               return;
            }

            String var13 = var2[1];
            if (StaffUtils.isStaff(var13)) {
               this.logDirect(String.format("Игрок %s уже в списке персонала!", var13), Formatting.RED);
               return;
            }

            StaffUtils.addStaffAndSave(var13);
            this.logDirect(String.format("Игрок %s добавлен в персонал!", var13), Formatting.GREEN);
            break;
         case "remove":
         case "del":
         case "delete":
            if (var2.length < 2) {
               this.logDirect("Использование: staff remove <name>", Formatting.RED);
               return;
            }

            String var12 = var2[1];
            if (!StaffUtils.isStaff(var12)) {
               this.logDirect(String.format("Игрок %s не найден в списке персонала!", var12), Formatting.RED);
               return;
            }

            StaffUtils.removeStaffAndSave(var12);
            this.logDirect(String.format("Игрок %s удален из персонала!", var12), Formatting.GREEN);
            break;
         case "clear":
            int var11 = StaffUtils.size();
            StaffUtils.clearAndSave();
            this.logDirect(String.format("Список персонала очищен! Удалено: %d", var11), Formatting.GREEN);
            break;
         case "list":
            int var7 = 1;
            if (var2.length > 1) {
               try {
                  var7 = Integer.parseInt(var2[1]);
               } catch (NumberFormatException var10) {
               }
            }

            List var8 = StaffUtils.getStaffNames();
            if (var8.isEmpty()) {
               this.logDirect("Список персонала пуст!", Formatting.RED);
               return;
            }

            Paginator var9 = new Paginator(var8);
            var9.setPage(var7);
            var9.display(() -> {
               this.logDirectRaw(Text.literal(HelpCommand.getLine()));
               this.logDirect("§f§lСПИСОК ПЕРСОНАЛА §7(" + var8.size() + ")");
               this.logDirectRaw(Text.literal(HelpCommand.getLine()));
            }, var1x -> {
               MutableText var2x = Text.literal("  §c● §f" + var1x);
               MutableText var3x = Text.literal("§7Нажмите чтобы удалить §f" + var1x + " §7из персонала");
               String var4x = var3.getPrefix() + "staff remove " + var1x;
               var2x.setStyle(var2x.getStyle().withHoverEvent(new net.minecraft.text.HoverEvent.ShowText(var3x)).withClickEvent(new net.minecraft.text.ClickEvent.RunCommand(var4x)));
               return var2x;
            }, var3.getPrefix() + var1 + " list");
            break;
         default:
            this.logDirectRaw(Text.literal(HelpCommand.getLine()));
            this.logDirect("§f§lУПРАВЛЕНИЕ ПЕРСОНАЛОМ");
            this.logDirectRaw(Text.literal(HelpCommand.getLine()));
            this.logDirect("§7> staff add <name> §8- §fДобавить игрока в персонал");
            this.logDirect("§7> staff remove <name> §8- §fУдалить игрока из персонала");
            this.logDirect("§7> staff list §8- §fПоказать список персонала");
            this.logDirect("§7> staff clear §8- §fОчистить список персонала");
            this.logDirectRaw(Text.literal(HelpCommand.getLine()));
      }
   }

   @Override
   public Stream<String> tabComplete(String var1, String[] var2) {
      if (var2.length == 1) {
         return new TabCompleteHelper().append("add", "remove", "list", "clear").sortAlphabetically().filterPrefix(var2[0]).stream();
      }

      if (var2.length == 2) {
         String var3 = var2[0].toLowerCase();
         if (var3.equals("add")) {
            return new TabCompleteHelper().append(this.getOnlinePlayers().toArray(new String[0])).filterPrefix(var2[1]).stream();
         }

         if (var3.equals("remove") || var3.equals("del") || var3.equals("delete")) {
            return new TabCompleteHelper().append(StaffUtils.getStaffNames().toArray(new String[0])).filterPrefix(var2[1]).stream();
         }
      }

      return Stream.empty();
   }

   @Override
   public String getShortDesc() {
      return "Управление списком персонала";
   }

   @Override
   public List<String> getLongDesc() {
      return Arrays.asList(
         "Команда для управления списком персонала сервера",
         "Использование:",
         "> staff add <name> - Добавить игрока в персонал",
         "> staff remove <name> - Удалить игрока из персонала",
         "> staff list - Показать список персонала",
         "> staff clear - Очистить список персонала"
      );
   }

   private List<String> getOnlinePlayers() {
      ArrayList var1 = new ArrayList();
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.getNetworkHandler() != null) {
         for (PlayerListEntry var4 : var2.getNetworkHandler().getPlayerList()) {
            String var5 = var4.getProfile().name();
            if (!StaffUtils.isStaff(var5)) {
               var1.add(var5);
            }
         }
      }

      return var1;
   }
}
