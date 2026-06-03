package rich.modules.impl.combat.aura.target;

import java.security.SecureRandom;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3545;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_3960;
import rich.IMinecraft;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.MathAngle;

public class MultiPoint implements IMinecraft {
   private final Random random = new SecureRandom();
   private class_243 offset = class_243.field_1353;

   public class_3545<class_243, class_238> computeVector(class_1309 var1, float var2, Angle var3, class_243 var4, boolean var5) {
      class_3545 var6 = this.generateCandidatePoints(var1, var2, var5);
      class_243 var7 = this.findBestVector((List<class_243>)var6.method_15442(), var3);
      return new class_3545((var7 == null ? var1.method_33571() : var7).method_1019(this.offset), (class_238)var6.method_15441());
   }

   public class_3545<List<class_243>, class_238> generateCandidatePoints(class_1309 var1, float var2, boolean var3) {
      class_238 var4 = var1.method_5829();
      double var5 = var4.method_17940() / 10.0;
      List var7 = Stream.<Double>iterate(var4.field_1322, var1x -> var1x <= var4.field_1325, var2x -> var2x + var5)
         .map(var1x -> new class_243(var4.method_1005().field_1352, var1x, var4.method_1005().field_1350))
         .filter(var3x -> this.isValidPoint(mc.field_1724.method_33571(), var3x, var2, var3))
         .toList();
      return new class_3545(var7, var4);
   }

   public boolean hasValidPoint(class_1309 var1, float var2, boolean var3) {
      class_238 var4 = var1.method_5829();
      double var5 = var4.method_17940() / 10.0;
      return Stream.<Double>iterate(var4.field_1322, var1x -> var1x < var4.field_1325, var2x -> var2x + var5)
         .map(var1x -> new class_243(var4.method_1005().field_1352, var1x, var4.method_1005().field_1350))
         .anyMatch(var3x -> this.isValidPoint(mc.field_1724.method_33571(), var3x, var2, var3));
   }

   private boolean isValidPoint(class_243 var1, class_243 var2, float var3, boolean var4) {
      return var1.method_1022(var2) <= var3 && (var4 || !RaycastAngle.raycast(var1, var2, class_3960.field_17558).method_17783().equals(class_240.field_1332));
   }

   private class_243 findBestVector(List<class_243> var1, Angle var2) {
      return var1.stream().min(Comparator.comparing(var2x -> this.calculateRotationDifference(mc.field_1724.method_33571(), var2x, var2))).orElse(null);
   }

   private double calculateRotationDifference(class_243 var1, class_243 var2, Angle var3) {
      Angle var4 = MathAngle.fromVec3d(var2.method_1020(var1));
      Angle var5 = MathAngle.calculateDelta(var3, var4);
      return Math.hypot(var5.getYaw(), var5.getPitch());
   }

   private void updateOffset(class_243 var1) {
      this.offset = this.offset.method_1031(this.random.nextGaussian(), this.random.nextGaussian(), this.random.nextGaussian()).method_18806(var1);
   }

   public Random getRandom() {
      return this.random;
   }

   public class_243 getOffset() {
      return this.offset;
   }
}
