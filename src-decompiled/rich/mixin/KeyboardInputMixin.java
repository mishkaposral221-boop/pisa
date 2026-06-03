package rich.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.class_10185;
import net.minecraft.class_3532;
import net.minecraft.class_743;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import rich.IMinecraft;
import rich.events.api.EventManager;
import rich.events.impl.InputEvent;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.impl.combat.aura.AngleConstructor;

@Mixin(class_743.class)
public class KeyboardInputMixin {
   @ModifyExpressionValue(method = "method_3129", at = @At(value = "NEW", target = "(ZZZZZZZ)Lnet/minecraft/class_10185;"))
   private class_10185 tickHook(class_10185 var1) {
      InputEvent var2 = new InputEvent(var1);
      EventManager.callEvent(var2);
      return this.transformInput(var2.getInput());
   }

   @Unique
   private class_10185 transformInput(class_10185 var1) {
      AngleConnection var2 = AngleConnection.INSTANCE;
      Angle var3 = var2.getCurrentAngle();
      AngleConstructor var4 = var2.getCurrentRotationPlan();
      if (IMinecraft.mc.field_1724 != null && var3 != null && var4 != null && var4.isMoveCorrection() && var4.isFreeCorrection()) {
         float var5 = IMinecraft.mc.field_1724.method_36454() - var3.getYaw();
         float var6 = class_743.method_40218(var1.comp_3159(), var1.comp_3160());
         float var7 = class_743.method_40218(var1.comp_3161(), var1.comp_3162());
         float var8 = var7 * class_3532.method_15362(var5 * (float) (Math.PI / 180.0)) - var6 * class_3532.method_15374(var5 * (float) (Math.PI / 180.0));
         float var9 = var6 * class_3532.method_15362(var5 * (float) (Math.PI / 180.0)) + var7 * class_3532.method_15374(var5 * (float) (Math.PI / 180.0));
         int var10 = Math.round(var8);
         int var11 = Math.round(var9);
         return new class_10185(var11 > 0.0F, var11 < 0.0F, var10 > 0.0F, var10 < 0.0F, var1.comp_3163(), var1.comp_3164(), var1.comp_3165());
      } else {
         return var1;
      }
   }
}
