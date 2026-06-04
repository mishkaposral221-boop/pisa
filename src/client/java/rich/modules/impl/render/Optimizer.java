package rich.modules.impl.render;

import net.minecraft.client.option.CloudRenderMode;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.util.c;

public class Optimizer extends ModuleStructure {
   private boolean savedEntityShadows;
   private CloudRenderMode savedClouds;
   private int savedViewDistance;
   private int savedSimulationDistance;
   private boolean savedVsync;
   private int savedMaxFps;
   private boolean savedBobView;
   private double savedEntityDistanceScaling;

   public static Optimizer getInstance() {
      return c.a(Optimizer.class);
   }

   public Optimizer() {
      super("Optimizer", "Boost FPS", ModuleCategory.UTILITIES);
   }

   @Override
   public void activate() {
      if (mc != null && mc.options != null) {
         this.savedEntityShadows = (Boolean)mc.options.getEntityShadows().getValue();
         this.savedClouds = (CloudRenderMode)mc.options.getCloudRenderMode().getValue();
         this.savedViewDistance = (Integer)mc.options.getViewDistance().getValue();
         this.savedSimulationDistance = (Integer)mc.options.getSimulationDistance().getValue();
         this.savedVsync = (Boolean)mc.options.getEnableVsync().getValue();
         this.savedMaxFps = (Integer)mc.options.getMaxFps().getValue();
         this.savedBobView = (Boolean)mc.options.getBobView().getValue();
         this.savedEntityDistanceScaling = (Double)mc.options.getEntityDistanceScaling().getValue();
         mc.options.getEntityShadows().setValue(false);
         mc.options.getCloudRenderMode().setValue(CloudRenderMode.OFF);
         mc.options.getViewDistance().setValue(6);
         mc.options.getSimulationDistance().setValue(6);
         mc.options.getEnableVsync().setValue(false);
         mc.options.getMaxFps().setValue(260);
         mc.options.getBobView().setValue(false);
         mc.options.getEntityDistanceScaling().setValue(0.5);
         mc.options.sendClientSettings();
      }
   }

   @Override
   public void deactivate() {
      if (mc != null && mc.options != null) {
         mc.options.getEntityShadows().setValue(this.savedEntityShadows);
         mc.options.getCloudRenderMode().setValue(this.savedClouds);
         mc.options.getViewDistance().setValue(this.savedViewDistance);
         mc.options.getSimulationDistance().setValue(this.savedSimulationDistance);
         mc.options.getEnableVsync().setValue(this.savedVsync);
         mc.options.getMaxFps().setValue(this.savedMaxFps);
         mc.options.getBobView().setValue(this.savedBobView);
         mc.options.getEntityDistanceScaling().setValue(this.savedEntityDistanceScaling);
         mc.options.sendClientSettings();
      }
   }
}
