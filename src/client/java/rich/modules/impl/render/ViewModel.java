package rich.modules.impl.render;

import net.minecraft.util.Hand;
import net.minecraft.item.CrossbowItem;
import net.minecraft.client.util.math.MatrixStack;
import rich.events.api.EventHandler;
import rich.events.impl.HandOffsetEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SliderSettings;

public class ViewModel extends ModuleStructure {
   private final SliderSettings mainHandXSetting = new SliderSettings("Основная рука X", "Настройка значения X для основной руки")
      .setValue(0.0F)
      .range(-1.0F, 1.0F);
   private final SliderSettings mainHandYSetting = new SliderSettings("Основная рука Y", "Настройка значения Y для основной руки")
      .setValue(0.0F)
      .range(-1.0F, 1.0F);
   private final SliderSettings mainHandZSetting = new SliderSettings("Основная рука Z", "Настройка значения Z для основной руки")
      .setValue(0.0F)
      .range(-2.5F, 2.5F);
   private final SliderSettings offHandXSetting = new SliderSettings("Второстепенная рука X", "Настройка значения X для второстепенной руки")
      .setValue(0.0F)
      .range(-1.0F, 1.0F);
   private final SliderSettings offHandYSetting = new SliderSettings("Второстепенная рука Y", "Настройка значения Y для второстепенной руки")
      .setValue(0.0F)
      .range(-1.0F, 1.0F);
   private final SliderSettings offHandZSetting = new SliderSettings("Второстепенная рука Z", "Настройка значения Z для второстепенной руки")
      .setValue(0.0F)
      .range(-2.5F, 2.5F);

   public ViewModel() {
      super("ViewModel", "View Model", ModuleCategory.VISUALS);
      this.settings(this.mainHandXSetting, this.mainHandYSetting, this.mainHandZSetting, this.offHandXSetting, this.offHandYSetting, this.offHandZSetting);
   }

   @EventHandler
   public void onHandOffset(HandOffsetEvent var1) {
      Hand var2 = var1.getHand();
      if (!var2.equals(Hand.MAIN_HAND) || !(var1.getStack().getItem() instanceof CrossbowItem)) {
         MatrixStack var3 = var1.getMatrices();
         if (var2.equals(Hand.MAIN_HAND)) {
            var3.translate(this.mainHandXSetting.getValue(), this.mainHandYSetting.getValue(), this.mainHandZSetting.getValue());
         } else {
            var3.translate(this.offHandXSetting.getValue(), this.offHandYSetting.getValue(), this.offHandZSetting.getValue());
         }
      }
   }
}
