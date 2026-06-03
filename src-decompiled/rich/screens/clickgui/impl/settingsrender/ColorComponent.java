package rich.screens.clickgui.impl.settingsrender;

import java.awt.Color;
import net.minecraft.class_332;
import org.lwjgl.glfw.GLFW;
import rich.modules.module.setting.implement.ColorSetting;
import rich.util.interfaces.AbstractSettingComponent;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.shader.Scissor;

public class ColorComponent extends AbstractSettingComponent {
   private final ColorSetting colorSetting;
   private boolean expanded = false;
   private float expandAnimation = 0.0F;
   private float hoverAnimation = 0.0F;
   private float previewHoverAnimation = 0.0F;
   private float contentAlpha = 0.0F;
   private boolean draggingPalette = false;
   private boolean draggingHue = false;
   private boolean draggingAlpha = false;
   private float paletteHandleAnimation = 0.0F;
   private float hueHandleAnimation = 0.0F;
   private float alphaHandleAnimation = 0.0F;
   private boolean hexInputActive = false;
   private String hexInputText = "";
   private int hexCursorPosition = 0;
   private int hexSelectionStart = -1;
   private int hexSelectionEnd = -1;
   private float hexInputAnimation = 0.0F;
   private float hexSelectionAnimation = 0.0F;
   private float hexCursorBlinkAnimation = 0.0F;
   private float displayHue;
   private float displaySaturation;
   private float displayBrightness;
   private float displayAlpha;
   private boolean colorInitialized = false;
   private long lastUpdateTime = System.currentTimeMillis();
   private static final float ANIMATION_SPEED = 8.0F;
   private static final float FAST_ANIMATION_SPEED = 15.0F;
   private static final float COLOR_TRANSITION_SPEED = 6.0F;
   private static final float CONTENT_FADE_SPEED = 15.0F;
   private static final float PALETTE_SIZE = 70.0F;
   private static final float SLIDER_WIDTH = 8.0F;
   private static final float SPACING = 4.0F;
   private static final float PREVIEW_SIZE = 12.0F;

