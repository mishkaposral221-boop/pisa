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
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.class_10789;
import net.minecraft.class_276;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

public class ArcOutline2D {
   private static RenderPipeline pipeline;
   private static GpuBuffer uniformBuffer;
   private static final int UNIFORM_SIZE = 176;
   private static final float FIXED_GUI_SCALE = 2.0F;

   public static void init() {
      if (pipeline == null) {
         try {
            pipeline = RenderPipeline.builder(new Snippet[0])
               .withLocation(class_2960.method_60655("rich", "core/arc_outline"))
               .withVertexShader(class_2960.method_60655("rich", "core/arc_outline_vertex"))
               .withFragmentShader(class_2960.method_60655("rich", "core/arc_outline_fragment"))
               .withVertexFormat(VertexFormat.builder().build(), class_5596.field_27379)
               .withUniform("Uniforms", class_10789.field_60031)
               .withBlend(BlendFunction.TRANSLUCENT)
               .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
               .withCull(false)
               .build();
            uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "ArcOutline2D Uniforms", 136, 176L);
         } catch (Exception var1) {
            System.err.println("[ArcOutline2D] Failed to init: " + var1.getMessage());
         }
      }
   }

   public static void draw(Matrix4f var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7, int var8, int var9, float var10) {
      if (pipeline == null) {
         init();
      }

      if (pipeline != null && uniformBuffer != null) {
         class_310 var11 = class_310.method_1551();
         int var12 = var11.method_22683().method_4489();
         int var13 = var11.method_22683().method_4506();
         float var14 = (var8 >> 16 & 0xFF) / 255.0F;
         float var15 = (var8 >> 8 & 0xFF) / 255.0F;
         float var16 = (var8 & 0xFF) / 255.0F;
         float var17 = (var8 >> 24 & 0xFF) / 255.0F;
         float var18 = (var9 >> 16 & 0xFF) / 255.0F;
         float var19 = (var9 >> 8 & 0xFF) / 255.0F;
         float var20 = (var9 & 0xFF) / 255.0F;
         float var21 = (var9 >> 24 & 0xFF) / 255.0F;
         ByteBuffer var22 = MemoryUtil.memAlloc(176);
         var22.putFloat(var0.m00()).putFloat(var0.m01()).putFloat(var0.m02()).putFloat(var0.m03());
         var22.putFloat(var0.m10()).putFloat(var0.m11()).putFloat(var0.m12()).putFloat(var0.m13());
         var22.putFloat(var0.m20()).putFloat(var0.m21()).putFloat(var0.m22()).putFloat(var0.m23());
         var22.putFloat(var0.m30()).putFloat(var0.m31()).putFloat(var0.m32()).putFloat(var0.m33());
         var22.position(64);
         var22.putFloat(var1 * 2.0F).putFloat(var2 * 2.0F).putFloat(var3 * 2.0F).putFloat(var3 * 2.0F);
         var22.putFloat(var3 * 2.0F).putFloat(var4 * 2.0F).putFloat(var5).putFloat(var6);
         var22.putFloat(var10).putFloat(var7 * 2.0F).putFloat(var12).putFloat(var13);
         var22.putFloat(var14).putFloat(var15).putFloat(var16).putFloat(var17);
         var22.putFloat(var18).putFloat(var19).putFloat(var20).putFloat(var21);
         var22.putFloat(2.0F).putFloat(0.0F).putFloat(0.0F).putFloat(0.0F);
         var22.flip();
         CommandEncoder var23 = RenderSystem.getDevice().createCommandEncoder();
         var23.writeToBuffer(uniformBuffer.slice(), var22);
         MemoryUtil.memFree(var22);
         class_276 var24 = var11.method_1522();
         RenderPass var25 = var23.createRenderPass(
            () -> "ArcOutline2D", var24.method_71639(), OptionalInt.empty(), var24.method_71640(), OptionalDouble.of(1.0)
         );

         try {
            var25.setPipeline(pipeline);
            var25.setUniform("Uniforms", uniformBuffer);
            var25.draw(0, 6);
         } catch (Throwable var29) {
            if (var25 != null) {
               try {
                  var25.close();
               } catch (Throwable var28) {
                  var29.addSuppressed(var28);
               }
            }

            throw var29;
         }

         if (var25 != null) {
            var25.close();
         }
      }
   }

   public static void shutdown() {
      if (uniformBuffer != null) {
         uniformBuffer.close();
         uniformBuffer = null;
      }

      pipeline = null;
   }
}
