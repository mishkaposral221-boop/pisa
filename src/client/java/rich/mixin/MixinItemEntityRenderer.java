package rich.mixin;

import java.util.WeakHashMap;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.render.entity.state.ItemStackEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Box;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.modules.impl.render.ItemPhysic;

@Mixin(ItemEntityRenderer.class)
public abstract class MixinItemEntityRenderer {
   @Unique
   private static final WeakHashMap<ItemEntityRenderState, Boolean> groundStateMap = new WeakHashMap<>();
   @Unique
   private ItemEntityRenderState currentState = null;

   @Inject(method = "updateRenderState(Lnet/minecraft/ItemEntity;Lnet/minecraft/ItemEntityRenderState;F)V", at = @At("HEAD"))
   private void captureGroundState(ItemEntity var1, ItemEntityRenderState var2, float var3, CallbackInfo var4) {
      groundStateMap.put(var2, var1.isOnGround());
   }

   @Redirect(
      method = "render(Lnet/minecraft/ItemEntityRenderState;Lnet/minecraft/MatrixStack;Lnet/minecraft/OrderedRenderCommandQueue;Lnet/minecraft/CameraRenderState;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/MatrixStack;translate(FFF)V", ordinal = 0)
   )
   private void redirectTranslate(MatrixStack var1, float var2, float var3, float var4, ItemEntityRenderState var5, MatrixStack var6, OrderedRenderCommandQueue var7, CameraRenderState var8) {
      this.currentState = var5;
      ItemPhysic var9 = ItemPhysic.getInstance();
      if (var9 != null && var9.isState() && var9.mode.isSelected("Обычная")) {
         Box var10 = var5.itemRenderState.getModelBoundingBox();
         float var11 = -((float)var10.minY) + 0.0625F;
         var1.translate(var2, var11, var4);
      } else {
         var1.translate(var2, var3, var4);
      }
   }

   @Redirect(
      method = "render(Lnet/minecraft/ItemEntityRenderState;Lnet/minecraft/MatrixStack;Lnet/minecraft/OrderedRenderCommandQueue;Lnet/minecraft/CameraRenderState;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/ItemEntityRenderer;render(Lnet/minecraft/MatrixStack;Lnet/minecraft/OrderedRenderCommandQueue;ILnet/minecraft/ItemStackEntityRenderState;Lnet/minecraft/Random;Lnet/minecraft/Box;)V"
      )
   )
   private void redirectRender(MatrixStack var1, OrderedRenderCommandQueue var2, int var3, ItemStackEntityRenderState var4, Random var5, Box var6) {
      ItemPhysic var7 = ItemPhysic.getInstance();
      if (var7 != null && var7.isState() && var7.mode.isSelected("Обычная") && this.currentState != null) {
         float var8 = this.currentState.age;
         float var9 = this.currentState.uniqueOffset;
         boolean var10 = groundStateMap.getOrDefault(this.currentState, false);
         float var11 = ItemEntity.getRotation(var8, var9);
         var1.multiply(RotationAxis.POSITIVE_Y.rotation(-var11));
         if (var10) {
            var1.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
            float var12 = (float)var6.getLengthY() / 2.0F;
            var1.translate(0.0F, -var12 + 0.0625F, 0.0F);
         } else {
            float var14 = 15.0F;
            float var13 = (var8 * var14 + var9 * 360.0F) % 360.0F;
            var1.multiply(RotationAxis.POSITIVE_X.rotationDegrees(var13));
         }
      }

      ItemEntityRenderer.render(var1, var2, var3, var4, var5, var6);
   }
}
