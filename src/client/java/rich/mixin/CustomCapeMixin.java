package rich.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public class CustomCapeMixin {
   @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
   private void replaceCape(CallbackInfoReturnable<SkinTextures> var1) {
      AbstractClientPlayerEntity var2 = (AbstractClientPlayerEntity)(Object)this;
      MinecraftClient var3 = MinecraftClient.getInstance();
      if (var3.player != null && var2.getUuid().equals(var3.player.getUuid())) {
         SkinTextures var4 = (SkinTextures)var1.getReturnValue();
         var1.setReturnValue(new SkinTextures(var4.body(), null, null, var4.model(), var4.secure()));
      }
   }
}
