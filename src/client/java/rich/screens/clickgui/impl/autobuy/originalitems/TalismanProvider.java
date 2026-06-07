package rich.screens.clickgui.impl.autobuy.originalitems;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import rich.screens.clickgui.impl.autobuy.AutoBuyableItem;
import rich.screens.clickgui.impl.autobuy.items.customitem.CustomItem;
import rich.screens.clickgui.impl.autobuy.items.price.Defaultpricec;

public class TalismanProvider {
   public static List<AutoBuyableItem> getTalismans() {
      ArrayList var0 = new ArrayList();
      List var1 = List.of(
         Text.literal("Легендарный символ."), Text.literal("Несокрушимая мощь,"), Text.literal("Ломающая преграды.")
      );
      var0.add(new CustomItem("[★] Талисман Крушителя", null, Items.TOTEM_OF_UNDYING, Defaultpricec.getPrice("Талисман Крушителя"), null, var1, true));
      List var2 = List.of(
         Text.literal("Раздор жаждет хаоса,"), Text.literal("Даруя безумный темп,"), Text.literal("Но разрушая броню")
      );
      var0.add(new CustomItem("[★] Талисман Раздора", null, Items.TOTEM_OF_UNDYING, Defaultpricec.getPrice("Талисман Раздора"), null, var2, true));
      List var3 = List.of(
         Text.literal("Тиран подавляет слабых."), Text.literal("Дает защиту и силу,"), Text.literal("Взимая кровавый налог.")
      );
      var0.add(new CustomItem("[★] Талисман Тирана", null, Items.TOTEM_OF_UNDYING, Defaultpricec.getPrice("Талисман Тирана"), null, var3, true));
      List var4 = List.of(
         Text.literal("Чистая, дикая агрессия."), Text.literal("Граничит с безумием,"), Text.literal("Меняя жизнь на урон.")
      );
      var0.add(new CustomItem("[★] Талисман Ярости", null, Items.TOTEM_OF_UNDYING, Defaultpricec.getPrice("Талисман Ярости"), null, var4, true));
      List var5 = List.of(
         Text.literal("Вихрь не знает покоя,"), Text.literal("Ускоряя владельца"), Text.literal("И закаляя его дух.")
      );
      var0.add(new CustomItem("[★] Талисман Вихря", null, Items.TOTEM_OF_UNDYING, Defaultpricec.getPrice("Талисман Вихря"), null, var5, true));
      List var6 = List.of(
         Text.literal("Мрак сгущается рядом,"), Text.literal("Укрывая владельца"), Text.literal("И питая его силы.")
      );
      var0.add(new CustomItem("[★] Талисман Мрака", null, Items.TOTEM_OF_UNDYING, Defaultpricec.getPrice("Талисман Мрака"), null, var6, true));
      List var7 = List.of(
         Text.literal("Печать разжигает ярость,"), Text.literal("Ускоряя удары сердца"), Text.literal("И силу каждой атаки.")
      );
      var0.add(new CustomItem("[★] Талисман Демона", null, Items.TOTEM_OF_UNDYING, Defaultpricec.getPrice("Талисман Демона"), null, var7, true));
      List var8 = List.of(
         Text.literal("Несёт строгий приговор,"), Text.literal("Карая всех врагов,"), Text.literal("Но ослабляя тело.")
      );
      var0.add(new CustomItem("[★] Талисман Карателя", null, Items.TOTEM_OF_UNDYING, Defaultpricec.getPrice("Талисман Карателя"), null, var8, true));
      List var9 = List.of(
         Text.literal("Похититель праздника легок,"),
         Text.literal("Его карманы полны удачи,"),
         Text.literal("Но сердце слишком мало")
      );
      var0.add(new CustomItem("[★] Талисман Гринча", null, Items.TOTEM_OF_UNDYING, Defaultpricec.getPrice("Талисман Гринча"), null, var9, true));
      return var0;
   }
}
