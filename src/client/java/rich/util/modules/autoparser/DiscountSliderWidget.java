package rich.util.modules.autoparser;

import net.minecraft.text.Text;
import net.minecraft.client.gui.widget.SliderWidget;

public class DiscountSliderWidget extends SliderWidget {
   public DiscountSliderWidget(int var1, int var2, int var3, int var4, int var5) {
      super(var1, var2, var3, var4, Text.literal("Discount: " + var5 + "%"), (var5 - 10) / 80.0);
   }

   protected void updateMessage() {
      int var1 = (int)(this.value * 80.0) + 10;
      this.setMessage(Text.literal("Discount: " + var1 + "%"));
   }

   protected void applyValue() {
   }
}
