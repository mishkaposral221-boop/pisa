package rich.modules.impl.render.worldparticles;

import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_4588;
import org.joml.Matrix4f;
import rich.util.render.clientpipeline.ClientPipelines;

public class ParticleRenderer {
   private static final class_2960 GLOW_TEXTURE = class_2960.method_60655("rich", "textures/world/dashbloom.png");
   private static final class_2960 GLOW_TEXTURE_SECONDARY = class_2960.method_60655("rich", "textures/world/dashbloomsample.png");

   public static class_1921 getQuadsLayer() {
      return ClientPipelines.WORLD_PARTICLES_QUADS;
   }

   public static class_1921 getLinesLayer() {
      return ClientPipelines.WORLD_PARTICLES_LINES;
   }

   public static class_1921 getGlowLayer() {
      return ClientPipelines.WORLD_PARTICLES_GLOW.apply(GLOW_TEXTURE);
   }

   public static class_1921 getGlowLayerSecondary() {
      return ClientPipelines.WORLD_PARTICLES_GLOW.apply(GLOW_TEXTURE_SECONDARY);
   }

   public static void drawCube(class_4588 var0, Matrix4f var1, int var2, float var3) {
      float var4 = var3 / 2.0F;
      int var5 = var2 >> 16 & 0xFF;
      int var6 = var2 >> 8 & 0xFF;
      int var7 = var2 & 0xFF;
      int var8 = var2 >> 24 & 0xFF;
      var0.method_22918(var1, -var4, var4, -var4).method_1336(var5, var6, var7, var8);
      var0.method_22918(var1, -var4, var4, var4).method_1336(var5, var6, var7, var8);
      var0.method_22918(var1, var4, var4, var4).method_1336(var5, var6, var7, var8);
      var0.method_22918(var1, var4, var4, -var4).method_1336(var5, var6, var7, var8);
      var0.method_22918(var1, -var4, -var4, -var4).method_1336(var5, var6, var7, var8);
      var0.method_22918(var1, var4, -var4, -var4).method_1336(var5, var6, var7, var8);
      var0.method_22918(var1, var4, -var4, var4).method_1336(var5, var6, var7, var8);
      var0.method_22918(var1, -var4, -var4, var4).method_1336(var5, var6, var7, var8);
      var0.method_22918(var1, -var4, var4, var4).method_1336(var5, var6, var7, var8);
      var0.method_22918(var1, -var4, -var4, var4).method_1336(var5, var6, var7, var8);
      var0.method_22918(var1, var4, -var4, var4).method_1336(var5, var6, var7, var8);
      var0.method_22918(var1, var4, var4, var4).method_1336(var5, var6, var7, var8);
      var0.method_22918(var1, -var4, var4, -var4).method_1336(var5, var6, var7, var8);
      var0.method_22918(var1, var4, var4, -var4).method_1336(var5, var6, var7, var8);
      var0.method_22918(var1, var4, -var4, -var4).method_1336(var5, var6, var7, var8);
      var0.method_22918(var1, -var4, -var4, -var4).method_1336(var5, var6, var7, var8);
      var0.method_22918(var1, -var4, var4, -var4).method_1336(var5, var6, var7, var8);
      var0.method_22918(var1, -var4, -var4, -var4).method_1336(var5, var6, var7, var8);
      var0.method_22918(var1, -var4, -var4, var4).method_1336(var5, var6, var7, var8);
      var0.method_22918(var1, -var4, var4, var4).method_1336(var5, var6, var7, var8);
      var0.method_22918(var1, var4, var4, -var4).method_1336(var5, var6, var7, var8);
      var0.method_22918(var1, var4, var4, var4).method_1336(var5, var6, var7, var8);
      var0.method_22918(var1, var4, -var4, var4).method_1336(var5, var6, var7, var8);
      var0.method_22918(var1, var4, -var4, -var4).method_1336(var5, var6, var7, var8);
   }

   public static void drawLines(class_4588 var0, Matrix4f var1, int var2, float var3) {
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
      class_4588 var0, Matrix4f var1, float var2, float var3, float var4, float var5, float var6, float var7, int var8, int var9, int var10, int var11
   ) {
      var0.method_22918(var1, var2, var3, var4).method_1336(var8, var9, var10, var11);
      var0.method_22918(var1, var5, var6, var7).method_1336(var8, var9, var10, var11);
   }

   public static void drawGlow(class_4588 var0, Matrix4f var1, int var2, int var3, float var4) {
      int var5 = var2 >> 16 & 0xFF;
      int var6 = var2 >> 8 & 0xFF;
      int var7 = var2 & 0xFF;
      float var8 = var4 / 2.0F;
      var0.method_22918(var1, -var8, -var8, 0.0F).method_22913(0.0F, 0.0F).method_1336(var5, var6, var7, var3);
      var0.method_22918(var1, -var8, var8, 0.0F).method_22913(0.0F, 1.0F).method_1336(var5, var6, var7, var3);
      var0.method_22918(var1, var8, var8, 0.0F).method_22913(1.0F, 1.0F).method_1336(var5, var6, var7, var3);
      var0.method_22918(var1, var8, -var8, 0.0F).method_22913(1.0F, 0.0F).method_1336(var5, var6, var7, var3);
   }
}
