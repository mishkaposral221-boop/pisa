package rich.modules.impl.combat.aura;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import rich.modules.impl.combat.aura.impl.LinearConstructor;
import rich.modules.impl.combat.aura.impl.RotateConstructor;

public class AngleConfig {
   public static AngleConfig DEFAULT = new AngleConfig(new LinearConstructor(), true, true);
   public static boolean moveCorrection;
   public static boolean freeCorrection;
   private final RotateConstructor angleSmooth;
   private final int resetThreshold = 1;

   public AngleConfig(boolean var1, boolean var2) {
      this(new LinearConstructor(), var1, var2);
   }

   public AngleConfig(boolean var1) {
      this(new LinearConstructor(), var1, true);
   }

   public AngleConfig(RotateConstructor var1, boolean var2, boolean var3) {
      this.angleSmooth = var1;
      moveCorrection = var2;
      freeCorrection = var3;
   }

   public AngleConstructor createRotationPlan(Angle var1, Vec3d var2, Entity var3, int var4) {
      return new AngleConstructor(var1, var2, var3, this.angleSmooth, var4, 1.0F, moveCorrection, freeCorrection);
   }

   public AngleConstructor createRotationPlan(Angle var1, Vec3d var2, Entity var3, boolean var4, boolean var5) {
      return new AngleConstructor(var1, var2, var3, this.angleSmooth, 1, 1.0F, var4, var5);
   }
}
