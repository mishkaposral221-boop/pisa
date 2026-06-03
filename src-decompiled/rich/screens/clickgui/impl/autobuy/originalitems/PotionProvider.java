package rich.screens.clickgui.impl.autobuy.originalitems;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_2561;
import rich.screens.clickgui.impl.autobuy.AutoBuyableItem;
import rich.screens.clickgui.impl.autobuy.items.customitem.CustomItem;
import rich.screens.clickgui.impl.autobuy.items.price.Defaultpricec;

public class PotionProvider {
   public static List<AutoBuyableItem> getPotions() {
      ArrayList var0 = new ArrayList();
      List var1 = List.of(class_2561.method_43470("Хлопушка"));
      List var2 = List.of(
         new class_1293(class_1294.field_5909, 200, 9),
         new class_1293(class_1294.field_5904, 400, 4),
         new class_1293(class_1294.field_5919, 100, 9),
         new class_1293(class_1294.field_5912, 3600, 0)
      );
      var0.add(
         new CustomItem(
            "[★] Хлопушка",
            null,
            class_1802.field_8436,
            Defaultpricec.getPrice("Хлопушка"),
            new class_1844(Optional.empty(), Optional.of(16738740), var2, Optional.empty()),
            var1
         )
      );
      List var3 = List.of(class_2561.method_43470("Святая вода"));
      List var4 = List.of(
         new class_1293(class_1294.field_5924, 1200, 2), new class_1293(class_1294.field_5905, 12000, 1), new class_1293(class_1294.field_5915, 0, 1)
      );
      var0.add(
         new CustomItem(
            "[★] Святая вода",
            null,
            class_1802.field_8436,
            Defaultpricec.getPrice("Святая вода"),
            new class_1844(Optional.empty(), Optional.of(16777215), var4, Optional.empty()),
            var3
         )
      );
      List var5 = List.of(class_2561.method_43470("Зелье Гнева"));
      List var6 = List.of(new class_1293(class_1294.field_5910, 600, 4), new class_1293(class_1294.field_5909, 600, 3));
      var0.add(
         new CustomItem(
            "[★] Зелье Гнева",
            null,
            class_1802.field_8436,
            Defaultpricec.getPrice("Зелье Гнева"),
            new class_1844(Optional.empty(), Optional.of(10040115), var6, Optional.empty()),
            var5
         )
      );
      List var7 = List.of(class_2561.method_43470("Зелье Палладина"));
      List var8 = List.of(
         new class_1293(class_1294.field_5907, 12000, 0),
         new class_1293(class_1294.field_5918, 12000, 0),
         new class_1293(class_1294.field_5905, 18000, 0),
         new class_1293(class_1294.field_5914, 1200, 2)
      );
      var0.add(
         new CustomItem(
            "[★] Зелье Палладина",
            null,
            class_1802.field_8436,
            Defaultpricec.getPrice("Зелье Палладина"),
            new class_1844(Optional.empty(), Optional.of(65535), var8, Optional.empty()),
            var7
         )
      );
      List var9 = List.of(class_2561.method_43470("Зелье Ассасина"));
      List var10 = List.of(
         new class_1293(class_1294.field_5910, 1200, 3),
         new class_1293(class_1294.field_5904, 6000, 2),
         new class_1293(class_1294.field_5917, 1200, 0),
         new class_1293(class_1294.field_5921, 1, 1)
      );
      var0.add(
         new CustomItem(
            "[★] Зелье Ассасина",
            null,
            class_1802.field_8436,
            Defaultpricec.getPrice("Зелье Ассасина"),
            new class_1844(Optional.empty(), Optional.of(3355443), var10, Optional.empty()),
            var9
         )
      );
      List var11 = List.of(class_2561.method_43470("Зелье Радиации"));
      List var12 = List.of(
         new class_1293(class_1294.field_5899, 1200, 1),
         new class_1293(class_1294.field_5920, 1200, 1),
         new class_1293(class_1294.field_5909, 1800, 2),
         new class_1293(class_1294.field_5903, 1200, 4),
         new class_1293(class_1294.field_5912, 2400, 0)
      );
      var0.add(
         new CustomItem(
            "[★] Зелье Радиации",
            null,
            class_1802.field_8436,
            Defaultpricec.getPrice("Зелье Радиации"),
            new class_1844(Optional.empty(), Optional.of(3329330), var12, Optional.empty()),
            var11
         )
      );
      List var13 = List.of(class_2561.method_43470("Снотворное"));
      List var14 = List.of(
         new class_1293(class_1294.field_5911, 1800, 1),
         new class_1293(class_1294.field_5901, 200, 1),
         new class_1293(class_1294.field_5920, 1800, 2),
         new class_1293(class_1294.field_5919, 200, 0)
      );
      var0.add(
         new CustomItem(
            "[★] Снотворное",
            null,
            class_1802.field_8436,
            Defaultpricec.getPrice("Снотворное"),
            new class_1844(Optional.empty(), Optional.of(4737096), var14, Optional.empty()),
            var13
         )
      );
      List var15 = List.of(class_2561.method_43470("Заряд витаминов и удачи"));
      List var16 = List.of(
         new class_1293(class_1294.field_5918, 3600, 0),
         new class_1293(class_1294.field_5913, 3600, 1),
         new class_1293(class_1294.field_5926, 3600, 0),
         new class_1293(class_1294.field_5917, 3600, 1)
      );
      var0.add(
         new CustomItem(
            "[\ud83c\udf79] Мандариновый сок",
            null,
            class_1802.field_8574,
            Defaultpricec.getPrice("Мандариновый сок"),
            new class_1844(Optional.empty(), Optional.of(14077507), var16, Optional.empty()),
            var15
         )
      );
      return var0;
   }
}
