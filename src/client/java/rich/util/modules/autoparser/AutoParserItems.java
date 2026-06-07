package rich.util.modules.autoparser;

import java.util.ArrayList;
import java.util.List;

public class AutoParserItems {
   private static final List<AutoParserItems.ParserItemEntry> items = new ArrayList<>();

   public static List<AutoParserItems.ParserItemEntry> getItems() {
      return new ArrayList<>(items);
   }

   static {
      items.add(new AutoParserItems.ParserItemEntry("Шлем крушителя", "Шлем Крушителя"));
      items.add(new AutoParserItems.ParserItemEntry("Нагрудник крушителя", "Нагрудник Крушителя"));
      items.add(new AutoParserItems.ParserItemEntry("поножи крушителя", "Поножи Крушителя"));
      items.add(new AutoParserItems.ParserItemEntry("ботинки крушителя", "Ботинки Крушителя"));
      items.add(new AutoParserItems.ParserItemEntry("меч крушителя", "Меч Крушителя"));
      items.add(new AutoParserItems.ParserItemEntry("кирка крушителя", "Кирка Крушителя"));
      items.add(new AutoParserItems.ParserItemEntry("арбалет крушителя", "Арбалет Крушителя"));
      items.add(new AutoParserItems.ParserItemEntry("Трезубец крушителя", "Трезубец Крушителя"));
      items.add(new AutoParserItems.ParserItemEntry("булава крушителя", "Булава Крушителя"));
      items.add(new AutoParserItems.ParserItemEntry("талисман крушителя", "[★] Талисман Крушителя"));
      items.add(new AutoParserItems.ParserItemEntry("талисман раздора", "[★] Талисман Раздора"));
      items.add(new AutoParserItems.ParserItemEntry("талисман тирана", "[★] Талисман Тирана"));
      items.add(new AutoParserItems.ParserItemEntry("талисман ярости", "[★] Талисман Ярости"));
      items.add(new AutoParserItems.ParserItemEntry("талисман вихря", "[★] Талисман Вихря"));
      items.add(new AutoParserItems.ParserItemEntry("талисман мрака", "[★] Талисман Мрака"));
      items.add(new AutoParserItems.ParserItemEntry("талисман демона", "[★] Талисман Демона"));
      items.add(new AutoParserItems.ParserItemEntry("талисман карателя", "[★] Талисман Карателя"));
      items.add(new AutoParserItems.ParserItemEntry("талисман гринча", "[★] Талисман Гринча"));
      items.add(new AutoParserItems.ParserItemEntry("сфера хаоса", "[★] Сфера Хаоса"));
      items.add(new AutoParserItems.ParserItemEntry("сфера сатира", "[★] Сфера Сатира"));
      items.add(new AutoParserItems.ParserItemEntry("сфера бестии", "[★] Сфера Бестии"));
      items.add(new AutoParserItems.ParserItemEntry("сфера ареса", "[★] Сфера Ареса"));
      items.add(new AutoParserItems.ParserItemEntry("сфера эрида", "[★] Сфера Эрида"));
      items.add(new AutoParserItems.ParserItemEntry("сфера титана", "[★] Сфера Титана"));
      items.add(new AutoParserItems.ParserItemEntry("сфера Мороза", "[❄] Сфера Мороза"));
      items.add(new AutoParserItems.ParserItemEntry("золотое яблоко", "Золотое яблоко"));
      items.add(new AutoParserItems.ParserItemEntry("яблоко", "Яблоко"));
      items.add(new AutoParserItems.ParserItemEntry("зачарованное золотое яблоко", "Зачарованное золотое яблоко"));
      items.add(new AutoParserItems.ParserItemEntry("порох", "Порох"));
      items.add(new AutoParserItems.ParserItemEntry("бирка", "Бирка"));
      items.add(new AutoParserItems.ParserItemEntry("трезубец", "Трезубец"));
      items.add(new AutoParserItems.ParserItemEntry("незеритовый слиток", "Незеритовый слиток"));
      items.add(new AutoParserItems.ParserItemEntry("незеритовое улучшение", "Незеритовое улучшение"));
      items.add(new AutoParserItems.ParserItemEntry("алмаз", "Алмаз"));
      items.add(new AutoParserItems.ParserItemEntry("алмазный блок", "Алмазный блок"));
      items.add(new AutoParserItems.ParserItemEntry("золотой слиток", "Золотой слиток"));
      items.add(new AutoParserItems.ParserItemEntry("золотой блок", "Блок золота"));
      items.add(new AutoParserItems.ParserItemEntry("маяк", "Маяк", "Загадочный маяк"));
      items.add(new AutoParserItems.ParserItemEntry("пузырёк опыта", "Пузырёк опыта"));
      items.add(new AutoParserItems.ParserItemEntry("звезда незера", "Звезда Незера"));
      items.add(new AutoParserItems.ParserItemEntry("Шалкеровый ящик", "Шалкеровый ящик"));
      items.add(new AutoParserItems.ParserItemEntry("железный слиток", "Железный слиток"));
      items.add(new AutoParserItems.ParserItemEntry("железный блок", "Железный блок"));
      items.add(new AutoParserItems.ParserItemEntry("незеритовый блок", "Незеритовый блок"));
      items.add(new AutoParserItems.ParserItemEntry("спавнер", "Спавнер"));
      items.add(new AutoParserItems.ParserItemEntry("элитры", "Элитры", "Элитры Крушителя", "[⚒] Нерушимые элитры"));
      items.add(new AutoParserItems.ParserItemEntry("эндер жемчуг", "Эндер жемчуг"));
      items.add(new AutoParserItems.ParserItemEntry("обсидиан", "Обсидиан"));
      items.add(new AutoParserItems.ParserItemEntry("тотем бессмертия", "Тотем бессмертия"));
      items.add(new AutoParserItems.ParserItemEntry("палка ифрита", "Палка ифрита"));
      items.add(new AutoParserItems.ParserItemEntry("динамит", "Динамит"));
      items.add(new AutoParserItems.ParserItemEntry("яйцо зомби-крестьянина", "Яйцо зомби-жителя"));
      items.add(new AutoParserItems.ParserItemEntry("голова скелета", "Голова скелета"));
      items.add(new AutoParserItems.ParserItemEntry("голова зомби", "Голова зомби"));
      items.add(new AutoParserItems.ParserItemEntry("голова крипера", "Голова крипера"));
      items.add(new AutoParserItems.ParserItemEntry("голова визер-скелета", "Голова визер-скелета"));
      items.add(new AutoParserItems.ParserItemEntry("голова пиглина", "Голова пиглина"));
      items.add(new AutoParserItems.ParserItemEntry("голова дракона", "Голова дракона"));
      items.add(new AutoParserItems.ParserItemEntry("алмазная руда", "Алмазная руда"));
      items.add(new AutoParserItems.ParserItemEntry("изумрудная руда", "Изумрудная руда"));
      items.add(new AutoParserItems.ParserItemEntry("торт", "Торт"));
      items.add(new AutoParserItems.ParserItemEntry("Фейерверк", "Фейерверк"));
      items.add(new AutoParserItems.ParserItemEntry("яйцо жителя", "Яйцо жителя"));
      items.add(new AutoParserItems.ParserItemEntry("яйцо призыва вихря", "Яйцо вихря"));
      items.add(new AutoParserItems.ParserItemEntry("мешок", "Мешок"));
      items.add(new AutoParserItems.ParserItemEntry("булава", "Булава"));
      items.add(new AutoParserItems.ParserItemEntry("зловещий ключ испытаний", "Зловещий ключ испытаний"));
      items.add(new AutoParserItems.ParserItemEntry("ключ испытаний", "Ключ испытаний"));
      items.add(new AutoParserItems.ParserItemEntry("заряд ветра", "Заряд ветра"));
      items.add(new AutoParserItems.ParserItemEntry("Вихревой стержень", "Стержень вихря"));
      items.add(new AutoParserItems.ParserItemEntry("явная пыль", "[★] Явная пыль"));
      items.add(new AutoParserItems.ParserItemEntry("Дезориентация", "[★] Дезориентация"));
      items.add(new AutoParserItems.ParserItemEntry("Трапка", "[★] Трапка"));
      items.add(new AutoParserItems.ParserItemEntry("отмычка к сферам", "Отмычка к Сферам"));
      items.add(new AutoParserItems.ParserItemEntry("пласт", "[★] Пласт"));
      items.add(new AutoParserItems.ParserItemEntry("Пузырек с уровнем 15", "Пузырек опыта [15 ур]"));
      items.add(new AutoParserItems.ParserItemEntry("Пузырек с уровнем 30", "Пузырёк опыта [30 Ур.]"));
      items.add(new AutoParserItems.ParserItemEntry("опыт с уровнем 50", "Пузырек опыта [50 ур]"));
      items.add(new AutoParserItems.ParserItemEntry("вайт", "[★] TNT - TIER WHITE"));
      items.add(new AutoParserItems.ParserItemEntry("блэк", "[★] TNT - TIER BLACK"));
      items.add(
         new AutoParserItems.ParserItemEntry(
            "сигнальный огонь", "Сигнальный огонь [Случайный]", "Сигнальный огонь [Обычный]", "Сигнальный огонь [Богатый]", "Сигнальный огонь [Легендарный]"
         )
      );
      items.add(new AutoParserItems.ParserItemEntry("обычный мист", "Сигнальный огонь [Обычный]"));
      items.add(new AutoParserItems.ParserItemEntry("Богатый мист", "Сигнальный огонь [Богатый]"));
      items.add(new AutoParserItems.ParserItemEntry("Легендарный мист", "Сигнальный огонь [Легендарный]"));
      items.add(new AutoParserItems.ParserItemEntry("блок дамагер", "[★] Блок дамагер"));
      items.add(new AutoParserItems.ParserItemEntry("Прогрузчик чанков", "Прогрузчик чанков [1x1]", "Прогрузчик чанков [3x3]", "Прогрузчик чанков [5x5]"));
      items.add(new AutoParserItems.ParserItemEntry("проклятая душа", "[★] Проклятая душа"));
      items.add(new AutoParserItems.ParserItemEntry("драконий скин", "[★] Драконий скин"));
      items.add(new AutoParserItems.ParserItemEntry("огненный смерч", "[★] Огненный смерч"));
      items.add(new AutoParserItems.ParserItemEntry("снежок заморозка", "[★] Снежок заморозка"));
      items.add(new AutoParserItems.ParserItemEntry("божья аура", "[★] Божья аура"));
      items.add(new AutoParserItems.ParserItemEntry("серебро", "[★] Серебро"));
      items.add(new AutoParserItems.ParserItemEntry("божье касание", "[★] Божье касание"));
      items.add(new AutoParserItems.ParserItemEntry("мощный удар", "[★] Мощный удар"));
      items.add(new AutoParserItems.ParserItemEntry("кирка мега-бульдозер", "[★] Кирка мега-бульдозер"));
      items.add(new AutoParserItems.ParserItemEntry("хлопушка", "[★] Хлопушка"));
      items.add(new AutoParserItems.ParserItemEntry("святая вода", "[★] Святая вода"));
      items.add(new AutoParserItems.ParserItemEntry("зелье гнева", "[★] Зелье Гнева"));
      items.add(new AutoParserItems.ParserItemEntry("зелье палладина", "[★] Зелье Палладина"));
      items.add(new AutoParserItems.ParserItemEntry("зелье ассасина", "[★] Зелье Ассасина"));
      items.add(new AutoParserItems.ParserItemEntry("зелье радиации", "[★] Зелье Радиации"));
      items.add(new AutoParserItems.ParserItemEntry("снотворное", "[★] Снотворное"));
      items.add(new AutoParserItems.ParserItemEntry("мандариновый сок", "[\ud83c\udf79] Мандариновый сок"));
   }

   public static class ParserItemEntry {
      private final String searchQuery;
      private final String[] autoBuyNames;

      public ParserItemEntry(String var1, String... var2) {
         this.searchQuery = var1;
         this.autoBuyNames = var2;
      }

      public String getSearchQuery() {
         return this.searchQuery;
      }

      public String[] getAutoBuyNames() {
         return this.autoBuyNames;
      }
   }
}
