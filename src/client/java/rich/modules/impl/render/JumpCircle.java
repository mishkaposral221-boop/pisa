package rich.modules.impl.render;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import org.joml.Matrix4f;
import rich.IMinecraft;
import rich.events.api.EventHandler;
import rich.events.impl.JumpEvent;
import rich.events.impl.WorldRenderEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.color.ColorUtil;
import rich.util.render.clientpipeline.ClientPipelines;
import rich.util.timer.StopWatch;

public class JumpCircle extends ModuleStructure implements IMinecraft {
   private final List<JumpCircle.Circle> circles = new ArrayList<>(8);
   private final Identifier circleTexture = Identifier.of("rich", "images/circle/circle.png");
   private final Identifier cosmoTexture = Identifier.of("rich", "images/circle/notion.png");
   private final Identifier vertexTexture = Identifier.of("rich", "images/circle/vertex.png");
   private final Identifier glowTexture = Identifier.of("rich", "images/particle/glow.png");
   private final SelectSetting style = new SelectSetting("Style", "Стиль круга").value("Default", "cosmo", "vertex");
   private final SliderSettings maxSize = new SliderSettings("Max Size", "Максимальный размер круга").setValue(2.0F).range(1.0F, 2.0F);
   private final SliderSettings speed = new SliderSettings("Speed", "Скорость анимации").setValue(2000.0F).range(1000.0F, 2000.0F);
   private final BooleanSetting glow = new BooleanSetting("Glow", "Эффект свечения").setValue(true);
   private final ColorSetting color1 = new ColorSetting("Цвет 1", "Первый цвет").value(ColorUtil.d(137, 97, 72, 255));
   private final ColorSetting color2 = new ColorSetting("Цвет 2", "Второй цвет").value(ColorUtil.d(255, 255, 255, 255));
   private static final int SEGMENTS = 48;
   private static final int GLOW_SEGMENTS = 12;
   private static final float[] SEG_COS = new float[49];
   private static final float[] SEG_SIN = new float[49];
   private static final float[] SEG_U = new float[49];
   private static final float[] SEG_V = new float[49];
   private static final float[] GLOW_COS = new float[12];
   private static final float[] GLOW_SIN = new float[12];

   public JumpCircle() {
      super("JumpCircle", "Jump Circle", ModuleCategory.VISUALS);
      this.settings(this.style, this.maxSize, this.speed, this.glow, this.color1, this.color2);
   }

   @EventHandler
   public void onJump(JumpEvent var1) {
      if (mc.player != null && var1.getPlayer() == mc.player) {
         if (this.circles.size() < 4) {
            Vec3d var2 = new Vec3d(mc.player.getX(), Math.floor(mc.player.getY()) + 0.001, mc.player.getZ());
            this.circles.add(new JumpCircle.Circle(var2, new StopWatch()));
         }
      }
   }

   @EventHandler
   public void onWorldRender(WorldRenderEvent var1) {
      long var2 = (long)this.speed.getValue();
      this.circles.removeIf(var2x -> var2x.timer.elapsedTime() > var2);
      if (!this.circles.isEmpty()) {
         MatrixStack var4 = var1.getStack();
         net.minecraft.client.render.VertexConsumerProvider.Immediate var5 = mc.getBufferBuilders().getEntityVertexConsumers();
         Vec3d var6 = mc.gameRenderer.getCamera().getCameraPos();
         int var7 = 0;

         for (int var8 = this.circles.size(); var7 < var8; var7++) {
            this.renderSingleCircle(var4, var5, this.circles.get(var7), var6);
         }

         var5.draw();
      }
   }

