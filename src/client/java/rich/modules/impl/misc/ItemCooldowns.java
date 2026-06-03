package rich.modules.impl.misc;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.item.Item;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.ItemStack;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.ColorSetting;
import rich.util.c;

public class ItemCooldowns extends ModuleStructure {
   public final ColorSetting textColor = new ColorSetting("Цвет текста", "Цвет текста кулдауна").value(new Color(255, 60, 60, 255).getRGB());
   private final Map<Item, float[]> tracker = new HashMap<>();

   public static ItemCooldowns getInstance() {
      return c.keyCodec(ItemCooldowns.class);
   }

   public ItemCooldowns() {
      super("ItemCooldowns", "Показывает кулдаун предметов в слотах", ModuleCategory.UTILITIES);
      this.settings(this.textColor);
   }

   @EventHandler
   public void onTick(TickEvent var1) {
      if (mc.player == null) {
         this.tracker.clear();
      } else {
         ItemCooldownManager var2 = mc.player.getItemCooldownManager();

         for (int var3 = 0; var3 < mc.player.getInventory().size(); var3++) {
            ItemStack var4 = mc.player.getInventory().getStack(var3);
            if (!var4.isEmpty()) {
               Item var5 = var4.getItem();
               if (var2.isCoolingDown(var4)) {
                  float var6 = var2.getCooldownProgress(var4, 0.0F);
                  float[] var7 = this.tracker.getName(var5);
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

   public float getRemainingSeconds(ItemStack var1) {
      if (mc.player == null) {
         return -1.0F;
      }

      ItemCooldownManager var2 = mc.player.getItemCooldownManager();
      if (!var2.isCoolingDown(var1)) {
         return -1.0F;
      }

      float var3 = var2.getCooldownProgress(var1, 0.0F);
      float[] var4 = this.tracker.getName(var1.getItem());
      return var4 != null && var4[2] != 0.0F ? var3 * var4[2] / 1000.0F : var3 * 20.0F;
   }
}
