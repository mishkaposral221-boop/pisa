package rich.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil.Type;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.events.api.EventManager;
import rich.events.impl.FovEvent;
import rich.events.impl.HotBarScrollEvent;
import rich.events.impl.KeyEvent;
import rich.events.impl.MouseRotationEvent;
import rich.screens.clickgui.ClickGui;
import rich.update.UpdateChecker;
import rich.update.UpdateToast;

@Mixin(Mouse.class)
public abstract class MouseMixin {
   @Final
   @Shadow
   private MinecraftClient client;
   @Shadow
   private boolean cursorLocked;
   @Shadow
   private double x;
   @Shadow
   private double y;
   @Shadow
   private double cursorDeltaX;
   @Shadow
   private double cursorDeltaY;
   @Shadow
   private boolean hasResolutionChanged;

   @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
   public void onMouseButtonHook(long var1, MouseInput var3, int var4, CallbackInfo var5) {
      if (this.client.currentScreen == null && var3.button() != -1 && var1 == this.client.getWindow().getHandle()) {
         if (var3.button() == 0 && var4 == 1) {
            UpdateChecker.UpdateInfo var6 = UpdateChecker.getInstance().getPendingUpdate();
            UpdateToast var7 = UpdateToast.getIngameInstance();
            if (var7 != null && var7.isVisible() && var6 != null) {
               float var8 = (float)(this.x * this.client.getWindow().getScaledWidth() / this.client.getWindow().getWidth());
               float var9 = (float)(this.y * this.client.getWindow().getScaledHeight() / this.client.getWindow().getHeight());
               if (var7.mouseClicked(var8, var9, this.client.getWindow().getScaledWidth(), this.client.getWindow().getScaledHeight(), var6)) {
                  var5.cancel();
                  return;
               }
            }
         }

         KeyEvent var10 = new KeyEvent(this.client.currentScreen, net.minecraft.client.util.InputUtil.Type.MOUSE, var3.button(), var4);
         EventManager.callEvent(var10);
         if (var10.isCancelled()) {
            var5.cancel();
         }
      }
   }

   @Inject(
      method = "onMouseScroll",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getInventory()Lnet/minecraft/entity/player/PlayerInventory;"),
      cancellable = true
   )
   public void onMouseScrollHook(long var1, double var3, double var5, CallbackInfo var7) {
      HotBarScrollEvent var8 = new HotBarScrollEvent(var3, var5);
      EventManager.callEvent(var8);
      if (var8.isCancelled()) {
         var7.cancel();
      }
   }

   @Inject(method = "lockCursor", at = @At("HEAD"), cancellable = true)
   private void onLockCursor(CallbackInfo var1) {
      if (this.client.currentScreen instanceof ClickGui var2 && var2.isClosing()) {
         this.cursorLocked = true;
         this.cursorDeltaX = 0.0;
         this.cursorDeltaY = 0.0;
         this.x = this.client.getWindow().getWidth() / 2.0;
         this.y = this.client.getWindow().getHeight() / 2.0;
         this.hasResolutionChanged = true;
         var1.cancel();
      }
   }

   @Inject(method = "updateMouse", at = @At("HEAD"))
   private void onUpdateMouse(double var1, CallbackInfo var3) {
      FovEvent var4 = new FovEvent();
      EventManager.callEvent(var4);
      if (var4.isCancelled()) {
         double var5 = (double)var4.getFov() / ((Integer)this.client.options.getFov().getValue()).intValue();
         this.cursorDeltaX *= var5;
         this.cursorDeltaY *= var5;
      }
   }

   @WrapWithCondition(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"), require = 1, allow = 1)
   private boolean modifyMouseRotationInput(ClientPlayerEntity var1, double var2, double var4) {
      MouseRotationEvent var6 = new MouseRotationEvent((float)var2, (float)var4);
      EventManager.callEvent(var6);
      if (var6.isCancelled()) {
         return false;
      }

      var1.changeLookDirection(var6.getCursorDeltaX(), var6.getCursorDeltaY());
      return false;
   }

   @Inject(method = "tick", at = @At("HEAD"))
   private void onTick(CallbackInfo var1) {
      if (this.client.currentScreen instanceof ClickGui var2 && var2.isClosing() && !this.cursorLocked) {
         this.cursorLocked = true;
         this.cursorDeltaX = 0.0;
         this.cursorDeltaY = 0.0;
         this.hasResolutionChanged = true;
      }
   }
}
