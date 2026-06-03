package rich.screens.clickgui.impl.configs;

import java.awt.Color;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import net.minecraft.client.gui.DrawContext;
import rich.modules.module.category.ModuleCategory;
import rich.screens.clickgui.impl.configs.handler.ConfigAnimationHandler;
import rich.screens.clickgui.impl.configs.handler.ConfigDataHandler;
import rich.screens.clickgui.impl.configs.render.ConfigCreateBoxRenderer;
import rich.screens.clickgui.impl.configs.render.ConfigListRenderer;
import rich.screens.clickgui.impl.configs.render.ConfigNotificationRenderer;
import rich.screens.clickgui.impl.configs.render.ConfigSearchRenderer;
import rich.theme.ClientTheme;
import rich.util.config.impl.ConfigPath;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;

public class ConfigsRenderer {
   public static final float PANEL_X_OFFSET = 6.0F;
   public static final float PANEL_Y_OFFSET = 6.0F;
   public static final float PANEL_WIDTH = 388.0F;
   public static final float PANEL_HEIGHT = 238.0F;
   public static final float CORNER_RADIUS = 6.0F;
   private final ConfigAnimationHandler animationHandler = new ConfigAnimationHandler();
   private final ConfigDataHandler dataHandler = new ConfigDataHandler(this.animationHandler);
   private final ConfigNotificationRenderer notificationRenderer = new ConfigNotificationRenderer();
   private final ConfigListRenderer listRenderer = new ConfigListRenderer(this.animationHandler, this.dataHandler, this.notificationRenderer);
   private final ConfigCreateBoxRenderer createBoxRenderer = new ConfigCreateBoxRenderer(this.dataHandler, this.notificationRenderer);
   private final ConfigSearchRenderer searchRenderer = new ConfigSearchRenderer(this.dataHandler);
   private boolean wasActive = false;
   private float alpha = 0.0F;
   private long lastTime = System.currentTimeMillis();

   public void render(DrawContext var1, float var2, float var3, float var4, float var5, float var6, int var7, float var8, ModuleCategory var9) {
      long var10 = System.currentTimeMillis();
      float var12 = Math.min((float)(var10 - this.lastTime) / 1000.0F, 0.1F);
      this.lastTime = var10;
      boolean var13 = var9 == ModuleCategory.CONFIGS;
      if (var13 && !this.wasActive) {
         this.dataHandler.refreshConfigs();
         this.animationHandler.initItemAnimationsFromEntries(this.dataHandler.getConfigs());
      }

      this.wasActive = var13;
      ArrayList var14 = new ArrayList();

      for (ConfigDataHandler.ConfigEntry var16 : this.dataHandler.getConfigs()) {
         var14.add(var16.name);
      }

      this.animationHandler.update(var13, var14, this.dataHandler.isCreating());
      this.alpha = this.alpha + ((var13 ? 1.0F : 0.0F) - this.alpha) * 14.0F * var12;
      this.alpha = Math.max(0.0F, Math.min(1.0F, this.alpha));
      if (!(this.alpha < 0.01F)) {
         float var18 = var2 + 6.0F;
         float var19 = var3 + 6.0F;
         float var17 = var8 * this.alpha;
         this.renderPanel(var18, var19, var17);
         this.renderHeader(var18, var19, var4, var5, var17);
         this.searchRenderer.render(var18, var19, var4, var5, var17);
         this.listRenderer.render(var1, var18, var19, var4, var5, var7, var17);
         this.createBoxRenderer.render(var18, var19, var17);
         this.notificationRenderer.render(var18, var19, var17);
      }
   }

   private void renderPanel(float var1, float var2, float var3) {
      int[] var4 = ClientTheme.bgGradient((int)(180.0F * var3));
      Render2D.gradientRect(var1, var2, 388.0F, 238.0F, var4, 6.0F);
      Render2D.outline(var1, var2, 388.0F, 238.0F, 0.5F, ClientTheme.outline((int)(200.0F * var3)), 6.0F);
   }

