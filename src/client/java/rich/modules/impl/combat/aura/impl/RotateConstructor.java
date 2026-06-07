package rich.modules.impl.combat.aura.impl;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import rich.IMinecraft;
import rich.modules.impl.combat.aura.Angle;

public abstract class RotateConstructor implements IMinecraft {
   private final String name;

   public Angle limitAngleChange(Angle var1, Angle var2) {
      return this.limitAngleChange(var1, var2, null, null);
   }

   public Angle limitAngleChange(Angle var1, Angle var2, Vec3d var3) {
      return this.limitAngleChange(var1, var2, var3, null);
   }

   public abstract Angle limitAngleChange(Angle var1, Angle var2, Vec3d var3, Entity var4);

   public abstract Vec3d randomValue();

   public String getName() {
      return this.name;
   }

   public RotateConstructor(String var1) {
      this.name = var1;
   }
}
