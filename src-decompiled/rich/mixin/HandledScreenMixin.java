package rich.mixin;

import java.awt.Color;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_465;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.modules.impl.misc.ItemCooldowns;
import rich.modules.impl.misc.ItemHelper;
import rich.modules.impl.util.PvpHelper;

@Mixin(class_465.class)
public abstract class HandledScreenMixin {
   @Inject(method = "method_2385(Lnet/minecraft/class_332;Lnet/minecraft/class_1735;II)V", at = @At("HEAD"))
   private void onDrawSlot(class_332 var1, class_1735 var2, int var3, int var4, CallbackInfo var5) {
      ItemHelper var6 = ItemHelper.getInstance();
      PvpHelper var7 = PvpHelper.getInstance();
      if (var6 != null && var6.isState()) {
         class_1799 var11 = var2.method_7677();
         if (!var11.method_7960()) {
            int var12 = 0;
            if (var11.method_31574(class_1802.field_8463)) {
               var12 = var6.getGoldenApple().getColor();
            } else if (var11.method_31574(class_1802.field_8367)) {
               var12 = var6.getEnchantedGoldenApple().getColor();
            } else if (var11.method_31574(class_1802.field_8288)) {
               var12 = var6.getTotemOfUndying().getColor();
            } else if (var11.method_31574(class_1802.field_8634)) {
               var12 = var6.getEnderPearl().getColor();
            } else if (var11.method_31574(class_1802.field_8287)) {
               var12 = var6.getExperienceBottle().getColor();
            } else if (var11.method_31574(class_1802.field_8233)) {
               var12 = var6.getChorusFruit().getColor();
            } else if (var11.method_31574(class_1802.field_8449)) {
               var12 = var6.getEnderEye().getColor();
            } else if (var11.method_31574(class_1802.field_8479)) {
               var12 = var6.getSugar().getColor();
            } else if (var11.method_31574(class_1802.field_8814)) {
               var12 = var6.getFireCharge().getColor();
            } else if (var11.method_31574(class_1802.field_8614)) {
               var12 = var6.getPhantomMembrane().getColor();
            } else if (var11.method_31574(class_1802.field_22021)) {
               var12 = var6.getNetheriteScrap().getColor();
            } else if (var11.method_31574(class_1802.field_8551)) {
               var12 = var6.getDriedKelp().getColor();
            } else if (var11.method_31574(class_1802.field_8543)) {
               var12 = var6.getSnowball().getColor();
            }

            if (var12 != 0) {
               var1.method_25294(var2.field_7873, var2.field_7872, var2.field_7873 + 16, var2.field_7872 + 16, var12);
            }

            if (var7 != null && var7.isState() && var7.isInCombat()) {
               int var10 = var7.getPulseAlpha(var2.method_34266());
               if (var10 > 0) {
                  var1.method_25294(var2.field_7873, var2.field_7872, var2.field_7873 + 16, var2.field_7872 + 16, new Color(0, 220, 80, var10).getRGB());
               }
            }
         }
      } else {
         if (var7 != null && var7.isState() && var7.isInCombat()) {
            class_1799 var8 = var2.method_7677();
            if (!var8.method_7960()) {
               int var9 = var7.getPulseAlpha(var2.method_34266());
               if (var9 > 0) {
                  var1.method_25294(var2.field_7873, var2.field_7872, var2.field_7873 + 16, var2.field_7872 + 16, new Color(0, 220, 80, var9).getRGB());
               }
            }
         }
      }
   }

   @Inject(method = "method_2385(Lnet/minecraft/class_332;Lnet/minecraft/class_1735;II)V", at = @At("TAIL"))
   private void onDrawSlotTail(class_332 var1, class_1735 var2, int var3, int var4, CallbackInfo var5) {
      ItemCooldowns var6 = ItemCooldowns.getInstance();
      class_310 var7 = class_310.method_1551();
      if (var6 != null && var6.isState() && var7 != null && var7.field_1724 != null) {
         class_1799 var8 = var2.method_7677();
         if (!var8.method_7960()) {
            float var9 = var6.getRemainingSeconds(var8);
            if (!(var9 <= 0.0F)) {
               String var10 = var9 >= 10.0F ? String.format("%.0f", var9) : String.format("%.1f", var9);
               int var11 = new Color(var6.textColor.getColor()).getRGB();
               var1.method_51433(var7.field_1772, var10, var2.field_7873 + 1, var2.field_7872 + 1, var11, true);
            }
         }
      }
   }
}
