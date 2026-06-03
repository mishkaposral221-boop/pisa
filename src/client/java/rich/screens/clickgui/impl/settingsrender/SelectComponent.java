package rich.screens.clickgui.impl.settingsrender;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.DrawContext;
import rich.modules.module.setting.implement.SelectSetting;
import rich.util.interfaces.AbstractSettingComponent;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.shader.Scissor;

public class SelectComponent extends AbstractSettingComponent {
   private final SelectSetting selectSetting;
   private boolean expanded = false;
   private float expandAnimation = 0.0F;
   private float hoverAnimation = 0.0F;
   private float descScrollOffset = 0.0F;
   private boolean descScrollingRight = true;
   private long descScrollPauseTime = 0L;
   private float arrowRotation = 0.0F;
   private final Map<String, Float> optionHoverAnimations = new HashMap<>();
   private final Map<String, Float> selectAnimations = new HashMap<>();
   private String previousSelected = "";
   private float selectedTextAlpha = 1.0F;
   private float selectedTextSlide = 1.0F;
   private float newSelectedTextAlpha = 0.0F;
   private float newSelectedTextSlide = 0.0F;
   private String animatingFromText = "";
   private boolean isAnimatingSelection = false;
   private long lastUpdateTime = System.currentTimeMillis();
   private static final float ANIMATION_SPEED = 8.0F;
   private static final float COLLAPSE_SPEED = 15.0F;
   private static final float BOX_WIDTH = 65.0F;
   private static final float OPTION_HEIGHT = 14.0F;
   private static final long SCROLL_PAUSE_DURATION = 2000L;
   private static final float SCROLL_PIXELS_PER_SECOND = 20.0F;
   private static final float DESC_PADDING = 8.0F;
   private static final float SELECTION_ANIMATION_SPEED = 10.0F;

   public SelectComponent(SelectSetting var1) {
      super(var1);
      this.selectSetting = var1;
      this.previousSelected = var1.getSelected();

      for (String var3 : var1.getList()) {
         this.optionHoverAnimations.put(var3, 0.0F);
         this.selectAnimations.put(var3, var1.isSelected(var3) ? 1.0F : 0.0F);
      }
   }

   private float getDeltaTime() {
      long var1 = System.currentTimeMillis();
      float var3 = Math.min((float)(var1 - this.lastUpdateTime) / 1000.0F, 0.1F);
      this.lastUpdateTime = var1;
      return var3;
   }

   private float lerp(float var1, float var2, float var3) {
      float var4 = var2 - var1;
      return Math.abs(var4) < 0.001F ? var2 : var1 + var4 * Math.min(var3, 1.0F);
   }

   private void updateSelectionAnimation(float var1) {
      String var2 = this.selectSetting.getSelected();
      if (!var2.equals(this.previousSelected) && !this.isAnimatingSelection) {
         this.animatingFromText = this.previousSelected;
         this.isAnimatingSelection = true;
         this.selectedTextAlpha = 1.0F;
         this.selectedTextSlide = 1.0F;
         this.newSelectedTextAlpha = 0.0F;
         this.newSelectedTextSlide = 0.0F;
      }

      if (this.isAnimatingSelection) {
         this.selectedTextAlpha = this.lerp(this.selectedTextAlpha, 0.0F, var1 * 10.0F);
         this.selectedTextSlide = this.lerp(this.selectedTextSlide, 0.0F, var1 * 10.0F);
         if (this.selectedTextAlpha < 0.5F) {
            this.newSelectedTextAlpha = this.lerp(this.newSelectedTextAlpha, 1.0F, var1 * 10.0F);
            this.newSelectedTextSlide = this.lerp(this.newSelectedTextSlide, 1.0F, var1 * 10.0F);
         }

         if (this.newSelectedTextAlpha > 0.99F && this.newSelectedTextSlide > 0.99F) {
            this.isAnimatingSelection = false;
            this.previousSelected = var2;
            this.selectedTextAlpha = 1.0F;
            this.selectedTextSlide = 1.0F;
            this.newSelectedTextAlpha = 1.0F;
            this.newSelectedTextSlide = 1.0F;
         }
      } else {
         this.previousSelected = var2;
      }
   }