   private void renderSingleCircle(MatrixStack var1, net.minecraft.client.render.VertexConsumerProvider.Immediate var2, JumpCircle.Circle var3, Vec3d var4) {
      float var5 = (float)var3.timer.elapsedTime();
      float var6 = this.speed.getValue();
      float var7 = Math.min(var5 / var6, 1.0F);
      if (!(var7 >= 1.0F)) {
         float var8 = this.bounceOut(var7);
         float var9 = var8 * this.maxSize.getValue();
         float var11 = 0.15F;
         float var12 = 0.65F;
         float var13 = 0.85F;
         float var10;
         if (var7 < var11) {
            var10 = var7 / var11;
         } else if (var7 >= var13) {
            float var14 = (var7 - var13) / (1.0F - var13);
            var10 = 1.0F - var14;
            if (var7 > var12) {
               float var15 = (var7 - var12) / (var13 - var12);
               var10 += (float)(Math.sin(var15 * Math.PI * 3.0) * 0.3 + 0.3) * (1.0F - var14);
            }
         } else if (var7 > var12) {
            float var17 = (var7 - var12) / (var13 - var12);
            var10 = 1.0F + (float)(Math.sin(var17 * Math.PI * 3.0) * 0.3 + 0.3);
         } else {
            var10 = 1.0F;
         }

         var10 = Math.max(0.0F, Math.min(1.0F, var10));
         float var18 = var5 / 1000.0F * 0.5F * 360.0F;
         if (this.glow.isValue()) {
            this.renderGradientGlow(var1, var2, var3.pos(), var9, var10 * 0.1F, var18, var4);
         }

         this.renderGradientCircle(var1, var2, var3.pos(), var9, var10, var18, var4);
      }
   }

   private void renderGradientCircle(MatrixStack var1, net.minecraft.client.render.VertexConsumerProvider.Immediate var2, Vec3d var3, float var4, float var5, float var6, Vec3d var7) {
      Identifier var8 = this.style.isSelected("cosmo") ? this.cosmoTexture : (this.style.isSelected("vertex") ? this.vertexTexture : this.circleTexture);
      VertexConsumer var9 = var2.getBuffer(ClientPipelines.BLOOM_ESP.apply(var8));
      var1.push();
      var1.translate((float)(var3.x - var7.x), (float)(var3.y - var7.y), (float)(var3.z - var7.z));
      var1.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
      Matrix4f var10 = var1.peek().getPositionMatrix();
      float var11 = var4 / 2.0F;
      int var12 = this.color1.getColor();
      int var13 = this.color2.getColor();

      for (int var14 = 0; var14 < 48; var14++) {
         float var15 = var14 / 48.0F;
         float var16 = (var14 + 1) / 48.0F;
         float var17 = (var15 + var6 / 360.0F) % 1.0F;
         float var18 = (var16 + var6 / 360.0F) % 1.0F;
         int var19 = this.getGradientColor(var12, var13, var17, var5);
         int var20 = this.getGradientColor(var12, var13, var18, var5);
         int var21 = ColorUtil.b(var19, var20, 0.5F);
         float var22 = SEG_COS[var14] * var11;
         float var23 = SEG_SIN[var14] * var11;
         float var24 = SEG_COS[var14 + 1] * var11;
         float var25 = SEG_SIN[var14 + 1] * var11;
         var9.vertex(var10, 0.0F, 0.0F, 0.0F).texture(0.5F, 0.5F).color(var21);
         var9.vertex(var10, var22, var23, 0.0F).texture(SEG_U[var14], SEG_V[var14]).color(var19);
         var9.vertex(var10, var24, var25, 0.0F).texture(SEG_U[var14 + 1], SEG_V[var14 + 1]).color(var20);
         var9.vertex(var10, var24, var25, 0.0F).texture(SEG_U[var14 + 1], SEG_V[var14 + 1]).color(var20);
      }

      var1.pop();
   }

   private void renderGradientGlow(MatrixStack var1, net.minecraft.client.render.VertexConsumerProvider.Immediate var2, Vec3d var3, float var4, float var5, float var6, Vec3d var7) {
      int var8 = this.color1.getColor();
      int var9 = this.color2.getColor();

      for (int var10 = 0; var10 < 2; var10++) {
         float var11 = var4 * (1.3F + var10 * 0.4F);
         float var12 = var5 * (0.35F - var10 * 0.1F);
         this.renderGlowLayer(var1, var2, var3, var11, var12, var6, var8, var9, var7);
      }

      int var13 = ColorUtil.b(ColorUtil.d(var8, var5 * 0.2F), ColorUtil.d(var9, var5 * 0.2F), 0.5F);
      this.renderTexturedQuad(var1, var2, var3, var4 * 2.5F, var13, this.glowTexture, var7);
   }

