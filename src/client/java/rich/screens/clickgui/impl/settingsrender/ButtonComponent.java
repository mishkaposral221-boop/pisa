package rich.screens.clickgui.impl.settingsrender;

import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import rich.modules.module.setting.implement.ButtonSetting;
import rich.util.interfaces.AbstractSettingComponent;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;

public class ButtonComponent extends AbstractSettingComponent {
   private final ButtonSetting buttonSetting;
   private float pressAnimation = 0.0F;
   private float hoverAnimation = 0.0F;
   private float scaleAnimation = 1.0F;
   private float rippleAnimation = 0.0F;
   private float rippleX = 0.0F;
   private float rippleY = 0.0F;
   private boolean wasPressed = false;
   private boolean rippleActive = false;
   private long lastUpdateTime = System.currentTimeMillis();
   private static final float ANIMATION_SPEED = 8.0F;
   private static final float FAST_ANIMATION_SPEED = 12.0F;
   private static final float BUTTON_WIDTH = 65.0F;
   private static final float BUTTON_HEIGHT = 12.0F;

   public ButtonComponent(ButtonSetting var1) {
      super(var1);
      this.buttonSetting = var1;
   }

   private float getDeltaTime() {
      long var1 = System.currentTimeMillis();
      float var3 = Math.min((float)(var1 - this.lastUpdateTime) / 1000.0F, 0.1F);
      this.lastUpdateTime = var1;
      return var3;
   }

   private float lerp(float var1, float var2, float var3) {
      float var4 = var2 - var1;
      return Math.abs(var4) < 0.001F ? var2 : var1 + var4 * Math.min(var3, 1.0F);
   }

   private int clamp(int var1) {
      return Math.max(0, Math.min(255, var1));
   }

   @Override
   public void render(DrawContext var1, int var2, int var3, float var4) {
      float var5 = this.getDeltaTime();
      boolean var6 = this.isButtonHover(var2, var3);
      this.hoverAnimation = this.lerp(this.hoverAnimation, var6 ? 1.0F : 0.0F, var5 * 8.0F);
      float var7 = this.wasPressed ? 0.95F : (var6 ? 1.02F : 1.0F);
      this.scaleAnimation = this.lerp(this.scaleAnimation, var7, var5 * 12.0F);
      this.pressAnimation = this.lerp(this.pressAnimation, this.wasPressed ? 1.0F : 0.0F, var5 * 12.0F);
      if (this.rippleActive) {
         this.rippleAnimation += var5 * 3.0F;
         if (this.rippleAnimation >= 1.0F) {
            this.rippleAnimation = 0.0F;
            this.rippleActive = false;
         }
      }

      if (this.pressAnimation < 0.05F && this.wasPressed) {
         this.wasPressed = false;
      }

      int var8 = (int)(200.0F * this.alphaMultiplier);
      Fonts.GUI_ICONS.draw("U", this.x + 0.5F, this.y + this.height / 2.0F - 12.0F, 13.0F, new Color(210, 210, 210, var8).getRGB());
      Fonts.BOLD
         .draw(this.buttonSetting.getName(), this.x + 9.5F, this.y + this.height / 2.0F - 7.5F, 6.0F, this.applyAlpha(new Color(210, 210, 220, 200)).getRGB());
      String var9 = this.buttonSetting.getDescription();
      if (var9 != null && !var9.isEmpty()) {
         Fonts.BOLD.draw(var9, this.x + 0.5F, this.y + this.height / 2.0F + 0.5F, 5.0F, this.applyAlpha(new Color(128, 128, 128, 128)).getRGB());
      }

      this.renderButton(var2, var3);
   }

