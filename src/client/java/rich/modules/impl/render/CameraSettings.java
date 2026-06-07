package rich.modules.impl.render;

import net.minecraft.util.math.MathHelper;
import rich.events.api.EventHandler;
import rich.events.impl.CameraEvent;
import rich.events.impl.FovEvent;
import rich.events.impl.HotBarScrollEvent;
import rich.events.impl.KeyEvent;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BindSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.math.MathUtils;
import rich.util.string.PlayerInteractionHelper;

public class CameraSettings extends ModuleStructure {
   private float fov = 110.0F;
   private float smoothFov = 30.0F;
   private float lastChangedFov = 30.0F;
   private SliderSettings distanceSetting = new SliderSettings("Дистанция камеры", "Настройка расстояния камеры").setValue(3.0F).range(2.0F, 5.0F);
   private BindSetting zoomSetting = new BindSetting("Зум", "Клавиша для увеличения камеры");

   public CameraSettings() {
      super("CameraSettings", "Camera Settings", ModuleCategory.VISUALS);
      this.settings(this.distanceSetting, this.zoomSetting);
   }

   @EventHandler
   public void onKey(KeyEvent var1) {
      if (var1.isKeyDown(this.zoomSetting.getKey())) {
         this.fov = Math.min(this.lastChangedFov, (Integer)mc.options.getFov().getValue() - 20);
      }

      if (var1.isKeyReleased(this.zoomSetting.getKey(), true)) {
         this.lastChangedFov = this.fov;
         this.fov = ((Integer)mc.options.getFov().getValue()).intValue();
      }
   }

   @EventHandler
   public void onHotBarScroll(HotBarScrollEvent var1) {
      if (PlayerInteractionHelper.isKey(this.zoomSetting)) {
         this.fov = (int)MathHelper.clamp(this.fov - var1.getVertical() * 10.0, 10.0, ((Integer)mc.options.getFov().getValue()).intValue());
         var1.cancel();
      }
   }

   @EventHandler
   public void onFov(FovEvent var1) {
      var1.setFov(
         (int)MathHelper.clamp(
            (this.smoothFov = MathUtils.interpolateSmooth(1.6, this.smoothFov, this.fov)) + 1.0F,
            10.0F,
            ((Integer)mc.options.getFov().getValue()).intValue()
         )
      );
      var1.cancel();
   }

   @EventHandler
   public void onCamera(CameraEvent var1) {
      var1.setDistance(this.distanceSetting.getValue());
      var1.setAngle(MathAngle.cameraAngle());
      var1.cancel();
   }
}
