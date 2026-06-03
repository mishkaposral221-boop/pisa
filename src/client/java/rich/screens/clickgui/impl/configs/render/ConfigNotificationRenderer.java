package rich.screens.clickgui.impl.configs.render;

import java.awt.Color;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;

public class ConfigNotificationRenderer {
   private String notification = null;
   private ConfigNotificationRenderer.NotificationType notificationType = ConfigNotificationRenderer.NotificationType.SUCCESS;
   private float notificationAlpha = 0.0F;
   private long notificationTime = 0L;
   private long lastUpdateTime = System.currentTimeMillis();

   public void render(float var1, float var2, float var3) {
      this.updateAnimation();
      if (this.notification != null && !(this.notificationAlpha < 0.01F)) {
         float var4 = var2 + 238.0F - 25.0F;
         float var5 = this.notificationAlpha * var3;
         Color var6 = this.notificationType.getBgColor();
         Color var7 = this.notificationType.getTextColor();
         float var8 = Fonts.BOLD.getWidth(this.notification, 5.0F);
         float var9 = var8 + 20.0F;
         float var10 = var1 + (388.0F - var9) / 2.0F;
         Render2D.rect(var10, var4, var9, 18.0F, new Color(var6.getRed(), var6.getGreen(), var6.getBlue(), (int)(60.0F * var5)).getRGB(), 4.0F);
         Fonts.BOLD
            .draw(this.notification, var10 + 10.0F, var4 + 6.0F, 5.0F, new Color(var7.getRed(), var7.getGreen(), var7.getBlue(), (int)(255.0F * var5)).getRGB());
      }
   }

   private void updateAnimation() {
      long var1 = System.currentTimeMillis();
      float var3 = Math.min((float)(var1 - this.lastUpdateTime) / 1000.0F, 0.1F);
      this.lastUpdateTime = var1;
      if (this.notification != null) {
         if (System.currentTimeMillis() - this.notificationTime < 2000L) {
            this.notificationAlpha = this.notificationAlpha + (1.0F - this.notificationAlpha) * 8.0F * var3;
         } else {
            this.notificationAlpha = this.notificationAlpha + (0.0F - this.notificationAlpha) * 4.0F * var3;
            if (this.notificationAlpha < 0.01F) {
               this.notification = null;
            }
         }
      }
   }

   public void show(String var1, ConfigNotificationRenderer.NotificationType var2) {
      this.notification = var1;
      this.notificationType = var2;
      this.notificationTime = System.currentTimeMillis();
      this.notificationAlpha = 0.0F;
   }

   public enum NotificationType {
      SUCCESS(new Color(60, 120, 60), new Color(180, 255, 180)),
      ERROR(new Color(120, 60, 60), new Color(255, 180, 180)),
      INFO(new Color(60, 100, 140), new Color(180, 220, 255));

      private final Color bgColor;
      private final Color textColor;

      NotificationType(Color var3, Color var4) {
         this.bgColor = var3;
         this.textColor = var4;
      }

      public Color getBgColor() {
         return this.bgColor;
      }

      public Color getTextColor() {
         return this.textColor;
      }
   }
}
