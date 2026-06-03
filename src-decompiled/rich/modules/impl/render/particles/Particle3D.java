package rich.modules.impl.render.particles;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1921;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_7833;
import org.joml.Matrix4f;
import rich.IMinecraft;
import rich.modules.impl.render.worldparticles.ParticleRenderer;
import rich.util.a;
import rich.util.animations.Animation;
import rich.util.animations.Direction;
import rich.util.animations.EaseInOutQuad;
import rich.util.render.clientpipeline.ClientPipelines;

public class Particle3D implements IMinecraft {
   private static final Particle3D.ParticleMode[] RANDOM_MODES = new Particle3D.ParticleMode[]{
      Particle3D.ParticleMode.CUBES,
      Particle3D.ParticleMode.CROWN,
      Particle3D.ParticleMode.CUBE_BLAST,
      Particle3D.ParticleMode.DOLLAR,
      Particle3D.ParticleMode.HEART,
      Particle3D.ParticleMode.LIGHTNING,
      Particle3D.ParticleMode.LINE,
      Particle3D.ParticleMode.RHOMBUS,
      Particle3D.ParticleMode.SNOWFLAKE,
      Particle3D.ParticleMode.STAR,
      Particle3D.ParticleMode.STAR_ALT,
      Particle3D.ParticleMode.TRIANGLE
   };
   private static final class_2960 TEXTURE_CROWN = class_2960.method_60655("rich", "textures/world/crown.png");
   private static final class_2960 TEXTURE_CUBE_BLAST = class_2960.method_60655("rich", "textures/world/cubeblast1.png");
   private static final class_2960 TEXTURE_DOLLAR = class_2960.method_60655("rich", "textures/world/dollar.png");
   private static final class_2960 TEXTURE_HEART = class_2960.method_60655("rich", "textures/world/heart.png");
   private static final class_2960 TEXTURE_LIGHTNING = class_2960.method_60655("rich", "textures/world/lightning.png");
   private static final class_2960 TEXTURE_LINE = class_2960.method_60655("rich", "textures/world/line.png");
   private static final class_2960 TEXTURE_RHOMBUS = class_2960.method_60655("rich", "textures/world/rhombus.png");
   private static final class_2960 TEXTURE_SNOWFLAKE = class_2960.method_60655("rich", "textures/world/snowflake.png");
   private static final class_2960 TEXTURE_STAR = class_2960.method_60655("rich", "textures/world/star.png");
   private static final class_2960 TEXTURE_STAR_ALT = class_2960.method_60655("rich", "textures/world/star1.png");
   private static final class_2960 TEXTURE_TRIANGLE = class_2960.method_60655("rich", "textures/world/triangle.png");
   private static final class_2960 GLOW_BLOOM = class_2960.method_60655("rich", "textures/world/dashbloom.png");
   private static final class_2960 GLOW_BLOOM_SAMPLE = class_2960.method_60655("rich", "textures/world/dashbloomsample.png");
   private static final class_1921 LAYER_QUADS = ParticleRenderer.getQuadsLayer();
   private static final class_1921 LAYER_LINES = ParticleRenderer.getLinesLayer();
   private double x;
   private double y;
   private double z;
   private double lastX;
   private double lastY;
   private double lastZ;
   private double velocityX;
   private double velocityY;
   private double velocityZ;
   private long start;
   private float phase;
   private int color;
   private float scale;
   private long lifeTimeMs;
   private float rotation;
   private Animation fadeInAnimation;
   private Animation fadeOutAnimation;
   private float cachedAlpha = 0.0F;
   private long lastAlphaUpdate = 0L;
   private boolean fadingOut = false;
   private float gravityStrength = 0.04F;
   private float velocityMultiplier = 0.98F;
   private boolean collidesWithWorld = true;
   private Particle3D.ParticleMode actualMode = Particle3D.ParticleMode.CUBES;
   private Particle3D.GlowMode glowMode = Particle3D.GlowMode.BOTH;
   private boolean spinning = true;
   private class_1921 cachedGlowLayer1;
   private class_1921 cachedGlowLayer2;

   public Particle3D(class_243 var1, class_243 var2, int var3, float var4, float var5) {
      this.start = System.currentTimeMillis();
      ThreadLocalRandom var6 = ThreadLocalRandom.current();
      this.phase = var6.nextFloat() * 100.0F;
      this.rotation = var6.nextFloat() * 360.0F;
      this.x = var1.field_1352;
      this.y = var1.field_1351;
      this.z = var1.field_1350;
      this.lastX = var1.field_1352;
      this.lastY = var1.field_1351;
      this.lastZ = var1.field_1350;
      this.velocityX = var2.field_1352;
      this.velocityY = var2.field_1351;
      this.velocityZ = var2.field_1350;
      this.color = var3;
      this.scale = var4;
      this.lifeTimeMs = (long)(var5 * 1000.0F);
      this.fadeInAnimation = new EaseInOutQuad().setMs(150).setValue(1.0);
      this.fadeInAnimation.setDirection(Direction.FORWARDS);
      this.fadeOutAnimation = new EaseInOutQuad().setMs(250).setValue(1.0);
      this.fadeOutAnimation.setDirection(Direction.FORWARDS);
      this.rebuildGlowLayers();
   }

