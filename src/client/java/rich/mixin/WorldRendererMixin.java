package rich.mixin;

import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.gl.DynamicUniforms.ChunkSectionsValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.IMinecraft;
import rich.modules.impl.render.ChunkAnimator;
import rich.modules.impl.render.NoRender;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin implements IMinecraft {
   @ModifyArg(method = "renderBlockLayers", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0), index = 0)
   private Object modifyChunkSectionsValue(Object var1) {
      if (var1 instanceof net.minecraft.client.gl.DynamicUniforms.ChunkSectionsValue var2) {
         ChunkAnimator var3 = ChunkAnimator.getInstance();
         if (var3 != null && var3.isState()) {
            float var4 = var2.visibility();
            float var5 = (1.0F - var4) * 100.0F;
            int var6 = var2.y() - (int)var5;
            return new net.minecraft.client.gl.DynamicUniforms.ChunkSectionsValue(var2.modelView(), var2.x(), var6, var2.z(), var2.visibility(), var2.textureAtlasWidth(), var2.textureAtlasHeight());
         }
      }

      return var1;
   }

   @Inject(method = "hasBlindnessOrDarkness", at = @At("HEAD"), cancellable = true)
   private void onHasBlindnessOrDarkness(Camera var1, CallbackInfoReturnable<Boolean> var2) {
      NoRender var3 = NoRender.getInstance();
      if (var3 != null && var3.isState()) {
         if (var1.getFocusedEntity() instanceof LivingEntity var5) {
            boolean var6 = var5.hasStatusEffect(StatusEffects.BLINDNESS);
            boolean var7 = var5.hasStatusEffect(StatusEffects.DARKNESS);
            if (var3.modeSetting.isSelected("Bad Effects") && var6 && !var7) {
               var2.setReturnValue(false);
            }

            if (var3.modeSetting.isSelected("Darkness") && var7 && !var6) {
               var2.setReturnValue(false);
            }

            if (var3.modeSetting.isSelected("Bad Effects") && var3.modeSetting.isSelected("Darkness")) {
               var2.setReturnValue(false);
            }
         }
      }
   }
}
