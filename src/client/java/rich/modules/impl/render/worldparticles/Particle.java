package rich.modules.impl.render.worldparticles;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import rich.IMinecraft;
import rich.util.render.clientpipeline.ClientPipelines;

public class Particle implements IMinecraft {
   private static final Particle.ParticleType[] RANDOM_TYPES = new Particle.ParticleType[]{
      Particle.ParticleType.CROWN,
      Particle.ParticleType.CUBE_BLAST,
      Particle.ParticleType.DOLLAR,
      Particle.ParticleType.HEART,
      Particle.ParticleType.LIGHTNING,
      Particle.ParticleType.LINE,
      Particle.ParticleType.RHOMBUS,
      Particle.ParticleType.SNOWFLAKE,
      Particle.ParticleType.STAR,
      Particle.ParticleType.STAR_ALT,
      Particle.ParticleType.TRIANGLE
   };
   private static final Identifier TEX_CROWN = Identifier.of("rich", "textures/world/crown.png");
   private static final Identifier TEX_CUBE = Identifier.of("rich", "textures/world/cubeblast1.png");
   private static final Identifier TEX_DOLLAR = Identifier.of("rich", "textures/world/dollar.png");
   private static final Identifier TEX_HEART = Identifier.of("rich", "textures/world/heart.png");
   private static final Identifier TEX_LIGHTNING = Identifier.of("rich", "textures/world/lightning.png");
   private static final Identifier TEX_LINE = Identifier.of("rich", "textures/world/line.png");
   private static final Identifier TEX_RHOMBUS = Identifier.of("rich", "textures/world/rhombus.png");
   private static final Identifier TEX_SNOWFLAKE = Identifier.of("rich", "textures/world/snowflake.png");
   private static final Identifier TEX_STAR = Identifier.of("rich", "textures/world/star.png");
   private static final Identifier TEX_STAR_ALT = Identifier.of("rich", "textures/world/star1.png");
   private static final Identifier TEX_TRIANGLE = Identifier.of("rich", "textures/world/triangle.png");
   private static final Identifier TEX_GLOW = Identifier.of("rich", "textures/world/dashbloom.png");
   private static final RenderLayer LAYER_QUADS = ParticleRenderer.getQuadsLayer();
   private static final RenderLayer LAYER_LINES = ParticleRenderer.getLinesLayer();
   double x;
   double y;
   double z;
   double mX;
   double mY;
   double mZ;
   private final long startTime;
   private final long lifeTimeMs;
   private boolean fadingOut = false;
   float alpha = 0.0F;
   private final float phase;
   private float rotation;
   private final float rotationSpeed;
   private final int randomColor;
   private Particle.ParticleType actualType = Particle.ParticleType.CUBE_3D;
   private Identifier texture = null;
   private float size = 0.5F;
   private boolean physicsEnabled = false;
   private static final int[] RANDOM_COLORS = new int[]{-65536, -33024, -256, -16711936, -16711681, -16776961, -7667457, -65281, -60269, -1, -16711809, -40121};

   public Particle(double var1, double var3, double var5, double var7, double var9, double var11, long var13) {
      this.x = var1;
      this.y = var3;
      this.z = var5;
      this.mX = var7;
      this.mY = var9;
      this.mZ = var11;
      this.startTime = System.currentTimeMillis();
      this.lifeTimeMs = var13;
      ThreadLocalRandom var15 = ThreadLocalRandom.current();
      this.phase = var15.nextFloat() * 360.0F;
      this.rotation = var15.nextFloat() * 360.0F;
      this.rotationSpeed = var15.nextFloat() * 1.5F + 0.3F;
      this.randomColor = RANDOM_COLORS[var15.nextInt(RANDOM_COLORS.length)];
   }

   public Particle setType(Particle.ParticleType var1) {
      if (var1 == Particle.ParticleType.RANDOM) {
         this.actualType = RANDOM_TYPES[ThreadLocalRandom.current().nextInt(RANDOM_TYPES.length)];
      } else {
         this.actualType = var1;
      }

      this.texture = this.actualType == Particle.ParticleType.CUBE_3D ? null : getTexture(this.actualType);
      return this;
   }

   public Particle setPhysics(boolean var1) {
      this.physicsEnabled = var1;
      return this;
   }

   public Particle setSize(float var1) {
      this.size = var1;
      return this;
   }

   private static Identifier getTexture(Particle.ParticleType var0) {
      return switch (var0) {
         case CROWN -> TEX_CROWN;
         case CUBE_BLAST -> TEX_CUBE;
         case DOLLAR -> TEX_DOLLAR;
         case HEART -> TEX_HEART;
         case LIGHTNING -> TEX_LIGHTNING;
         case LINE -> TEX_LINE;
         case RHOMBUS -> TEX_RHOMBUS;
         case SNOWFLAKE -> TEX_SNOWFLAKE;
         case STAR -> TEX_STAR;
         case STAR_ALT -> TEX_STAR_ALT;
         case TRIANGLE -> TEX_TRIANGLE;
         case GLOW -> TEX_GLOW;
         default -> null;
      };
   }

   public double getDistanceSquaredTo(Vec3d var1) {
      double var2 = this.x - var1.x;
      double var4 = this.y - var1.y;
      double var6 = this.z - var1.z;
      return var2 * var2 + var4 * var4 + var6 * var6;
   }

   public double getHorizontalDistanceSquaredTo(Vec3d var1) {
      double var2 = this.x - var1.x;
      double var4 = this.z - var1.z;
      return var2 * var2 + var4 * var4;
   }

   public void startFadeOut() {
      this.fadingOut = true;
   }

   public boolean isFadingOut() {
      return this.fadingOut;
   }

