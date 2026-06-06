package rich.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.Camera;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogRenderer.FogType;
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
import rich.util.PerfStats;
import rich.util.render.Render3D;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
   @Shadow
   @Final
   private MinecraftClient client;
   @Shadow
   @Final
   private Camera camera;
   @Shadow
   @Final
   GuiRenderState guiState;
   @Shadow
   @Final
   private GuiRenderer guiRenderer;
   @Shadow
   @Final
   private FogRenderer fogRenderer;
   @Unique
   private final MatrixStack matrices = new MatrixStack();

   @Shadow
   protected abstract void bobView(MatrixStack var1, float var2);

   @Shadow
   protected abstract void tiltViewWhenHurt(MatrixStack var1, float var2);

   @Shadow
   public abstract float getFov(Camera var1, float var2, boolean var3);

   @Inject(method = "close", at = @At("RETURN"))
   private void onClose(CallbackInfo var1) {
      if (Initialization.getInstance() != null
         && Initialization.getInstance().getManager() != null
         && Initialization.getInstance().getManager().getRenderCore() != null) {
         Initialization.getInstance().getManager().getRenderCore().close();
      }
   }

   @ModifyReturnValue(method = "getFov", at = @At("RETURN"))
   private float hookGetFovReturn(float var1) {
      FovEvent var2 = new FovEvent();
      var2.setFov((int)var1);
      EventManager.callEvent(var2);
      return var2.isCancelled() ? var2.getFov() : var1;
   }

   @Inject(method = "updateCrosshairTarget", at = @At("HEAD"), cancellable = true)
   private void updateCrosshairTargetHook(float var1, CallbackInfo var2) {
   }

   @Inject(
      method = "renderWorld",
      at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=hand")
   )
   public void hookWorldRender(
      RenderTickCounter var1,
      CallbackInfo var2,
      @Local(ordinal = 0) Matrix4f var3,
      @Local(ordinal = 1) Matrix4f var4,
      @Local(ordinal = 0) float var5,
      @Local MatrixStack var6
   ) {
      if (this.client.world != null && this.client.player != null) {
         long t0 = PerfStats.begin();

         long t = PerfStats.begin();
         MatrixStack var7 = new MatrixStack();
         var7.multiply(RotationAxis.POSITIVE_X.rotationDegrees(this.camera.getPitch()));
         var7.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(this.camera.getYaw() + 180.0F));
         PerfStats.end("world:camMatrix", t);

         t = PerfStats.begin();
         Render3D.lastProjMat.set(this.client.gameRenderer.getBasicProjectionMatrix(this.getFov(this.camera, var5, true)));
         Render3D.lastModMat.set(RenderSystem.getModelViewMatrix());
         Render3D.lastWorldSpaceMatrix.set(var7.peek().getPositionMatrix());
         Render3D.setLastWorldSpaceEntry(var6.peek());
         Render3D.setLastTickDelta(var5);
         Render3D.setLastCameraPos(this.camera.getCameraPos());
         Render3D.setLastCameraRotation(new Quaternionf(this.camera.getRotation()));
         PerfStats.end("world:render3dState", t);

         t = PerfStats.begin();
         Matrix4fStack var8 = RenderSystem.getModelViewStack();
         var8.pushMatrix().mul(var4);
         this.matrices.push();
         this.tiltViewWhenHurt(this.matrices, this.camera.getLastTickProgress());
         if ((Boolean)this.client.options.getBobView().getValue()) {
            this.bobView(this.matrices, this.camera.getLastTickProgress());
         }

         var8.mul(this.matrices.peek().getPositionMatrix().invert(new Matrix4f()));
         this.matrices.pop();
         PerfStats.end("world:viewTransforms", t);

         t = PerfStats.begin();
         WorldRenderEvent var9 = new WorldRenderEvent(var6, var5);
         EventManager.callEvent(var9);
         Render3D.onWorldRender(var9);
         PerfStats.end("world:events+render3d", t);

         var8.popMatrix();

         PerfStats.end("world:total", t0);
         PerfStats.tickAndMaybeDump();
      }
   }

   @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
   private void onTiltViewWhenHurt(MatrixStack var1, float var2, CallbackInfo var3) {
      NoRender var4 = NoRender.getInstance();
      if (var4 != null && var4.isState() && var4.modeSetting.isSelected("Damage")) {
         var3.cancel();
      }
   }

   @ModifyExpressionValue(method = "renderWorld", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F", ordinal = 0))
   private float onNauseaDistortion(float var1) {
      NoNausea var2 = NoNausea.getInstance();
      if (var2 != null && var2.isState()) {
         return 0.0F;
      }

      NoRender var3 = NoRender.getInstance();
      return var3 != null && var3.isState() && var3.modeSetting.isSelected("Nausea") ? 0.0F : var1;
   }

   @Inject(
      method = "render",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V", shift = Shift.AFTER)
   )
   private void afterGuiRender(RenderTickCounter var1, boolean var2, CallbackInfo var3) {
      if (this.client.world != null && this.client.player != null) {
         if (!this.isLoadingScreen(this.client.currentScreen)) {
            if (this.client.getOverlay() == null) {
               if (this.shouldRenderOnTop(this.client.currentScreen)) {
                  long t0 = PerfStats.begin();

                  long t = PerfStats.begin();
                  this.guiState.clear();
                  int var4 = (int)this.client.mouse.getScaledX(this.client.getWindow());
                  int var5 = (int)this.client.mouse.getScaledY(this.client.getWindow());
                  float var6 = var1.getTickProgress(false);
                  DrawContext var7 = new DrawContext(this.client, this.guiState, var4, var5);
                  PerfStats.end("gui:setup", t);

                  Hud var8 = Hud.getInstance();
                  if (var8 != null && var8.isState()) {
                     boolean var9 = this.client.currentScreen instanceof ChatScreen;
                     t = PerfStats.begin();
                     Drag.onDraw(var7, var4, var5, var6, var9);
                     PerfStats.end("gui:dragOnDraw", t);
                  }

                  if (this.client.currentScreen instanceof ClickGui var11) {
                     t = PerfStats.begin();
                     var11.renderOverlay(var7, var1);
                     PerfStats.end("gui:clickGuiOverlay", t);
                  }

                  t = PerfStats.begin();
                  this.guiRenderer.render(this.fogRenderer.getFogBuffer(net.minecraft.client.render.fog.FogRenderer.FogType.NONE));
                  PerfStats.end("gui:rendererRender", t);

                  PerfStats.end("gui:total", t0);
                  PerfStats.tickAndMaybeDump();
               }
            }
         }
      }
   }

   @Unique
   private boolean shouldRenderOnTop(Screen var1) {
      if (var1 == null) {
         return true;
      } else {
         return var1 instanceof ClickGui ? true : var1 instanceof ChatScreen;
      }
   }

   @Unique
   private boolean isLoadingScreen(Screen var1) {
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

   @ModifyReturnValue(method = "getBasicProjectionMatrix", at = @At("RETURN"))
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
