package rich.modules.module.setting.implement;

import java.util.function.Supplier;
import rich.modules.module.setting.Setting;

public class BooleanSetting extends Setting {
   private boolean value;
   private int key = -1;
   private int type = 1;

   public BooleanSetting(String var1, String var2) {
      super(var1, var2);
   }

   public BooleanSetting visible(Supplier<Boolean> var1) {
      this.setVisible(var1);
      return this;
   }

   public boolean isValue() {
      return this.value;
   }

   public int getKey() {
      return this.key;
   }

   public int getType() {
      return this.type;
   }

   public BooleanSetting setValue(boolean var1) {
      this.value = var1;
      return this;
   }

   public BooleanSetting setKey(int var1) {
      this.key = var1;
      return this;
   }

   public BooleanSetting setType(int var1) {
      this.type = var1;
      return this;
   }
}
