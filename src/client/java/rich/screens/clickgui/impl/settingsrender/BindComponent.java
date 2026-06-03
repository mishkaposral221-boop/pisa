package rich.screens.clickgui.impl.settingsrender;

import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import rich.modules.module.setting.implement.BindSetting;
import rich.util.interfaces.AbstractSettingComponent;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;

public class BindComponent extends AbstractSettingComponent {
   private boolean listening = false;
   private float listeningAnimation = 0.0F;
   private float hoverAnimation = 0.0F;
   private float bindHoverAnimation = 0.0F;
   private float pulseAnimation = 0.0F;
   private float scaleAnimation = 1.0F;
   private float glowAnimation = 0.0F;
   private float textChangeAnimation = 0.0F;
   private String previousBindText = "";
   private String currentBindText = "";
   private long lastUpdateTime = System.currentTimeMillis();
   private static final float ANIMATION_SPEED = 8.0F;
   private static final float FAST_ANIMATION_SPEED = 12.0F;
   private static final float BIND_BOX_WIDTH = 32.0F;
   private static final float BIND_BOX_HEIGHT = 10.0F;
   public static final int SCROLL_UP_BIND = 1000;
   public static final int SCROLL_DOWN_BIND = 1001;
   public static final int MIDDLE_MOUSE_BIND = 1002;

   public BindComponent(BindSetting var1) {
      super(var1);
      BindSetting var2 = (BindSetting)this.getSetting();
      this.currentBindText = this.getBindDisplayName(var2.getKey(), var2.getType());
      this.previousBindText = this.currentBindText;
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
      boolean var6 = this.isHover(var2, var3);
      boolean var7 = this.isBindHover(var2, var3);
      this.hoverAnimation = this.lerp(this.hoverAnimation, var6 ? 1.0F : 0.0F, var5 * 8.0F);
      this.bindHoverAnimation = this.lerp(this.bindHoverAnimation, var7 ? 1.0F : 0.0F, var5 * 8.0F);
      this.listeningAnimation = this.lerp(this.listeningAnimation, this.listening ? 1.0F : 0.0F, var5 * 12.0F);
      float var8 = this.listening ? 1.05F : (var7 ? 1.02F : 1.0F);
      this.scaleAnimation = this.lerp(this.scaleAnimation, var8, var5 * 8.0F);
      this.glowAnimation = this.lerp(this.glowAnimation, this.listening ? 1.0F : 0.0F, var5 * 8.0F);
      if (this.listening) {
         this.pulseAnimation += var5 * 4.0F;
         if (this.pulseAnimation > Math.PI * 2) {
            this.pulseAnimation -= (float) (Math.PI * 2);
         }
      } else {
         this.pulseAnimation = this.lerp(this.pulseAnimation, 0.0F, var5 * 8.0F);
      }

      BindSetting var9 = (BindSetting)this.getSetting();
      String var10 = this.listening ? "" : this.getBindDisplayName(var9.getKey(), var9.getType());
      if (!var10.equals(this.currentBindText)) {
         this.previousBindText = this.currentBindText;
         this.currentBindText = var10;
         this.textChangeAnimation = 0.0F;
      }

      this.textChangeAnimation = this.lerp(this.textChangeAnimation, 1.0F, var5 * 12.0F);
      int var11 = (int)(200.0F * this.alphaMultiplier);
      Fonts.GUI_ICONS.draw("L", this.x + 1.5F, this.y + this.height / 2.0F - 6.0F, 6.0F, new Color(210, 210, 210, var11).getRGB());
      Fonts.BOLD
         .draw(this.getSetting().getName(), this.x + 9.5F, this.y + this.height / 2.0F - 7.5F, 6.0F, this.applyAlpha(new Color(210, 210, 220, 200)).getRGB());
      String var12 = this.getSetting().getDescription();
      if (var12 != null && !var12.isEmpty()) {
         Fonts.BOLD.draw(var12, this.x + 0.5F, this.y + this.height / 2.0F + 0.5F, 5.0F, this.applyAlpha(new Color(128, 128, 128, 128)).getRGB());
      }

      this.renderBindBox(var2, var3, var9);
   }