   @Override
   public void render(DrawContext var1, int var2, int var3, float var4) {
      float var5 = this.getDeltaTime();
      this.updateSelectionAnimation(var5);
      boolean var6 = this.isMainHover(var2, var3);
      this.hoverAnimation = this.lerp(this.hoverAnimation, var6 ? 1.0F : 0.0F, var5 * 8.0F);
      float var7 = this.expanded ? 8.0F : 15.0F;
      this.expandAnimation = this.lerp(this.expandAnimation, this.expanded ? 1.0F : 0.0F, var5 * var7);
      float var8 = this.expanded ? 90.0F : 0.0F;
      this.arrowRotation = this.lerp(this.arrowRotation, var8, var5 * 8.0F);
      Fonts.GUI_ICONS.draw("J", this.x - 0.5F, this.y + this.height / 2.0F - 8.5F, 9.0F, this.applyAlpha(new Color(210, 210, 210, 200)).getRGB());
      Fonts.BOLD
         .draw(this.selectSetting.getName(), this.x + 9.5F, this.y + this.height / 2.0F - 7.5F, 6.0F, this.applyAlpha(new Color(210, 210, 220, 200)).getRGB());
      String var9 = this.selectSetting.getDescription();
      if (var9 != null && !var9.isEmpty()) {
         this.renderScrollingDescription(var9, var5);
      }

      float var10 = this.x + this.width - 65.0F - 2.0F;
      float var11 = this.y + this.height / 2.0F - 5.0F;
      float var12 = 10.0F;
      int var13 = 25 + (int)(this.hoverAnimation * 15.0F);
      Render2D.rect(var10, var11, 65.0F, var12, this.applyAlpha(new Color(55, 55, 55, var13)).getRGB(), 3.0F);
      int var14 = 60 + (int)(this.hoverAnimation * 40.0F);
      Render2D.outline(var10, var11, 65.0F, var12, 0.5F, this.applyAlpha(new Color(155, 155, 155, var14)).getRGB(), 3.0F);
      this.renderAnimatedSelectedText(var10, var11, var12);
      this.renderArrowIcon(var10 + 65.0F - 8.0F, var11 + var12 / 2.0F - 4.0F);
      if (this.expandAnimation > 0.01F) {
         this.renderExpandedOptions(var1, var2, var3, var10, var11 + var12 + 2.0F, var5);
      }
   }

   private void renderArrowIcon(float var1, float var2) {
      int var3 = 120 + (int)(this.hoverAnimation * 60.0F);
      float var4 = var1 + 4.0F;
      float var5 = var2 + 4.0F;
      float var6 = (float)Math.toRadians(this.arrowRotation);
      float var7 = (float)Math.cos(var6);
      float var8 = (float)Math.sin(var6);
      float var9 = -4.0F;
      float var10 = -4.0F;
      float var11 = var4 + (var9 * var7 - var10 * var8);
      float var12 = var5 + (var9 * var8 + var10 * var7);
   }

   private void renderAnimatedSelectedText(float var1, float var2, float var3) {
      float var4 = 51.0F;
      float var5 = var2 + var3 / 2.0F - 2.5F;
      Scissor.enable(var1 + 2.0F, var2, var4 + 2.0F, var3, 2.0F);
      if (this.isAnimatingSelection) {
         if (this.selectedTextAlpha > 0.01F) {
            String var6 = this.truncateText(this.animatingFromText, var4);
            float var7 = (1.0F - this.selectedTextSlide) * -15.0F;
            int var8 = (int)(200.0F * this.selectedTextAlpha * this.alphaMultiplier);
            Fonts.BOLD.draw(var6, var1 + 4.0F + var7, var5, 5.0F, new Color(160, 160, 165, var8).getRGB());
         }

         if (this.newSelectedTextAlpha > 0.01F) {
            String var10 = this.selectSetting.getSelected();
            String var12 = this.truncateText(var10, var4);
            float var14 = (1.0F - this.newSelectedTextSlide) * 20.0F;
            int var9 = (int)(200.0F * this.newSelectedTextAlpha * this.alphaMultiplier);
            Fonts.BOLD.draw(var12, var1 + 4.0F + var14, var5, 5.0F, new Color(160, 160, 165, var9).getRGB());
         }
      } else {
         String var11 = this.selectSetting.getSelected();
         String var13 = this.truncateText(var11, var4);
         Fonts.BOLD.draw(var13, var1 + 4.0F, var5, 5.0F, this.applyAlpha(new Color(160, 160, 165, 200)).getRGB());
      }

      Scissor.disable();
   }

