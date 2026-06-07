package rich.screens.clickgui.impl.background.search;

import java.awt.Color;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import rich.modules.module.ModuleStructure;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.shader.Scissor;

public class SearchRenderer {
   private final SearchHandler searchHandler;

   public SearchRenderer(SearchHandler var1) {
      this.searchHandler = var1;
   }

   public void render(DrawContext var1, float var2, float var3, float var4, float var5, float var6, float var7, int var8, float var9) {
      if (!(this.searchHandler.getSearchPanelAlpha() <= 0.01F)) {
         float var10 = var2 + 6.0F;
         float var11 = var3 + 6.0F;
         float var12 = var4 - 12.0F;
         float var13 = var5 - 12.0F;
         float var14 = this.searchHandler.getSearchPanelAlpha() * var9;
         this.renderPanelBackground(var10, var11, var12, var13, var14);
         List var15 = this.searchHandler.getSearchResults();
         if (var15.isEmpty()) {
            this.renderEmptyState(var10, var11, var12, var13, var14);
         } else {
            Scissor.enable(var10 + 3.0F, var11 + 3.0F, var12 - 6.0F, var13 - 6.0F, 2.0F);
            this.renderResults(var10, var11, var12, var13, var6, var7, var14);
            Scissor.disable();
            this.renderScrollIndicators(var10, var11, var12, var13, var14);
         }
      }
   }

   private void renderPanelBackground(float var1, float var2, float var3, float var4, float var5) {
      int var6 = (int)(15.0F * var5);
      int var7 = (int)(215.0F * var5);
      Render2D.rect(var1, var2, var3, var4, new Color(64, 64, 64, var6).getRGB(), 7.0F);
      Render2D.outline(var1, var2, var3, var4, 0.5F, new Color(55, 55, 55, var7).getRGB(), 7.0F);
   }

   private void renderEmptyState(float var1, float var2, float var3, float var4, float var5) {
      String var6 = this.searchHandler.getSearchText().isEmpty() ? "Start typing to search..." : "No modules found";
      float var7 = 6.0F;
      float var8 = Fonts.BOLD.getWidth(var6, var7);
      float var9 = Fonts.BOLD.getHeight(var7);
      float var10 = var1 + (var3 - var8) / 2.0F;
      float var11 = var2 + (var4 - var9) / 2.0F;
      Fonts.BOLD.draw(var6, var10, var11, var7, new Color(100, 100, 100, (int)(150.0F * var5)).getRGB());
   }

   private void renderResults(float var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      List var8 = this.searchHandler.getSearchResults();
      float var9 = var2 + 5.0F + this.searchHandler.getSearchScrollOffset();
      float var10 = this.searchHandler.getSearchResultHeight();
      int var11 = -1;

      for (int var12 = 0; var12 < var8.size(); var12++) {
         ModuleStructure var13 = (ModuleStructure)var8.get(var12);
         float var14 = var9 + var12 * (var10 + 2.0F);
         if (!(var14 + var10 < var2) && !(var14 > var2 + var4)) {
            float var15 = this.searchHandler.getSearchResultAnimations().getOrDefault(var13, 0.0F);
            float var16 = var15 * var7;
            if (!(var16 <= 0.01F)) {
               float var17 = (1.0F - var15) * 20.0F;
               boolean var18 = var5 >= var1 + 5.0F && var5 <= var1 + var3 - 5.0F && var6 >= var14 && var6 <= var14 + var10;
               if (var18) {
                  var11 = var12;
               }

               boolean var19 = var13 == this.searchHandler.getSelectedSearchModule();
               this.renderResultItem(var13, var1, var14, var3, var10, var17, var16, var18, var19);
            }
         }
      }

      this.searchHandler.setHoveredSearchIndex(var11);
   }

