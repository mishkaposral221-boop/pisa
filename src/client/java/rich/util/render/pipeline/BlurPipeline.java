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
import com.mojang.blaze3d.vertex.VertexFormat;

import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

public class BlurPipeline {
   private static final Identifier PIPELINE_ID = Identifier.of("rich", "pipeline/blur");
   private static final Identifier VERTEX_SHADER = Identifier.of("rich", "core/blur");
   private static final Identifier FRAGMENT_SHADER = Identifier.of("rich", "core/blur");
   private static final float FIXED_GUI_SCALE = 2.0F;
   private static final RenderPipeline PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET})
         .withLocation(PIPELINE_ID)
         .withVertexShader(VERTEX_SHADER)
         .withFragmentShader(FRAGMENT_SHADER)
         .withVertexFormat(VertexFormats.EMPTY, VertexFormat.DrawMode.TRIANGLES)
         .withUniform("BlurData", UniformType.UNIFORM_BUFFER)
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
   private static final int BUFFER_SIZE = 128;
   private GpuBuffer uniformBuffer;
   private GpuBuffer dummyVertexBuffer;
   private ByteBuffer dataBuffer;
   private GpuTexture copyTexture;
   private GpuTextureView copyTextureView;
   private int lastWidth = 0;
   private int lastHeight = 0;
   private boolean initialized = false;

   private int getFixedScaledWidth() {
      MinecraftClient var1 = MinecraftClient.getInstance();
      return var1 != null && var1.getWindow() != null ? (int)Math.ceil(var1.getWindow().getFramebufferWidth() / 2.0) : 960;
   }

   private int getFixedScaledHeight() {
      MinecraftClient var1 = MinecraftClient.getInstance();
      return var1 != null && var1.getWindow() != null ? (int)Math.ceil(var1.getWindow().getFramebufferHeight() / 2.0) : 540;
   }

   private void ensureInitialized() {
      if (!this.initialized) {
         this.dataBuffer = MemoryUtil.memAlloc(128);
         ByteBuffer var1 = MemoryUtil.memAlloc(4);
         var1.putInt(0);
         var1.flip();
         this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:blur_dummy_vertex", 32, var1);
         MemoryUtil.memFree(var1);
         this.initialized = true;
      }
   }

   private void ensureCopyTexture(int var1, int var2) {
      if (this.copyTexture == null || this.lastWidth != var1 || this.lastHeight != var2) {
         if (this.copyTextureView != null) {
            this.copyTextureView.close();
            this.copyTextureView = null;
         }

         if (this.copyTexture != null) {
            this.copyTexture.close();
            this.copyTexture = null;
         }

         this.copyTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:blur_copy", 5, TextureFormat.RGBA8, var1, var2, 1, 1);
         this.copyTextureView = RenderSystem.getDevice().createTextureView(this.copyTexture);
         this.lastWidth = var1;
         this.lastHeight = var2;
      }
   }

   public void drawBlur(float var1, float var2, float var3, float var4, float var5, float[] var6, int var7) {
      MinecraftClient var8 = MinecraftClient.getInstance();
      if (var8.getFramebuffer() != null) {
         if (var8.getFramebuffer().getColorAttachment() != null) {
            this.ensureInitialized();
            int var9 = var8.getFramebuffer().textureWidth;
            int var10 = var8.getFramebuffer().textureHeight;
            this.ensureCopyTexture(var9, var10);
            int var11 = this.getFixedScaledWidth();
            int var12 = this.getFixedScaledHeight();
            this.prepareUniformData(var1, var2, var3, var4, var11, var12, var9, var10, 2.0F, var5, var6, var7);
            CommandEncoder var13 = RenderSystem.getDevice().createCommandEncoder();
            var13.copyTextureToTexture(var8.getFramebuffer().getColorAttachment(), this.copyTexture, 0, 0, 0, 0, 0, var9, var10);
            var13.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
            GpuBufferSlice var14 = RenderSystem.getDynamicUniforms()
               .write(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
            GpuSampler var15 = RenderSystem.getSamplerCache().getName(FilterMode.LINEAR);
            RenderPass var16 = var13.createRenderPass(
               () -> "minecraft:blur_pass", var8.getFramebuffer().getColorAttachmentView(), OptionalInt.empty(), var8.getFramebuffer().getDepthAttachmentView(), OptionalDouble.empty()
            );

            try {
               var16.setPipeline(PIPELINE);
               var16.setVertexBuffer(0, this.dummyVertexBuffer);
               var16.bindTexture("Sampler0", this.copyTextureView, var15);
               RenderSystem.bindDefaultUniforms(var16);
               var16.setUniform("DynamicTransforms", var14);
               var16.setUniform("BlurData", this.uniformBuffer);
               var16.draw(0, 6);
            } catch (Throwable var20) {
               if (var16 != null) {
                  try {
                     var16.close();
                  } catch (Throwable var19) {
                     var20.addSuppressed(var19);
                  }
               }

               throw var20;
            }

            if (var16 != null) {
               var16.close();
            }
         }
      }
   }

   private void prepareUniformData(
      float var1, float var2, float var3, float var4, float var5, float var6, int var7, int var8, float var9, float var10, float[] var11, int var12
   ) {
      this.dataBuffer.clear();
      this.dataBuffer.putFloat(var1);
      this.dataBuffer.putFloat(var2);
      this.dataBuffer.putFloat(var3);
      this.dataBuffer.putFloat(var4);
      this.dataBuffer.putFloat(var5);
      this.dataBuffer.putFloat(var6);
      this.dataBuffer.putFloat(var9);
      this.dataBuffer.putFloat(var10);
      this.dataBuffer.putFloat(var7);
      this.dataBuffer.putFloat(var8);
      this.dataBuffer.putFloat(0.0F);
      this.dataBuffer.putFloat(0.0F);
      this.dataBuffer.putFloat(var11[0]);
      this.dataBuffer.putFloat(var11[1]);
      this.dataBuffer.putFloat(var11[2]);
      this.dataBuffer.putFloat(var11[3]);
      float var13 = (var12 >> 24 & 0xFF) / 255.0F;
      float var14 = (var12 >> 16 & 0xFF) / 255.0F;
      float var15 = (var12 >> 8 & 0xFF) / 255.0F;
      float var16 = (var12 & 0xFF) / 255.0F;
      this.dataBuffer.putFloat(var14);
      this.dataBuffer.putFloat(var15);
      this.dataBuffer.putFloat(var16);
      this.dataBuffer.putFloat(var13);
      this.dataBuffer.flip();
      int var17 = this.dataBuffer.remaining();
      if (this.uniformBuffer == null || this.uniformBuffer.size() < var17) {
         if (this.uniformBuffer != null) {
            this.uniformBuffer.close();
         }

         this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:blur_uniform", 136, var17);
      }
   }

   public void close() {
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

      if (this.copyTextureView != null) {
         this.copyTextureView.close();
         this.copyTextureView = null;
      }

      if (this.copyTexture != null) {
         this.copyTexture.close();
         this.copyTexture = null;
      }

      this.lastWidth = 0;
      this.lastHeight = 0;
      this.initialized = false;
   }
}
