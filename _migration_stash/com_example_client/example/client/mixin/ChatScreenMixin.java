package com.example.client.mixin;

import com.example.client.hud.HudManager;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ChatScreen.class})
public class ChatScreenMixin {
    @Inject(method={"mouseClicked"}, at={@At(value="HEAD")}, cancellable=true)
    private void onMouseClicked(Click event, boolean consumed, CallbackInfoReturnable<Boolean> cir) {
        HudManager hud = HudManager.getInstance();
        if (hud != null && event.buttonInfo().button() == 0 && hud.mouseClicked(event.x(), event.y(), 0)) {
            cir.setReturnValue(true);
        }
    }
}

