package rich.theme;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_332;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.shader.Scissor;

public class ThemeRenderer {
   private static final ClientTheme.Theme[] THEMES = ClientTheme.Theme.values();
   private static final int COLS = 3;
   private static final float CARD_W = 118.0F;
   private static final float CARD_H = 60.0F;
   private static final float CARD_GAP_X = 8.0F;
   private static final float CARD_GAP_Y = 8.0F;
   private static final float RADIUS = 8.0F;
   private static final float SWATCH_SIZE = 8.0F;
   private static final float SWATCH_GAP = 4.0F;
   private final Map<ClientTheme.Theme, Float> hoverAnims = new HashMap<>();
   private final Map<ClientTheme.Theme, Float> selectAnims = new HashMap<>();
   private float scrollOffset = 0.0F;
   private float scrollTarget = 0.0F;

   public ThemeRenderer() {
      for (ClientTheme.Theme var4 : THEMES) {
         this.hoverAnims.put(var4, 0.0F);
         this.selectAnims.put(var4, var4 == ClientTheme.get() ? 1.0F : 0.0F);
      }
   }

   public void render(class_332 var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      float var9 = 0.016F;
      this.scrollOffset = this.scrollOffset + (this.scrollTarget - this.scrollOffset) * 0.15F;
      this.updateAnimations(var6, var7, var2, var3, var9);
      int var10 = (int)Math.ceil(THEMES.length / 3.0F);
      float var11 = 36.0F + var10 * 68.0F;
      float var12 = Math.max(0.0F, var11 - var5);
      this.scrollTarget = Math.max(-var12, Math.min(0.0F, this.scrollTarget));
      int var13 = (int)(15.0F * var8);
      int var14 = (int)(215.0F * var8);
      Render2D.rect(var2, var3, var4, var5, new Color(64, 64, 64, var13).getRGB(), 6.0F);
      Render2D.outline(var2, var3, var4, var5, 0.5F, new Color(55, 55, 55, var14).getRGB(), 6.0F);
      int var15 = (int)(180.0F * var8);
      Fonts.BOLD.draw("Themes", var2 + 8.0F, var3 + 8.0F, 7.0F, new Color(200, 200, 200, var15).getRGB());
      int var16 = (int)(100.0F * var8);
      Fonts.BOLD.draw("Select a colour theme for the client", var2 + 8.0F, var3 + 18.0F, 5.0F, new Color(120, 120, 120, var16).getRGB());
      float var17 = 30.0F * var8;
      Render2D.rect(var2 + 8.0F, var3 + 28.0F, var4 - 16.0F, 0.5F, new Color(255, 255, 255, (int)var17).getRGB(), 0.0F);
      float var18 = var2 + 8.0F;
      float var19 = var3 + 36.0F;
      Scissor.enable(var2 + 2.0F, var3 + 30.0F, var4 - 4.0F, var5 - 32.0F, 2.0F);

      for (int var20 = 0; var20 < THEMES.length; var20++) {
         ClientTheme.Theme var21 = THEMES[var20];
         int var22 = var20 % 3;
         int var23 = var20 / 3;
         float var24 = var18 + var22 * 126.0F;
         float var25 = var19 + var23 * 68.0F + this.scrollOffset;
         this.renderCard(var21, var24, var25, var8);
      }

      Scissor.disable();
   }