   private void renderBindBox(int var1, int var2, BindSetting var3) {
      float var4 = this.x + this.width - 32.0F - 2.0F;
      float var5 = this.y + this.height / 2.0F - 5.0F;
      float var6 = 32.0F * this.scaleAnimation;
      float var7 = 10.0F * this.scaleAnimation;
      float var8 = var4 - (var6 - 32.0F) / 2.0F;
      float var9 = var5 - (var7 - 10.0F) / 2.0F;
      int var10 = (int)(25.0F + this.bindHoverAnimation * 15.0F + this.listeningAnimation * 20.0F);
      Color var11;
      if (this.listening) {
         float var12 = (float)(Math.sin(this.pulseAnimation) * 0.15 + 0.85);
         var11 = new Color((int)(60.0F + 40.0F * var12), (int)(80.0F + 40.0F * var12), (int)(120.0F + 35.0F * var12), (int)(var10 * this.alphaMultiplier));
      } else if (var3.getKey() != -1 && var3.getKey() != -1) {
         var11 = this.applyAlpha(new Color(40, 60, 50, var10));
      } else {
         var11 = this.applyAlpha(new Color(40, 40, 45, var10));
      }

      Render2D.rect(var8, var9, var6, var7, var11.getRGB(), 3.0F);
      Color var13;
      if (this.listening) {
         float var14 = (float)(Math.sin(this.pulseAnimation) * 0.3 + 0.7);
         float var15 = 150.0F * var14 * this.listeningAnimation;
         var13 = new Color(120, 160, 220, (int)(var15 * this.alphaMultiplier));
      } else if (var3.getKey() != -1 && var3.getKey() != -1) {
         float var17 = 80.0F + this.bindHoverAnimation * 40.0F;
         var13 = new Color(100, 160, 120, (int)(var17 * this.alphaMultiplier));
      } else {
         float var16 = 60.0F + this.bindHoverAnimation * 40.0F;
         var13 = new Color(120, 120, 125, (int)(var16 * this.alphaMultiplier));
      }

      Render2D.outline(var8, var9, var6, var7, 0.5F, var13.getRGB(), 3.0F);
      this.renderBindText(var8, var9, var6, var7, var3);
      if (this.listening) {
         this.renderListeningIndicator(var8, var9, var6, var7);
      }
   }

   private void renderBindText(float var1, float var2, float var3, float var4, BindSetting var5) {
      float var6 = var2 + var4 / 2.0F - 2.5F;
      float var7 = var1 + var3 / 2.0F;
      Color var8;
      if (this.listening) {
         float var9 = (float)(Math.sin(this.pulseAnimation * 2.0F) * 0.2 + 0.8);
         int var10 = (int)(220.0F * var9 * this.alphaMultiplier);
         var8 = new Color(180, 200, 240, var10);
      } else if (var5.getKey() != -1 && var5.getKey() != -1) {
         int var15 = (int)(200.0F * this.alphaMultiplier);
         var8 = new Color(140, 200, 150, var15);
      } else {
         int var14 = (int)(150.0F * this.alphaMultiplier);
         var8 = new Color(140, 140, 150, var14);
      }

      if (this.textChangeAnimation < 1.0F && !this.previousBindText.equals(this.currentBindText)) {
         float var16 = 1.0F - this.textChangeAnimation;
         float var17 = this.textChangeAnimation;
         float var11 = -3.0F * this.textChangeAnimation;
         float var12 = 3.0F * (1.0F - this.textChangeAnimation);
         if (var16 > 0.01F) {
            Color var13 = new Color(var8.getRed(), var8.getGreen(), var8.getBlue(), (int)(var8.getAlpha() * var16));
            Fonts.BOLD.drawCentered(this.previousBindText, var7, var6 + var11, 5.0F, var13.getRGB());
         }

         Color var18 = new Color(var8.getRed(), var8.getGreen(), var8.getBlue(), (int)(var8.getAlpha() * var17));
         Fonts.BOLD.drawCentered(this.currentBindText, var7, var6 + var12, 5.0F, var18.getRGB());
      } else {
         Fonts.BOLD.drawCentered(this.currentBindText, var7, var6, 5.0F, var8.getRGB());
      }
   }

   private void renderListeningIndicator(float var1, float var2, float var3, float var4) {
      float var5 = 3.0F;
      float var6 = 1.5F;
      float var7 = var5 * 2.0F;
      float var8 = var1 + (var3 - var7) / 2.0F - var6 / 2.0F;
      float var9 = var2 + var4 - 5.5F;

      for (int var10 = 0; var10 < 3; var10++) {
         float var11 = this.pulseAnimation + var10 * 0.5F;
         float var12 = (float)(Math.sin(var11 * 2.0F) * 0.5 + 0.5);
         float var13 = var6 * (0.5F + var12 * 0.5F);
         int var14 = (int)(150.0F * (0.3F + var12 * 0.7F) * this.listeningAnimation * this.alphaMultiplier);
         float var15 = var8 + var10 * var5 + (var6 - var13) / 2.0F;
         float var16 = var9 + (var6 - var13) / 2.0F;
         Render2D.rect(var15, var16, var13, var13, new Color(120, 160, 220, var14).getRGB(), var13 / 2.0F);
      }
   }

