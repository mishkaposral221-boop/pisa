package rich.modules.impl.misc;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1792;
import net.minecraft.class_1796;
import net.minecraft.class_1799;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.ColorSetting;
import rich.util.c;

public class ItemCooldowns extends ModuleStructure {
   public final ColorSetting textColor = new ColorSetting("Цвет текста", "Цвет текста кулдауна").value(new Color(255, 60, 60, 255).getRGB());
   private final Map<class_1792, float[]> tracker = new HashMap<>();

   public static ItemCooldowns getInstance() {
      return c.a(ItemCooldowns.class);
   }

   public ItemCooldowns() {
      super("ItemCooldowns", "Показывает кулдаун предметов в слотах", ModuleCategory.UTILITIES);
      this.settings(this.textColor);
   }

   @EventHandler
   public void onTick(TickEvent var1) {
      if (mc.field_1724 == null) {
         this.tracker.clear();
      } else {
         class_1796 var2 = mc.field_1724.method_7357();

         for (int var3 = 0; var3 < mc.field_1724.method_31548().method_5439(); var3++) {
            class_1799 var4 = mc.field_1724.method_31548().method_5438(var3);
            if (!var4.method_7960()) {
               class_1792 var5 = var4.method_7909();
               if (var2.method_7904(var4)) {
                  float var6 = var2.method_7905(var4, 0.0F);
                  float[] var7 = this.tracker.get(var5);
                  if (var7 == null) {
                     this.tracker.put(var5, new float[]{var6, (float)System.currentTimeMillis(), 0.0F});
                  } else if (var7[2] == 0.0F && var7[0] > var6) {
                     float var8 = var7[0] - var6;
                     long var9 = (long)((float)System.currentTimeMillis() - var7[1]);
                     if (var8 > 0.01F && var9 > 100L) {
                        var7[2] = (float)var9 / var8;
                     }
                  }
               } else {
                  this.tracker.remove(var5);
               }
            }
         }
      }
   }

   public float getRemainingSeconds(class_1799 var1) {
      if (mc.field_1724 == null) {
         return -1.0F;
      }

      class_1796 var2 = mc.field_1724.method_7357();
      if (!var2.method_7904(var1)) {
         return -1.0F;
      }

      float var3 = var2.method_7905(var1, 0.0F);
      float[] var4 = this.tracker.get(var1.method_7909());
      return var4 != null && var4[2] != 0.0F ? var3 * var4[2] / 1000.0F : var3 * 20.0F;
   }
}
