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
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.class_1044;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.system.MemoryUtil;

public class TexturePipeline {
   private static final class_2960 PIPELINE_ID = class_2960.method_60655("rich", "pipeline/texture");
   private static final class_2960 VERTEX_SHADER = class_2960.method_60655("rich", "core/texture");
   private static final class_2960 FRAGMENT_SHADER = class_2960.method_60655("rich", "core/texture");
   private static final Vector3f MODEL_OFFSET = new Vector3f(0.0F, 0.0F, 0.0F);
   private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
   private static final float FIXED_GUI_SCALE = 2.0F;
   private static final RenderPipeline PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation(PIPELINE_ID)
         .withVertexShader(VERTEX_SHADER)
         .withFragmentShader(FRAGMENT_SHADER)
         .withVertexFormat(class_290.field_60033, class_5596.field_27379)
         .withUniform("TextureData", class_10789.field_60031)
         .withSampler("Sampler0")
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

   private void ensureInitialized() {
      if (!this.initialized) {
         this.dataBuffer = MemoryUtil.memAlloc(256);
         ByteBuffer var1 = MemoryUtil.memAlloc(4);
         var1.putInt(0);
         var1.flip();
         this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:texture_dummy_vertex", 32, var1);
         MemoryUtil.memFree(var1);
         this.initialized = true;
      }
   }

   public void drawTexture(
      class_2960 var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, int[] var10, float[] var11, float var12
   ) {
      this.drawTexture(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, var12, 0.0F);
   }

   public void drawTexture(
      class_2960 var1,
      float var2,
      float var3,
      float var4,
      float var5,
      float var6,
      float var7,
      float var8,
      float var9,
      int[] var10,
      float[] var11,
      float var12,
      float var13
   ) {
      class_310 var14 = class_310.method_1551();
      if (var14.method_1522() != null) {
         class_1044 var15 = var14.method_1531().method_4619(var1);
         if (var15 != null) {
            int var16;
            try {
               GpuTexture var17 = var15.method_68004();
               if (var17 == null) {
                  return;
               }

               var16 = this.getTextureGlId(var17);
               if (var16 <= 0) {
                  return;
               }
            } catch (Exception var21) {
               return;
            }

            this.ensureInitialized();
            int var22 = var14.method_22683().method_4489();
            int var18 = var14.method_22683().method_4506();
            float var19 = var22 / 2.0F;
            float var20 = var18 / 2.0F;
            this.prepareUniformData(var2, var3, var4, var5, var6, var7, var8, var9, var19, var20, 2.0F, var10, var11, var12, var13);
            this.uploadAndDraw(var14, var16);
         }
      }
   }

   public void drawFramebufferTexture(int var1, float var2, float var3, float var4, float var5, int[] var6, float[] var7, float var8) {
      if (var1 > 0) {
         class_310 var9 = class_310.method_1551();
         if (var9.method_1522() != null) {
            this.ensureInitialized();
            int var10 = var9.method_22683().method_4489();
            int var11 = var9.method_22683().method_4506();
            float var12 = var10 / 2.0F;
            float var13 = var11 / 2.0F;
            this.prepareUniformData(var2, var3, var4, var5, 0.0F, 0.0F, 1.0F, 1.0F, var12, var13, 2.0F, var6, var7, 1.0F, 0.0F);
            this.uploadAndDraw(var9, var1);
         }
      }
   }

   private void prepareUniformData(
      float var1,
      float var2,
      float var3,
      float var4,
      float var5,
      float var6,
      float var7,
      float var8,
      float var9,
      float var10,
      float var11,
      int[] var12,
      float[] var13,
      float var14,
      float var15
   ) {
      this.dataBuffer.clear();
      this.dataBuffer.putFloat(var9);
      this.dataBuffer.putFloat(var10);
      this.dataBuffer.putFloat(var14);
      this.dataBuffer.putFloat(var11);
      this.dataBuffer.putFloat(var1);
      this.dataBuffer.putFloat(var2);
      this.dataBuffer.putFloat(var3);
      this.dataBuffer.putFloat(var4);
      this.dataBuffer.putFloat(var5);
      this.dataBuffer.putFloat(var6);
      this.dataBuffer.putFloat(var7);
      this.dataBuffer.putFloat(var8);
      this.dataBuffer.putFloat(var13[0]);
      this.dataBuffer.putFloat(var13[1]);
      this.dataBuffer.putFloat(var13[2]);
      this.dataBuffer.putFloat(var13[3]);
      float var16 = (float)Math.toRadians(var15);
      this.dataBuffer.putFloat(var16);
      this.dataBuffer.putFloat(0.0F);
      this.dataBuffer.putFloat(0.0F);
      this.dataBuffer.putFloat(0.0F);

      for (int var17 = 0; var17 < 4; var17++) {
         int var18 = var17 < var12.length ? var12[var17] : var12[var12.length - 1];
         float var19 = (var18 >> 24 & 0xFF) / 255.0F;
         float var20 = (var18 >> 16 & 0xFF) / 255.0F;
         float var21 = (var18 >> 8 & 0xFF) / 255.0F;
         float var22 = (var18 & 0xFF) / 255.0F;
         this.dataBuffer.putFloat(var20);
         this.dataBuffer.putFloat(var21);
         this.dataBuffer.putFloat(var22);
         this.dataBuffer.putFloat(var19);
      }

      this.dataBuffer.flip();
   }

   private void uploadAndDraw(class_310 var1, int var2) {
      int var3 = this.dataBuffer.remaining();
      if (this.uniformBuffer == null || this.uniformBuffer.size() < var3) {
         if (this.uniformBuffer != null) {
            this.uniformBuffer.close();
         }

         this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:texture_uniform", 136, var3);
      }

      CommandEncoder var4 = RenderSystem.getDevice().createCommandEncoder();
      var4.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
      GpuBufferSlice var5 = RenderSystem.getDynamicUniforms().method_71106(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
      GL13.glActiveTexture(33984);
      GL11.glBindTexture(3553, var2);
      RenderPass var6 = var4.createRenderPass(
         () -> "minecraft:texture_pass", var1.method_1522().method_71639(), OptionalInt.empty(), var1.method_1522().method_71640(), OptionalDouble.empty()
      );

      try {
         var6.setPipeline(PIPELINE);
         var6.setVertexBuffer(0, this.dummyVertexBuffer);
         RenderSystem.bindDefaultUniforms(var6);
         var6.setUniform("DynamicTransforms", var5);
         var6.setUniform("TextureData", this.uniformBuffer);
         var6.draw(0, 6);
      } catch (Throwable var10) {
         if (var6 != null) {
            try {
               var6.close();
            } catch (Throwable var9) {
               var10.addSuppressed(var9);
            }
         }

         throw var10;
      }

      if (var6 != null) {
         var6.close();
      }

      GL11.glBindTexture(3553, 0);
   }

   private int getTextureGlId(GpuTexture var1) {
      try {
         Field var2 = var1.getClass().getDeclaredField("id");
         var2.setAccessible(true);
         return var2.getInt(var1);
      } catch (Exception var11) {
         try {
            Field var3 = var1.getClass().getDeclaredField("glId");
            var3.setAccessible(true);
            return var3.getInt(var1);
         } catch (Exception var10) {
            try {
               for (Field var7 : var1.getClass().getDeclaredFields()) {
                  if (var7.getType() == int.class) {
                     var7.setAccessible(true);
                     int var8 = var7.getInt(var1);
                     if (var8 > 0) {
                        return var8;
                     }
                  }
               }
            } catch (Exception var9) {
            }

            return 0;
         }
      }
   }

   public void flush() {
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
