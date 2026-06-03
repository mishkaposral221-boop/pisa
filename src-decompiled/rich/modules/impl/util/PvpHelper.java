package rich.modules.impl.util;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1661;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.util.c;

public class PvpHelper extends ModuleStructure {
   private static final class_1792[] PVP_ITEMS = new class_1792[]{
      class_1802.field_8463, class_1802.field_8367, class_1802.field_8288, class_1802.field_8634, class_1802.field_8233
   };
   private static final class_1792[] FOOD_ITEMS = new class_1792[]{
      class_1802.field_8176,
      class_1802.field_8261,
      class_1802.field_8544,
      class_1802.field_8347,
      class_1802.field_8752,
      class_1802.field_8509,
      class_1802.field_8373,
      class_1802.field_8229,
      class_1802.field_8512,
      class_1802.field_8741,
      class_1802.field_8071,
      class_1802.field_8208,
      class_1802.field_8308,
      class_1802.field_8515,
      class_1802.field_8766
   };
   private final Map<Integer, Long> pulseMap = new HashMap<>();
   private boolean inCombat = false;
   private long lastHitTime = 0L;
   private static final long COMBAT_TIMEOUT = 5000L;
   private static final long PULSE_DURATION = 800L;

   public static PvpHelper getInstance() {
      return c.a(PvpHelper.class);
   }

   public PvpHelper() {
      super("PvpHelper", "Подсвечивает полезные предметы во время PvP", ModuleCategory.UTILITIES);
   }

   @EventHandler
   public void onTick(TickEvent var1) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         long var2 = System.currentTimeMillis();
         boolean var4 = false;

         for (class_1297 var6 : mc.field_1687.method_18112()) {
            if (var6 instanceof class_1309 var7 && var6 != mc.field_1724 && var7.method_6052() == mc.field_1724) {
               var4 = true;
               this.lastHitTime = var2;
               break;
            }
         }

         float var16 = mc.field_1724.method_6063();
         float var17 = mc.field_1724.method_6032();
         boolean var18 = var17 < var16 * 0.7F;
         int var8 = mc.field_1724.method_7344().method_7586();
         boolean var9 = var8 < 14;
         boolean var10 = var2 - this.lastHitTime < 5000L;
         this.inCombat = (var4 || var10) && (var18 || var9);
         if (!this.inCombat) {
            this.pulseMap.clear();
         } else {
            class_1661 var11 = mc.field_1724.method_31548();

            for (int var12 = 0; var12 < 36; var12++) {
               class_1799 var13 = var11.method_5438(var12);
               class_1792 var14 = var13.method_7909();
               boolean var15 = this.isPvpItem(var14) || var9 && this.isFoodItem(var14);
               if (var15) {
                  if (!this.pulseMap.containsKey(var12)) {
                     this.pulseMap.put(var12, var2);
                  }
               } else {
                  this.pulseMap.remove(var12);
               }
            }
         }
      }
   }

   private boolean isPvpItem(class_1792 var1) {
      for (class_1792 var5 : PVP_ITEMS) {
         if (var1 == var5) {
            return true;
         }
      }

      return false;
   }

   private boolean isFoodItem(class_1792 var1) {
      for (class_1792 var5 : FOOD_ITEMS) {
         if (var1 == var5) {
            return true;
         }
      }

      return false;
   }

   public int getPulseAlpha(int var1) {
      if (this.isState() && this.inCombat) {
         Long var2 = this.pulseMap.get(var1);
         if (var2 == null) {
            return 0;
         }

         long var3 = (System.currentTimeMillis() - var2) % 800L;
         float var5 = (float)var3 / 800.0F;
         float var6 = (float)Math.sin(var5 * Math.PI);
         return (int)(var6 * 200.0F);
      } else {
         return 0;
      }
   }

   public boolean isInCombat() {
      return this.inCombat;
   }
}
