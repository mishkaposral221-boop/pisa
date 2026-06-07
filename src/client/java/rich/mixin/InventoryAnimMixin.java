package rich.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.modules.impl.render.Animations;

@Mixin(HandledScreen.class)
public abstract class InventoryAnimMixin {
   @Unique
   private float richInvScale = 0.0F;
   @Unique
   private long richInvLastTime = -1L;
   @Unique
   private boolean richInvPushed = false;

   @Inject(method = "init", at = @At("TAIL"))
   private void onInit(CallbackInfo var1) {
      this.richInvScale = 0.0F;
      this.richInvLastTime = System.currentTimeMillis();
      this.richInvPushed = false;
   }

   @Inject(method = "render", at = @At("HEAD"))
   private void onRenderHead(DrawContext var1, int var2, int var3, float var4, CallbackInfo var5) {
      this.richInvPushed = false;
      Animations var6 = Animations.getInstance();
      if (var6 != null && var6.isState() && var6.inventoryAnim.isValue()) {
         long var7 = System.currentTimeMillis();
         if (this.richInvLastTime < 0L) {
            this.richInvLastTime = var7;
         }

         float var9 = Math.min((float)(var7 - this.richInvLastTime) / 1000.0F, 0.1F);
         this.richInvLastTime = var7;
         this.richInvScale = this.richInvScale + (1.0F - this.richInvScale) * var6.lerpFactor(var9);
         if (this.richInvScale > 0.999F) {
            this.richInvScale = 1.0F;
         } else {
            MinecraftClient var10 = MinecraftClient.getInstance();
            float var11 = var10.getWindow().getScaledWidth() / 2.0F;
            float var12 = var10.getWindow().getScaledHeight() / 2.0F;
            float var13 = this.richInvScale;
            var1.getMatrices().pushMatrix();
            var1.getMatrices().translate(var11, var12);
            var1.getMatrices().scale(var13, var13);
            var1.getMatrices().translate(-var11, -var12);
            this.richInvPushed = true;
         }
      } else {
         this.richInvScale = 1.0F;
      }
   }

   @Inject(method = "render", at = @At("TAIL"))
   private void onRenderTail(DrawContext var1, int var2, int var3, float var4, CallbackInfo var5) {
      if (this.richInvPushed) {
         var1.getMatrices().popMatrix();
         this.richInvPushed = false;
      }
   }
}
