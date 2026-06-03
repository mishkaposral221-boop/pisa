package rich.screens.hud;

import java.awt.Color;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.DrawContext;
import rich.Initialization;
import rich.client.draggables.AbstractHudElement;
import rich.modules.impl.player.NameProtect;
import rich.modules.impl.render.Hud;
import rich.theme.ClientTheme;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.tps.TPSCalculate;

public class Watermark extends AbstractHudElement {
   private String lastFps = "";
   private String oldFps = "";
   private long fpsAnimationStart = 0L;
   private String lastTime = "";
   private String oldTime = "";
   private long timeAnimationStart = 0L;
   private String lastTps = "";
   private String oldTps = "";
   private long tpsAnimationStart = 0L;
   private static final long ANIMATION_DURATION = 200L;
   private static final float ANIMATION_OFFSET = 8.0F;

   public Watermark() {
      super("Watermark", 10, 10, 200, 26, true);
      this.startAnimation();
   }

   @Override
   public void tick() {
   }

   private int clampAlpha(float var1) {
      return Math.max(0, Math.min(255, (int)(var1 * 255.0F)));
   }

   @Override
   public void drawDraggable(DrawContext var1, int var2) {
      if (var2 > 0) {
         float var3 = this.getX();
         float var4 = this.getY();
         NameProtect var5 = Initialization.getInstance().getManager().getModuleProvider().get(NameProtect.class);
         String var6 = var5 != null && var5.isState() ? "Protected" : this.mc.getSession().getUsername();
         String var7 = String.valueOf(this.mc.getCurrentFps());
         String var8 = "fps";
         String var9 = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
         boolean var10 = Hud.getInstance() != null && Hud.getInstance().showTps.isValue();
         float var11 = 20.0F;
         if (TPSCalculate.getInstance() != null) {
            var11 = TPSCalculate.getInstance().getTpsRounded();
         }

         String var12 = String.format("%.1f", var11);
         String var13 = "tps";
         long var14 = System.currentTimeMillis();
         if (!var7.equals(this.lastFps)) {
            this.oldFps = this.lastFps;
            this.lastFps = var7;
            this.fpsAnimationStart = var14;
         }

         if (!var9.equals(this.lastTime)) {
            this.oldTime = this.lastTime;
            this.lastTime = var9;
            this.timeAnimationStart = var14;
         }

         if (!var12.equals(this.lastTps)) {
            this.oldTps = this.lastTps;
            this.lastTps = var12;
            this.tpsAnimationStart = var14;
         }

         float var16 = Math.min(1.0F, (float)(var14 - this.fpsAnimationStart) / 200.0F);
         float var17 = Math.min(1.0F, (float)(var14 - this.timeAnimationStart) / 200.0F);
         float var18 = Math.min(1.0F, (float)(var14 - this.tpsAnimationStart) / 200.0F);
         float var19 = Fonts.BOLD.getWidth(var6, 6.0F);
         float var20 = Fonts.BOLD.getWidth(var7, 6.0F);
         float var21 = Fonts.BOLD.getWidth(var8, 6.0F);
         float var22 = Fonts.BOLD.getWidth(var9, 6.0F);
         float var23 = Fonts.BOLD.getWidth(var12, 6.0F);
         float var24 = Fonts.BOLD.getWidth(var13, 6.0F);
         float var25 = 22.0F + var19 + 10.0F + 8.0F + 10.0F + 12.0F + var20 + 2.0F + var21 + 10.0F + 8.0F + 10.0F + 12.0F + var22 - 18.0F;
         float var26 = 34.0F + var23 + 2.0F + var24 + 2.0F;
         int var27 = var10 ? (int)(22.0F + var25 + var26) : (int)(22.0F + var25);
         this.setWidth(var27);
         this.setHeight(26);
         Render2D.gradientRect(var3, var4 + 3.0F, 20.0F, 20.0F, ClientTheme.bgGradient(255), 5.0F);
         Render2D.outline(var3, var4 + 3.0F, 20.0F, 20.0F, 0.35F, ClientTheme.outline(255), 5.0F);
         float var28 = var3 + 22.0F;
         Render2D.gradientRect(var28, var4 + 3.0F, var25, 20.0F, ClientTheme.bgGradient(255), 5.0F);
         Render2D.outline(var28, var4 + 3.0F, var25, 20.0F, 0.35F, ClientTheme.outline(255), 5.0F);
         float var29 = var28 + var25 + 2.0F;
         if (var10) {
            Render2D.gradientRect(var29, var4 + 3.0F, var26, 20.0F, ClientTheme.bgGradient(255), 5.0F);
            Render2D.outline(var29, var4 + 3.0F, var26, 20.0F, 0.35F, ClientTheme.outline(255), 5.0F);
         }

         float var30 = 16.0F;
         Render2D.texture(Identifier.of("rich", "icon.png"), var3 + 2.0F, var4 + 5.0F, var30, var30, -1);
         float var31 = var4 + 7.0F;
         float var32 = var28 + 5.0F;
         Fonts.CATEGORY_ICONS.draw("d", var32, var31 + 1.0F, 10.0F, new Color(225, 225, 225, 255).getRGB());
         var32 += 12.0F;
         Fonts.BOLD.draw(var6, var32, var31 + 3.0F, 6.0F, new Color(255, 255, 255, 255).getRGB());
         var32 += var19 + 5.0F;
         Fonts.TEST.draw("»", var32, var31 + 1.5F, 8.0F, new Color(155, 155, 155, 255).getRGB());
         var32 += 12.0F;
         Fonts.CATEGORY_ICONS.draw("b", var32, var31 + 2.5F, 9.0F, new Color(225, 225, 225, 255).getRGB());
         var32 += 12.0F;
         this.drawAnimatedTextPerChar(var7, this.oldFps, var32, var31 + 3.0F, 6.0F, var16);
         var32 += var20 + 2.0F;
         Fonts.BOLD.draw(var8, var32, var31 + 3.0F, 6.0F, new Color(155, 155, 155, 255).getRGB());
         var32 += var21 + 5.0F;
         Fonts.TEST.draw("»", var32, var31 + 1.5F, 8.0F, new Color(155, 155, 155, 255).getRGB());
         var32 += 12.0F;
         Fonts.CATEGORY_ICONS.draw("n", var32, var31 + 2.5F, 9.0F, new Color(225, 225, 225, 255).getRGB());
         var32 += 12.0F;
         this.drawAnimatedTextPerChar(var9, this.oldTime, var32, var31 + 3.0F, 6.0F, var17);
         if (var10) {
            Fonts.ICONSTYPETHO.draw("t", var29 + 5.0F, var31, 12.0F, new Color(225, 225, 225, 255).getRGB());
            float var33 = var29 + 19.0F;
            Fonts.TEST.draw("»", var33, var31 + 1.5F, 8.0F, new Color(155, 155, 155, 255).getRGB());
            var33 += 8.0F;
            this.drawAnimatedTextPerChar(var12, this.oldTps, var33, var31 + 3.0F, 6.0F, var18);
            var33 += var23 + 2.0F;
            Fonts.BOLD.draw(var13, var33, var31 + 3.0F, 6.0F, new Color(155, 155, 155, 255).getRGB());
         }
      }
   }

