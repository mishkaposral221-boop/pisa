package rich.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.IMinecraft;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.impl.render.HitEffect;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<S extends LivingEntityRenderState, M extends EntityModel<? super S>> implements IMinecraft {
   @Unique
   private static final Map<LivingEntityRenderState, Integer> RICH$STATE_ENTITY_ID = Collections.synchronizedMap(new WeakHashMap<>());

   @Shadow
   @Nullable
   protected abstract RenderLayer getRenderLayer(S var1, boolean var2, boolean var3, boolean var4);

   @ModifyExpressionValue(
      method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerpAngleDegrees(FFF)F")
   )
   private float lerpAngleDegreesHook(float var1, @Local(ordinal = 0, argsOnly = true) LivingEntity var2, @Local(ordinal = 0, argsOnly = true) float var3) {
      AngleConnection var4 = AngleConnection.INSTANCE;
      if (var2.equals(mc.player) && var4.getCurrentAngle() != null && !(mc.currentScreen instanceof HandledScreen)) {
         float var5 = var4.getPreviousRotation().getYaw();
         float var6 = var4.getRotation().getYaw();
         return MathHelper.lerpAngleDegrees(var3, var5, var6);
      } else {
         return var1;
      }
   }

   @ModifyExpressionValue(
      method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getLerpedPitch(F)F")
   )
   private float getLerpedPitchHook(float var1, @Local(ordinal = 0, argsOnly = true) LivingEntity var2, @Local(ordinal = 0, argsOnly = true) float var3) {
      AngleConnection var4 = AngleConnection.INSTANCE;
      if (var2.equals(mc.player) && var4.getCurrentAngle() != null && !(mc.currentScreen instanceof HandledScreen)) {
         float var5 = var4.getPreviousRotation().getPitch();
         float var6 = var4.getRotation().getPitch();
         return MathHelper.lerp(var3, var5, var6);
      } else {
         return var1;
      }
   }

   @Inject(
      method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
      at = @At("TAIL")
   )
   private void updateRenderStateHook(LivingEntity var1, S var2, float var3, CallbackInfo var4) {
      RICH$STATE_ENTITY_ID.put(var2, var1.getId());
   }

   @Redirect(
      method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getRenderLayer(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/render/RenderLayer;"
      )
   )
   private RenderLayer renderLayerHook(LivingEntityRenderer<?, ?, ?> var1, LivingEntityRenderState var2, boolean var3, boolean var4, boolean var5) {
      Integer var6 = RICH$STATE_ENTITY_ID.get(var2);
      HitEffect var7 = HitEffect.getInstance();
      if (!var4 && var6 != null && var7 != null && var7.shouldTintEntity(var6)) {
         var4 = true;
      }

      return this.getRenderLayer((S)var2, var3, var4, var5);
   }

   @ModifyReturnValue(
      method = "getMixColor(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;)I",
      at = @At("RETURN")
   )
   private int modifyMixColor(int var1, @Local(argsOnly = true) S var2) {
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
