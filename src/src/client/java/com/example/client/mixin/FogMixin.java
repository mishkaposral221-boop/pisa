package com.example.client.mixin;

import com.example.client.module.NoRender;
import net.minecraft.entity.Entity;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.fog.StatusEffectFogModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={StatusEffectFogModifier.class})
public class FogMixin {
    @Inject(method={"shouldApply"}, at={@At(value="HEAD")}, cancellable=true)
    private void onIsApplicable(CameraSubmersionType fogType, Entity entity, CallbackInfoReturnable<Boolean> cir) {
        NoRender nr = NoRender.getInstance();
        if (nr != null && nr.noDarkness()) {
            cir.setReturnValue(false);
        }
    }
}