   public Particle3D setGravity(float var1) {
      this.gravityStrength = var1;
      return this;
   }

   public Particle3D setVelocityMultiplier(float var1) {
      this.velocityMultiplier = var1;
      return this;
   }

   public Particle3D setCollision(boolean var1) {
      this.collidesWithWorld = var1;
      return this;
   }

   public Particle3D setSpinning(boolean var1) {
      this.spinning = var1;
      return this;
   }

   public Particle3D setMode(Particle3D.ParticleMode var1) {
      this.actualMode = var1 == Particle3D.ParticleMode.RANDOM ? RANDOM_MODES[ThreadLocalRandom.current().nextInt(RANDOM_MODES.length)] : var1;
      return this;
   }

   public Particle3D setGlowMode(Particle3D.GlowMode var1) {
      this.glowMode = var1;
      this.rebuildGlowLayers();
      return this;
   }

   private void rebuildGlowLayers() {
      switch (this.glowMode) {
         case BLOOM:
            this.cachedGlowLayer1 = ClientPipelines.WORLD_PARTICLES_GLOW.apply(GLOW_BLOOM);
            this.cachedGlowLayer2 = null;
            break;
         case BLOOM_SAMPLE:
            this.cachedGlowLayer1 = ClientPipelines.WORLD_PARTICLES_GLOW.apply(GLOW_BLOOM_SAMPLE);
            this.cachedGlowLayer2 = null;
            break;
         case BOTH:
            this.cachedGlowLayer1 = ClientPipelines.WORLD_PARTICLES_GLOW.apply(GLOW_BLOOM);
            this.cachedGlowLayer2 = ClientPipelines.WORLD_PARTICLES_GLOW.apply(GLOW_BLOOM_SAMPLE);
      }
   }

   public void update() {
      long var1 = System.currentTimeMillis();
      this.lastX = this.x;
      this.lastY = this.y;
      this.lastZ = this.z;
      this.velocityY = this.velocityY - this.gravityStrength;
      if (this.collidesWithWorld && mc.field_1687 != null) {
         if (this.isHit(this.x + this.velocityX, this.y, this.z)) {
            this.velocityX *= -0.8;
         } else {
            this.x = this.x + this.velocityX;
         }

         if (this.isHit(this.x, this.y + this.velocityY, this.z)) {
            this.velocityX *= 0.999;
            this.velocityZ *= 0.999;
            this.velocityY *= -0.7;
         } else {
            this.y = this.y + this.velocityY;
         }

         if (this.isHit(this.x, this.y, this.z + this.velocityZ)) {
            this.velocityZ *= -0.8;
         } else {
            this.z = this.z + this.velocityZ;
         }
      } else {
         this.x = this.x + this.velocityX;
         this.y = this.y + this.velocityY;
         this.z = this.z + this.velocityZ;
      }

      this.velocityX = this.velocityX * this.velocityMultiplier;
      this.velocityZ = this.velocityZ * this.velocityMultiplier;
      if (this.spinning) {
         this.rotation += 2.0F;
      }

      if (!this.fadingOut && var1 - this.start > this.lifeTimeMs) {
         this.fadingOut = true;
         this.fadeOutAnimation.setDirection(Direction.BACKWARDS);
      }

      if (var1 - this.lastAlphaUpdate > 16L) {
         this.cachedAlpha = this.fadingOut ? this.fadeOutAnimation.getOutput().floatValue() : this.fadeInAnimation.getOutput().floatValue();
         this.lastAlphaUpdate = var1;
      }
   }

   private boolean isHit(double var1, double var3, double var5) {
      if (mc.field_1687 == null) {
         return false;
      }

      class_2338 var7 = class_2338.method_49637(var1, var3, var5);
      return mc.field_1687.method_8320(var7).method_26234(mc.field_1687, var7);
   }

   public boolean isDead() {
      return this.fadingOut && this.cachedAlpha <= 0.0F;
   }

   public float getAlpha() {
      return this.cachedAlpha;
   }

   private class_2960 getTexture() {
      return switch (this.actualMode) {
         case CROWN -> TEXTURE_CROWN;
         case CUBE_BLAST -> TEXTURE_CUBE_BLAST;
         case DOLLAR -> TEXTURE_DOLLAR;
         case HEART -> TEXTURE_HEART;
         case LIGHTNING -> TEXTURE_LIGHTNING;
         case LINE -> TEXTURE_LINE;
         case RHOMBUS -> TEXTURE_RHOMBUS;
         case SNOWFLAKE -> TEXTURE_SNOWFLAKE;
         case STAR -> TEXTURE_STAR;
         case STAR_ALT -> TEXTURE_STAR_ALT;
         case TRIANGLE -> TEXTURE_TRIANGLE;
         default -> null;
      };
   }

