package rich.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.lang.reflect.Method;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.client.network.ClientPlayerEntity;
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

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
   @Shadow
   public float bodyYaw;
   @Unique
   private final MinecraftClient client = MinecraftClient.getInstance();
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
   public abstract boolean hasStatusEffect(RegistryEntry<StatusEffect> var1);

   @Shadow
   @Nullable
   public abstract StatusEffectInstance getStatusEffect(RegistryEntry<StatusEffect> var1);

   @Shadow
   public abstract boolean isInSwimmingPose();

   @Shadow
   protected abstract double getEffectiveGravity();

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

   @Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
   public void isPushable(CallbackInfoReturnable<Boolean> var1) {
      PushEvent var2 = new PushEvent(PushEvent.Type.COLLISION);
      EventManager.callEvent(var2);
      if (var2.isCancelled()) {
         var1.setReturnValue(false);
      }
   }

   @ModifyExpressionValue(method = "jump", at = @At(value = "NEW", target = "(DDD)Lnet/minecraft/Vec3d;"))
   private Vec3d hookFixRotation(Vec3d var1) {
      if (this != this.client.player) {
         return var1;
      }

      if (this.isBaritonePathing()) {
         return var1;
      }

      if (!this.shouldApplyRichMoveCorrection()) {
         return var1;
      }

      float var2 = AngleConnection.INSTANCE.getMoveRotation().getYaw() * (float) (Math.PI / 180.0);
      return new Vec3d(-MathHelper.sin(var2) * 0.2F, 0.0, MathHelper.cos(var2) * 0.2F);
   }

   @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
   private void jump(CallbackInfo var1) {
      if (this instanceof ClientPlayerEntity var2) {
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

   @Inject(method = "getHandSwingDuration", at = @At("HEAD"), cancellable = true)
   private void swingProgressHook(CallbackInfoReturnable<Integer> var1) {
      if (this == this.client.player) {
         SwingDurationEvent var2 = new SwingDurationEvent();
         EventManager.callEvent(var2);
         if (var2.isCancelled()) {
            float var3 = var2.getAnimation();
            if (StatusEffectUtil.hasHaste(this.client.player)) {
               var3 *= 6 - (1 + StatusEffectUtil.getHasteAmplifier(this.client.player));
            } else {
               var3 *= this.hasStatusEffect(StatusEffects.MINING_FATIGUE) ? 6 + (1 + this.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) * 2 : 6;
            }

            var1.setReturnValue((int)var3);
         }
      }
   }

   @Inject(method = "calcGlidingVelocity", at = @At("HEAD"), cancellable = true)
   private void calcGlidingVelocityFull(Vec3d var1, CallbackInfoReturnable<Vec3d> var2) {
      if (this == this.client.player) {
         if (!this.isBaritonePathing()) {
            AngleConnection var3 = AngleConnection.INSTANCE;
            Angle var4 = var3.getRotation();
            AngleConstructor var5 = var3.getCurrentRotationPlan();
            if (var4 != null && var5 != null && var5.isMoveCorrection() && !var5.isChangeLook()) {
               Vec3d var6 = var4.toVector();
               float var7 = var4.getPitch() * (float) (Math.PI / 180.0);
               double var8 = Math.sqrt(var6.x * var6.x + var6.z * var6.z);
               double var10 = var1.horizontalLength();
               double var12 = this.getEffectiveGravity();
               double var14 = MathHelper.square(Math.cos(var7));
               var1 = var1.add(0.0, var12 * (-1.0 + var14 * 0.75), 0.0);
               if (var1.y < 0.0 && var8 > 0.0) {
                  double var16 = var1.y * -0.1 * var14;
                  var1 = var1.add(var6.x * var16 / var8, var16, var6.z * var16 / var8);
               }

               if (var7 < 0.0F && var8 > 0.0) {
                  double var19 = var10 * -MathHelper.sin(var7) * 0.04;
                  var1 = var1.add(-var6.x * var19 / var8, var19 * 3.2, -var6.z * var19 / var8);
               }

               if (var8 > 0.0) {
                  var1 = var1.add(
                     (var6.x / var8 * var10 - var1.x) * 0.1, 0.0, (var6.z / var8 * var10 - var1.z) * 0.1
                  );
               }

               var2.setReturnValue(var1.multiply(0.99F, 0.98F, 0.99F));
               var2.cancel();
            }
         }
      }
   }
}
