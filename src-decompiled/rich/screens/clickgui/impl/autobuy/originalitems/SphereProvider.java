package rich.screens.clickgui.impl.autobuy.originalitems;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.class_1802;
import net.minecraft.class_2487;
import net.minecraft.class_2495;
import net.minecraft.class_2499;
import net.minecraft.class_2561;
import rich.screens.clickgui.impl.autobuy.AutoBuyableItem;
import rich.screens.clickgui.impl.autobuy.items.customitem.CustomItem;
import rich.screens.clickgui.impl.autobuy.items.price.Defaultpricec;

public class SphereProvider {
   public static List<AutoBuyableItem> getSpheres() {
      ArrayList var0 = new ArrayList();
      List var1 = List.of(
         class_2561.method_43470("Хаос искажает реальность,"), class_2561.method_43470("Усиливая ваш натиск,"), class_2561.method_43470("Ценой жизненных сил.")
      );
      var0.add(
         createSphere(
            "[★] Сфера Хаоса",
            "0000000b-0000-000b-0000-000b0000000b",
            "ewogICJ0aW1lc3RhbXAiIDogMTc1MDI3ODY0MTkwMCwKICAicHJvZmlsZUlkIiA6ICIxNzRjZmRiNGEzY2I0M2I1YmZjZGU0MjRjM2JiMmM2ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJtYXJhZWwxOCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lN2E3YWU3Y2RjZjYxNmU4YjdhNDIyMWE2MjFiMjQzNTc1M2M2MGVkNmEyNThlYTA2MGRhZTMwMDJmZmU5ZTI4IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
            Defaultpricec.getPrice("Сфера Хаоса"),
            var1
         )
      );
      List var2 = List.of(
         class_2561.method_43470("Шёпот Сатира звучит,"), class_2561.method_43470("Ускоряя расправу,"), class_2561.method_43470("Но сковывая прыжок.")
      );
      var0.add(
         createSphere(
            "[★] Сфера Сатира",
            "0000000b-0000-000b-0000-000b0000000b",
            "ewogICJ0aW1lc3RhbXAiIDogMTc1MDI3ODYwODUyOCwKICAicHJvZmlsZUlkIiA6ICJkMTQ4NjFiM2UwZmM0Njk5OTFlMTcyNTllMzdiZjZhZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJyYXhpdG9jbCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS83NzFhOWE0OThiNGZhNWVjNDkzNjJmOWJjODhlZGE0ZjUyYjA0ZGU0OWQ3NWFhM2NhMzMyYTFmZWExYWEwZTU3IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
            Defaultpricec.getPrice("Сфера Сатира"),
            var2
         )
      );
      List var3 = List.of(
         class_2561.method_43470("Звериная дикая мощь"), class_2561.method_43470("Обостряет реакции,"), class_2561.method_43470("Укрепляя ваше тело.")
      );
      var0.add(
         createSphere(
            "[★] Сфера Бестии",
            "0000000b-0000-000b-0000-000b0000000b",
            "ewogICJ0aW1lc3RhbXAiIDogMTc1MDM0MzgzNDkzMCwKICAicHJvZmlsZUlkIiA6ICI1MzUzNWIxN2M0ZDY0NWQ0YWUwY2U2ZjM4Zjk0NTFjYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJVYml2aXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTQxMWFjMTczODFiOWZjZTliYWIzYzcyYWZkYjdmMTk4NTcwZGFmNDczMmJkODExZDMxYzIyN2Q4MGZhMzliMSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
            Defaultpricec.getPrice("Сфера Бестии"),
            var3
         )
      );
      List var4 = List.of(
         class_2561.method_43470("Дух Ареса пылает внутри,"), class_2561.method_43470("Даруя мощь в атаке,"), class_2561.method_43470("Но требуя жертв.")
      );
      var0.add(
         createSphere(
            "[★] Сфера Ареса",
            "0000000b-0000-000b-0000-000b0000000b",
            "ewogICJ0aW1lc3RhbXAiIDogMTc1MDM0Mzc3NDI1NSwKICAicHJvZmlsZUlkIiA6ICJhYWMxYjA2OWNkMjE0NWE2ODNlNzQxNzE4MDcxMGU4MiIsCiAgInByb2ZpbGVOYW1lIiA6ICJqdXNhbXUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzE2YWRjNmJhZmNiNTdmZDcwN2RlZTdkZDZhNzM2ZmUxMjY3MTFkNTNhMWZkNmNlNzg5ZGE0MWIzYmUxM2YyYSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
            Defaultpricec.getPrice("Сфера Ареса"),
            var4
         )
      );
      List var5 = List.of(
         class_2561.method_43470("Живучесть темных глубин"), class_2561.method_43470("Оберегает хозяина,"), class_2561.method_43470("Даруя силы в воде.")
      );
      var0.add(
         createSphere(
            "[★] Сфера Гидры",
            "0000000b-0000-000b-0000-000b0000000b",
            "ewogICJ0aW1lc3RhbXAiIDogMTc1MDI3ODUzMjE4MywKICAicHJvZmlsZUlkIiA6ICI1OGZmZWI5NTMxNGQ0ODcwYTQwYjVjYjQyZDRlYTU5OCIsCiAgInByb2ZpbGVOYW1lIiA6ICJTa2luREJuZXQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2UzYzExOGQ2OTZkOTEwZTU0ZGUwMmNhNGQ4MDc1NDNmOWIxOGMwMDhjOTgzOGQyZmY2OTM3NzYyMmZiMWQzMiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
            Defaultpricec.getPrice("Сфера Гидры"),
            var5
         )
      );
      List var6 = List.of(
         class_2561.method_43470("Хранит волю Икара,"), class_2561.method_43470("Превращая риск в силу,"), class_2561.method_43470("А ярость — в удар.")
      );
      var0.add(
         createSphere(
            "[★] Сфера Икара",
            "0000000b-0000-000b-0000-000b0000000b",
            "ewogICJ0aW1lc3RhbXAiIDogMTc1MDI3ODU4MjQ5MSwKICAicHJvZmlsZUlkIiA6ICJhZWNkODIxZTQyYzE0ZDJlOThmNTA1OTg1MWI5OWMzNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJSb2RyaVgyMDc1IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2M2ODAzZTZkNTY2N2EyZDYxMDYyOGJjM2IzMmY4NjNjZGE0OTVjNDY1NjE2ZGU2NTVjYjMyOTkzM2I2MWFmNzciLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
            Defaultpricec.getPrice("Сфера Икара"),
            var6
         )
      );
      List var7 = List.of(
         class_2561.method_43470("Холод Эриды вечен,"), class_2561.method_43470("Приносит удачу в бою,"), class_2561.method_43470("Укрепляя дух и тело.")
      );
      var0.add(
         createSphere(
            "[★] Сфера Эрида",
            "0000000b-0000-000b-0000-000b0000000b",
            "ewogICJ0aW1lc3RhbXAiIDogMTc1MDM0Mzg2MTE4NywKICAicHJvZmlsZUlkIiA6ICJlZGUyYzdhMGFjNjM0MTNiYjA5ZDNmMGJlZTllYzhlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ0aGVEZXZKYWRlIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzZlNGUyZjEwNDdmM2VjNmU5ZTQ1OTE4NDczOWUzM2I3YzFmYzYzYWQ4MjAyYmRhYjlmMDI0NTA4YWRkMjNlNWIiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
            Defaultpricec.getPrice("Сфера Эрида"),
            var7
         )
      );
      List var8 = List.of(
         class_2561.method_43470("Мощь Титанов крепка,"), class_2561.method_43470("Дарует стойкость стали,"), class_2561.method_43470("Но тяжелит шаг.")
      );
      var0.add(
         createSphere(
            "[★] Сфера Титана",
            "0000000b-0000-000b-0000-000b0000000b",
            "ewogICJ0aW1lc3RhbXAiIDogMTc1MDM1NDQ1NTE5MiwKICAicHJvZmlsZUlkIiA6ICJkOTcwYzEzZTM4YWI0NzlhOTY1OGM1ZDQ1MjZkMTM0YiIsCiAgInByb2ZpbGVOYW1lIiA6ICJDcmltcHlMYWNlODUxMjciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODFlOTY5ODQ1OGI3ODQxYzk2YWU0ZjI0ZWM4NGFlMDE3MjQxMDA2NDFjNTY0ZTJhN2IxODVmNDA2ZThlZDIzIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
            Defaultpricec.getPrice("Сфера Титана"),
            var8
         )
      );
      List var9 = List.of(
         class_2561.method_43470("Вечная мерзлота сковывает,"), class_2561.method_43470("Даруя твердость льда,"), class_2561.method_43470("Но лишая гибкости")
      );
      var0.add(
         createSphere(
            "[❄] Сфера Мороза",
            "0000000b-0000-000b-0000-000b0000000b",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjNmZDM4MTQxMDhkZDA0MmM4NzU1NWYwMjNkNTcwY2UyNmI4M2MwZTM1YjIxYTdiMTI4MWE3ZTA1NDVjZjllMCJ9fX0=",
            Defaultpricec.getPrice("Сфера Мороза"),
            var9
         )
      );
      return var0;
   }

   private static AutoBuyableItem createSphere(String var0, String var1, String var2, int var3, List<class_2561> var4) {
      class_2487 var5 = new class_2487();
      var5.method_10556("HideFlags", true);
      var5.method_10556("Unbreakable", true);
      class_2487 var6 = new class_2487();
      UUID var7 = UUID.fromString(var1);
      int[] var8 = uuidToIntArray(var7);
      var6.method_10566("Id", new class_2495(var8));
      class_2487 var9 = new class_2487();
      class_2499 var10 = new class_2499();
      class_2487 var11 = new class_2487();
      var11.method_10582("Value", var2);
      var10.add(var11);
      var9.method_10566("textures", var10);
      var6.method_10566("Properties", var9);
      var5.method_10566("SkullOwner", var6);
      return new CustomItem(var0, var5, class_1802.field_8575, var3, null, var4);
   }

   private static int[] uuidToIntArray(UUID var0) {
      long var1 = var0.getMostSignificantBits();
      long var3 = var0.getLeastSignificantBits();
      return new int[]{(int)(var1 >> 32), (int)var1, (int)(var3 >> 32), (int)var3};
   }
}
