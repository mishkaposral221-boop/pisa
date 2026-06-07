package rich.screens.clickgui.impl.configs.render;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.DrawContext;
import rich.screens.clickgui.impl.configs.handler.ConfigAnimationHandler;
import rich.screens.clickgui.impl.configs.handler.ConfigDataHandler;
import rich.theme.ClientTheme;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.shader.Scissor;

public class ConfigListRenderer {
   private static final float ITEM_H = 38.0F;
   private static final float ITEM_SPACING = 4.0F;
   private static final float SPEED = 0.15F;
   private final ConfigAnimationHandler animationHandler;
   private final ConfigDataHandler dataHandler;
   private final ConfigNotificationRenderer notificationRenderer;

   public ConfigListRenderer(ConfigAnimationHandler var1, ConfigDataHandler var2, ConfigNotificationRenderer var3) {
      this.animationHandler = var1;
      this.dataHandler = var2;
      this.notificationRenderer = var3;
   }

   public void render(DrawContext var1, float var2, float var3, float var4, float var5, int var6, float var7) {
      float var8 = var2 + 8.0F;
      float var9 = var3 + 53.0F;
      float var10 = 372.0F;
      float var11 = 177.0F;
      if (this.dataHandler.isCreating()) {
         var11 -= 76.0F;
      }

      this.dataHandler.updateScroll(0.016F);
      Scissor.enable(var8, var9, var10, var11, var6);
      List<ConfigDataHandler.ConfigEntry> var12 = this.dataHandler.getFilteredConfigs();
      float var13 = var9 + (float)this.dataHandler.getScrollOffset();

      for (ConfigDataHandler.ConfigEntry var15 : var12) {
         float var16 = this.animationHandler.getItemAppearAnimation(var15.name);
         if (var16 < 0.01F) {
            var13 += 42.0F;
         } else if (!(var13 + 38.0F < var9) && !(var13 > var9 + var11)) {
            float var17 = (1.0F - var16) * 12.0F;
            this.renderItem(var15, var8 + var17, var13, var10, var4, var5, var7 * var16);
            var13 += 42.0F;
         } else {
            var13 += 42.0F;
         }
      }

      if (var12.isEmpty()) {
         String var18 = this.dataHandler.getSearchQuery().isEmpty() ? "No configs yet" : "Nothing found";
         float var19 = Fonts.BOLD.getWidth(var18, 6.0F);
         Fonts.BOLD.draw(var18, var2 + (388.0F - var19) / 2.0F, var3 + 119.0F - 10.0F, 6.0F, ClientTheme.textSub((int)(120.0F * var7)));
      }

      Scissor.disable();
   }

   private void renderItem(ConfigDataHandler.ConfigEntry var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      boolean var8 = var5 >= var2 && var5 <= var2 + var4 && var6 >= var3 && var6 <= var3 + 38.0F;
      float var9 = this.animationHandler.getHoverAnimation(var1.name);
      var9 += ((var8 ? 1.0F : 0.0F) - var9) * 0.15F;
      this.animationHandler.setHoverAnimation(var1.name, var9);
      int var10 = (int)((20.0F + 15.0F * var9) * var7);
      int var11 = (int)(50.0F + 15.0F * var9);
      Render2D.rect(var2, var3, var4, 38.0F, new Color(var11, var11, var11 + 5, var10).getRGB(), 5.0F);
      if (var9 > 0.01F) {
         Render2D.outline(var2, var3, var4, 38.0F, 0.5F, ClientTheme.outline((int)(60.0F * var9 * var7)), 5.0F);
      }

      Fonts.BOLD.draw(var1.name, var2 + 10.0F, var3 + 8.0F, 6.0F, ClientTheme.text((int)(230.0F * var7)));
      String var12 = var1.date + "  •  " + var1.author;
      Fonts.BOLD.draw(var12, var2 + 10.0F, var3 + 22.0F, 5.0F, ClientTheme.textSub((int)(160.0F * var7)));
      float var13 = 20.0F;
      float var14 = var3 + (38.0F - var13) / 2.0F;
      float var15 = var2 + var4 - var13 - 6.0F;
      float var16 = var15 - var13 - 5.0F;
      this.renderBtn(var15, var14, var13, "O", 13.0F, var5, var6, this.animationHandler.getDeleteHoverAnimations(), var1.name, new Color(180, 70, 70), var7);
      this.renderBtn(var16, var14, var13, "P", 14.0F, var5, var6, this.animationHandler.getLoadHoverAnimations(), var1.name, new Color(70, 160, 90), var7);
   }

