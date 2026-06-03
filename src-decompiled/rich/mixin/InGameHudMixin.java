package rich.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.awt.Color;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_408;
import net.minecraft.class_437;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.IMinecraft;
import rich.Initialization;
import rich.events.api.EventManager;
import rich.events.impl.DrawEvent;
import rich.events.impl.HotbarItemRenderEvent;
import rich.modules.impl.misc.ItemCooldowns;
import rich.modules.impl.misc.ItemHelper;
import rich.modules.impl.render.Animations;
import rich.modules.impl.render.CustomCrosshair;
import rich.modules.impl.render.Hud;
import rich.modules.impl.render.NoRender;
import rich.modules.impl.util.PvpHelper;
import rich.screens.clickgui.ClickGui;
import rich.update.UpdateChecker;
import rich.update.UpdateToast;
import rich.util.render.Render2D;
import rich.util.render.hud.HotbarAnimState;

@Mixin(class_329.class)
public abstract class InGameHudMixin implements IMinecraft {
   @Shadow
   @Final
   private class_310 field_2035;
   @Unique
   private int richCurrentHotbarIndex = 0;
   @Unique
   private final UpdateToast richIngameUpdateNotif = new UpdateToast();

   @Inject(method = "<init>", at = @At("TAIL"))
   private void onInit(CallbackInfo var1) {
      UpdateToast.setIngameInstance(this.richIngameUpdateNotif);
   }

   @Inject(method = "method_1759", at = @At("HEAD"))
   private void onRenderHotbarStart(class_332 var1, class_9779 var2, CallbackInfo var3) {
      this.richCurrentHotbarIndex = 0;
      Animations var4 = Animations.getInstance();
      if (var4 != null && var4.isState() && var4.hotbarAnim.isValue() && this.field_2035.field_1724 != null) {
         float var5 = HotbarAnimState.smoothSlot;
         if (var5 >= 0.0F) {
            int var6 = this.field_2035.method_22683().method_4486();
            int var7 = this.field_2035.method_22683().method_4502();
            float var8 = var6 / 2.0F - 91.0F;
            float var9 = var7 - 22.0F;
            float var10 = var8 + var5 * 20.0F;
            var1.method_25294((int)(var10 - 1.0F), (int)(var9 - 1.0F), (int)(var10 + 19.0F), (int)(var9 + 19.0F), new Color(255, 255, 255, 40).getRGB());
         }
      }
   }

