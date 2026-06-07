package rich.screens.clickgui.impl.settingsrender;

import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.interfaces.AbstractSettingComponent;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;

public class SliderComponent extends AbstractSettingComponent {
   private final SliderSettings sliderSettings;
   private boolean dragging = false;
   private float animatedPercentage = 0.0F;
   private float knobAnimation = 0.0F;
   private boolean inputMode = false;
   private String inputText = "";
   private int cursorPosition = 0;
   private float inputAnimation = 0.0F;
   private float hoverAnimation = 0.0F;
   private float unitsAlpha = 1.0F;
   private float valueOffsetX = 0.0F;
   private float backgroundAlpha = 0.0F;
   private long lastUpdateTime = System.currentTimeMillis();
   private static final float ANIMATION_SPEED = 8.0F;
   private static final float FAST_ANIMATION_SPEED = 12.0F;

   public SliderComponent(SliderSettings var1) {
      super(var1);
      this.sliderSettings = var1;
      float var2 = this.sliderSettings.getMax() - this.sliderSettings.getMin();
      if (var2 > 0.0F) {
         this.animatedPercentage = (this.sliderSettings.getValue() - this.sliderSettings.getMin()) / var2;
      }
   }

   private int clampAlpha(float var1) {
      return Math.max(0, Math.min(255, (int)var1));
   }

