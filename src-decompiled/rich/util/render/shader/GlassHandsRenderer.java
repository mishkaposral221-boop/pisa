package rich.util.render.shader;

import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.class_276;
import net.minecraft.class_310;
import rich.util.render.pipeline.GlassCompositePipeline;
import rich.util.render.pipeline.KawaseBlurPipeline;
import rich.util.render.pipeline.MaskDiffPipeline;

public class GlassHandsRenderer {
   private static GlassHandsRenderer instance;
   private final class_310 client;
   private KawaseBlurPipeline kawaseBlur;
   private GlassCompositePipeline glassComposite;
   private MaskDiffPipeline maskDiff;
   private GpuTexture sceneBeforeTexture;
   private GpuTextureView sceneBeforeTextureView;
   private GpuTexture sceneAfterTexture;
   private GpuTextureView sceneAfterTextureView;
   private GpuTexture depthBeforeTexture;
   private GpuTextureView depthBeforeTextureView;
   private GpuTexture depthAfterTexture;
   private GpuTextureView depthAfterTextureView;
   private GpuTexture maskTexture;
   private GpuTextureView maskTextureView;
   private int lastWidth = 0;
   private int lastHeight = 0;
   private boolean capturing = false;
   private boolean enabled = false;
   private boolean initialized = false;
   private float blurRadius = 6.0F;
   private int blurIterations = 4;
   private float saturation = 1.0F;
   private boolean reflect = true;
   private int tintColor = 0;
   private float tintIntensity = 0.1F;
   private float edgeGlowIntensity = 0.3F;

   public GlassHandsRenderer() {
      this.client = class_310.method_1551();
      instance = this;
   }

   public static GlassHandsRenderer getInstance() {
      if (instance == null) {
         instance = new GlassHandsRenderer();
      }

      return instance;
   }

   public static void resetInstance() {
      if (instance != null) {
         instance.close();
         instance.initialized = false;
      }
   }

   private void ensureInitialized() {
      if (!this.initialized) {
         if (this.kawaseBlur != null) {
            this.kawaseBlur.close();
         }

         if (this.glassComposite != null) {
            this.glassComposite.close();
         }

         if (this.maskDiff != null) {
            this.maskDiff.close();
         }

         this.kawaseBlur = new KawaseBlurPipeline();
         this.glassComposite = new GlassCompositePipeline();
         this.maskDiff = new MaskDiffPipeline();
         this.lastWidth = 0;
         this.lastHeight = 0;
         this.initialized = true;
      }
   }

