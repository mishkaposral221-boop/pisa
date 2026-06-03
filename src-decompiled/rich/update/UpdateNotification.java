package rich.update;

import net.minecraft.class_156;
import net.minecraft.class_3532;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;

public class UpdateNotification {
   private static final float W = 260.0F;
   private static final float H = 130.0F;
   private static final float RADIUS = 8.0F;
   private static final float BLUR = 18.0F;
   private float alpha = 0.0F;
   private boolean visible = false;
   private long lastTime = 0L;
   private float closeHover = 0.0F;
   private float btnHover = 0.0F;

   public void show() {
      this.visible = true;
      this.lastTime = class_156.method_658();
      this.alpha = 0.0F;
   }

   public void hide() {
      this.visible = false;
   }

   public boolean isVisible() {
      return this.visible || this.alpha > 0.01F;
   }

   public void render(float var1, float var2, float var3, float var4, UpdateChecker.UpdateInfo var5) {
      long var6 = class_156.method_658();
      float var8 = Math.min((float)(var6 - this.lastTime) / 1000.0F, 0.1F);
      this.lastTime = var6;
      this.alpha = this.alpha + ((this.visible ? 1.0F : 0.0F) - this.alpha) * 12.0F * var8;
      this.alpha = class_3532.method_15363(this.alpha, 0.0F, 1.0F);
      if (!(this.alpha < 0.01F)) {
         float var9 = (var1 - 260.0F) / 2.0F;
         float var10 = (var2 - 130.0F) / 2.0F;
         float var11 = this.alpha;
         float var12 = var9 + 260.0F - 22.0F;
         float var13 = var10 + 7.0F;
         float var14 = 14.0F;
         float var15 = 110.0F;
         float var16 = 22.0F;
         float var17 = var9 + (260.0F - var15) / 2.0F;
         float var18 = var10 + 130.0F - 32.0F;
         boolean var19 = this.isOver(var3, var4, var12, var13, var14, var14);
         boolean var20 = this.isOver(var3, var4, var17, var18, var15, var16);
         this.closeHover = this.closeHover + ((var19 ? 1.0F : 0.0F) - this.closeHover) * 14.0F * var8;
         this.btnHover = this.btnHover + ((var20 ? 1.0F : 0.0F) - this.btnHover) * 14.0F * var8;
         Render2D.blur(var9, var10, 260.0F, 130.0F, 18.0F, 8.0F, this.withAlpha(395280, (int)(80.0F * var11)));
         int var21 = this.withAlpha(1316639, (int)(220.0F * var11));
         int var22 = this.withAlpha(1053466, (int)(220.0F * var11));
         Render2D.gradientRect(var9, var10, 260.0F, 130.0F, new int[]{var21, var21, var22, var22}, 8.0F);
         Render2D.outline(var9, var10, 260.0F, 130.0F, 0.5F, this.withAlpha(2435638, (int)(200.0F * var11)), 8.0F);
         Render2D.gradientRect(
            var9 + 8.0F,
            var10,
            244.0F,
            1.5F,
            new int[]{
               this.withAlpha(4886745, (int)(180.0F * var11)),
               this.withAlpha(2777000, (int)(180.0F * var11)),
               this.withAlpha(2777000, (int)(180.0F * var11)),
               this.withAlpha(4886745, (int)(180.0F * var11))
            },
            0.0F
         );
         String var23 = "Доступно обновление!";
         float var24 = Fonts.BOLD.getWidth(var23, 8.0F);
         Fonts.BOLD.draw(var23, var9 + (260.0F - var24) / 2.0F, var10 + 12.0F, 8.0F, this.withAlpha(16777215, (int)(240.0F * var11)));
         Render2D.rect(var9 + 12.0F, var10 + 30.0F, 236.0F, 0.5F, this.withAlpha(2435638, (int)(150.0F * var11)), 0.0F);
         String var25 = var5.description();
         if (var25.length() > 38) {
            var25 = var25.substring(0, 38) + "...";
         }

         float var26 = Fonts.BOLD.getWidth(var25, 7.0F);
         Fonts.BOLD.draw(var25, var9 + (260.0F - var26) / 2.0F, var10 + 38.0F, 7.0F, this.withAlpha(11186368, (int)(210.0F * var11)));
         String var27 = "v1.6.0  →  v" + var5.version();
         float var28 = Fonts.BOLD.getWidth(var27, 6.5F);
         Fonts.BOLD.draw(var27, var9 + (260.0F - var28) / 2.0F, var10 + 56.0F, 6.5F, this.withAlpha(6983624, (int)(190.0F * var11)));
         int var29 = this.withAlpha(this.lerpColor(1714746, 1981018, this.btnHover), (int)((160.0F + 40.0F * this.btnHover) * var11));
         int var30 = this.withAlpha(this.lerpColor(2771562, 4885192, this.btnHover), (int)((180.0F + 60.0F * this.btnHover) * var11));
         Render2D.rect(var17, var18, var15, var16, var29, 5.0F);
         Render2D.outline(var17, var18, var15, var16, 0.5F, var30, 5.0F);
         String var31 = "Обновить";
         float var32 = Fonts.BOLD.getWidth(var31, 7.5F);
         Fonts.BOLD
            .draw(
               var31,
               var17 + (var15 - var32) / 2.0F,
               var18 + (var16 - Fonts.BOLD.getHeight(7.5F)) / 2.0F,
               7.5F,
               this.withAlpha(this.lerpColor(11192552, 16777215, this.btnHover), (int)(240.0F * var11))
            );
         float var33 = this.closeHover;
         Render2D.rect(var12, var13, var14, var14, this.withAlpha(this.lerpColor(1974832, 3807256, var33), (int)((120.0F + 80.0F * var33) * var11)), 4.0F);
         Render2D.outline(
            var12, var13, var14, var14, 0.5F, this.withAlpha(this.lerpColor(3159365, 9056304, var33), (int)((150.0F + 80.0F * var33) * var11)), 4.0F
         );
         String var34 = "x";
         float var35 = Fonts.BOLD.getWidth(var34, 7.0F);
         float var36 = Fonts.BOLD.getHeight(7.0F);
         Fonts.BOLD
            .draw(
               var34,
               var12 + (var14 - var35) / 2.0F,
               var13 + (var14 - var36) / 2.0F,
               7.0F,
               this.withAlpha(this.lerpColor(8425640, 16740464, var33), (int)((210.0F + 45.0F * var33) * var11))
            );
      }
   }

