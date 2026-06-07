package rich.screens.clickgui.impl.autobuy.originalitems;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.text.Text;
import rich.screens.clickgui.impl.autobuy.AutoBuyableItem;
import rich.screens.clickgui.impl.autobuy.items.customitem.CustomItem;
import rich.screens.clickgui.impl.autobuy.items.price.Defaultpricec;

public class PotionProvider {
   public static List<AutoBuyableItem> getPotions() {
      ArrayList var0 = new ArrayList();
      List var1 = List.of(Text.literal("Хлопушка"));
      List var2 = List.of(
         new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 9),
         new StatusEffectInstance(StatusEffects.SPEED, 400, 4),
         new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 9),
         new StatusEffectInstance(StatusEffects.GLOWING, 3600, 0)
      );
      var0.add(
         new CustomItem(
            "[★] Хлопушка",
            null,
            Items.SPLASH_POTION,
            Defaultpricec.getPrice("Хлопушка"),
            new PotionContentsComponent(Optional.empty(), Optional.of(16738740), var2, Optional.empty()),
            var1
         )
      );
      List var3 = List.of(Text.literal("Святая вода"));
      List var4 = List.of(
         new StatusEffectInstance(StatusEffects.REGENERATION, 1200, 2), new StatusEffectInstance(StatusEffects.INVISIBILITY, 12000, 1), new StatusEffectInstance(StatusEffects.INSTANT_HEALTH, 0, 1)
      );
      var0.add(
         new CustomItem(
            "[★] Святая вода",
            null,
            Items.SPLASH_POTION,
            Defaultpricec.getPrice("Святая вода"),
            new PotionContentsComponent(Optional.empty(), Optional.of(16777215), var4, Optional.empty()),
            var3
         )
      );
      List var5 = List.of(Text.literal("Зелье Гнева"));
      List var6 = List.of(new StatusEffectInstance(StatusEffects.STRENGTH, 600, 4), new StatusEffectInstance(StatusEffects.SLOWNESS, 600, 3));
      var0.add(
         new CustomItem(
            "[★] Зелье Гнева",
            null,
            Items.SPLASH_POTION,
            Defaultpricec.getPrice("Зелье Гнева"),
            new PotionContentsComponent(Optional.empty(), Optional.of(10040115), var6, Optional.empty()),
            var5
         )
      );
      List var7 = List.of(Text.literal("Зелье Палладина"));
      List var8 = List.of(
         new StatusEffectInstance(StatusEffects.RESISTANCE, 12000, 0),
         new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 12000, 0),
         new StatusEffectInstance(StatusEffects.INVISIBILITY, 18000, 0),
         new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 1200, 2)
      );
      var0.add(
         new CustomItem(
            "[★] Зелье Палладина",
            null,
            Items.SPLASH_POTION,
            Defaultpricec.getPrice("Зелье Палладина"),
            new PotionContentsComponent(Optional.empty(), Optional.of(65535), var8, Optional.empty()),
            var7
         )
      );
      List var9 = List.of(Text.literal("Зелье Ассасина"));
      List var10 = List.of(
         new StatusEffectInstance(StatusEffects.STRENGTH, 1200, 3),
         new StatusEffectInstance(StatusEffects.SPEED, 6000, 2),
         new StatusEffectInstance(StatusEffects.HASTE, 1200, 0),
         new StatusEffectInstance(StatusEffects.INSTANT_DAMAGE, 1, 1)
      );
      var0.add(
         new CustomItem(
            "[★] Зелье Ассасина",
            null,
            Items.SPLASH_POTION,
            Defaultpricec.getPrice("Зелье Ассасина"),
            new PotionContentsComponent(Optional.empty(), Optional.of(3355443), var10, Optional.empty()),
            var9
         )
      );
      List var11 = List.of(Text.literal("Зелье Радиации"));
      List var12 = List.of(
         new StatusEffectInstance(StatusEffects.POISON, 1200, 1),
         new StatusEffectInstance(StatusEffects.WITHER, 1200, 1),
         new StatusEffectInstance(StatusEffects.SLOWNESS, 1800, 2),
         new StatusEffectInstance(StatusEffects.HUNGER, 1200, 4),
         new StatusEffectInstance(StatusEffects.GLOWING, 2400, 0)
      );
      var0.add(
         new CustomItem(
            "[★] Зелье Радиации",
            null,
            Items.SPLASH_POTION,
            Defaultpricec.getPrice("Зелье Радиации"),
            new PotionContentsComponent(Optional.empty(), Optional.of(3329330), var12, Optional.empty()),
            var11
         )
      );
      List var13 = List.of(Text.literal("Снотворное"));
      List var14 = List.of(
         new StatusEffectInstance(StatusEffects.WEAKNESS, 1800, 1),
         new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 200, 1),
         new StatusEffectInstance(StatusEffects.WITHER, 1800, 2),
         new StatusEffectInstance(StatusEffects.BLINDNESS, 200, 0)
      );
      var0.add(
         new CustomItem(
            "[★] Снотворное",
            null,
            Items.SPLASH_POTION,
            Defaultpricec.getPrice("Снотворное"),
            new PotionContentsComponent(Optional.empty(), Optional.of(4737096), var14, Optional.empty()),
            var13
         )
      );
      List var15 = List.of(Text.literal("Заряд витаминов и удачи"));
      List var16 = List.of(
         new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 3600, 0),
         new StatusEffectInstance(StatusEffects.JUMP_BOOST, 3600, 1),
         new StatusEffectInstance(StatusEffects.LUCK, 3600, 0),
         new StatusEffectInstance(StatusEffects.HASTE, 3600, 1)
      );
      var0.add(
         new CustomItem(
            "[\ud83c\udf79] Мандариновый сок",
            null,
            Items.POTION,
            Defaultpricec.getPrice("Мандариновый сок"),
            new PotionContentsComponent(Optional.empty(), Optional.of(14077507), var16, Optional.empty()),
            var15
         )
      );
      return var0;
   }
}