   public void setEnabled(boolean var1) {
      this.enabled = var1;
      if (var1) {
         this.ensureInitialized();
      }
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setBlurRadius(float var1) {
      this.blurRadius = var1;
   }

   public void setBlurIterations(int var1) {
      this.blurIterations = Math.max(1, Math.min(8, var1));
   }

   public void setSaturation(float var1) {
      this.saturation = var1;
   }

   public void setReflect(boolean var1) {
      this.reflect = var1;
   }

   public void setTintColor(int var1) {
      this.tintColor = var1;
   }

   public void setTintIntensity(float var1) {
      this.tintIntensity = var1;
   }

   public void setEdgeGlowIntensity(float var1) {
      this.edgeGlowIntensity = var1;
   }

   private void ensureTextures(int var1, int var2) {
      if (var1 != this.lastWidth || var2 != this.lastHeight || this.sceneBeforeTexture == null) {
         this.cleanupTextures();
         this.sceneBeforeTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:glass_scene_before", 13, TextureFormat.RGBA8, var1, var2, 1, 1);
         this.sceneBeforeTextureView = RenderSystem.getDevice().createTextureView(this.sceneBeforeTexture);
         this.sceneAfterTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:glass_scene_after", 13, TextureFormat.RGBA8, var1, var2, 1, 1);
         this.sceneAfterTextureView = RenderSystem.getDevice().createTextureView(this.sceneAfterTexture);
         this.depthBeforeTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:glass_depth_before", 13, TextureFormat.DEPTH32, var1, var2, 1, 1);
         this.depthBeforeTextureView = RenderSystem.getDevice().createTextureView(this.depthBeforeTexture);
         this.depthAfterTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:glass_depth_after", 13, TextureFormat.DEPTH32, var1, var2, 1, 1);
         this.depthAfterTextureView = RenderSystem.getDevice().createTextureView(this.depthAfterTexture);
         this.maskTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:glass_mask", 13, TextureFormat.RGBA8, var1, var2, 1, 1);
         this.maskTextureView = RenderSystem.getDevice().createTextureView(this.maskTexture);
         this.lastWidth = var1;
         this.lastHeight = var2;
      }
   }

   private void cleanupTextures() {
      if (this.sceneBeforeTextureView != null) {
         this.sceneBeforeTextureView.close();
         this.sceneBeforeTextureView = null;
      }

      if (this.sceneBeforeTexture != null) {
         this.sceneBeforeTexture.close();
         this.sceneBeforeTexture = null;
      }

      if (this.sceneAfterTextureView != null) {
         this.sceneAfterTextureView.close();
         this.sceneAfterTextureView = null;
      }

      if (this.sceneAfterTexture != null) {
         this.sceneAfterTexture.close();
         this.sceneAfterTexture = null;
      }

      if (this.depthBeforeTextureView != null) {
         this.depthBeforeTextureView.close();
         this.depthBeforeTextureView = null;
      }

      if (this.depthBeforeTexture != null) {
         this.depthBeforeTexture.close();
         this.depthBeforeTexture = null;
      }

      if (this.depthAfterTextureView != null) {
         this.depthAfterTextureView.close();
         this.depthAfterTextureView = null;
      }

      if (this.depthAfterTexture != null) {
         this.depthAfterTexture.close();
         this.depthAfterTexture = null;
      }

      if (this.maskTextureView != null) {
         this.maskTextureView.close();
         this.maskTextureView = null;
      }

      if (this.maskTexture != null) {
         this.maskTexture.close();
         this.maskTexture = null;
      }
   }

   public void captureSceneBeforeHands() {
      if (this.enabled) {
         this.ensureInitialized();
         class_276 var1 = this.client.method_1522();
         if (var1 != null && var1.method_30277() != null) {
            int var2 = var1.field_1482;
            int var3 = var1.field_1481;
            this.ensureTextures(var2, var3);
            CommandEncoder var4 = RenderSystem.getDevice().createCommandEncoder();
            var4.copyTextureToTexture(var1.method_30277(), this.sceneBeforeTexture, 0, 0, 0, 0, 0, var2, var3);
            if (var1.method_30278() != null) {
               var4.copyTextureToTexture(var1.method_30278(), this.depthBeforeTexture, 0, 0, 0, 0, 0, var2, var3);
            }

            this.capturing = true;
         }
      }
   }

   public void captureSceneAfterHands() {
      if (this.enabled && this.capturing) {
         class_276 var1 = this.client.method_1522();
         if (var1 != null && var1.method_30277() != null) {
            CommandEncoder var2 = RenderSystem.getDevice().createCommandEncoder();
            var2.copyTextureToTexture(var1.method_30277(), this.sceneAfterTexture, 0, 0, 0, 0, 0, this.lastWidth, this.lastHeight);
            if (var1.method_30278() != null) {
               var2.copyTextureToTexture(var1.method_30278(), this.depthAfterTexture, 0, 0, 0, 0, 0, this.lastWidth, this.lastHeight);
            }
         }
      }
   }

   public void renderGlassEffect() {
      if (this.enabled && this.capturing) {
         class_276 var1 = this.client.method_1522();
         if (var1 != null && var1.method_30277() != null) {
            this.maskDiff
               .createMask(
                  this.maskTextureView,
                  this.sceneBeforeTextureView,
                  this.sceneAfterTextureView,
                  this.depthBeforeTextureView,
                  this.depthAfterTextureView,
                  this.lastWidth,
                  this.lastHeight
               );
            GpuTextureView var2 = this.kawaseBlur
               .blur(this.sceneBeforeTexture, this.sceneBeforeTextureView, this.lastWidth, this.lastHeight, this.blurIterations, this.blurRadius);
            if (var2 == null) {
               this.capturing = false;
            } else {
               this.glassComposite
                  .composite(
                     var1.method_71639(),
                     this.sceneBeforeTextureView,
                     var2,
                     this.maskTextureView,
                     this.lastWidth,
                     this.lastHeight,
                     this.saturation,
                     this.reflect,
                     this.tintColor,
                     this.tintIntensity,
                     this.edgeGlowIntensity
                  );
               this.capturing = false;
            }
         } else {
            this.capturing = false;
         }
      }
   }

   public boolean isCapturing() {
      return this.capturing;
   }

   public void invalidate() {
      this.cleanupTextures();
      if (this.kawaseBlur != null) {
         this.kawaseBlur.close();
      }

      if (this.glassComposite != null) {
         this.glassComposite.close();
      }

      if (this.maskDiff != null) {
         this.maskDiff.close();
      }

      this.kawaseBlur = null;
      this.glassComposite = null;
      this.maskDiff = null;
      this.lastWidth = 0;
      this.lastHeight = 0;
      this.initialized = false;
      this.capturing = false;
   }

   public void close() {
      this.cleanupTextures();
      if (this.kawaseBlur != null) {
         this.kawaseBlur.close();
         this.kawaseBlur = null;
      }

      if (this.glassComposite != null) {
         this.glassComposite.close();
         this.glassComposite = null;
      }

      if (this.maskDiff != null) {
         this.maskDiff.close();
         this.maskDiff = null;
      }

      this.lastWidth = 0;
      this.lastHeight = 0;
      this.initialized = false;
   }
}
