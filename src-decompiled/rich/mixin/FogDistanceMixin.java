package rich.mixin;

import net.minecraft.class_758;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import rich.modules.impl.render.CustomFog;

@Mixin(class_758.class)
public class FogDistanceMixin {
   @ModifyArg(
      method = "method_3211(Lnet/minecraft/class_4184;ILnet/minecraft/class_9779;FLnet/minecraft/class_638;)Lorg/joml/Vector4f;",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_758;method_71110(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"),
      index = 3
   )
   private static float onFogStart(float var0) {
      CustomFog var1 = CustomFog.getInstance();
      return var1 != null && var1.isState() ? 0.0F : var0;
   }

   @ModifyArg(
      method = "method_3211(Lnet/minecraft/class_4184;ILnet/minecraft/class_9779;FLnet/minecraft/class_638;)Lorg/joml/Vector4f;",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_758;method_71110(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"),
      index = 4
   )
   private static float onFogEnd(float var0) {
      CustomFog var1 = CustomFog.getInstance();
      return var1 != null && var1.isState() ? var1.distance.getValue() : var0;
   }
}
