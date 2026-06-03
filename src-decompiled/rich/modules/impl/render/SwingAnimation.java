package rich.modules.impl.render;

import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import rich.events.api.EventHandler;
import rich.events.impl.HandAnimationEvent;
import rich.events.impl.SwingDurationEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;

public class SwingAnimation extends ModuleStructure {
   private final SelectSetting swingType = new SelectSetting("Тип взмаха", "Выберите тип взмаха")
      .value("Chop", "Swipe", "Down", "Smooth", "Smooth 2", "Power", "Feast", "Twist", "Default");
   private final SliderSettings hitStrengthSetting = new SliderSettings("Сила взмаха", "Сила анимации взмаха").range(0.5F, 3.0F).setValue(1.0F);
   private final SliderSettings swingSpeedSetting = new SliderSettings("Длительность взмаха", "Длительность анимации удара").range(0.5F, 4.0F).setValue(1.0F);
   private final BooleanSetting onlySwing = new BooleanSetting("Только при взмахе", "Показывает анимацию только при взмахе").setValue(false);
   private float spinAngle = 0.0F;
   private float spinBackTimer = 0.0F;
   private boolean wasSwinging = false;

   public SwingAnimation() {
      super("SwingAnimation", "Swing Animation", ModuleCategory.VISUALS);
      this.settings(this.swingType, this.hitStrengthSetting, this.swingSpeedSetting, this.onlySwing);
   }

   @EventHandler
   public void onSwingDuration(SwingDurationEvent var1) {
      var1.setAnimation(this.swingSpeedSetting.getValue());
      var1.cancel();
   }

