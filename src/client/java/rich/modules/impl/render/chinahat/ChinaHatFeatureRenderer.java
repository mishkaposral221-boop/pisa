package rich.modules.impl.render.chinahat;

import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import org.joml.Matrix4f;
import rich.modules.impl.render.ChinaHat;
import rich.util.color.ColorUtil;
import rich.util.render.clientpipeline.ClientPipelines;

public class ChinaHatFeatureRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
   private static final float PI2 = (float) (Math.PI * 2);
   private static final int CIRCLE_SEGMENTS = 720;
   private static final int OUTLINE_SEGMENTS = 360;

   public ChinaHatFeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> var1) {
      super(var1);
   }

   public void render(MatrixStack var1, OrderedRenderCommandQueue var2, int var3, PlayerEntityRenderState var4, float var5, float var6) {
      MinecraftClient var7 = MinecraftClient.getInstance();
      ChinaHat var8 = ChinaHat.getInstance();
      if (var8 != null && var8.isState()) {
         if (var7.player != null) {
            if (this.isLocalPlayer(var4, var7)) {
               var1.push();
               ((PlayerEntityModel)this.getContextModel()).head.applyTransform(var1);
               var1.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));
               var1.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
               var1.translate(0.0F, 0.5F, 0.0F);
               net.minecraft.client.render.VertexConsumerProvider.Immediate var9 = var7.getBufferBuilders().getEntityVertexConsumers();
               boolean var10 = var8.style.getSelected().equals("Сеточный");
               if (var10) {
                  this.renderGrid(var1, var9, var8);
                  this.renderOutline(var1, var9, var8);
               } else {
                  this.renderFlatHat(var1, var9, var8);
                  this.renderOutline(var1, var9, var8);
               }

               var9.draw();
               var1.pop();
            }
         }
      }
   }

   private boolean isLocalPlayer(PlayerEntityRenderState var1, MinecraftClient var2) {
      try {
         if (var1.id == var2.player.getId()) {
            return true;
         }
      } catch (Exception var5) {
      }

      try {
         if (var1.playerName != null && var2.player.getName() != null) {
            return var1.playerName.getString().equals(var2.player.getName().getString());
         }
      } catch (Exception var4) {
      }

      return false;
   }

   private void renderGrid(MatrixStack var1, VertexConsumerProvider var2, ChinaHat var3) {
      VertexConsumer var4 = var2.getBuffer(ClientPipelines.CHINA_HAT_GRID);
      Matrix4f var5 = var1.peek().getPositionMatrix();
      float var6 = 0.55F;
      float var7 = 0.31F;
      float var8 = 5.0F;
      short var9 = 220;
      byte var10 = 32;

      for (int var11 = 0; var11 < var10; var11++) {
         float var12 = var11 * (float) (Math.PI * 2) / var10;
         float var13 = -MathHelper.sin(var12) * var6;
         float var14 = MathHelper.cos(var12) * var6;
         int var15 = this.getGradientColor(var11 * (720 / var10), 720, var3, var8);
         var15 = ColorUtil.c(var15, var9);
         var4.vertex(var5, 0.0F, var7, 0.0F).color(var15);
         var4.vertex(var5, var13, 0.0F, var14).color(var15);
      }

      byte var25 = 10;
      byte var26 = 64;

      for (int var27 = 1; var27 <= var25; var27++) {
         float var28 = (float)var27 / var25;
         float var30 = var6 * var28;
         float var16 = var7 * (1.0F - var28);

         for (int var17 = 0; var17 < var26; var17++) {
            float var18 = var17 * (float) (Math.PI * 2) / var26;
            float var19 = (var17 + 1) * (float) (Math.PI * 2) / var26;
            int var20 = this.getGradientColor(var17 * (720 / var26), 720, var3, var8);
            var20 = ColorUtil.c(var20, var9);
            float var21 = -MathHelper.sin(var18) * var30;
            float var22 = MathHelper.cos(var18) * var30;
            float var23 = -MathHelper.sin(var19) * var30;
            float var24 = MathHelper.cos(var19) * var30;
            var4.vertex(var5, var21, var16, var22).color(var20);
            var4.vertex(var5, var23, var16, var24).color(var20);
         }
      }
   }

   private void renderFlatHat(MatrixStack var1, VertexConsumerProvider var2, ChinaHat var3) {
      VertexConsumer var4 = var2.getBuffer(ClientPipelines.CHINA_HAT);
      Matrix4f var5 = var1.peek().getPositionMatrix();
      float var6 = 0.55F;
      float var7 = 0.31F;
      short var8 = 185;
      float var9 = 5.0F;
      int var10 = this.getGradientColor(0, 720, var3, var9);
      var10 = ColorUtil.c(var10, var8);
      var4.vertex(var5, 0.0F, var7, 0.0F).color(var10);

      for (int var11 = 0; var11 <= 720; var11++) {
         int var12 = this.getGradientColor(var11, 720, var3, var9);
         var12 = ColorUtil.c(var12, var8);
         float var13 = var11 * (float) (Math.PI * 2) / 720.0F;
         float var14 = -MathHelper.sin(var13) * var6;
         float var15 = MathHelper.cos(var13) * var6;
         var4.vertex(var5, var14, 0.0F, var15).color(var12);
      }

      for (int var17 = 720; var17 >= 0; var17--) {
         int var19 = this.getGradientColor(var17, 720, var3, var9);
         var19 = ColorUtil.c(var19, var8);
         float var21 = var17 * (float) (Math.PI * 2) / 720.0F;
         float var22 = -MathHelper.sin(var21) * var6;
         float var23 = MathHelper.cos(var21) * var6;
         var4.vertex(var5, var22, 0.0F, var23).color(var19);
      }

      var4.vertex(var5, 0.0F, var7, 0.0F).color(var10);
   }

   private void renderOutline(MatrixStack var1, VertexConsumerProvider var2, ChinaHat var3) {
      VertexConsumer var4 = var2.getBuffer(ClientPipelines.CHINA_HAT_OUTLINE);
      Matrix4f var5 = var1.peek().getPositionMatrix();
      float var6 = 0.55F;
      float var7 = 5.0F;
      short var8 = 255;

      for (int var9 = 0; var9 <= 360; var9++) {
         int var10 = this.getGradientColor(var9 * 2, 720, var3, var7);
         var10 = ColorUtil.c(var10, var8);
         float var11 = var9 * (float) (Math.PI * 2) / 360.0F;
         float var12 = -MathHelper.sin(var11) * var6;
         float var13 = MathHelper.cos(var11) * var6;
         var4.vertex(var5, var12, 0.0F, var13).color(var10);
      }
   }

   private int getGradientColor(int var1, int var2, ChinaHat var3, float var4) {
      long var5 = System.currentTimeMillis();
      float var7 = (float)var5 / (1000.0F / var4) % var2;
      int var8 = (int)((var1 + var7) % var2);
      float var9 = (float)var8 / var2;
      String var10 = var3.colorMode.getSelected();
      if (var10.equals("1 Color")) {
         return var3.color1.getColor();
      }

      if (var10.equals("2 Colors")) {
         int var16 = var3.color1.getColor();
         int var18 = var3.color2.getColor();
         return var9 < 0.5F ? ColorUtil.a(var16, var18, var9 * 2.0F) : ColorUtil.a(var18, var16, (var9 - 0.5F) * 2.0F);
      }

      if (var10.equals("3 Colors")) {
         int var15 = var3.color1.getColor();
         int var17 = var3.color2.getColor();
         int var19 = var3.color3.getColor();
         if (var9 < 0.333F) {
            return ColorUtil.a(var15, var17, var9 * 3.0F);
         } else {
            return var9 < 0.666F ? ColorUtil.a(var17, var19, (var9 - 0.333F) * 3.0F) : ColorUtil.a(var19, var15, (var9 - 0.666F) * 3.0F);
         }
      } else if (var10.equals("4 Colors")) {
         int var11 = var3.color1.getColor();
         int var12 = var3.color2.getColor();
         int var13 = var3.color3.getColor();
         int var14 = var3.color4.getColor();
         if (var9 < 0.25F) {
            return ColorUtil.a(var11, var12, var9 * 4.0F);
         } else if (var9 < 0.5F) {
            return ColorUtil.a(var12, var13, (var9 - 0.25F) * 4.0F);
         } else {
            return var9 < 0.75F ? ColorUtil.a(var13, var14, (var9 - 0.5F) * 4.0F) : ColorUtil.a(var14, var11, (var9 - 0.75F) * 4.0F);
         }
      } else {
         return var3.color1.getColor();
      }
   }
}
