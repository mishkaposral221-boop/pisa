package rich.screens.clickgui.impl.autobuy.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1802;
import rich.screens.clickgui.impl.autobuy.AutoBuyableItem;
import rich.screens.clickgui.impl.autobuy.items.price.Defaultpricec;

public class KrushProvider {
   public static List<AutoBuyableItem> getKrush() {
      ArrayList var0 = new ArrayList();
      var0.add(new KrushItem("Шлем Крушителя", class_1802.field_22027, KrushItems.getHelmet(), Defaultpricec.getPrice("Шлем крушителя"), false, true));
      var0.add(
         new KrushItem("Нагрудник Крушителя", class_1802.field_22028, KrushItems.getChestplate(), Defaultpricec.getPrice("Нагрудник крушителя"), false, true)
      );
      var0.add(new KrushItem("Поножи Крушителя", class_1802.field_22029, KrushItems.getLeggings(), Defaultpricec.getPrice("Поножи крушителя"), false, true));
      var0.add(new KrushItem("Ботинки Крушителя", class_1802.field_22030, KrushItems.getBoots(), Defaultpricec.getPrice("Ботинки крушителя"), false, true));
      var0.add(new KrushItem("Меч Крушителя", class_1802.field_22022, KrushItems.getSword(), Defaultpricec.getPrice("Меч крушителя"), false, true));
      var0.add(new KrushItem("Кирка Крушителя", class_1802.field_22024, KrushItems.getPickaxe(), Defaultpricec.getPrice("Кирка крушителя"), false, true));
      var0.add(new KrushItem("Арбалет Крушителя", class_1802.field_8399, KrushItems.getCrossbow(), Defaultpricec.getPrice("Арбалет крушителя"), false, true));
      var0.add(new KrushItem("Лук Крушителя", class_1802.field_8102, KrushItems.getBow(), Defaultpricec.getPrice("Лук крушителя"), false, true));
      var0.add(new KrushItem("Трезубец Крушителя", class_1802.field_8547, KrushItems.getTrident(), Defaultpricec.getPrice("Трезубец крушителя"), false, true));
      var0.add(new KrushItem("Булава Крушителя", class_1802.field_49814, KrushItems.getMace(), Defaultpricec.getPrice("Булава крушителя"), false, true));
      var0.add(new KrushItem("Элитры Крушителя", class_1802.field_8833, KrushItems.getElytra(), Defaultpricec.getPrice("Элитры крушителя"), false, true));
      return var0;
   }
}
