package rich.modules.impl.combat.aura.target;

import java.security.SecureRandom;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.world.RaycastContext.ShapeType;
import rich.IMinecraft;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.MathAngle;

public class MultiPoint implements IMinecraft {
   private final Random random = new SecureRandom();
   private Vec3d offset = Vec3d.ZERO;

   public Pair<Vec3d, Box> computeVector(LivingEntity var1, float var2, Angle var3, Vec3d var4, boolean var5) {
      Pair var6 = this.generateCandidatePoints(var1, var2, var5);
      Vec3d var7 = this.findBestVector((List<Vec3d>)var6.getLeft(), var3);
      return new Pair((var7 == null ? var1.getEyePos() : var7).add(this.offset), (Box)var6.getRight());
   }

   public Pair<List<Vec3d>, Box> generateCandidatePoints(LivingEntity var1, float var2, boolean var3) {
      Box var4 = var1.getBoundingBox();
      double var5 = var4.getLengthY() / 10.0;
      List var7 = Stream.<Double>iterate(var4.minY, var1x -> var1x <= var4.maxY, var2x -> var2x + var5)
         .map(var1x -> new Vec3d(var4.getCenter().x, var1x, var4.getCenter().z))
         .filter(var3x -> this.isValidPoint(mc.player.getEyePos(), var3x, var2, var3))
         .toList();
      return new Pair(var7, var4);
   }

   public boolean hasValidPoint(LivingEntity var1, float var2, boolean var3) {
      Box var4 = var1.getBoundingBox();
      double var5 = var4.getLengthY() / 10.0;
      return Stream.<Double>iterate(var4.minY, var1x -> var1x < var4.maxY, var2x -> var2x + var5)
         .map(var1x -> new Vec3d(var4.getCenter().x, var1x, var4.getCenter().z))
         .anyMatch(var3x -> this.isValidPoint(mc.player.getEyePos(), var3x, var2, var3));
   }

   private boolean isValidPoint(Vec3d var1, Vec3d var2, float var3, boolean var4) {
      return var1.distanceTo(var2) <= var3 && (var4 || !RaycastAngle.raycast(var1, var2, net.minecraft.world.RaycastContext.ShapeType.COLLIDER).getType().equals(net.minecraft.util.hit.HitResult.Type.BLOCK));
   }

   private Vec3d findBestVector(List<Vec3d> var1, Angle var2) {
      return var1.stream().min(Comparator.comparing(var2x -> this.calculateRotationDifference(mc.player.getEyePos(), var2x, var2))).orElse(null);
   }

   private double calculateRotationDifference(Vec3d var1, Vec3d var2, Angle var3) {
      Angle var4 = MathAngle.fromVec3d(var2.subtract(var1));
      Angle var5 = MathAngle.calculateDelta(var3, var4);
      return Math.hypot(var5.getYaw(), var5.getPitch());
   }

   private void updateOffset(Vec3d var1) {
      this.offset = this.offset.add(this.random.nextGaussian(), this.random.nextGaussian(), this.random.nextGaussian()).multiply(var1);
   }

   public Random getRandom() {
      return this.random;
   }

   public Vec3d getOffset() {
      return this.offset;
   }
}
