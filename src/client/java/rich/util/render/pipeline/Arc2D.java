package rich.util.render.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

public class Arc2D {
   private static RenderPipeline pipeline;
   private static GpuBuffer uniformBuffer;
   private static final int UNIFORM_SIZE = 320;
   private static final float FIXED_GUI_SCALE = 2.0F;

   public static void init() {
      if (pipeline == null) {
         try {
            pipeline = RenderPipeline.builder(new Snippet[0])
               .withLocation(Identifier.of("rich", "core/arc"))
               .withVertexShader(Identifier.of("rich", "core/arc_vertex"))
               .withFragmentShader(Identifier.of("rich", "core/arc_fragment"))
               .withVertexFormat(VertexFormat.builder().build(), VertexFormat.DrawMode.TRIANGLES)
               .withUniform("Uniforms", UniformType.UNIFORM_BUFFER)
               .withBlend(BlendFunction.TRANSLUCENT)
               .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
               .withCull(false)
               .build();
            uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "Arc2D Uniforms", 144, 320L);
         } catch (Exception var1) {
            System.err.println("[Arc2D] Failed to init: " + var1.getMessage());
            var1.printStackTrace();
         }
      }
   }

   public static void draw(Matrix4f var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7, int... var8) {
      if (pipeline == null) {
         init();
      }

      if (pipeline != null && uniformBuffer != null) {
         MinecraftClient var9 = MinecraftClient.getInstance();
         int var10 = var9.getWindow().getFramebufferWidth();
         int var11 = var9.getWindow().getFramebufferHeight();
         int[] var12 = normalizeColors(var8);
         ByteBuffer var13 = MemoryUtil.memAlloc(320);
         var13.putFloat(var0.m00()).putFloat(var0.m01()).putFloat(var0.m02()).putFloat(var0.m03());
         var13.putFloat(var0.m10()).putFloat(var0.m11()).putFloat(var0.m12()).putFloat(var0.m13());
         var13.putFloat(var0.m20()).putFloat(var0.m21()).putFloat(var0.m22()).putFloat(var0.m23());
         var13.putFloat(var0.m30()).putFloat(var0.m31()).putFloat(var0.m32()).putFloat(var0.m33());
         var13.position(64);
         var13.putFloat(var1 * 2.0F);
         var13.putFloat(var2 * 2.0F);
         var13.putFloat(var3 * 2.0F);
         var13.putFloat(var3 * 2.0F);
         var13.putFloat(var3 * 2.0F);
         var13.putFloat(var4 * 2.0F);
         var13.putFloat(var5);
         var13.putFloat(var6);
         var13.putFloat(var7);
         var13.putFloat(2.0F);
         var13.putFloat(var10);
         var13.putFloat(var11);
         var13.position(112);

         for (int var14 = 0; var14 < 9; var14++) {
            int var15 = var12[var14];
            var13.putFloat((var15 >> 16 & 0xFF) / 255.0F);
            var13.putFloat((var15 >> 8 & 0xFF) / 255.0F);
            var13.putFloat((var15 & 0xFF) / 255.0F);
            var13.putFloat((var15 >> 24 & 0xFF) / 255.0F);
         }

         var13.flip();
         CommandEncoder var21 = RenderSystem.getDevice().createCommandEncoder();
         var21.writeToBuffer(uniformBuffer.slice(), var13);
         MemoryUtil.memFree(var13);
         Framebuffer var22 = var9.getFramebuffer();
         RenderPass var16 = var21.createRenderPass(() -> "Arc2D", var22.getColorAttachmentView(), OptionalInt.empty(), var22.getDepthAttachmentView(), OptionalDouble.of(1.0));

         try {
            var16.setPipeline(pipeline);
            var16.setUniform("Uniforms", uniformBuffer);
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

   private static int[] normalizeColors(int[] var0) {
      if (var0.length == 1) {
         int var3 = var0[0];
         return new int[]{var3, var3, var3, var3, var3, var3, var3, var3, var3};
      }

      if (var0.length >= 9) {
         return var0;
      }

      int[] var1 = new int[9];

      for (int var2 = 0; var2 < 9; var2++) {
         var1[var2] = var2 < var0.length ? var0[var2] : var0[var0.length - 1];
      }

      return var1;
   }

   public static void shutdown() {
      if (uniformBuffer != null) {
         uniformBuffer.close();
         uniformBuffer = null;
      }

      pipeline = null;
   }
}