   private void drawAnimatedTextPerChar(String var1, String var2, float var3, float var4, float var5, float var6) {
      if (!var2.isEmpty() && !(var6 >= 1.0F)) {
         float var7 = var3;
         int var8 = Math.max(var1.length(), var2.length());
         String var9 = this.padLeft(var1, var8);
         String var10 = this.padLeft(var2, var8);

         for (int var11 = 0; var11 < var9.length(); var11++) {
            char var12 = var9.charAt(var11);
            char var13 = var10.charAt(var11);
            if (var12 != ' ' || var13 != ' ') {
               float var14 = Fonts.BOLD.getWidth(String.valueOf(var12 != ' ' ? var12 : var13), var5);
               boolean var15 = Character.isDigit(var12) || var12 == '.';
               boolean var16 = Character.isDigit(var13) || var13 == '.';
               if (var12 == var13 || !var15 && !var16) {
                  if (var12 != ' ') {
                     Fonts.BOLD.draw(String.valueOf(var12), var7, var4, var5, new Color(255, 255, 255, 255).getRGB());
                  }
               } else {
                  float var17 = this.easeOutCubic(var6);
                  if (var13 != ' ' && var16) {
                     int var18 = this.clampAlpha(1.0F - var17);
                     if (var18 > 0) {
                        Fonts.BOLD.draw(String.valueOf(var13), var7, var4 + var17 * 8.0F, var5, new Color(255, 255, 255, var18).getRGB());
                     }
                  }

                  if (var12 != ' ' && var15) {
                     int var19 = this.clampAlpha(var17);
                     if (var19 > 0) {
                        Fonts.BOLD.draw(String.valueOf(var12), var7, var4 + (1.0F - var17) * -8.0F, var5, new Color(255, 255, 255, var19).getRGB());
                     }
                  }
               }

               if (var12 != ' ') {
                  var7 += var14;
               }
            }
         }
      } else {
         Fonts.BOLD.draw(var1, var3, var4, var5, new Color(255, 255, 255, 255).getRGB());
      }
   }

   private String padLeft(String var1, int var2) {
      if (var1.length() >= var2) {
         return var1;
      }

      StringBuilder var3 = new StringBuilder();

      for (int var4 = 0; var4 < var2 - var1.length(); var4++) {
         var3.append(' ');
      }

      var3.append(var1);
      return var3.toString();
   }

   private float easeOutCubic(float var1) {
      return 1.0F - (float)Math.pow(1.0 - var1, 3.0);
   }
}
