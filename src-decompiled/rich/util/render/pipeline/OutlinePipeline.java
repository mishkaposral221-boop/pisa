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
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

public class OutlinePipeline {
   private static final class_2960 PIPELINE_ID = class_2960.method_60655("rich", "pipeline/outline");
   private static final class_2960 VERTEX_SHADER = class_2960.method_60655("rich", "core/outline");
   private static final class_2960 FRAGMENT_SHADER = class_2960.method_60655("rich", "core/outline");
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private static final float FIXED_GUI_SCALE = 2.0F;
   private static final RenderPipeline PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(PIPELINE_ID)
         .withVertexShader(VERTEX_SHADER)
         .withFragmentShader(FRAGMENT_SHADER)
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("OutlineData", class_10789.field_60031)
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final int BUFFER_SIZE = 512;
   private GpuBuffer uniformBuffer;
   private GpuBuffer dummyVertexBuffer;
   private ByteBuffer dataBuffer;
   private boolean initialized = false;

   private void ensureInitialized() {
      if (!this.initialized) {
         this.dataBuffer = MemoryUtil.memAlloc(512);
         ByteBuffer var1 = MemoryUtil.memAlloc(4);
         var1.putInt(0);
         var1.flip();
         this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:outline_dummy_vertex", 32, var1);
         MemoryUtil.memFree(var1);
         this.initialized = true;
      }
   }

   public void drawOutline(float var1, float var2, float var3, float var4, int[] var5, float[] var6, float[] var7, float var8) {
      class_310 var9 = class_310.method_1551();
      if (var9.method_1522() != null) {
         this.ensureInitialized();
         int var10 = var9.method_22683().method_4489();
         int var11 = var9.method_22683().method_4506();
         float var12 = var10 / 2.0F;
         float var13 = var11 / 2.0F;
         this.prepareUniformData(var1, var2, var3, var4, var12, var13, 2.0F, var5, var6, var7, var8);
         this.uploadAndDraw(var9);
      }
   }

   private void prepareUniformData(
      float var1, float var2, float var3, float var4, float var5, float var6, float var7, int[] var8, float[] var9, float[] var10, float var11
   ) {
      this.dataBuffer.clear();
      this.dataBuffer.putFloat(var1);
      this.dataBuffer.putFloat(var2);
      this.dataBuffer.putFloat(var3);
      this.dataBuffer.putFloat(var4);
      this.dataBuffer.putFloat(var5);
      this.dataBuffer.putFloat(var6);
      this.dataBuffer.putFloat(var11);
      this.dataBuffer.putFloat(var7);
      this.dataBuffer.putFloat(var10[0]);
      this.dataBuffer.putFloat(var10[1]);
      this.dataBuffer.putFloat(var10[2]);
      this.dataBuffer.putFloat(var10[3]);

      for (int var12 = 0; var12 < 8; var12++) {
         int var13 = var12 < var8.length ? var8[var12] : var8[var8.length - 1];
         float var14 = (var13 >> 24 & 0xFF) / 255.0F;
         float var15 = (var13 >> 16 & 0xFF) / 255.0F;
         float var16 = (var13 >> 8 & 0xFF) / 255.0F;
         float var17 = (var13 & 0xFF) / 255.0F;
         this.dataBuffer.putFloat(var15);
         this.dataBuffer.putFloat(var16);
         this.dataBuffer.putFloat(var17);
         this.dataBuffer.putFloat(var14);
      }

      for (int var18 = 0; var18 < 8; var18++) {
         float var19 = var18 < var9.length ? var9[var18] : var9[var9.length - 1];
         this.dataBuffer.putFloat(var19);
      }

      this.dataBuffer.flip();
   }

   private void uploadAndDraw(class_310 var1) {
      int var2 = this.dataBuffer.remaining();
      if (this.uniformBuffer == null || this.uniformBuffer.size() < var2) {
         if (this.uniformBuffer != null) {
            this.uniformBuffer.close();
         }

         this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:outline_uniform", 136, var2);
      }

      CommandEncoder var3 = RenderSystem.getDevice().createCommandEncoder();
      var3.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
      GpuBufferSlice var4 = RenderSystem.getDynamicUniforms().method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      RenderPass var5 = var3.createRenderPass(
         () -> "minecraft:outline_pass", var1.method_1522().method_71639(), OptionalInt.empty(), var1.method_1522().method_71640(), OptionalDouble.empty()
      );

      try {
         var5.setPipeline(PIPELINE);
         var5.setVertexBuffer(0, this.dummyVertexBuffer);
         RenderSystem.bindDefaultUniforms(var5);
         var5.setUniform("DynamicTransforms", var4);
         var5.setUniform("OutlineData", this.uniformBuffer);
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
