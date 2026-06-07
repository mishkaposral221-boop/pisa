package rich.modules.module.setting.implement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import rich.modules.module.setting.Setting;

public class GroupSetting extends Setting {
   private boolean value;
   private List<Setting> subSettings = new ArrayList<>();

   public GroupSetting(String var1, String var2) {
      super(var1, var2);
   }

   public GroupSetting settings(Setting... var1) {
      this.subSettings.addAll(Arrays.asList(var1));
      return this;
   }

   public GroupSetting visible(Supplier<Boolean> var1) {
      this.setVisible(var1);
      return this;
   }

   public Setting getSubSetting(String var1) {
      return this.subSettings.stream().filter(var1x -> var1x.getName().equalsIgnoreCase(var1)).findFirst().orElse(null);
   }

   public boolean isValue() {
      return this.value;
   }

   public List<Setting> getSubSettings() {
      return this.subSettings;
   }

   public GroupSetting setValue(boolean var1) {
      this.value = var1;
      return this;
   }

   public GroupSetting setSubSettings(List<Setting> var1) {
      this.subSettings = var1;
      return this;
   }
}
