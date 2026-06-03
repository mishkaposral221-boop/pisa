package com.example.client.mixin;

import com.example.client.account.AccountSwitcherScreen;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={TitleScreen.class})
public abstract class TitleScreenMixin
extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method={"init"}, at={@At(value="TAIL")})
    private void onInit(CallbackInfo ci) {
        int x = this.width / 2 + 104;
        int y = this.height / 4 + 48 + 72;
        ButtonWidget accountsBtn = ButtonWidget.builder((Text)Text.literal((String)"Accounts"), btn -> MinecraftClient.getInstance().setScreen((Screen)new AccountSwitcherScreen())).dimensions(x, y, 50, 20).build();
        this.addDrawableChild(accountsBtn);
    }
}