   private void renderBtn(
      float var1, float var2, float var3, String var4, float var5, float var6, float var7, Map<String, Float> var8, String var9, Color var10, float var11
   ) {
      boolean var12 = var6 >= var1 && var6 <= var1 + var3 && var7 >= var2 && var7 <= var2 + var3;
      float var13 = var8.getOrDefault(var9, 0.0F);
      var13 += ((var12 ? 1.0F : 0.0F) - var13) * 0.15F;
      var8.put(var9, var13);
      int var14 = (int)(50.0F + (var10.getRed() - 50) * var13);
      int var15 = (int)(50.0F + (var10.getGreen() - 50) * var13);
      int var16 = (int)(50.0F + (var10.getBlue() - 50) * var13);
      Render2D.rect(var1, var2, var3, var3, new Color(var14, var15, var16, (int)((25.0F + 30.0F * var13) * var11)).getRGB(), 4.0F);
      float var17 = Fonts.GUI_ICONS.getWidth(var4, var5);
      float var18 = var5 * 0.75F;
      Fonts.GUI_ICONS.draw(var4, var1 + (var3 - var17) / 2.0F, var2 + (var3 - var18) / 2.0F, var5, ClientTheme.text((int)((140.0F + 115.0F * var13) * var11)));
   }

   public boolean mouseClicked(double var1, double var3, int var5, float var6, float var7) {
      float var8 = var6 + 8.0F;
      float var9 = var7 + 53.0F;
      float var10 = 372.0F;
      float var11 = 177.0F;
      if (this.dataHandler.isCreating()) {
         var11 -= 76.0F;
      }

      if (!(var1 < var8) && !(var1 > var8 + var10) && !(var3 < var9) && !(var3 > var9 + var11)) {
         float var12 = var9 + (float)this.dataHandler.getScrollOffset();

         for (ConfigDataHandler.ConfigEntry var14 : this.dataHandler.getFilteredConfigs()) {
            float var15 = this.animationHandler.getItemAppearAnimation(var14.name);
            if (var15 < 0.5F) {
               var12 += 42.0F;
            } else {
               if (var3 >= var12 && var3 <= var12 + 38.0F) {
                  float var16 = 20.0F;
                  float var17 = var12 + (38.0F - var16) / 2.0F;
                  float var18 = var8 + var10 - var16 - 6.0F;
                  float var19 = var18 - var16 - 5.0F;
                  if (var5 == 0 && var1 >= var18 && var1 <= var18 + var16 && var3 >= var17 && var3 <= var17 + var16) {
                     if (this.dataHandler.deleteConfig(var14.name)) {
                        this.notificationRenderer.show("Deleted: " + var14.name, ConfigNotificationRenderer.NotificationType.SUCCESS);
                     }

                     return true;
                  }

                  if (var5 == 0 && var1 >= var19 && var1 <= var19 + var16 && var3 >= var17 && var3 <= var17 + var16) {
                     if (this.dataHandler.loadConfig(var14.name)) {
                        this.notificationRenderer.show("Loaded: " + var14.name, ConfigNotificationRenderer.NotificationType.SUCCESS);
                     } else {
                        this.notificationRenderer.show("Failed to load", ConfigNotificationRenderer.NotificationType.ERROR);
                     }

                     return true;
                  }

                  return true;
               }

               var12 += 42.0F;
            }
         }

         return false;
      } else {
         return false;
      }
   }
}
