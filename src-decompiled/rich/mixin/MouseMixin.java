package rich.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.class_11910;
import net.minecraft.class_310;
import net.minecraft.class_312;
import net.minecraft.class_746;
import net.minecraft.class_3675.class_307;
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

@Mixin(class_312.class)
public abstract class MouseMixin {
   @Final
   @Shadow
   private class_310 field_1779;
   @Shadow
   private boolean field_1783;
   @Shadow
   private double field_1795;
   @Shadow
   private double field_1794;
   @Shadow
   private double field_1789;
   @Shadow
   private double field_1787;
   @Shadow
   private boolean field_1784;

   @Inject(method = "method_1601", at = @At("HEAD"), cancellable = true)
   public void onMouseButtonHook(long var1, class_11910 var3, int var4, CallbackInfo var5) {
      if (this.field_1779.field_1755 == null && var3.comp_4801() != -1 && var1 == this.field_1779.method_22683().method_4490()) {
         if (var3.comp_4801() == 0 && var4 == 1) {
            UpdateChecker.UpdateInfo var6 = UpdateChecker.getInstance().getPendingUpdate();
            UpdateToast var7 = UpdateToast.getIngameInstance();
            if (var7 != null && var7.isVisible() && var6 != null) {
               float var8 = (float)(this.field_1795 * this.field_1779.method_22683().method_4486() / this.field_1779.method_22683().method_4480());
               float var9 = (float)(this.field_1794 * this.field_1779.method_22683().method_4502() / this.field_1779.method_22683().method_4507());
               if (var7.mouseClicked(var8, var9, this.field_1779.method_22683().method_4486(), this.field_1779.method_22683().method_4502(), var6)) {
                  var5.cancel();
                  return;
               }
            }
         }

         KeyEvent var10 = new KeyEvent(this.field_1779.field_1755, class_307.field_1672, var3.comp_4801(), var4);
         EventManager.callEvent(var10);
         if (var10.isCancelled()) {
            var5.cancel();
         }
      }
   }

   @Inject(
      method = "method_1598",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_746;method_31548()Lnet/minecraft/class_1661;"),
      cancellable = true
   )
   public void onMouseScrollHook(long var1, double var3, double var5, CallbackInfo var7) {
      HotBarScrollEvent var8 = new HotBarScrollEvent(var3, var5);
      EventManager.callEvent(var8);
      if (var8.isCancelled()) {
         var7.cancel();
      }
   }

   @Inject(method = "method_1612", at = @At("HEAD"), cancellable = true)
   private void onLockCursor(CallbackInfo var1) {
      if (this.field_1779.field_1755 instanceof ClickGui var2 && var2.isClosing()) {
         this.field_1783 = true;
         this.field_1789 = 0.0;
         this.field_1787 = 0.0;
         this.field_1795 = this.field_1779.method_22683().method_4480() / 2.0;
         this.field_1794 = this.field_1779.method_22683().method_4507() / 2.0;
         this.field_1784 = true;
         var1.cancel();
      }
   }

   @Inject(method = "method_1606", at = @At("HEAD"))
   private void onUpdateMouse(double var1, CallbackInfo var3) {
      FovEvent var4 = new FovEvent();
      EventManager.callEvent(var4);
      if (var4.isCancelled()) {
         double var5 = (double)var4.getFov() / ((Integer)this.field_1779.field_1690.method_41808().method_41753()).intValue();
         this.field_1789 *= var5;
         this.field_1787 *= var5;
      }
   }

   @WrapWithCondition(method = "method_1606", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_746;method_5872(DD)V"), require = 1, allow = 1)
   private boolean modifyMouseRotationInput(class_746 var1, double var2, double var4) {
      MouseRotationEvent var6 = new MouseRotationEvent((float)var2, (float)var4);
      EventManager.callEvent(var6);
      if (var6.isCancelled()) {
         return false;
      }

      var1.method_5872(var6.getCursorDeltaX(), var6.getCursorDeltaY());
      return false;
   }

   @Inject(method = "method_55793", at = @At("HEAD"))
   private void onTick(CallbackInfo var1) {
      if (this.field_1779.field_1755 instanceof ClickGui var2 && var2.isClosing() && !this.field_1783) {
         this.field_1783 = true;
         this.field_1789 = 0.0;
         this.field_1787 = 0.0;
         this.field_1784 = true;
      }
   }
}
