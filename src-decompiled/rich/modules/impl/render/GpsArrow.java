package rich.modules.impl.render;

import java.awt.Color;
import net.minecraft.class_10799;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import rich.events.api.EventHandler;
import rich.events.impl.DrawEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.util.c;
import rich.util.render.font.Fonts;

public class GpsArrow extends ModuleStructure {
   private static volatile double targetX = 0.0;
   private static volatile double targetY = 0.0;
   private static volatile double targetZ = 0.0;
   private static volatile boolean active = false;
   private static volatile boolean needReset = false;
   private float smoothAngle = 0.0F;
   private boolean firstFrame = true;
   private static final class_2960 ARROW_TEXTURE = class_2960.method_60655("rich", "textures/world/arrow.png");

   public static GpsArrow getInstance() {
      return c.a(GpsArrow.class);
   }

   public static void setTarget(double var0, double var2, double var4) {
      targetX = var0;
      targetY = var2;
      targetZ = var4;
      active = true;
      needReset = true;
   }

   public static void clearTarget() {
      active = false;
   }

   public GpsArrow() {
      super("GpsArrow", "GPS стрелка навигации", ModuleCategory.VISUALS);
   }

   @EventHandler
   public void onDraw(DrawEvent var1) {
      if (active) {
         class_310 var2 = class_310.method_1551();
         if (var2.field_1724 != null && var2.field_1687 != null) {
            class_332 var3 = var1.getDrawContext();
            float var4 = var1.getPartialTicks();
            class_243 var5 = var2.field_1773.method_19418().method_71156();
            double var6 = targetX - var5.field_1352;
            double var8 = targetZ - var5.field_1350;
            double var10 = targetY - var5.field_1351;
            double var12 = Math.sqrt(var6 * var6 + var10 * var10 + var8 * var8);
            float var14 = var2.field_1724.method_5705(var4);
            float var15 = (float)Math.toDegrees(Math.atan2(-var6, var8));
            float var16 = var15 - var14;

            while (var16 > 180.0F) {
               var16 -= 360.0F;
            }

            while (var16 < -180.0F) {
               var16 += 360.0F;
            }

            if (!this.firstFrame && !needReset) {
               float var17 = var16 - this.smoothAngle;

               while (var17 > 180.0F) {
                  var17 -= 360.0F;
               }

               while (var17 < -180.0F) {
                  var17 += 360.0F;
               }

               this.smoothAngle += var17 * 0.2F;
            } else {
               this.smoothAngle = var16;
               this.firstFrame = false;
               needReset = false;
            }

            int var29 = var2.method_22683().method_4486();
            int var18 = var2.method_22683().method_4502();
            float var19 = var29 / 2.0F;
            float var20 = var18 / 2.0F;
            float var21 = var19;
            float var22 = var20 - 60.0F;
            this.drawArrow(var3, var21, var22, this.smoothAngle);
            String var23 = this.formatDistance(var12);
            float var24 = Fonts.BOLD.getWidth(var23, 7.0F);
            float var25 = var21 - var24 / 2.0F;
            float var26 = var22 + 16.0F;
            Fonts.BOLD.draw(var23, var25 + 0.5F, var26 + 0.5F, 7.0F, new Color(0, 0, 0, 120).getRGB());
            Fonts.BOLD.draw(var23, var25, var26, 7.0F, Color.WHITE.getRGB());
            String var27 = (int)targetX + " " + (int)targetZ;
            float var28 = Fonts.BOLD.getWidth(var27, 5.0F);
            Fonts.BOLD.draw(var27, var21 - var28 / 2.0F + 0.5F, var26 + 10.5F, 5.0F, new Color(0, 0, 0, 100).getRGB());
            Fonts.BOLD.draw(var27, var21 - var28 / 2.0F, var26 + 10.0F, 5.0F, new Color(180, 180, 180, 200).getRGB());
         }
      }
   }

   private void drawArrow(class_332 var1, float var2, float var3, float var4) {
      float var5 = 20.0F;
      float var6 = var5 / 2.0F;
      var1.method_51448().pushMatrix();
      var1.method_51448().translate(var2, var3);
      var1.method_51448().rotate((float)Math.toRadians(var4));
      var1.method_25291(
         class_10799.field_56883,
         ARROW_TEXTURE,
         (int)(-var6 + 1.0F),
         (int)(-var6 - 3.0F),
         0.0F,
         0.0F,
         (int)var5,
         (int)var5,
         (int)var5,
         (int)var5,
         new Color(255, 255, 255, 220).getRGB()
      );
      var1.method_51448().popMatrix();
   }

   private String formatDistance(double var1) {
      return var1 >= 1000.0 ? String.format("%.1fk блоков", var1 / 1000.0) : (int)var1 + " блоков";
   }
}
