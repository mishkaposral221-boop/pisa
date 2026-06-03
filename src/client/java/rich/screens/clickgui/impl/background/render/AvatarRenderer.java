package rich.screens.clickgui.impl.background.render;

import java.awt.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import rich.Initialization;
import rich.modules.impl.player.NameProtect;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.shader.Scissor;

public class AvatarRenderer {
   private static final int FORCED_GUI_SCALE = 2;
   private static final MinecraftClient mc = MinecraftClient.getInstance();

   public void render(DrawContext var1, float var2, float var3, float var4) {
      int var5 = (int)(255.0F * var4);
      int var6 = (int)(105.0F * var4);
      int var7 = (int)(200.0F * var4);
      NameProtect var8 = Initialization.getInstance().getManager().getModuleProvider().get(NameProtect.class);
      String var9 = var8.isState() ? "Protected" : mc.getSession().getUsername();
      Render2D.rect(var2 + 12.5F, var3 + 12.5F, 70.0F, 30.0F, new Color(30, 30, 35, var6).getRGB(), 7.0F);
      Render2D.rect(var2 + 15.0F, var3 + 15.0F, 25.0F, 25.0F, new Color(42, 42, 42, var5).getRGB(), 15.0F);
      Render2D.rect(var2 + 33.0F, var3 + 33.0F, 5.0F, 5.0F, new Color(0, 255, 0, var5).getRGB(), 10.0F);
      float var10 = var2 + 44.0F;
      float var11 = var3 + 22.0F;
      float var12 = 35.0F;
      float var13 = 14.0F;
      Scissor.enable(var10, var11 - 2.0F, var12, var13, 2.0F);
      Fonts.BOLD.draw(var9, var10, var11, 6.0F, new Color(255, 255, 255, var7).getRGB());
      Scissor.disable();
   }
}
