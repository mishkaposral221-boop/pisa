package rich.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import rich.util.profiler.FrameProfiler;

@Mixin(InGameHud.class)
public abstract class InGameHudRootLayerProfilingMixin {
   @Unique
   private void richBeginRootLayer() {
      FrameProfiler profiler = FrameProfiler.getInstance();
      if (profiler.isEnabled()) {
         profiler.begin("Minecraft/InGameHud/createNewRootLayer");
      }
   }

   @Unique
   private void richEndRootLayer() {
      FrameProfiler profiler = FrameProfiler.getInstance();
      if (profiler.isEnabled()) {
         profiler.end();
      }
   }

   @WrapOperation(
      method = {
         "render",
         "renderSleepOverlay",
         "renderOverlayMessage",
         "renderTitleAndSubtitle",
         "renderChat",
         "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V",
         "renderPlayerList",
         "renderDemoTimer"
      },
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;createNewRootLayer()V"),
      require = 0
   )
   private void richProfileCreateNewRootLayer(DrawContext context, Operation<Void> original) {
      this.richBeginRootLayer();
      try {
         original.call(new Object[]{context});
      } finally {
         this.richEndRootLayer();
      }
   }
}
