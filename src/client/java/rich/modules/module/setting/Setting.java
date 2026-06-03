package rich.modules.module.setting;

import java.util.function.Supplier;

public class Setting {
   private final String name;
   private String description;
   private Supplier<Boolean> visible;

   public Setting(String var1) {
      this.name = var1;
   }

   public Setting(String var1, String var2) {
      this.name = var1;
      this.description = var2;
   }

   public boolean isVisible() {
      return this.visible == null || this.visible.get();
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public Supplier<Boolean> getVisible() {
      return this.visible;
   }

   public void setVisible(Supplier<Boolean> var1) {
      this.visible = var1;
   }
}
