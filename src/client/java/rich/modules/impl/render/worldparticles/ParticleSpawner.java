package rich.modules.impl.render.worldparticles;

import net.minecraft.util.math.Vec3d;

public class ParticleSpawner {
   private static final double MIN_RADIUS = 3.0;
   private static final double MAX_RADIUS = 60.0;
   private static final double MAX_HEIGHT = 25.0;
   private static final double DESPAWN_DISTANCE = 65.0;

   public static Particle createParticle(Vec3d var0, Vec3d var1, double var2, long var4, Particle.ParticleType var6) {
      double var7 = 3.0 + Math.random() * 57.0;
      double var9 = Math.random() * Math.PI * 2.0;
      double var11 = var0.x;
      double var13 = var0.z;
      if (var2 > 0.05 && var1.horizontalLength() > 0.01) {
         Vec3d var15 = var1.normalize();
         double var16 = Math.atan2(var15.z, var15.x);
         double var18 = Math.PI * 2.0 / 5.0;
         var9 = var16 + (Math.random() - 0.5) * var18 * 2.0;
         double var20 = var7 * 0.7 * Math.min(var2 * 8.0, 1.0);
         var11 += var15.x * var20;
         var13 += var15.z * var20;
      }

      double var27 = var11 + Math.cos(var9) * var7;
      double var17 = var13 + Math.sin(var9) * var7;
      double var19 = var0.y - 5.0 + Math.random() * 25.0;
      double var21 = (Math.random() - 0.5) * 0.08;
      double var23 = (Math.random() - 0.5) * 0.02;
      double var25 = (Math.random() - 0.5) * 0.08;
      return new Particle(var27, var19, var17, var21, var23, var25, var4).setType(var6);
   }

   public static Particle createParticle(Vec3d var0, Vec3d var1, double var2, long var4) {
      return createParticle(var0, var1, var2, var4, Particle.ParticleType.CUBE_3D);
   }

   public static int calculateSpawnDelay(double var0) {
      byte var2 = 40;
      int var3 = var2;
      if (var0 > 0.05) {
         double var4 = Math.min(var0 * 5.0, 4.0);
         var3 = (int)(var2 / (1.0 + var4));
         var3 = Math.max(var3, 8);
      }

      return var3;
   }

   public static int calculateSpawnCount(double var0, int var2, int var3) {
      int var4 = 1;
      if (var0 > 0.1) {
         var4 = Math.min(8, var3 - var2);
         var4 = Math.max(1, (int)(var4 * Math.min(var0 * 5.0, 1.0)));
      }

      return var4;
   }

   public static double getDespawnDistance() {
      return 65.0;
   }

   public static double getDespawnDistanceSquared() {
      return 4225.0;
   }
}