   private void renderHeader(float var1, float var2, float var3, float var4, float var5) {
      Fonts.BOLD.draw("Configs", var1 + 10.0F, var2 + 10.0F, 7.0F, ClientTheme.text((int)(220.0F * var5)));
      float var6 = var1 + 388.0F - 90.0F;
      float var7 = var2 + 7.0F;
      boolean var8 = var3 >= var6 && var3 <= var6 + 58.0F && var4 >= var7 && var4 <= var7 + 18.0F;
      boolean var9 = this.dataHandler.isCreating();
      Render2D.rect(var6, var7, 58.0F, 18.0F, new Color(60, 60, 65, (int)((var8 ? 50 : 25) * var5)).getRGB(), 4.0F);
      Render2D.outline(var6, var7, 58.0F, 18.0F, 0.5F, new Color(100, 100, 110, (int)((var8 ? 120 : 60) * var5)).getRGB(), 4.0F);
      String var10 = var9 ? "Cancel" : "+ Create";
      float var11 = Fonts.BOLD.getWidth(var10, 5.0F);
      Fonts.BOLD.draw(var10, var6 + (58.0F - var11) / 2.0F, var7 + 5.5F, 5.0F, ClientTheme.text((int)(220.0F * var5)));
      float var12 = var1 + 388.0F - 28.0F;
      float var13 = var2 + 7.0F;
      boolean var14 = var3 >= var12 && var3 <= var12 + 18.0F && var4 >= var13 && var4 <= var13 + 18.0F;
      Render2D.rect(var12, var13, 18.0F, 18.0F, new Color(60, 60, 65, (int)((var14 ? 50 : 25) * var5)).getRGB(), 4.0F);
      Render2D.outline(var12, var13, 18.0F, 18.0F, 0.5F, ClientTheme.outline((int)((var14 ? 120 : 60) * var5)), 4.0F);
      float var15 = Fonts.GUI_ICONS.getWidth("B", 13.0F);
      Fonts.GUI_ICONS.draw("B", var12 + (18.0F - var15) / 2.0F, var13 + 2.5F, 13.0F, ClientTheme.text((int)((var14 ? 255 : 160) * var5)));
      Render2D.rect(var1 + 8.0F, var2 + 29.0F, 372.0F, 0.5F, ClientTheme.outline((int)(80.0F * var5)), 0.0F);
   }

   public boolean mouseClicked(float var1, float var2, int var3, float var4, float var5) {
      if (this.alpha < 0.3F) {
         return false;
      } else {
         float var6 = var4 + 6.0F;
         float var7 = var5 + 6.0F;
         float var8 = var6 + 388.0F - 28.0F;
         float var9 = var7 + 7.0F;
         if (var3 == 0 && var1 >= var8 && var1 <= var8 + 18.0F && var2 >= var9 && var2 <= var9 + 18.0F) {
            this.openConfigFolder();
            return true;
         } else {
            float var10 = var6 + 388.0F - 90.0F;
            float var11 = var7 + 7.0F;
            if (var3 == 0 && var1 >= var10 && var1 <= var10 + 58.0F && var2 >= var11 && var2 <= var11 + 18.0F) {
               this.dataHandler.toggleCreating();
               return true;
            } else if (this.searchRenderer.mouseClicked(var1, var2, var3, var6, var7)) {
               return true;
            } else {
               return this.createBoxRenderer.mouseClicked(var1, var2, var3, var6, var7) ? true : this.listRenderer.mouseClicked(var1, var2, var3, var6, var7);
            }
         }
      }
   }

   public boolean mouseScrolled(double var1, double var3, double var5, float var7, float var8) {
      if (this.alpha < 0.3F) {
         return false;
      }

      float var9 = var7 + 6.0F;
      float var10 = var8 + 6.0F;
      if (var1 >= var9 && var1 <= var9 + 388.0F && var3 >= var10 && var3 <= var10 + 238.0F) {
         float var11 = 158.0F;
         if (this.dataHandler.isCreating()) {
            var11 -= 70.0F;
         }

         this.dataHandler.handleScroll(var5, var11);
         return true;
      } else {
         return false;
      }
   }

   public boolean keyPressed(int var1) {
      if (this.dataHandler.isCreating()) {
         return this.createBoxRenderer.keyPressed(var1);
      }

      if (this.dataHandler.isSearchFocused()) {
         if (var1 == 259) {
            this.dataHandler.removeSearchChar();
            return true;
         } else if (var1 == 256) {
            this.dataHandler.setSearchFocused(false);
            return true;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public boolean charTyped(char var1) {
      if (this.dataHandler.isCreating()) {
         return this.createBoxRenderer.charTyped(var1);
      } else if (this.dataHandler.isSearchFocused()) {
         this.dataHandler.appendSearchChar(var1);
         return true;
      } else {
         return false;
      }
   }

   public boolean isEditing() {
      return this.dataHandler.isCreating() || this.dataHandler.isSearchFocused();
   }

   private void openConfigFolder() {
      try {
         Path var1 = ConfigPath.getConfigDirectory();
         if (!Files.exists(var1)) {
            Files.createDirectories(var1);
         }

         File var2 = var1.toFile();
         Runtime.getRuntime().exec(new String[]{"explorer.exe", var2.getAbsolutePath()});
      } catch (Exception var3) {
         var3.printStackTrace();
      }
   }
}
