package rich.modules.module.setting.implement;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import rich.modules.module.setting.Setting;

public class SelectSetting extends Setting {
   private String selected;
   private List<String> list;

   public SelectSetting(String var1, String var2) {
      super(var1, var2);
   }

   public SelectSetting value(String... var1) {
      this.list = Arrays.asList(var1);
      this.selected = this.list.isEmpty() ? "" : this.list.get(0);
      return this;
   }

   public SelectSetting visible(Supplier<Boolean> var1) {
      this.setVisible(var1);
      return this;
   }

   public SelectSetting selected(String var1) {
      if (this.list.contains(var1)) {
         this.selected = var1;
      }

      return this;
   }

   public boolean isSelected(String var1) {
      return this.selected.equals(var1);
   }

   public String getSelected() {
      return this.selected;
   }

   public List<String> getList() {
      return this.list;
   }

   public void setSelected(String var1) {
      this.selected = var1;
   }
}
