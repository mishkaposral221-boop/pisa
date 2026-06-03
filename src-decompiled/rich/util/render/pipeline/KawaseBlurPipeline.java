package rich.util.render.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
import net.minecraft.class_12137;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

public class KawaseBlurPipeline {
   private static final class_2960 DOWN_PIPELINE_ID = class_2960.method_60655("rich", "pipeline/kawase_down");
   private static final class_2960 DOWN_VERTEX_SHADER = class_2960.method_60655("rich", "core/kawase_down");
   private static final class_2960 DOWN_FRAGMENT_SHADER = class_2960.method_60655("rich", "core/kawase_down");
   private static final class_2960 UP_PIPELINE_ID = class_2960.method_60655("rich", "pipeline/kawase_up");
   private static final class_2960 UP_VERTEX_SHADER = class_2960.method_60655("rich", "core/kawase_up");
   private static final class_2960 UP_FRAGMENT_SHADER = class_2960.method_60655("rich", "core/kawase_up");
   private static final RenderPipeline DOWN_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(DOWN_PIPELINE_ID)
         .withVertexShader(DOWN_VERTEX_SHADER)
         .withFragmentShader(DOWN_FRAGMENT_SHADER)
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("KawaseData", class_10789.field_60031)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final RenderPipeline UP_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(UP_PIPELINE_ID)
         .withVertexShader(UP_VERTEX_SHADER)
         .withFragmentShader(UP_FRAGMENT_SHADER)
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("KawaseData", class_10789.field_60031)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private static final int MAX_ITERATIONS = 8;
   private static final int BUFFER_SIZE = 32;
   private GpuBuffer uniformBuffer;
   private GpuBuffer dummyVertexBuffer;
   private ByteBuffer dataBuffer;
   private GpuTexture[] downTextures;
   private GpuTextureView[] downTextureViews;
   private GpuTexture[] upTextures;
   private GpuTextureView[] upTextureViews;
   private int[] downWidths;
   private int[] downHeights;
   private int[] upWidths;
   private int[] upHeights;
   private GpuTexture finalTexture;
   private GpuTextureView finalTextureView;
   private int lastWidth = 0;
   private int lastHeight = 0;
   private boolean initialized = false;

   private void ensureInitialized() {
      if (!this.initialized) {
         this.dataBuffer = MemoryUtil.memAlloc(32);
         ByteBuffer var1 = MemoryUtil.memAlloc(4);
         var1.putInt(0);
         var1.flip();
         this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:kawase_dummy_vertex", 32, var1);
         MemoryUtil.memFree(var1);
         this.downTextures = new GpuTexture[8];
         this.downTextureViews = new GpuTextureView[8];
         this.upTextures = new GpuTexture[8];
         this.upTextureViews = new GpuTextureView[8];
         this.downWidths = new int[8];
         this.downHeights = new int[8];
         this.upWidths = new int[8];
         this.upHeights = new int[8];
         this.initialized = true;
      }
   }

   private void ensureFramebuffers(int var1, int var2) {
      if (var1 != this.lastWidth || var2 != this.lastHeight) {
         this.cleanupFramebuffers();
         this.finalTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:kawase_final", 13, TextureFormat.RGBA8, var1, var2, 1, 1);
         this.finalTextureView = RenderSystem.getDevice().createTextureView(this.finalTexture);
         int var3 = var1;
         int var4 = var2;

         for (int var5 = 0; var5 < 8; var5++) {
            var3 = Math.max(1, var3 / 2);
            var4 = Math.max(1, var4 / 2);
            int var6 = var5;
            int var7 = var3;
            int var8 = var4;
            this.downWidths[var5] = var7;
            this.downHeights[var5] = var8;
            this.upWidths[var5] = var7;
            this.upHeights[var5] = var8;
            this.downTextures[var5] = RenderSystem.getDevice().createTexture(() -> "minecraft:kawase_down_" + var6, 13, TextureFormat.RGBA8, var7, var8, 1, 1);
            this.downTextureViews[var5] = RenderSystem.getDevice().createTextureView(this.downTextures[var5]);
            this.upTextures[var5] = RenderSystem.getDevice().createTexture(() -> "minecraft:kawase_up_" + var6, 13, TextureFormat.RGBA8, var7, var8, 1, 1);
            this.upTextureViews[var5] = RenderSystem.getDevice().createTextureView(this.upTextures[var5]);
         }

         this.lastWidth = var1;
         this.lastHeight = var2;
      }
   }

   private void cleanupFramebuffers() {
      if (this.finalTextureView != null) {
         this.finalTextureView.close();
         this.finalTextureView = null;
      }

      if (this.finalTexture != null) {
         this.finalTexture.close();
         this.finalTexture = null;
      }

      if (this.downTextureViews != null) {
         for (int var1 = 0; var1 < 8; var1++) {
            if (this.downTextureViews[var1] != null) {
               this.downTextureViews[var1].close();
               this.downTextureViews[var1] = null;
            }

            if (this.downTextures[var1] != null) {
               this.downTextures[var1].close();
               this.downTextures[var1] = null;
            }

            if (this.upTextureViews[var1] != null) {
               this.upTextureViews[var1].close();
               this.upTextureViews[var1] = null;
            }

            if (this.upTextures[var1] != null) {
               this.upTextures[var1].close();
               this.upTextures[var1] = null;
            }
         }
      }
   }

