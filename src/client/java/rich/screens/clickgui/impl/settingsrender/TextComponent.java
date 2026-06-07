package rich.screens.clickgui.impl.settingsrender;

import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import rich.modules.module.setting.implement.TextSetting;
import rich.util.interfaces.AbstractSettingComponent;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.shader.Scissor;

public class TextComponent extends AbstractSettingComponent {
   public static boolean typing = false;
   private final TextSetting textSetting;
   private boolean focused = false;
   private int cursorPosition = 0;
   private int selectionStart = -1;
   private int selectionEnd = -1;
   private long lastClickTime = 0L;
   private String text = "";
   private float focusAnimation = 0.0F;
   private float hoverAnimation = 0.0F;
   private float textScrollOffset = 0.0F;
   private float targetScrollOffset = 0.0F;
   private float cursorBlinkAnimation = 0.0F;
   private float selectionAnimation = 0.0F;
   private long lastUpdateTime = System.currentTimeMillis();
   private static final float ANIMATION_SPEED = 8.0F;
   private static final float SCROLL_ANIMATION_SPEED = 10.0F;
   private static final float INPUT_BOX_WIDTH = 65.0F;
   private static final float INPUT_BOX_HEIGHT = 10.0F;
   private static final float TEXT_PADDING = 4.0F;