   @EventHandler
   public void onHandAnimation(HandAnimationEvent var1) {
      boolean var2 = var1.getHand().equals(class_1268.field_5808);
      if (var2) {
         class_4587 var3 = var1.getMatrices();
         float var4 = var1.getSwingProgress();
         int var5 = mc.field_1724.method_6068().equals(class_1306.field_6183) ? 1 : -1;
         float var6 = class_3532.method_15374(var4 * var4 * (float) Math.PI);
         float var7 = class_3532.method_15374(class_3532.method_15355(var4) * (float) Math.PI);
         float var8 = (float)(Math.sin(var4 * Math.PI) * 0.5);
         float var9 = this.hitStrengthSetting.getValue();
         if (this.onlySwing.isValue() && mc.field_1724.field_6279 == 0) {
            var3.method_46416(var5 * 0.56F, -0.52F, -0.72F);
         } else {
            switch (this.swingType.getSelected()) {
               case "Chop":
                  var3.method_46416(0.56F * var5, -0.44F, -0.72F);
                  var3.method_46416(0.0F, -0.19800001F, 0.0F);
                  var3.method_22907(class_7833.field_40716.rotationDegrees(45.0F * var5));
                  float var12 = class_3532.method_15374(var4 * var4 * (float) Math.PI);
                  float var13 = class_3532.method_15374(class_3532.method_15355(var4) * (float) Math.PI);
                  var3.method_22907(class_7833.field_40718.rotationDegrees(var13 * -20.0F * var5 * var9));
                  var3.method_22907(class_7833.field_40714.rotationDegrees(var13 * -80.0F * var9));
                  var3.method_46416(0.4F, 0.2F, 0.2F);
                  var3.method_46416(-0.5F, 0.08F, 0.0F);
                  var3.method_22907(class_7833.field_40716.rotationDegrees(20.0F));
                  var3.method_22907(class_7833.field_40714.rotationDegrees(-80.0F));
                  var3.method_22907(class_7833.field_40716.rotationDegrees(20.0F));
                  break;
               case "Twist":
                  var3.method_46416(var5 * 0.56F, -0.36F, -0.72F);
                  var3.method_22907(class_7833.field_40716.rotationDegrees(80 * var5));
                  var3.method_22907(class_7833.field_40714.rotationDegrees(var7 * -90.0F * var9));
                  var3.method_22907(class_7833.field_40718.rotationDegrees((var6 - var7) * 60.0F * var5 * var9));
                  var3.method_22907(class_7833.field_40714.rotationDegrees(-30.0F));
                  var3.method_46416(0.0F, -0.1F, 0.05F);
                  break;
               case "Swipe":
                  var3.method_46416(0.56F * var5, -0.32F, -0.72F);
                  var3.method_22907(class_7833.field_40716.rotationDegrees(70 * var5));
                  var3.method_22907(class_7833.field_40718.rotationDegrees(-20 * var5));
                  var3.method_22907(class_7833.field_40716.rotationDegrees(var7 * var6 * -5.0F * var9));
                  var3.method_22907(class_7833.field_40714.rotationDegrees(var7 * var6 * -120.0F * var9));
                  var3.method_22907(class_7833.field_40714.rotationDegrees(-70.0F));
                  break;
               case "Default":
                  var3.method_46416(var5 * 0.56F, -0.52F - var7 * 0.5F * var9, -0.72F);
                  var3.method_22907(class_7833.field_40716.rotationDegrees(45 * var5));
                  var3.method_22907(class_7833.field_40716.rotationDegrees(-45 * var5));
                  break;
               case "Down":
                  var3.method_46416(var5 * 0.56F, -0.32F, -0.72F);
                  var3.method_22907(class_7833.field_40716.rotationDegrees(76 * var5));
                  var3.method_22907(class_7833.field_40716.rotationDegrees(var7 * -5.0F * var9));
                  var3.method_22907(class_7833.field_40713.rotationDegrees(var7 * -100.0F * var9));
                  var3.method_22907(class_7833.field_40714.rotationDegrees(var7 * -155.0F * var9));
                  var3.method_22907(class_7833.field_40714.rotationDegrees(-100.0F));
                  break;
               case "Smooth":
                  var3.method_46416(var5 * 0.56F, -0.42F, -0.72F);
                  var3.method_22907(class_7833.field_40716.rotationDegrees(var5 * (45.0F + var6 * -20.0F * var9)));
                  var3.method_22907(class_7833.field_40718.rotationDegrees(var5 * var7 * -20.0F * var9));
                  var3.method_22907(class_7833.field_40714.rotationDegrees(var7 * -80.0F * var9));
                  var3.method_22907(class_7833.field_40716.rotationDegrees(var5 * -45.0F));
                  var3.method_22904(0.0, -0.1, 0.0);
                  break;
               case "Smooth 2":
                  var3.method_46416(var5 * 0.56F, -0.42F, -0.72F);
                  var3.method_22907(class_7833.field_40714.rotationDegrees(var7 * -80.0F * var9));
                  var3.method_22904(0.0, -0.1, 0.0);
                  break;
               case "Power":
                  var3.method_46416(var5 * 0.56F, -0.32F, -0.72F);
                  var3.method_46416(-var8 * var8 * var6 * var5 * var9, 0.0F, 0.0F);
                  var3.method_22907(class_7833.field_40716.rotationDegrees(61 * var5));
                  var3.method_22907(class_7833.field_40718.rotationDegrees(var7 * var9));
                  var3.method_22907(class_7833.field_40716.rotationDegrees(var7 * var6 * -5.0F * var9));
                  var3.method_22907(class_7833.field_40714.rotationDegrees(var7 * var6 * -30.0F * var9));
                  var3.method_22907(class_7833.field_40714.rotationDegrees(-60.0F));
                  var3.method_22907(class_7833.field_40714.rotationDegrees(var8 * -60.0F * var9));
                  break;
               case "Feast":
                  var3.method_46416(var5 * 0.56F, -0.32F, -0.72F);
                  var3.method_22907(class_7833.field_40716.rotationDegrees(30 * var5));
                  var3.method_22907(class_7833.field_40716.rotationDegrees(var7 * 75.0F * var5 * var9));
                  var3.method_22907(class_7833.field_40714.rotationDegrees(var7 * -45.0F * var9));
                  var3.method_22907(class_7833.field_40716.rotationDegrees(30 * var5));
                  var3.method_22907(class_7833.field_40714.rotationDegrees(-80.0F));
                  var3.method_22907(class_7833.field_40716.rotationDegrees(35 * var5));
            }
         }

         var1.cancel();
      }
   }
}
