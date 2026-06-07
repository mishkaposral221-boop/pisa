package rich.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import rich.IMinecraft;
import rich.events.api.EventManager;
import rich.events.impl.InputEvent;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.impl.combat.aura.AngleConstructor;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {
   @ModifyExpressionValue(method = "tick", at = @At(value = "NEW", target = "(ZZZZZZZ)Lnet/minecraft/util/PlayerInput;"))
   private PlayerInput tickHook(PlayerInput var1) {
      InputEvent var2 = new InputEvent(var1);
      EventManager.callEvent(var2);
      return this.transformInput(var2.getInput());
   }

   @Unique
   private PlayerInput transformInput(PlayerInput var1) {
      AngleConnection var2 = AngleConnection.INSTANCE;
      Angle var3 = var2.getCurrentAngle();
      AngleConstructor var4 = var2.getCurrentRotationPlan();
      if (IMinecraft.mc.player != null && var3 != null && var4 != null && var4.isMoveCorrection() && var4.isFreeCorrection()) {
         float var5 = IMinecraft.mc.player.getYaw() - var3.getYaw();
         float var6 = KeyboardInput.getMovementMultiplier(var1.forward(), var1.backward());
         float var7 = KeyboardInput.getMovementMultiplier(var1.left(), var1.right());
         float var8 = var7 * MathHelper.cos(var5 * (float) (Math.PI / 180.0)) - var6 * MathHelper.sin(var5 * (float) (Math.PI / 180.0));
         float var9 = var6 * MathHelper.cos(var5 * (float) (Math.PI / 180.0)) + var7 * MathHelper.sin(var5 * (float) (Math.PI / 180.0));
         int var10 = Math.round(var8);
         int var11 = Math.round(var9);
         return new PlayerInput(var11 > 0.0F, var11 < 0.0F, var10 > 0.0F, var10 < 0.0F, var1.jump(), var1.sneak(), var1.sprint());
      } else {
         return var1;
      }
   }
}
