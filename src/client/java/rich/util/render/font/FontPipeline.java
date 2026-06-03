package rich.util.render.font;

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
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.texture.AbstractTexture;
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

public class FontPipeline {
   private static final Identifier PIPELINE_ID = Identifier.of("rich", "pipeline/msdf");
   private static final Identifier SHADER_ID = Identifier.of("rich", "core/msdf");
   private static final float FIXED_GUI_SCALE = 2.0F;
   private static final RenderPipeline PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET})
         .withLocation(PIPELINE_ID)
         .withVertexShader(SHADER_ID)
         .withFragmentShader(SHADER_ID)
         .withVertexFormat(VertexFormats.EMPTY, VertexFormat.DrawMode.TRIANGLES)
         .withUniform("FontData", UniformType.UNIFORM_BUFFER)
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
   private static final int[] LEGACY_COLORS = new int[32];
   private static final int MAX_CHARS = 256;
   private static final int BUFFER_SIZE = 16448;
   private GpuBuffer uniformBuffer;
   private GpuBuffer dummyVertexBuffer;
   private ByteBuffer dataBuffer;
   private boolean initialized = false;
   private final List<FontPipeline.CharData> charBatch = new ArrayList<>(256);
   private FontAtlas currentAtlas = null;
   private float currentOutlineWidth = 0.0F;
   private int currentOutlineColor = 0;
   private GpuTextureView cachedTextureView = null;
   private GpuTexture cachedGpuTexture = null;

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
         this.dataBuffer = MemoryUtil.memAlloc(16448);
         ByteBuffer var1 = MemoryUtil.memAlloc(4);
         var1.putInt(0);
         var1.flip();
         this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "rich:font_dummy_vertex", 32, var1);
         MemoryUtil.memFree(var1);
         this.initialized = true;
      }
   }

   public void drawText(FontAtlas var1, String var2, float var3, float var4, float var5, int var6) {
      this.drawText(var1, var2, var3, var4, var5, var6, 0.0F, 0, 0.0F);
   }

   public void drawText(FontAtlas var1, String var2, float var3, float var4, float var5, int var6, float var7, int var8, float var9) {
      MinecraftClient var10 = MinecraftClient.getInstance();
      if (var10.getFramebuffer() != null) {
         if (var2 != null && !var2.isEmpty()) {
            var1.ensureLoaded();
            if (var1.getGlyphCount() != 0) {
               this.ensureInitialized();
               if (this.currentAtlas != null && (this.currentAtlas != var1 || this.currentOutlineWidth != var7 || this.currentOutlineColor != var8)) {
                  this.flush();
               }

               this.currentAtlas = var1;
               this.currentOutlineWidth = var7;
               this.currentOutlineColor = var8;
               float var11 = var5 / var1.getFontSize();
               float var12 = var3;
               float var13 = var4;
               float var14 = this.getTextWidth(var1, var2, var5);
               float var15 = this.getTextHeight(var1, var2, var5);
               float var16 = var3 + var14 / 2.0F;
               float var17 = var4 + var15 / 2.0F;
               float var18 = (float)Math.toRadians(var9);
               int var19 = var6;
               int var20 = 0;

               while (var20 < var2.length()) {
                  int var21 = var2.codePointAt(var20);
                  int var22 = Character.charCount(var21);
                  if ((var21 == 167 || var21 == 38) && var20 + var22 < var2.length()) {
                     int var23 = var2.codePointAt(var20 + var22);
                     if (var23 == 35 && var20 + var22 + 6 < var2.length()) {
                        try {
                           String var32 = var2.substring(var20 + var22 + 1, var20 + var22 + 7);
                           var19 = 0xFF000000 | Integer.parseInt(var32, 16);
                           var20 += var22 + 7;
                           continue;
                        } catch (Exception var28) {
                        }
                     }

                     int var24 = "0123456789abcdefklmnor".indexOf(Character.toLowerCase((char)var23));
                     if (var24 >= 0) {
                        if (var24 < 16) {
                           var19 = LEGACY_COLORS[var24];
                        } else if (var24 == 21) {
                           var19 = var6;
                        }

                        var20 += var22 + Character.charCount(var23);
                        continue;
                     }
                  }

                  if (var21 == 10) {
                     var12 = var3;
                     var13 += var1.getLineHeight() * var11;
                     var20 += var22;
                  } else {
                     Glyph var29 = var1.getGlyph(var21);
                     if (var29 == null) {
                        Glyph var30 = var1.getGlyph(63);
                        if (var30 != null) {
                           var12 += var30.xAdvance * var11;
                        } else {
                           var12 += var5 * 0.5F;
                        }

                        var20 += var22;
                     } else {
                        float var31 = var12 + var29.xOffset * var11;
                        float var25 = var13 + var29.yOffset * var11;
                        float var26 = var29.width * var11;
                        float var27 = var29.height * var11;
                        if (var29.width > 0.0F && var29.height > 0.0F) {
                           this.charBatch
                              .add(
                                 new FontPipeline.CharData(
                                    var31, var25, var26, var27, var29.u0, var29.v0, var29.u1, var29.v1, var19, var18, var16, var17, var11
                                 )
                              );
                        }

                        var12 += var29.xAdvance * var11;
                        if (this.charBatch.size() >= 256) {
                           this.flush();
                        }

                        var20 += var22;
                     }
                  }
               }

               if (!this.charBatch.isEmpty() && this.currentAtlas != null) {
                  this.flush();
               }
            }
         }
      }
   }

   public void drawTextRotatedAroundPoint(
      FontAtlas var1, String var2, float var3, float var4, float var5, int var6, float var7, int var8, float var9, float var10, float var11
   ) {
      MinecraftClient var12 = MinecraftClient.getInstance();
      if (var12.getFramebuffer() != null) {
         if (var2 != null && !var2.isEmpty()) {
            var1.ensureLoaded();
            if (var1.getGlyphCount() != 0) {
               this.ensureInitialized();
               if (this.currentAtlas != null && (this.currentAtlas != var1 || this.currentOutlineWidth != var7 || this.currentOutlineColor != var8)) {
                  this.flush();
               }

               this.currentAtlas = var1;
               this.currentOutlineWidth = var7;
               this.currentOutlineColor = var8;
               float var13 = var5 / var1.getFontSize();
               float var14 = var3;
               float var15 = var4;
               float var16 = (float)Math.toRadians(var9);
               int var17 = var6;
               int var18 = 0;

               while (var18 < var2.length()) {
                  int var19 = var2.codePointAt(var18);
                  int var20 = Character.charCount(var19);
                  if ((var19 == 167 || var19 == 38) && var18 + var20 < var2.length()) {
                     int var21 = var2.codePointAt(var18 + var20);
                     if (var21 == 35 && var18 + var20 + 6 < var2.length()) {
                        try {
                           String var30 = var2.substring(var18 + var20 + 1, var18 + var20 + 7);
                           var17 = 0xFF000000 | Integer.parseInt(var30, 16);
                           var18 += var20 + 7;
                           continue;
                        } catch (Exception var26) {
                        }
                     }

                     int var22 = "0123456789abcdefklmnor".indexOf(Character.toLowerCase((char)var21));
                     if (var22 >= 0) {
                        if (var22 < 16) {
                           var17 = LEGACY_COLORS[var22];
                        } else if (var22 == 21) {
                           var17 = var6;
                        }

                        var18 += var20 + Character.charCount(var21);
                        continue;
                     }
                  }

                  if (var19 == 10) {
                     var14 = var3;
                     var15 += var1.getLineHeight() * var13;
                     var18 += var20;
                  } else {
                     Glyph var27 = var1.getGlyph(var19);
                     if (var27 == null) {
                        Glyph var28 = var1.getGlyph(63);
                        if (var28 != null) {
                           var14 += var28.xAdvance * var13;
                        } else {
                           var14 += var5 * 0.5F;
                        }

                        var18 += var20;
                     } else {
                        float var29 = var14 + var27.xOffset * var13;
                        float var23 = var15 + var27.yOffset * var13;
                        float var24 = var27.width * var13;
                        float var25 = var27.height * var13;
                        if (var27.width > 0.0F && var27.height > 0.0F) {
                           this.charBatch
                              .add(
                                 new FontPipeline.CharData(
                                    var29, var23, var24, var25, var27.u0, var27.v0, var27.u1, var27.v1, var17, var16, var10, var11, var13
                                 )
                              );
                        }

                        var14 += var27.xAdvance * var13;
                        if (this.charBatch.size() >= 256) {
                           this.flush();
                        }

                        var18 += var20;
                     }
                  }
               }

               if (!this.charBatch.isEmpty() && this.currentAtlas != null) {
                  this.flush();
               }
            }
         }
      }
   }

   public void flush() {
      if (!this.charBatch.isEmpty() && this.currentAtlas != null) {
         MinecraftClient var1 = MinecraftClient.getInstance();
         if (var1.getFramebuffer() == null) {
            this.charBatch.clear();
            this.currentAtlas = null;
         } else {
            AbstractTexture var2 = var1.getTextureManager().getTexture(this.currentAtlas.getTextureId());
            if (var2 == null) {
               this.charBatch.clear();
               this.currentAtlas = null;
            } else {
               GpuTexture var3;
               try {
                  var3 = var2.getGlTexture();
               } catch (Exception var12) {
                  this.charBatch.clear();
                  this.currentAtlas = null;
                  return;
               }

               if (var3 != this.cachedGpuTexture) {
                  if (this.cachedTextureView != null) {
                     this.cachedTextureView.close();
                  }

                  this.cachedTextureView = RenderSystem.getDevice().createTextureView(var3);
                  this.cachedGpuTexture = var3;
               }

               this.prepareUniformData(var1, this.currentAtlas, this.currentOutlineWidth, this.currentOutlineColor);
               int var4 = this.dataBuffer.remaining();
               if (this.uniformBuffer == null || this.uniformBuffer.size() < var4) {
                  if (this.uniformBuffer != null) {
                     this.uniformBuffer.close();
                  }

                  this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "rich:font_uniform", 136, var4);
               }

               CommandEncoder var5 = RenderSystem.getDevice().createCommandEncoder();
               var5.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
               GpuBufferSlice var6 = RenderSystem.getDynamicUniforms()
                  .write(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
               GpuSampler var7 = RenderSystem.getSamplerCache().get(FilterMode.LINEAR);
               RenderPass var8 = var5.createRenderPass(
                  () -> "rich:font_pass", var1.getFramebuffer().getColorAttachmentView(), OptionalInt.empty(), var1.getFramebuffer().getDepthAttachmentView(), OptionalDouble.empty()
               );

               try {
                  var8.setPipeline(PIPELINE);
                  var8.setVertexBuffer(0, this.dummyVertexBuffer);
                  var8.bindTexture("Sampler0", this.cachedTextureView, var7);
                  RenderSystem.bindDefaultUniforms(var8);
                  var8.setUniform("DynamicTransforms", var6);
                  var8.setUniform("FontData", this.uniformBuffer);
                  var8.draw(0, this.charBatch.size() * 6);
               } catch (Throwable var13) {
                  if (var8 != null) {
                     try {
                        var8.close();
                     } catch (Throwable var11) {
                        var13.addSuppressed(var11);
                     }
                  }

                  throw var13;
               }

               if (var8 != null) {
                  var8.close();
               }

               this.charBatch.clear();
               this.currentAtlas = null;
            }
         }
      } else {
         this.charBatch.clear();
         this.currentAtlas = null;
      }
   }

   private void prepareUniformData(MinecraftClient var1, FontAtlas var2, float var3, int var4) {
      this.dataBuffer.clear();
      float var5 = this.getFixedScaledWidth();
      float var6 = this.getFixedScaledHeight();
      float var7 = 2.0F;
      this.dataBuffer.putFloat(var5);
      this.dataBuffer.putFloat(var6);
      this.dataBuffer.putFloat(var7);
      this.dataBuffer.putFloat(var3);
      float var8 = (var4 >> 24 & 0xFF) / 255.0F;
      float var9 = (var4 >> 16 & 0xFF) / 255.0F;
      float var10 = (var4 >> 8 & 0xFF) / 255.0F;
      float var11 = (var4 & 0xFF) / 255.0F;
      this.dataBuffer.putFloat(var9);
      this.dataBuffer.putFloat(var10);
      this.dataBuffer.putFloat(var11);
      this.dataBuffer.putFloat(var8);
      this.dataBuffer.putFloat(var2.getAtlasWidth());
      this.dataBuffer.putFloat(var2.getAtlasHeight());
      this.dataBuffer.putFloat(var2.getDistanceRange());
      this.dataBuffer.putFloat(var2.getFontSize());
      this.dataBuffer.putInt(this.charBatch.size());
      this.dataBuffer.putInt(0);
      this.dataBuffer.putInt(0);
      this.dataBuffer.putInt(0);

      for (FontPipeline.CharData var13 : this.charBatch) {
         this.dataBuffer.putFloat(var13.x);
         this.dataBuffer.putFloat(var13.y);
         this.dataBuffer.putFloat(var13.width);
         this.dataBuffer.putFloat(var13.height);
         this.dataBuffer.putFloat(var13.u0);
         this.dataBuffer.putFloat(var13.v0);
         this.dataBuffer.putFloat(var13.u1);
         this.dataBuffer.putFloat(var13.v1);
         float var14 = (var13.color >> 24 & 0xFF) / 255.0F;
         float var15 = (var13.color >> 16 & 0xFF) / 255.0F;
         float var16 = (var13.color >> 8 & 0xFF) / 255.0F;
         float var17 = (var13.color & 0xFF) / 255.0F;
         this.dataBuffer.putFloat(var15);
         this.dataBuffer.putFloat(var16);
         this.dataBuffer.putFloat(var17);
         this.dataBuffer.putFloat(var14);
         this.dataBuffer.putFloat(var13.rotation);
         this.dataBuffer.putFloat(var13.pivotX);
         this.dataBuffer.putFloat(var13.pivotY);
         this.dataBuffer.putFloat(var13.glyphScale);
      }

      this.dataBuffer.flip();
   }

   public float getTextWidth(FontAtlas var1, String var2, float var3) {
      var1.ensureLoaded();
      float var4 = var3 / var1.getFontSize();
      float var5 = 0.0F;
      float var6 = 0.0F;
      int var7 = 0;

      while (var7 < var2.length()) {
         int var8 = var2.codePointAt(var7);
         int var9 = Character.charCount(var8);
         if ((var8 == 167 || var8 == 38) && var7 + var9 < var2.length()) {
            int var10 = var2.codePointAt(var7 + var9);
            if (var10 == 35 && var7 + var9 + 6 < var2.length()) {
               var7 += var9 + 7;
               continue;
            }

            int var11 = "0123456789abcdefklmnor".indexOf(Character.toLowerCase((char)var10));
            if (var11 >= 0) {
               var7 += var9 + Character.charCount(var10);
               continue;
            }
         }

         if (var8 == 10) {
            var6 = Math.max(var6, var5);
            var5 = 0.0F;
            var7 += var9;
         } else {
            Glyph var12 = var1.getGlyph(var8);
            if (var12 != null) {
               var5 += var12.xAdvance * var4;
            } else {
               Glyph var13 = var1.getGlyph(63);
               if (var13 != null) {
                  var5 += var13.xAdvance * var4;
               } else {
                  var5 += var3 * 0.5F;
               }
            }

            var7 += var9;
         }
      }

      return Math.max(var6, var5);
   }

   public float getTextHeight(FontAtlas var1, String var2, float var3) {
      var1.ensureLoaded();
      float var4 = var3 / var1.getFontSize();
      int var5 = 1;

      for (int var6 = 0; var6 < var2.length(); var6++) {
         if (var2.charAt(var6) == '\n') {
            var5++;
         }
      }

      return var5 * var1.getLineHeight() * var4;
   }

   public void close() {
      if (this.cachedTextureView != null) {
         this.cachedTextureView.close();
         this.cachedTextureView = null;
         this.cachedGpuTexture = null;
      }

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

   static {
      for (int var0 = 0; var0 < 16; var0++) {
         int var1 = (var0 >> 3 & 1) * 85;
         int var2 = (var0 >> 2 & 1) * 170 + var1;
         int var3 = (var0 >> 1 & 1) * 170 + var1;
         int var4 = (var0 & 1) * 170 + var1;
         if (var0 == 6) {
            var2 += 85;
         }

         LEGACY_COLORS[var0] = 0xFF000000 | var2 << 16 | var3 << 8 | var4;
         LEGACY_COLORS[var0 + 16] = (var2 & 16579836) >> 2 << 24 | var2 << 16 | var3 << 8 | var4;
      }
   }

   private static class CharData {
      float x;
      float y;
      float width;
      float height;
      float u0;
      float v0;
      float u1;
      float v1;
      int color;
      float rotation;
      float pivotX;
      float pivotY;
      float glyphScale;

      CharData(
         float var1,
         float var2,
         float var3,
         float var4,
         float var5,
         float var6,
         float var7,
         float var8,
         int var9,
         float var10,
         float var11,
         float var12,
         float var13
      ) {
         this.x = var1;
         this.y = var2;
         this.width = var3;
         this.height = var4;
         this.u0 = var5;
         this.v0 = var6;
         this.u1 = var7;
         this.v1 = var8;
         this.color = var9;
         this.rotation = var10;
         this.pivotX = var11;
         this.pivotY = var12;
         this.glyphScale = var13;
      }
   }
}
