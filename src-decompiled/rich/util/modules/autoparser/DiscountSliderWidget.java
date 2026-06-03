package rich.util.modules.autoparser;

import net.minecraft.class_2561;
import net.minecraft.class_357;

public class DiscountSliderWidget extends class_357 {
   public DiscountSliderWidget(int var1, int var2, int var3, int var4, int var5) {
      super(var1, var2, var3, var4, class_2561.method_43470("Discount: " + var5 + "%"), (var5 - 10) / 80.0);
   }

   protected void method_25346() {
      int var1 = (int)(this.field_22753 * 80.0) + 10;
      this.method_25355(class_2561.method_43470("Discount: " + var1 + "%"));
   }

   protected void method_25344() {
   }
}
