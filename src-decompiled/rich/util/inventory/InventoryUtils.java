package rich.util.inventory;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.class_10192;
import net.minecraft.class_1268;
import net.minecraft.class_1304;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2815;
import net.minecraft.class_2868;
import net.minecraft.class_2886;
import net.minecraft.class_310;
import net.minecraft.class_408;
import net.minecraft.class_7202;
import net.minecraft.class_9334;
import rich.mixin.ClientWorldAccessor;

public final class InventoryUtils {
   private static final class_310 mc = class_310.method_1551();
   private static final class_1304[] ARMOR_SLOTS = new class_1304[]{class_1304.field_6166, class_1304.field_6172, class_1304.field_6174, class_1304.field_6169};
   private static int savedSlot = -1;
   private static int silentSlot = -1;

   private InventoryUtils() {
   }

   public static int findItemInHotbar(class_1792 var0) {
      if (mc.field_1724 == null) {
         return -1;
      }

      for (int var1 = 0; var1 < 9; var1++) {
         class_1799 var2 = mc.field_1724.method_31548().method_5438(var1);
         if (!var2.method_7960() && var2.method_7909() == var0) {
            return var1;
         }
      }

      return -1;
   }

   public static int findItemInInventory(class_1792 var0) {
      if (mc.field_1724 == null) {
         return -1;
      }

      for (int var1 = 9; var1 < 36; var1++) {
         class_1799 var2 = mc.field_1724.method_31548().method_5438(var1);
         if (!var2.method_7960() && var2.method_7909() == var0) {
            return var1;
         }
      }

      return -1;
   }

   public static int findItemAnywhere(class_1792 var0) {
      int var1 = findItemInHotbar(var0);
      return var1 != -1 ? var1 : findItemInInventory(var0);
   }

   public static InventoryResult find(class_1792 var0) {
      return find(var1 -> var1.method_7909() == var0);
   }

   public static InventoryResult find(class_1792... var0) {
      return find(Arrays.asList(var0));
   }

   public static InventoryResult find(List<class_1792> var0) {
      return find(var1 -> var0.contains(var1.method_7909()));
   }

   public static boolean hasElytra() {
      return mc.field_1724 == null ? false : mc.field_1724.method_6118(class_1304.field_6174).method_58694(class_9334.field_54197) != null;
   }

   public static int findHotbarItem(class_1792 var0) {
      if (mc.field_1724 == null) {
         return -1;
      }

      for (int var1 = 0; var1 < 9; var1++) {
         if (mc.field_1724.method_31548().method_5438(var1).method_7909() == var0) {
            return var1;
         }
      }

      return -1;
   }

   public static int findElytraSlot() {
      if (mc.field_1724 == null) {
         return -1;
      }

      for (int var0 = 0; var0 < 46; var0++) {
         if (mc.field_1724.method_31548().method_5438(var0).method_7909() == class_1802.field_8833) {
            return var0;
         }
      }

      return -1;
   }

   public static int findChestArmorSlot() {
      if (mc.field_1724 == null) {
         return -1;
      }

      for (int var0 = 0; var0 < 46; var0++) {
         class_1799 var1 = mc.field_1724.method_31548().method_5438(var0);
         class_10192 var2 = (class_10192)var1.method_58694(class_9334.field_54196);
         if (var2 != null && var2.comp_3174() == class_1304.field_6174 && var1.method_7909() != class_1802.field_8833) {
            return var0;
         }
      }

      return -1;
   }

   public static InventoryResult find(ItemSearcher var0) {
      if (mc.field_1724 == null) {
         return InventoryResult.notFound();
      }

      for (class_1304 var4 : ARMOR_SLOTS) {
         class_1799 var5 = mc.field_1724.method_6118(var4);
         if (isValid(var5) && var0.matches(var5)) {
            return new InventoryResult(-2, true, var5);
         }
      }

      for (int var6 = 35; var6 >= 0; var6--) {
         class_1799 var7 = mc.field_1724.method_31548().method_5438(var6);
         if (isValid(var7) && var0.matches(var7)) {
            int var8 = var6 < 9 ? var6 + 36 : var6;
            return InventoryResult.of(var8, var7);
         }
      }

      return InventoryResult.notFound();
   }

