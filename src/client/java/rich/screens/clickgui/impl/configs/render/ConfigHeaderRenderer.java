package rich.screens.clickgui.impl.configs.render;

import java.awt.Color;
import rich.screens.clickgui.impl.configs.handler.ConfigDataHandler;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;

public class ConfigHeaderRenderer {
   private final ConfigDataHandler dataHandler;

   public ConfigHeaderRenderer(ConfigDataHandler var1) {
      this.dataHandler = var1;
   }

   public void render(float var1, float var2, float var3, float var4, float var5) {
      Fonts.BOLD.draw("Configurations", var1 + 10.0F, var2 + 10.0F, 7.0F, new Color(255, 255, 255, (int)(200.0F * var5)).getRGB());
      this.renderCreateButton(var1, var2, var3, var4, var5);
      this.renderSeparator(var1, var2, var5);
   }

   private void renderCreateButton(float var1, float var2, float var3, float var4, float var5) {
      float var6 = var1 + 388.0F - 70.0F;
      float var7 = var2 + 8.0F;
      float var8 = 60.0F;
      float var9 = 16.0F;
      boolean var10 = var3 >= var6 && var3 <= var6 + var8 && var4 >= var7 && var4 <= var7 + var9;
      int var11 = (int)((var10 ? 40 : 25) * var5);
      int var12 = (int)((var10 ? 100 : 60) * var5);
      Render2D.rect(var6, var7, var8, var9, new Color(64, 64, 64, var11).getRGB(), 4.0F);
      Render2D.outline(var6, var7, var8, var9, 0.5F, new Color(100, 100, 100, var12).getRGB(), 4.0F);
      String var13 = this.dataHandler.isCreating() ? "Cancel" : "+ Create";
      float var14 = Fonts.BOLD.getWidth(var13, 5.0F);
      Fonts.BOLD.draw(var13, var6 + (var8 - var14) / 2.0F, var7 + 5.5F, 5.0F, new Color(180, 180, 180, (int)(255.0F * var5)).getRGB());
   }

   private void renderSeparator(float var1, float var2, float var3) {
      Render2D.rect(var1 + 10.0F, var2 + 28.0F, 368.0F, 0.5F, new Color(64, 64, 64, (int)(100.0F * var3)).getRGB(), 0.0F);
   }

   public boolean mouseClicked(double var1, double var3, int var5, float var6, float var7) {
      float var8 = var6 + 388.0F - 70.0F;
      float var9 = var7 + 8.0F;
      if (var1 >= var8 && var1 <= var8 + 60.0F && var3 >= var9 && var3 <= var9 + 16.0F && var5 == 0) {
         this.dataHandler.toggleCreating();
         return true;
      } else {
         return false;
      }
   }
}
