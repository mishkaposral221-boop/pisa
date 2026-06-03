package rich.modules.impl.render;

import rich.events.api.EventHandler;
import rich.events.impl.DrawEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.c;

public class AspectRatio extends ModuleStructure {
   public SelectSetting aspectRatio = new SelectSetting("Шаблон", "Готовые соотношения сторон").value("16:9", "4:3", "16:10", "Кастом").selected("16:9");
   public SliderSettings customWidth = new SliderSettings("Ширина", "Кастомная ширина экрана")
      .range(800, 3840)
      .setValue(1920.0F)
      .visible(() -> this.aspectRatio.isSelected("Кастом"));
   public SliderSettings customHeight = new SliderSettings("Высота", "Кастомная высота экрана")
      .range(600, 2160)
      .setValue(1080.0F)
      .visible(() -> this.aspectRatio.isSelected("Кастом"));
   private int targetWidth;
   private int targetHeight;
   private int originalWidth = -1;
   private int originalHeight = -1;
   private String lastSelectedRatio;
   private float lastCustomWidth;
   private float lastCustomHeight;

   public static AspectRatio getInstance() {
      return c.keyCodec(AspectRatio.class);
   }

   public AspectRatio() {
      super("AspectRatio", "Растяг экрана и соотношение сторон", ModuleCategory.VISUALS);
      this.settings(this.aspectRatio, this.customWidth, this.customHeight);
   }

   @Override
   public void activate() {
      this.updateAspectRatio();
   }

   @Override
   public void deactivate() {
   }

   @EventHandler
   public void onRender(DrawEvent var1) {
      if (this.isState()) {
         boolean var2 = !this.aspectRatio.getSelected().equals(this.lastSelectedRatio)
            || this.aspectRatio.isSelected("Кастом")
               && (this.customWidth.getValue() != this.lastCustomWidth || this.customHeight.getValue() != this.lastCustomHeight);
         if (var2) {
            this.updateAspectRatio();
         }
      }
   }

   public float getAspectRatio() {
      if (this.targetWidth == 0 || this.targetHeight == 0) {
         this.updateAspectRatio();
      }

      return (float)this.targetWidth / this.targetHeight;
   }

   private void updateAspectRatio() {
      String var1 = this.aspectRatio.getSelected();
      this.lastSelectedRatio = var1;
      this.lastCustomWidth = this.customWidth.getValue();
      this.lastCustomHeight = this.customHeight.getValue();
      switch (var1) {
         case "16:9":
            this.targetWidth = 1920;
            this.targetHeight = 1080;
            break;
         case "4:3":
            this.targetWidth = 1440;
            this.targetHeight = 1080;
            break;
         case "16:10":
            this.targetWidth = 1680;
            this.targetHeight = 1050;
            break;
         case "Кастом":
            this.targetWidth = (int)this.customWidth.getValue();
            this.targetHeight = (int)this.customHeight.getValue();
      }
   }
}
