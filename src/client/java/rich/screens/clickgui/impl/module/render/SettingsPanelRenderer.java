package rich.screens.clickgui.impl.module.render;

import java.util.List;
import net.minecraft.client.gui.DrawContext;
import rich.modules.module.ModuleStructure;
import rich.screens.clickgui.impl.module.handler.ModuleAnimationHandler;
import rich.screens.clickgui.impl.module.handler.ModuleScrollHandler;
import rich.screens.clickgui.impl.settingsrender.ColorComponent;
import rich.screens.clickgui.impl.settingsrender.MultiSelectComponent;
import rich.screens.clickgui.impl.settingsrender.SelectComponent;
import rich.theme.ClientTheme;
import rich.util.interfaces.AbstractSettingComponent;
import rich.util.profiler.FrameProfiler;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.shader.Scissor;

public class SettingsPanelRenderer {
   private static final float SETTINGS_PANEL_CORNER_RADIUS = 7.0F;
   private static final float CORNER_INSET = 3.0F;
   private static final int SETTING_HEIGHT = 16;
   private static final int SETTING_SPACING = 2;
   private final ModuleAnimationHandler animationHandler;

   public SettingsPanelRenderer(ModuleAnimationHandler var1) {
      this.animationHandler = var1;
   }

   public void render(
      DrawContext var1,
      ModuleStructure var2,
      List<AbstractSettingComponent> var3,
      float var4,
      float var5,
      float var6,
      float var7,
      float var8,
      float var9,
      float var10,
      int var11,
      float var12,
      ModuleScrollHandler var13,
      ModuleAnimationHandler var14
   ) {
      FrameProfiler var10000 = FrameProfiler.getInstance();
      boolean var10001 = var10000.isEnabled();
      if (var10001) {
         var10000.begin("ClickGui/settingsPanel/updateAnimations");
      }

      try {
         var14.updateSettingAnimations(var3);
         var14.updateVisibilityAnimations(var3);
      } finally {
         if (var10001) {
            var10000.end();
         }
      }

      int var15 = (int)(15.0F * var12);
      int var16 = (int)(215.0F * var12);
      Render2D.rect(var4, var5, var6, var7, ClientTheme.panel(var15), SETTINGS_PANEL_CORNER_RADIUS);
      Render2D.outline(var4, var5, var6, var7, 0.5F, ClientTheme.outline(var16), SETTINGS_PANEL_CORNER_RADIUS);
      if (var2 == null) {
         String var40 = "Select a module";
         float var42 = 6.0F;
         float var43 = Fonts.BOLD.getWidth(var40, var42);
         float var44 = Fonts.BOLD.getHeight(var42);
         Fonts.BOLD.draw(var40, var4 + (var6 - var43) / 2.0F, var5 + (var7 - var44) / 2.0F, var42, (int)(150.0F * var12) << 24 | 6579300);
      } else {
         Fonts.BOLD.draw(var2.getName(), var4 + 8.0F, var5 + 8.0F, 7.0F, (int)(200.0F * var12) << 24 | 16777215);
         String var17 = var2.getDescription();
         if (var17 != null && !var17.isEmpty()) {
            int var18 = (int)(150.0F * var12);
            Fonts.BOLD.draw(var17.length() > 52 ? var17.substring(0, 55) + "..." : var17, var4 + 15.0F, var5 + 20.0F, 5.0F, var18 << 24 | 8421504);
            Fonts.GUI_ICONS.draw("C", var4 + 8.0F, var5 + 20.0F, 6.0F, var18 << 24 | 8421504);
         }

         Render2D.rect(var4 + 8.0F, var5 + 30.0F, var6 - 16.0F, 1.25F, (int)(64.0F * var12) << 24 | 4210752, 10.0F);
         float var41 = CORNER_INSET;
         float var19 = 6.0F;
         float var20 = var5 + 31.0F;
         float var21 = var7 - 26.0F - var19;
         float var22 = var4 + var41;
         float var23 = var6 - var41 * 2.0F;
         float var45 = var20;
         float var46 = var20 + var21;
         boolean var48 = false;

         Scissor.enable(var22, var20, var23, var21, var11);
         try {
            float var26 = var5 + 38.0F + (float)var13.getSettingDisplayScroll();
            if (var10001) {
               var10000.begin("ClickGui/settingsPanel/renderVisibleComponents");
            }

            try {
               for (AbstractSettingComponent var49 : var3) {
                  float var33 = var14.getHeightAnimations().getOrDefault(var49, var49.getSetting().isVisible() ? 1.0F : 0.0F);
                  if (var33 <= 0.001F) {
                     continue;
                  }

                  float var34 = this.getComponentBaseHeight(var49) * var33;
                  float var52 = var26;
                  var26 += var34 + SETTING_SPACING * var33;
                  float var32 = var14.getVisibilityAnimations().getOrDefault(var49, var49.getSetting().isVisible() ? 1.0F : 0.0F);
                  if (var32 <= 0.001F && var33 <= 0.001F) {
                     continue;
                  }

                  if (var32 > 0.01F) {
                     var48 = true;
                  }

                  float var35 = var14.getSettingAnimations().getOrDefault(var49, 1.0F);
                  float var36 = var35 * var32 * var12;
                  var49.position(var4 + 8.0F, var52);
                  var49.size(var6 - 16.0F, SETTING_HEIGHT);
                  var49.setAlphaMultiplier(var36);
                  if (var52 + var34 >= var45 && var52 <= var46 && var36 > 0.01F) {
                     // One outer scissor is enough to clip the settings list. Avoiding one
                     // enable/disable pair per component prevents large GUI-driver stalls.
                     var1.getMatrices().pushMatrix();
                     if (var10001) {
                        var10000.begin("ClickGui/settingsPanel/component/" + var49.getClass().getSimpleName());
                     }

                     try {
                        var49.render(var1, (int)var8, (int)var9, var10);
                     } finally {
                        if (var10001) {
                           var10000.end();
                        }
                     }

                     var1.getMatrices().popMatrix();
                  }
               }
            } finally {
               if (var10001) {
                  var10000.end();
               }
            }
         } finally {
            Scissor.disable();
         }

         if (!var48) {
            String var51 = "This module doesn't have settings";
            float var54 = 6.0F;
            float var56 = Fonts.BOLD.getWidth(var51, var54);
            float var57 = Fonts.BOLD.getHeight(var54);
            Fonts.BOLD.draw(var51, var4 + (var6 - var56) / 2.0F, var5 + (var7 - var57) / 2.0F + 10.0F, var54, (int)(150.0F * var12) << 24 | 6579300);
         }

         this.renderScrollFade(
            var4 + var41, var20, var6 - var41 * 2.0F, var21, var13.getSettingScrollTopFade() * var12, var13.getSettingScrollBottomFade() * var12, 60, 12
         );
      }
   }

   public float calculateTotalHeight(List<AbstractSettingComponent> var1, ModuleAnimationHandler var2) {
      float var3 = 0.0F;

      for (AbstractSettingComponent var5 : var1) {
         float var6 = var2.getHeightAnimations().getOrDefault(var5, var5.getSetting().isVisible() ? 1.0F : 0.0F);
         if (!(var6 <= 0.001F)) {
            float var7 = this.getComponentBaseHeight(var5);
            var3 += (var7 + SETTING_SPACING) * var6;
         }
      }

      return var3;
   }

   private float getComponentBaseHeight(AbstractSettingComponent var1) {
      if (var1 instanceof SelectComponent) {
         return ((SelectComponent)var1).getTotalHeight();
      } else if (var1 instanceof MultiSelectComponent) {
         return ((MultiSelectComponent)var1).getTotalHeight();
      } else {
         return var1 instanceof ColorComponent ? ((ColorComponent)var1).getTotalHeight() : SETTING_HEIGHT;
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
}
