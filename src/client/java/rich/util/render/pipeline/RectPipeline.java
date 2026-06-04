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
import com.mojang.blaze3d.vertex.VertexFormat;

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

public class RectPipeline {
   private static final Identifier PIPELINE_ID = Identifier.of("rich", "pipeline/rect");
   private static final Identifier VERTEX_SHADER = Identifier.of("rich", "core/rect");
   private static final Identifier FRAGMENT_SHADER = Identifier.of("rich", "core/rect");
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private static final float FIXED_GUI_SCALE = 2.0F;
   private static final RenderPipeline PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET})
         .withLocation(PIPELINE_ID)
         .withVertexShader(VERTEX_SHADER)
         .withFragmentShader(FRAGMENT_SHADER)
         .withVertexFormat(VertexFormats.EMPTY, VertexFormat.DrawMode.TRIANGLES)
         .withUniform("RectData", UniformType.UNIFORM_BUFFER)
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final int BUFFER_SIZE = 256;
   private GpuBuffer uniformBuffer;
   private GpuBuffer dummyVertexBuffer;
   private ByteBuffer dataBuffer;
   private boolean initialized = false;
   private final int[] colors9 = new int[9];

   private void ensureInitialized() {
      if (!this.initialized) {
         this.dataBuffer = MemoryUtil.memAlloc(256);
         ByteBuffer var1 = MemoryUtil.memAlloc(4);
         var1.putInt(0);
         var1.flip();
         this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:dummy_vertex", 32, var1);
         MemoryUtil.memFree(var1);
         this.initialized = true;
      }
   }

   public void drawRect(float var1, float var2, float var3, float var4, int[] var5, float[] var6) {
      this.drawRect(var1, var2, var3, var4, var5, var6, 0.0F);
   }

   public void drawRect(float var1, float var2, float var3, float var4, int[] var5, float[] var6, float var7) {
      if (var3 <= 0.0F || var4 <= 0.0F || isFullyTransparent(var5)) {
         return;
      }

      MinecraftClient var8 = MinecraftClient.getInstance();
      if (var8.getFramebuffer() != null) {
         this.ensureInitialized();
         int var9 = var8.getWindow().getFramebufferWidth();
         int var10 = var8.getWindow().getFramebufferHeight();
         float var11 = var9 / 2.0F;
         float var12 = var10 / 2.0F;
         int[] var13 = this.convertTo9Colors(var5);
         this.prepareUniformData(var1, var2, var3, var4, var11, var12, 2.0F, var7, var13, var6);
         this.uploadAndDraw(var8);
      }
   }

   private static boolean isFullyTransparent(int[] var0) {
      for (int var1 : var0) {
         if ((var1 >>> 24) != 0) {
            return false;
         }
      }

      return true;
   }

   private int[] convertTo9Colors(int[] var1) {
      int[] var2 = this.colors9;
      if (var1.length == 1) {
         for (int var3 = 0; var3 < 9; var3++) {
            var2[var3] = var1[0];
         }
      } else if (var1.length == 4) {
         var2[0] = var1[0];
         var2[1] = this.blendColors(var1[0], var1[1]);
         var2[2] = var1[1];
         var2[3] = this.blendColors(var1[0], var1[3]);
         var2[4] = this.blendColors(var1[0], var1[1], var1[2], var1[3]);
         var2[5] = this.blendColors(var1[1], var1[2]);
         var2[6] = var1[3];
         var2[7] = this.blendColors(var1[3], var1[2]);
         var2[8] = var1[2];
      } else if (var1.length >= 9) {
         System.arraycopy(var1, 0, var2, 0, 9);
      } else {
         for (int var4 = 0; var4 < 9; var4++) {
            var2[var4] = var1[var4 % var1.length];
         }
      }

      return var2;
   }

   private int blendColors(int... var1) {
      int var2 = 0;
      int var3 = 0;
      int var4 = 0;
      int var5 = 0;

      for (int var9 : var1) {
         var5 += var9 >> 24 & 0xFF;
         var2 += var9 >> 16 & 0xFF;
         var3 += var9 >> 8 & 0xFF;
         var4 += var9 & 0xFF;
      }

      int var10 = var1.length;
      return var5 / var10 << 24 | var2 / var10 << 16 | var3 / var10 << 8 | var4 / var10;
   }

   private void prepareUniformData(float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, int[] var9, float[] var10) {
      this.dataBuffer.clear();
      this.dataBuffer.putFloat(var1);
      this.dataBuffer.putFloat(var2);
      this.dataBuffer.putFloat(var3);
      this.dataBuffer.putFloat(var4);
      this.dataBuffer.putFloat(var5);
      this.dataBuffer.putFloat(var6);
      this.dataBuffer.putFloat(var7);
      this.dataBuffer.putFloat(var8);
      this.dataBuffer.putFloat(var10[0]);
      this.dataBuffer.putFloat(var10[1]);
      this.dataBuffer.putFloat(var10[2]);
      this.dataBuffer.putFloat(var10[3]);

      for (int var11 = 0; var11 < 9; var11++) {
         int var12 = var9[var11];
         float var13 = (var12 >> 24 & 0xFF) / 255.0F;
         float var14 = (var12 >> 16 & 0xFF) / 255.0F;
         float var15 = (var12 >> 8 & 0xFF) / 255.0F;
         float var16 = (var12 & 0xFF) / 255.0F;
         this.dataBuffer.putFloat(var14);
         this.dataBuffer.putFloat(var15);
         this.dataBuffer.putFloat(var16);
         this.dataBuffer.putFloat(var13);
      }

      this.dataBuffer.flip();
   }

   private void uploadAndDraw(MinecraftClient var1) {
      int var2 = this.dataBuffer.remaining();
      if (this.uniformBuffer == null || this.uniformBuffer.size() < var2) {
         if (this.uniformBuffer != null) {
            this.uniformBuffer.close();
         }

         this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:rect_uniform", 144, var2);
      }

      CommandEncoder var3 = RenderSystem.getDevice().createCommandEncoder();
      var3.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
      GpuBufferSlice var4 = RenderSystem.getDynamicUniforms().write(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      RenderPass var5 = var3.createRenderPass(
         () -> "minecraft:rect_pass", var1.getFramebuffer().getColorAttachmentView(), OptionalInt.empty(), var1.getFramebuffer().getDepthAttachmentView(), OptionalDouble.empty()
      );

      try {
         var5.setPipeline(PIPELINE);
         var5.setVertexBuffer(0, this.dummyVertexBuffer);
         RenderSystem.bindDefaultUniforms(var5);
         var5.setUniform("DynamicTransforms", var4);
         var5.setUniform("RectData", this.uniformBuffer);
         var5.draw(0, 6);
      } catch (Throwable var9) {
         if (var5 != null) {
            try {
               var5.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }
         }

         throw var9;
      }

      if (var5 != null) {
         var5.close();
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
