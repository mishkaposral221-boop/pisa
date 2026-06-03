package rich.modules.impl.render;

import net.minecraft.class_4063;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.util.c;

public class Optimizer extends ModuleStructure {
   private boolean savedEntityShadows;
   private class_4063 savedClouds;
   private int savedViewDistance;
   private int savedSimulationDistance;
   private boolean savedVsync;
   private int savedMaxFps;
   private boolean savedBobView;

   public static Optimizer getInstance() {
      return c.a(Optimizer.class);
   }

   public Optimizer() {
      super("Optimizer", "Boost FPS", ModuleCategory.UTILITIES);
   }

   @Override
   public void activate() {
      if (mc != null && mc.field_1690 != null) {
         this.savedEntityShadows = (Boolean)mc.field_1690.method_42435().method_41753();
         this.savedClouds = (class_4063)mc.field_1690.method_42528().method_41753();
         this.savedViewDistance = (Integer)mc.field_1690.method_42503().method_41753();
         this.savedSimulationDistance = (Integer)mc.field_1690.method_42510().method_41753();
         this.savedVsync = (Boolean)mc.field_1690.method_42433().method_41753();
         this.savedMaxFps = (Integer)mc.field_1690.method_42524().method_41753();
         this.savedBobView = (Boolean)mc.field_1690.method_42448().method_41753();
         mc.field_1690.method_42435().method_41748(false);
         mc.field_1690.method_42528().method_41748(class_4063.field_18162);
         mc.field_1690.method_42503().method_41748(6);
         mc.field_1690.method_42510().method_41748(6);
         mc.field_1690.method_42433().method_41748(false);
         mc.field_1690.method_42524().method_41748(260);
         mc.field_1690.method_42448().method_41748(false);
         mc.field_1690.method_1643();
      }
   }

   @Override
   public void deactivate() {
      if (mc != null && mc.field_1690 != null) {
         mc.field_1690.method_42435().method_41748(this.savedEntityShadows);
         mc.field_1690.method_42528().method_41748(this.savedClouds);
         mc.field_1690.method_42503().method_41748(this.savedViewDistance);
         mc.field_1690.method_42510().method_41748(this.savedSimulationDistance);
         mc.field_1690.method_42433().method_41748(this.savedVsync);
         mc.field_1690.method_42524().method_41748(this.savedMaxFps);
         mc.field_1690.method_42448().method_41748(this.savedBobView);
         mc.field_1690.method_1643();
      }
   }
}
