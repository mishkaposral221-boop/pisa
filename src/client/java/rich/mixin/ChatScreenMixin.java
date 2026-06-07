package rich.mixin;

import net.minecraft.client.gui.Click;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.Initialization;
import rich.client.draggables.Drag;
import rich.util.render.Render2D;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
   protected ChatScreenMixin(Text var1) {
      super(var1);
   }

   @Inject(method = "render", at = @At("TAIL"))
   private void onRender(DrawContext var1, int var2, int var3, float var4, CallbackInfo var5) {
      // Render draggable HUD elements the same way as the in-game HUD overlay.
      // Without a fresh root layer / overlay reset, ChatScreen can leave render state
      // that makes custom font quads get clipped or disappear while the panel background remains.
      var1.createNewRootLayer();
      Render2D.beginOverlay();
      var1.getMatrices().pushMatrix();

      try {
         Drag.onDraw(var1, var2, var3, var4, true);
      } finally {
         var1.getMatrices().popMatrix();
         Render2D.endOverlay();
      }
   }

   @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
   private void onMouseClicked(Click var1, boolean var2, CallbackInfoReturnable<Boolean> var3) {
      int var4 = (int)var1.x();
      int var5 = (int)var1.y();
      int var6 = var1.button();
      if (Initialization.getInstance() != null
         && Initialization.getInstance().getManager() != null
         && Initialization.getInstance().getManager().getHudManager() != null
         && Initialization.getInstance().getManager().getHudManager().mouseClicked(var4, var5, var6)) {
         var3.setReturnValue(true);
      } else {
         Drag.onMouseClick(var1);
         if (Drag.isDragging()) {
            var3.setReturnValue(true);
         }
      }
   }

   public boolean mouseReleased(Click var1) {
      Drag.onMouseRelease(var1);
      return super.mouseReleased(var1);
   }

   public boolean mouseDragged(Click var1, double var2, double var4) {
      return super.mouseDragged(var1, var2, var4);
   }

   public void removed() {
      Drag.resetDragging();
      super.removed();
   }

   public void close() {
      Drag.resetDragging();
      super.close();
   }
}
