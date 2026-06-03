package rich.screens.clickgui.impl.configs.render;

import java.awt.Color;
import rich.screens.clickgui.impl.configs.handler.ConfigDataHandler;
import rich.theme.ClientTheme;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;

public class ConfigCreateBoxRenderer {
   private final ConfigDataHandler dataHandler;
   private final ConfigNotificationRenderer notificationRenderer;
   private float anim = 0.0F;
   private float cursorBlink = 0.0F;
   private long lastTime = System.currentTimeMillis();

   public ConfigCreateBoxRenderer(ConfigDataHandler var1, ConfigNotificationRenderer var2) {
      this.dataHandler = var1;
      this.notificationRenderer = var2;
   }

   public void render(float var1, float var2, float var3) {
      long var4 = System.currentTimeMillis();
      float var6 = Math.min((float)(var4 - this.lastTime) / 1000.0F, 0.1F);
      this.lastTime = var4;
      this.anim = this.anim + ((this.dataHandler.isCreating() ? 1.0F : 0.0F) - this.anim) * 14.0F * var6;
      this.cursorBlink += var6 * 2.0F;
      if (this.cursorBlink > 1.0F) {
         this.cursorBlink--;
      }

      if (!(this.anim < 0.01F)) {
         float var7 = 90.0F;
         float var8 = var2 + 238.0F - var7 - 4.0F;
         float var9 = var1 + 8.0F;
         float var10 = 372.0F;
         float var11 = this.anim * var3;
         Render2D.rect(var9, var8, var10, var7, new Color(35, 35, 40, (int)(50.0F * var11)).getRGB(), 5.0F);
         Render2D.outline(var9, var8, var10, var7, 0.5F, ClientTheme.outline((int)(120.0F * var11)), 5.0F);
         Render2D.rect(var9, var8, var10, 0.5F, ClientTheme.outline((int)(80.0F * var11)), 0.0F);
         float var12 = var10 - 20.0F;
         float var13 = (float)(Math.sin(this.cursorBlink * Math.PI * 2.0) * 0.5 + 0.5);
         float var14 = var8 + 8.0F;
         Fonts.BOLD.draw("Name", var9 + 4.0F, var14 + 4.0F, 5.0F, ClientTheme.textSub((int)(160.0F * var11)));
         float var15 = var9 + 36.0F;
         this.renderField(
            var15, var14, var12 - 36.0F, this.dataHandler.getNewConfigName(), "Config name...", !this.dataHandler.isAuthorFieldFocused(), var13, var11
         );
         float var16 = var8 + 30.0F;
         Fonts.BOLD.draw("Author", var9 + 4.0F, var16 + 4.0F, 5.0F, ClientTheme.textSub((int)(160.0F * var11)));
         float var17 = var9 + 36.0F;
         this.renderField(
            var17, var16, var12 - 36.0F, this.dataHandler.getNewConfigAuthor(), "Your name...", this.dataHandler.isAuthorFieldFocused(), var13, var11
         );
         float var18 = 62.0F;
         float var19 = var9 + (var10 - var18) / 2.0F;
         float var20 = var8 + 54.0F;
         Render2D.rect(var19, var20, var18, 20.0F, new Color(60, 120, 70, (int)(50.0F * var11)).getRGB(), 4.0F);
         Render2D.outline(var19, var20, var18, 20.0F, 0.5F, new Color(80, 180, 100, (int)(100.0F * var11)).getRGB(), 4.0F);
         float var21 = Fonts.BOLD.getWidth("Create", 5.0F);
         Fonts.BOLD.draw("Create", var19 + (var18 - var21) / 2.0F, var20 + 7.0F, 5.0F, new Color(160, 230, 170, (int)(255.0F * var11)).getRGB());
      }
   }

