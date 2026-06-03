package rich.screens.clickgui.impl.background.render;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import rich.modules.module.category.ModuleCategory;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;

public class CategoryRenderer {
   private static final ModuleCategory[] MAIN_CATEGORIES = new ModuleCategory[]{
      ModuleCategory.VISUALS, ModuleCategory.HUD, ModuleCategory.UTILITIES, ModuleCategory.THEMES, ModuleCategory.CONFIGS
   };
   private static final String[] MAIN_CATEGORY_NAMES = new String[]{"Visuals", "HUD", "Utilities", "Themes", "Configs"};
   private static final String[] MAIN_CATEGORY_ICONS = new String[]{"c", "d", "e", "f", "f"};
   private final Map<ModuleCategory, Float> categoryAnimations = new HashMap<>();
   private static final float ANIMATION_SPEED = 8.0F;
   private static final float MAX_OFFSET = 5.0F;
   private static final float BALL_SIZE = 3.0F;
   private static final float TEXT_SIZE = 6.0F;
   private static final float ICON_SIZE = 6.0F;
   private static final float ICON_SPACING = 4.0F;
   private static final float SECTION_TEXT_SIZE = 5.0F;

   public CategoryRenderer() {
      for (ModuleCategory var4 : MAIN_CATEGORIES) {
         this.categoryAnimations.put(var4, 0.0F);
      }
   }

   public void updateAnimations(ModuleCategory var1, float var2) {
      for (ModuleCategory var6 : MAIN_CATEGORIES) {
         this.updateCategoryAnimation(var6, var1, var2);
      }
   }

   private void updateCategoryAnimation(ModuleCategory var1, ModuleCategory var2, float var3) {
      float var4 = var1 == var2 ? 1.0F : 0.0F;
      float var5 = this.categoryAnimations.getOrDefault(var1, 0.0F);
      float var6 = var4 - var5;
      float var7 = var6 * 8.0F * var3;
      if (Math.abs(var6) < 0.001F) {
         this.categoryAnimations.put(var1, var4);
      } else {
         this.categoryAnimations.put(var1, var5 + var7);
      }
   }

   public void render(float var1, float var2, ModuleCategory var3, float var4) {
      this.renderSectionHeader(var1, var2 + 52.0F, "Категории", var4);
      this.renderMainCategories(var1, var2, var4);
   }

   private void renderSectionHeader(float var1, float var2, String var3, float var4) {
      float var5 = 18.0F;
      float var6 = Fonts.BOLD.getWidth(var3, 5.0F);
      float var7 = 65.0F;
      float var8 = var1 + 15.0F + (var7 - var6) / 2.0F;
      float var9 = var2 + 3.0F;
      int var10 = (int)(40.0F * var4);
      int var11 = (int)(100.0F * var4);
      Render2D.rect(var1 + 15.0F, var9, var5, 0.5F, new Color(255, 255, 255, var10).getRGB(), 0.0F);
      Render2D.rect(var1 + 15.0F + var7 - var5, var9, var5, 0.5F, new Color(255, 255, 255, var10).getRGB(), 0.0F);
      Fonts.BOLD.draw(var3, var8, var2, 5.0F, new Color(150, 150, 150, var11).getRGB());
   }

   private void renderMainCategories(float var1, float var2, float var3) {
      for (int var4 = 0; var4 < MAIN_CATEGORY_NAMES.length; var4++) {
         ModuleCategory var5 = MAIN_CATEGORIES[var4];
         float var6 = this.categoryAnimations.getOrDefault(var5, 0.0F);
         float var7 = var2 + 65.0F + var4 * 15.0F;
         this.renderCategoryItem(var1, var7, MAIN_CATEGORY_NAMES[var4], MAIN_CATEGORY_ICONS[var4], var6, var3);
      }
   }

   private void renderCategoryItem(float var1, float var2, String var3, String var4, float var5, float var6) {
      float var7 = var5 * 5.0F;
      short var8 = 128;
      short var9 = 255;
      int var10 = (int)(var8 + (var9 - var8) * var5);
      int var11 = (int)((128.0F + 127.0F * var5) * var6);
      Color var12 = new Color(var10, var10, var10, var11);
      float var13 = var1 + 17.0F + var7;
      float var14 = Fonts.CATEGORY_ICONS.getWidth(var4, 6.0F);
      float var15 = var13 + var14 + 4.0F;
      float var16 = Fonts.BOLD.getWidth(var3, 6.0F);
      Fonts.CATEGORY_ICONS.draw(var4, var13, var2 + 0.5F, 6.0F, var12.getRGB());
      if (var5 > 0.01F) {
         float var17 = (var14 + 4.0F + var16) * var5;
         float var18 = var5 * 60.0F * var6;
         Render2D.rect(var13, var2 + 9.0F, var17, 0.5F, new Color(255, 255, 255, (int)var18).getRGB(), 0.0F);
         float var19 = var5 * 200.0F * var6;
         float var20 = var1 + 12.0F;
         float var21 = var2 + 2.5F;
         Render2D.rect(var20, var21, 3.0F, 3.0F, new Color(255, 255, 255, (int)var19).getRGB(), 1.5F);
      }

      Fonts.BOLD.draw(var3, var15, var2, 6.0F, var12.getRGB());
   }

   public ModuleCategory getCategoryAtPosition(double var1, double var3, float var5, float var6) {
      if (!(var1 < var5 + 10.0F) && !(var1 > var5 + 95.0F)) {
         for (int var7 = 0; var7 < MAIN_CATEGORY_NAMES.length; var7++) {
            float var8 = 65.0F + var7 * 15.0F;
            if (var3 >= var6 + var8 && var3 <= var6 + var8 + 13.0F) {
               return MAIN_CATEGORIES[var7];
            }
         }

         return null;
      } else {
         return null;
      }
   }
}
