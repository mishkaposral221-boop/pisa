package rich.modules.impl.combat.aura.attack;

import net.minecraft.class_1799;
import rich.IMinecraft;

public class Pressing implements IMinecraft {
   private long lastClickTime = System.currentTimeMillis();

   public boolean isCooldownComplete(int var1) {
      if (mc.field_1724 == null) {
         return false;
      }

      if (this.isHoldingMace()) {
         return this.lastClickPassed() >= 50L;
      }

      float var2 = mc.field_1724.method_7261(var1);
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
      if (mc.field_1724 == null) {
         return false;
      }

      class_1799 var1 = mc.field_1724.method_6047();
      return var1.method_7909().method_7876().toLowerCase().contains("mace");
   }

   public boolean isWeapon() {
      if (mc.field_1724 == null) {
         return false;
      }

      class_1799 var1 = mc.field_1724.method_6047();
      if (var1.method_7960()) {
         return false;
      }

      String var2 = var1.method_7909().method_7876().toLowerCase();
      return var2.contains("sword") || var2.contains("axe") || var2.contains("trident");
   }
}