   private String getBindDisplayName(int var1, int var2) {
      if (var1 == -1 || var1 == -1) {
         return "None";
      }

      if (var1 == 1000) {
         return "ScrollUp";
      }

      if (var1 == 1001) {
         return "ScrollDn";
      }

      if (var1 == 1002) {
         return "MMB";
      }

      if (var2 == 0) {
         return switch (var1) {
            case 0 -> "LMB";
            case 1 -> "RMB";
            case 2 -> "MMB";
            case 3 -> "M4";
            case 4 -> "M5";
            case 5 -> "M6";
            case 6 -> "M7";
            case 7 -> "M8";
            default -> "M" + var1;
         };
      } else {
         String var3 = GLFW.glfwGetKeyName(var1, 0);
         if (var3 == null) {
            return switch (var1) {
               case 32 -> "Space";
               case 256 -> "Esc";
               case 257 -> "Enter";
               case 258 -> "Tab";
               case 259 -> "Back";
               case 260 -> "Ins";
               case 261 -> "Del";
               case 262 -> "Right";
               case 263 -> "Left";
               case 264 -> "Down";
               case 265 -> "Up";
               case 266 -> "PgUp";
               case 267 -> "PgDn";
               case 268 -> "Home";
               case 269 -> "End";
               case 280 -> "Caps";
               case 281 -> "Scroll";
               case 282 -> "NumLk";
               case 283 -> "Print";
               case 284 -> "Pause";
               case 290 -> "F1";
               case 291 -> "F2";
               case 292 -> "F3";
               case 293 -> "F4";
               case 294 -> "F5";
               case 295 -> "F6";
               case 296 -> "F7";
               case 297 -> "F8";
               case 298 -> "F9";
               case 299 -> "F10";
               case 300 -> "F11";
               case 301 -> "F12";
               case 320 -> "Num0";
               case 321 -> "Num1";
               case 322 -> "Num2";
               case 323 -> "Num3";
               case 324 -> "Num4";
               case 325 -> "Num5";
               case 326 -> "Num6";
               case 327 -> "Num7";
               case 328 -> "Num8";
               case 329 -> "Num9";
               case 330 -> "Num.";
               case 331 -> "Num/";
               case 332 -> "Num*";
               case 333 -> "Num-";
               case 334 -> "Num+";
               case 335 -> "NumEnt";
               case 340 -> "LShift";
               case 341 -> "LCtrl";
               case 342 -> "LAlt";
               case 344 -> "RShift";
               case 345 -> "RCtrl";
               case 346 -> "RAlt";
               default -> "Key" + var1;
            };
         } else {
            return var3.toUpperCase();
         }
      }
   }

   private boolean isBindHover(double var1, double var3) {
      float var5 = this.x + this.width - 32.0F - 2.0F;
      float var6 = this.y + this.height / 2.0F - 5.0F;
      return var1 >= var5 && var1 <= var5 + 32.0F && var3 >= var6 && var3 <= var6 + 10.0F;
   }

   public void handleScrollBind(double var1) {
      if (this.listening) {
         BindSetting var3 = (BindSetting)this.getSetting();
         if (var1 > 0.0) {
            var3.setKey(1000);
         } else {
            var3.setKey(1001);
         }

         var3.setType(2);
         this.listening = false;
      }
   }

   public void handleMiddleMouseBind() {
      if (this.listening) {
         BindSetting var1 = (BindSetting)this.getSetting();
         var1.setKey(1002);
         var1.setType(2);
         this.listening = false;
      }
   }

   @Override
   public boolean mouseClicked(double var1, double var3, int var5) {
      if (this.isBindHover(var1, var3)) {
         if (var5 == 1) {
            ((BindSetting)this.getSetting()).setKey(-1);
            ((BindSetting)this.getSetting()).setType(1);
            this.listening = false;
            return true;
         }

         if (this.listening) {
            ((BindSetting)this.getSetting()).setKey(var5);
            ((BindSetting)this.getSetting()).setType(0);
            this.listening = false;
            return true;
         }

         if (var5 == 0) {
            this.listening = true;
            return true;
         }
      } else if (this.listening) {
         this.listening = false;
         return true;
      }

      return false;
   }

   @Override
   public boolean keyPressed(int var1, int var2, int var3) {
      if (this.listening) {
         if (var1 == 256) {
            this.listening = false;
            return true;
         } else if (var1 == 259 || var1 == 261) {
            ((BindSetting)this.getSetting()).setKey(-1);
            ((BindSetting)this.getSetting()).setType(1);
            this.listening = false;
            return true;
         } else if (var1 != -1) {
            ((BindSetting)this.getSetting()).setKey(var1);
            ((BindSetting)this.getSetting()).setType(1);
            this.listening = false;
            return true;
         } else {
            return true;
         }
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

   public boolean isListening() {
      return this.listening;
   }
}
