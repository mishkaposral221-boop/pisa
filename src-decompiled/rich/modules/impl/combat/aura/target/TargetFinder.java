package rich.modules.impl.combat.aura.target;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.class_1297;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1429;
import net.minecraft.class_1531;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import rich.IMinecraft;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.impl.combat.aura.impl.LinearConstructor;
import rich.util.repository.friend.FriendUtils;

public class TargetFinder implements IMinecraft {
   private final MultiPoint pointFinder = new MultiPoint();
   private class_1309 currentTarget = null;
   private Stream<class_1309> potentialTargets;

   public void lockTarget(class_1309 var1) {
      if (this.currentTarget == null) {
         this.currentTarget = var1;
      }
   }

   public void releaseTarget() {
      this.currentTarget = null;
   }

   public void validateTarget(Predicate<class_1309> var1) {
      this.findFirstMatch(var1).ifPresent(this::lockTarget);
      if (this.currentTarget != null && !var1.test(this.currentTarget)) {
         this.releaseTarget();
      }
   }

   public void searchTargets(Iterable<class_1297> var1, float var2, float var3, boolean var4) {
      if (this.currentTarget != null && (!this.pointFinder.hasValidPoint(this.currentTarget, var2, var4) || this.getFov(this.currentTarget, var2, var4) > var3)
         )
       {
         this.releaseTarget();
      }

      this.potentialTargets = this.createStreamFromEntities(var1, var2, var3, var4);
   }

   private double getFov(class_1309 var1, float var2, boolean var3) {
      class_243 var4 = (class_243)this.pointFinder
         .computeVector(var1, var2, AngleConnection.INSTANCE.getRotation(), new LinearConstructor().randomValue(), var3)
         .method_15442();
      return RaycastAngle.rayTrace(var2, var1.method_5829())
         ? 0.0
         : AngleConnection.computeRotationDifference(MathAngle.cameraAngle(), MathAngle.calculateAngle(var4));
   }

   private Stream<class_1309> createStreamFromEntities(Iterable<class_1297> var1, float var2, float var3, boolean var4) {
      return StreamSupport.<class_1297>stream(var1.spliterator(), false)
         .filter(class_1309.class::isInstance)
         .map(class_1309.class::cast)
         .filter(var4x -> this.pointFinder.hasValidPoint(var4x, var2, var4) && this.getFov(var4x, var2, var4) < var3)
         .sorted(Comparator.comparingDouble(var0 -> var0.method_5739(mc.field_1724)));
   }

   private Optional<class_1309> findFirstMatch(Predicate<class_1309> var1) {
      return this.potentialTargets.filter(var1).findFirst();
   }

   public MultiPoint getPointFinder() {
      return this.pointFinder;
   }

   public class_1309 getCurrentTarget() {
      return this.currentTarget;
   }

   public Stream<class_1309> getPotentialTargets() {
      return this.potentialTargets;
   }

   public static class EntityFilter {
      private final List<String> targetSettings;

      public boolean isValid(class_1309 var1) {
         if (this.isLocalPlayer(var1)) {
            return false;
         } else if (this.isInvalidHealth(var1)) {
            return false;
         } else {
            return this.isBotPlayer(var1) ? false : this.isValidEntityType(var1);
         }
      }

      private boolean isLocalPlayer(class_1309 var1) {
         return var1 == IMinecraft.mc.field_1724;
      }

      private boolean isInvalidHealth(class_1309 var1) {
         return !var1.method_5805() || var1.method_6032() <= 0.0F;
      }

      private boolean isBotPlayer(class_1309 var1) {
         return false;
      }

      private boolean isFriendPlayer(class_1309 var1) {
         return var1 instanceof class_1657 && FriendUtils.isFriend(var1);
      }

      private boolean isValidEntityType(class_1309 var1) {
         if (var1 instanceof class_1657 var2) {
            return FriendUtils.isFriend(var2) ? this.targetSettings.contains("Друзья") : this.targetSettings.contains("Игроки");
         } else if (var1 instanceof class_1429) {
            return this.targetSettings.contains("Животные");
         } else if (var1 instanceof class_1308) {
            return this.targetSettings.contains("Мобы");
         } else {
            return var1 instanceof class_1531 ? this.targetSettings.contains("Стойки для брони") : false;
         }
      }

      public EntityFilter(List<String> var1) {
         this.targetSettings = var1;
      }
   }
}