   public ColorComponent(ColorSetting var1) {
      super(var1);
      this.colorSetting = var1;
      this.updateHexFromColor();
      this.displayHue = var1.getHue();
      this.displaySaturation = var1.getSaturation();
      this.displayBrightness = var1.getBrightness();
      this.displayAlpha = var1.getAlpha();
      this.colorInitialized = true;
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

   private float lerpHue(float var1, float var2, float var3) {
      float var4 = var2 - var1;
      if (var4 > 0.5F) {
         var4--;
      } else if (var4 < -0.5F) {
         var4++;
      }

      if (Math.abs(var4) < 0.001F) {
         return var2;
      }

      float var5 = var1 + var4 * Math.min(var3, 1.0F);
      if (var5 < 0.0F) {
         var5++;
      }

      if (var5 > 1.0F) {
         var5--;
      }

      return var5;
   }

   private int clamp(int var1) {
      return Math.max(0, Math.min(255, var1));
   }

   private void updateDisplayColors(float var1) {
      if (!this.colorInitialized) {
         this.displayHue = this.colorSetting.getHue();
         this.displaySaturation = this.colorSetting.getSaturation();
         this.displayBrightness = this.colorSetting.getBrightness();
         this.displayAlpha = this.colorSetting.getAlpha();
         this.colorInitialized = true;
      } else {
         float var2 = var1 * 6.0F;
         if (!this.draggingPalette && !this.draggingHue && !this.draggingAlpha) {
            this.displayHue = this.lerpHue(this.displayHue, this.colorSetting.getHue(), var2);
            this.displaySaturation = this.lerp(this.displaySaturation, this.colorSetting.getSaturation(), var2);
            this.displayBrightness = this.lerp(this.displayBrightness, this.colorSetting.getBrightness(), var2);
            this.displayAlpha = this.lerp(this.displayAlpha, this.colorSetting.getAlpha(), var2);
         } else {
            this.displayHue = this.colorSetting.getHue();
            this.displaySaturation = this.colorSetting.getSaturation();
            this.displayBrightness = this.colorSetting.getBrightness();
            this.displayAlpha = this.colorSetting.getAlpha();
         }
      }
   }

   private int getDisplayColor() {
      int var1 = Color.HSBtoRGB(this.displayHue, this.displaySaturation, this.displayBrightness);
      int var2 = Math.round(this.displayAlpha * 255.0F);
      return var2 << 24 | var1 & 16777215;
   }

   private int getDisplayColorNoAlpha() {
      return Color.HSBtoRGB(this.displayHue, this.displaySaturation, this.displayBrightness) | 0xFF000000;
   }

   private Color applyContentAlpha(Color var1) {
      int var2 = Math.max(0, Math.min(255, (int)(var1.getAlpha() * this.alphaMultiplier * this.contentAlpha)));
      return new Color(var1.getRed(), var1.getGreen(), var1.getBlue(), var2);
   }

   private int applyContentAlpha(int var1) {
      int var2 = var1 >> 24 & 0xFF;
      int var3 = var1 >> 16 & 0xFF;
      int var4 = var1 >> 8 & 0xFF;
      int var5 = var1 & 0xFF;
      int var6 = Math.max(0, Math.min(255, (int)(var2 * this.alphaMultiplier * this.contentAlpha)));
      return var6 << 24 | var3 << 16 | var4 << 8 | var5;
   }

   private boolean isControlDown() {
      long var1 = mc.method_22683().method_4490();
      return GLFW.glfwGetKey(var1, 341) == 1 || GLFW.glfwGetKey(var1, 345) == 1;
   }

   private boolean isShiftDown() {
      long var1 = mc.method_22683().method_4490();
      return GLFW.glfwGetKey(var1, 340) == 1 || GLFW.glfwGetKey(var1, 344) == 1;
   }

   private boolean hasHexSelection() {
      return this.hexSelectionStart != -1 && this.hexSelectionEnd != -1 && this.hexSelectionStart != this.hexSelectionEnd;
   }

   private int getHexSelectionStart() {
      return Math.min(this.hexSelectionStart, this.hexSelectionEnd);
   }

   private int getHexSelectionEnd() {
      return Math.max(this.hexSelectionStart, this.hexSelectionEnd);
   }

   private String getHexSelectedText() {
      return !this.hasHexSelection() ? "" : this.hexInputText.substring(this.getHexSelectionStart(), this.getHexSelectionEnd());
   }

   private void clearHexSelection() {
      this.hexSelectionStart = -1;
      this.hexSelectionEnd = -1;
   }

   private void selectAllHexText() {
      this.hexSelectionStart = 0;
      this.hexSelectionEnd = this.hexInputText.length();
      this.hexCursorPosition = this.hexInputText.length();
   }

   private void deleteHexSelectedText() {
      if (this.hasHexSelection()) {
         int var1 = this.getHexSelectionStart();
         int var2 = this.getHexSelectionEnd();
         this.hexInputText = this.hexInputText.substring(0, var1) + this.hexInputText.substring(var2);
         this.hexCursorPosition = var1;
         this.clearHexSelection();
      }
   }

   private void pasteHexFromClipboard() {
      String var1 = GLFW.glfwGetClipboardString(mc.method_22683().method_4490());
      if (var1 != null && !var1.isEmpty()) {
         var1 = var1.replace("#", "").replaceAll("[^0-9A-Fa-f]", "").toUpperCase();
         if (this.hasHexSelection()) {
            this.deleteHexSelectedText();
         }

         int var2 = 8 - this.hexInputText.length();
         if (var1.length() > var2) {
            var1 = var1.substring(0, var2);
         }

         if (!var1.isEmpty()) {
            this.hexInputText = this.hexInputText.substring(0, this.hexCursorPosition) + var1 + this.hexInputText.substring(this.hexCursorPosition);
            this.hexCursorPosition = this.hexCursorPosition + var1.length();
         }
      }
   }

   private void copyHexToClipboard() {
      if (this.hasHexSelection()) {
         GLFW.glfwSetClipboardString(mc.method_22683().method_4490(), "#" + this.getHexSelectedText());
      } else if (!this.hexInputText.isEmpty()) {
         GLFW.glfwSetClipboardString(mc.method_22683().method_4490(), "#" + this.hexInputText);
      }
   }

   private void moveHexCursor(int var1) {
      if (this.hasHexSelection() && !this.isShiftDown()) {
         if (var1 < 0) {
            this.hexCursorPosition = this.getHexSelectionStart();
         } else {
            this.hexCursorPosition = this.getHexSelectionEnd();
         }

         this.clearHexSelection();
      } else {
         if (var1 < 0 && this.hexCursorPosition > 0) {
            this.hexCursorPosition--;
         } else if (var1 > 0 && this.hexCursorPosition < this.hexInputText.length()) {
            this.hexCursorPosition++;
         }

         this.updateHexSelectionAfterCursorMove();
      }
   }

   private void updateHexSelectionAfterCursorMove() {
      if (this.isShiftDown()) {
         if (this.hexSelectionStart == -1) {
            this.hexSelectionStart = this.hexSelectionEnd != -1 ? this.hexSelectionEnd : this.hexCursorPosition;
         }

         this.hexSelectionEnd = this.hexCursorPosition;
      } else {
         this.clearHexSelection();
      }
   }

   @Override
   public void render(class_332 var1, int var2, int var3, float var4) {
      float var5 = this.getDeltaTime();
      this.updateDisplayColors(var5);
      if (this.draggingPalette) {
         this.updatePalette(var2, var3);
      }

      if (this.draggingHue) {
         this.updateHue(var3);
      }

      if (this.draggingAlpha) {
         this.updateAlpha(var3);
      }

      boolean var6 = this.isHover(var2, var3);
      boolean var7 = this.isPreviewHover(var2, var3);
      this.hoverAnimation = this.lerp(this.hoverAnimation, var6 ? 1.0F : 0.0F, var5 * 8.0F);
      this.previewHoverAnimation = this.lerp(this.previewHoverAnimation, var7 ? 1.0F : 0.0F, var5 * 8.0F);
      this.expandAnimation = this.lerp(this.expandAnimation, this.expanded ? 1.0F : 0.0F, var5 * 8.0F);
      this.hexInputAnimation = this.lerp(this.hexInputAnimation, this.hexInputActive ? 1.0F : 0.0F, var5 * 15.0F);
      this.hexSelectionAnimation = this.lerp(this.hexSelectionAnimation, this.hasHexSelection() ? 1.0F : 0.0F, var5 * 8.0F);
      if (this.hexInputActive) {
         this.hexCursorBlinkAnimation += var5 * 2.0F;
         if (this.hexCursorBlinkAnimation > 1.0F) {
            this.hexCursorBlinkAnimation--;
         }
      } else {
         this.hexCursorBlinkAnimation = 0.0F;
      }

      float var8 = this.expanded ? 1.0F : 0.0F;
      float var9 = this.expanded ? 15.0F : 22.5F;
      this.contentAlpha = this.lerp(this.contentAlpha, var8, var5 * var9);
      this.paletteHandleAnimation = this.lerp(this.paletteHandleAnimation, this.draggingPalette ? 1.0F : 0.0F, var5 * 15.0F);
      this.hueHandleAnimation = this.lerp(this.hueHandleAnimation, this.draggingHue ? 1.0F : 0.0F, var5 * 15.0F);
      this.alphaHandleAnimation = this.lerp(this.alphaHandleAnimation, this.draggingAlpha ? 1.0F : 0.0F, var5 * 15.0F);
      int var10 = (int)(200.0F * this.alphaMultiplier);
      Fonts.GUI_ICONS.draw("R", this.x + 0.5F, this.y + this.height / 2.0F - 11.5F, 16.0F, new Color(210, 210, 210, var10).getRGB());
      Fonts.BOLD
         .draw(this.colorSetting.getName(), this.x + 11.5F, this.y + this.height / 2.0F - 6.5F, 6.0F, this.applyAlpha(new Color(210, 210, 220, 200)).getRGB());
      String var11 = this.colorSetting.getDescription();
      if (var11 != null && !var11.isEmpty()) {
         Fonts.BOLD.draw(var11, this.x + 8.5F, this.y + this.height / 2.0F + 0.5F, 5.0F, this.applyAlpha(new Color(128, 128, 128, 128)).getRGB());
      }

      this.renderColorPreview(var2, var3);
      if (this.expandAnimation > 0.01F) {
         this.renderColorPicker(var1, var2, var3, var5);
      }
   }

   private void renderColorPreview(int var1, int var2) {
      float var3 = this.x + this.width - 14.0F;
      float var4 = this.y + this.height / 2.0F / 2.0F;
      float var5 = 1.0F + this.previewHoverAnimation * 0.1F;
      float var6 = var3 - var5 / 2.0F + 1.0F;
      float var7 = var4 - var5 / 2.0F;
      int var8 = this.getDisplayColor();
      Color var9 = new Color(var8, true);
      Render2D.rect(var6 + 0.5F, var7 + 0.5F, 9.0F, 9.0F, this.applyAlpha(var9).getRGB(), 15.0F);
      int var10 = this.clamp((int)((255.0F + this.previewHoverAnimation * 60.0F) * this.alphaMultiplier));
      Render2D.outline(var6, var7, 10.0F, 10.0F, 1.0F, new Color(125, 125, 125, var10).getRGB(), 15.0F);
   }

   private void renderColorPicker(class_332 var1, int var2, int var3, float var4) {
      float var5 = this.x;
      float var6 = this.y + this.height + 4.0F;
      float var7 = this.width;
      float var8 = 96.0F;
      float var9 = var8 * this.expandAnimation;
      int var10 = this.clamp((int)(60.0F * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
      Render2D.outline(var5, var6, var7, var9 + 2.0F, 0.5F, new Color(80, 80, 85, var10).getRGB(), 4.0F);
      if (!(this.expandAnimation < 0.3F) && !(this.contentAlpha < 0.01F)) {
         Scissor.enable(var5, var6, var7, var9, 2.0F);
         float var11 = var5 + 4.0F;
         float var12 = var6 + 4.0F;
         float var13 = var7 - 8.0F;
         float var14 = 20.0F;
         float var15 = var13 - var14 - 4.0F;
         this.renderHueSlider(var11, var12, 8.0F, 70.0F, var2, var3);
         this.renderAlphaSlider(var11 + 8.0F + 4.0F, var12, 8.0F, 70.0F, var2, var3);
         this.renderSaturationBrightnessPalette(var11 + var14 + 4.0F, var12, var15, 70.0F, var2, var3);
         var12 += 74.0F;
         this.renderHexInput(var11, var12, var13, 16.0F, var2, var3);
         Scissor.disable();
      }
   }

   private void renderSaturationBrightnessPalette(float var1, float var2, float var3, float var4, int var5, int var6) {
      int var7 = Color.HSBtoRGB(this.displayHue, 1.0F, 1.0F);
      Color var8 = new Color(var7);
      int[] var9 = new int[]{
         this.applyContentAlpha(Color.WHITE).getRGB(),
         this.applyContentAlpha(var8).getRGB(),
         this.applyContentAlpha(var8).getRGB(),
         this.applyContentAlpha(Color.WHITE).getRGB()
      };
      Render2D.gradientRect(var1, var2, var3, var4 - 0.5F, var9, 5.0F);
      int[] var10 = new int[]{
         new Color(0, 0, 0, 0).getRGB(),
         new Color(0, 0, 0, 0).getRGB(),
         this.applyContentAlpha(Color.BLACK).getRGB(),
         this.applyContentAlpha(Color.BLACK).getRGB()
      };
      Render2D.gradientRect(var1, var2, var3, var4, var10, 3.0F);
      float var11 = var1 + this.displaySaturation * var3;
      float var12 = var2 + (1.0F - this.displayBrightness) * var4;
      float var13 = 6.0F + this.paletteHandleAnimation * 2.0F;
      int var14 = this.clamp((int)(255.0F * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
      Render2D.rect(var11 - var13 / 2.0F, var12 - var13 / 2.0F, var13, var13, new Color(255, 255, 255, var14).getRGB(), var13 / 2.0F);
      int var15 = Color.HSBtoRGB(this.displayHue, this.displaySaturation, this.displayBrightness);
      Color var16 = new Color(var15);
      Render2D.rect(
         var11 - var13 / 2.0F + 1.0F, var12 - var13 / 2.0F + 1.0F, var13 - 2.0F, var13 - 2.0F, this.applyContentAlpha(var16).getRGB(), (var13 - 2.0F) / 2.0F
      );
   }

   private void renderHueSlider(float var1, float var2, float var3, float var4, int var5, int var6) {
      int[] var7 = new int[]{
         Color.HSBtoRGB(0.0F, 1.0F, 1.0F),
         Color.HSBtoRGB(0.16666667F, 1.0F, 1.0F),
         Color.HSBtoRGB(0.33333334F, 1.0F, 1.0F),
         Color.HSBtoRGB(0.5F, 1.0F, 1.0F),
         Color.HSBtoRGB(0.6666667F, 1.0F, 1.0F),
         Color.HSBtoRGB(0.8333333F, 1.0F, 1.0F),
         Color.HSBtoRGB(1.0F, 1.0F, 1.0F)
      };
      float var8 = var4 / 6.0F;
      int[] var9 = new int[]{
         this.applyContentAlpha(new Color(var7[0])).getRGB(),
         this.applyContentAlpha(new Color(var7[0])).getRGB(),
         this.applyContentAlpha(new Color(var7[1])).getRGB(),
         this.applyContentAlpha(new Color(var7[1])).getRGB()
      };
      Render2D.gradientRect(var1, var2, var3, var8, var9, 2.0F, 2.0F, 0.0F, 0.0F);

      for (int var10 = 1; var10 < 5; var10++) {
         float var11 = var2 + var10 * var8;
         int[] var12 = new int[]{
            this.applyContentAlpha(new Color(var7[var10])).getRGB(),
            this.applyContentAlpha(new Color(var7[var10])).getRGB(),
            this.applyContentAlpha(new Color(var7[var10 + 1])).getRGB(),
            this.applyContentAlpha(new Color(var7[var10 + 1])).getRGB()
         };
         Render2D.gradientRect(var1, var11 - 0.5F, var3, var8 + 0.5F, var12, 0.0F);
      }

      int[] var17 = new int[]{
         this.applyContentAlpha(new Color(var7[5])).getRGB(),
         this.applyContentAlpha(new Color(var7[5])).getRGB(),
         this.applyContentAlpha(new Color(var7[6])).getRGB(),
         this.applyContentAlpha(new Color(var7[6])).getRGB()
      };
      Render2D.gradientRect(var1, var2 + 5.0F * var8 - 0.5F, var3, var8, var17, 0.0F, 0.0F, 2.0F, 2.0F);
      int var18 = this.clamp((int)(80.0F * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
      Render2D.outline(var1, var2, var3, var4, 0.5F, new Color(100, 100, 105, var18).getRGB(), 3.0F);
      float var19 = var2 + this.displayHue * var4;
      float var13 = 3.0F + this.hueHandleAnimation * 1.0F;
      float var14 = var3 + 2.0F;
      int var15 = this.clamp((int)(255.0F * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
      Render2D.rect(var1 - 1.0F, var19 - var13 / 2.0F, var14, var13, new Color(255, 255, 255, var15).getRGB(), 1.5F);
      int var16 = this.clamp((int)(100.0F * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
      Render2D.outline(var1 - 1.0F, var19 - var13 / 2.0F, var14, var13, 0.5F, new Color(0, 0, 0, var16).getRGB(), 1.5F);
   }

   private void renderAlphaSlider(float var1, float var2, float var3, float var4, int var5, int var6) {
      int var7 = this.clamp((int)(150.0F * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
      Render2D.rect(var1, var2, var3, var4, new Color(180, 180, 180, var7).getRGB(), 2.0F);
      int var8 = this.getDisplayColorNoAlpha() & 16777215;
      int var9 = var8;
      int var10 = var8 | 0xFF000000;
      int[] var11 = new int[]{
         this.applyContentAlpha(new Color(var9, true), 0.0F).getRGB(),
         this.applyContentAlpha(new Color(var9, true), 0.0F).getRGB(),
         this.applyContentAlpha(new Color(var10, true)).getRGB(),
         this.applyContentAlpha(new Color(var10, true)).getRGB()
      };
      Render2D.gradientRect(var1, var2, var3, var4, var11, 2.0F);
      int var12 = this.clamp((int)(80.0F * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
      Render2D.outline(var1, var2, var3, var4, 0.5F, new Color(100, 100, 105, var12).getRGB(), 3.0F);
      float var13 = var2 + this.displayAlpha * var4;
      float var14 = 3.0F + this.alphaHandleAnimation * 1.0F;
      float var15 = var3 + 2.0F;
      int var16 = this.clamp((int)(255.0F * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
      Render2D.rect(var1 - 1.0F, var13 - var14 / 2.0F, var15, var14, new Color(255, 255, 255, var16).getRGB(), 1.5F);
      int var17 = this.clamp((int)(100.0F * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
      Render2D.outline(var1 - 1.0F, var13 - var14 / 2.0F, var15, var14, 0.5F, new Color(0, 0, 0, var17).getRGB(), 1.5F);
   }

   private Color applyContentAlpha(Color var1, float var2) {
      int var3 = Math.max(0, Math.min(255, (int)(var1.getAlpha() * this.alphaMultiplier * this.contentAlpha * var2)));
      return new Color(var1.getRed(), var1.getGreen(), var1.getBlue(), var3);
   }

   private void renderHexInput(float var1, float var2, float var3, float var4, int var5, int var6) {
      boolean var7 = var5 >= var1 && var5 <= var1 + var3 && var6 >= var2 && var6 <= var2 + var4;
      int var8 = this.clamp((int)((40.0F + this.hexInputAnimation * 20.0F + (var7 ? 10 : 0)) * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
      Render2D.rect(var1, var2, var3, var4, new Color(35, 35, 40, var8).getRGB(), 3.0F);
      int var9 = this.clamp((int)((60.0F + this.hexInputAnimation * 80.0F + (var7 ? 20 : 0)) * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
      Color var10 = this.hexInputActive ? new Color(100, 140, 180, var9) : new Color(80, 80, 85, var9);
      Render2D.outline(var1, var2, var3, var4, 0.5F, var10.getRGB(), 3.0F);
      int var11 = this.clamp((int)(200.0F * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
      Fonts.GUI_ICONS.draw("V", var1 + 4.0F, var2 + var4 / 2.0F - 7.5F, 12.0F, new Color(210, 210, 210, var11).getRGB());
      String var12 = "HEX: ";
      float var13 = 10.0F;
      float var14 = Fonts.BOLD.getWidth(var12, 5.0F);
      int var15 = this.clamp((int)(150.0F * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
      Fonts.BOLD.draw(var12, var1 + 4.0F + var13, var2 + var4 / 2.0F - 2.5F, 5.0F, new Color(140, 140, 150, var15).getRGB());
      String var16 = this.hexInputActive ? this.hexInputText : this.getDisplayHexString();
      float var17 = var1 + 4.0F + var13 + var14;
      float var18 = var2 + var4 / 2.0F - 2.5F;
      if (this.hexInputActive && this.hasHexSelection() && this.hexSelectionAnimation > 0.01F) {
         int var19 = this.getHexSelectionStart();
         int var20 = this.getHexSelectionEnd();
         String var21 = "#" + this.hexInputText.substring(0, var19);
         String var22 = this.hexInputText.substring(var19, var20);
         float var23 = var17 + Fonts.BOLD.getWidth(var21, 5.0F);
         float var24 = Fonts.BOLD.getWidth(var22, 5.0F);
         int var25 = this.clamp((int)(100.0F * this.hexSelectionAnimation * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
      }

      int var26 = this.clamp((int)((180.0F + this.hexInputAnimation * 40.0F) * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
      Fonts.BOLD.draw("#" + var16, var17, var18, 5.0F, new Color(210, 210, 220, var26).getRGB());
      if (this.hexInputActive && !this.hasHexSelection()) {
         float var27 = (float)(Math.sin(this.hexCursorBlinkAnimation * Math.PI * 2.0) * 0.5 + 0.5);
         if (var27 > 0.3F) {
            String var29 = "#" + this.hexInputText.substring(0, this.hexCursorPosition);
            float var31 = var17 + Fonts.BOLD.getWidth(var29, 5.0F);
            int var33 = this.clamp((int)(255.0F * var27 * this.hexInputAnimation * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
            Render2D.rect(var31, var2 + 3.0F, 0.5F, var4 - 6.0F, new Color(180, 180, 185, var33).getRGB(), 0.0F);
         }
      }

      float var28 = var1 + var3 - 15.0F;
      float var30 = var2 + 3.0F;
      float var32 = var4 - 6.0F;
      int var34 = this.clamp((int)(120.0F * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
      Render2D.rect(var28, var30, var32, var32, new Color(150, 150, 150, var34).getRGB(), 3.0F);
      Render2D.rect(var28, var30, var32, var32, this.applyContentAlpha(new Color(this.getDisplayColor(), true)).getRGB(), 3.0F);
      int var35 = this.clamp((int)(80.0F * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
      Render2D.outline(var28, var30, var32, var32, 0.5F, new Color(80, 80, 85, var35).getRGB(), 3.0F);
   }

   private String getDisplayHexString() {
      int var1 = this.getDisplayColor();
      int var2 = var1 >> 24 & 0xFF;
      int var3 = var1 >> 16 & 0xFF;
      int var4 = var1 >> 8 & 0xFF;
      int var5 = var1 & 0xFF;
      return String.format("%02X%02X%02X%02X", var3, var4, var5, var2);
   }

   private boolean isPreviewHover(double var1, double var3) {
      float var5 = this.x + this.width - 12.0F - 4.0F;
      float var6 = this.y + this.height / 2.0F - 6.0F;
      return var1 >= var5 && var1 <= var5 + 12.0F && var3 >= var6 && var3 <= var6 + 12.0F;
   }

   private boolean isPaletteHover(double var1, double var3) {
      float var5 = this.x;
      float var6 = this.y + this.height + 4.0F;
      float var7 = var5 + 4.0F;
      float var8 = var6 + 4.0F;
      float var9 = this.width - 8.0F;
      float var10 = 20.0F;
      float var11 = var9 - var10 - 4.0F;
      float var12 = var7 + var10 + 4.0F;
      return var1 >= var12 && var1 <= var12 + var11 && var3 >= var8 && var3 <= var8 + 70.0F;
   }

   private boolean isHueSliderHover(double var1, double var3) {
      float var5 = this.x;
      float var6 = this.y + this.height + 4.0F;
      float var7 = var5 + 4.0F;
      float var8 = var6 + 4.0F;
      return var1 >= var7 && var1 <= var7 + 8.0F && var3 >= var8 && var3 <= var8 + 70.0F;
   }

   private boolean isAlphaSliderHover(double var1, double var3) {
      float var5 = this.x;
      float var6 = this.y + this.height + 4.0F;
      float var7 = var5 + 4.0F;
      float var8 = var6 + 4.0F;
      float var9 = var7 + 8.0F + 4.0F;
      return var1 >= var9 && var1 <= var9 + 8.0F && var3 >= var8 && var3 <= var8 + 70.0F;
   }

   private boolean isHexInputHover(double var1, double var3) {
      float var5 = this.x;
      float var6 = this.y + this.height + 4.0F;
      float var7 = var5 + 4.0F;
      float var8 = var6 + 4.0F + 70.0F + 4.0F;
      float var9 = this.width - 8.0F;
      return var1 >= var7 && var1 <= var7 + var9 && var3 >= var8 && var3 <= var8 + 16.0F;
   }

   @Override
   public boolean mouseClicked(double var1, double var3, int var5) {
      if (var5 == 0) {
         if (this.isPreviewHover(var1, var3)) {
            this.expanded = !this.expanded;
            if (!this.expanded) {
               this.hexInputActive = false;
               this.draggingPalette = false;
               this.draggingHue = false;
               this.draggingAlpha = false;
               this.clearHexSelection();
            }

            return true;
         }

         if (this.expanded && this.expandAnimation > 0.8F && this.contentAlpha > 0.5F) {
            if (this.isPaletteHover(var1, var3)) {
               this.draggingPalette = true;
               this.updatePalette(var1, var3);
               this.hexInputActive = false;
               this.clearHexSelection();
               return true;
            }

            if (this.isHueSliderHover(var1, var3)) {
               this.draggingHue = true;
               this.updateHue(var3);
               this.hexInputActive = false;
               this.clearHexSelection();
               return true;
            }

            if (this.isAlphaSliderHover(var1, var3)) {
               this.draggingAlpha = true;
               this.updateAlpha(var3);
               this.hexInputActive = false;
               this.clearHexSelection();
               return true;
            }

            if (this.isHexInputHover(var1, var3)) {
               this.hexInputActive = true;
               this.hexInputText = this.getHexString();
               this.hexCursorPosition = this.hexInputText.length();
               this.hexSelectionStart = 0;
               this.hexSelectionEnd = this.hexInputText.length();
               return true;
            }

            if (this.hexInputActive) {
               this.applyHexInput();
               this.hexInputActive = false;
               this.clearHexSelection();
            }
         }
      }

      return false;
   }

   @Override
   public boolean mouseReleased(double var1, double var3, int var5) {
      if (var5 == 0) {
         boolean var6 = this.draggingPalette || this.draggingHue || this.draggingAlpha;
         this.draggingPalette = false;
         this.draggingHue = false;
         this.draggingAlpha = false;
         if (var6) {
            this.updateHexFromColor();
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean mouseDragged(double var1, double var3, int var5, double var6, double var8) {
      if (var5 == 0) {
         if (this.draggingPalette) {
            this.updatePalette(var1, var3);
            return true;
         }

         if (this.draggingHue) {
            this.updateHue(var3);
            return true;
         }

         if (this.draggingAlpha) {
            this.updateAlpha(var3);
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean keyPressed(int var1, int var2, int var3) {
      if (!this.hexInputActive) {
         return false;
      }

      if (this.isControlDown()) {
         switch (var1) {
            case 65:
               this.selectAllHexText();
               return true;
            case 67:
               this.copyHexToClipboard();
               return true;
            case 86:
               this.pasteHexFromClipboard();
               return true;
            case 88:
               if (this.hasHexSelection()) {
                  this.copyHexToClipboard();
                  this.deleteHexSelectedText();
               }

               return true;
         }
      }

      switch (var1) {
         case 256:
            this.hexInputActive = false;
            this.clearHexSelection();
            return true;
         case 257:
         case 335:
            this.applyHexInput();
            this.hexInputActive = false;
            this.clearHexSelection();
            return true;
         case 259:
            if (this.hasHexSelection()) {
               this.deleteHexSelectedText();
            } else if (this.hexCursorPosition > 0) {
               this.hexInputText = this.hexInputText.substring(0, this.hexCursorPosition - 1) + this.hexInputText.substring(this.hexCursorPosition);
               this.hexCursorPosition--;
            }

            return true;
         case 261:
            if (this.hasHexSelection()) {
               this.deleteHexSelectedText();
            } else if (this.hexCursorPosition < this.hexInputText.length()) {
               this.hexInputText = this.hexInputText.substring(0, this.hexCursorPosition) + this.hexInputText.substring(this.hexCursorPosition + 1);
            }

            return true;
         case 262:
            this.moveHexCursor(1);
            return true;
         case 263:
            this.moveHexCursor(-1);
            return true;
         case 268:
            this.hexCursorPosition = 0;
            this.updateHexSelectionAfterCursorMove();
            return true;
         case 269:
            this.hexCursorPosition = this.hexInputText.length();
            this.updateHexSelectionAfterCursorMove();
            return true;
         default:
            return false;
      }
   }

   @Override
   public boolean charTyped(char var1, int var2) {
      if (!this.hexInputActive) {
         return false;
      }

      if (this.isHexChar(var1)) {
         if (this.hasHexSelection()) {
            this.deleteHexSelectedText();
         }

         if (this.hexInputText.length() < 8) {
            this.hexInputText = this.hexInputText.substring(0, this.hexCursorPosition)
               + Character.toUpperCase(var1)
               + this.hexInputText.substring(this.hexCursorPosition);
            this.hexCursorPosition++;
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean isHexChar(char var1) {
      return var1 >= '0' && var1 <= '9' || var1 >= 'A' && var1 <= 'F' || var1 >= 'a' && var1 <= 'f';
   }

   private void updatePalette(double var1, double var3) {
      float var5 = this.x;
      float var6 = this.y + this.height + 4.0F;
      float var7 = var5 + 4.0F;
      float var8 = var6 + 4.0F;
      float var9 = this.width - 8.0F;
      float var10 = 20.0F;
      float var11 = var9 - var10 - 4.0F;
      float var12 = var7 + var10 + 4.0F;
      float var13 = (float)((var1 - var12) / var11);
      float var14 = 1.0F - (float)((var3 - var8) / 70.0);
      this.colorSetting.setSaturation(var13);
      this.colorSetting.setBrightness(var14);
   }

   private void updateHue(double var1) {
      float var3 = this.y + this.height + 4.0F;
      float var4 = var3 + 4.0F;
      float var5 = (float)((var1 - var4) / 70.0);
      this.colorSetting.setHue(var5);
   }

   private void updateAlpha(double var1) {
      float var3 = this.y + this.height + 4.0F;
      float var4 = var3 + 4.0F;
      float var5 = (float)((var1 - var4) / 70.0);
      this.colorSetting.setAlpha(var5);
   }

   private String getHexString() {
      int var1 = this.colorSetting.getColor();
      int var2 = var1 >> 24 & 0xFF;
      int var3 = var1 >> 16 & 0xFF;
      int var4 = var1 >> 8 & 0xFF;
      int var5 = var1 & 0xFF;
      return String.format("%02X%02X%02X%02X", var3, var4, var5, var2);
   }

   private void updateHexFromColor() {
      this.hexInputText = this.getHexString();
      this.hexCursorPosition = this.hexInputText.length();
   }

   private void applyHexInput() {
      String var1 = this.hexInputText.toUpperCase();

      try {
         int var5 = 255;
         int var2;
         int var3;
         int var4;
         if (var1.length() == 6) {
            var2 = Integer.parseInt(var1.substring(0, 2), 16);
            var3 = Integer.parseInt(var1.substring(2, 4), 16);
            var4 = Integer.parseInt(var1.substring(4, 6), 16);
         } else if (var1.length() == 8) {
            var2 = Integer.parseInt(var1.substring(0, 2), 16);
            var3 = Integer.parseInt(var1.substring(2, 4), 16);
            var4 = Integer.parseInt(var1.substring(4, 6), 16);
            var5 = Integer.parseInt(var1.substring(6, 8), 16);
         } else {
            if (var1.length() != 3) {
               this.updateHexFromColor();
               return;
            }

            var2 = Integer.parseInt(var1.substring(0, 1) + var1.substring(0, 1), 16);
            var3 = Integer.parseInt(var1.substring(1, 2) + var1.substring(1, 2), 16);
            var4 = Integer.parseInt(var1.substring(2, 3) + var1.substring(2, 3), 16);
         }

         float[] var6 = Color.RGBtoHSB(var2, var3, var4, null);
         this.colorSetting.setHue(var6[0]);
         this.colorSetting.setSaturation(var6[1]);
         this.colorSetting.setBrightness(var6[2]);
         this.colorSetting.setAlpha(var5 / 255.0F);
      } catch (NumberFormatException var7) {
         this.updateHexFromColor();
      }
   }

   @Override
   public void tick() {
   }

   @Override
   public boolean isHover(double var1, double var3) {
      return var1 >= this.x && var1 <= this.x + this.width && var3 >= this.y && var3 <= this.y + this.height;
   }

   public float getTotalHeight() {
      float var1 = 104.0F;
      float var2 = var1 * this.expandAnimation;
      return this.height + var2;
   }

   public boolean isExpanded() {
      return this.expanded;
   }

   public boolean isHexInputActive() {
      return this.hexInputActive;
   }

   public boolean isDragging() {
      return this.draggingPalette || this.draggingHue || this.draggingAlpha;
   }
}