   private void renderButton(int var1, int var2) {
      float var3 = this.x + this.width - 65.0F - 2.0F;
      float var4 = this.y + this.height / 2.0F - 6.0F;
      float var5 = 65.0F * this.scaleAnimation;
      float var6 = 12.0F * this.scaleAnimation;
      float var7 = var3 - (var5 - 65.0F) / 2.0F;
      float var8 = var4 - (var6 - 12.0F) / 2.0F;
      float var9 = this.pressAnimation * 1.0F;
      var8 += var9;
      int var10 = this.clamp((int)((30.0F + this.hoverAnimation * 20.0F + this.pressAnimation * 15.0F) * this.alphaMultiplier));
      int var11 = this.clamp((int)(35.0F + this.hoverAnimation * 15.0F + this.pressAnimation * 20.0F));
      Color var12 = new Color(var11, var11, var11, var10);
      Render2D.rect(var7, var8, var5, var6, var12.getRGB(), 4.0F);
      if (this.rippleActive && this.rippleAnimation > 0.0F) {
         float var13 = 20.0F * this.rippleAnimation;
         float var14 = (1.0F - this.rippleAnimation) * 0.4F;
         int var15 = this.clamp((int)(255.0F * var14 * this.alphaMultiplier));
         float var16 = this.rippleX - var7;
         float var17 = this.rippleY - var8;
         Render2D.rect(var7 + var16 - var13 / 2.0F, var8 + var17 - var13 / 2.0F, var13, var13, new Color(200, 200, 210, var15).getRGB(), var13 / 2.0F);
      }

      int var19 = this.clamp((int)((60.0F + this.hoverAnimation * 60.0F + this.pressAnimation * 40.0F) * this.alphaMultiplier));
      int var20 = this.clamp((int)(80.0F + this.hoverAnimation * 40.0F + this.pressAnimation * 30.0F));
      Color var21 = new Color(var20, var20, var20, var19);
      Render2D.outline(var7, var8, var5, var6, 0.5F, var21.getRGB(), 4.0F);
      this.renderButtonContent(var7, var8, var5, var6);
   }

   private void renderButtonContent(float var1, float var2, float var3, float var4) {
      String var5 = this.buttonSetting.getButtonName() != null ? this.buttonSetting.getButtonName() : "Run";
      float var6 = 4.0F;
      float var7 = Fonts.BOLD.getWidth(var5, 5.0F);
      float var8 = var6 + 4.0F + var7;
      float var9 = var1 + (var3 - var8) / 2.0F;
      float var10 = var9;
      float var11 = var2 + var4 / 2.0F - var6 / 2.0F;
      this.renderPlayIcon(var10 - 5.0F, var11, var6);
      float var12 = var9 + var6;
      float var13 = var2 + var4 / 2.0F - 3.0F;
      int var14 = this.clamp((int)((180.0F + this.hoverAnimation * 50.0F + this.pressAnimation * 25.0F) * this.alphaMultiplier));
      int var15 = this.clamp((int)(180.0F + this.hoverAnimation * 40.0F + this.pressAnimation * 30.0F));
      Color var16 = new Color(var15, var15, var15, var14);
      Fonts.BOLD.draw(var5, var12, var13, 5.0F, var16.getRGB());
   }

   private void renderPlayIcon(float var1, float var2, float var3) {
      int var4 = this.clamp((int)((160.0F + this.hoverAnimation * 60.0F + this.pressAnimation * 35.0F) * this.alphaMultiplier));
      int var5 = this.clamp((int)(170.0F + this.hoverAnimation * 50.0F + this.pressAnimation * 30.0F));
      Color var6 = new Color(var5, var5, var5, var4);
      float var7 = var3 * 0.8F;
      float var8 = var3;
      Render2D.rect(var1, var2, var7 * 0.4F, var8, var6.getRGB(), 1.0F);
      float var9 = var3 * 0.35F;
      float var10 = var1 + var7 * 0.5F;
      float var11 = var2 + (var8 - var9) / 2.0F;
      Render2D.rect(var10, var11, var9, var9, var6.getRGB(), var9 / 2.0F);
   }

   private boolean isButtonHover(double var1, double var3) {
      float var5 = this.x + this.width - 65.0F - 2.0F;
      float var6 = this.y + this.height / 2.0F - 6.0F;
      return var1 >= var5 && var1 <= var5 + 65.0F && var3 >= var6 && var3 <= var6 + 12.0F;
   }

   @Override
   public boolean mouseClicked(double var1, double var3, int var5) {
      if (this.isButtonHover(var1, var3) && var5 == 0) {
         if (this.buttonSetting.getRunnable() != null) {
            this.buttonSetting.getRunnable().run();
         }

         this.wasPressed = true;
         this.pressAnimation = 1.0F;
         this.rippleActive = true;
         this.rippleAnimation = 0.0F;
         this.rippleX = (float)var1;
         this.rippleY = (float)var3;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void tick() {
   }

   @Override
   public boolean isHover(double var1, double var3) {
      return var1 >= this.x && var1 <= this.x + this.width && var3 >= this.y && var3 <= this.y + this.height;
   }
}
