package rich.util.inventory;

import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;

public class MovementController {
   private static final class_310 mc = class_310.method_1551();
   private boolean forward;
   private boolean back;
   private boolean left;
   private boolean right;
   private boolean jump;
   private boolean sprint;
   private boolean saved = false;
   private boolean blocked = false;

   public void saveState() {
      if (mc.field_1724 != null) {
         this.forward = this.isKeyPressed(mc.field_1690.field_1894);
         this.back = this.isKeyPressed(mc.field_1690.field_1881);
         this.left = this.isKeyPressed(mc.field_1690.field_1913);
         this.right = this.isKeyPressed(mc.field_1690.field_1849);
         this.jump = this.isKeyPressed(mc.field_1690.field_1903);
         this.sprint = mc.field_1724.method_5624();
         this.saved = true;
      }
   }

   public void block() {
      if (mc.field_1724 != null) {
         mc.field_1690.field_1894.method_23481(false);
         mc.field_1690.field_1881.method_23481(false);
         mc.field_1690.field_1913.method_23481(false);
         mc.field_1690.field_1849.method_23481(false);
         mc.field_1690.field_1903.method_23481(false);
         mc.field_1690.field_1867.method_23481(false);
         this.blocked = true;
      }
   }

   public void stopSprint() {
      if (mc.field_1724 != null) {
         mc.field_1724.method_5728(false);
         mc.field_1690.field_1867.method_23481(false);
      }
   }

   public void restore() {
      if (this.saved) {
         mc.field_1690.field_1894.method_23481(this.forward && this.isCurrentlyPressed(mc.field_1690.field_1894));
         mc.field_1690.field_1881.method_23481(this.back && this.isCurrentlyPressed(mc.field_1690.field_1881));
         mc.field_1690.field_1913.method_23481(this.left && this.isCurrentlyPressed(mc.field_1690.field_1913));
         mc.field_1690.field_1849.method_23481(this.right && this.isCurrentlyPressed(mc.field_1690.field_1849));
         mc.field_1690.field_1903.method_23481(this.jump && this.isCurrentlyPressed(mc.field_1690.field_1903));
         this.blocked = false;
         this.saved = false;
      }
   }

   public void restoreFromCurrent() {
      mc.field_1690.field_1894.method_23481(this.isCurrentlyPressed(mc.field_1690.field_1894));
      mc.field_1690.field_1881.method_23481(this.isCurrentlyPressed(mc.field_1690.field_1881));
      mc.field_1690.field_1913.method_23481(this.isCurrentlyPressed(mc.field_1690.field_1913));
      mc.field_1690.field_1849.method_23481(this.isCurrentlyPressed(mc.field_1690.field_1849));
      mc.field_1690.field_1903.method_23481(this.isCurrentlyPressed(mc.field_1690.field_1903));
      mc.field_1690.field_1867.method_23481(this.isCurrentlyPressed(mc.field_1690.field_1867));
      this.blocked = false;
   }

   public boolean isPlayerStopped(double var1) {
      if (mc.field_1724 == null) {
         return true;
      }

      double var3 = Math.abs(mc.field_1724.method_18798().field_1352);
      double var5 = Math.abs(mc.field_1724.method_18798().field_1350);
      return var3 < var1 && var5 < var1;
   }

   public boolean isBlocked() {
      return this.blocked;
   }

   public void reset() {
      this.saved = false;
      this.blocked = false;
   }

   private boolean isKeyPressed(class_304 var1) {
      return var1.method_1434();
   }

   private boolean isCurrentlyPressed(class_304 var1) {
      return class_3675.method_15987(mc.method_22683(), class_3675.method_15981(var1.method_1428()).method_1444());
   }
}
