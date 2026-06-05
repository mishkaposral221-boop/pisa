package rich.mixin;

import net.minecraft.client.input.KeyInput;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.events.api.EventManager;
import rich.events.impl.KeyEvent;
import rich.modules.impl.misc.PanicMode;
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

         PanicMode panicMode = PanicMode.getInstance();

         // ── Активация Panic Mode (нажатие = action 1) ─────────────────────────
         if (var3 == 1 && panicMode != null && var4.key() == panicMode.getKey()) {
            panicMode.activatePanic();
         }

         // ── Открытие GUI мода (отпускание кнопки = action 0) ──────────────────
         if (var3 == 0 && var4.key() == BindConfig.getInstance().getBindKey() && this.canOpenClickGui()) {
            if (panicMode != null && panicMode.isGuiBlocked()) {
               // Паника активна, перезахода ещё не было — GUI заблокирован
               // ничего не делаем
            } else {
               // Если паника активна, но игрок уже перезашёл —
               // восстанавливаем модули перед открытием GUI
               if (panicMode != null && panicMode.isAwaitingGuiOpen()) {
                  panicMode.onGuiOpenedAfterPanic();
               }
               ClickGui.INSTANCE.openGui();
            }
         }

         EventManager.callEvent(new KeyEvent(this.client.currentScreen, net.minecraft.client.util.InputUtil.Type.KEYSYM, var4.key(), var3));
      }
   }

   private boolean canOpenClickGui() {
      return this.client.world == null || this.client.player == null ? false : this.client.currentScreen == null;
   }
}
