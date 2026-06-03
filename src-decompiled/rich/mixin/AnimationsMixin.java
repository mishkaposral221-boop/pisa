package rich.mixin;

import net.minecraft.class_310;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_408;
import net.minecraft.class_9779;
import org.joml.Vector2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.modules.impl.render.Animations;
import rich.util.render.hud.HotbarAnimState;

@Mixin(value = class_329.class, priority = 900)
public abstract class AnimationsMixin {
   @Unique
   private static float richChatOffsetY = 0.0F;
   @Unique
   private static long richChatLastTime = System.currentTimeMillis();
   @Unique
   private static boolean richChatWasOpen = false;
   @Unique
   private static float richTabScale = 0.0F;
   @Unique
   private static long richTabLastTime = System.currentTimeMillis();
   @Unique
   private static long richHotbarLastTime = System.currentTimeMillis();

   @Inject(method = "method_55802", at = @At("HEAD"))
   private void onRenderChatHead(class_332 var1, class_9779 var2, CallbackInfo var3) {
      Animations var4 = Animations.getInstance();
      if (var4 != null && var4.isState() && var4.chatAnim.isValue()) {
         class_310 var5 = class_310.method_1551();
         boolean var6 = var5.field_1755 instanceof class_408;
         long var7 = System.currentTimeMillis();
         float var9 = Math.min((float)(var7 - richChatLastTime) / 1000.0F, 0.1F);
         richChatLastTime = var7;
         if (var6 && !richChatWasOpen) {
            richChatOffsetY = 15.0F;
         }

         richChatWasOpen = var6;
         if (var6) {
            richChatOffsetY = richChatOffsetY + (0.0F - richChatOffsetY) * var4.lerpFactor(var9);
            if (Math.abs(richChatOffsetY) < 0.1F) {
               richChatOffsetY = 0.0F;
            }
         } else {
            richChatOffsetY = 0.0F;
         }

         if (Math.abs(richChatOffsetY) > 0.05F) {
            var1.method_51448().pushMatrix();
            var1.method_51448().translate(new Vector2f(0.0F, richChatOffsetY));
         }
      } else {
         richChatOffsetY = 0.0F;
      }
   }

   @Inject(method = "method_55802", at = @At("TAIL"))
   private void onRenderChatTail(class_332 var1, class_9779 var2, CallbackInfo var3) {
      Animations var4 = Animations.getInstance();
      if (var4 != null && var4.isState() && var4.chatAnim.isValue()) {
         if (Math.abs(richChatOffsetY) > 0.05F) {
            var1.method_51448().popMatrix();
         }
      }
   }

   @Inject(method = "method_55804", at = @At("HEAD"))
   private void onRenderTabHead(class_332 var1, class_9779 var2, CallbackInfo var3) {
      Animations var4 = Animations.getInstance();
      if (var4 != null && var4.isState() && var4.tabAnim.isValue()) {
         long var5 = System.currentTimeMillis();
         float var7 = Math.min((float)(var5 - richTabLastTime) / 1000.0F, 0.1F);
         richTabLastTime = var5;
         richTabScale = richTabScale + (1.0F - richTabScale) * var4.lerpFactor(var7);
         float var8 = 0.88F + 0.12F * richTabScale;
         class_310 var9 = class_310.method_1551();
         float var10 = var9.method_22683().method_4486() / 2.0F;
         float var11 = var9.method_22683().method_4502() / 2.0F;
         var1.method_51448().pushMatrix();
         var1.method_51448().translate(new Vector2f(var10, var11));
         var1.method_51448().scale(var8, var8);
         var1.method_51448().translate(new Vector2f(-var10, -var11));
      } else {
         richTabScale = 1.0F;
      }
   }

   @Inject(method = "method_55804", at = @At("TAIL"))
   private void onRenderTabTail(class_332 var1, class_9779 var2, CallbackInfo var3) {
      Animations var4 = Animations.getInstance();
      if (var4 != null && var4.isState() && var4.tabAnim.isValue()) {
         var1.method_51448().popMatrix();
      }
   }

   @Inject(method = "method_39191", at = @At("HEAD"))
   private void onTick(CallbackInfo var1) {
      class_310 var2 = class_310.method_1551();
      if (var2 != null) {
         if (!var2.field_1690.field_1907.method_1434()) {
            richTabScale = 0.0F;
         }
      }
   }

   @Inject(method = "method_1759", at = @At("HEAD"))
   private void onRenderHotbarHead(class_332 var1, class_9779 var2, CallbackInfo var3) {
      Animations var4 = Animations.getInstance();
      class_310 var5 = class_310.method_1551();
      if (var5.field_1724 != null) {
         int var6 = var5.field_1724.method_31548().method_67532();
         long var7 = System.currentTimeMillis();
         float var9 = Math.min((float)(var7 - richHotbarLastTime) / 1000.0F, 0.1F);
         richHotbarLastTime = var7;
         if (HotbarAnimState.smoothSlot < 0.0F) {
            HotbarAnimState.smoothSlot = var6;
         }

         if (var4 != null && var4.isState() && var4.hotbarAnim.isValue()) {
            HotbarAnimState.smoothSlot = HotbarAnimState.smoothSlot + (var6 - HotbarAnimState.smoothSlot) * var4.lerpFactor(var9);
            if (Math.abs(HotbarAnimState.smoothSlot - var6) < 0.01F) {
               HotbarAnimState.smoothSlot = var6;
            }
         } else {
            HotbarAnimState.smoothSlot = var6;
         }
      }
   }
}
