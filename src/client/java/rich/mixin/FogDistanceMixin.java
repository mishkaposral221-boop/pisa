package rich.mixin;

import net.minecraft.client.render.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import rich.modules.impl.render.CustomFog;

@Mixin(FogRenderer.class)
public class FogDistanceMixin {
   @ModifyArg(
      method = "applyFog(Lnet/minecraft/Camera;ILnet/minecraft/RenderTickCounter;FLnet/minecraft/ClientWorld;)Lorg/joml/Vector4f;",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/FogRenderer;applyFog(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"),
      index = 3
   )
   private static float onFogStart(float var0) {
      CustomFog var1 = CustomFog.getInstance();
      return var1 != null && var1.isState() ? 0.0F : var0;
   }

   @ModifyArg(
      method = "applyFog(Lnet/minecraft/Camera;ILnet/minecraft/RenderTickCounter;FLnet/minecraft/ClientWorld;)Lorg/joml/Vector4f;",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/FogRenderer;applyFog(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"),
      index = 4
   )
   private static float onFogEnd(float var0) {
      CustomFog var1 = CustomFog.getInstance();
      return var1 != null && var1.isState() ? var1.distance.getValue() : var0;
   }
}