   public boolean shouldRemove() {
      return this.fadingOut && this.alpha <= 0.0F;
   }

   public void update(long var1) {
      this.x = this.x + this.mX;
      this.y = this.y + this.mY;
      this.z = this.z + this.mZ;
      this.mX *= 0.98;
      this.mY *= 0.98;
      this.mZ *= 0.98;
      if (this.physicsEnabled) {
         this.mY -= 2.0E-4;
      }

      this.rotation = this.rotation + this.rotationSpeed;
      long var3 = var1 - this.startTime;
      if (!this.fadingOut && var3 > this.lifeTimeMs) {
         this.fadingOut = true;
      }

      if (this.fadingOut) {
         this.alpha = Math.max(0.0F, this.alpha - 0.04F);
      } else {
         this.alpha = Math.min(1.0F, this.alpha + 0.05F);
      }
   }

   public void render(
      MatrixStack var1, VertexConsumerProvider var2, Vec3d var3, int var4, float var5, float var6, float var7, float var8, boolean var9, boolean var10, boolean var11
   ) {
      if (!(this.alpha <= 0.01F)) {
         float var12 = (float)(this.x - var3.x);
         float var13 = (float)(this.y - var3.y);
         float var14 = (float)(this.z - var3.z);
         int var15 = var9 ? this.randomColor : var4;
         if (var10) {
            long var16 = System.currentTimeMillis() - this.startTime;
            if (var16 < 5000L) {
               float var18 = (float)var16 / 5000.0F;
               int var19 = (int)(255.0F + ((var15 >> 16 & 0xFF) - 255) * var18);
               int var20 = (int)(255.0F + ((var15 >> 8 & 0xFF) - 255) * var18);
               int var21 = (int)(255.0F + ((var15 & 0xFF) - 255) * var18);
               var15 = var15 & 0xFF000000 | var19 << 16 | var20 << 8 | var21;
            }
         }

         int var22 = (int)(this.alpha * 255.0F);
         int var17 = var15 >> 16 & 0xFF;
         int var23 = var15 >> 8 & 0xFF;
         int var24 = var15 & 0xFF;
         if (this.actualType == Particle.ParticleType.CUBE_3D) {
            this.renderCube(var1, var2, var12, var13, var14, var17, var23, var24, var22, var5);
         } else if (this.texture != null) {
            this.renderTextured(var1, var2, var12, var13, var14, var17, var23, var24, var22, var6, var7, var11);
         }
      }
   }

   private void renderCube(MatrixStack var1, VertexConsumerProvider var2, float var3, float var4, float var5, int var6, int var7, int var8, int var9, float var10) {
      float var11 = this.size * 0.4F;
      int var12 = (int)(var9 * 0.25F);
      int var13 = (int)(var9 * 0.5F);
      var1.push();
      var1.translate(var3, var4, var5);
      var1.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(var10 + this.phase));
      var1.multiply(RotationAxis.POSITIVE_X.rotationDegrees(var10 * 0.5F));
      Matrix4f var14 = var1.peek().getPositionMatrix();
      if (var12 > 0) {
         ParticleRenderer.drawCube(var2.getBuffer(LAYER_QUADS), var14, var12 << 24 | var6 << 16 | var7 << 8 | var8, var11);
      }

      if (var13 > 0) {
         ParticleRenderer.drawLines(var2.getBuffer(LAYER_LINES), var14, var13 << 24 | var6 << 16 | var7 << 8 | var8, var11);
      }

      var1.pop();
   }

   private void renderTextured(
      MatrixStack var1, VertexConsumerProvider var2, float var3, float var4, float var5, int var6, int var7, int var8, int var9, float var10, float var11, boolean var12
   ) {
      float var13 = this.size * 0.25F;
      RenderLayer var14 = ClientPipelines.WORLD_PARTICLES_GLOW.apply(this.texture);
      var1.push();
      var1.translate(var3, var4, var5);
      var1.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-var10));
      var1.multiply(RotationAxis.POSITIVE_X.rotationDegrees(var11));
      var1.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(this.rotation));
      Matrix4f var15 = var1.peek().getPositionMatrix();
      VertexConsumer var16 = var2.getBuffer(var14);
      var16.vertex(var15, -var13, -var13, 0.0F).texture(0.0F, 0.0F).color(var6, var7, var8, var9);
      var16.vertex(var15, -var13, var13, 0.0F).texture(0.0F, 1.0F).color(var6, var7, var8, var9);
      var16.vertex(var15, var13, var13, 0.0F).texture(1.0F, 1.0F).color(var6, var7, var8, var9);
      var16.vertex(var15, var13, -var13, 0.0F).texture(1.0F, 0.0F).color(var6, var7, var8, var9);
      if (var12) {
         float var17 = var13 * 0.4F;
         int var18 = (int)(var9 * 0.8F);
         var16.vertex(var15, -var17, -var17, 0.001F).texture(0.25F, 0.25F).color(255, 255, 255, var18);
         var16.vertex(var15, -var17, var17, 0.001F).texture(0.25F, 0.75F).color(255, 255, 255, var18);
         var16.vertex(var15, var17, var17, 0.001F).texture(0.75F, 0.75F).color(255, 255, 255, var18);
         var16.vertex(var15, var17, -var17, 0.001F).texture(0.75F, 0.25F).color(255, 255, 255, var18);
      }

      var1.pop();
   }

   public enum ParticleType {
      CUBE_3D,
      CROWN,
      CUBE_BLAST,
      DOLLAR,
      HEART,
      LIGHTNING,
      LINE,
      RHOMBUS,
      SNOWFLAKE,
      STAR,
      STAR_ALT,
      TRIANGLE,
      GLOW,
      RANDOM;
   }
}
