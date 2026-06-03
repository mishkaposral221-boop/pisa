package rich.modules.module.setting.implement;

import java.util.function.Supplier;
import rich.modules.module.setting.Setting;

public class BindSetting extends Setting {
   private int key = -1;
   private int type = 1;

   public BindSetting(String var1, String var2) {
      super(var1, var2);
   }

   public BindSetting visible(Supplier<Boolean> var1) {
      this.setVisible(var1);
      return this;
   }

   public int getKey() {
      return this.key;
   }

   public int getType() {
      return this.type;
   }

   public BindSetting setKey(int var1) {
      this.key = var1;
      return this;
   }

   public BindSetting setType(int var1) {
      this.type = var1;
      return this;
   }
}
