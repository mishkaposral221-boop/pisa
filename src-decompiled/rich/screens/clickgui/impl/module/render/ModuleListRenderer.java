package rich.screens.clickgui.impl.module.render;

import java.util.List;
import java.util.Map;
import net.minecraft.class_332;
import rich.modules.module.ModuleStructure;
import rich.screens.clickgui.impl.module.handler.ModuleAnimationHandler;
import rich.screens.clickgui.impl.module.handler.ModuleBindHandler;
import rich.screens.clickgui.impl.module.handler.ModuleScrollHandler;
import rich.screens.clickgui.impl.module.util.ModuleDisplayHelper;
import rich.theme.ClientTheme;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.shader.Scissor;

public class ModuleListRenderer {
   private static final float MODULE_ITEM_HEIGHT = 22.0F;
   private static final float MODULE_LIST_CORNER_RADIUS = 6.0F;
   private static final float CORNER_INSET = 3.0F;
   private static final float STATE_BALL_SIZE = 3.0F;
   private static final float STATE_TEXT_OFFSET = 6.0F;
   private static final float BIND_BOX_HEIGHT = 9.0F;
   private static final float BIND_BOX_MIN_WIDTH = 18.0F;
   private static final float BIND_BOX_PADDING = 6.0F;
   private static final float BIND_WIDTH_ANIM_SPEED = 12.0F;
   private static final float COL_GAP = 4.0F;
   private static final int COLUMNS = 2;
   private final ModuleAnimationHandler animationHandler;
   private final ModuleBindHandler bindHandler;
   private final ModuleDisplayHelper displayHelper;

   public ModuleListRenderer(ModuleAnimationHandler var1, ModuleBindHandler var2, ModuleDisplayHelper var3) {
      this.animationHandler = var1;
      this.bindHandler = var2;
      this.displayHelper = var3;
   }

   public void render(
      class_332 var1,
      List<ModuleStructure> var2,
      ModuleStructure var3,
      ModuleStructure var4,
      float var5,
      float var6,
      float var7,
      float var8,
      float var9,
      float var10,
      int var11,
      float var12,
      ModuleAnimationHandler var13,
      ModuleScrollHandler var14
   ) {
      int var15 = (int)(15.0F * var12);
      int var16 = (int)(215.0F * var12);
      Render2D.rect(var5, var6, var7, var8, ClientTheme.panel(var15), 6.0F);
      Render2D.outline(var5, var6, var7, var8, 0.5F, ClientTheme.outline(var16), 6.0F);
      float var17 = 3.0F;
      float var18 = 3.0F;
      float var19 = 3.0F;
      Scissor.enable(var5 + var19, var6 + var17 - 1.5F, var7 - var19 * 2.0F, var8 - var17 - var18 + 3.0F, var11);
      if (var13.isCategoryTransitioning() && !var13.getOldModules().isEmpty()) {
         float var20 = (1.0F - var13.getCategoryTransitionProgress()) * var12;
         float var21 = var13.easeInCubic(var13.getCategoryTransitionProgress()) * -var13.getCategorySlideDistance();
         float var22 = 1.0F - var13.getCategoryTransitionProgress() * 0.1F;
         this.renderModuleItems(
            var1,
            var13.getOldModules(),
            var13.getOldModuleAnimations(),
            var3,
            var4,
            var5,
            var6,
            var7,
            var8,
            var9,
            var10,
            var20,
            var21,
            var22,
            (float)var13.getOldModuleDisplayScroll(),
            false,
            var17,
            var18,
            var13
         );
      }

      float var24;
      float var25;
      float var26;
      if (var13.isCategoryTransitioning()) {
         float var23 = Math.max(0.0F, (var13.getCategoryTransitionProgress() - 0.2F) / 0.8F);
         var23 = var13.easeOutQuart(var23);
         var24 = var23 * var12;
         var25 = (1.0F - var23) * var13.getCategorySlideDistance();
         var26 = 0.9F + var23 * 0.1F;
      } else {
         var24 = var12;
         var25 = 0.0F;
         var26 = 1.0F;
      }

      this.renderModuleItems(
         var1,
         var2,
         var13.getModuleAnimations(),
         var3,
         var4,
         var5,
         var6,
         var7,
         var8,
         var9,
         var10,
         var24,
         var25,
         var26,
         (float)var14.getModuleDisplayScroll(),
         true,
         var17,
         var18,
         var13
      );
      Scissor.disable();
      this.renderScrollFade(
         var5, var6 + var17, var7, var8 - var17 - var18, var14.getModuleScrollTopFade() * var12, var14.getModuleScrollBottomFade() * var12, 80, 15
      );
   }

