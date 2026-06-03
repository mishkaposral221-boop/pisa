package rich.mixin;

import net.minecraft.client.input.KeyInput;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil.Type;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.events.api.EventManager;
import rich.events.impl.KeyEvent;
import rich.screens.clickgui.ClickGui;
import rich.util.config.impl.bind.BindConfig;

@Mixin(Keyboard.class)
public class KeyboardMixin {
   @Final
   @Shadow
   private MinecraftClient client;

   @Inject(method = "onKey", at = @At("HEAD"))
   private void onKey(long var1, int var3, KeyInput var4, CallbackInfo var5) {
      if (var4.key() != -1 && var1 == this.client.getWindow().getHandle()) {
         if (var3 == 0 && var4.key() == BindConfig.getInstance().getBindKey() && this.canOpenClickGui()) {
            ClickGui.INSTANCE.openGui();
         }

         EventManager.callEvent(new KeyEvent(this.client.currentScreen, net.minecraft.client.util.InputUtil.Type.KEYSYM, var4.key(), var3));
      }
   }

   private boolean canOpenClickGui() {
      return this.client.world == null || this.client.player == null ? false : this.client.currentScreen == null;
   }
}
