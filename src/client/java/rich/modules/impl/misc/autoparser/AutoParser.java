package rich.modules.impl.misc.autoparser;

import rich.modules.module.ModuleStructure;

public class AutoParser extends ModuleStructure {
   private static AutoParser instance;

   public AutoParser() {
      super("Auto Parser", null);
      instance = this;
   }

   public static AutoParser getInstance() {
      return instance;
   }

   public boolean isRunning() {
      return false;
   }

   public void setDiscountPercent(int var1) {
   }

   public int getDiscountPercent() {
      return 60;
   }

   public void startParsing() {
   }

   public void stopParsing() {
   }
}
