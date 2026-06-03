package rich.modules.impl.render;

import rich.events.api.EventHandler;
import rich.events.impl.CameraEvent;
import rich.events.impl.KeyEvent;
import rich.events.impl.MouseRotationEvent;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SliderSettings;

public class FreeLook extends ModuleStructure {
   public final SliderSettings sensitivity = new SliderSettings("Чувствительность", "Чувствительность камеры").range(0.1F, 3.0F).setValue(1.0F);
   private float cameraYaw = 0.0F;
   private float cameraPitch = 0.0F;
   private boolean active = false;

   public FreeLook() {
      super("FreeLook", "Свободная камера", ModuleCategory.VISUALS);
      this.settings(this.sensitivity);
   }

   @Override
   public void deactivate() {
      this.active = false;
      super.deactivate();
   }

   @EventHandler
   public void onKey(KeyEvent var1) {
      if (mc.field_1724 != null) {
         if (var1.getKey() == 294 && var1.getAction() == 1) {
            this.active = !this.active;
            if (this.active) {
               this.cameraYaw = mc.field_1724.method_36454();
               this.cameraPitch = mc.field_1724.method_36455();
            }
         }
      }
   }

   @EventHandler
   public void onMouseRotation(MouseRotationEvent var1) {
      if (this.active && mc.field_1724 != null) {
         float var2 = this.sensitivity.getValue();
         this.cameraYaw = this.cameraYaw + var1.getCursorDeltaX() * 0.15F * var2;
         this.cameraPitch = this.cameraPitch + var1.getCursorDeltaY() * 0.15F * var2;
         this.cameraPitch = Math.max(-90.0F, Math.min(90.0F, this.cameraPitch));
         var1.cancel();
      }
   }

   @EventHandler
   public void onCamera(CameraEvent var1) {
      if (this.active && mc.field_1724 != null) {
         var1.setAngle(new Angle(this.cameraYaw, this.cameraPitch));
         var1.cancel();
      }
   }

   public boolean isFreeLookActive() {
      return this.active && this.isState();
   }
}
