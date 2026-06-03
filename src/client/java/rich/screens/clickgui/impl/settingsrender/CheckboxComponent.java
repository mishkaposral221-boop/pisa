package rich.screens.clickgui.impl.settingsrender;

import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.util.interfaces.AbstractSettingComponent;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;

public class CheckboxComponent extends AbstractSettingComponent {
   private final BooleanSetting booleanSetting;
   private float checkAnimation = 0.0F;
   private float hoverAnimation = 0.0F;
   private float stretchAnimation = 0.0F;
   private float velocity = 0.0F;

   public CheckboxComponent(BooleanSetting var1) {
      super(var1);
      this.booleanSetting = var1;
      this.checkAnimation = var1.isValue() ? 1.0F : 0.0F;
   }

   @Override
   public void render(DrawContext var1, int var2, int var3, float var4) {
      boolean var5 = this.isHover(var2, var3);
      float var6 = var5 ? 1.0F : 0.0F;
      this.hoverAnimation = this.hoverAnimation + (var6 - this.hoverAnimation) * 0.2F;
      this.hoverAnimation = this.clamp(this.hoverAnimation, 0.0F, 1.0F);
      float var7 = this.booleanSetting.isValue() ? 1.0F : 0.0F;
      float var8 = this.checkAnimation;
      float var9 = 0.35F;
      this.checkAnimation = this.checkAnimation + (var7 - this.checkAnimation) * var9;
      if (Math.abs(var7 - this.checkAnimation) < 0.001F) {
         this.checkAnimation = var7;
      }

      this.velocity = this.checkAnimation - var8;
      float var10 = Math.abs(this.velocity);
      float var11 = var10 * 30.0F;
      var11 = this.clamp(var11, 0.0F, 1.0F);
      float var12 = var11 > this.stretchAnimation ? 0.5F : 0.2F;
      this.stretchAnimation = this.stretchAnimation + (var11 - this.stretchAnimation) * var12;
      this.stretchAnimation = this.clamp(this.stretchAnimation, 0.0F, 1.0F);
      int var13 = (int)(200.0F * this.alphaMultiplier);
      Fonts.GUI_ICONS.draw("T", this.x + 0.5F, this.y + this.height / 2.0F - 11.0F, 11.0F, new Color(210, 210, 210, var13).getRGB());
      Fonts.BOLD
         .draw(this.booleanSetting.getName(), this.x + 9.5F, this.y + this.height / 2.0F - 7.5F, 6.0F, this.applyAlpha(new Color(210, 210, 220, 200)).getRGB());
      Fonts.BOLD
         .draw(
            this.booleanSetting.getDescription(),
            this.x + 0.5F,
            this.y + this.height / 2.0F + 0.5F,
            5.0F,
            this.applyAlpha(new Color(128, 128, 128, 128)).getRGB()
         );
      float var14 = 10.0F;
      float var15 = var14 + 6.0F;
      float var16 = this.x + this.width - var15 - 2.0F;
      float var17 = this.y + this.height / 2.0F - var14 / 2.0F;
      Render2D.rect(var16, var17, var15, var14, this.applyAlpha(new Color(55, 55, 55, 25)).getRGB(), 4.0F);
      int var18 = 60 + (int)(this.hoverAnimation * 40.0F);
      Render2D.outline(var16, var17, var15, var14, 0.5F, this.applyAlpha(new Color(155, 155, 155, var18)).getRGB(), 4.0F);
      float var19 = var14 - 3.0F;
      float var20 = 4.0F;
      float var21 = this.stretchAnimation * var20;
      float var22 = var19 + var21;
      float var23 = var19 - this.stretchAnimation * 1.0F;
      float var24 = 1.5F;
      float var25 = var15 - var19 - var24 * 2.0F;
      float var26 = var16 + var24;
      float var27;
      if (this.velocity > 0.0F) {
         var27 = -var21 * 0.3F;
      } else if (this.velocity < 0.0F) {
         var27 = var21 * 0.3F;
      } else {
         var27 = 0.0F;
      }

      float var28 = var26 + var25 * this.checkAnimation - var21 * this.checkAnimation + var27;
      float var29 = var17 + (var14 - var23) / 2.0F;
      Color var30 = new Color(59, 59, 59, 200);
      Color var31 = new Color(159, 159, 159, 200);
      Color var32 = this.lerpColor(var30, var31, this.checkAnimation);
      Render2D.rect(var28, var29, var22, var23, this.applyAlpha(var32).getRGB(), 4.0F);
   }

   private Color lerpColor(Color var1, Color var2, float var3) {
      int var4 = (int)(var1.getRed() + (var2.getRed() - var1.getRed()) * var3);
      int var5 = (int)(var1.getGreen() + (var2.getGreen() - var1.getGreen()) * var3);
      int var6 = (int)(var1.getBlue() + (var2.getBlue() - var1.getBlue()) * var3);
      int var7 = (int)(var1.getAlpha() + (var2.getAlpha() - var1.getAlpha()) * var3);
      return new Color(this.clamp(var4, 0, 255), this.clamp(var5, 0, 255), this.clamp(var6, 0, 255), this.clamp(var7, 0, 255));
   }

   private float clamp(float var1, float var2, float var3) {
      return Math.max(var2, Math.min(var3, var1));
   }

   private int clamp(int var1, int var2, int var3) {
      return Math.max(var2, Math.min(var3, var1));
   }

   @Override
   public boolean mouseClicked(double var1, double var3, int var5) {
      if (this.isHover(var1, var3) && var5 == 0) {
         this.booleanSetting.setValue(!this.booleanSetting.isValue());
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
