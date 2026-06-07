package rich.modules.impl.combat.aura.target;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import rich.IMinecraft;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.impl.combat.aura.impl.LinearConstructor;
import rich.util.repository.friend.FriendUtils;

public class TargetFinder implements IMinecraft {
   private final MultiPoint pointFinder = new MultiPoint();
   private LivingEntity currentTarget = null;
   private Stream<LivingEntity> potentialTargets;

   public void lockTarget(LivingEntity var1) {
      if (this.currentTarget == null) {
         this.currentTarget = var1;
      }
   }

   public void releaseTarget() {
      this.currentTarget = null;
   }

   public void validateTarget(Predicate<LivingEntity> var1) {
      this.findFirstMatch(var1).ifPresent(this::lockTarget);
      if (this.currentTarget != null && !var1.test(this.currentTarget)) {
         this.releaseTarget();
      }
   }

   public void searchTargets(Iterable<Entity> var1, float var2, float var3, boolean var4) {
      if (this.currentTarget != null && (!this.pointFinder.hasValidPoint(this.currentTarget, var2, var4) || this.getFov(this.currentTarget, var2, var4) > var3)
         )
       {
         this.releaseTarget();
      }

      this.potentialTargets = this.createStreamFromEntities(var1, var2, var3, var4);
   }

   private double getFov(LivingEntity var1, float var2, boolean var3) {
      Vec3d var4 = (Vec3d)this.pointFinder
         .computeVector(var1, var2, AngleConnection.INSTANCE.getRotation(), new LinearConstructor().randomValue(), var3)
         .getLeft();
      return RaycastAngle.rayTrace(var2, var1.getBoundingBox())
         ? 0.0
         : AngleConnection.computeRotationDifference(MathAngle.cameraAngle(), MathAngle.calculateAngle(var4));
   }

   private Stream<LivingEntity> createStreamFromEntities(Iterable<Entity> var1, float var2, float var3, boolean var4) {
      return StreamSupport.<Entity>stream(var1.spliterator(), false)
         .filter(LivingEntity.class::isInstance)
         .map(LivingEntity.class::cast)
         .filter(var4x -> this.pointFinder.hasValidPoint(var4x, var2, var4) && this.getFov(var4x, var2, var4) < var3)
         .sorted(Comparator.comparingDouble(var0 -> var0.distanceTo(mc.player)));
   }

   private Optional<LivingEntity> findFirstMatch(Predicate<LivingEntity> var1) {
      return this.potentialTargets.filter(var1).findFirst();
   }

   public MultiPoint getPointFinder() {
      return this.pointFinder;
   }

   public LivingEntity getCurrentTarget() {
      return this.currentTarget;
   }

   public Stream<LivingEntity> getPotentialTargets() {
      return this.potentialTargets;
   }

   public static class EntityFilter {
      private final List<String> targetSettings;

      public boolean isValid(LivingEntity var1) {
         if (this.isLocalPlayer(var1)) {
            return false;
         } else if (this.isInvalidHealth(var1)) {
            return false;
         } else {
            return this.isBotPlayer(var1) ? false : this.isValidEntityType(var1);
         }
      }

      private boolean isLocalPlayer(LivingEntity var1) {
         return var1 == IMinecraft.mc.player;
      }

      private boolean isInvalidHealth(LivingEntity var1) {
         return !var1.isAlive() || var1.getHealth() <= 0.0F;
      }

      private boolean isBotPlayer(LivingEntity var1) {
         return false;
      }

      private boolean isFriendPlayer(LivingEntity var1) {
         return var1 instanceof PlayerEntity && FriendUtils.isFriend(var1);
      }

      private boolean isValidEntityType(LivingEntity var1) {
         if (var1 instanceof PlayerEntity var2) {
            return FriendUtils.isFriend(var2) ? this.targetSettings.contains("Друзья") : this.targetSettings.contains("Игроки");
         } else if (var1 instanceof AnimalEntity) {
            return this.targetSettings.contains("Животные");
         } else if (var1 instanceof MobEntity) {
            return this.targetSettings.contains("Мобы");
         } else {
            return var1 instanceof ArmorStandEntity ? this.targetSettings.contains("Стойки для брони") : false;
         }
      }

      public EntityFilter(List<String> var1) {
         this.targetSettings = var1;
      }
   }
}
