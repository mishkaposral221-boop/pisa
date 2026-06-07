package rich.modules.impl.util;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.util.c;

public class PvpHelper extends ModuleStructure {
   private static final Item[] PVP_ITEMS = new Item[]{
      Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.TOTEM_OF_UNDYING, Items.ENDER_PEARL, Items.CHORUS_FRUIT
   };
   private static final Item[] FOOD_ITEMS = new Item[]{
      Items.COOKED_BEEF,
      Items.COOKED_PORKCHOP,
      Items.COOKED_CHICKEN,
      Items.COOKED_MUTTON,
      Items.COOKED_RABBIT,
      Items.COOKED_SALMON,
      Items.COOKED_COD,
      Items.BREAD,
      Items.BAKED_POTATO,
      Items.PUMPKIN_PIE,
      Items.GOLDEN_CARROT,
      Items.MUSHROOM_STEW,
      Items.RABBIT_STEW,
      Items.BEETROOT_SOUP,
      Items.SUSPICIOUS_STEW
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
      if (mc.player != null && mc.world != null) {
         long var2 = System.currentTimeMillis();
         boolean var4 = false;

         for (Entity var6 : mc.world.getEntities()) {
            if (var6 instanceof LivingEntity var7 && var6 != mc.player && var7.getAttacking() == mc.player) {
               var4 = true;
               this.lastHitTime = var2;
               break;
            }
         }

         float var16 = mc.player.getMaxHealth();
         float var17 = mc.player.getHealth();
         boolean var18 = var17 < var16 * 0.7F;
         int var8 = mc.player.getHungerManager().getFoodLevel();
         boolean var9 = var8 < 14;
         boolean var10 = var2 - this.lastHitTime < 5000L;
         this.inCombat = (var4 || var10) && (var18 || var9);
         if (!this.inCombat) {
            this.pulseMap.clear();
         } else {
            PlayerInventory var11 = mc.player.getInventory();

            for (int var12 = 0; var12 < 36; var12++) {
               ItemStack var13 = var11.getStack(var12);
               Item var14 = var13.getItem();
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

   private boolean isPvpItem(Item var1) {
      for (Item var5 : PVP_ITEMS) {
         if (var1 == var5) {
            return true;
         }
      }

      return false;
   }

   private boolean isFoodItem(Item var1) {
      for (Item var5 : FOOD_ITEMS) {
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
