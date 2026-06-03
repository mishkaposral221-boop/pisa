package rich.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.class_11228;
import net.minecraft.class_11246;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_408;
import net.minecraft.class_4184;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import net.minecraft.class_757;
import net.minecraft.class_758;
import net.minecraft.class_7833;
import net.minecraft.class_9779;
import net.minecraft.class_758.class_4596;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.Initialization;
import rich.client.draggables.Drag;
import rich.events.api.EventManager;
import rich.events.impl.FovEvent;
import rich.events.impl.WorldRenderEvent;
import rich.modules.impl.render.AspectRatio;
import rich.modules.impl.render.Hud;
import rich.modules.impl.render.NoNausea;
import rich.modules.impl.render.NoRender;
import rich.screens.clickgui.ClickGui;
import rich.util.render.Render3D;

@Mixin(class_757.class)
public abstract class GameRendererMixin {
   @Shadow
   @Final
   private class_310 field_4015;
   @Shadow
   @Final
   private class_4184 field_18765;
   @Shadow
   @Final
   class_11246 field_59966;
   @Shadow
   @Final
   private class_11228 field_59965;
   @Shadow
   @Final
   private class_758 field_60793;
   @Unique
   private final class_4587 matrices = new class_4587();

   @Shadow
   protected abstract void method_3186(class_4587 var1, float var2);

   @Shadow
   protected abstract void method_3198(class_4587 var1, float var2);

   @Shadow
   public abstract float method_3196(class_4184 var1, float var2, boolean var3);

   @Inject(method = "close", at = @At("RETURN"))
   private void onClose(CallbackInfo var1) {
      if (Initialization.getInstance() != null
         && Initialization.getInstance().getManager() != null
         && Initialization.getInstance().getManager().getRenderCore() != null) {
         Initialization.getInstance().getManager().getRenderCore().close();
      }
   }

   @ModifyReturnValue(method = "method_3196", at = @At("RETURN"))
   private float hookGetFovReturn(float var1) {
      FovEvent var2 = new FovEvent();
      var2.setFov((int)var1);
      EventManager.callEvent(var2);
      return var2.isCancelled() ? var2.getFov() : var1;
   }

   @Inject(method = "method_3190", at = @At("HEAD"), cancellable = true)
   private void updateCrosshairTargetHook(float var1, CallbackInfo var2) {
   }

   @Inject(
      method = "method_3188",
      at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/class_3695;method_15405(Ljava/lang/String;)V", args = "ldc=hand")
   )
   public void hookWorldRender(
      class_9779 var1,
      CallbackInfo var2,
      @Local(ordinal = 0) Matrix4f var3,
      @Local(ordinal = 1) Matrix4f var4,
      @Local(ordinal = 0) float var5,
      @Local class_4587 var6
   ) {
      if (this.field_4015.field_1687 != null && this.field_4015.field_1724 != null) {
         class_4587 var7 = new class_4587();
         var7.method_22907(class_7833.field_40714.rotationDegrees(this.field_18765.method_19329()));
         var7.method_22907(class_7833.field_40716.rotationDegrees(this.field_18765.method_19330() + 180.0F));
         Render3D.lastProjMat.set(this.field_4015.field_1773.method_22973(this.method_3196(this.field_18765, var5, true)));
         Render3D.lastModMat.set(RenderSystem.getModelViewMatrix());
         Render3D.lastWorldSpaceMatrix.set(var7.method_23760().method_23761());
         Render3D.setLastWorldSpaceEntry(var6.method_23760());
         Render3D.setLastTickDelta(var5);
         Render3D.setLastCameraPos(this.field_18765.method_71156());
         Render3D.setLastCameraRotation(new Quaternionf(this.field_18765.method_23767()));
         Matrix4fStack var8 = RenderSystem.getModelViewStack();
         var8.pushMatrix().mul(var4);
         this.matrices.method_22903();
         this.method_3198(this.matrices, this.field_18765.method_55437());
         if ((Boolean)this.field_4015.field_1690.method_42448().method_41753()) {
            this.method_3186(this.matrices, this.field_18765.method_55437());
         }

         var8.mul(this.matrices.method_23760().method_23761().invert(new Matrix4f()));
         this.matrices.method_22909();
         WorldRenderEvent var9 = new WorldRenderEvent(var6, var5);
         EventManager.callEvent(var9);
         Render3D.onWorldRender(var9);
         var8.popMatrix();
      }
   }