   private String truncateText(String var1, float var2) {
      if (Fonts.BOLD.getWidth(var1, 5.0F) <= var2) {
         return var1;
      }

      String var3 = var1;

      while (Fonts.BOLD.getWidth(var3 + "..", 5.0F) > var2 && var3.length() > 1) {
         var3 = var3.substring(0, var3.length() - 1);
      }

      return var3 + "..";
   }

   private void renderScrollingDescription(String var1, float var2) {
      float var3 = this.y + this.height / 2.0F + 0.5F;
      float var4 = this.x + this.width - 65.0F - 2.0F;
      float var5 = var4 - this.x - 8.0F;
      float var6 = Fonts.BOLD.getWidth(var1, 5.0F);
      if (var6 <= var5) {
         this.descScrollOffset = 0.0F;
         Fonts.BOLD.draw(var1, this.x + 0.5F, var3, 5.0F, this.applyAlpha(new Color(128, 128, 128, 128)).getRGB());
      } else {
         this.updateDescScrollAnimation(var2, var6, var5);
         float var7 = var6 - var5 + 5.0F;
         float var8 = this.descScrollOffset * var7;
         Scissor.enable(this.x, var3 - 2.0F, var5, 10.0F, 2.0F);
         Fonts.BOLD.draw(var1, this.x + 0.5F - var8, var3, 5.0F, this.applyAlpha(new Color(128, 128, 128, 128)).getRGB());
         Scissor.disable();
      }
   }

   private void updateDescScrollAnimation(float var1, float var2, float var3) {
      long var4 = System.currentTimeMillis();
      if (this.descScrollPauseTime > 0L) {
         if (var4 - this.descScrollPauseTime < 2000L) {
            return;
         }

         this.descScrollPauseTime = 0L;
      }

      float var6 = var2 - var3 + 5.0F;
      if (var6 <= 0.0F) {
         this.descScrollOffset = 0.0F;
      } else {
         float var7 = 20.0F / var6;
         if (this.descScrollingRight) {
            this.descScrollOffset += var1 * var7;
            if (this.descScrollOffset >= 1.0F) {
               this.descScrollOffset = 1.0F;
               this.descScrollingRight = false;
               this.descScrollPauseTime = var4;
            }
         } else {
            this.descScrollOffset -= var1 * var7;
            if (this.descScrollOffset <= 0.0F) {
               this.descScrollOffset = 0.0F;
               this.descScrollingRight = true;
               this.descScrollPauseTime = var4;
            }
         }
      }
   }

