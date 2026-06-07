package rich.screens.clickgui.impl.background.render;

import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import rich.modules.impl.render.ClickGuiSettings;
import rich.theme.ClientTheme;
import rich.util.render.Render2D;

public class BackgroundRenderer {
   public void render(DrawContext var1, float var2, float var3, float var4) {
      ClickGuiSettings var5 = ClickGuiSettings.getInstance();
      float var6 = var5 != null ? var5.opacity.getValue() : 1.0F;
      boolean var7 = var5 == null || var5.blur.isValue();
      float var8 = var5 != null ? var5.blurStrength.getValue() : 8.0F;
      float var9 = var4 * var6;
      int var10 = (int)(255.0F * var9);
      if (var7) {
         Render2D.blur(var2, var3, 400.0F, 250.0F, var8, 15.0F, new Color(0, 0, 0, (int)(80.0F * var9)).getRGB());
      }

      Render2D.gradientRect(var2, var3, 400.0F, 250.0F, ClientTheme.bgGradient(var10), 15.0F);
      Render2D.outline(var2, var3, 400.0F, 250.0F, 0.5F, ClientTheme.outline((int)(200.0F * var9)), 15.0F);
   }

   public void renderCategoryPanel(float var1, float var2, float var3, float var4) {
      ClickGuiSettings var5 = ClickGuiSettings.getInstance();
      float var6 = var5 != null ? var5.opacity.getValue() : 1.0F;
      float var7 = var4 * var6;
      int var8 = (int)(25.0F * var7);
      int var9 = (int)(255.0F * var7);
      Render2D.rect(var1 + 7.5F, var2 + 7.5F, 80.0F, var3 - 15.0F, ClientTheme.panel(var8), 10.0F);
      Render2D.outline(var1 + 7.5F, var2 + 7.5F, 80.0F, var3 - 15.0F, 0.5F, ClientTheme.outline(var9), 10.0F);
   }
}