   @Inject(method = "method_3198", at = @At("HEAD"), cancellable = true)
   private void onTiltViewWhenHurt(class_4587 var1, float var2, CallbackInfo var3) {
      NoRender var4 = NoRender.getInstance();
      if (var4 != null && var4.isState() && var4.modeSetting.isSelected("Damage")) {
         var3.cancel();
      }
   }

   @ModifyExpressionValue(method = "method_3188", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F", ordinal = 0))
   private float onNauseaDistortion(float var1) {
      NoNausea var2 = NoNausea.getInstance();
      if (var2 != null && var2.isState()) {
         return 0.0F;
      }

      NoRender var3 = NoRender.getInstance();
      return var3 != null && var3.isState() && var3.modeSetting.isSelected("Nausea") ? 0.0F : var1;
   }

   @Inject(
      method = "method_3192",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_11228;method_70890(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V", shift = Shift.AFTER)
   )
   private void afterGuiRender(class_9779 var1, boolean var2, CallbackInfo var3) {
      if (this.field_4015.field_1687 != null && this.field_4015.field_1724 != null) {
         if (!this.isLoadingScreen(this.field_4015.field_1755)) {
            if (this.field_4015.method_18506() == null) {
               if (this.shouldRenderOnTop(this.field_4015.field_1755)) {
                  this.field_59966.method_70926();
                  int var4 = (int)this.field_4015.field_1729.method_68879(this.field_4015.method_22683());
                  int var5 = (int)this.field_4015.field_1729.method_68883(this.field_4015.method_22683());
                  float var6 = var1.method_60637(false);
                  class_332 var7 = new class_332(this.field_4015, this.field_59966, var4, var5);
                  Hud var8 = Hud.getInstance();
                  if (var8 != null && var8.isState()) {
                     boolean var9 = this.field_4015.field_1755 instanceof class_408;
                     Drag.onDraw(var7, var4, var5, var6, var9);
                  }

                  if (this.field_4015.field_1755 instanceof ClickGui var11) {
                     var11.renderOverlay(var7, var1);
                  }

                  this.field_59965.method_70890(this.field_60793.method_71109(class_4596.field_60101));
               }
            }
         }
      }
   }

   @Unique
   private boolean shouldRenderOnTop(class_437 var1) {
      if (var1 == null) {
         return true;
      } else {
         return var1 instanceof ClickGui ? true : var1 instanceof class_408;
      }
   }

   @Unique
   private boolean isLoadingScreen(class_437 var1) {
      if (var1 == null) {
         return false;
      } else {
         String var2 = var1.getClass().getSimpleName().toLowerCase();
         String var3 = var1.getClass().getName().toLowerCase();
         if (var2.contains("loading")) {
            return true;
         } else if (var2.contains("progress")) {
            return true;
         } else if (var2.contains("connecting")) {
            return true;
         } else if (var2.contains("downloading")) {
            return true;
         } else if (var2.contains("terrain")) {
            return true;
         } else if (var2.contains("generating")) {
            return true;
         } else if (var2.contains("saving")) {
            return true;
         } else if (var2.contains("reload")) {
            return true;
         } else if (var2.contains("resource")) {
            return true;
         } else {
            return var2.contains("pack") ? true : var3.contains("mojang");
         }
      }
   }

   @ModifyReturnValue(method = "method_22973", at = @At("RETURN"))
   private Matrix4f onGetBasicProjectionMatrix(Matrix4f var1) {
      AspectRatio var2 = AspectRatio.getInstance();
      if (var2 != null && var2.isState()) {
         Matrix4f var3 = new Matrix4f(var1);
         var3.m00(var3.m11() / var2.getAspectRatio());
         return var3;
      } else {
         return var1;
      }
   }
}