   public static InventoryResult findHotbar(class_1792 var0) {
      return findHotbar(var1 -> var1.method_7909() == var0);
   }

   public static InventoryResult findHotbar(ItemSearcher var0) {
      if (mc.field_1724 == null) {
         return InventoryResult.notFound();
      }

      for (int var1 = 0; var1 < 9; var1++) {
         class_1799 var2 = mc.field_1724.method_31548().method_5438(var1);
         if (isValid(var2) && var0.matches(var2)) {
            return InventoryResult.of(var1, var2);
         }
      }

      return InventoryResult.notFound();
   }

   public static class_1735 findSlot(class_1792 var0) {
      return findSlot(var1 -> var1.method_7677().method_7909() == var0, null);
   }

   public static class_1735 findSlot(Predicate<class_1735> var0) {
      return findSlot(var0, null);
   }

   public static class_1735 findSlot(Predicate<class_1735> var0, Comparator<class_1735> var1) {
      if (mc.field_1724 == null) {
         return null;
      }

      Stream var2 = mc.field_1724.field_7512.field_7761.stream().filter(var0);
      return var1 != null ? (class_1735)var2.max(var1).orElse(null) : (class_1735)var2.findFirst().orElse(null);
   }

   public static class_1735 findSlot(class_1792 var0, Predicate<class_1735> var1, Comparator<class_1735> var2) {
      Predicate var3 = var2x -> var2x.method_7677().method_7909() == var0 && var1.test(var2x);
      return findSlot(var3, var2);
   }

   public static class_1735 findSlotInHotbar(class_1792 var0) {
      if (mc.field_1724 == null) {
         return null;
      }

      for (int var1 = 36; var1 <= 44; var1++) {
         class_1735 var2 = mc.field_1724.field_7498.method_7611(var1);
         if (var2 != null && !var2.method_7677().method_7960() && var2.method_7677().method_7909() == var0) {
            return var2;
         }
      }

      return null;
   }

   public static class_1735 findSlotInInventory(class_1792 var0) {
      if (mc.field_1724 == null) {
         return null;
      }

      for (int var1 = 9; var1 <= 35; var1++) {
         class_1735 var2 = mc.field_1724.field_7498.method_7611(var1);
         if (var2 != null && !var2.method_7677().method_7960() && var2.method_7677().method_7909() == var0) {
            return var2;
         }
      }

      return null;
   }

   public static class_1735 findSlotAnywhere(class_1792 var0) {
      class_1735 var1 = findSlotInHotbar(var0);
      return var1 != null ? var1 : findSlotInInventory(var0);
   }

   public static class_1735 findRegularTotemSlot() {
      if (mc.field_1724 == null) {
         return null;
      }

      for (int var0 = 36; var0 <= 44; var0++) {
         class_1735 var1 = mc.field_1724.field_7498.method_7611(var0);
         if (var1 != null
            && !var1.method_7677().method_7960()
            && var1.method_7677().method_7909() == class_1802.field_8288
            && !var1.method_7677().method_7942()) {
            return var1;
         }
      }

      for (int var2 = 9; var2 <= 35; var2++) {
         class_1735 var3 = mc.field_1724.field_7498.method_7611(var2);
         if (var3 != null
            && !var3.method_7677().method_7960()
            && var3.method_7677().method_7909() == class_1802.field_8288
            && !var3.method_7677().method_7942()) {
            return var3;
         }
      }

      return null;
   }

