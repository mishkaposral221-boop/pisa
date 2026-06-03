package rich.modules.module.setting.implement;

import java.util.function.Supplier;
import rich.modules.module.setting.Setting;

public class ButtonSetting extends Setting {
   private Runnable runnable;
   private String buttonName;

   public ButtonSetting(String var1, String var2) {
      super(var1, var2);
   }

   public ButtonSetting visible(Supplier<Boolean> var1) {
      this.setVisible(var1);
      return this;
   }

   public Runnable getRunnable() {
      return this.runnable;
   }

   public String getButtonName() {
      return this.buttonName;
   }

   public ButtonSetting setRunnable(Runnable var1) {
      this.runnable = var1;
      return this;
   }

   public ButtonSetting setButtonName(String var1) {
      this.buttonName = var1;
      return this;
   }
}
