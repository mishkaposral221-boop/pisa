package rich.screens.clickgui.impl.autobuy.originalitems;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1802;
import net.minecraft.class_2561;
import rich.screens.clickgui.impl.autobuy.AutoBuyableItem;
import rich.screens.clickgui.impl.autobuy.items.customitem.CustomItem;
import rich.screens.clickgui.impl.autobuy.items.price.Defaultpricec;

public class TalismanProvider {
   public static List<AutoBuyableItem> getTalismans() {
      ArrayList var0 = new ArrayList();
      List var1 = List.of(
         class_2561.method_43470("Легендарный символ."), class_2561.method_43470("Несокрушимая мощь,"), class_2561.method_43470("Ломающая преграды.")
      );
      var0.add(new CustomItem("[★] Талисман Крушителя", null, class_1802.field_8288, Defaultpricec.getPrice("Талисман Крушителя"), null, var1, true));
      List var2 = List.of(
         class_2561.method_43470("Раздор жаждет хаоса,"), class_2561.method_43470("Даруя безумный темп,"), class_2561.method_43470("Но разрушая броню")
      );
      var0.add(new CustomItem("[★] Талисман Раздора", null, class_1802.field_8288, Defaultpricec.getPrice("Талисман Раздора"), null, var2, true));
      List var3 = List.of(
         class_2561.method_43470("Тиран подавляет слабых."), class_2561.method_43470("Дает защиту и силу,"), class_2561.method_43470("Взимая кровавый налог.")
      );
      var0.add(new CustomItem("[★] Талисман Тирана", null, class_1802.field_8288, Defaultpricec.getPrice("Талисман Тирана"), null, var3, true));
      List var4 = List.of(
         class_2561.method_43470("Чистая, дикая агрессия."), class_2561.method_43470("Граничит с безумием,"), class_2561.method_43470("Меняя жизнь на урон.")
      );
      var0.add(new CustomItem("[★] Талисман Ярости", null, class_1802.field_8288, Defaultpricec.getPrice("Талисман Ярости"), null, var4, true));
      List var5 = List.of(
         class_2561.method_43470("Вихрь не знает покоя,"), class_2561.method_43470("Ускоряя владельца"), class_2561.method_43470("И закаляя его дух.")
      );
      var0.add(new CustomItem("[★] Талисман Вихря", null, class_1802.field_8288, Defaultpricec.getPrice("Талисман Вихря"), null, var5, true));
      List var6 = List.of(
         class_2561.method_43470("Мрак сгущается рядом,"), class_2561.method_43470("Укрывая владельца"), class_2561.method_43470("И питая его силы.")
      );
      var0.add(new CustomItem("[★] Талисман Мрака", null, class_1802.field_8288, Defaultpricec.getPrice("Талисман Мрака"), null, var6, true));
      List var7 = List.of(
         class_2561.method_43470("Печать разжигает ярость,"), class_2561.method_43470("Ускоряя удары сердца"), class_2561.method_43470("И силу каждой атаки.")
      );
      var0.add(new CustomItem("[★] Талисман Демона", null, class_1802.field_8288, Defaultpricec.getPrice("Талисман Демона"), null, var7, true));
      List var8 = List.of(
         class_2561.method_43470("Несёт строгий приговор,"), class_2561.method_43470("Карая всех врагов,"), class_2561.method_43470("Но ослабляя тело.")
      );
      var0.add(new CustomItem("[★] Талисман Карателя", null, class_1802.field_8288, Defaultpricec.getPrice("Талисман Карателя"), null, var8, true));
      List var9 = List.of(
         class_2561.method_43470("Похититель праздника легок,"),
         class_2561.method_43470("Его карманы полны удачи,"),
         class_2561.method_43470("Но сердце слишком мало")
      );
      var0.add(new CustomItem("[★] Талисман Гринча", null, class_1802.field_8288, Defaultpricec.getPrice("Талисман Гринча"), null, var9, true));
      return var0;
   }
}