   private void renderResultItem(ModuleStructure var1, float var2, float var3, float var4, float var5, float var6, float var7, boolean var8, boolean var9) {
      Color var10;
      if (var9) {
         var10 = new Color(140, 140, 140, (int)(60.0F * var7));
      } else if (var8) {
         var10 = new Color(100, 100, 100, (int)(40.0F * var7));
      } else {
         var10 = new Color(64, 64, 64, (int)(25.0F * var7));
      }

      float var11 = var2 + 5.0F + var6;
      float var12 = var4 - 10.0F;
      Render2D.rect(var11, var3, var12, var5, var10.getRGB(), 5.0F);
      if (var9) {
         Render2D.outline(var11, var3, var12, var5, 0.5F, new Color(160, 160, 160, (int)(100.0F * var7)).getRGB(), 5.0F);
      }

      Color var13 = var1.isState() ? new Color(255, 255, 255, (int)(255.0F * var7)) : new Color(180, 180, 180, (int)(200.0F * var7));
      Fonts.BOLD.draw(var1.getName(), var11 + 5.0F, var3 + 3.0F, 6.0F, var13.getRGB());
      String var14 = var1.getCategory().getReadableName();
      Color var15 = new Color(140, 140, 140, (int)(180.0F * var7));
      Fonts.BOLD.draw(var14, var11 + 5.0F, var3 + 11.0F, 4.0F, var15.getRGB());
      if (var1.isState()) {
         float var16 = var11 + var12 - 10.0F;
         float var17 = var3 + var5 / 2.0F - 2.0F;
         Render2D.rect(var16, var17, 4.0F, 4.0F, new Color(100, 200, 100, (int)(200.0F * var7)).getRGB(), 2.0F);
      }
   }

   private void renderScrollIndicators(float var1, float var2, float var3, float var4, float var5) {
      List var6 = this.searchHandler.getSearchResults();
      float var7 = this.searchHandler.getSearchResultHeight();
      float var8 = Math.max(0.0F, var6.size() * (var7 + 2.0F) - var4 + 10.0F);
      if (var8 > 0.0F) {
         if (this.searchHandler.getSearchScrollOffset() < -0.5F) {
            for (int var9 = 0; var9 < 10; var9++) {
               float var10 = 60.0F * var5 * (1.0F - var9 / 10.0F);
               Render2D.rect(var1 + 3.0F, var2 + 3.0F + var9, var3 - 6.0F, 1.0F, new Color(20, 20, 20, (int)var10).getRGB(), 0.0F);
            }
         }

         if (this.searchHandler.getSearchScrollOffset() > -var8 + 0.5F) {
            for (int var11 = 0; var11 < 10; var11++) {
               float var12 = 60.0F * var5 * (var11 / 10.0F);
               Render2D.rect(var1 + 3.0F, var2 + var4 - 13.0F + var11, var3 - 6.0F, 1.0F, new Color(20, 20, 20, (int)var12).getRGB(), 0.0F);
            }
         }
      }
   }

   public ModuleStructure getModuleAtPosition(double var1, double var3, float var5, float var6, float var7, float var8, SearchHandler var9) {
      if (var9.isSearchActive() && !var9.getSearchResults().isEmpty()) {
         float var10 = var5 + 6.0F;
         float var11 = var6 + 6.0F;
         float var12 = var7 - 12.0F;
         float var13 = var8 - 12.0F;
         if (!(var1 < var10 + 5.0F) && !(var1 > var10 + var12 - 5.0F) && !(var3 < var11) && !(var3 > var11 + var13)) {
            float var14 = var11 + 5.0F + var9.getSearchScrollOffset();
            float var15 = var9.getSearchResultHeight();
            List var16 = var9.getSearchResults();

            for (int var17 = 0; var17 < var16.size(); var17++) {
               float var18 = var14 + var17 * (var15 + 2.0F);
               if (var3 >= var18 && var3 <= var18 + var15) {
                  return (ModuleStructure)var16.get(var17);
               }
            }

            return null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }
}
