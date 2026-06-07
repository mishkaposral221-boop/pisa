package rich.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.awt.Color;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderTickCounter;
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

@Mixin(InGameHud.class)
public abstract class InGameHudMixin implements IMinecraft {
   @Shadow
   @Final
   private MinecraftClient client;
   @Unique
   private int richCurrentHotbarIndex = 0;
   @Unique
   private final UpdateToast richIngameUpdateNotif = new UpdateToast();

   @Inject(method = "<init>", at = @At("TAIL"))
   private void onInit(CallbackInfo var1) {
      UpdateToast.setIngameInstance(this.richIngameUpdateNotif);
   }

   @Inject(method = "renderHotbar", at = @At("HEAD"))
   private void onRenderHotbarStart(DrawContext var1, RenderTickCounter var2, CallbackInfo var3) {
      this.richCurrentHotbarIndex = 0;
      Animations var4 = Animations.getInstance();
      if (var4 != null && var4.isState() && var4.hotbarAnim.isValue() && this.client.player != null) {
         float var5 = HotbarAnimState.smoothSlot;
         if (var5 >= 0.0F) {
            int var6 = this.client.getWindow().getScaledWidth();
            int var7 = this.client.getWindow().getScaledHeight();
            float var8 = var6 / 2.0F - 91.0F;
            float var9 = var7 - 22.0F;
            float var10 = var8 + var5 * 20.0F;
            var1.fill((int)(var10 - 1.0F), (int)(var9 - 1.0F), (int)(var10 + 19.0F), (int)(var9 + 19.0F), new Color(255, 255, 255, 40).getRGB());
         }
      }
   }

