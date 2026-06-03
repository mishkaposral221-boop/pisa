package rich.screens.clickgui.impl.autobuy.originalitems;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1802;
import net.minecraft.class_2561;
import rich.screens.clickgui.impl.autobuy.AutoBuyableItem;
import rich.screens.clickgui.impl.autobuy.items.customitem.CustomItem;
import rich.screens.clickgui.impl.autobuy.items.customitem.UnbreakableItem;
import rich.screens.clickgui.impl.autobuy.items.price.Defaultpricec;

public class DonatorProvider {
   public static List<AutoBuyableItem> getDonator() {
      ArrayList var0 = new ArrayList();
      List var1 = List.of(
         class_2561.method_43470("Каст: Световая вспышка"),
         class_2561.method_43470("Радиус: 10 блоков"),
         class_2561.method_43470("Эффекты для противников:"),
         class_2561.method_43470(" • Свечение (00:30)"),
         class_2561.method_43470(" • Слепота (00:01)"),
         class_2561.method_43470("Чем ближе цель, тем дольше длительность эффектов")
      );
      var0.add(new CustomItem("[★] Явная пыль", null, class_1802.field_8479, Defaultpricec.getPrice("Явная пыль"), null, var1, false, true));
      List var2 = List.of(class_2561.method_43470("Чем ближе цель, тем дольше длительность эффектов"));
      var0.add(new CustomItem("[★] Дезориентация", null, class_1802.field_8449, Defaultpricec.getPrice("Дезориентация"), null, var2, false, true));
      List var3 = List.of(
         class_2561.method_43470("Каст: Нерушимая клетка"),
         class_2561.method_43470("Длительность: 15 секунд"),
         class_2561.method_43470("Используйте скины: /tskins")
      );
      var0.add(new CustomItem("[★] Трапка", null, class_1802.field_22021, Defaultpricec.getPrice("Трапка"), null, var3, false, true));
      List var4 = List.of(class_2561.method_43470("Этой отмычкой можно"), class_2561.method_43470("Открыть хранилище"), class_2561.method_43470("С Сферами"));
      var0.add(new CustomItem("[★] Отмычка к Сферам", null, class_1802.field_8366, Defaultpricec.getPrice("Отмычка к Сферам"), null, var4, false, true));
      List var5 = List.of(
         class_2561.method_43470("Каст: Нерушимая стена"),
         class_2561.method_43470("Длительность:"),
         class_2561.method_43470("Вертикальный: 20 секунд"),
         class_2561.method_43470("Горизонтальный: 60 секунд")
      );
      var0.add(new CustomItem("[★] Пласт", null, class_1802.field_8551, Defaultpricec.getPrice("Пласт"), null, var5, false, true));
      List var6 = List.of(class_2561.method_43470("Содержит 15 ур опыта"));
      var0.add(new CustomItem("Пузырек опыта [15 ур]", null, class_1802.field_8287, Defaultpricec.getPrice("Пузырек опыта [15 ур]"), null, var6, false, true));
      List var7 = List.of(class_2561.method_43470("Содержит: 30 Ур. опыта"));
      var0.add(new CustomItem("Пузырёк опыта [30 Ур.]", null, class_1802.field_8287, Defaultpricec.getPrice("Пузырёк опыта [30 Ур.]"), null, var7, false, true));
      List var8 = List.of(class_2561.method_43470("Содержит 50 ур опыта"));
      var0.add(new CustomItem("Пузырек опыта [50 ур]", null, class_1802.field_8287, Defaultpricec.getPrice("Пузырек опыта [50 ур]"), null, var8, false, true));
      List var9 = List.of(class_2561.method_43470("Этот динамит взрывается"), class_2561.method_43470("в 10 раз сильнее обычного"));
      var0.add(new CustomItem("[★] TNT - TIER WHITE", null, class_1802.field_8626, Defaultpricec.getPrice("TNT - TIER WHITE"), null, var9, false, true));
      List var10 = List.of(
         class_2561.method_43470("Этот динамит взрывается"),
         class_2561.method_43470("в 10 раз сильнее обычного"),
         class_2561.method_43470("и способен взорвать обсидиан")
      );
      var0.add(new CustomItem("[★] TNT - TIER BLACK", null, class_1802.field_8626, Defaultpricec.getPrice("TNT - TIER BLACK"), null, var10, false, true));
      List var11 = List.of(
         class_2561.method_43470("Уровень лута: Случайный"),
         class_2561.method_43470("При помощи этого предмета"),
         class_2561.method_43470("можно призвать оригинальный"),
         class_2561.method_43470("Мистический Сундук!")
      );
      var0.add(
         new CustomItem("Сигнальный огонь [Случайный]", null, class_1802.field_17346, Defaultpricec.getPrice("Сигнальный огонь [Случайный]"), null, var11)
      );
      List var12 = List.of(
         class_2561.method_43470("Уровень лута: Обычный"),
         class_2561.method_43470("При помощи этого предмета"),
         class_2561.method_43470("можно призвать оригинальный"),
         class_2561.method_43470("Мистический Сундук!")
      );
      var0.add(new CustomItem("Сигнальный огонь [Обычный]", null, class_1802.field_17346, Defaultpricec.getPrice("Сигнальный огонь [Обычный]"), null, var12));
      List var13 = List.of(
         class_2561.method_43470("Уровень лута: Богатый"),
         class_2561.method_43470("При помощи этого предмета"),
         class_2561.method_43470("можно призвать оригинальный"),
         class_2561.method_43470("Мистический Сундук!")
      );
      var0.add(new CustomItem("Сигнальный огонь [Богатый]", null, class_1802.field_17346, Defaultpricec.getPrice("Сигнальный огонь [Богатый]"), null, var13));
      List var14 = List.of(
         class_2561.method_43470("Уровень лута: Легендарный"),
         class_2561.method_43470("При помощи этого предмета"),
         class_2561.method_43470("можно призвать оригинальный"),
         class_2561.method_43470("Мистический Сундук!")
      );
      var0.add(
         new CustomItem("Сигнальный огонь [Легендарный]", null, class_1802.field_23842, Defaultpricec.getPrice("Сигнальный огонь [Легендарный]"), null, var14)
      );
      List var15 = List.of(class_2561.method_43470("● Каст: Нанесение урона"), class_2561.method_43470("● Радиус: 1,5 блока"));
      var0.add(new CustomItem("[★] Блок дамагер", null, class_1802.field_16538, Defaultpricec.getPrice("Блок дамагер"), null, var15));
      List var16 = List.of(
         class_2561.method_43470("Прогружает чанк, в котором"),
         class_2561.method_43470("находится этот прогрузчик."),
         class_2561.method_43470("Нажмите на него, чтобы на"),
         class_2561.method_43470("30 секунд увидеть границы"),
         class_2561.method_43470("прогружаемой области (1x1).")
      );
      var0.add(new CustomItem("[★] Прогрузчик чанков [1x1]", null, class_1802.field_8238, Defaultpricec.getPrice("Прогрузчик чанков [1x1]"), null, var16));
      List var17 = List.of(
         class_2561.method_43470("Прогружает чанк, в котором"),
         class_2561.method_43470("находится этот прогрузчик."),
         class_2561.method_43470("Нажмите на него, чтобы на"),
         class_2561.method_43470("30 секунд увидеть границы"),
         class_2561.method_43470("прогружаемой области (3x3).")
      );
      var0.add(new CustomItem("[★] Прогрузчик чанков [3x3]", null, class_1802.field_8238, Defaultpricec.getPrice("Прогрузчик чанков [3x3]"), null, var17));
      List var18 = List.of(
         class_2561.method_43470("Прогружает чанк, в котором"),
         class_2561.method_43470("находится этот прогрузчик."),
         class_2561.method_43470("Нажмите на него, чтобы на"),
         class_2561.method_43470("30 секунд увидеть границы"),
         class_2561.method_43470("прогружаемой области (5x5).")
      );
      var0.add(new CustomItem("[★] Прогрузчик чанков [5x5]", null, class_1802.field_8238, Defaultpricec.getPrice("Прогрузчик чанков [5x5]"), null, var18));
      List var19 = List.of(
         class_2561.method_43470("Маяк установит временный"),
         class_2561.method_43470("ивент, раздающий Монеты"),
         class_2561.method_43470("игрокам поблизости.")
      );
      var0.add(new CustomItem("Загадочный маяк", null, class_1802.field_8668, Defaultpricec.getPrice("Загадочный маяк"), null, var19));
      List var20 = List.of(
         class_2561.method_43470("Обменяй души на ценные"), class_2561.method_43470("ресурсы у Собирателя душ"), class_2561.method_43470("/warp soulcollector")
      );
      var0.add(new CustomItem("[★] Проклятая душа", null, class_1802.field_22016, Defaultpricec.getPrice("Проклятая душа"), null, var20, false, true));
      List var21 = List.of(
         class_2561.method_43470("Используя этот предмет"),
         class_2561.method_43470("Вы его расходуете"),
         class_2561.method_43470("и получаете Драконий скин взамен"),
         class_2561.method_43470("[ПКМ], чтобы использовать x1 скин"),
         class_2561.method_43470("[SHIFT+ПКМ], чтобы использовать все скины"),
         class_2561.method_43470("Предмет нужно держать в руке")
      );
      var0.add(new CustomItem("[★] Драконий скин", null, class_1802.field_8407, Defaultpricec.getPrice("Драконий скин"), null, var21, false, true));
      List var22 = List.of(
         class_2561.method_43470("● Каст: Огненная волна"),
         class_2561.method_43470("● Радиус: 10 блоков"),
         class_2561.method_43470(""),
         class_2561.method_43470("● Эффекты для противников:"),
         class_2561.method_43470(" - Поджог (00:03)"),
         class_2561.method_43470(""),
         class_2561.method_43470("Чем ближе цель, тем дольше"),
         class_2561.method_43470("длительность эффектов")
      );
      var0.add(new CustomItem("[★] Огненный смерч", null, class_1802.field_8814, Defaultpricec.getPrice("Огненный смерч"), null, var22, false, true));
      List var23 = List.of(
         class_2561.method_43470("● Каст: Ледяная сфера"),
         class_2561.method_43470("● Радиус: 7 блоков"),
         class_2561.method_43470(""),
         class_2561.method_43470("● Эффекты для противников:"),
         class_2561.method_43470(" - Заморозка (00:01)"),
         class_2561.method_43470(" - Слабость (03:00)")
      );
      var0.add(new CustomItem("[★] Снежок заморозка", null, class_1802.field_8543, Defaultpricec.getPrice("Снежок заморозка"), null, var23, false, true));
      List var24 = List.of(
         class_2561.method_43470("● Каст: Божественная аура"),
         class_2561.method_43470("● Радиус: 2 блока"),
         class_2561.method_43470(""),
         class_2561.method_43470("● Эффекты для союзников:"),
         class_2561.method_43470(" - Снятие всех эффектов"),
         class_2561.method_43470(" - Невидимость (04:00)"),
         class_2561.method_43470(" - Сила II (03:00)"),
         class_2561.method_43470(" - Скорость II (03:00)")
      );
      var0.add(new CustomItem("[★] Божья аура", null, class_1802.field_8614, Defaultpricec.getPrice("Божья аура"), null, var24, false, true));
      List var25 = List.of(
         class_2561.method_43470("Это валюта для покупки"), class_2561.method_43470("отмычек к тайникам"), class_2561.method_43470("у Знахаря (/warp stash)")
      );
      var0.add(new CustomItem("[★] Серебро", null, class_1802.field_8675, Defaultpricec.getPrice("Серебро"), null, var25, false, true));
      List var26 = List.of(
         class_2561.method_43470("Божье касание"), class_2561.method_43470("Может добыть спавнер,"), class_2561.method_43470("но только один раз")
      );
      var0.add(new CustomItem("[★] Божье касание", null, class_1802.field_8335, Defaultpricec.getPrice("Божье касание"), null, var26));
      List var27 = List.of(
         class_2561.method_43470("Мощный удар"), class_2561.method_43470("Может разрушить бедрок,"), class_2561.method_43470("но только один раз")
      );
      var0.add(new CustomItem("[★] Мощный удар", null, class_1802.field_8335, Defaultpricec.getPrice("Мощный удар"), null, var27));
      List var28 = List.of(class_2561.method_43470("Вскапывает территорию"), class_2561.method_43470("размером 9x9x5 блоков"));
      var0.add(new CustomItem("[★] Кирка мега-бульдозер", null, class_1802.field_22024, Defaultpricec.getPrice("Кирка мега-бульдозер"), null, var28));
      List var29 = List.of(class_2561.method_43470("[⚒] Нерушимый предмет"));
      var0.add(new UnbreakableItem("[⚒] Нерушимые элитры", class_1802.field_8833, Defaultpricec.getPrice("Нерушимые элитры"), var29));
      return var0;
   }
}