   @WrapOperation(
      method = "method_1759",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_329;method_1762(Lnet/minecraft/class_332;IILnet/minecraft/class_9779;Lnet/minecraft/class_1657;Lnet/minecraft/class_1799;I)V"
      )
   )
   private void onRenderHotbarItem(
      class_329 var1, class_332 var2, int var3, int var4, class_9779 var5, class_1657 var6, class_1799 var7, int var8, Operation<Void> var9
   ) {
      int var10 = this.richCurrentHotbarIndex;
      if (this.richCurrentHotbarIndex < 9) {
         this.richCurrentHotbarIndex++;
      }

      ItemHelper var11 = ItemHelper.getInstance();
      PvpHelper var12 = PvpHelper.getInstance();
      if (var11 != null && var11.isState()) {
         int var13 = 0;
         if (var7.method_31574(class_1802.field_8463)) {
            var13 = var11.getGoldenApple().getColor();
         } else if (var7.method_31574(class_1802.field_8367)) {
            var13 = var11.getEnchantedGoldenApple().getColor();
         } else if (var7.method_31574(class_1802.field_8288)) {
            var13 = var11.getTotemOfUndying().getColor();
         } else if (var7.method_31574(class_1802.field_8634)) {
            var13 = var11.getEnderPearl().getColor();
         } else if (var7.method_31574(class_1802.field_8287)) {
            var13 = var11.getExperienceBottle().getColor();
         } else if (var7.method_31574(class_1802.field_8233)) {
            var13 = var11.getChorusFruit().getColor();
         } else if (var7.method_31574(class_1802.field_8449)) {
            var13 = var11.getEnderEye().getColor();
         } else if (var7.method_31574(class_1802.field_8479)) {
            var13 = var11.getSugar().getColor();
         } else if (var7.method_31574(class_1802.field_8814)) {
            var13 = var11.getFireCharge().getColor();
         } else if (var7.method_31574(class_1802.field_8614)) {
            var13 = var11.getPhantomMembrane().getColor();
         } else if (var7.method_31574(class_1802.field_22021)) {
            var13 = var11.getNetheriteScrap().getColor();
         } else if (var7.method_31574(class_1802.field_8551)) {
            var13 = var11.getDriedKelp().getColor();
         } else if (var7.method_31574(class_1802.field_8543)) {
            var13 = var11.getSnowball().getColor();
         }

         if (var13 != 0) {
            var2.method_25294(var3 - 1, var4 - 1, var3 + 17, var4 + 17, var13);
         }
      }

      if (var12 != null && var12.isState() && var12.isInCombat()) {
         int var18 = var12.getPulseAlpha(var10);
         if (var18 > 0) {
            var2.method_25294(var3 - 1, var4 - 1, var3 + 17, var4 + 17, new Color(0, 220, 80, var18).getRGB());
         }
      }

      HotbarItemRenderEvent var19 = new HotbarItemRenderEvent(var7, var10);
      EventManager.callEvent(var19);
      var9.call(new Object[]{var1, var2, var3, var4, var5, var6, var19.getStack(), var8});
      ItemCooldowns var14 = ItemCooldowns.getInstance();
      if (var14 != null && var14.isState() && this.field_2035.field_1724 != null && !var7.method_7960()) {
         float var15 = var14.getRemainingSeconds(var7);
         if (var15 > 0.0F) {
            String var16 = var15 >= 10.0F ? String.format("%.0f", var15) : String.format("%.1f", var15);
            int var17 = new Color(var14.textColor.getColor()).getRGB();
            var2.method_51433(this.field_2035.field_1772, var16, var3 + 1, var4 + 1, var17, true);
         }
      }
   }

   @WrapOperation(method = "method_1749", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1799;method_7964()Lnet/minecraft/class_2561;"))
   private class_2561 onGetHeldItemName(class_1799 var1, Operation<class_2561> var2) {
      ItemHelper var3 = ItemHelper.getInstance();
      if (var3 != null && var3.isState()) {
         if (var1.method_31574(class_1802.field_8449)) {
            return class_2561.method_43470("Дезка");
         }

         if (var1.method_31574(class_1802.field_8479)) {
            return class_2561.method_43470("Явка");
         }

         if (var1.method_31574(class_1802.field_8814)) {
            return class_2561.method_43470("Огненный заряд");
         }

         if (var1.method_31574(class_1802.field_8614)) {
            return class_2561.method_43470("Божья аура");
         }

         if (var1.method_31574(class_1802.field_22021)) {
            return class_2561.method_43470("Трапка");
         }

         if (var1.method_31574(class_1802.field_8551)) {
            return class_2561.method_43470("Пласт");
         }

         if (var1.method_31574(class_1802.field_8543)) {
            return class_2561.method_43470("Снежок");
         }
      }

      return (class_2561)var2.call(new Object[]{var1});
   }

   @Inject(method = "method_61980", at = @At("HEAD"), cancellable = true)
   private void onRenderNauseaOverlay(class_332 var1, float var2, CallbackInfo var3) {
      NoRender var4 = NoRender.getInstance();
      if (var4 != null && var4.isState() && var4.modeSetting.isSelected("Nausea")) {
         var3.cancel();
      }
   }

   @Inject(method = "method_55803(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V", at = @At("HEAD"), cancellable = true)
   private void onRenderScoreboard(class_332 var1, class_9779 var2, CallbackInfo var3) {
      NoRender var4 = NoRender.getInstance();
      if (var4 != null && var4.isState() && var4.modeSetting.isSelected("Scoreboard")) {
         var3.cancel();
      }
   }

   @Inject(method = "method_70837", at = @At("HEAD"), cancellable = true)
   private void onRenderBossBar(class_332 var1, class_9779 var2, CallbackInfo var3) {
      NoRender var4 = NoRender.getInstance();
      if (var4 != null && var4.isState() && var4.modeSetting.isSelected("BossBar")) {
         var3.cancel();
      }
   }

   @Inject(method = "method_1736", at = @At("HEAD"), cancellable = true)
   private void onRenderCrosshair(class_332 var1, class_9779 var2, CallbackInfo var3) {
      CustomCrosshair var4 = CustomCrosshair.getInstance();
      if (var4 != null && var4.shouldReplaceVanillaCrosshair()) {
         var3.cancel();
      }
   }

   @Inject(method = "method_1753", at = @At("TAIL"))
   public void onRenderCustomHud(class_332 var1, class_9779 var2, CallbackInfo var3) {
      if (!this.field_2035.field_1690.field_1842) {
         if (this.field_2035.field_1687 != null && this.field_2035.field_1724 != null) {
            if (this.field_2035.method_18506() == null) {
               class_437 var4 = this.field_2035.field_1755;
               if (!this.isLoadingScreen(var4)) {
                  var1.method_71048();
                  Render2D.beginOverlay();
                  var1.method_51448().pushMatrix();
                  DrawEvent var5 = new DrawEvent(var1, drawEngine, var2.method_60637(false));
                  EventManager.callEvent(var5);
                  var1.method_51448().popMatrix();
                  if (this.shouldRenderHud(var4)) {
                     int var6 = (int)this.field_2035.field_1729.method_68879(this.field_2035.method_22683());
                     int var7 = (int)this.field_2035.field_1729.method_68883(this.field_2035.method_22683());
                     float var8 = var2.method_60637(false);
                     if (!this.field_2035.field_1690.field_1842) {
                        Hud var9 = Hud.getInstance();
                        if (var9 != null
                           && var9.isState()
                           && Initialization.getInstance() != null
                           && Initialization.getInstance().getManager() != null
                           && Initialization.getInstance().getManager().getHudManager() != null) {
                           Initialization.getInstance().getManager().getHudManager().render(var1, var8, var6, var7);
                        }
                     }
                  }

                  UpdateChecker var12 = UpdateChecker.getInstance();
                  UpdateChecker.UpdateInfo var13 = var12.getPendingUpdate();
                  if (var13 != null && !var12.isNotified() && !this.richIngameUpdateNotif.isVisible()) {
                     this.richIngameUpdateNotif.show();
                     var12.markNotified();
                  }

                  if (this.richIngameUpdateNotif.isVisible() && var13 != null) {
                     int var14 = this.field_2035.method_22683().method_4486();
                     int var15 = this.field_2035.method_22683().method_4502();
                     float var10 = (float)this.field_2035.field_1729.method_68879(this.field_2035.method_22683());
                     float var11 = (float)this.field_2035.field_1729.method_68883(this.field_2035.method_22683());
                     this.richIngameUpdateNotif.render(var14, var15, var10, var11, var13);
                  }

                  Render2D.endOverlay();
               }
            }
         }
      }
   }

   @Unique
   private boolean shouldRenderHud(class_437 var1) {
      if (var1 == null) {
         return true;
      } else if (var1 instanceof ClickGui) {
         return false;
      } else {
         return var1 instanceof class_408 ? false : !this.isLoadingScreen(var1);
      }
   }

   @Unique
   private boolean isLoadingScreen(class_437 var1) {
      if (var1 == null) {
         return false;
      } else {
         String var2 = var1.getClass().getSimpleName().toLowerCase();
         String var3 = var1.getClass().getName().toLowerCase();
         if (var2.contains("loading")) {
            return true;
         } else if (var2.contains("progress")) {
            return true;
         } else if (var2.contains("connecting")) {
            return true;
         } else if (var2.contains("downloading")) {
            return true;
         } else if (var2.contains("terrain")) {
            return true;
         } else if (var2.contains("generating")) {
            return true;
         } else if (var2.contains("saving")) {
            return true;
         } else if (var2.contains("reload")) {
            return true;
         } else if (var2.contains("resource")) {
            return true;
         } else {
            return var2.contains("pack") ? true : var3.contains("mojang");
         }
      }
   }
}