   private void renderModuleItems(
      class_332 var1,
      List<ModuleStructure> var2,
      Map<ModuleStructure, Float> var3,
      ModuleStructure var4,
      ModuleStructure var5,
      float var6,
      float var7,
      float var8,
      float var9,
      float var10,
      float var11,
      float var12,
      float var13,
      float var14,
      float var15,
      boolean var16,
      float var17,
      float var18,
      ModuleAnimationHandler var19
   ) {
      if (!(var12 <= 0.01F)) {
         float var20 = (var8 - 6.0F - 4.0F) / 2.0F;
         float var21 = var7 + var17 + 2.0F + var15;
         float var22 = var7 + var17;
         float var23 = var7 + var9 - var18;

         for (int var24 = 0; var24 < var2.size(); var24++) {
            ModuleStructure var25 = (ModuleStructure)var2.get(var24);
            int var26 = var24 % 2;
            int var27 = var24 / 2;
            float var28 = var6 + 3.0F + var26 * (var20 + 4.0F);
            float var29 = var21 + var27 * 24.0F;
            if (!(var29 + 22.0F < var22) && !(var29 > var23)) {
               float var30 = var3.getOrDefault(var25, 1.0F);
               float var31 = var19.getPositionAnimations().getOrDefault(var25, 1.0F);
               float var32 = var19.getModuleAlphaAnimations().getOrDefault(var25, 1.0F);
               float var33 = var30 * var12 * var32;
               if (!(var33 <= 0.01F)) {
                  float var34 = (1.0F - var30) * 20.0F;
                  float var35 = (1.0F - this.easeOutCubic(var31)) * 15.0F;
                  float var36 = 22.0F * var14;
                  float var37 = var20 * var14;
                  float var38 = var28 + var13 + var34 + var35;
                  float var39 = var7 + var17 + 2.0F + var15 + var27 * 24.0F;
                  boolean var40 = var16 && var25 == var4;
                  boolean var41 = var16 && var25 == var19.getHighlightedModule() && var19.getHighlightAnimation() > 0.01F;
                  float var42 = var16 ? var19.getHoverAnimations().getOrDefault(var25, 0.0F) : 0.0F;
                  float var43 = var16 ? var19.getStateAnimations().getOrDefault(var25, var25.isState() ? 1.0F : 0.0F) : (var25.isState() ? 1.0F : 0.0F);
                  float var44 = var16 ? var19.getSelectedIconAnimations().getOrDefault(var25, 0.0F) : 0.0F;
                  float var45 = var16 ? var19.getFavoriteAnimations().getOrDefault(var25, 0.0F) : 0.0F;
                  boolean var46 = this.displayHelper.hasSettings(var25);
                  int var48;
                  if (var40) {
                     int var47 = (int)((55.0F + var42 * 10.0F) * var33);
                     var48 = var47 << 24 | 4671303;
                  } else {
                     int var62 = (int)((25.0F + 20.0F * var42) * var33);
                     int var49 = (int)(64.0F + 36.0F * var42);
                     var48 = var62 << 24 | var49 << 16 | var49 << 8 | var49;
                  }

                  Render2D.rect(var38, var39, var37, var36, var48, 5.0F);
                  if (var40) {
                     float var63 = (float)(Math.sin(var19.getSelectedPulseAnimation()) * 0.5 + 0.5);
                     float var50 = var41 ? var19.getHighlightAnimation() * 0.5F : 0.0F;
                     int var51 = (int)((80.0F + 80.0F * var50 + (40.0F + 40.0F * var50) * var63) * var33);
                     int var52 = (int)Math.min(255.0F, 80.0F + 50.0F * var50 + 30.0F * var63);
                     Render2D.outline(var38, var39, var37, var36, 0.5F, var51 << 24 | var52 << 16 | var52 << 8 | var52, 5.0F);
                  } else if (var42 > 0.01F) {
                     int var64 = (int)(60.0F * var42 * var33);
                     Render2D.outline(var38, var39, var37, var36, 0.5F, var64 << 24 | 7895160, 5.0F);
                  }

                  float var65 = var43 * 6.0F;
                  if (var43 > 0.01F) {
                     int var66 = (int)(var43 * 200.0F * var33);
                     Render2D.rect(
                        var38 + 4.0F, var39 + (var36 - 3.0F * var14) / 2.0F + 1.0F, 3.0F * var14, 3.0F * var14, var66 << 24 | 16777215, 3.0F * var14 / 2.0F
                     );
                  }

                  short var67 = 128;
                  int var68 = (int)(var67 + (255 - var67) * var43);
                  int var70 = (int)((180.0F + 75.0F * var43) * var33);
                  if (var42 > 0.01F && var43 < 0.99F) {
                     var68 = (int)(var68 + 40.0F * var42 * (1.0F - var43));
                     var70 = (int)(var70 + 40.0F * var42 * (1.0F - var43));
                  }

                  if (var41) {
                     var68 = (int)Math.min(255.0F, var68 + 30.0F * var19.getHighlightAnimation());
                  }

                  var68 = Math.min(255, var68);
                  var70 = Math.min(255, var70);
                  int var53 = var70 << 24 | var68 << 16 | var68 << 8 | var68;
                  Fonts.BOLD.draw(var25.getName(), var38 + 5.0F + var65, var39 + (var36 - 6.0F * var14) / 2.0F, 6.0F * var14, var53);
                  if (var16) {
                     this.renderBindBox(var25, var5, var38, var39, var37, var36, var14, var33, var65, var19);
                     float var54 = var38 + var37 - 14.0F;
                     float var55 = var39 + (var36 - 8.0F * var14) / 2.0F;
                     float var56 = var46 ? var54 - 12.0F : var54;
                     int var57 = (int)(50.0F + 205.0F * var45);
                     int var58 = (int)(50.0F + 165.0F * var45);
                     int var59 = (int)(50.0F * (1.0F - var45));
                     int var60 = (int)((80.0F + 120.0F * var45 + 55.0F * var42) * var33);
                     Fonts.GUI_ICONS.draw("D", var56, var55 + 1.0F, 8.0F * var14, var60 << 24 | var57 << 16 | var58 << 8 | var59);
                     if (var46) {
                        if (var44 > 0.01F) {
                           int var61 = (int)(150.0F * var44 * var33);
                           Fonts.GUI_ICONS.draw("B", var54, var55 + 1.0F, 8.0F * var14, var61 << 24 | 13158600);
                        }

                        if (var44 < 0.99F) {
                           int var72 = (int)(120.0F * (1.0F - var44) * var33);
                           Fonts.BOLD.draw("...", var54 + 1.0F, var55 - 1.0F, 7.0F * var14, var72 << 24 | 9868950);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void renderBindBox(
      ModuleStructure var1,
      ModuleStructure var2,
      float var3,
      float var4,
      float var5,
      float var6,
      float var7,
      float var8,
      float var9,
      ModuleAnimationHandler var10
   ) {
      boolean var11 = var1 == var2;
      int var12 = var1.getKey();
      float var13 = var10.getBindBoxAlphaAnimations().getOrDefault(var1, 0.0F);
      if (!(var13 <= 0.01F) || var11 || var12 != -1 && var12 != -1) {
         String var14;
         if (var11) {
            var14 = "...";
         } else {
            var14 = this.bindHandler.getBindDisplayName(var12);
         }

         float var15 = Fonts.BOLD.getWidth(var14, 5.0F * var7);
         float var16 = Math.max(18.0F, var15 + 12.0F);
         float var17 = var10.getBindBoxWidthAnimations().getOrDefault(var1, var16);
         float var18 = var16 - var17;
         if (Math.abs(var18) > 0.1F) {
            var17 += var18 * 12.0F * 0.016F;
            var10.getBindBoxWidthAnimations().put(var1, var17);
         } else {
            var17 = var16;
            var10.getBindBoxWidthAnimations().put(var1, var17);
         }

         float var19 = 9.0F * var7;
         float var20 = var17 * var7 * var13;
         float var21 = Fonts.BOLD.getWidth(var1.getName(), 6.0F * var7);
         float var22 = var3 + 5.0F + var9 + var21;
         float var23 = var4 + (var6 - var19) / 2.0F + 0.5F;
         float var24 = var8 * var13;
         int var25 = (int)(30.0F * var24);
         Render2D.rect(var22 + 3.0F, var23 + 0.5F, var20 - 6.0F, var19, var25 << 24 | 3289655, 3.0F * var7);
         int var26 = (int)(60.0F * var24);
         Render2D.outline(var22 + 3.0F, var23 + 0.5F, var20 - 6.0F, var19, 0.5F, var26 << 24 | 5263445, 3.0F * var7);
         if (var13 > 0.5F) {
            int var27 = (int)(160.0F * var24);
            float var28 = var22 + (var20 - var15) / 2.0F;
            float var29 = var23 + (var19 - 5.0F * var7) / 2.0F;
            Fonts.BOLD.draw(var14, var28, var29, 5.0F * var7, var27 << 24 | 9211025);
         }
      }
   }

   private void renderScrollFade(float var1, float var2, float var3, float var4, float var5, float var6, int var7, int var8) {
      if (var5 > 0.01F) {
         int var9 = (int)(var7 * var5);
         Render2D.gradientRect(var1, var2, var3, var8, new int[]{var9 << 24 | 1315860, var9 << 24 | 1315860, 1315860, 1315860}, 0.0F);
      }

      if (var6 > 0.01F) {
         int var10 = (int)(var7 * var6);
         Render2D.gradientRect(var1, var2 + var4 - var8, var3, var8, new int[]{1315860, 1315860, var10 << 24 | 1315860, var10 << 24 | 1315860}, 0.0F);
      }
   }

   public ModuleStructure getModuleAtPosition(
      List<ModuleStructure> var1, double var2, double var4, float var6, float var7, float var8, float var9, double var10, boolean var12
   ) {
      if (var12) {
         return null;
      }

      if (!(var2 < var6) && !(var2 > var6 + var8) && !(var4 < var7) && !(var4 > var7 + var9)) {
         float var13 = (var8 - 6.0F - 4.0F) / 2.0F;
         float var14 = var7 + 3.0F + 2.0F + (float)var10;

         for (int var15 = 0; var15 < var1.size(); var15++) {
            int var16 = var15 % 2;
            int var17 = var15 / 2;
            float var18 = var6 + 3.0F + var16 * (var13 + 4.0F);
            float var19 = var14 + var17 * 24.0F;
            if (var2 >= var18 && var2 <= var18 + var13 && var4 >= var19 && var4 <= var19 + 22.0F) {
               return (ModuleStructure)var1.get(var15);
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public boolean isStarClicked(
      List<ModuleStructure> var1,
      double var2,
      double var4,
      float var6,
      float var7,
      float var8,
      float var9,
      double var10,
      ModuleDisplayHelper var12,
      boolean var13
   ) {
      if (var13) {
         return false;
      }

      float var14 = var7 + 3.0F + 2.0F + (float)var10;

      for (int var15 = 0; var15 < var1.size(); var15++) {
         ModuleStructure var16 = (ModuleStructure)var1.get(var15);
         float var17 = var14 + var15 * 24.0F;
         if (var4 >= var17 && var4 <= var17 + 22.0F) {
            float var18 = var8 - 6.0F;
            float var19 = var6 + 3.0F;
            boolean var20 = var12.hasSettings(var16);
            float var21;
            if (var20) {
               var21 = var19 + var18 - 14.0F - 12.0F;
            } else {
               var21 = var19 + var18 - 14.0F;
            }

            if (var2 >= var21 && var2 <= var21 + 10.0F) {
               return true;
            }
         }
      }

      return false;
   }

   public ModuleStructure getModuleForStarClick(
      List<ModuleStructure> var1,
      double var2,
      double var4,
      float var6,
      float var7,
      float var8,
      float var9,
      double var10,
      ModuleDisplayHelper var12,
      boolean var13
   ) {
      if (var13) {
         return null;
      }

      float var14 = (var8 - 6.0F - 4.0F) / 2.0F;
      float var15 = var7 + 3.0F + 2.0F + (float)var10;

      for (int var16 = 0; var16 < var1.size(); var16++) {
         ModuleStructure var17 = (ModuleStructure)var1.get(var16);
         int var18 = var16 % 2;
         int var19 = var16 / 2;
         float var20 = var6 + 3.0F + var18 * (var14 + 4.0F);
         float var21 = var15 + var19 * 24.0F;
         if (var4 >= var21 && var4 <= var21 + 22.0F) {
            boolean var22 = var12.hasSettings(var17);
            float var23 = var22 ? var20 + var14 - 14.0F - 12.0F : var20 + var14 - 14.0F;
            if (var2 >= var23 && var2 <= var23 + 10.0F) {
               return var17;
            }
         }
      }

      return null;
   }

   private float easeOutCubic(float var1) {
      return 1.0F - (float)Math.pow(1.0F - var1, 3.0);
   }
}