   @WrapOperation(
      method = "renderHotbar",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V"
      )
   )
   private void onRenderHotbarItem(
      InGameHud var1, DrawContext var2, int var3, int var4, RenderTickCounter var5, PlayerEntity var6, ItemStack var7, int var8, Operation<Void> var9
   ) {
      int var10 = this.richCurrentHotbarIndex;
      if (this.richCurrentHotbarIndex < 9) {
         this.richCurrentHotbarIndex++;
      }

      ItemHelper var11 = ItemHelper.getInstance();
      PvpHelper var12 = PvpHelper.getInstance();
      if (var11 != null && var11.isState()) {
         int var13 = 0;
         if (var7.isOf(Items.GOLDEN_APPLE)) {
            var13 = var11.getGoldenApple().getColor();
         } else if (var7.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
            var13 = var11.getEnchantedGoldenApple().getColor();
         } else if (var7.isOf(Items.TOTEM_OF_UNDYING)) {
            var13 = var11.getTotemOfUndying().getColor();
         } else if (var7.isOf(Items.ENDER_PEARL)) {
            var13 = var11.getEnderPearl().getColor();
         } else if (var7.isOf(Items.EXPERIENCE_BOTTLE)) {
            var13 = var11.getExperienceBottle().getColor();
         } else if (var7.isOf(Items.CHORUS_FRUIT)) {
            var13 = var11.getChorusFruit().getColor();
         } else if (var7.isOf(Items.ENDER_EYE)) {
            var13 = var11.getEnderEye().getColor();
         } else if (var7.isOf(Items.SUGAR)) {
            var13 = var11.getSugar().getColor();
         } else if (var7.isOf(Items.FIRE_CHARGE)) {
            var13 = var11.getFireCharge().getColor();
         } else if (var7.isOf(Items.PHANTOM_MEMBRANE)) {
            var13 = var11.getPhantomMembrane().getColor();
         } else if (var7.isOf(Items.NETHERITE_SCRAP)) {
            var13 = var11.getNetheriteScrap().getColor();
         } else if (var7.isOf(Items.DRIED_KELP)) {
            var13 = var11.getDriedKelp().getColor();
         } else if (var7.isOf(Items.SNOWBALL)) {
            var13 = var11.getSnowball().getColor();
         }

         if (var13 != 0) {
            var2.fill(var3 - 1, var4 - 1, var3 + 17, var4 + 17, var13);
         }
      }

      if (var12 != null && var12.isState() && var12.isInCombat()) {
         int var18 = var12.getPulseAlpha(var10);
         if (var18 > 0) {
            var2.fill(var3 - 1, var4 - 1, var3 + 17, var4 + 17, new Color(0, 220, 80, var18).getRGB());
         }
      }

      HotbarItemRenderEvent var19 = new HotbarItemRenderEvent(var7, var10);
      EventManager.callEvent(var19);
      var9.call(new Object[]{var1, var2, var3, var4, var5, var6, var19.getStack(), var8});
      ItemCooldowns var14 = ItemCooldowns.getInstance();
      if (var14 != null && var14.isState() && this.client.player != null && !var7.isEmpty()) {
         float var15 = var14.getRemainingSeconds(var7);
         if (var15 > 0.0F) {
            String var16 = var15 >= 10.0F ? String.format("%.0f", var15) : String.format("%.1f", var15);
            int var17 = new Color(var14.textColor.getColor()).getRGB();
            var2.drawText(this.client.textRenderer, var16, var3 + 1, var4 + 1, var17, true);
         }
      }
   }

   @WrapOperation(method = "renderHeldItemTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getName()Lnet/minecraft/text/Text;"))
   private Text onGetHeldItemName(ItemStack var1, Operation<Text> var2) {
      ItemHelper var3 = ItemHelper.getInstance();
      if (var3 != null && var3.isState()) {
         if (var1.isOf(Items.ENDER_EYE)) {
            return Text.literal("Дезка");
         }

         if (var1.isOf(Items.SUGAR)) {
            return Text.literal("Явка");
         }

         if (var1.isOf(Items.FIRE_CHARGE)) {
            return Text.literal("Огненный заряд");
         }

         if (var1.isOf(Items.PHANTOM_MEMBRANE)) {
            return Text.literal("Божья аура");
         }

         if (var1.isOf(Items.NETHERITE_SCRAP)) {
            return Text.literal("Трапка");
         }

         if (var1.isOf(Items.DRIED_KELP)) {
            return Text.literal("Пласт");
         }

         if (var1.isOf(Items.SNOWBALL)) {
            return Text.literal("Снежок");
         }
      }

      return (Text)var2.call(new Object[]{var1});
   }

   @Inject(method = "renderNauseaOverlay", at = @At("HEAD"), cancellable = true)
   private void onRenderNauseaOverlay(DrawContext var1, float var2, CallbackInfo var3) {
      NoRender var4 = NoRender.getInstance();
      if (var4 != null && var4.isState() && var4.modeSetting.isSelected("Nausea")) {
         var3.cancel();
      }
   }

   @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", at = @At("HEAD"), cancellable = true)
   private void onRenderScoreboard(DrawContext var1, RenderTickCounter var2, CallbackInfo var3) {
      NoRender var4 = NoRender.getInstance();
      if (var4 != null && var4.isState() && var4.modeSetting.isSelected("Scoreboard")) {
         var3.cancel();
      }
   }

   @Inject(method = "renderBossBarHud", at = @At("HEAD"), cancellable = true)
   private void onRenderBossBar(DrawContext var1, RenderTickCounter var2, CallbackInfo var3) {
      NoRender var4 = NoRender.getInstance();
      if (var4 != null && var4.isState() && var4.modeSetting.isSelected("BossBar")) {
         var3.cancel();
      }
   }

   @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
   private void onRenderCrosshair(DrawContext var1, RenderTickCounter var2, CallbackInfo var3) {
      CustomCrosshair var4 = CustomCrosshair.getInstance();
      if (var4 != null && var4.shouldReplaceVanillaCrosshair()) {
         var3.cancel();
      }
   }

   @Inject(method = "render", at = @At("TAIL"))
   public void onRenderCustomHud(DrawContext var1, RenderTickCounter var2, CallbackInfo var3) {
      if (!this.client.options.hudHidden) {
         if (this.client.world != null && this.client.player != null) {
            if (this.client.getOverlay() == null) {
               Screen var4 = this.client.currentScreen;
               if (!this.isLoadingScreen(var4) && this.shouldRenderHud(var4)) {
                  var1.createNewRootLayer();
                  Render2D.beginOverlay();

                  try {
                     var1.getMatrices().pushMatrix();

                     try {
                        DrawEvent var5 = new DrawEvent(var1, drawEngine, var2.getTickProgress(false));
                        EventManager.callEvent(var5);
                     } finally {
                        var1.getMatrices().popMatrix();
                     }

                     int var6 = (int)this.client.mouse.getScaledX(this.client.getWindow());
                     int var7 = (int)this.client.mouse.getScaledY(this.client.getWindow());
                     float var8 = var2.getTickProgress(false);
                     Hud var9 = Hud.getInstance();
                     if (var9 != null
                        && var9.isState()
                        && Initialization.getInstance() != null
                        && Initialization.getInstance().getManager() != null
                        && Initialization.getInstance().getManager().getHudManager() != null) {
                        Initialization.getInstance().getManager().getHudManager().render(var1, var8, var6, var7);
                     }

                     UpdateChecker var12 = UpdateChecker.getInstance();
                     UpdateChecker.UpdateInfo var13 = var12.getPendingUpdate();
                     if (var13 != null && !var12.isNotified() && !this.richIngameUpdateNotif.isVisible()) {
                        this.richIngameUpdateNotif.show();
                        var12.markNotified();
                     }

                     if (this.richIngameUpdateNotif.isVisible() && var13 != null) {
                        int var14 = this.client.getWindow().getScaledWidth();
                        int var15 = this.client.getWindow().getScaledHeight();
                        float var10 = (float)this.client.mouse.getScaledX(this.client.getWindow());
                        float var11 = (float)this.client.mouse.getScaledY(this.client.getWindow());
                        this.richIngameUpdateNotif.render(var14, var15, var10, var11, var13);
                     }
                  } finally {
                     Render2D.endOverlay();
                  }
               }
            }
         }
      }
   }

   @Unique
   private boolean shouldRenderHud(Screen var1) {
      if (var1 == null) {
         return true;
      } else if (var1 instanceof ClickGui) {
         return false;
      } else if (var1 instanceof ChatScreen) {
         return false;
      } else {
         return false;
      }
   }

   @Unique
   private boolean isLoadingScreen(Screen var1) {
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
