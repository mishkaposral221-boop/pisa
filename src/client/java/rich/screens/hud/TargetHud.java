package rich.screens.hud;

import java.awt.Color;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import rich.client.draggables.AbstractHudElement;
import rich.theme.ClientTheme;
import rich.util.network.Network;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.timer.StopWatch;

public class TargetHud extends AbstractHudElement {
   private final StopWatch stopWatch = new StopWatch();
   private LivingEntity lastTarget;
   private float healthAnimation = 0.0F;
   private float trailAnimation = 0.0F;
   private float absorptionAnimation = 0.0F;
   private float displayedHealth = 0.0F;
   private long lastUpdateTime = System.currentTimeMillis();
   private long startTime = System.currentTimeMillis();

   public TargetHud() {
      super("TargetHud", 10, 80, 112, 40, true);
   }

   @Override
   public boolean visible() {
      return true;
   }

   @Override
   public void tick() {
      if (this.mc.player != null && this.mc.world != null) {
         LivingEntity var1 = null;
         if (this.mc.targetedEntity instanceof LivingEntity var2 && (!var2.isInvisible() || this.hasArmor(var2))) {
            var1 = var2;
         }

         if (var1 != null) {
            this.lastTarget = var1;
            this.startAnimation();
            this.stopWatch.reset();
         } else if (this.isChat(this.mc.currentScreen) && this.lastTarget != null) {
            this.startAnimation();
            this.stopWatch.reset();
         } else if (this.isChat(this.mc.currentScreen) && this.mc.player != null) {
            this.lastTarget = this.mc.player;
            this.startAnimation();
            this.stopWatch.reset();
         } else if (this.stopWatch.finished(5000.0)) {
            this.stopAnimation();
         }
      } else {
         this.stopAnimation();
      }
   }

   private float lerp(float var1, float var2, float var3, float var4) {
      float var5 = (float)(1.0 - Math.pow(0.001, var3 * var4));
      return var1 + (var2 - var1) * var5;
   }

   private float snapToStep(float var1, float var2) {
      return Math.round(var1 / var2) * var2;
   }

   private float getHealth(LivingEntity var1) {
      return var1.isInvisible() && !this.hasArmor(var1) && !Network.isSpookyTime() && !Network.isCopyTime() ? var1.getMaxHealth() : var1.getHealth();
   }

   private String getHealthString(float var1) {
      if (this.lastTarget != null && this.lastTarget.isInvisible() && !this.hasArmor(this.lastTarget) && !Network.isSpookyTime() && !Network.isCopyTime()) {
         return "??";
      } else {
         return String.valueOf(Math.round(var1));
      }
   }

   @Override
   public void drawDraggable(DrawContext var1, int var2) {
      if (var2 > 0) {
         if (this.lastTarget != null) {
            long var3 = System.currentTimeMillis();
            float var5 = (float)(var3 - this.lastUpdateTime) / 1000.0F;
            this.lastUpdateTime = var3;
            var5 = Math.min(var5, 0.1F);
            float var6 = this.getX();
            float var7 = this.getY();
            this.setWidth(112);
            this.setHeight(40);
            float var8 = this.scaleAnimation.getOutput().floatValue();
            this.drawBackground(var6, var7, var8);
            this.drawFace(var6, var7, var8);
            this.drawContent(var6, var7, var8, var5);
         }
      }
   }

   private void drawBackground(float var1, float var2, float var3) {
      int var4 = (int)(255.0F * var3);
      Render2D.gradientRect(var1 + 2.0F, var2 + 2.0F, this.getWidth() - 4, this.getHeight() - 4, ClientTheme.bgGradient(var4), 6.0F);
      Render2D.outline(var1 + 2.0F, var2 + 2.0F, this.getWidth() - 4, this.getHeight() - 4, 0.35F, ClientTheme.outline(var4), 5.0F);
   }

