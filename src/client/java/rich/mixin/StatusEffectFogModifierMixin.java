package rich.mixin;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.client.render.fog.StatusEffectFogModifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.modules.impl.render.NoRender;

@Mixin(StatusEffectFogModifier.class)
public abstract class StatusEffectFogModifierMixin {
   @Shadow
   public abstract RegistryEntry<StatusEffect> getStatusEffect();

   @Inject(method = "shouldApply", at = @At("HEAD"), cancellable = true)
   private void onShouldApply(@Nullable CameraSubmersionType var1, Entity var2, CallbackInfoReturnable<Boolean> var3) {
      NoRender var4 = NoRender.getInstance();
      if (var4.isState()) {
         RegistryEntry var5 = this.getStatusEffect();
         if (var4.modeSetting.isSelected("Bad Effects") && var5 == StatusEffects.BLINDNESS) {
            var3.setReturnValue(false);
         }

         if (var4.modeSetting.isSelected("Darkness") && var5 == StatusEffects.DARKNESS) {
            var3.setReturnValue(false);
         }
      }
   }
}
