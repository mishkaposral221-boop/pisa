package rich.screens.clickgui.impl.autobuy.items.customitem;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2561;
import net.minecraft.class_9279;
import net.minecraft.class_9290;
import net.minecraft.class_9296;
import net.minecraft.class_9334;
import rich.screens.clickgui.impl.autobuy.AutoBuyableItem;
import rich.screens.clickgui.impl.autobuy.settings.AutoBuyItemSettings;
import rich.util.config.impl.autobuyconfig.AutoBuyConfig;

public class CustomItem implements AutoBuyableItem {
   private final String displayName;
   private final class_2487 nbt;
   private final class_1792 material;
   private final int price;
   private final class_1844 potionContents;
   private final List<class_2561> loreTexts;
   private final AutoBuyItemSettings settings;
   private final boolean hasGlint;
   private boolean enabled;

   public CustomItem(String var1, class_2487 var2, class_1792 var3, int var4, class_1844 var5, List<class_2561> var6) {
      this(var1, var2, var3, var4, var5, var6, shouldHaveGlint(var3, var1), false);
   }

   public CustomItem(String var1, class_2487 var2, class_1792 var3, int var4, class_1844 var5, List<class_2561> var6, boolean var7) {
      this(var1, var2, var3, var4, var5, var6, var7, false);
   }

   public CustomItem(String var1, class_2487 var2, class_1792 var3, int var4, class_1844 var5, List<class_2561> var6, boolean var7, boolean var8) {
      this.displayName = var1;
      this.nbt = var2;
      this.material = var3;
      this.price = var4;
      this.potionContents = var5;
      this.loreTexts = var6;
      this.hasGlint = var7;
      this.settings = new AutoBuyItemSettings(var4, var3, var1, var8);
      AutoBuyConfig var9 = AutoBuyConfig.getInstance();
      if (var9.hasItemConfig(var1)) {
         this.enabled = var9.isItemEnabled(var1);
      } else {
         this.enabled = true;
         var9.loadItemSettings(var1, var4);
      }
   }

   public CustomItem(String var1, class_2487 var2, class_1792 var3, int var4) {
      this(var1, var2, var3, var4, null, null);
   }

   public CustomItem(String var1, class_2487 var2, class_1792 var3, int var4, boolean var5) {
      this(var1, var2, var3, var4, null, null, shouldHaveGlint(var3, var1), var5);
   }

   public CustomItem(String var1, class_2487 var2, class_1792 var3, int var4, class_1844 var5, List<class_2561> var6, int var7) {
      this(var1, var2, var3, var4, var5, var6, shouldHaveGlint(var3, var1), true);
   }

   private static boolean shouldHaveGlint(class_1792 var0, String var1) {
      if (var0 == class_1802.field_8288 || var0 == class_1802.field_8833) {
         return false;
      } else {
         return var0 != class_1802.field_22027
               && var0 != class_1802.field_22028
               && var0 != class_1802.field_22029
               && var0 != class_1802.field_22030
               && var0 != class_1802.field_22022
               && var0 != class_1802.field_22024
               && var0 != class_1802.field_22025
               && var0 != class_1802.field_22023
               && var0 != class_1802.field_22026
               && var0 != class_1802.field_8805
               && var0 != class_1802.field_8058
               && var0 != class_1802.field_8348
               && var0 != class_1802.field_8285
               && var0 != class_1802.field_8802
               && var0 != class_1802.field_8377
               && var0 != class_1802.field_8556
               && var0 != class_1802.field_8250
               && var0 != class_1802.field_8527
               && var0 != class_1802.field_8743
               && var0 != class_1802.field_8523
               && var0 != class_1802.field_8396
               && var0 != class_1802.field_8660
               && var0 != class_1802.field_8371
               && var0 != class_1802.field_8403
               && var0 != class_1802.field_8475
               && var0 != class_1802.field_8699
               && var0 != class_1802.field_8609
               && var0 != class_1802.field_8862
               && var0 != class_1802.field_8678
               && var0 != class_1802.field_8416
               && var0 != class_1802.field_8753
               && var0 != class_1802.field_8845
               && var0 != class_1802.field_8335
               && var0 != class_1802.field_8825
               && var0 != class_1802.field_8322
               && var0 != class_1802.field_8303
               && var0 != class_1802.field_8102
               && var0 != class_1802.field_8399
               && var0 != class_1802.field_8547
               && var0 != class_1802.field_49814
               && var0 != class_1802.field_8255
               && var0 != class_1802.field_8378
            ? var1 != null && var1.contains("[★]") && (var0 == class_1802.field_8574 || var0 == class_1802.field_8436 || var0 == class_1802.field_8150)
            : true;
      }
   }

