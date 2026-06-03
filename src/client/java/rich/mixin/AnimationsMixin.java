package rich.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.RenderTickCounter;
import org.joml.Vector2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.modules.impl.render.Animations;
import rich.util.render.hud.HotbarAnimState;

@Mixin(value = InGameHud.class, priority = 900)
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

   @Inject(method = "renderChat", at = @At("HEAD"))
   private void onRenderChatHead(DrawContext var1, RenderTickCounter var2, CallbackInfo var3) {
      Animations var4 = Animations.getInstance();
      if (var4 != null && var4.isState() && var4.chatAnim.isValue()) {
         MinecraftClient var5 = MinecraftClient.getInstance();
         boolean var6 = var5.currentScreen instanceof ChatScreen;
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
            var1.getMatrices().pushMatrix();
            var1.getMatrices().translate(new Vector2f(0.0F, richChatOffsetY));
         }
      } else {
         richChatOffsetY = 0.0F;
      }
   }

   @Inject(method = "renderChat", at = @At("TAIL"))
   private void onRenderChatTail(DrawContext var1, RenderTickCounter var2, CallbackInfo var3) {
      Animations var4 = Animations.getInstance();
      if (var4 != null && var4.isState() && var4.chatAnim.isValue()) {
         if (Math.abs(richChatOffsetY) > 0.05F) {
            var1.getMatrices().popMatrix();
         }
      }
   }

   @Inject(method = "renderPlayerList", at = @At("HEAD"))
   private void onRenderTabHead(DrawContext var1, RenderTickCounter var2, CallbackInfo var3) {
      Animations var4 = Animations.getInstance();
      if (var4 != null && var4.isState() && var4.tabAnim.isValue()) {
         long var5 = System.currentTimeMillis();
         float var7 = Math.min((float)(var5 - richTabLastTime) / 1000.0F, 0.1F);
         richTabLastTime = var5;
         richTabScale = richTabScale + (1.0F - richTabScale) * var4.lerpFactor(var7);
         float var8 = 0.88F + 0.12F * richTabScale;
         MinecraftClient var9 = MinecraftClient.getInstance();
         float var10 = var9.getWindow().getScaledWidth() / 2.0F;
         float var11 = var9.getWindow().getScaledHeight() / 2.0F;
         var1.getMatrices().pushMatrix();
         var1.getMatrices().translate(new Vector2f(var10, var11));
         var1.getMatrices().scale(var8, var8);
         var1.getMatrices().translate(new Vector2f(-var10, -var11));
      } else {
         richTabScale = 1.0F;
      }
   }

   @Inject(method = "renderPlayerList", at = @At("TAIL"))
   private void onRenderTabTail(DrawContext var1, RenderTickCounter var2, CallbackInfo var3) {
      Animations var4 = Animations.getInstance();
      if (var4 != null && var4.isState() && var4.tabAnim.isValue()) {
         var1.getMatrices().popMatrix();
      }
   }

   @Inject(method = "tick", at = @At("HEAD"))
   private void onTick(CallbackInfo var1) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2 != null) {
         if (!var2.options.playerListKey.isPressed()) {
            richTabScale = 0.0F;
         }
      }
   }

   @Inject(method = "renderHotbar", at = @At("HEAD"))
   private void onRenderHotbarHead(DrawContext var1, RenderTickCounter var2, CallbackInfo var3) {
      Animations var4 = Animations.getInstance();
      MinecraftClient var5 = MinecraftClient.getInstance();
      if (var5.player != null) {
         int var6 = var5.player.getInventory().getSelectedSlot();
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