   @Override
   public void render(DrawContext var1, int var2, int var3, float var4) {
      if (this.dragging) {
         this.updateValue(var2);
      }

      float var5 = this.getDeltaTime();
      this.updateAnimations(var2, var3, var5);
      float var6 = this.sliderSettings.getMax() - this.sliderSettings.getMin();
      float var7 = var6 > 0.0F ? (this.sliderSettings.getValue() - this.sliderSettings.getMin()) / var6 : 0.0F;
      this.animatedPercentage = this.animatedPercentage + (var7 - this.animatedPercentage) * 0.25F;
      float var8 = this.dragging ? 1.0F : 0.0F;
      this.knobAnimation = this.knobAnimation + (var8 - this.knobAnimation) * 0.25F;
      this.knobAnimation = Math.max(0.0F, Math.min(1.0F, this.knobAnimation));
      int var9 = (int)(200.0F * this.alphaMultiplier);
      Fonts.GUI_ICONS.draw("H", this.x - 0.5F, this.y + 0.5F, 9.0F, new Color(210, 210, 210, var9).getRGB());
      Fonts.BOLD.draw(this.sliderSettings.getName(), this.x + 9.5F, this.y + 1.0F, 6.0F, this.applyAlpha(new Color(210, 210, 220, 200)).getRGB());
      this.renderValueInput(var2, var3);
      this.renderSlider();
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

   private void updateAnimations(int var1, int var2, float var3) {
      float var4 = this.inputMode ? 1.0F : 0.0F;
      this.inputAnimation = this.lerp(this.inputAnimation, var4, var3 * 12.0F);
      boolean var5 = this.isValueHover(var1, var2) && !this.inputMode;
      float var6 = var5 ? 1.0F : 0.0F;
      this.hoverAnimation = this.lerp(this.hoverAnimation, var6, var3 * 8.0F);
      float var7 = this.inputMode ? 0.0F : 1.0F;
      this.unitsAlpha = this.lerp(this.unitsAlpha, var7, var3 * 8.0F);
      float var8 = this.inputMode ? 1.0F : 0.0F;
      this.valueOffsetX = this.lerp(this.valueOffsetX, var8, var3 * 8.0F);
      float var9 = this.inputMode ? 1.0F : 0.0F;
      this.backgroundAlpha = this.lerp(this.backgroundAlpha, var9, var3 * 8.0F);
   }

   private void renderValueInput(int var1, int var2) {
      String var3 = this.sliderSettings.isInteger()
         ? String.valueOf((int)this.sliderSettings.getValue())
         : String.format("%.1f", this.sliderSettings.getValue());
      String var4 = " units";
      String var5 = var3 + var4;
      float var6 = Fonts.BOLD.getWidth(var5, 5.0F);
      float var7 = Fonts.BOLD.getWidth(var3, 5.0F);
      float var8 = Fonts.BOLD.getWidth(var4, 5.0F);
      float var9 = this.x + this.width - var6 - 4.0F;
      float var10 = this.y + 2.0F;
      float var11 = var8 / 2.0F * this.valueOffsetX;
      float var12 = var9 + var11;
      float var13 = var9 - 3.0F;
      float var14 = var10 - 1.0F;
      float var15 = var6 + 6.0F;
      float var16 = 8.0F;
      if (this.backgroundAlpha > 0.01F) {
         int var17 = this.clampAlpha(200.0F * this.backgroundAlpha * this.alphaMultiplier);
         Render2D.rect(var13, var14, var15, var16, new Color(40, 40, 45, var17).getRGB(), 2.0F);
      }

      float var27 = Math.max(this.hoverAnimation * 0.4F, this.inputAnimation);
      if (var27 > 0.01F) {
         int var18 = this.clampAlpha(180.0F * var27 * this.alphaMultiplier);
         Render2D.outline(var13, var14, var15, var16, 0.1F, new Color(180, 180, 180, var18).getRGB(), 2.0F);
      }

      if (this.inputMode && this.inputAnimation > 0.5F) {
         String var29 = this.inputText;
         float var30 = Fonts.BOLD.getWidth(var29, 5.0F);
         float var31 = var13 + (var15 - var30) / 2.0F;
         int var21 = this.clampAlpha(220.0F * Math.min(1.0F, (this.inputAnimation - 0.5F) * 2.0F) * this.alphaMultiplier);
         Fonts.BOLD.draw(var29, var31, var10, 5.0F, new Color(230, 230, 235, var21).getRGB());
         long var22 = System.currentTimeMillis();
         if (var22 % 1000L < 500L) {
            String var24 = this.inputText.substring(0, this.cursorPosition);
            float var25 = var31 + Fonts.BOLD.getWidth(var24, 5.0F);
            int var26 = this.clampAlpha(255.0F * this.inputAnimation * this.alphaMultiplier);
            Render2D.rect(var25, var14 + 2.0F, 0.5F, var16 - 4.0F, new Color(180, 180, 180, var26).getRGB(), 0.0F);
         }
      } else {
         float var28 = 1.0F - this.inputAnimation * 0.5F;
         int var19 = this.clampAlpha(160.0F * var28 * this.alphaMultiplier);
         if (var19 > 0) {
            Fonts.BOLD.draw(var3, var12, var10, 5.0F, new Color(100, 100, 105, var19).getRGB());
         }

         if (this.unitsAlpha > 0.01F) {
            int var20 = this.clampAlpha(160.0F * this.unitsAlpha * this.alphaMultiplier);
            if (var20 > 0) {
               Fonts.BOLD.draw(var4, var12 + var7, var10, 5.0F, new Color(100, 100, 105, var20).getRGB());
            }
         }
      }
   }

   private void renderSlider() {
      float var1 = this.y + 11.0F;
      float var2 = 2.5F;
      float var3 = 1.0F;
      float var4 = this.width - 2.0F;
      Render2D.rect(this.x + var3, var1, var4, var2, this.applyAlpha(new Color(60, 60, 65, 220)).getRGB(), 2.0F);
      float var5 = var4 * this.animatedPercentage;
      if (var5 > 0.0F) {
         Render2D.rect(this.x + var3, var1, var5, var2, this.applyAlpha(new Color(130, 130, 135, 230)).getRGB(), 2.0F);
      }

      float var6 = 5.0F;
      float var7 = var6 + this.knobAnimation * 1.0F;
      float var8 = this.x + var3 + var4 * this.animatedPercentage - var7 / 2.0F;
      float var9 = var1 + var2 / 2.0F - var7 / 2.0F;
      var8 = Math.max(this.x + var3 - var7 / 2.0F, Math.min(var8, this.x + var3 + var4 - var7 / 2.0F));
      Render2D.rect(var8, var9, var7, var7, this.applyAlpha(new Color(180, 180, 185, 255)).getRGB(), var7 / 2.0F);
   }

   private boolean isValueHover(double var1, double var3) {
      String var5 = this.sliderSettings.isInteger()
         ? String.valueOf((int)this.sliderSettings.getValue())
         : String.format("%.1f", this.sliderSettings.getValue());
      String var6 = var5 + " units";
      float var7 = Fonts.BOLD.getWidth(var6, 5.0F);
      float var8 = this.x + this.width - var7 - 7.0F;
      float var9 = this.y;
      return var1 >= var8 && var1 <= var8 + var7 + 10.0F && var3 >= var9 && var3 <= var9 + 10.0F;
   }

   @Override
   public boolean mouseClicked(double var1, double var3, int var5) {
      if (var5 == 0) {
         if (this.isValueHover(var1, var3) && !this.inputMode) {
            this.inputMode = true;
            String var6 = this.sliderSettings.isInteger()
               ? String.valueOf((int)this.sliderSettings.getValue())
               : String.format("%.1f", this.sliderSettings.getValue());
            this.inputText = var6;
            this.cursorPosition = this.inputText.length();
            return true;
         }

         if (this.inputMode && !this.isValueHover(var1, var3)) {
            this.applyInputValue();
            this.inputMode = false;
            this.inputText = "";
            return true;
         }

         if (this.isSliderHover(var1, var3) && !this.inputMode) {
            this.dragging = true;
            this.updateValue(var1);
            return true;
         }
      }

      return false;
   }

   private boolean isSliderHover(double var1, double var3) {
      float var5 = this.y + 6.0F;
      float var6 = 12.0F;
      return var1 >= this.x && var1 <= this.x + this.width && var3 >= var5 && var3 <= var5 + var6;
   }

   @Override
   public boolean mouseReleased(double var1, double var3, int var5) {
      if (var5 == 0 && this.dragging) {
         this.dragging = false;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseDragged(double var1, double var3, int var5, double var6, double var8) {
      if (this.dragging && var5 == 0) {
         this.updateValue(var1);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean keyPressed(int var1, int var2, int var3) {
      if (!this.inputMode) {
         return false;
      }

      switch (var1) {
         case 256:
            this.inputMode = false;
            this.inputText = "";
            return true;
         case 257:
         case 335:
            this.applyInputValue();
            this.inputMode = false;
            this.inputText = "";
            return true;
         case 259:
            if (this.cursorPosition > 0) {
               this.inputText = this.inputText.substring(0, this.cursorPosition - 1) + this.inputText.substring(this.cursorPosition);
               this.cursorPosition--;
            }

            return true;
         case 261:
            if (this.cursorPosition < this.inputText.length()) {
               this.inputText = this.inputText.substring(0, this.cursorPosition) + this.inputText.substring(this.cursorPosition + 1);
            }

            return true;
         case 262:
            if (this.cursorPosition < this.inputText.length()) {
               this.cursorPosition++;
            }

            return true;
         case 263:
            if (this.cursorPosition > 0) {
               this.cursorPosition--;
            }

            return true;
         case 268:
            this.cursorPosition = 0;
            return true;
         case 269:
            this.cursorPosition = this.inputText.length();
            return true;
         default:
            return false;
      }
   }

   @Override
   public boolean charTyped(char var1, int var2) {
      if (!this.inputMode) {
         return false;
      }

      if (this.isValidInputChar(var1)) {
         String var3 = this.inputText.substring(0, this.cursorPosition) + var1 + this.inputText.substring(this.cursorPosition);
         if (this.isValidInputFormat(var3)) {
            this.inputText = var3;
            this.cursorPosition++;
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean isValidInputChar(char var1) {
      return Character.isDigit(var1) || var1 == '.' || var1 == '-';
   }

   private boolean isValidInputFormat(String var1) {
      if (!var1.isEmpty() && !var1.equals("-") && !var1.equals(".") && !var1.equals("-.")) {
         int var2 = 0;
         int var3 = 0;
         int var4 = 0;
         boolean var5 = false;

         for (int var6 = 0; var6 < var1.length(); var6++) {
            char var7 = var1.charAt(var6);
            if (var7 == '-') {
               if (var6 != 0) {
                  return false;
               }

               if (++var3 > 1) {
                  return false;
               }
            } else if (var7 == '.') {
               if (this.sliderSettings.isInteger()) {
                  return false;
               }

               if (++var2 > 1) {
                  return false;
               }

               var5 = true;
            } else {
               if (!Character.isDigit(var7)) {
                  return false;
               }

               if (var5) {
                  if (++var4 > 1) {
                     return false;
                  }
               }
            }
         }

         return true;
      } else {
         return true;
      }
   }

   private void applyInputValue() {
      if (!this.inputText.isEmpty() && !this.inputText.equals("-") && !this.inputText.equals(".") && !this.inputText.equals("-.")) {
         try {
            float var1;
            if (this.sliderSettings.isInteger()) {
               var1 = Integer.parseInt(this.inputText);
            } else {
               var1 = Float.parseFloat(this.inputText);
            }

            var1 = Math.max(this.sliderSettings.getMin(), Math.min(this.sliderSettings.getMax(), var1));
            if (this.sliderSettings.isInteger()) {
               var1 = Math.round(var1);
            }

            this.sliderSettings.setValue(var1);
         } catch (NumberFormatException var2) {
         }
      }
   }

   private void updateValue(double var1) {
      float var3 = 1.0F;
      float var4 = this.width - 2.0F;
      float var5 = (float)((var1 - this.x - var3) / var4);
      var5 = Math.max(0.0F, Math.min(1.0F, var5));
      float var6 = this.sliderSettings.getMax() - this.sliderSettings.getMin();
      float var7 = this.sliderSettings.getMin() + var6 * var5;
      if (this.sliderSettings.isInteger()) {
         var7 = Math.round(var7);
      }

      this.sliderSettings.setValue(var7);
   }

   @Override
   public void tick() {
   }

   @Override
   public boolean isHover(double var1, double var3) {
      return var1 >= this.x && var1 <= this.x + this.width && var3 >= this.y && var3 <= this.y + this.height;
   }

   public boolean isInputMode() {
      return this.inputMode;
   }
}
