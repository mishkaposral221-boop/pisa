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

public class MaskDiffPipeline {
   private static final class_2960 PIPELINE_ID = class_2960.method_60655("rich", "pipeline/mask_diff");
   private static final class_2960 VERTEX_SHADER = class_2960.method_60655("rich", "core/mask_diff");
   private static final class_2960 FRAGMENT_SHADER = class_2960.method_60655("rich", "core/mask_diff");
   private static final BlendFunction REPLACE_BLEND = new BlendFunction(SourceFactor.ONE, DestFactor.ZERO, SourceFactor.ONE, DestFactor.ZERO);
   private static final RenderPipeline PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(PIPELINE_ID)
         .withVertexShader(VERTEX_SHADER)
         .withFragmentShader(FRAGMENT_SHADER)
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("MaskData", class_10789.field_60031)
         .withSampler("BeforeSampler")
         .withSampler("AfterSampler")
         .withSampler("DepthBeforeSampler")
         .withSampler("DepthAfterSampler")
         .withBlend(REPLACE_BLEND)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private static final int BUFFER_SIZE = 16;
   private GpuBuffer uniformBuffer;
   private GpuBuffer dummyVertexBuffer;
   private ByteBuffer dataBuffer;
   private boolean initialized = false;

   private void ensureInitialized() {
      if (!this.initialized) {
         this.dataBuffer = MemoryUtil.memAlloc(16);
         ByteBuffer var1 = MemoryUtil.memAlloc(4);
         var1.putInt(0);
         var1.flip();
         this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:mask_diff_dummy_vertex", 32, var1);
         MemoryUtil.memFree(var1);
         this.initialized = true;
      }
   }

   public void createMask(GpuTextureView var1, GpuTextureView var2, GpuTextureView var3, GpuTextureView var4, GpuTextureView var5, int var6, int var7) {
      this.ensureInitialized();
      CommandEncoder var8 = RenderSystem.getDevice().createCommandEncoder();
      class_12137 var9 = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
      class_12137 var10 = RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST);
      this.prepareUniformData(var6, var7);
      int var11 = this.dataBuffer.remaining();
      if (this.uniformBuffer == null || this.uniformBuffer.size() < var11) {
         if (this.uniformBuffer != null) {
            this.uniformBuffer.close();
         }

         this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:mask_diff_uniform", 136, var11);
      }

      var8.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
      GpuBufferSlice var12 = RenderSystem.getDynamicUniforms().method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      RenderPass var13 = var8.createRenderPass(() -> "minecraft:mask_diff_pass", var1, OptionalInt.of(0));

      try {
         var13.setPipeline(PIPELINE);
         var13.setVertexBuffer(0, this.dummyVertexBuffer);
         var13.bindTexture("BeforeSampler", var2, var9);
         var13.bindTexture("AfterSampler", var3, var9);
         var13.bindTexture("DepthBeforeSampler", var4, var10);
         var13.bindTexture("DepthAfterSampler", var5, var10);
         RenderSystem.bindDefaultUniforms(var13);
         var13.setUniform("DynamicTransforms", var12);
         var13.setUniform("MaskData", this.uniformBuffer);
         var13.draw(0, 6);
      } catch (Throwable var17) {
         if (var13 != null) {
            try {
               var13.close();
            } catch (Throwable var16) {
               var17.addSuppressed(var16);
            }
         }

         throw var17;
      }

      if (var13 != null) {
         var13.close();
      }
   }

   private void prepareUniformData(int var1, int var2) {
      this.dataBuffer.clear();
      this.dataBuffer.putFloat(var1);
      this.dataBuffer.putFloat(var2);
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
