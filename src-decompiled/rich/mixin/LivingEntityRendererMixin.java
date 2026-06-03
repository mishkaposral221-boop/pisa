package rich.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.class_10042;
import net.minecraft.class_1309;
import net.minecraft.class_1921;
import net.minecraft.class_3532;
import net.minecraft.class_465;
import net.minecraft.class_583;
import net.minecraft.class_922;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.IMinecraft;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.impl.render.HitEffect;

@Mixin(class_922.class)
public abstract class LivingEntityRendererMixin<S extends class_10042, M extends class_583<? super S>> implements IMinecraft {
   @Unique
   private static final Map<class_10042, Integer> RICH$STATE_ENTITY_ID = Collections.synchronizedMap(new WeakHashMap<>());

   @Shadow
   @Nullable
   protected abstract class_1921 method_24302(S var1, boolean var2, boolean var3, boolean var4);

   @ModifyExpressionValue(
      method = "method_62355(Lnet/minecraft/class_1309;Lnet/minecraft/class_10042;F)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_3532;method_17821(FFF)F")
   )
   private float lerpAngleDegreesHook(float var1, @Local(ordinal = 0, argsOnly = true) class_1309 var2, @Local(ordinal = 0, argsOnly = true) float var3) {
      AngleConnection var4 = AngleConnection.INSTANCE;
      if (var2.equals(mc.field_1724) && var4.getCurrentAngle() != null && !(mc.field_1755 instanceof class_465)) {
         float var5 = var4.getPreviousRotation().getYaw();
         float var6 = var4.getRotation().getYaw();
         return class_3532.method_17821(var3, var5, var6);
      } else {
         return var1;
      }
   }

   @ModifyExpressionValue(
      method = "method_62355(Lnet/minecraft/class_1309;Lnet/minecraft/class_10042;F)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1309;method_61414(F)F")
   )
   private float getLerpedPitchHook(float var1, @Local(ordinal = 0, argsOnly = true) class_1309 var2, @Local(ordinal = 0, argsOnly = true) float var3) {
      AngleConnection var4 = AngleConnection.INSTANCE;
      if (var2.equals(mc.field_1724) && var4.getCurrentAngle() != null && !(mc.field_1755 instanceof class_465)) {
         float var5 = var4.getPreviousRotation().getPitch();
         float var6 = var4.getRotation().getPitch();
         return class_3532.method_16439(var3, var5, var6);
      } else {
         return var1;
      }
   }

   @Inject(method = "method_62355(Lnet/minecraft/class_1309;Lnet/minecraft/class_10042;F)V", at = @At("TAIL"))
   private void updateRenderStateHook(class_1309 var1, S var2, float var3, CallbackInfo var4) {
      RICH$STATE_ENTITY_ID.put(var2, var1.method_5628());
   }

   @Redirect(
      method = "method_4054(Lnet/minecraft/class_10042;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_12075;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_922;method_24302(Lnet/minecraft/class_10042;ZZZ)Lnet/minecraft/class_1921;")
   )
   private class_1921 renderLayerHook(class_922<?, ?, ?> var1, class_10042 var2, boolean var3, boolean var4, boolean var5) {
      Integer var6 = RICH$STATE_ENTITY_ID.get(var2);
      HitEffect var7 = HitEffect.getInstance();
      if (!var4 && var6 != null && var7 != null && var7.shouldTintEntity(var6)) {
         var4 = true;
      }

      return this.method_24302((S)var2, var3, var4, var5);
   }

   @ModifyArg(
      method = "method_4054(Lnet/minecraft/class_10042;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_12075;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_11659;method_73490(Lnet/minecraft/class_3879;Ljava/lang/Object;Lnet/minecraft/class_4587;Lnet/minecraft/class_1921;IIILnet/minecraft/class_1058;ILnet/minecraft/class_11683$class_11792;)V"
      ),
      index = 6
   )
   private int modifyColor(int var1, @Local(argsOnly = true) S var2) {
      Integer var3 = RICH$STATE_ENTITY_ID.get(var2);
      HitEffect var4 = HitEffect.getInstance();
      if (var3 != null && var4 != null && var4.shouldTintEntity(var3)) {
         int var5 = var4.getEntityTintColor();
         int var6 = var5 >> 24 & 0xFF;
         int var7 = var5 >> 16 & 0xFF;
         int var8 = var5 >> 8 & 0xFF;
         int var9 = var5 & 0xFF;
         int var10 = var1 >> 24 & 0xFF;
         int var11 = var1 >> 16 & 0xFF;
         int var12 = var1 >> 8 & 0xFF;
         int var13 = var1 & 0xFF;
         float var14 = var6 / 255.0F;
         int var15 = (int)(var11 * var7 / 255.0F);
         int var16 = (int)(var12 * var8 / 255.0F);
         int var17 = (int)(var13 * var9 / 255.0F);
         int var18 = (int)(var11 + (var15 - var11) * var14);
         int var19 = (int)(var12 + (var16 - var12) * var14);
         int var20 = (int)(var13 + (var17 - var13) * var14);
         var18 = Math.min(255, Math.max(0, var18));
         var19 = Math.min(255, Math.max(0, var19));
         var20 = Math.min(255, Math.max(0, var20));
         return var10 << 24 | var18 << 16 | var19 << 8 | var20;
      } else {
         return var1;
      }
   }
}
