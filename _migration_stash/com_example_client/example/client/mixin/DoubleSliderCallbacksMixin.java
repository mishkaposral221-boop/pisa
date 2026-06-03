package com.example.client.mixin;

import java.util.Optional;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={SimpleOption.DoubleSliderCallbacks.class})
public class DoubleSliderCallbacksMixin {
    @Inject(method={"validate"}, at={@At(value="HEAD")}, cancellable=true)
    private void unlimitValidateValues(Double value, CallbackInfoReturnable<Optional<Double>> cir) {
        cir.setReturnValue(Optional.of(value));
    }
}

