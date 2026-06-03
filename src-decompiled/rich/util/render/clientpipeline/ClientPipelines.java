package rich.util.render.clientpipeline;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.util.function.Function;
import net.minecraft.class_10799;
import net.minecraft.class_12247;
import net.minecraft.class_156;
import net.minecraft.class_1921;
import net.minecraft.class_290;
import net.minecraft.class_2960;

public class ClientPipelines {
   public static final RenderPipeline ROMB_ESP_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation("pipeline/wtex")
         .withVertexShader("core/position_tex_color")
         .withFragmentShader("core/position_tex_color")
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withCull(false)
         .withVertexFormat(class_290.field_1575, class_5596.field_27382)
         .build()
   );
   public static final Function<class_2960, class_1921> ROMB_ESP = class_156.method_34866(var0 -> {
      class_12247 var1 = class_12247.method_75927(ROMB_ESP_PIPELINE).method_75934("Sampler0", var0).method_75937().method_75929(1536).method_75938();
      return class_1921.method_75940("wtex", var1);
   });
   public static final RenderPipeline GHOSTS_ESP_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation("pipeline/wtex")
         .withVertexShader("core/position_tex_color")
         .withFragmentShader("core/position_tex_color")
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .withVertexFormat(class_290.field_1575, class_5596.field_27382)
         .build()
   );
   public static final Function<class_2960, class_1921> GHOSTS_ESP = class_156.method_34866(var0 -> {
      class_12247 var1 = class_12247.method_75927(GHOSTS_ESP_PIPELINE).method_75934("Sampler0", var0).method_75937().method_75929(1536).method_75938();
      return class_1921.method_75940("wtex", var1);
   });
   public static final RenderPipeline CHAIN_ESP_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation("pipeline/wtex")
         .withVertexShader("core/position_tex_color")
         .withFragmentShader("core/position_tex_color")
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .withVertexFormat(class_290.field_1575, class_5596.field_27382)
         .build()
   );
   public static final Function<class_2960, class_1921> CHAIN_ESP = class_156.method_34866(var0 -> {
      class_12247 var1 = class_12247.method_75927(CHAIN_ESP_PIPELINE).method_75934("Sampler0", var0).method_75937().method_75929(1536).method_75938();
      return class_1921.method_75940("wtex", var1);
   });
   public static final RenderPipeline CRYSTAL_FILLED_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation("pipeline/crystal_filled")
         .withVertexShader("core/position_color")
         .withFragmentShader("core/position_color")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .build()
   );
   public static final class_1921 CRYSTAL_FILLED = class_1921.method_75940(
      "crystal_filled", class_12247.method_75927(CRYSTAL_FILLED_PIPELINE).method_75937().method_75929(8192).method_75938()
   );
   public static final RenderPipeline CRYSTAL_GLOW_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation("pipeline/crystal_glow")
         .withVertexShader("core/position_color")
         .withFragmentShader("core/position_color")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .build()
   );
   public static final class_1921 CRYSTAL_GLOW = class_1921.method_75940(
      "crystal_glow", class_12247.method_75927(CRYSTAL_GLOW_PIPELINE).method_75937().method_75929(4096).method_75938()
   );
   public static final RenderPipeline BLOOM_ESP_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation("pipeline/bloom_esp")
         .withVertexShader("core/position_tex_color")
         .withFragmentShader("core/position_tex_color")
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .withVertexFormat(class_290.field_1575, class_5596.field_27382)
         .build()
   );
   public static final Function<class_2960, class_1921> BLOOM_ESP = class_156.method_34866(var0 -> {
      class_12247 var1 = class_12247.method_75927(BLOOM_ESP_PIPELINE).method_75934("Sampler0", var0).method_75937().method_75929(2048).method_75938();
      return class_1921.method_75940("bloom_esp", var1);
   });
   public static final RenderPipeline CHINA_HAT_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation("pipeline/china_hat")
         .withVertexShader("core/position_color")
         .withFragmentShader("core/position_color")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(true)
         .withCull(false)
         .withVertexFormat(class_290.field_1576, class_5596.field_27381)
         .build()
   );
   public static final class_1921 CHINA_HAT = class_1921.method_75940(
      "china_hat", class_12247.method_75927(CHINA_HAT_PIPELINE).method_75937().method_75929(8192).method_75938()
   );
   public static final RenderPipeline CHINA_HAT_OUTLINE_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation("pipeline/china_hat_outline")
         .withVertexShader("core/position_color")
         .withFragmentShader("core/position_color")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(true)
         .withCull(false)
         .withVertexFormat(class_290.field_1576, class_5596.field_29345)
         .build()
   );
   public static final class_1921 CHINA_HAT_OUTLINE = class_1921.method_75940(
      "china_hat_outline", class_12247.method_75927(CHINA_HAT_OUTLINE_PIPELINE).method_75937().method_75929(4096).method_75938()
   );
   public static final RenderPipeline CHINA_HAT_GRID_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation("pipeline/china_hat_grid")
         .withVertexShader("core/position_color")
         .withFragmentShader("core/position_color")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(true)
         .withCull(false)
         .withVertexFormat(class_290.field_1576, class_5596.field_29344)
         .build()
   );
   public static final class_1921 CHINA_HAT_GRID = class_1921.method_75940(
      "china_hat_grid", class_12247.method_75927(CHINA_HAT_GRID_PIPELINE).method_75937().method_75929(8192).method_75938()
   );
   public static final RenderPipeline LINES_NO_DEPTH_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56859})
         .withLocation("pipeline/lines_no_depth")
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .build()
   );
   public static final class_1921 LINES_NO_DEPTH = class_1921.method_75940(
      "lines_no_depth", class_12247.method_75927(LINES_NO_DEPTH_PIPELINE).method_75937().method_75929(8192).method_75938()
   );
   public static final RenderPipeline QUADS_NO_DEPTH_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation("pipeline/quads_no_depth")
         .withVertexShader("core/position_color")
         .withFragmentShader("core/position_color")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .build()
   );
   public static final class_1921 QUADS_NO_DEPTH = class_1921.method_75940(
      "quads_no_depth", class_12247.method_75927(QUADS_NO_DEPTH_PIPELINE).method_75937().method_75929(8192).method_75938()
   );
   public static final RenderPipeline TEX_QUADS_NO_DEPTH_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation("pipeline/tex_quads_no_depth")
         .withVertexShader("core/position_tex_color")
         .withFragmentShader("core/position_tex_color")
         .withSampler("Sampler0")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .withVertexFormat(class_290.field_1575, class_5596.field_27382)
         .build()
   );
   public static final Function<class_2960, class_1921> TEX_QUADS_NO_DEPTH = class_156.method_34866(var0 -> {
      class_12247 var1 = class_12247.method_75927(TEX_QUADS_NO_DEPTH_PIPELINE).method_75934("Sampler0", var0).method_75937().method_75929(8192).method_75938();
      return class_1921.method_75940("tex_quads_no_depth", var1);
   });
   public static final RenderPipeline WHEEL_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation("pipeline/wheel")
         .withVertexShader("core/position_color")
         .withFragmentShader("core/position_color")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .withVertexFormat(class_290.field_1576, class_5596.field_27379)
         .build()
   );
   public static final class_1921 WHEEL = class_1921.method_75940(
      "wheel", class_12247.method_75927(WHEEL_PIPELINE).method_75937().method_75929(32768).method_75938()
   );
   public static final RenderPipeline WORLD_PARTICLES_COLOR_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("rich", "world_particles_color"))
         .withVertexFormat(class_290.field_1576, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   public static final class_1921 WORLD_PARTICLES_QUADS = class_1921.method_75940(
      "world_particles_cube", class_12247.method_75927(WORLD_PARTICLES_COLOR_PIPELINE).method_75937().method_75929(2048).method_75938()
   );
   public static final RenderPipeline WORLD_PARTICLES_LINES_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56860})
         .withLocation(class_2960.method_60655("rich", "world_particles_lines"))
         .withVertexFormat(class_290.field_1576, class_5596.field_29344)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   public static final class_1921 WORLD_PARTICLES_LINES = class_1921.method_75940(
      "world_particles_lines", class_12247.method_75927(WORLD_PARTICLES_LINES_PIPELINE).method_75937().method_75929(2048).method_75938()
   );
   public static final RenderPipeline WORLD_PARTICLES_GLOW_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_56864})
         .withLocation(class_2960.method_60655("rich", "world_particles_glow"))
         .withVertexFormat(class_290.field_1575, class_5596.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .withSampler("Sampler0")
         .build()
   );
   public static final Function<class_2960, class_1921> WORLD_PARTICLES_GLOW = class_156.method_34866(
      var0 -> {
         class_12247 var1 = class_12247.method_75927(WORLD_PARTICLES_GLOW_PIPELINE)
            .method_75934("Sampler0", var0)
            .method_75937()
            .method_75929(2048)
            .method_75938();
         return class_1921.method_75940("world_particles_glow", var1);
      }
   );
   public static final RenderPipeline GUI_ARROW_BLEND_PIPELINE = class_10799.method_67887(
      RenderPipeline.builder(new Snippet[]{class_10799.field_60125})
         .withLocation("pipeline/gui_arrow_blend")
         .withVertexShader("core/position_tex_color")
         .withFragmentShader("core/position_tex_color")
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withCull(false)
         .withVertexFormat(class_290.field_1575, class_5596.field_27382)
         .build()
   );
   public static final Function<class_2960, class_1921> GUI_ARROW_BLEND = class_156.method_34866(var0 -> {
      class_12247 var1 = class_12247.method_75927(GUI_ARROW_BLEND_PIPELINE).method_75934("Sampler0", var0).method_75937().method_75929(256).method_75938();
      return class_1921.method_75940("gui_arrow_blend", var1);
   });
}
