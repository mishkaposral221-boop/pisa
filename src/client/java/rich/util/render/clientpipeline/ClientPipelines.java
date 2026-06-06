package rich.util.render.clientpipeline;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;

import java.util.function.Function;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.util.Util;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public class ClientPipelines {
   public static final RenderPipeline ROMB_ESP_PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET})
         .withLocation("pipeline/wtex")
         .withVertexShader("core/position_tex_color")
         .withFragmentShader("core/position_tex_color")
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withCull(false)
         .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
         .build()
   );
   public static final Function<Identifier, RenderLayer> ROMB_ESP = Util.memoize(var0 -> {
      RenderSetup var1 = RenderSetup.builder(ROMB_ESP_PIPELINE).texture("Sampler0", var0).translucent().expectedBufferSize(1536).build();
      return RenderLayer.of("wtex", var1);
   });
   public static final RenderPipeline GHOSTS_ESP_PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET})
         .withLocation("pipeline/wtex")
         .withVertexShader("core/position_tex_color")
         .withFragmentShader("core/position_tex_color")
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
         .build()
   );
   public static final Function<Identifier, RenderLayer> GHOSTS_ESP = Util.memoize(var0 -> {
      RenderSetup var1 = RenderSetup.builder(GHOSTS_ESP_PIPELINE).texture("Sampler0", var0).translucent().expectedBufferSize(1536).build();
      return RenderLayer.of("wtex", var1);
   });
   public static final RenderPipeline CHAIN_ESP_PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET})
         .withLocation("pipeline/wtex")
         .withVertexShader("core/position_tex_color")
         .withFragmentShader("core/position_tex_color")
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
         .build()
   );
   public static final Function<Identifier, RenderLayer> CHAIN_ESP = Util.memoize(var0 -> {
      RenderSetup var1 = RenderSetup.builder(CHAIN_ESP_PIPELINE).texture("Sampler0", var0).translucent().expectedBufferSize(1536).build();
      return RenderLayer.of("wtex", var1);
   });
   public static final RenderPipeline CRYSTAL_FILLED_PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET})
         .withLocation("pipeline/crystal_filled")
         .withVertexShader("core/position_color")
         .withFragmentShader("core/position_color")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
         .build()
   );
   public static final RenderLayer CRYSTAL_FILLED = RenderLayer.of(
      "crystal_filled", RenderSetup.builder(CRYSTAL_FILLED_PIPELINE).translucent().expectedBufferSize(8192).build()
   );
   public static final RenderPipeline CRYSTAL_GLOW_PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET})
         .withLocation("pipeline/crystal_glow")
         .withVertexShader("core/position_color")
         .withFragmentShader("core/position_color")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
         .build()
   );
   public static final RenderLayer CRYSTAL_GLOW = RenderLayer.of(
      "crystal_glow", RenderSetup.builder(CRYSTAL_GLOW_PIPELINE).translucent().expectedBufferSize(4096).build()
   );
   public static final RenderPipeline BLOOM_ESP_PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET})
         .withLocation("pipeline/bloom_esp")
         .withVertexShader("core/position_tex_color")
         .withFragmentShader("core/position_tex_color")
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
         .build()
   );
   public static final Function<Identifier, RenderLayer> BLOOM_ESP = Util.memoize(var0 -> {
      RenderSetup var1 = RenderSetup.builder(BLOOM_ESP_PIPELINE).texture("Sampler0", var0).translucent().expectedBufferSize(2048).build();
      return RenderLayer.of("bloom_esp", var1);
   });
   public static final RenderPipeline CHINA_HAT_PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET})
         .withLocation("pipeline/china_hat")
         .withVertexShader("core/position_color")
         .withFragmentShader("core/position_color")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(true)
         .withCull(false)
         .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLE_FAN)
         .build()
   );
   public static final RenderLayer CHINA_HAT = RenderLayer.of(
      "china_hat", RenderSetup.builder(CHINA_HAT_PIPELINE).translucent().expectedBufferSize(8192).build()
   );
   public static final RenderPipeline CHINA_HAT_OUTLINE_PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET})
         .withLocation("pipeline/china_hat_outline")
         .withVertexShader("core/position_color")
         .withFragmentShader("core/position_color")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(true)
         .withCull(false)
         .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.DEBUG_LINE_STRIP)
         .build()
   );
   public static final RenderLayer CHINA_HAT_OUTLINE = RenderLayer.of(
      "china_hat_outline", RenderSetup.builder(CHINA_HAT_OUTLINE_PIPELINE).translucent().expectedBufferSize(4096).build()
   );
   public static final RenderPipeline CHINA_HAT_GRID_PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET})
         .withLocation("pipeline/china_hat_grid")
         .withVertexShader("core/position_color")
         .withFragmentShader("core/position_color")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(true)
         .withCull(false)
         .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.DEBUG_LINES)
         .build()
   );
   public static final RenderLayer CHINA_HAT_GRID = RenderLayer.of(
      "china_hat_grid", RenderSetup.builder(CHINA_HAT_GRID_PIPELINE).translucent().expectedBufferSize(8192).build()
   );
   public static final RenderPipeline LINES_NO_DEPTH_PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.RENDERTYPE_LINES_SNIPPET})
         .withLocation("pipeline/lines_no_depth")
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .build()
   );
   public static final RenderLayer LINES_NO_DEPTH = RenderLayer.of(
      "lines_no_depth", RenderSetup.builder(LINES_NO_DEPTH_PIPELINE).translucent().expectedBufferSize(8192).build()
   );
   public static final RenderPipeline QUADS_NO_DEPTH_PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET})
         .withLocation("pipeline/quads_no_depth")
         .withVertexShader("core/position_color")
         .withFragmentShader("core/position_color")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
         .build()
   );
   public static final RenderLayer QUADS_NO_DEPTH = RenderLayer.of(
      "quads_no_depth", RenderSetup.builder(QUADS_NO_DEPTH_PIPELINE).translucent().expectedBufferSize(8192).build()
   );
   public static final RenderPipeline TEX_QUADS_NO_DEPTH_PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET})
         .withLocation("pipeline/tex_quads_no_depth")
         .withVertexShader("core/position_tex_color")
         .withFragmentShader("core/position_tex_color")
         .withSampler("Sampler0")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
         .build()
   );
   public static final Function<Identifier, RenderLayer> TEX_QUADS_NO_DEPTH = Util.memoize(var0 -> {
      RenderSetup var1 = RenderSetup.builder(TEX_QUADS_NO_DEPTH_PIPELINE).texture("Sampler0", var0).translucent().expectedBufferSize(8192).build();
      return RenderLayer.of("tex_quads_no_depth", var1);
   });
   public static final RenderPipeline WHEEL_PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET})
         .withLocation("pipeline/wheel")
         .withVertexShader("core/position_color")
         .withFragmentShader("core/position_color")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLES)
         .build()
   );
   public static final RenderLayer WHEEL = RenderLayer.of(
      "wheel", RenderSetup.builder(WHEEL_PIPELINE).translucent().expectedBufferSize(32768).build()
   );
   public static final RenderPipeline WORLD_PARTICLES_COLOR_PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.POSITION_COLOR_SNIPPET})
         .withLocation(Identifier.of("rich", "world_particles_color"))
         .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   public static final RenderLayer WORLD_PARTICLES_QUADS = RenderLayer.of(
      "world_particles_cube", RenderSetup.builder(WORLD_PARTICLES_COLOR_PIPELINE).translucent().expectedBufferSize(2048).build()
   );
   public static final RenderPipeline WORLD_PARTICLES_LINES_PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.POSITION_COLOR_SNIPPET})
         .withLocation(Identifier.of("rich", "world_particles_lines"))
         .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.DEBUG_LINES)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   public static final RenderLayer WORLD_PARTICLES_LINES = RenderLayer.of(
      "world_particles_lines", RenderSetup.builder(WORLD_PARTICLES_LINES_PIPELINE).translucent().expectedBufferSize(2048).build()
   );
   public static final RenderPipeline WORLD_PARTICLES_GLOW_PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.POSITION_TEX_COLOR_SNIPPET})
         .withLocation(Identifier.of("rich", "world_particles_glow"))
         .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .withSampler("Sampler0")
         .build()
   );
   public static final Function<Identifier, RenderLayer> WORLD_PARTICLES_GLOW = Util.memoize(
      var0 -> {
         RenderSetup var1 = RenderSetup.builder(WORLD_PARTICLES_GLOW_PIPELINE)
            .texture("Sampler0", var0)
            .translucent()
            .expectedBufferSize(2048)
            .build();
         return RenderLayer.of("world_particles_glow", var1);
      }
   );
   public static final RenderPipeline GUI_ARROW_BLEND_PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET})
         .withLocation("pipeline/gui_arrow_blend")
         .withVertexShader("core/position_tex_color")
         .withFragmentShader("core/position_tex_color")
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withCull(false)
         .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
         .build()
   );
   public static final Function<Identifier, RenderLayer> GUI_ARROW_BLEND = Util.memoize(var0 -> {
      RenderSetup var1 = RenderSetup.builder(GUI_ARROW_BLEND_PIPELINE).texture("Sampler0", var0).translucent().expectedBufferSize(256).build();
      return RenderLayer.of("gui_arrow_blend", var1);
   });

   // ── Body chams (skin through walls) ─────────────────────────────────────────
   // withCull(true): body geometry always faces outward, culling is fine.
   public static final RenderPipeline CHAMS_ENTITY_PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.ENTITY_SNIPPET})
         .withLocation("pipeline/chams_entity")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(true)
         .withCull(true)
         .build()
   );
   public static final Function<Identifier, RenderLayer> CHAMS_ENTITY = Util.memoize(var0 -> {
      RenderSetup var1 = RenderSetup.builder(CHAMS_ENTITY_PIPELINE).texture("Sampler0", var0).useLightmap().useOverlay().translucent().expectedBufferSize(8192).build();
      return RenderLayer.of("chams_entity", var1);
   });

   // ── Armor chams (armor through walls) ───────────────────────────────────────
   // withCull(false): REQUIRED for leggings (HUMANOID_LEGGINGS / layer_2).
   // The inner leg geometry has back-facing polygons that get culled with
   // withCull(true), causing leggings to appear solid black.
   // Using withCull(false) ensures all armor faces are rendered correctly.
   public static final RenderPipeline CHAMS_ARMOR_PIPELINE = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.ENTITY_SNIPPET})
         .withLocation("pipeline/chams_armor")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(true)
         .withCull(false)
         .build()
   );
   public static final Function<Identifier, RenderLayer> CHAMS_ARMOR = Util.memoize(var0 -> {
      RenderSetup var1 = RenderSetup.builder(CHAMS_ARMOR_PIPELINE).texture("Sampler0", var0).useLightmap().useOverlay().translucent().expectedBufferSize(8192).build();
      return RenderLayer.of("chams_armor", var1);
   });
}
