package rich.modules.impl.combat.aura;

import net.minecraft.class_1297;
import net.minecraft.class_243;
import rich.IMinecraft;
import rich.modules.impl.combat.aura.impl.RotateConstructor;

public class AngleConstructor implements IMinecraft {
   private final Angle angle;
   private final class_243 vec3d;
   private final class_1297 entity;
   private final RotateConstructor angleSmooth;
   private final int ticksUntilReset;
   private final float resetThreshold;
   public final boolean moveCorrection;
   public final boolean freeCorrection;
   public final boolean changeLook = false;

   public Angle nextRotation(Angle var1, boolean var2) {
      return var2
         ? this.angleSmooth.limitAngleChange(var1, MathAngle.fromVec2f(mc.field_1724.method_5802()))
         : this.angleSmooth.limitAngleChange(var1, this.angle, this.vec3d, this.entity);
   }

   public Angle getAngle() {
      return this.angle;
   }

   public class_243 getVec3d() {
      return this.vec3d;
   }

   public class_1297 getEntity() {
      return this.entity;
   }

   public RotateConstructor getAngleSmooth() {
      return this.angleSmooth;
   }

   public int getTicksUntilReset() {
      return this.ticksUntilReset;
   }

   public float getResetThreshold() {
      return this.resetThreshold;
   }

   public boolean isMoveCorrection() {
      return this.moveCorrection;
   }

   public boolean isChangeLook() {
      return false;
   }

   public AngleConstructor(Angle var1, class_243 var2, class_1297 var3, RotateConstructor var4, int var5, float var6, boolean var7, boolean var8) {
      this.angle = var1;
      this.vec3d = var2;
      this.entity = var3;
      this.angleSmooth = var4;
      this.ticksUntilReset = var5;
      this.resetThreshold = var6;
      this.moveCorrection = var7;
      this.freeCorrection = var8;
   }

   public boolean isFreeCorrection() {
      return this.freeCorrection;
   }
}
