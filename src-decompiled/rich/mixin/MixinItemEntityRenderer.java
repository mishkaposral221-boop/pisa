package rich.mixin;

import java.util.WeakHashMap;
import net.minecraft.class_10039;
import net.minecraft.class_10428;
import net.minecraft.class_11659;
import net.minecraft.class_12075;
import net.minecraft.class_1542;
import net.minecraft.class_238;
import net.minecraft.class_4587;
import net.minecraft.class_5819;
import net.minecraft.class_7833;
import net.minecraft.class_916;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.modules.impl.render.ItemPhysic;

@Mixin(class_916.class)
public abstract class MixinItemEntityRenderer {
   @Unique
   private static final WeakHashMap<class_10039, Boolean> groundStateMap = new WeakHashMap<>();
   @Unique
   private class_10039 currentState = null;

   @Inject(method = "method_62470(Lnet/minecraft/class_1542;Lnet/minecraft/class_10039;F)V", at = @At("HEAD"))
   private void captureGroundState(class_1542 var1, class_10039 var2, float var3, CallbackInfo var4) {
      groundStateMap.put(var2, var1.method_24828());
   }

   @Redirect(
      method = "method_3996(Lnet/minecraft/class_10039;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_12075;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4587;method_46416(FFF)V", ordinal = 0)
   )
   private void redirectTranslate(class_4587 var1, float var2, float var3, float var4, class_10039 var5, class_4587 var6, class_11659 var7, class_12075 var8) {
      this.currentState = var5;
      ItemPhysic var9 = ItemPhysic.getInstance();
      if (var9 != null && var9.isState() && var9.mode.isSelected("Обычная")) {
         class_238 var10 = var5.field_55310.method_72173();
         float var11 = -((float)var10.field_1322) + 0.0625F;
         var1.method_46416(var2, var11, var4);
      } else {
         var1.method_46416(var2, var3, var4);
      }
   }

   @Redirect(
      method = "method_3996(Lnet/minecraft/class_10039;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_12075;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_916;method_72986(Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;ILnet/minecraft/class_10428;Lnet/minecraft/class_5819;Lnet/minecraft/class_238;)V"
      )
   )
   private void redirectRender(class_4587 var1, class_11659 var2, int var3, class_10428 var4, class_5819 var5, class_238 var6) {
      ItemPhysic var7 = ItemPhysic.getInstance();
      if (var7 != null && var7.isState() && var7.mode.isSelected("Обычная") && this.currentState != null) {
         float var8 = this.currentState.field_53328;
         float var9 = this.currentState.field_53435;
         boolean var10 = groundStateMap.getOrDefault(this.currentState, false);
         float var11 = class_1542.method_27314(var8, var9);
         var1.method_22907(class_7833.field_40716.rotation(-var11));
         if (var10) {
            var1.method_22907(class_7833.field_40714.rotationDegrees(90.0F));
            float var12 = (float)var6.method_17940() / 2.0F;
            var1.method_46416(0.0F, -var12 + 0.0625F, 0.0F);
         } else {
            float var14 = 15.0F;
            float var13 = (var8 * var14 + var9 * 360.0F) % 360.0F;
            var1.method_22907(class_7833.field_40714.rotationDegrees(var13));
         }
      }

      class_916.method_72986(var1, var2, var3, var4, var5, var6);
   }
}