   private void renderField(float var1, float var2, float var3, String var4, String var5, boolean var6, float var7, float var8) {
      int var9 = new Color(40, 40, 45, (int)(50.0F * var8)).getRGB();
      int var10 = ClientTheme.outline((int)((var6 ? 180 : 70) * var8));
      Render2D.rect(var1, var2, var3, 16.0F, var9, 4.0F);
      Render2D.outline(var1, var2, var3, 16.0F, 0.5F, var10, 4.0F);
      if (var4.isEmpty()) {
         Fonts.BOLD.draw(var5, var1 + 5.0F, var2 + 5.0F, 5.0F, ClientTheme.textSub((int)(100.0F * var8)));
      } else {
         Fonts.BOLD.draw(var4, var1 + 5.0F, var2 + 5.0F, 5.0F, ClientTheme.text((int)(255.0F * var8)));
      }

      if (var6 && var7 > 0.3F) {
         float var11 = var1 + 5.0F + Fonts.BOLD.getWidth(var4, 5.0F);
         Render2D.rect(var11, var2 + 3.0F, 0.5F, 10.0F, ClientTheme.text((int)(255.0F * var7 * var8)), 0.0F);
      }
   }

   public boolean mouseClicked(double var1, double var3, int var5, float var6, float var7) {
      if (this.dataHandler.isCreating() && !(this.anim < 0.3F)) {
         float var8 = 90.0F;
         float var9 = var7 + 238.0F - var8 - 4.0F;
         float var10 = var6 + 8.0F;
         float var11 = 372.0F;
         float var12 = var11 - 20.0F - 36.0F;
         float var13 = var10 + 36.0F;
         float var14 = var9 + 8.0F;
         if (var1 >= var13 && var1 <= var13 + var12 && var3 >= var14 && var3 <= var14 + 16.0F) {
            this.dataHandler.setAuthorFieldFocused(false);
            return true;
         } else {
            float var15 = var10 + 36.0F;
            float var16 = var9 + 30.0F;
            if (var1 >= var15 && var1 <= var15 + var12 && var3 >= var16 && var3 <= var16 + 16.0F) {
               this.dataHandler.setAuthorFieldFocused(true);
               return true;
            } else {
               float var17 = 62.0F;
               float var18 = var10 + (var11 - var17) / 2.0F;
               float var19 = var9 + 54.0F;
               if (var5 == 0 && var1 >= var18 && var1 <= var18 + var17 && var3 >= var19 && var3 <= var19 + 20.0F) {
                  this.saveConfig();
                  return true;
               } else {
                  return false;
               }
            }
         }
      } else {
         return false;
      }
   }

   public boolean keyPressed(int var1) {
      if (!this.dataHandler.isCreating()) {
         return false;
      } else if (var1 == 259) {
         this.dataHandler.removeLastChar();
         return true;
      } else if (var1 == 257 || var1 == 335) {
         this.saveConfig();
         return true;
      } else if (var1 == 258) {
         this.dataHandler.setAuthorFieldFocused(!this.dataHandler.isAuthorFieldFocused());
         return true;
      } else if (var1 == 256) {
         this.dataHandler.toggleCreating();
         return true;
      } else {
         return false;
      }
   }

   public boolean charTyped(char var1) {
      if (!this.dataHandler.isCreating()) {
         return false;
      }

      this.dataHandler.appendChar(var1);
      return true;
   }

   private void saveConfig() {
      String var1 = this.dataHandler.getNewConfigName();
      if (var1.isEmpty()) {
         this.notificationRenderer.show("Enter a config name", ConfigNotificationRenderer.NotificationType.ERROR);
      } else if (var1.equalsIgnoreCase("autoconfig")) {
         this.notificationRenderer.show("This name is reserved", ConfigNotificationRenderer.NotificationType.ERROR);
      } else {
         if (this.dataHandler.saveConfig(var1, this.dataHandler.getNewConfigAuthor())) {
            this.notificationRenderer.show("Created: " + var1, ConfigNotificationRenderer.NotificationType.SUCCESS);
            this.dataHandler.clearNewConfig();
            this.dataHandler.setCreating(false);
         } else {
            this.notificationRenderer.show("Config already exists", ConfigNotificationRenderer.NotificationType.ERROR);
         }
      }
   }
}