   public TextComponent(TextSetting var1) {
      super(var1);
      this.textSetting = var1;
      this.text = this.textSetting.getText() != null ? this.textSetting.getText() : "";
      this.cursorPosition = this.text.length();
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

   @Override
   public void render(DrawContext var1, int var2, int var3, float var4) {
      float var5 = this.getDeltaTime();
      boolean var6 = this.isInputBoxHover(var2, var3);
      this.hoverAnimation = this.lerp(this.hoverAnimation, var6 ? 1.0F : 0.0F, var5 * 8.0F);
      this.focusAnimation = this.lerp(this.focusAnimation, this.focused ? 1.0F : 0.0F, var5 * 8.0F);
      this.selectionAnimation = this.lerp(this.selectionAnimation, this.hasSelection() ? 1.0F : 0.0F, var5 * 8.0F);
      if (this.focused) {
         this.cursorBlinkAnimation += var5 * 2.0F;
         if (this.cursorBlinkAnimation > 1.0F) {
            this.cursorBlinkAnimation--;
         }
      } else {
         this.cursorBlinkAnimation = 0.0F;
      }

      int var7 = (int)(200.0F * this.alphaMultiplier);
      Fonts.GUI_ICONS.draw("S", this.x + 0.5F, this.y + this.height / 2.0F - 10.25F, 11.0F, new Color(210, 210, 220, var7).getRGB());
      Fonts.BOLD
         .draw(this.textSetting.getName(), this.x + 9.5F, this.y + this.height / 2.0F - 7.5F, 6.0F, this.applyAlpha(new Color(210, 210, 220, 200)).getRGB());
      String var8 = this.textSetting.getDescription();
      if (var8 != null && !var8.isEmpty()) {
         Fonts.BOLD.draw(var8, this.x + 0.5F, this.y + this.height / 2.0F + 0.5F, 5.0F, this.applyAlpha(new Color(128, 128, 128, 128)).getRGB());
      }

      float var9 = this.x + this.width - 65.0F - 2.0F;
      float var10 = this.y + this.height / 2.0F - 5.0F;
      int var11 = (int)(25.0F + this.focusAnimation * 15.0F + this.hoverAnimation * 10.0F);
      Render2D.rect(var9, var10, 65.0F, 10.0F, this.applyAlpha(new Color(40, 40, 45, var11)).getRGB(), 3.0F);
      float var12 = 60.0F + this.hoverAnimation * 40.0F + this.focusAnimation * 60.0F;
      Color var13 = this.focused
         ? new Color(100, 140, 180, (int)(var12 * this.alphaMultiplier))
         : new Color(155, 155, 155, (int)(var12 * this.alphaMultiplier));
      Render2D.outline(var9, var10, 65.0F, 10.0F, 0.5F, var13.getRGB(), 3.0F);
      this.renderTextContent(var9, var10, var5);
   }

   private void renderTextContent(float var1, float var2, float var3) {
      float var4 = var1 + 4.0F;
      float var5 = 57.0F;
      float var6 = var2 + 5.0F - 2.5F;
      String var7 = this.text;
      float var8 = Fonts.BOLD.getWidth(var7, 5.0F);
      if (this.focused) {
         String var9 = this.text.substring(0, this.cursorPosition);
         float var10 = Fonts.BOLD.getWidth(var9, 5.0F);
         if (var10 - this.targetScrollOffset > var5 - 2.0F) {
            this.targetScrollOffset = var10 - var5 + 2.0F;
         } else if (var10 - this.targetScrollOffset < 0.0F) {
            this.targetScrollOffset = var10;
         }

         if (var8 <= var5) {
            this.targetScrollOffset = 0.0F;
         }

         this.targetScrollOffset = Math.max(0.0F, Math.min(this.targetScrollOffset, Math.max(0.0F, var8 - var5)));
      } else {
         this.targetScrollOffset = 0.0F;
      }

      this.textScrollOffset = this.lerp(this.textScrollOffset, this.targetScrollOffset, var3 * 10.0F);
      Scissor.enable(var1 + 2.0F, var2, 61.0F, 10.0F, 2.0F);
      if (this.text.isEmpty() && !this.focused) {
         Fonts.BOLD.draw("Enter text...", var4, var6, 5.0F, this.applyAlpha(new Color(100, 100, 105, 100)).getRGB());
      } else {
         if (this.focused && this.hasSelection() && this.selectionAnimation > 0.01F) {
            int var16 = this.getStartOfSelection();
            int var18 = this.getEndOfSelection();
            String var11 = this.text.substring(0, var16);
            String var12 = this.text.substring(var16, var18);
            float var13 = var4 + Fonts.BOLD.getWidth(var11, 5.0F) - this.textScrollOffset;
            float var14 = Fonts.BOLD.getWidth(var12, 5.0F);
            int var15 = (int)(100.0F * this.selectionAnimation * this.alphaMultiplier);
            Render2D.rect(var13, var2 + 2.0F, var14, 6.0F, new Color(100, 140, 180, var15).getRGB(), 2.0F);
         }

         int var17 = (int)((160.0F + this.focusAnimation * 60.0F) * this.alphaMultiplier);
         Fonts.BOLD.draw(var7, var4 - this.textScrollOffset, var6, 5.0F, new Color(210, 210, 220, var17).getRGB());
         if (this.focused && !this.hasSelection()) {
            float var19 = (float)(Math.sin(this.cursorBlinkAnimation * Math.PI * 2.0) * 0.5 + 0.5);
            if (var19 > 0.3F) {
               String var20 = this.text.substring(0, this.cursorPosition);
               float var21 = var4 + Fonts.BOLD.getWidth(var20, 5.0F) - this.textScrollOffset;
               int var22 = (int)(255.0F * var19 * this.focusAnimation * this.alphaMultiplier);
               Render2D.rect(var21, var2 + 2.0F, 0.5F, 6.0F, new Color(180, 180, 185, var22).getRGB(), 0.0F);
            }
         }
      }

      Scissor.disable();
   }

   private boolean isInputBoxHover(double var1, double var3) {
      float var5 = this.x + this.width - 65.0F - 2.0F;
      float var6 = this.y + this.height / 2.0F - 5.0F;
      return var1 >= var5 && var1 <= var5 + 65.0F && var3 >= var6 && var3 <= var6 + 10.0F;
   }

   @Override
   public boolean mouseClicked(double var1, double var3, int var5) {
      boolean var6 = this.isInputBoxHover(var1, var3);
      if (var6 && var5 == 0) {
         long var7 = System.currentTimeMillis();
         if (var7 - this.lastClickTime < 250L && this.focused) {
            this.selectAllText();
         } else {
            this.focused = true;
            typing = true;
            this.cursorPosition = this.getCursorIndexAt(var1);
            this.selectionStart = this.cursorPosition;
            this.selectionEnd = this.cursorPosition;
         }

         this.lastClickTime = var7;
         return true;
      } else {
         if (!var6 && this.focused) {
            this.applyText();
            this.focused = false;
            typing = false;
            this.clearSelection();
         }

         return false;
      }
   }

   @Override
   public boolean mouseDragged(double var1, double var3, int var5, double var6, double var8) {
      if (this.focused && var5 == 0) {
         this.cursorPosition = this.getCursorIndexAt(var1);
         this.selectionEnd = this.cursorPosition;
         return true;
      } else {
         return false;
      }
   }

   private boolean isControlDown() {
      long var1 = mc.getWindow().getHandle();
      return GLFW.glfwGetKey(var1, 341) == 1 || GLFW.glfwGetKey(var1, 345) == 1;
   }

   private boolean isShiftDown() {
      long var1 = mc.getWindow().getHandle();
      return GLFW.glfwGetKey(var1, 340) == 1 || GLFW.glfwGetKey(var1, 344) == 1;
   }

   @Override
   public boolean keyPressed(int var1, int var2, int var3) {
      if (!this.focused) {
         return false;
      }

      if (this.isControlDown()) {
         switch (var1) {
            case 65:
               this.selectAllText();
               return true;
            case 67:
               this.copyToClipboard();
               return true;
            case 86:
               this.pasteFromClipboard();
               return true;
            case 88:
               if (this.hasSelection()) {
                  this.copyToClipboard();
                  this.deleteSelectedText();
               }

               return true;
         }
      } else {
         switch (var1) {
            case 256:
               this.text = this.textSetting.getText() != null ? this.textSetting.getText() : "";
               this.cursorPosition = this.text.length();
               this.focused = false;
               typing = false;
               return true;
            case 257:
               this.applyText();
               this.focused = false;
               typing = false;
               return true;
            case 258:
            case 260:
            case 264:
            case 265:
            case 266:
            case 267:
            default:
               break;
            case 259:
               this.handleBackspace();
               return true;
            case 261:
               this.handleDelete();
               return true;
            case 262:
               this.moveCursor(1);
               return true;
            case 263:
               this.moveCursor(-1);
               return true;
            case 268:
               this.cursorPosition = 0;
               this.updateSelectionAfterCursorMove();
               return true;
            case 269:
               this.cursorPosition = this.text.length();
               this.updateSelectionAfterCursorMove();
               return true;
         }
      }

      return false;
   }

   @Override
   public boolean charTyped(char var1, int var2) {
      if (!this.focused) {
         return false;
      }

      if (Character.isISOControl(var1)) {
         return false;
      }

      int var3 = this.textSetting.getMax() > 0 ? this.textSetting.getMax() : Integer.MAX_VALUE;
      if (this.text.length() >= var3 && !this.hasSelection()) {
         return false;
      }

      this.deleteSelectedText();
      this.text = this.text.substring(0, this.cursorPosition) + var1 + this.text.substring(this.cursorPosition);
      this.cursorPosition++;
      this.clearSelection();
      return true;
   }

   @Override
   public void tick() {
   }

   private void applyText() {
      int var1 = this.textSetting.getMin() > 0 ? this.textSetting.getMin() : 0;
      int var2 = this.textSetting.getMax() > 0 ? this.textSetting.getMax() : Integer.MAX_VALUE;
      if (this.text.length() >= var1 && this.text.length() <= var2) {
         this.textSetting.setText(this.text);
      } else {
         this.text = this.textSetting.getText() != null ? this.textSetting.getText() : "";
         this.cursorPosition = this.text.length();
      }
   }

   private void handleBackspace() {
      if (this.hasSelection()) {
         this.replaceText(this.getStartOfSelection(), this.getEndOfSelection(), "");
      } else if (this.cursorPosition > 0) {
         this.replaceText(this.cursorPosition - 1, this.cursorPosition, "");
      }
   }

   private void handleDelete() {
      if (this.hasSelection()) {
         this.replaceText(this.getStartOfSelection(), this.getEndOfSelection(), "");
      } else if (this.cursorPosition < this.text.length()) {
         this.text = this.text.substring(0, this.cursorPosition) + this.text.substring(this.cursorPosition + 1);
      }
   }

   private void moveCursor(int var1) {
      if (this.hasSelection() && !this.isShiftDown()) {
         if (var1 < 0) {
            this.cursorPosition = this.getStartOfSelection();
         } else {
            this.cursorPosition = this.getEndOfSelection();
         }

         this.clearSelection();
      } else {
         if (var1 < 0 && this.cursorPosition > 0) {
            this.cursorPosition--;
         } else if (var1 > 0 && this.cursorPosition < this.text.length()) {
            this.cursorPosition++;
         }

         this.updateSelectionAfterCursorMove();
      }
   }

   private void updateSelectionAfterCursorMove() {
      if (this.isShiftDown()) {
         if (this.selectionStart == -1) {
            this.selectionStart = this.selectionEnd != -1 ? this.selectionEnd : this.cursorPosition;
         }

         this.selectionEnd = this.cursorPosition;
      } else {
         this.clearSelection();
      }
   }

   private void pasteFromClipboard() {
      String var1 = GLFW.glfwGetClipboardString(mc.getWindow().getHandle());
      if (var1 != null && !var1.isEmpty()) {
         var1 = var1.replaceAll("[\n\r\t]", "");
         if (this.hasSelection()) {
            this.deleteSelectedText();
         }

         int var2 = this.textSetting.getMax() > 0 ? this.textSetting.getMax() : Integer.MAX_VALUE;
         int var3 = var2 - this.text.length();
         if (var1.length() > var3) {
            var1 = var1.substring(0, var3);
         }

         if (!var1.isEmpty()) {
            this.text = this.text.substring(0, this.cursorPosition) + var1 + this.text.substring(this.cursorPosition);
            this.cursorPosition = this.cursorPosition + var1.length();
         }
      }
   }

   private void copyToClipboard() {
      if (this.hasSelection()) {
         GLFW.glfwSetClipboardString(mc.getWindow().getHandle(), this.getSelectedText());
      }
   }

   private void selectAllText() {
      this.selectionStart = 0;
      this.selectionEnd = this.text.length();
      this.cursorPosition = this.text.length();
   }

   private void replaceText(int var1, int var2, String var3) {
      if (var1 < 0) {
         var1 = 0;
      }

      if (var2 > this.text.length()) {
         var2 = this.text.length();
      }

      if (var1 > var2) {
         int var4 = var1;
         var1 = var2;
         var2 = var4;
      }

      this.text = this.text.substring(0, var1) + var3 + this.text.substring(var2);
      this.cursorPosition = var1 + var3.length();
      this.clearSelection();
   }

   private void deleteSelectedText() {
      if (this.hasSelection()) {
         this.replaceText(this.getStartOfSelection(), this.getEndOfSelection(), "");
      }
   }

   private boolean hasSelection() {
      return this.selectionStart != -1 && this.selectionEnd != -1 && this.selectionStart != this.selectionEnd;
   }

   private String getSelectedText() {
      return !this.hasSelection() ? "" : this.text.substring(this.getStartOfSelection(), this.getEndOfSelection());
   }

   private int getStartOfSelection() {
      return Math.min(this.selectionStart, this.selectionEnd);
   }

   private int getEndOfSelection() {
      return Math.max(this.selectionStart, this.selectionEnd);
   }

   private void clearSelection() {
      this.selectionStart = -1;
      this.selectionEnd = -1;
   }

   private int getCursorIndexAt(double var1) {
      float var3 = this.x + this.width - 65.0F - 2.0F;
      float var4 = var3 + 4.0F;
      float var5 = (float)(var1 - var4 + this.textScrollOffset);
      if (var5 <= 0.0F) {
         return 0;
      }

      int var6 = 0;
      float var7 = 0.0F;

      while (var6 < this.text.length()) {
         float var8 = Fonts.BOLD.getWidth(this.text.substring(0, var6 + 1), 5.0F);
         float var9 = (var7 + var8) / 2.0F;
         if (var5 < var9) {
            return var6;
         }

         var7 = var8;
         var6++;
      }

      return this.text.length();
   }

   @Override
   public boolean isHover(double var1, double var3) {
      return var1 >= this.x && var1 <= this.x + this.width && var3 >= this.y && var3 <= this.y + this.height;
   }

   public boolean isFocused() {
      return this.focused;
   }
}