   @Override
   public String getDisplayName() {
      return this.displayName;
   }

   @Override
   public class_1799 createItemStack() {
      class_1799 var1 = new class_1799(this.material);
      var1.method_57379(class_9334.field_49631, class_2561.method_43470(this.displayName));
      if (this.isPotion(this.material)) {
         if (this.potionContents != null) {
            var1.method_57379(class_9334.field_49651, this.potionContents);
         } else {
            int var2 = this.getPotionColorByName(this.displayName);
            var1.method_57379(class_9334.field_49651, new class_1844(Optional.empty(), Optional.of(var2), List.of(), Optional.empty()));
         }
      }

      if (this.loreTexts != null && !this.loreTexts.isEmpty()) {
         var1.method_57379(class_9334.field_49632, new class_9290(this.loreTexts));
      }

      if (this.hasGlint) {
         var1.method_57379(class_9334.field_49641, true);
      }

      if (this.nbt != null) {
         class_2487 var13 = this.nbt.method_10553();
         if (this.material == class_1802.field_8575 && var13.method_10562("SkullOwner").isPresent()) {
            class_2487 var3 = (class_2487)var13.method_10562("SkullOwner").get();
            Optional var4 = var3.method_10561("Id");
            UUID var5;
            if (var4.isPresent()) {
               int[] var6 = (int[])var4.get();
               var5 = uuidFromIntArray(var6);
            } else {
               Optional var14 = var3.method_10558("Id");
               var5 = var14.map(UUID::fromString).orElse(UUID.randomUUID());
            }

            Builder var15 = ImmutableMultimap.builder();
            Optional var7 = var3.method_10562("Properties");
            if (var7.isPresent()) {
               class_2487 var8 = (class_2487)var7.get();
               Optional var9 = var8.method_10554("textures");
               if (var9.isPresent()) {
                  class_2499 var10 = (class_2499)var9.get();
                  if (!var10.isEmpty()) {
                     Optional var11 = var10.method_10602(0);
                     if (var11.isPresent()) {
                        Optional var12 = ((class_2487)var11.get()).method_10558("Value");
                        if (var12.isPresent()) {
                           var15.put("textures", new Property("textures", (String)var12.get()));
                        }
                     }
                  }
               }
            }

            PropertyMap var16 = new PropertyMap(var15.build());
            GameProfile var17 = new GameProfile(var5, "", var16);
            var1.method_57379(class_9334.field_49617, class_9296.method_73307(var17));
            var13.method_10551("SkullOwner");
         }

         if (!var13.method_33133()) {
            var1.method_57379(class_9334.field_49628, class_9279.method_57456(var13));
         }
      }

      return var1;
   }

   private boolean isPotion(class_1792 var1) {
      return var1 == class_1802.field_8574 || var1 == class_1802.field_8436 || var1 == class_1802.field_8150;
   }

   private int getPotionColorByName(String var1) {
      return switch (var1) {
         case "Зелье отрыжки" -> 16735488;
         case "Зелье серной кислоты" -> 49664;
         case "Зелье вспышки" -> 16777215;
         case "Зелье мочи Флеша" -> 6092799;
         case "Зелье победителя" -> 65280;
         case "Зелье агента" -> 16775936;
         case "Зелье медика" -> 16711902;
         case "Зелье киллера" -> 16711680;
         case "[★] Хлопушка" -> 16738740;
         case "[★] Святая вода" -> 16777215;
         case "[★] Зелье Гнева" -> 10040115;
         case "[★] Зелье Палладина" -> 65535;
         case "[★] Зелье Ассасина" -> 3355443;
         case "[★] Зелье Радиации" -> 3329330;
         case "[★] Снотворное" -> 4737096;
         case "[\ud83c\udf79] Мандариновый сок" -> 14077507;
         default -> 3694022;
      };
   }

   private static UUID uuidFromIntArray(int[] var0) {
      if (var0.length != 4) {
         return UUID.randomUUID();
      }

      long var1 = (long)var0[0] << 32 | var0[1] & 4294967295L;
      long var3 = (long)var0[2] << 32 | var0[3] & 4294967295L;
      return new UUID(var1, var3);
   }

   @Override
   public int getPrice() {
      return this.price;
   }

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   @Override
   public void setEnabled(boolean var1) {
      this.enabled = var1;
   }

   @Override
   public AutoBuyItemSettings getSettings() {
      return this.settings;
   }
}