   public boolean mouseClicked(float var1, float var2, float var3, float var4, UpdateChecker.UpdateInfo var5) {
      if (!(this.alpha < 0.3F) && var5 != null) {
         float var6 = (var3 - 260.0F) / 2.0F;
         float var7 = (var4 - 130.0F) / 2.0F;
         if (this.isOver(var1, var2, var6 + 260.0F - 22.0F, var7 + 7.0F, 14.0F, 14.0F)) {
            this.hide();
            UpdateChecker.getInstance().dismiss();
            return true;
         } else {
            float var8 = 110.0F;
            float var9 = 22.0F;
            float var10 = var6 + (260.0F - var8) / 2.0F;
            float var11 = var7 + 130.0F - 32.0F;
            if (this.isOver(var1, var2, var10, var11, var8, var9)) {
               this.openUrl(var5.downloadUrl());
               this.hide();
               UpdateChecker.getInstance().dismiss();
               return true;
            } else {
               return this.isOver(var1, var2, var6, var7, 260.0F, 130.0F);
            }
         }
      } else {
         return false;
      }
   }

   public boolean keyPressed(int var1) {
      if (var1 == 256 && this.isVisible()) {
         this.hide();
         UpdateChecker.getInstance().dismiss();
         return true;
      } else {
         return false;
      }
   }

   private void openUrl(String var1) {
      try {
         Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "", var1});
      } catch (Exception var3) {
      }
   }

   private boolean isOver(float var1, float var2, float var3, float var4, float var5, float var6) {
      return var1 >= var3 && var1 <= var3 + var5 && var2 >= var4 && var2 <= var4 + var6;
   }

   private int withAlpha(int var1, int var2) {
      return var1 & 16777215 | class_3532.method_15340(var2, 0, 255) << 24;
   }

   private int lerpColor(int var1, int var2, float var3) {
      int var4 = var1 >> 16 & 0xFF;
      int var5 = var1 >> 8 & 0xFF;
      int var6 = var1 & 0xFF;
      int var7 = var2 >> 16 & 0xFF;
      int var8 = var2 >> 8 & 0xFF;
      int var9 = var2 & 0xFF;
      return (int)(var4 + (var7 - var4) * var3) << 16 | (int)(var5 + (var8 - var5) * var3) << 8 | (int)(var6 + (var9 - var6) * var3);
   }
}
