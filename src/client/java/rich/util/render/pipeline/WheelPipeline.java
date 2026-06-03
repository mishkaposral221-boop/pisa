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
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

public class WheelPipeline {
   private static final float FIXED_GUI_SCALE = 2.0F;
   private static final int UNIFORM_SIZE = 64;
   private static final RenderPipeline PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET})
         .withLocation(Identifier.of("rich", "pipeline/wheel_segment"))
         .withVertexShader(Identifier.of("rich", "core/wheel_segment"))
         .withFragmentShader(Identifier.of("rich", "core/wheel_segment"))
         .withVertexFormat(VertexFormats.EMPTY, VertexFormat.DrawMode.TRIANGLES)
         .withUniform("WheelSegData", UniformType.UNIFORM_BUFFER)
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
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
         this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "rich:wheel_dummy_vertex", 32, var1);
         MemoryUtil.memFree(var1);
         this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "rich:wheel_uniform", 136, 64L);
         this.initialized = true;
      }
   }

   public void drawSegment(float var1, float var2, float var3, float var4, float var5, float var6, int var7) {
      MinecraftClient var8 = MinecraftClient.getInstance();
      if (var8.getFramebuffer() != null) {
         this.ensureInitialized();
         int var9 = var8.getWindow().getFramebufferWidth();
         int var10 = var8.getWindow().getFramebufferHeight();
         float var11 = (float)var9 / var8.getWindow().getScaledWidth();
         float var12 = (float)var10 / var8.getWindow().getScaledHeight();
         float var13 = var1 * var11;
         float var14 = var2 * var12;
         float var15 = var3 * var11;
         float var16 = var4 * var11;
         float var17 = (var7 >> 16 & 0xFF) / 255.0F;
         float var18 = (var7 >> 8 & 0xFF) / 255.0F;
         float var19 = (var7 & 0xFF) / 255.0F;
         float var20 = (var7 >> 24 & 0xFF) / 255.0F;
         this.dataBuffer.clear();
         this.dataBuffer.putFloat(var9);
         this.dataBuffer.putFloat(var10);
         this.dataBuffer.putFloat(var11);
         this.dataBuffer.putFloat(0.0F);
         this.dataBuffer.putFloat(var13);
         this.dataBuffer.putFloat(var14);
         this.dataBuffer.putFloat(var15);
         this.dataBuffer.putFloat(var16);
         this.dataBuffer.putFloat(var5);
         this.dataBuffer.putFloat(var6);
         this.dataBuffer.putFloat(0.0F);
         this.dataBuffer.putFloat(0.0F);
         this.dataBuffer.putFloat(var17);
         this.dataBuffer.putFloat(var18);
         this.dataBuffer.putFloat(var19);
         this.dataBuffer.putFloat(var20);
         this.dataBuffer.flip();
         CommandEncoder var21 = RenderSystem.getDevice().createCommandEncoder();
         var21.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
         GpuBufferSlice var22 = RenderSystem.getDynamicUniforms()
            .write(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
         RenderPass var23 = var21.createRenderPass(
            () -> "rich:wheel_segment", var8.getFramebuffer().getColorAttachmentView(), OptionalInt.empty(), var8.getFramebuffer().getDepthAttachmentView(), OptionalDouble.empty()
         );

         try {
            var23.setPipeline(PIPELINE);
            var23.setVertexBuffer(0, this.dummyVertexBuffer);
            RenderSystem.bindDefaultUniforms(var23);
            var23.setUniform("DynamicTransforms", var22);
            var23.setUniform("WheelSegData", this.uniformBuffer);
            var23.draw(0, 6);
         } catch (Throwable var27) {
            if (var23 != null) {
               try {
                  var23.close();
               } catch (Throwable var26) {
                  var27.addSuppressed(var26);
               }
            }

            throw var27;
         }

         if (var23 != null) {
            var23.close();
         }
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

      this.initialized = false;
   }
}
