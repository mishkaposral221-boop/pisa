package rich.modules.impl.combat.aura.impl;

import net.minecraft.class_1297;
import net.minecraft.class_243;
import rich.IMinecraft;
import rich.modules.impl.combat.aura.Angle;

public abstract class RotateConstructor implements IMinecraft {
   private final String name;

   public Angle limitAngleChange(Angle var1, Angle var2) {
      return this.limitAngleChange(var1, var2, null, null);
   }

   public Angle limitAngleChange(Angle var1, Angle var2, class_243 var3) {
      return this.limitAngleChange(var1, var2, var3, null);
   }

   public abstract Angle limitAngleChange(Angle var1, Angle var2, class_243 var3, class_1297 var4);

   public abstract class_243 randomValue();

   public String getName() {
      return this.name;
   }

   public RotateConstructor(String var1) {
      this.name = var1;
   }
}
