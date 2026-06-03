package rich.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.lang.reflect.Method;
import net.minecraft.class_1291;
import net.minecraft.class_1292;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_6880;
import net.minecraft.class_746;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.events.api.EventManager;
import rich.events.impl.JumpEvent;
import rich.events.impl.PushEvent;
import rich.events.impl.SwingDurationEvent;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.impl.combat.aura.AngleConstructor;

@Mixin(class_1309.class)
public abstract class LivingEntityMixin {
   @Shadow
   public float field_6283;
   @Unique
   private final class_310 client = class_310.method_1551();
   @Unique
   private static boolean baritoneChecked = false;
   @Unique
   private static boolean baritoneAvailable = false;
   @Unique
   private static Method getProviderMethod;
   @Unique
   private static Method getPrimaryBaritoneMethod;
   @Unique
   private static Method getPathingBehaviorMethod;
   @Unique
   private static Method isPathingMethod;

   @Shadow
   public abstract boolean method_6059(class_6880<class_1291> var1);

   @Shadow
   @Nullable
   public abstract class_1293 method_6112(class_6880<class_1291> var1);

   @Shadow
   public abstract boolean method_20232();

   @Shadow
   protected abstract double method_61426();

   @Unique
   private boolean isBaritonePathing() {
      try {
         if (!baritoneChecked) {
            baritoneChecked = true;

            try {
               Class var1 = Class.forName("baritone.api.BaritoneAPI");
               getProviderMethod = var1.getMethod("getProvider");
               Class var2 = Class.forName("baritone.api.IBaritoneProvider");
               getPrimaryBaritoneMethod = var2.getMethod("getPrimaryBaritone");
               Class var3 = Class.forName("baritone.api.IBaritone");
               getPathingBehaviorMethod = var3.getMethod("getPathingBehavior");
               Class var4 = Class.forName("baritone.api.behavior.IPathingBehavior");
               isPathingMethod = var4.getMethod("isPathing");
               baritoneAvailable = true;
            } catch (ClassNotFoundException | NoSuchMethodException var5) {
               baritoneAvailable = false;
            }
         }

         if (!baritoneAvailable) {
            return false;
         }

         Object var7 = getProviderMethod.invoke(null);
         if (var7 == null) {
            return false;
         }

         Object var8 = getPrimaryBaritoneMethod.invoke(var7);
         if (var8 == null) {
            return false;
         }

         Object var9 = getPathingBehaviorMethod.invoke(var8);
         if (var9 == null) {
            return false;
         }

         Object var10 = isPathingMethod.invoke(var9);
         return Boolean.TRUE.equals(var10);
      } catch (Exception var6) {
         return false;
      }
   }

   @Unique
   private boolean shouldApplyRichMoveCorrection() {
      AngleConnection var1 = AngleConnection.INSTANCE;
      Angle var2 = var1.getRotation();
      AngleConstructor var3 = var1.getCurrentRotationPlan();
      return var2 != null && var3 != null && var3.isMoveCorrection();
   }

   @Inject(method = "method_5810", at = @At("HEAD"), cancellable = true)
   public void isPushable(CallbackInfoReturnable<Boolean> var1) {
      PushEvent var2 = new PushEvent(PushEvent.Type.COLLISION);
      EventManager.callEvent(var2);
      if (var2.isCancelled()) {
         var1.setReturnValue(false);
      }
   }

   @ModifyExpressionValue(method = "method_6043", at = @At(value = "NEW", target = "(DDD)Lnet/minecraft/class_243;"))
   private class_243 hookFixRotation(class_243 var1) {
      if (this != this.client.field_1724) {
         return var1;
      }

      if (this.isBaritonePathing()) {
         return var1;
      }

      if (!this.shouldApplyRichMoveCorrection()) {
         return var1;
      }

      float var2 = AngleConnection.INSTANCE.getMoveRotation().getYaw() * (float) (Math.PI / 180.0);
      return new class_243(-class_3532.method_15374(var2) * 0.2F, 0.0, class_3532.method_15362(var2) * 0.2F);
   }

   @Inject(method = "method_6043", at = @At("HEAD"), cancellable = true)
   private void jump(CallbackInfo var1) {
      if (this instanceof class_746 var2) {
         if (this.isBaritonePathing()) {
            return;
         }

         JumpEvent var4 = new JumpEvent(var2);
         EventManager.callEvent(var4);
         if (var4.isCancelled()) {
            var1.cancel();
         }
      }
   }

   @Inject(method = "method_6028", at = @At("HEAD"), cancellable = true)
   private void swingProgressHook(CallbackInfoReturnable<Integer> var1) {
      if (this == this.client.field_1724) {
         SwingDurationEvent var2 = new SwingDurationEvent();
         EventManager.callEvent(var2);
         if (var2.isCancelled()) {
            float var3 = var2.getAnimation();
            if (class_1292.method_5576(this.client.field_1724)) {
               var3 *= 6 - (1 + class_1292.method_5575(this.client.field_1724));
            } else {
               var3 *= this.method_6059(class_1294.field_5901) ? 6 + (1 + this.method_6112(class_1294.field_5901).method_5578()) * 2 : 6;
            }

            var1.setReturnValue((int)var3);
         }
      }
   }

   @Inject(method = "method_61430", at = @At("HEAD"), cancellable = true)
   private void calcGlidingVelocityFull(class_243 var1, CallbackInfoReturnable<class_243> var2) {
      if (this == this.client.field_1724) {
         if (!this.isBaritonePathing()) {
            AngleConnection var3 = AngleConnection.INSTANCE;
            Angle var4 = var3.getRotation();
            AngleConstructor var5 = var3.getCurrentRotationPlan();
            if (var4 != null && var5 != null && var5.isMoveCorrection() && !var5.isChangeLook()) {
               class_243 var6 = var4.toVector();
               float var7 = var4.getPitch() * (float) (Math.PI / 180.0);
               double var8 = Math.sqrt(var6.field_1352 * var6.field_1352 + var6.field_1350 * var6.field_1350);
               double var10 = var1.method_37267();
               double var12 = this.method_61426();
               double var14 = class_3532.method_33723(Math.cos(var7));
               var1 = var1.method_1031(0.0, var12 * (-1.0 + var14 * 0.75), 0.0);
               if (var1.field_1351 < 0.0 && var8 > 0.0) {
                  double var16 = var1.field_1351 * -0.1 * var14;
                  var1 = var1.method_1031(var6.field_1352 * var16 / var8, var16, var6.field_1350 * var16 / var8);
               }

               if (var7 < 0.0F && var8 > 0.0) {
                  double var19 = var10 * -class_3532.method_15374(var7) * 0.04;
                  var1 = var1.method_1031(-var6.field_1352 * var19 / var8, var19 * 3.2, -var6.field_1350 * var19 / var8);
               }

               if (var8 > 0.0) {
                  var1 = var1.method_1031(
                     (var6.field_1352 / var8 * var10 - var1.field_1352) * 0.1, 0.0, (var6.field_1350 / var8 * var10 - var1.field_1350) * 0.1
                  );
               }

               var2.setReturnValue(var1.method_18805(0.99F, 0.98F, 0.99F));
               var2.cancel();
            }
         }
      }
   }
}