   public static class_1735 findEnchantedTotemSlot() {
      if (mc.field_1724 == null) {
         return null;
      }

      for (int var0 = 36; var0 <= 44; var0++) {
         class_1735 var1 = mc.field_1724.field_7498.method_7611(var0);
         if (var1 != null && !var1.method_7677().method_7960() && var1.method_7677().method_7909() == class_1802.field_8288 && var1.method_7677().method_7942()
            )
          {
            return var1;
         }
      }

      for (int var2 = 9; var2 <= 35; var2++) {
         class_1735 var3 = mc.field_1724.field_7498.method_7611(var2);
         if (var3 != null && !var3.method_7677().method_7960() && var3.method_7677().method_7909() == class_1802.field_8288 && var3.method_7677().method_7942()
            )
          {
            return var3;
         }
      }

      return null;
   }

   public static class_1735 findTotemSlot(boolean var0) {
      if (mc.field_1724 == null) {
         return null;
      }

      class_1735 var1 = null;
      class_1735 var2 = null;

      for (int var3 = 36; var3 <= 44; var3++) {
         class_1735 var4 = mc.field_1724.field_7498.method_7611(var3);
         if (var4 != null && !var4.method_7677().method_7960() && var4.method_7677().method_7909() == class_1802.field_8288) {
            if (!var4.method_7677().method_7942()) {
               if (var1 == null) {
                  var1 = var4;
               }
            } else if (var2 == null) {
               var2 = var4;
            }
         }
      }

      for (int var5 = 9; var5 <= 35; var5++) {
         class_1735 var6 = mc.field_1724.field_7498.method_7611(var5);
         if (var6 != null && !var6.method_7677().method_7960() && var6.method_7677().method_7909() == class_1802.field_8288) {
            if (!var6.method_7677().method_7942()) {
               if (var1 == null) {
                  var1 = var6;
               }
            } else if (var2 == null) {
               var2 = var6;
            }
         }
      }

      if (var0) {
         return var1 != null ? var1 : var2;
      } else {
         return var1 != null ? var1 : var2;
      }
   }

   public static boolean hasEnchantedTotemInOffhand() {
      if (mc.field_1724 == null) {
         return false;
      }

      class_1799 var0 = mc.field_1724.method_6079();
      return var0.method_7909() == class_1802.field_8288 && var0.method_7942();
   }

   public static boolean hasRegularTotemInOffhand() {
      if (mc.field_1724 == null) {
         return false;
      }

      class_1799 var0 = mc.field_1724.method_6079();
      return var0.method_7909() == class_1802.field_8288 && !var0.method_7942();
   }

   public static void swap(int var0, int var1) {
      click(var0, 0, class_1713.field_7790);
      click(var1, 0, class_1713.field_7790);
      click(var0, 0, class_1713.field_7790);
   }

   public static void swapHotbar(int var0, int var1) {
      click(var0, var1, class_1713.field_7791);
   }

   public static void swapToOffhand(int var0) {
      click(var0, 40, class_1713.field_7791);
   }

   public static void swapToOffhand(class_1735 var0) {
      if (var0 != null) {
         click(var0.field_7874, 40, class_1713.field_7791);
      }
   }

   public static void swapOffhandWithSlot(int var0) {
      if (mc.field_1724 != null && mc.field_1761 != null) {
         int var1 = mc.field_1724.field_7498.field_7763;
         mc.field_1761.method_2906(var1, var0, 40, class_1713.field_7791, mc.field_1724);
      }
   }

   public static void moveToSlot(int var0, int var1) {
      swap(var0, var1);
   }

   public static void click(int var0, int var1, class_1713 var2) {
      if (mc.field_1724 != null && mc.field_1761 != null && var0 != -1) {
         mc.field_1761.method_2906(mc.field_1724.field_7512.field_7763, var0, var1, var2, mc.field_1724);
      }
   }

   public static void selectSlot(int var0) {
      if (mc.field_1724 != null && var0 >= 0 && var0 <= 8) {
         if (mc.field_1724.method_31548().method_67532() != var0) {
            mc.field_1724.method_31548().method_61496(var0);
         }
      }
   }

   public static void selectSlotSilent(int var0) {
      if (mc.field_1724 != null && mc.method_1562() != null && var0 >= 0 && var0 <= 8) {
         mc.method_1562().method_52787(new class_2868(var0));
      }
   }

