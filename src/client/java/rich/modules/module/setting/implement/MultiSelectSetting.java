package rich.modules.module.setting.implement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import rich.modules.module.setting.Setting;

public class MultiSelectSetting extends Setting {
   private List<String> list;
   private List<String> selected = new ArrayList<>();

   public MultiSelectSetting(String var1, String var2) {
      super(var1, var2);
   }

   public MultiSelectSetting value(String... var1) {
      this.list = Arrays.asList(var1);
      return this;
   }

   public MultiSelectSetting selected(String... var1) {
      this.selected = new ArrayList<>(Arrays.asList(var1));
      return this;
   }

   public MultiSelectSetting visible(Supplier<Boolean> var1) {
      this.setVisible(var1);
      return this;
   }

   public boolean isSelected(String var1) {
      return this.selected.contains(var1);
   }

   public void select(String var1) {
      if (!this.selected.contains(var1)) {
         this.selected.add(var1);
      }
   }

   public void deselect(String var1) {
      this.selected.remove(var1);
   }

   public List<String> getList() {
      return this.list;
   }

   public List<String> getSelected() {
      return this.selected;
   }

   public void setList(List<String> var1) {
      this.list = var1;
   }

   public void setSelected(List<String> var1) {
      this.selected = var1;
   }
}
