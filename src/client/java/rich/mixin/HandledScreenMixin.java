package rich.mixin;

import java.awt.Color;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.modules.impl.misc.ItemCooldowns;
import rich.modules.impl.misc.ItemHelper;
import rich.modules.impl.util.PvpHelper;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
   @Inject(method = "drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;II)V", at = @At("HEAD"))
   private void onDrawSlot(DrawContext var1, Slot var2, int var3, int var4, CallbackInfo var5) {
      ItemHelper var6 = ItemHelper.getInstance();
      PvpHelper var7 = PvpHelper.getInstance();
      if (var6 != null && var6.isState()) {
         ItemStack var11 = var2.getStack();
         if (!var11.isEmpty()) {
            int var12 = 0;
            if (var11.isOf(Items.GOLDEN_APPLE)) {
               var12 = var6.getGoldenApple().getColor();
            } else if (var11.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
               var12 = var6.getEnchantedGoldenApple().getColor();
            } else if (var11.isOf(Items.TOTEM_OF_UNDYING)) {
               var12 = var6.getTotemOfUndying().getColor();
            } else if (var11.isOf(Items.ENDER_PEARL)) {
               var12 = var6.getEnderPearl().getColor();
            } else if (var11.isOf(Items.EXPERIENCE_BOTTLE)) {
               var12 = var6.getExperienceBottle().getColor();
            } else if (var11.isOf(Items.CHORUS_FRUIT)) {
               var12 = var6.getChorusFruit().getColor();
            } else if (var11.isOf(Items.ENDER_EYE)) {
               var12 = var6.getEnderEye().getColor();
            } else if (var11.isOf(Items.SUGAR)) {
               var12 = var6.getSugar().getColor();
            } else if (var11.isOf(Items.FIRE_CHARGE)) {
               var12 = var6.getFireCharge().getColor();
            } else if (var11.isOf(Items.PHANTOM_MEMBRANE)) {
               var12 = var6.getPhantomMembrane().getColor();
            } else if (var11.isOf(Items.NETHERITE_SCRAP)) {
               var12 = var6.getNetheriteScrap().getColor();
            } else if (var11.isOf(Items.DRIED_KELP)) {
               var12 = var6.getDriedKelp().getColor();
            } else if (var11.isOf(Items.SNOWBALL)) {
               var12 = var6.getSnowball().getColor();
            }

            if (var12 != 0) {
               var1.fill(var2.x, var2.y, var2.x + 16, var2.y + 16, var12);
            }

            if (var7 != null && var7.isState() && var7.isInCombat()) {
               int var10 = var7.getPulseAlpha(var2.getIndex());
               if (var10 > 0) {
                  var1.fill(var2.x, var2.y, var2.x + 16, var2.y + 16, new Color(0, 220, 80, var10).getRGB());
               }
            }
         }
      } else {
         if (var7 != null && var7.isState() && var7.isInCombat()) {
            ItemStack var8 = var2.getStack();
            if (!var8.isEmpty()) {
               int var9 = var7.getPulseAlpha(var2.getIndex());
               if (var9 > 0) {
                  var1.fill(var2.x, var2.y, var2.x + 16, var2.y + 16, new Color(0, 220, 80, var9).getRGB());
               }
            }
         }
      }
   }

   @Inject(method = "drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;II)V", at = @At("TAIL"))
   private void onDrawSlotTail(DrawContext var1, Slot var2, int var3, int var4, CallbackInfo var5) {
      ItemCooldowns var6 = ItemCooldowns.getInstance();
      MinecraftClient var7 = MinecraftClient.getInstance();
      if (var6 != null && var6.isState() && var7 != null && var7.player != null) {
         ItemStack var8 = var2.getStack();
         if (!var8.isEmpty()) {
            float var9 = var6.getRemainingSeconds(var8);
            if (!(var9 <= 0.0F)) {
               String var10 = var9 >= 10.0F ? String.format("%.0f", var9) : String.format("%.1f", var9);
               int var11 = new Color(var6.textColor.getColor()).getRGB();
               var1.drawText(var7.textRenderer, var10, var2.x + 1, var2.y + 1, var11, true);
            }
         }
      }
   }
}