   private void renderGlowLayer(MatrixStack var1, net.minecraft.client.render.VertexConsumerProvider.Immediate var2, Vec3d var3, float var4, float var5, float var6, int var7, int var8, Vec3d var9) {
      VertexConsumer var10 = var2.getBuffer(ClientPipelines.BLOOM_ESP.apply(this.glowTexture));
      float var11 = var4 / 2.0F;
      float var12 = var4 * 0.4F;

      for (int var13 = 0; var13 < 12; var13++) {
         float var14 = var13 / 12.0F;
         float var15 = (var14 + var6 / 360.0F) % 1.0F;
         int var16 = this.getGradientColor(var7, var8, var15, var5);
         float var17 = (float)(var3.x + GLOW_COS[var13] * var11 * 0.8F);
         float var18 = (float)(var3.z + GLOW_SIN[var13] * var11 * 0.8F);
         var1.push();
         var1.translate((float)(var17 - var9.x), (float)(var3.y - var9.y), (float)(var18 - var9.z));
         var1.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
         Matrix4f var19 = var1.peek().getPositionMatrix();
         float var20 = var12 / 2.0F;
         var10.vertex(var19, -var20, -var20, 0.0F).texture(0.0F, 0.0F).color(var16);
         var10.vertex(var19, var20, -var20, 0.0F).texture(1.0F, 0.0F).color(var16);
         var10.vertex(var19, var20, var20, 0.0F).texture(1.0F, 1.0F).color(var16);
         var10.vertex(var19, -var20, var20, 0.0F).texture(0.0F, 1.0F).color(var16);
         var1.pop();
      }
   }

   private void renderTexturedQuad(MatrixStack var1, net.minecraft.client.render.VertexConsumerProvider.Immediate var2, Vec3d var3, float var4, int var5, Identifier var6, Vec3d var7) {
      VertexConsumer var8 = var2.getBuffer(ClientPipelines.BLOOM_ESP.apply(var6));
      var1.push();
      var1.translate((float)(var3.x - var7.x), (float)(var3.y - var7.y), (float)(var3.z - var7.z));
      var1.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
      Matrix4f var9 = var1.peek().getPositionMatrix();
      float var10 = var4 / 2.0F;
      var8.vertex(var9, -var10, -var10, 0.0F).texture(0.0F, 0.0F).color(var5);
      var8.vertex(var9, var10, -var10, 0.0F).texture(1.0F, 0.0F).color(var5);
      var8.vertex(var9, var10, var10, 0.0F).texture(1.0F, 1.0F).color(var5);
      var8.vertex(var9, -var10, var10, 0.0F).texture(0.0F, 1.0F).color(var5);
      var1.pop();
   }

   private int getGradientColor(int var1, int var2, float var3, float var4) {
      float var5 = var3 <= 0.5F ? var3 * 2.0F : (1.0F - var3) * 2.0F;
      return ColorUtil.d(ColorUtil.b(var1, var2, var5), var4);
   }

   private float bounceOut(float var1) {
      float var2 = 7.5625F;
      float var3 = 2.75F;
      if (var1 < 1.0F / var3) {
         return var2 * var1 * var1;
      } else if (var1 < 2.0F / var3) {
         float var6;
         return var2 * (var6 = var1 - 1.5F / var3) * var6 + 0.75F;
      } else {
         float var4;
         float var5;
         return var1 < 2.5F / var3 ? var2 * (var4 = var1 - 2.25F / var3) * var4 + 0.9375F : var2 * (var5 = var1 - 2.625F / var3) * var5 + 0.984375F;
      }
   }

   public void applyThemeColors(int var1, int var2) {
      this.color1.setColor(var1);
      this.color2.setColor(var2);
   }

   static {
      for (int var0 = 0; var0 <= 48; var0++) {
         double var1 = (Math.PI * 2) * var0 / 48.0;
         SEG_COS[var0] = (float)Math.cos(var1);
         SEG_SIN[var0] = (float)Math.sin(var1);
         SEG_U[var0] = (float)(0.5 + 0.5 * Math.cos(var1));
         SEG_V[var0] = (float)(0.5 + 0.5 * Math.sin(var1));
      }

      for (int var3 = 0; var3 < 12; var3++) {
         double var4 = (Math.PI * 2) * var3 / 12.0;
         GLOW_COS[var3] = (float)Math.cos(var4);
         GLOW_SIN[var3] = (float)Math.sin(var4);
      }
   }

   public record Circle(Vec3d pos, StopWatch timer) {
   }
}
