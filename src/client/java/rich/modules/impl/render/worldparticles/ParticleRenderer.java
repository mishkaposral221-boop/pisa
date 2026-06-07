package rich.modules.impl.render.worldparticles;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.VertexConsumer;
import org.joml.Matrix4f;
import rich.util.render.clientpipeline.ClientPipelines;

public class ParticleRenderer {
   private static final Identifier GLOW_TEXTURE = Identifier.of("rich", "textures/world/dashbloom.png");
   private static final Identifier GLOW_TEXTURE_SECONDARY = Identifier.of("rich", "textures/world/dashbloomsample.png");

   public static RenderLayer getQuadsLayer() {
      return ClientPipelines.WORLD_PARTICLES_QUADS;
   }

   public static RenderLayer getLinesLayer() {
      return ClientPipelines.WORLD_PARTICLES_LINES;
   }

   public static RenderLayer getGlowLayer() {
      return ClientPipelines.WORLD_PARTICLES_GLOW.apply(GLOW_TEXTURE);
   }

   public static RenderLayer getGlowLayerSecondary() {
      return ClientPipelines.WORLD_PARTICLES_GLOW.apply(GLOW_TEXTURE_SECONDARY);
   }

   public static void drawCube(VertexConsumer var0, Matrix4f var1, int var2, float var3) {
      float var4 = var3 / 2.0F;
      int var5 = var2 >> 16 & 0xFF;
      int var6 = var2 >> 8 & 0xFF;
      int var7 = var2 & 0xFF;
      int var8 = var2 >> 24 & 0xFF;
      var0.vertex(var1, -var4, var4, -var4).color(var5, var6, var7, var8);
      var0.vertex(var1, -var4, var4, var4).color(var5, var6, var7, var8);
      var0.vertex(var1, var4, var4, var4).color(var5, var6, var7, var8);
      var0.vertex(var1, var4, var4, -var4).color(var5, var6, var7, var8);
      var0.vertex(var1, -var4, -var4, -var4).color(var5, var6, var7, var8);
      var0.vertex(var1, var4, -var4, -var4).color(var5, var6, var7, var8);
      var0.vertex(var1, var4, -var4, var4).color(var5, var6, var7, var8);
      var0.vertex(var1, -var4, -var4, var4).color(var5, var6, var7, var8);
      var0.vertex(var1, -var4, var4, var4).color(var5, var6, var7, var8);
      var0.vertex(var1, -var4, -var4, var4).color(var5, var6, var7, var8);
      var0.vertex(var1, var4, -var4, var4).color(var5, var6, var7, var8);
      var0.vertex(var1, var4, var4, var4).color(var5, var6, var7, var8);
      var0.vertex(var1, -var4, var4, -var4).color(var5, var6, var7, var8);
      var0.vertex(var1, var4, var4, -var4).color(var5, var6, var7, var8);
      var0.vertex(var1, var4, -var4, -var4).color(var5, var6, var7, var8);
      var0.vertex(var1, -var4, -var4, -var4).color(var5, var6, var7, var8);
      var0.vertex(var1, -var4, var4, -var4).color(var5, var6, var7, var8);
      var0.vertex(var1, -var4, -var4, -var4).color(var5, var6, var7, var8);
      var0.vertex(var1, -var4, -var4, var4).color(var5, var6, var7, var8);
      var0.vertex(var1, -var4, var4, var4).color(var5, var6, var7, var8);
      var0.vertex(var1, var4, var4, -var4).color(var5, var6, var7, var8);
      var0.vertex(var1, var4, var4, var4).color(var5, var6, var7, var8);
      var0.vertex(var1, var4, -var4, var4).color(var5, var6, var7, var8);
      var0.vertex(var1, var4, -var4, -var4).color(var5, var6, var7, var8);
   }

   public static void drawLines(VertexConsumer var0, Matrix4f var1, int var2, float var3) {
      float var4 = var3 / 2.0F;
      int var5 = var2 >> 16 & 0xFF;
      int var6 = var2 >> 8 & 0xFF;
      int var7 = var2 & 0xFF;
      int var8 = var2 >> 24 & 0xFF;
      line(var0, var1, -var4, -var4, -var4, var4, -var4, -var4, var5, var6, var7, var8);
      line(var0, var1, var4, -var4, -var4, var4, -var4, var4, var5, var6, var7, var8);
      line(var0, var1, var4, -var4, var4, -var4, -var4, var4, var5, var6, var7, var8);
      line(var0, var1, -var4, -var4, var4, -var4, -var4, -var4, var5, var6, var7, var8);
      line(var0, var1, -var4, var4, -var4, var4, var4, -var4, var5, var6, var7, var8);
      line(var0, var1, var4, var4, -var4, var4, var4, var4, var5, var6, var7, var8);
      line(var0, var1, var4, var4, var4, -var4, var4, var4, var5, var6, var7, var8);
      line(var0, var1, -var4, var4, var4, -var4, var4, -var4, var5, var6, var7, var8);
      line(var0, var1, -var4, -var4, -var4, -var4, var4, -var4, var5, var6, var7, var8);
      line(var0, var1, var4, -var4, -var4, var4, var4, -var4, var5, var6, var7, var8);
      line(var0, var1, var4, -var4, var4, var4, var4, var4, var5, var6, var7, var8);
      line(var0, var1, -var4, -var4, var4, -var4, var4, var4, var5, var6, var7, var8);
   }

   private static void line(
      VertexConsumer var0, Matrix4f var1, float var2, float var3, float var4, float var5, float var6, float var7, int var8, int var9, int var10, int var11
   ) {
      var0.vertex(var1, var2, var3, var4).color(var8, var9, var10, var11);
      var0.vertex(var1, var5, var6, var7).color(var8, var9, var10, var11);
   }

   public static void drawGlow(VertexConsumer var0, Matrix4f var1, int var2, int var3, float var4) {
      int var5 = var2 >> 16 & 0xFF;
      int var6 = var2 >> 8 & 0xFF;
      int var7 = var2 & 0xFF;
      float var8 = var4 / 2.0F;
      var0.vertex(var1, -var8, -var8, 0.0F).texture(0.0F, 0.0F).color(var5, var6, var7, var3);
      var0.vertex(var1, -var8, var8, 0.0F).texture(0.0F, 1.0F).color(var5, var6, var7, var3);
      var0.vertex(var1, var8, var8, 0.0F).texture(1.0F, 1.0F).color(var5, var6, var7, var3);
      var0.vertex(var1, var8, -var8, 0.0F).texture(1.0F, 0.0F).color(var5, var6, var7, var3);
   }
}