   private void renderCard(ClientTheme.Theme var1, float var2, float var3, float var4) {
      float var5 = this.hoverAnims.getOrDefault(var1, 0.0F);
      float var6 = this.selectAnims.getOrDefault(var1, 0.0F);
      boolean var7 = var1 == ClientTheme.get();
      Color var8 = var1.bgPrimary;
      Color var9 = var1.bgSecondary;
      int var10 = (int)((180.0F + 40.0F * var5) * var4);
      int[] var11 = new int[]{
         new Color(var8.getRed(), var8.getGreen(), var8.getBlue(), var10).getRGB(),
         new Color(var9.getRed(), var9.getGreen(), var9.getBlue(), var10).getRGB(),
         new Color(var8.getRed(), var8.getGreen(), var8.getBlue(), var10).getRGB(),
         new Color(var9.getRed(), var9.getGreen(), var9.getBlue(), var10).getRGB()
      };
      Render2D.gradientRect(var2, var3, 118.0F, 60.0F, var11, 8.0F);
      Color var12 = var1.panelOutline;
      float var13 = 0.4F + 0.4F * var5 + 0.2F * var6;
      int var14 = (int)(255.0F * var13 * var4);
      float var15 = 0.5F + var6 * 0.5F;
      Render2D.outline(var2, var3, 118.0F, 60.0F, var15, new Color(var12.getRed(), var12.getGreen(), var12.getBlue(), var14).getRGB(), 8.0F);
      if (var6 > 0.01F) {
         Color var16 = var1.textPrimary;
         int var17 = (int)(180.0F * var6 * var4);
         float var18 = 60.0F * var6;
         float var19 = var3 + (60.0F - var18) / 2.0F;
         Render2D.rect(var2 + 1.0F, var19, 2.0F, var18, new Color(var16.getRed(), var16.getGreen(), var16.getBlue(), var17).getRGB(), 1.0F);
      }

      Color var25 = var1.textPrimary;
      int var26 = (int)((180.0F + 75.0F * var6) * var4);
      Fonts.BOLD.draw(var1.name, var2 + 10.0F, var3 + 10.0F, 6.5F, new Color(var25.getRed(), var25.getGreen(), var25.getBlue(), var26).getRGB());
      if (var7) {
         int var27 = (int)(200.0F * var4);
         Color var29 = var1.textPrimary;
         float var20 = Fonts.BOLD.getWidth("Active", 4.5F) + 8.0F;
         float var21 = var2 + 118.0F - var20 - 6.0F;
         float var22 = var3 + 8.0F;
         Render2D.rect(var21, var22, var20, 10.0F, new Color(var29.getRed(), var29.getGreen(), var29.getBlue(), (int)(40.0F * var4)).getRGB(), 3.0F);
         Render2D.outline(var21, var22, var20, 10.0F, 0.5F, new Color(var29.getRed(), var29.getGreen(), var29.getBlue(), (int)(120.0F * var4)).getRGB(), 3.0F);
         Fonts.BOLD.draw("Active", var21 + 4.0F, var22 + 2.5F, 4.5F, new Color(var29.getRed(), var29.getGreen(), var29.getBlue(), var27).getRGB());
      }

      Color[] var28 = new Color[]{var1.bgPrimary, var1.bgSecondary, var1.panelBg, var1.panelOutline, var1.textPrimary, var1.textSecondary};
      float var30 = var3 + 60.0F - 8.0F - 8.0F;
      float var31 = var2 + 10.0F;

      for (int var32 = 0; var32 < var28.length; var32++) {
         Color var33 = var28[var32];
         int var23 = (int)(220.0F * var4);
         float var24 = var31 + var32 * 12.0F;
         Render2D.rect(var24, var30, 8.0F, 8.0F, new Color(var33.getRed(), var33.getGreen(), var33.getBlue(), var23).getRGB(), 4.0F);
         Render2D.outline(var24, var30, 8.0F, 8.0F, 0.35F, new Color(255, 255, 255, (int)(30.0F * var4)).getRGB(), 4.0F);
      }
   }

   private void updateAnimations(float var1, float var2, float var3, float var4, float var5) {
      float var6 = var3 + 8.0F;
      float var7 = var4 + 36.0F;
      float var8 = 10.0F;

      for (int var9 = 0; var9 < THEMES.length; var9++) {
         ClientTheme.Theme var10 = THEMES[var9];
         int var11 = var9 % 3;
         int var12 = var9 / 3;
         float var13 = var6 + var11 * 126.0F;
         float var14 = var7 + var12 * 68.0F + this.scrollOffset;
         boolean var15 = var1 >= var13 && var1 <= var13 + 118.0F && var2 >= var14 && var2 <= var14 + 60.0F;
         float var16 = var15 ? 1.0F : 0.0F;
         float var17 = this.hoverAnims.getOrDefault(var10, 0.0F);
         this.hoverAnims.put(var10, this.lerp(var17, var16, var5, var8));
         float var18 = var10 == ClientTheme.get() ? 1.0F : 0.0F;
         float var19 = this.selectAnims.getOrDefault(var10, 0.0F);
         this.selectAnims.put(var10, this.lerp(var19, var18, var5, var8));
      }
   }

   public void handleScroll(double var1) {
      this.scrollTarget += (float)var1 * 15.0F;
   }

   public boolean mouseClicked(float var1, float var2, float var3, float var4, int var5) {
      if (var5 != 0) {
         return false;
      }

      float var6 = var3 + 8.0F;
      float var7 = var4 + 36.0F;

      for (int var8 = 0; var8 < THEMES.length; var8++) {
         ClientTheme.Theme var9 = THEMES[var8];
         int var10 = var8 % 3;
         int var11 = var8 / 3;
         float var12 = var6 + var10 * 126.0F;
         float var13 = var7 + var11 * 68.0F + this.scrollOffset;
         if (var1 >= var12 && var1 <= var12 + 118.0F && var2 >= var13 && var2 <= var13 + 60.0F) {
            ClientTheme.set(var9);
            return true;
         }
      }

      return false;
   }

   private float lerp(float var1, float var2, float var3, float var4) {
      float var5 = (float)(1.0 - Math.pow(0.001, var3 * var4));
      float var6 = var1 + (var2 - var1) * var5;
      return Math.abs(var6 - var2) < 0.001F ? var2 : var6;
   }
}
