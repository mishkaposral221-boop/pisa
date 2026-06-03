package rich.screens.hud;

import java.awt.Color;
import net.minecraft.class_332;
import rich.client.draggables.AbstractHudElement;
import rich.modules.impl.render.Hud;
import rich.theme.ClientTheme;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;

public class Info extends AbstractHudElement {
   private double lastX = 0.0;
   private double lastZ = 0.0;
   private double currentBps = 0.0;
   private double displayBps = 0.0;
   private double targetBps = 0.0;
   private long lastUpdateTime = 0L;
   private static final double BPS_SMOOTHING = 0.05;
   private static final double DISPLAY_SMOOTHING = 0.03;

   public Info() {
      super("Info", 10, 0, 200, 26, true);
      this.startAnimation();
   }

   @Override
   public void tick() {
   }

   private double roundToStep(double var1, double var3) {
      return Math.round(var1 / var3) * var3;
   }

   @Override
   public void drawDraggable(class_332 var1, int var2) {
      if (var2 > 0 && this.mc.field_1724 != null) {
         boolean var3 = Hud.getInstance() != null && Hud.getInstance().showBps.isValue();
         long var4 = System.currentTimeMillis();
         double var6 = (var4 - this.lastUpdateTime) / 1000.0;
         if (this.lastUpdateTime > 0L && var6 > 0.0) {
            double var8 = this.mc.field_1724.method_23317() - this.lastX;
            double var10 = this.mc.field_1724.method_23321() - this.lastZ;
            double var12 = Math.sqrt(var8 * var8 + var10 * var10);
            this.currentBps = this.currentBps + (var12 / var6 - this.currentBps) * 0.05;
            this.targetBps = this.roundToStep(this.currentBps, 0.5);
         }

         this.displayBps = this.displayBps + (this.targetBps - this.displayBps) * 0.03;
         this.lastX = this.mc.field_1724.method_23317();
         this.lastZ = this.mc.field_1724.method_23321();
         this.lastUpdateTime = var4;
         float var32 = this.getX();
         float var9 = this.getY();
         int var33 = (int)this.mc.field_1724.method_23317();
         int var11 = (int)this.mc.field_1724.method_23318();
         int var34 = (int)this.mc.field_1724.method_23321();
         String var13 = String.valueOf(var33);
         String var14 = String.valueOf(var11);
         String var15 = String.valueOf(var34);
         String var16 = String.format("%.2f", this.roundToStep(this.displayBps, 0.5));
         float var17 = Fonts.BOLD.getWidth(var13, 6.0F);
         float var18 = Fonts.BOLD.getWidth(var14, 6.0F);
         float var19 = Fonts.BOLD.getWidth(var15, 6.0F);
         float var20 = Fonts.BOLD.getWidth(var16, 6.0F);
         float var21 = Fonts.BOLD.getWidth("b/s", 6.0F);
         float var22 = Fonts.BOLD.getWidth("x", 6.0F);
         float var23 = Fonts.BOLD.getWidth("y", 6.0F);
         float var24 = Fonts.BOLD.getWidth("z", 6.0F);
         float var25 = 30.0F + var22 + 2.0F + var17 + 12.0F + var23 + 2.0F + var18 + 12.0F + var24 + 2.0F + var19 + 10.0F;
         float var26 = 34.0F + var20 + 2.0F + var21 + 10.0F;
         int var27 = var3 ? (int)(var25 + var26) : (int)var25;
         this.setWidth(var27);
         this.setHeight(26);
         Render2D.gradientRect(var32, var9 + 3.0F, var25, 20.0F, ClientTheme.bgGradient(255), 5.0F);
         Render2D.outline(var32, var9 + 3.0F, var25, 20.0F, 0.35F, ClientTheme.outline(255), 5.0F);
         float var28 = var9 + 7.0F;
         Fonts.ICONSTYPETHO.draw("n", var32 + 5.0F, var28 + 0.5F, 11.0F, new Color(255, 255, 255, 255).getRGB());
         float var29 = var32 + 16.0F + 6.0F;
         Fonts.TEST.draw("»", var29, var28 + 1.5F, 8.0F, new Color(155, 155, 155, 255).getRGB());
         var29 += 8.0F;
         Fonts.BOLD.draw("x", var29, var28 + 3.0F, 6.0F, new Color(155, 155, 155, 255).getRGB());
         var29 += var22 + 2.0F;
         Fonts.BOLD.draw(var13, var29, var28 + 3.0F, 6.0F, new Color(255, 255, 255, 255).getRGB());
         var29 += var17;
         Fonts.TEST.draw("»", var29 + 4.0F, var28 + 1.5F, 8.0F, new Color(155, 155, 155, 255).getRGB());
         var29 += 12.0F;
         Fonts.BOLD.draw("y", var29, var28 + 3.0F, 6.0F, new Color(155, 155, 155, 255).getRGB());
         var29 += var23 + 2.0F;
         Fonts.BOLD.draw(var14, var29, var28 + 3.0F, 6.0F, new Color(255, 255, 255, 255).getRGB());
         var29 += var18;
         Fonts.TEST.draw("»", var29 + 4.0F, var28 + 1.5F, 8.0F, new Color(155, 155, 155, 255).getRGB());
         var29 += 12.0F;
         Fonts.BOLD.draw("z", var29, var28 + 3.0F, 6.0F, new Color(155, 155, 155, 255).getRGB());
         var29 += var24 + 2.0F;
         Fonts.BOLD.draw(var15, var29, var28 + 3.0F, 6.0F, new Color(255, 255, 255, 255).getRGB());
         if (var3) {
            float var30 = var32 + var25 + 4.0F;
            Render2D.gradientRect(var30, var9 + 3.0F, var26 - 4.0F, 20.0F, ClientTheme.bgGradient(255), 5.0F);
            Render2D.outline(var30, var9 + 3.0F, var26 - 4.0F, 20.0F, 0.35F, ClientTheme.outline(255), 5.0F);
            Fonts.ICONSTYPETHO.draw("l", var30 + 5.0F, var28 + 0.5F, 11.0F, new Color(255, 255, 255, 255).getRGB());
            float var31 = var30 + 16.0F + 6.0F;
            Fonts.TEST.draw("»", var31, var28 + 1.5F, 8.0F, new Color(155, 155, 155, 255).getRGB());
            var31 += 8.0F;
            Fonts.BOLD.draw(var16, var31, var28 + 3.0F, 6.0F, new Color(255, 255, 255, 255).getRGB());
            var31 += var20 + 2.0F;
            Fonts.BOLD.draw("b/s", var31, var28 + 3.0F, 6.0F, new Color(155, 155, 155, 255).getRGB());
         }
      }
   }
}