   private void drawFace(float var1, float var2, float var3) {
      if (this.mc.getEntityRenderDispatcher().getRenderer(this.lastTarget) instanceof LivingEntityRenderer var5) {
         LivingEntityRenderState var6 = (LivingEntityRenderState)var5.getAndUpdateRenderState(this.lastTarget, this.lastTickDelta);
         Identifier var7 = var5.getTexture(var6);
         float var8 = 24.0F;
         float var9 = var1 + 9.0F;
         float var10 = var2 + 8.0F;
         float var11 = this.lastTarget.hurtTime > 0 ? this.lastTarget.hurtTime / 10.0F : 0.0F;
         short var12 = 255;
         int var13 = (int)(255.0F * (1.0F - var11));
         int var14 = (int)(255.0F * (1.0F - var11));
         int var15 = new Color(var12, var13, var14, (int)(255.0F * var3)).getRGB();
         float var16 = 0.125F;
         float var17 = 0.125F;
         float var18 = 0.25F;
         float var19 = 0.25F;
         Render2D.texture(var7, var9, var10, var8, var8, var16, var17, var18, var19, var15, 0.0F, 4.0F);
         float var20 = 1.1F;
         float var21 = var8 * var20;
         float var22 = (var21 - var8) / 2.0F;
         float var23 = 0.625F;
         float var24 = 0.125F;
         float var25 = 0.75F;
         float var26 = 0.25F;
         Render2D.texture(var7, var9 - var22, var10 - var22, var21, var21, var23, var24, var25, var26, var15, 0.0F, 4.0F);
      }
   }