   public GpuTextureView blur(GpuTexture var1, GpuTextureView var2, int var3, int var4, int var5, float var6) {
      if (var1 != null && var2 != null) {
         this.ensureInitialized();
         this.ensureFramebuffers(var3, var4);
         var5 = Math.min(var5, 8);
         if (var5 < 1) {
            var5 = 1;
         }

         CommandEncoder var7 = RenderSystem.getDevice().createCommandEncoder();
         class_12137 var8 = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
         GpuTextureView var9 = var2;
         int var10 = var3;
         int var11 = var4;

         for (int var12 = 0; var12 < var5; var12++) {
            int var13 = this.downWidths[var12];
            int var14 = this.downHeights[var12];
            this.prepareUniformData(var10, var11, var6);
            int var15 = this.dataBuffer.remaining();
            if (this.uniformBuffer == null || this.uniformBuffer.size() < var15) {
               if (this.uniformBuffer != null) {
                  this.uniformBuffer.close();
               }

               this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:kawase_uniform", 136, var15);
            }

            var7.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
            GpuBufferSlice var16 = RenderSystem.getDynamicUniforms()
               .method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
            int var17 = var12;
            RenderPass var18 = var7.createRenderPass(() -> "minecraft:kawase_down_pass_" + var17, this.downTextureViews[var12], OptionalInt.empty());

            try {
               var18.setPipeline(DOWN_PIPELINE);
               var18.setVertexBuffer(0, this.dummyVertexBuffer);
               var18.bindTexture("Sampler0", var9, var8);
               RenderSystem.bindDefaultUniforms(var18);
               var18.setUniform("DynamicTransforms", var16);
               var18.setUniform("KawaseData", this.uniformBuffer);
               var18.draw(0, 6);
            } catch (Throwable var26) {
               if (var18 != null) {
                  try {
                     var18.close();
                  } catch (Throwable var23) {
                     var26.addSuppressed(var23);
                  }
               }

               throw var26;
            }

            if (var18 != null) {
               var18.close();
            }

            var9 = this.downTextureViews[var12];
            var10 = var13;
            var11 = var14;
         }

         for (int var28 = var5 - 1; var28 >= 0; var28--) {
            this.prepareUniformData(var10, var11, var6);
            var7.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
            GpuBufferSlice var30 = RenderSystem.getDynamicUniforms()
               .method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
            int var32 = var28;
            RenderPass var33 = var7.createRenderPass(() -> "minecraft:kawase_up_pass_" + var32, this.upTextureViews[var28], OptionalInt.empty());

            try {
               var33.setPipeline(UP_PIPELINE);
               var33.setVertexBuffer(0, this.dummyVertexBuffer);
               var33.bindTexture("Sampler0", var9, var8);
               RenderSystem.bindDefaultUniforms(var33);
               var33.setUniform("DynamicTransforms", var30);
               var33.setUniform("KawaseData", this.uniformBuffer);
               var33.draw(0, 6);
            } catch (Throwable var25) {
               if (var33 != null) {
                  try {
                     var33.close();
                  } catch (Throwable var22) {
                     var25.addSuppressed(var22);
                  }
               }

               throw var25;
            }

            if (var33 != null) {
               var33.close();
            }

            var9 = this.upTextureViews[var28];
            var10 = this.upWidths[var28];
            var11 = this.upHeights[var28];
         }

         this.prepareUniformData(var10, var11, var6);
         var7.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
         GpuBufferSlice var29 = RenderSystem.getDynamicUniforms()
            .method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
         RenderPass var31 = var7.createRenderPass(() -> "minecraft:kawase_final_pass", this.finalTextureView, OptionalInt.empty());

         try {
            var31.setPipeline(UP_PIPELINE);
            var31.setVertexBuffer(0, this.dummyVertexBuffer);
            var31.bindTexture("Sampler0", var9, var8);
            RenderSystem.bindDefaultUniforms(var31);
            var31.setUniform("DynamicTransforms", var29);
            var31.setUniform("KawaseData", this.uniformBuffer);
            var31.draw(0, 6);
         } catch (Throwable var24) {
            if (var31 != null) {
               try {
                  var31.close();
               } catch (Throwable var21) {
                  var24.addSuppressed(var21);
               }
            }

            throw var24;
         }

         if (var31 != null) {
            var31.close();
         }

         return this.finalTextureView;
      } else {
         return null;
      }
   }

   private void prepareUniformData(int var1, int var2, float var3) {
      this.dataBuffer.clear();
      this.dataBuffer.putFloat(var1);
      this.dataBuffer.putFloat(var2);
      this.dataBuffer.putFloat(var3);
      this.dataBuffer.putFloat(0.0F);
      this.dataBuffer.flip();
   }

   public void close() {
      this.cleanupFramebuffers();
      if (this.uniformBuffer != null) {
         this.uniformBuffer.close();
         this.uniformBuffer = null;
      }

      if (this.dummyVertexBuffer != null) {
         this.dummyVertexBuffer.close();
         this.dummyVertexBuffer = null;
      }

      if (this.dataBuffer != null) {
         MemoryUtil.memFree(this.dataBuffer);
         this.dataBuffer = null;
      }

      this.initialized = false;
   }
}
