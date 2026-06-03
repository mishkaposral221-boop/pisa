package rich.util.render.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;

import java.nio.ByteBuffer;
import java.util.OptionalInt;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

public class GlassCompositePipeline {
   private static final Identifier PIPELINE_ID = Identifier.of("rich", "pipeline/glass_composite");
   private static final Identifier VERTEX_SHADER = Identifier.of("rich", "core/glass_composite");
   private static final Identifier FRAGMENT_SHADER = Identifier.of("rich", "core/glass_composite");
   private static final BlendFunction REPLACE_BLEND = new BlendFunction(SourceFactor.ONE, DestFactor.ZERO, SourceFactor.ONE, DestFactor.ZERO);
   private static final RenderPipeline PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET})
         .withLocation(PIPELINE_ID)
         .withVertexShader(VERTEX_SHADER)
         .withFragmentShader(FRAGMENT_SHADER)
         .withVertexFormat(VertexFormats.EMPTY, VertexFormat.DrawMode.TRIANGLES)
         .withUniform("GlassData", UniformType.UNIFORM_BUFFER)
         .withSampler("SceneSampler")
         .withSampler("BlurSampler")
         .withSampler("MaskSampler")
         .withBlend(REPLACE_BLEND)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private static final int BUFFER_SIZE = 64;
   private GpuBuffer uniformBuffer;
   private GpuBuffer dummyVertexBuffer;
   private ByteBuffer dataBuffer;
   private boolean initialized = false;

   private void ensureInitialized() {
      if (!this.initialized) {
         this.dataBuffer = MemoryUtil.memAlloc(64);
         ByteBuffer var1 = MemoryUtil.memAlloc(4);
         var1.putInt(0);
         var1.flip();
         this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:glass_composite_dummy_vertex", 32, var1);
         MemoryUtil.memFree(var1);
         this.initialized = true;
      }
   }

   public void composite(
      GpuTextureView var1,
      GpuTextureView var2,
      GpuTextureView var3,
      GpuTextureView var4,
      int var5,
      int var6,
      float var7,
      boolean var8,
      int var9,
      float var10,
      float var11
   ) {
      this.ensureInitialized();
      this.prepareUniformData(var5, var6, var7, var8, var9, var10, var11);
      int var12 = this.dataBuffer.remaining();
      if (this.uniformBuffer == null || this.uniformBuffer.size() < var12) {
         if (this.uniformBuffer != null) {
            this.uniformBuffer.close();
         }

         this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:glass_composite_uniform", 136, var12);
      }

      CommandEncoder var13 = RenderSystem.getDevice().createCommandEncoder();
      var13.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
      GpuBufferSlice var14 = RenderSystem.getDynamicUniforms().write(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      GpuSampler var15 = RenderSystem.getSamplerCache().getName(FilterMode.LINEAR);
      RenderPass var16 = var13.createRenderPass(() -> "minecraft:glass_composite_pass", var1, OptionalInt.empty());

      try {
         var16.setPipeline(PIPELINE);
         var16.setVertexBuffer(0, this.dummyVertexBuffer);
         var16.bindTexture("SceneSampler", var2, var15);
         var16.bindTexture("BlurSampler", var3, var15);
         var16.bindTexture("MaskSampler", var4, var15);
         RenderSystem.bindDefaultUniforms(var16);
         var16.setUniform("DynamicTransforms", var14);
         var16.setUniform("GlassData", this.uniformBuffer);
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

   private void prepareUniformData(int var1, int var2, float var3, boolean var4, int var5, float var6, float var7) {
      this.dataBuffer.clear();
      this.dataBuffer.putFloat(var1);
      this.dataBuffer.putFloat(var2);
      this.dataBuffer.putFloat(var3);
      this.dataBuffer.putFloat(var4 ? 1.0F : 0.0F);
      float var8 = (var5 >> 24 & 0xFF) / 255.0F;
      float var9 = (var5 >> 16 & 0xFF) / 255.0F;
      float var10 = (var5 >> 8 & 0xFF) / 255.0F;
      float var11 = (var5 & 0xFF) / 255.0F;
      this.dataBuffer.putFloat(var9);
      this.dataBuffer.putFloat(var10);
      this.dataBuffer.putFloat(var11);
      this.dataBuffer.putFloat(var8);
      this.dataBuffer.putFloat(var6);
      this.dataBuffer.putFloat(var7);
      this.dataBuffer.putFloat(0.0F);
      this.dataBuffer.putFloat(0.0F);
      this.dataBuffer.flip();
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

      this.initialized = false;
   }
}
