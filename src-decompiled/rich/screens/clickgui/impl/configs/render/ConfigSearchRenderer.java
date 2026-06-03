package rich.screens.clickgui.impl.configs.render;

import java.awt.Color;
import rich.screens.clickgui.impl.configs.handler.ConfigDataHandler;
import rich.theme.ClientTheme;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;

public class ConfigSearchRenderer {
   private final ConfigDataHandler dataHandler;
   private float cursorBlink = 0.0F;
   private long lastTime = System.currentTimeMillis();

   public ConfigSearchRenderer(ConfigDataHandler var1) {
      this.dataHandler = var1;
   }

   public void render(float var1, float var2, float var3, float var4, float var5) {
      long var6 = System.currentTimeMillis();
      float var8 = Math.min((float)(var6 - this.lastTime) / 1000.0F, 0.1F);
      this.lastTime = var6;
      this.cursorBlink += var8 * 2.0F;
      if (this.cursorBlink > 1.0F) {
         this.cursorBlink--;
      }

      float var9 = var1 + 8.0F;
      float var10 = var2 + 33.0F;
      float var11 = 372.0F;
      float var12 = 16.0F;
      boolean var13 = this.dataHandler.isSearchFocused();
      boolean var14 = var3 >= var9 && var3 <= var9 + var11 && var4 >= var10 && var4 <= var10 + var12;
      int var15 = new Color(40, 40, 45, (int)(40.0F * var5)).getRGB();
      int var16 = ClientTheme.outline((int)((var13 ? 160 : (var14 ? 100 : 60)) * var5));
      Render2D.rect(var9, var10, var11, var12, var15, 4.0F);
      Render2D.outline(var9, var10, var11, var12, 0.5F, var16, 4.0F);
      Fonts.BOLD.draw(">", var9 + 3.0F, var10 + 4.0F, 5.0F, ClientTheme.textSub((int)(150.0F * var5)));
      String var17 = this.dataHandler.getSearchQuery();
      if (var17.isEmpty() && !var13) {
         Fonts.BOLD.draw("Search configs...", var9 + 16.0F, var10 + 5.0F, 5.0F, ClientTheme.textSub((int)(120.0F * var5)));
      } else {
         Fonts.BOLD.draw(var17, var9 + 16.0F, var10 + 5.0F, 5.0F, ClientTheme.text((int)(255.0F * var5)));
         if (var13) {
            float var18 = (float)(Math.sin(this.cursorBlink * Math.PI * 2.0) * 0.5 + 0.5);
            if (var18 > 0.3F) {
               float var19 = var9 + 16.0F + Fonts.BOLD.getWidth(var17, 5.0F);
               Render2D.rect(var19, var10 + 3.0F, 0.5F, var12 - 6.0F, ClientTheme.text((int)(255.0F * var18 * var5)), 0.0F);
            }
         }
      }
   }

   public boolean mouseClicked(float var1, float var2, int var3, float var4, float var5) {
      float var6 = var4 + 8.0F;
      float var7 = var5 + 33.0F;
      float var8 = 372.0F;
      if (var3 == 0 && var1 >= var6 && var1 <= var6 + var8 && var2 >= var7 && var2 <= var7 + 16.0F) {
         this.dataHandler.setSearchFocused(true);
         return true;
      }

      if (this.dataHandler.isSearchFocused()) {
         this.dataHandler.setSearchFocused(false);
      }

      return false;
   }
}
