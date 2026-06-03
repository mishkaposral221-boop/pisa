package rich.modules.module.setting.implement;

import java.util.function.Supplier;
import rich.modules.module.setting.Setting;

public class TextSetting extends Setting {
   private String text;
   private int min;
   private int max;

   public TextSetting(String var1, String var2) {
      super(var1, var2);
   }

   public TextSetting visible(Supplier<Boolean> var1) {
      this.setVisible(var1);
      return this;
   }

   public String getText() {
      return this.text;
   }

   public int getMin() {
      return this.min;
   }

   public int getMax() {
      return this.max;
   }

   public TextSetting setText(String var1) {
      this.text = var1;
      return this;
   }

   public TextSetting setMin(int var1) {
      this.min = var1;
      return this;
   }

   public TextSetting setMax(int var1) {
      this.max = var1;
      return this;
   }
}