   public static void saveSlot() {
      if (mc.field_1724 != null) {
         savedSlot = mc.field_1724.method_31548().method_67532();
      }
   }

   public static void restoreSlot() {
      if (savedSlot != -1) {
         selectSlot(savedSlot);
         savedSlot = -1;
      }
   }

   public static void restoreSlotSilent() {
      if (savedSlot != -1) {
         selectSlotSilent(savedSlot);
         savedSlot = -1;
      }
   }

   public static void silentUseHotbarItem(int var0) {
      if (mc.field_1724 != null && mc.method_1562() != null) {
         int var1 = mc.field_1724.method_31548().method_67532();
         if (var0 != var1) {
            mc.method_1562().method_52787(new class_2868(var0));
         }

         sendUsePacket(class_1268.field_5808);
         if (var0 != var1) {
            mc.method_1562().method_52787(new class_2868(var1));
         }
      }
   }

   public static void silentSwapUseAndReturn(int var0) {
      if (mc.field_1724 != null && mc.method_1562() != null) {
         int var1 = mc.field_1724.method_31548().method_67532();
         click(var0, var1, class_1713.field_7791);
         sendUsePacket(class_1268.field_5808);
         click(var0, var1, class_1713.field_7791);
      }
   }

   public static void silentUseItem(class_1792 var0) {
      if (mc.field_1724 != null) {
         int var1 = findItemInHotbar(var0);
         if (var1 != -1) {
            silentUseHotbarItem(var1);
         } else {
            int var2 = findItemInInventory(var0);
            if (var2 != -1) {
               int var3 = wrapSlot(var2);
               silentSwapUseAndReturn(var3);
               closeScreen();
            }
         }
      }
   }

   public static void sendUsePacket(class_1268 var0) {
      if (mc.field_1724 != null && mc.method_1562() != null && mc.field_1687 != null) {
         try {
            ClientWorldAccessor var1 = (ClientWorldAccessor)mc.field_1687;
            class_7202 var2 = var1.getPendingUpdateManager().method_41937();
            int var3 = var2.method_41942();
            mc.method_1562().method_52787(new class_2886(var0, var3, mc.field_1724.method_36454(), mc.field_1724.method_36455()));
            var2.close();
         } catch (Exception var4) {
            mc.method_1562().method_52787(new class_2886(var0, 0, mc.field_1724.method_36454(), mc.field_1724.method_36455()));
         }
      }
   }

   public static void use(class_1268 var0) {
      if (mc.field_1724 != null && mc.field_1761 != null) {
         mc.field_1761.method_2919(mc.field_1724, var0);
      }
   }

   public static void closeScreen() {
      if (mc.field_1724 != null && mc.method_1562() != null) {
         mc.method_1562().method_52787(new class_2815(mc.field_1724.field_7512.field_7763));
      }
   }

   public static boolean isScreenOpen() {
      return mc.field_1755 != null && !(mc.field_1755 instanceof class_408);
   }

   public static int wrapSlot(int var0) {
      return var0 < 9 ? var0 + 36 : var0;
   }

   public static int currentSlot() {
      return mc.field_1724 != null ? mc.field_1724.method_31548().method_67532() : 0;
   }

   public static class_1799 offhandStack() {
      return mc.field_1724 != null ? mc.field_1724.method_6079() : class_1799.field_8037;
   }

   public static class_1799 mainhandStack() {
      return mc.field_1724 != null ? mc.field_1724.method_6047() : class_1799.field_8037;
   }

   public static boolean hasTotemInOffhand() {
      return mc.field_1724 != null && mc.field_1724.method_6079().method_7909() == class_1802.field_8288;
   }

   public static class_1792 getOffhandItem() {
      return mc.field_1724 != null ? mc.field_1724.method_6079().method_7909() : class_1802.field_8162;
   }

   private static boolean isValid(class_1799 var0) {
      return !var0.method_7960() && var0.method_7919() < var0.method_7936() - 10;
   }
}