   public void render(class_4587 var1, class_4597 var2, float var3, float var4) {
      float var5 = this.cachedAlpha;
      if (!(var5 <= 0.01F)) {
         class_243 var6 = mc.field_1773.method_19418().method_71156();
         float var7 = mc.field_1773.method_19418().method_19330();
         float var8 = mc.field_1773.method_19418().method_19329();
         float var9 = (float)(class_3532.method_16436(var4, this.lastX, this.x) - var6.field_1352);
         float var10 = (float)(class_3532.method_16436(var4, this.lastY, this.y) - var6.field_1351);
         float var11 = (float)(class_3532.method_16436(var4, this.lastZ, this.z) - var6.field_1350);
         if (this.actualMode == Particle3D.ParticleMode.CUBES) {
            this.renderCube(var1, var2, var9, var10, var11, var5, var3, var7, var8);
         } else {
            this.renderTextured(var1, var2, var9, var10, var11, var5, var3, var7, var8);
         }
      }
   }

   private void renderCube(class_4587 var1, class_4597 var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9) {
      long var10 = System.currentTimeMillis();
      float var12 = (float)(var10 % 9000L) / 9000.0F * 360.0F;
      float var13 = this.scale * 0.25F;
      var1.method_22903();
      var1.method_46416(var3, var4, var5);
      var1.method_22907(class_7833.field_40716.rotationDegrees(var12 + this.phase));
      var1.method_22907(class_7833.field_40714.rotationDegrees(var12 * 0.5F));
      Matrix4f var14 = var1.method_23760().method_23761();
      ParticleRenderer.drawCube(var2.method_73477(LAYER_QUADS), var14, a.d(this.color, var6 * 0.2F), var13);
      ParticleRenderer.drawLines(var2.method_73477(LAYER_LINES), var14, a.d(this.color, var6 * 0.4F), var13);
      var1.method_22909();
      var1.method_22903();
      var1.method_46416(var3, var4, var5);
      var1.method_22907(class_7833.field_40716.rotationDegrees(-var8));
      var1.method_22907(class_7833.field_40714.rotationDegrees(var9));
      this.renderGlowEffect(var2, var1.method_23760().method_23761(), a.d(this.color, var6), var6, var13 * var7, var13 * (var7 / 3.0F));
      var1.method_22909();
   }

   private void renderTextured(class_4587 var1, class_4597 var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9) {
      class_2960 var10 = this.getTexture();
      if (var10 != null) {
         int var11 = a.d(this.color, var6);
         float var12 = this.scale * 0.5F;
         float var13 = var12 / 2.0F;
         int var14 = var11 >> 16 & 0xFF;
         int var15 = var11 >> 8 & 0xFF;
         int var16 = var11 & 0xFF;
         int var17 = (int)(255.0F * var6);
         var1.method_22903();
         var1.method_46416(var3, var4, var5);
         var1.method_22907(class_7833.field_40716.rotationDegrees(-var8));
         var1.method_22907(class_7833.field_40714.rotationDegrees(var9));
         if (this.spinning) {
            var1.method_22907(class_7833.field_40718.rotationDegrees(this.rotation));
         }

         Matrix4f var18 = var1.method_23760().method_23761();
         class_4588 var19 = var2.method_73477(ClientPipelines.WORLD_PARTICLES_GLOW.apply(var10));
         var19.method_22918(var18, -var13, -var13, 0.0F).method_22913(0.0F, 0.0F).method_1336(var14, var15, var16, var17);
         var19.method_22918(var18, -var13, var13, 0.0F).method_22913(0.0F, 1.0F).method_1336(var14, var15, var16, var17);
         var19.method_22918(var18, var13, var13, 0.0F).method_22913(1.0F, 1.0F).method_1336(var14, var15, var16, var17);
         var19.method_22918(var18, var13, -var13, 0.0F).method_22913(1.0F, 0.0F).method_1336(var14, var15, var16, var17);
         var1.method_22909();
         var1.method_22903();
         var1.method_46416(var3, var4, var5);
         var1.method_22907(class_7833.field_40716.rotationDegrees(-var8));
         var1.method_22907(class_7833.field_40714.rotationDegrees(var9));
         this.renderGlowEffect(var2, var1.method_23760().method_23761(), var11, var6, var12 * var7 * 0.5F, var12 * var7 * 0.2F);
         var1.method_22909();
      }
   }

   private void renderGlowEffect(class_4597 var1, Matrix4f var2, int var3, float var4, float var5, float var6) {
      switch (this.glowMode) {
         case BLOOM:
            ParticleRenderer.drawGlow(var1.method_73477(this.cachedGlowLayer1), var2, var3, (int)(80.0F * var4), var5);
            break;
         case BLOOM_SAMPLE:
            ParticleRenderer.drawGlow(var1.method_73477(this.cachedGlowLayer1), var2, var3, (int)(140.0F * var4), var6);
            break;
         case BOTH:
            ParticleRenderer.drawGlow(var1.method_73477(this.cachedGlowLayer1), var2, var3, (int)(80.0F * var4), var5);
            ParticleRenderer.drawGlow(var1.method_73477(this.cachedGlowLayer2), var2, var3, (int)(140.0F * var4), var6);
      }
   }

   public enum GlowMode {
      BLOOM,
      BLOOM_SAMPLE,
      BOTH;
   }

   public enum ParticleMode {
      CUBES,
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
      RANDOM;
   }
}
