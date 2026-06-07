package rich.modules.impl.combat.aura.attack;

import net.minecraft.item.ItemStack;
import rich.IMinecraft;

public class Pressing implements IMinecraft {
   private long lastClickTime = System.currentTimeMillis();

   public boolean isCooldownComplete(int var1) {
      if (mc.player == null) {
         return false;
      }

      if (this.isHoldingMace()) {
         return this.lastClickPassed() >= 50L;
      }

      float var2 = mc.player.getAttackCooldownProgress(var1);
      return var2 >= 0.95F;
   }

   public boolean isMaceFastAttack() {
      return this.isHoldingMace() && this.lastClickPassed() >= 50L;
   }

   public long lastClickPassed() {
      return System.currentTimeMillis() - this.lastClickTime;
   }

   public void recalculate() {
      this.lastClickTime = System.currentTimeMillis();
   }

   public boolean isHoldingMace() {
      if (mc.player == null) {
         return false;
      }

      ItemStack var1 = mc.player.getMainHandStack();
      return var1.getItem().getTranslationKey().toLowerCase().contains("mace");
   }

   public boolean isWeapon() {
      if (mc.player == null) {
         return false;
      }

      ItemStack var1 = mc.player.getMainHandStack();
      if (var1.isEmpty()) {
         return false;
      }

      String var2 = var1.getItem().getTranslationKey().toLowerCase();
      return var2.contains("sword") || var2.contains("axe") || var2.contains("trident");
   }
}