   private void renderExpandedOptions(DrawContext var1, int var2, int var3, float var4, float var5, float var6) {
      List var7 = this.selectSetting.getList();
      float var8 = var7.size() * 14.0F;
      float var9 = var8 * this.expandAnimation;
      float var10 = this.expandAnimation * this.alphaMultiplier;
      int var11 = (int)(200.0F * var10);
      Render2D.rect(var4, var5, 65.0F, var9, new Color(30, 30, 30, var11).getRGB(), 3.0F);
      int var12 = (int)(100.0F * var10);
      Render2D.outline(var4, var5, 65.0F, var9, 0.5F, new Color(80, 80, 85, var12).getRGB(), 3.0F);
      if (!(var9 < 1.0F)) {
         Scissor.enable(var4, var5, 65.0F, var9, 2.0F);
         float var13 = var5;

         for (int var14 = 0; var14 < var7.size(); var14++) {
            String var15 = (String)var7.getName(var14);
            boolean var16 = var2 >= var4 && var2 <= var4 + 65.0F && var3 >= var13 && var3 <= var13 + 14.0F && this.expandAnimation > 0.8F;
            float var17 = this.optionHoverAnimations.getOrDefault(var15, 0.0F);
            var17 = this.lerp(var17, var16 ? 1.0F : 0.0F, var6 * 8.0F);
            this.optionHoverAnimations.put(var15, var17);
            boolean var18 = this.selectSetting.isSelected(var15);
            float var19 = this.selectAnimations.getOrDefault(var15, 0.0F);
            var19 = this.lerp(var19, var18 ? 1.0F : 0.0F, var6 * 10.0F);
            this.selectAnimations.put(var15, var19);
            if (var17 > 0.01F) {
               int var20 = (int)(30.0F * var17 * var10);
               Render2D.rect(var4 + 2.0F, var13 + 1.0F, 61.0F, 12.0F, new Color(100, 100, 105, var20).getRGB(), 2.0F);
            }

            float var34 = 6.0F;
            float var21 = var4 + 5.0F;
            float var22 = var13 + 7.0F - var34 / 2.0F;
            int var23 = (int)((40.0F + var17 * 20.0F) * var10);
            Render2D.rect(var21, var22, var34, var34, new Color(55, 55, 60, var23).getRGB(), 2.0F);
            int var24 = (int)((80.0F + var17 * 40.0F) * var10);
            Render2D.outline(var21, var22, var34, var34, 0.5F, new Color(120, 120, 125, var24).getRGB(), 2.0F);
            if (var19 > 0.01F) {
               float var25 = (var34 - 2.0F) * var19;
               float var26 = var21 + (var34 - var25) / 2.0F;
               float var27 = var22 + (var34 - var25) / 2.0F;
               int var28 = (int)(220.0F * var19 * var10);
               Render2D.rect(var26, var27, var25, var25, new Color(140, 180, 160, var28).getRGB(), 1.5F);
            }

            float var35 = var21 + var34 + 4.0F;
            float var36 = var13 + 7.0F - 2.5F;
            float var37 = 65.0F - var34 - 14.0F;
            String var38 = var15;
            float var29 = Fonts.BOLD.getWidth(var15, 5.0F);
            if (var29 > var37) {
               while (Fonts.BOLD.getWidth(var38 + "..", 5.0F) > var37 && var38.length() > 1) {
                  var38 = var38.substring(0, var38.length() - 1);
               }

               var38 = var38 + "..";
            }

            int var30 = (int)(140.0F + var19 * 40.0F + var17 * 20.0F);
            int var31 = (int)(200.0F * var10);
            Fonts.BOLD.draw(var38, var35, var36, 5.0F, new Color(var30, var30, var30 + 5, var31).getRGB());
            var13 += 14.0F;
         }

         Scissor.disable();
      }
   }

   private boolean isMainHover(double var1, double var3) {
      float var5 = this.x + this.width - 65.0F - 2.0F;
      float var6 = this.y + this.height / 2.0F - 5.0F;
      float var7 = 10.0F;
      return var1 >= var5 && var1 <= var5 + 65.0F && var3 >= var6 && var3 <= var6 + var7;
   }

   @Override
   public boolean mouseClicked(double var1, double var3, int var5) {
      if (var5 == 0) {
         if (this.isMainHover(var1, var3)) {
            this.expanded = !this.expanded;
            return true;
         }

         if (this.expanded && this.expandAnimation > 0.8F) {
            float var6 = this.x + this.width - 65.0F - 2.0F;
            float var7 = this.y + this.height / 2.0F - 5.0F;
            float var8 = var7 + 10.0F + 2.0F;
            float var9 = var8;

            for (String var11 : this.selectSetting.getList()) {
               if (var1 >= var6 && var1 <= var6 + 65.0F && var3 >= var9 && var3 <= var9 + 14.0F) {
                  this.selectSetting.setSelected(var11);
                  this.expanded = false;
                  return true;
               }

               var9 += 14.0F;
            }
         }
      }

      return false;
   }

   @Override
   public void tick() {
   }

   @Override
   public boolean isHover(double var1, double var3) {
      return var1 >= this.x && var1 <= this.x + this.width && var3 >= this.y && var3 <= this.y + this.height;
   }

   public float getTotalHeight() {
      float var1 = this.height;
      float var2 = this.selectSetting.getList().size() * 14.0F * this.expandAnimation;
      return var1 + var2;
   }
}
