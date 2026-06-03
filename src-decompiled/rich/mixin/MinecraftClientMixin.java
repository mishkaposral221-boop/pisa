package rich.mixin;

import net.minecraft.class_310;
import net.minecraft.class_320;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.minecraft.class_636;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_757;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.IMinecraft;
import rich.Initialization;
import rich.events.api.EventManager;
import rich.events.impl.GameLeftEvent;
import rich.events.impl.HotBarUpdateEvent;
import rich.events.impl.SetScreenEvent;
import rich.modules.impl.render.Hud;
import rich.online.OnlineTracker;
import rich.screens.clickgui.ClickGui;
import rich.screens.menu.MainMenuScreen;
import rich.update.UpdateChecker;
import rich.util.config.ConfigSystem;
import rich.util.config.impl.account.AccountConfig;
import rich.util.render.font.FontRenderer;
import rich.util.session.SessionChanger;
import rich.util.window.WindowStyle;

@Mixin(class_310.class)
public abstract class MinecraftClientMixin {
   private static boolean initialized = false;
   @Shadow
   @Nullable
   public class_746 field_1724;
   @Shadow
   @Nullable
   public class_636 field_1761;
   @Shadow
   @Final
   public class_757 field_1773;
   @Shadow
   public class_638 field_1687;
   private static boolean fontsInitialized = false;
   @Shadow
   @Mutable
   private class_320 field_1726;

   @Inject(method = "method_1514", at = @At("HEAD"))
   private void onRun(CallbackInfo var1) {
      if (!initialized) {
         Initialization.getInstance().init();
         initialized = true;
      }
   }

   private void setSession(class_320 var1) {
      this.field_1726 = var1;
   }

   @Inject(method = "<init>", at = @At("TAIL"))
   private void onInit(CallbackInfo var1) {
      SessionChanger.setSessionSetter(this::setSession);
   }

   @Inject(method = "method_1490", at = @At("HEAD"))
   private void onStop(CallbackInfo var1) {
      OnlineTracker.getInstance().stop();
      UpdateChecker.getInstance().stop();
      ConfigSystem var2 = ConfigSystem.getInstance();
      if (var2 != null) {
         var2.shutdown();
      }

      (new Thread(() -> {
         try {
            Thread.sleep(2000L);
         } catch (InterruptedException var1x) {
         }

         Runtime.getRuntime().halt(0);
      }, "force-exit") {
         {
            this.setDaemon(true);
         }
      }).start();
   }

   @Inject(method = "method_1507", at = @At("HEAD"))
   private void onSetScreen(class_437 var1, CallbackInfo var2) {
      if (!fontsInitialized && var1 != null) {
         try {
            FontRenderer var3 = Initialization.getInstance().getManager().getRenderCore().getFontRenderer();
            if (var3 != null && !var3.isInitialized()) {
               var3.initialize();
               fontsInitialized = true;
            }
         } catch (Exception var4) {
         }
      }
   }

   @Inject(method = "method_1507", at = @At("HEAD"), cancellable = true)
   private void redirectTitleScreen(class_437 var1, CallbackInfo var2) {
      if (var1 instanceof class_442 && !(var1 instanceof MainMenuScreen)) {
         var2.cancel();
         ((class_310)this).method_1507(new MainMenuScreen());
      }
   }

   @Inject(method = "method_76795(Lnet/minecraft/class_437;Z)V", at = @At("HEAD"))
   private void onDisconnect(class_437 var1, boolean var2, CallbackInfo var3) {
      if (this.field_1687 != null) {
         EventManager.callEvent(GameLeftEvent.get());
      }
   }

   @Inject(method = "method_1574", at = @At("HEAD"))
   private void onTick(CallbackInfo var1) {
      if (initialized) {
         class_310 var2 = class_310.method_1551();
         if (var2.field_1724 != null && var2.field_1687 != null) {
            OnlineTracker.getInstance().setUsername(var2.method_1548().method_1676());
            Hud var3 = Hud.getInstance();
            if (var3 != null
               && var3.isState()
               && Initialization.getInstance() != null
               && Initialization.getInstance().getManager() != null
               && Initialization.getInstance().getManager().getHudManager() != null) {
               Initialization.getInstance().getManager().getHudManager().tick();
            }
         }
      }
   }

   @Inject(method = "method_1507", at = @At("HEAD"), cancellable = true)
   public void setScreenHook(class_437 var1, CallbackInfo var2) {
      class_310 var3 = (class_310)this;
      if (var3.field_1755 instanceof ClickGui var4 && var4.isClosing() && var1 == null) {
         var2.cancel();
      } else {
         SetScreenEvent var7 = new SetScreenEvent(var1);
         EventManager.callEvent(var7);
         Initialization var8 = Initialization.getInstance();
         class_437 var6 = var7.getScreen();
         if (var1 != var6) {
            IMinecraft.mc.method_1507(var6);
            var2.cancel();
         }
      }
   }

   @Inject(method = "method_24287", at = @At("RETURN"), cancellable = true)
   private void getWindowTitle(CallbackInfoReturnable<String> var1) {
      String var2 = AccountConfig.getInstance().getActiveAccountName();
      String var3 = var2 != null && !var2.isEmpty() ? var2 : "Unknown";
      var1.setReturnValue(String.format("RunTime Visuals (%s)", var3));
   }

   @Inject(
      method = "method_1508",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_746;method_31548()Lnet/minecraft/class_1661;"),
      cancellable = true
   )
   public void handleInputEventsHook(CallbackInfo var1) {
      HotBarUpdateEvent var2 = new HotBarUpdateEvent();
      EventManager.callEvent(var2);
      if (var2.isCancelled()) {
         var1.cancel();
      }
   }

   @Inject(method = "method_1583", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1268;values()[Lnet/minecraft/class_1268;"), cancellable = true)
   public void doItemUseHook(CallbackInfo var1) {
   }

   @Inject(method = "method_15993", at = @At("TAIL"))
   private void applyDarkMode(CallbackInfo var1) {
      class_310 var2 = class_310.method_1551();
      WindowStyle.setDarkMode(var2.method_22683().method_4490());
   }
}
