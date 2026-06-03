package rich.screens.clickgui.impl.autobuy.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_124;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1887;
import net.minecraft.class_1893;
import net.minecraft.class_2487;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_5321;
import net.minecraft.class_5455;
import net.minecraft.class_6880;
import net.minecraft.class_7924;
import net.minecraft.class_9279;
import net.minecraft.class_9290;
import net.minecraft.class_9304;
import net.minecraft.class_9334;
import net.minecraft.class_7225.class_7226;
import net.minecraft.class_9304.class_9305;

public class KrushItems {
   public static class_1799 getHelmet() {
      class_1799 var0 = new class_1799(class_1802.field_22027);
      ArrayList var1 = new ArrayList();
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9095, 5));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9111, 5));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9127, 3));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9096, 5));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9101, 1));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9119, 5));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9107, 5));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9105, 1));
      addEnchantments(var0, var1);
      setupItem(var0, createStyledName("Шлем Крушителя"), List.of(class_2561.method_43470("[★] Оригинальный предмет").method_27692(class_124.field_1080)));
      return var0;
   }

   public static class_1799 getChestplate() {
      class_1799 var0 = new class_1799(class_1802.field_22028);
      ArrayList var1 = new ArrayList();
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9111, 5));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9107, 5));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9095, 5));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9096, 5));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9119, 5));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9101, 1));
      addEnchantments(var0, var1);
      setupItem(var0, createStyledName("Нагрудник Крушителя"), List.of(class_2561.method_43470("[★] Оригинальный предмет").method_27692(class_124.field_1080)));
      return var0;
   }

   public static class_1799 getLeggings() {
      class_1799 var0 = new class_1799(class_1802.field_22029);
      ArrayList var1 = new ArrayList();
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9095, 5));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9096, 5));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9111, 5));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9101, 1));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9119, 5));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9107, 5));
      addEnchantments(var0, var1);
      setupItem(var0, createStyledName("Поножи Крушителя"), List.of(class_2561.method_43470("[★] Оригинальный предмет").method_27692(class_124.field_1080)));
      return var0;
   }

   public static class_1799 getBoots() {
      class_1799 var0 = new class_1799(class_1802.field_22030);
      ArrayList var1 = new ArrayList();
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9095, 5));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9111, 5));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_23071, 3));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9129, 4));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9128, 3));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9096, 5));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9101, 1));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9119, 5));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9107, 5));
      addEnchantments(var0, var1);
      setupItem(var0, createStyledName("Ботинки Крушителя"), List.of(class_2561.method_43470("[★] Оригинальный предмет").method_27692(class_124.field_1080)));
      return var0;
   }

   public static class_1799 getSword() {
      class_1799 var0 = new class_1799(class_1802.field_22022);
      ArrayList var1 = new ArrayList();
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9118, 7));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9112, 7));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9124, 2));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9115, 3));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9101, 1));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9110, 5));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9123, 7));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9119, 5));
      addEnchantments(var0, var1);
      setupItem(
         var0,
         createStyledName("Меч Крушителя"),
         List.of(
            class_2561.method_43470("Опытный III").method_27692(class_124.field_1080),
            class_2561.method_43470("Вампиризм II").method_27692(class_124.field_1080),
            class_2561.method_43470("Окисление II").method_27692(class_124.field_1080),
            class_2561.method_43470("Яд III").method_27692(class_124.field_1080),
            class_2561.method_43470("Детекция III").method_27692(class_124.field_1080),
            class_2561.method_43470("[★] Оригинальный предмет").method_27692(class_124.field_1080)
         )
      );
      return var0;
   }

   public static class_1799 getPickaxe() {
      class_1799 var0 = new class_1799(class_1802.field_22024);
      ArrayList var1 = new ArrayList();
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9131, 10));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9101, 1));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9130, 5));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9119, 5));
      addEnchantments(var0, var1);
      setupItem(
         var0,
         createStyledName("Кирка Крушителя"),
         List.of(
            class_2561.method_43470("Бульдозер II").method_27692(class_124.field_1080),
            class_2561.method_43470("Опытный III").method_27692(class_124.field_1080),
            class_2561.method_43470("Магнит").method_27692(class_124.field_1080),
            class_2561.method_43470("Авто-Плавка").method_27692(class_124.field_1080),
            class_2561.method_43470("Паутина").method_27692(class_124.field_1080),
            class_2561.method_43470("Пингер").method_27692(class_124.field_1080),
            class_2561.method_43470("[★] Оригинальный предмет").method_27692(class_124.field_1080)
         )
      );
      return var0;
   }

   public static class_1799 getCrossbow() {
      class_1799 var0 = new class_1799(class_1802.field_8399);
      ArrayList var1 = new ArrayList();
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9108, 1));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9101, 1));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9132, 5));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9119, 3));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9098, 3));
      addEnchantments(var0, var1);
      setupItem(var0, createStyledName("Арбалет Крушителя"), List.of(class_2561.method_43470("[★] Оригинальный предмет").method_27692(class_124.field_1080)));
      return var0;
   }

   public static class_1799 getBow() {
      class_1799 var0 = new class_1799(class_1802.field_8102);
      ArrayList var1 = new ArrayList();
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9103, 7));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9116, 3));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9126, 1));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9125, 1));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9119, 5));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9101, 1));
      addEnchantments(var0, var1);
      setupItem(
         var0,
         createStyledName("Лук Крушителя"),
         List.of(
            class_2561.method_43470("Снайпер II").method_27692(class_124.field_1080),
            class_2561.method_43470("Подрывник").method_27692(class_124.field_1080),
            class_2561.method_43470("[★] Оригинальный предмет").method_27692(class_124.field_1080)
         )
      );
      return var0;
   }

   public static class_1799 getTrident() {
      class_1799 var0 = new class_1799(class_1802.field_8547);
      ArrayList var1 = new ArrayList();
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9117, 1));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9118, 7));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9124, 2));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9101, 1));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9119, 5));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9120, 3));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9106, 5));
      addEnchantments(var0, var1);
      setupItem(
         var0,
         createStyledName("Трезубец Крушителя"),
         List.of(
            class_2561.method_43470("Скаут III").method_27692(class_124.field_1080),
            class_2561.method_43470("Опытный III").method_27692(class_124.field_1080),
            class_2561.method_43470("Вампиризм II").method_27692(class_124.field_1080),
            class_2561.method_43470("Ступор III").method_27692(class_124.field_1080),
            class_2561.method_43470("Притяжение II").method_27692(class_124.field_1080),
            class_2561.method_43470("Окисление II").method_27692(class_124.field_1080),
            class_2561.method_43470("Возвращение").method_27692(class_124.field_1080),
            class_2561.method_43470("Подрывник").method_27692(class_124.field_1080),
            class_2561.method_43470("Яд III").method_27692(class_124.field_1080),
            class_2561.method_43470("Детекция III").method_27692(class_124.field_1080),
            class_2561.method_43470("[★] Оригинальный предмет").method_27692(class_124.field_1080)
         )
      );
      return var0;
   }

   public static class_1799 getMace() {
      class_1799 var0 = new class_1799(class_1802.field_49814);
      ArrayList var1 = new ArrayList();
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9118, 7));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9123, 7));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9112, 7));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_50157, 5));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_50158, 3));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9115, 3));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9124, 2));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9110, 5));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9119, 5));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9101, 1));
      addEnchantments(var0, var1);
      setupItem(
         var0,
         createStyledName("Булава Крушителя"),
         List.of(
            class_2561.method_43470("Опытный III").method_27692(class_124.field_1080),
            class_2561.method_43470("Вампиризм II").method_27692(class_124.field_1080),
            class_2561.method_43470("Окисление II").method_27692(class_124.field_1080),
            class_2561.method_43470("Яд III").method_27692(class_124.field_1080),
            class_2561.method_43470("Детекция III").method_27692(class_124.field_1080),
            class_2561.method_43470("[★] Оригинальный предмет").method_27692(class_124.field_1080)
         )
      );
      return var0;
   }

   public static class_1799 getElytra() {
      class_1799 var0 = new class_1799(class_1802.field_8833);
      ArrayList var1 = new ArrayList();
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9119, 5));
      var1.add(new KrushItems.EnchantmentData(class_1893.field_9101, 1));
      addEnchantments(var0, var1);
      setupItem(var0, createStyledName("Элитры Крушителя"), List.of(class_2561.method_43470("[★] Оригинальный предмет").method_27692(class_124.field_1080)));
      return var0;
   }

   private static void addEnchantments(class_1799 var0, List<KrushItems.EnchantmentData> var1) {
      class_310 var2 = class_310.method_1551();
      if (var2.field_1687 != null) {
         try {
            class_5455 var3 = var2.field_1687.method_30349();
            class_7226 var4 = var3.method_46762(class_7924.field_41265);
            class_9305 var5 = new class_9305((class_9304)var0.method_58695(class_9334.field_49633, class_9304.field_49385));

            for (KrushItems.EnchantmentData var7 : var1) {
               try {
                  Optional var8 = var4.method_46746(var7.key);
                  if (var8.isPresent()) {
                     var5.method_57550((class_6880)var8.get(), var7.level);
                  }
               } catch (Exception var9) {
               }
            }

            var0.method_57379(class_9334.field_49633, var5.method_57549());
         } catch (Exception var10) {
            var10.printStackTrace();
         }
      }
   }

   private static void setupItem(class_1799 var0, class_2561 var1, List<class_2561> var2) {
      var0.method_57379(class_9334.field_49631, var1);
      class_2487 var3 = new class_2487();
      var3.method_10569("HideFlags", 127);
      var3.method_10556("Unbreakable", true);
      var0.method_57379(class_9334.field_49628, class_9279.method_57456(var3));
      if (!var2.isEmpty()) {
         var0.method_57379(class_9334.field_49632, new class_9290(var2));
      }
   }

   private static class_2561 createStyledName(String var0) {
      return class_2561.method_43470(var0).method_27695(new class_124[]{class_124.field_1067, class_124.field_1079});
   }

   private static class EnchantmentData {
      final class_5321<class_1887> key;
      final int level;

      EnchantmentData(class_5321<class_1887> var1, int var2) {
         this.key = var1;
         this.level = var2;
      }
   }
}
