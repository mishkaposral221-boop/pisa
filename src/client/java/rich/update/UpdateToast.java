package rich.update;

import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;

public class UpdateToast {
   private static UpdateToast ingameInstance;
   private static final float W = 200.0F;
   private static final float H = 52.0F;
   private static final float R = 8.0F;
   private static final float PAD = 12.0F;
   private static final float MARGIN = 10.0F;
   private float alpha = 0.0F;
   private boolean visible = false;
   private long lastTime = 0L;
   private float closeHover = 0.0F;
   private float btnHover = 0.0F;

   public static UpdateToast getIngameInstance() {
      return ingameInstance;
   }

   public static void setIngameInstance(UpdateToast var0) {
      ingameInstance = var0;
   }

   public void show() {
      this.visible = true;
      this.lastTime = Util.getMeasuringTimeMs();
      this.alpha = 0.0F;
   }

   public void hide() {
      this.visible = false;
   }

   public boolean isVisible() {
      return this.visible || this.alpha > 0.01F;
   }

   public void render(int var1, int var2, float var3, float var4, UpdateChecker.UpdateInfo var5) {
      long var6 = Util.getMeasuringTimeMs();
      float var8 = Math.min((float)(var6 - this.lastTime) / 1000.0F, 0.1F);
      this.lastTime = var6;
      this.alpha = this.alpha + ((this.visible ? 1.0F : 0.0F) - this.alpha) * 12.0F * var8;
      this.alpha = MathHelper.clamp(this.alpha, 0.0F, 1.0F);
      if (!(this.alpha < 0.01F)) {
         float var9 = this.alpha;
         float var10 = var1 - 200.0F - 10.0F;
         float var11 = var2 - 52.0F - 10.0F;
         float var12 = (1.0F - var9) * 210.0F;
         var10 += var12;
         float var13 = 10.0F;
         float var14 = var10 + 200.0F - var13 - 5.0F;
         float var15 = var11 + 5.0F;
         float var16 = 70.0F;
         float var17 = 16.0F;
         float var18 = var10 + 200.0F - var16 - 5.0F;
         float var19 = var11 + 52.0F - var17 - 6.0F;
         boolean var20 = this.isOver(var3, var4, var14, var15, var13, var13);
         boolean var21 = this.isOver(var3, var4, var18, var19, var16, var17);
         this.closeHover = this.closeHover + ((var20 ? 1.0F : 0.0F) - this.closeHover) * 14.0F * var8;
         this.btnHover = this.btnHover + ((var21 ? 1.0F : 0.0F) - this.btnHover) * 14.0F * var8;
         Render2D.blur(var10, var11, 200.0F, 52.0F, 15.0F, 8.0F, this.withAlpha(395280, (int)(70.0F * var9)));
         int var22 = this.withAlpha(1316639, (int)(230.0F * var9));
         int var23 = this.withAlpha(1053466, (int)(230.0F * var9));
         Render2D.gradientRect(var10, var11, 200.0F, 52.0F, new int[]{var22, var22, var23, var23}, 8.0F);
         Render2D.outline(var10, var11, 200.0F, 52.0F, 0.5F, this.withAlpha(2435638, (int)(200.0F * var9)), 8.0F);
         Render2D.gradientRect(
            var10,
            var11 + 8.0F,
            2.0F,
            36.0F,
            new int[]{
               this.withAlpha(4886745, (int)(200.0F * var9)),
               this.withAlpha(4886745, (int)(200.0F * var9)),
               this.withAlpha(2777000, (int)(200.0F * var9)),
               this.withAlpha(2777000, (int)(200.0F * var9))
            },
            1.0F
         );
         String var24 = "Новое обновление!";
         Fonts.BOLD.draw(var24, var10 + 12.0F, var11 + 7.0F, 6.5F, this.withAlpha(16777215, (int)(240.0F * var9)));
         String var25 = "v1.6.0 → v" + var5.version();
         Fonts.BOLD.draw(var25, var10 + 12.0F, var11 + 18.0F, 5.5F, this.withAlpha(6983624, (int)(200.0F * var9)));
         int var26 = this.withAlpha(this.lerpColor(1714746, 1981018, this.btnHover), (int)((150.0F + 50.0F * this.btnHover) * var9));
         int var27 = this.withAlpha(this.lerpColor(2771562, 4885192, this.btnHover), (int)((160.0F + 80.0F * this.btnHover) * var9));
         Render2D.rect(var18, var19, var16, var17, var26, 4.0F);
         Render2D.outline(var18, var19, var16, var17, 0.5F, var27, 4.0F);
         String var28 = "Обновить";
         float var29 = Fonts.BOLD.getWidth(var28, 6.0F);
         Fonts.BOLD
            .draw(
               var28,
               var18 + (var16 - var29) / 2.0F,
               var19 + (var17 - Fonts.BOLD.getHeight(6.0F)) / 2.0F,
               6.0F,
               this.withAlpha(this.lerpColor(11192552, 16777215, this.btnHover), (int)(240.0F * var9))
            );
         Render2D.rect(
            var14,
            var15,
            var13,
            var13,
            this.withAlpha(this.lerpColor(1974832, 3807256, this.closeHover), (int)((100.0F + 80.0F * this.closeHover) * var9)),
            3.0F
         );
         Render2D.outline(
            var14,
            var15,
            var13,
            var13,
            0.5F,
            this.withAlpha(this.lerpColor(3159365, 9056304, this.closeHover), (int)((130.0F + 80.0F * this.closeHover) * var9)),
            3.0F
         );
         String var30 = "x";
         float var31 = Fonts.BOLD.getWidth(var30, 5.5F);
         float var32 = Fonts.BOLD.getHeight(5.5F);
         Fonts.BOLD
            .draw(
               var30,
               var14 + (var13 - var31) / 2.0F,
               var15 + (var13 - var32) / 2.0F,
               5.5F,
               this.withAlpha(this.lerpColor(8425640, 16740464, this.closeHover), (int)(220.0F * var9))
            );
      }
   }

   public boolean mouseClicked(float var1, float var2, int var3, int var4, UpdateChecker.UpdateInfo var5) {
      if (!(this.alpha < 0.3F) && var5 != null) {
         float var6 = this.alpha;
         float var7 = var3 - 200.0F - 10.0F + (1.0F - var6) * 210.0F;
         float var8 = var4 - 52.0F - 10.0F;
         float var9 = 10.0F;
         float var10 = var7 + 200.0F - var9 - 5.0F;
         float var11 = var8 + 5.0F;
         if (this.isOver(var1, var2, var10, var11, var9, var9)) {
            this.hide();
            UpdateChecker.getInstance().dismiss();
            return true;
         } else {
            float var12 = 70.0F;
            float var13 = 16.0F;
            float var14 = var7 + 200.0F - var12 - 5.0F;
            float var15 = var8 + 52.0F - var13 - 6.0F;
            if (this.isOver(var1, var2, var14, var15, var12, var13)) {
               this.openUrl(var5.downloadUrl());
               this.hide();
               UpdateChecker.getInstance().dismiss();
               return true;
            } else {
               return this.isOver(var1, var2, var7, var8, 200.0F, 52.0F);
            }
         }
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
      return var1 & 16777215 | MathHelper.clamp(var2, 0, 255) << 24;
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