   private void drawContent(float var1, float var2, float var3, float var4) {
      float var5 = 24.0F;
      float var6 = var1 + 9.0F;
      float var7 = var6 + var5 + 6.0F;
      float var8 = var2 + 13.0F;
      float var9 = this.getHealth(this.lastTarget);
      float var10 = this.lastTarget.getMaxHealth();
      float var11 = this.lastTarget.getAbsorptionAmount();
      boolean var12 = this.lastTarget.isInvisible() && !Network.isSpookyTime() && !Network.isCopyTime();
      float var13;
      if (var12) {
         var13 = var10;
      } else {
         var13 = var9 + var11;
      }

      this.displayedHealth = this.lerp(this.displayedHealth, var13, var4, 5.0F);
      float var14 = this.snapToStep(this.displayedHealth, 0.25F);
      String var15 = this.getHealthString(var14);
      String var16 = this.lastTarget.getName().getString();
      int var60 = (int)(255.0F * var3);
      int var61 = new Color(0, 0, 0, var60).getRGB();
      Fonts.BOLD.draw(var16, var7 + 0.5F, var8 + 0.5F, 5.5F, var61);
      Fonts.BOLD.draw(var16, var7, var8, 5.5F, new Color(255, 255, 255, var60).getRGB());
      float var17 = Fonts.BOLD.getWidth(var15, 5.5F);
      float var59 = Fonts.BOLD.getWidth(var16, 5.5F);
      int var18 = new Color(235, 235, 235, var60).getRGB();
      float var57 = var1 + this.getWidth() - 8.0F - var17;
      float var58 = var7 + var59 + 4.0F;
      if (var57 < var58) {
         var57 = var58;
      }
      Fonts.BOLD.draw(var15, var57 + 0.5F, var8 + 0.5F, 5.5F, var61);
      Fonts.BOLD.draw(var15, var57, var8, 5.5F, var18);
      float var19;
      if (var12) {
         var19 = 1.0F;
      } else {
         var19 = var9 / var10;
      }

      this.healthAnimation = this.lerp(this.healthAnimation, var19, var4, 3.0F);
      if (var19 > this.trailAnimation) {
         this.trailAnimation = var19;
      }

      this.trailAnimation = this.lerp(this.trailAnimation, var19, var4, 3.5F);
      float var20;
      if (var12) {
         var20 = 0.0F;
      } else {
         var20 = var11 / var10;
      }

      this.absorptionAnimation = this.lerp(this.absorptionAnimation, var20, var4, 3.0F);
      float var21 = var7;
      float var22 = var8 + 12.0F;
      float var23 = 64.0F;
      float var24 = 4.0F;
      float var25 = 2.0F;
      Render2D.rect(var21, var22, var23, var24, new Color(30, 30, 30, (int)(200.0F * var3)).getRGB(), var25);
      float var26 = Math.max(0.0F, Math.min(1.0F, this.healthAnimation));
      float var27 = Math.max(0.0F, Math.min(1.0F, this.trailAnimation));
      if (var27 > var26) {
         int var28 = new Color(55, 55, 55, (int)(160.0F * var3)).getRGB();
         Render2D.rect(var21, var22, var23 * var27, var24, var28, var25);
      }

      if (var26 > 0.01F) {
         long var40 = System.currentTimeMillis() - this.startTime;
         float var30 = 1500.0F;
         float var31 = (float)(var40 % (long)var30) / var30 * (float) Math.PI * 2.0F;
         float var48 = Math.max(0.0F, Math.min(1.0F, var26));
         float var49;
         float var50;
         float var51;
         if (var48 >= 0.5F) {
            float var52 = (var48 - 0.5F) / 0.5F;
            var49 = 255.0F - 170.0F * var52;
            var50 = 170.0F + 85.0F * var52;
            var51 = 85.0F * var52;
         } else {
            float var53 = var48 / 0.5F;
            var49 = 255.0F;
            var50 = 85.0F * var53;
            var51 = 0.0F;
         }

         int[] var32 = new int[4];

         for (int var33 = 0; var33 < 2; var33++) {
            float var34 = (float)Math.sin(var31 - var33 * 1.5F);
            float var35 = 0.75F + 0.25F * (var34 + 1.0F) / 2.0F;
            int var54 = (int)Math.max(0.0F, Math.min(255.0F, var49 * var35));
            int var55 = (int)Math.max(0.0F, Math.min(255.0F, var50 * var35));
            int var56 = (int)Math.max(0.0F, Math.min(255.0F, var51 * var35));
            var32[var33 * 2] = new Color(var54, var55, var56, (int)(255.0F * var3)).getRGB();
            var32[var33 * 2 + 1] = new Color(var54, var55, var56, (int)(255.0F * var3)).getRGB();
         }

         Render2D.gradientRect(var21, var22, var23 * var26, var24, var32, var25);
      }

      float var41 = Math.max(0.0F, Math.min(1.0F, this.absorptionAnimation));
      if (var41 > 0.01F && !Network.isFunTime()) {
         long var29 = System.currentTimeMillis() - this.startTime;
         float var42 = 1200.0F;
         float var43 = (float)(var29 % (long)var42) / var42 * (float) Math.PI * 2.0F;
         int[] var44 = new int[4];

         for (int var45 = 0; var45 < 2; var45++) {
            float var46 = (float)Math.sin(var43 - var45 * 1.5F);
            float var47 = (var46 + 1.0F) / 2.0F;
            short var37 = 255;
            int var38 = (int)(165.0F + 50.0F * var47);
            byte var39 = 0;
            var44[var45 * 2] = new Color(var37, var38, var39, (int)(200.0F * var3)).getRGB();
            var44[var45 * 2 + 1] = new Color(var37, var38, var39, (int)(200.0F * var3)).getRGB();
         }

         Render2D.gradientRect(var21, var22, var23 * var41, var24, var44, var25);
      }
   }

   private boolean hasArmor(LivingEntity var1) {
      return !var1.getEquippedStack(EquipmentSlot.HEAD).isEmpty()
         || !var1.getEquippedStack(EquipmentSlot.CHEST).isEmpty()
         || !var1.getEquippedStack(EquipmentSlot.LEGS).isEmpty()
         || !var1.getEquippedStack(EquipmentSlot.FEET).isEmpty();
   }
}
