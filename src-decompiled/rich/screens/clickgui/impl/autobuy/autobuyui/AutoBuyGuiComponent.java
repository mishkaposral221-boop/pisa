package rich.screens.clickgui.impl.autobuy.autobuyui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1799;
import net.minecraft.class_332;
import org.joml.Matrix3x2fStack;
import rich.IMinecraft;
import rich.screens.clickgui.impl.autobuy.AutoBuyableItem;
import rich.screens.clickgui.impl.autobuy.items.ItemRegistry;
import rich.screens.clickgui.impl.autobuy.manager.AutoBuyManager;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.item.ItemRender;
import rich.util.render.shader.Scissor;

public class AutoBuyGuiComponent implements IMinecraft {
   private static final int FORCED_GUI_SCALE = 2;
   private float x;
   private float y;
   private float width;
   private float height;
   private float targetScroll = 0.0F;
   private float smoothScroll = 0.0F;
   private float slideOffsetX = 0.0F;
   private float targetSlideOffsetX = 0.0F;
   private boolean slidingOut = false;
   private static final float SLIDE_SPEED = 20.0F;
   private final Map<AutoBuyableItem, Float> toggleAnimations = new HashMap<>();
   private final Map<AutoBuyableItem, Float> hoverAnimations = new HashMap<>();
   private final Map<AutoBuyableItem, Float> enabledAnimations = new HashMap<>();
   private final AutoBuyManager autoBuyManager = AutoBuyManager.getInstance();
   private AutoBuyableItem hoveredItem = null;
   private AutoBuyableItem editingItem = null;
   private AutoBuyGuiComponent.EditField editingField = AutoBuyGuiComponent.EditField.NONE;
   private String inputText = "";
   private float cursorBlink = 0.0F;
   private long lastUpdateTime = System.currentTimeMillis();
   private float panelAlpha = 1.0F;
   private float currentScale = 1.0F;
   private static final float ITEM_HEIGHT = 22.0F;
   private static final float ITEM_SPACING = 3.0F;
   private static final float CATEGORY_HEIGHT = 18.0F;
   private static final float ANIM_SPEED = 11.0F;
   private static final String PRICE_LABEL = "Цена покупки: ";
   private static final String QUANTITY_LABEL = "Покупать от: ";
   private final List<AutoBuyGuiComponent.PendingIcon> pendingIcons = new ArrayList<>();
   private final List<AutoBuyGuiComponent.PendingContextIcon> pendingContextIcons = new ArrayList<>();

   private int getCurrentGuiScale() {
      int var1 = (Integer)mc.field_1690.method_42474().method_41753();
      if (var1 == 0) {
         var1 = mc.method_22683().method_4476(0, mc.method_1573());
      }

      return var1;
   }

   private float getScaleFactor() {
      return this.getCurrentGuiScale() / 2.0F;
   }

   public void position(float var1, float var2) {
      this.x = var1;
      this.y = var2;
   }

   public void size(float var1, float var2) {
      this.width = var1;
      this.height = var2;
   }

   public void setAlpha(float var1) {
      this.panelAlpha = var1;
   }

   public void startSlideOut() {
      this.slidingOut = true;
      this.targetSlideOffsetX = -(this.width + 100.0F);
   }

   public void startSlideIn() {
      this.slidingOut = false;
      this.targetSlideOffsetX = 0.0F;
   }

   public boolean isSlideComplete() {
      return Math.abs(this.slideOffsetX - this.targetSlideOffsetX) < 5.0F;
   }

   public boolean isSlidOut() {
      return this.slidingOut && this.isSlideComplete();
   }

   public void resetSlide() {
      this.slideOffsetX = 0.0F;
      this.targetSlideOffsetX = 0.0F;
      this.slidingOut = false;
   }

   public void setSlideInstant(float var1) {
      this.slideOffsetX = var1;
      this.targetSlideOffsetX = var1;
   }

   public boolean isEditing() {
      return this.editingItem != null && this.editingField != AutoBuyGuiComponent.EditField.NONE;
   }

   private boolean isHovered(double var1, double var3, float var5, float var6, float var7, float var8) {
      return var1 >= var5 && var1 <= var5 + var7 && var3 >= var6 && var3 <= var6 + var8;
   }

   private int clampAlpha(int var1) {
      return Math.max(0, Math.min(255, var1));
   }

   private int clampColor(int var1) {
      return Math.max(0, Math.min(255, var1));
   }

   private float easeOutCubic(float var1) {
      return 1.0F - (float)Math.pow(1.0F - var1, 3.0);
   }

   private float easeInOutCubic(float var1) {
      return var1 < 0.5F ? 4.0F * var1 * var1 * var1 : 1.0F - (float)Math.pow(-2.0F * var1 + 2.0F, 3.0) / 2.0F;
   }

   private float calculateSlideAlpha() {
      if (this.slideOffsetX >= 0.0F) {
         return 1.0F;
      }

      float var1 = this.width + 100.0F;
      float var2 = Math.abs(this.slideOffsetX) / var1;
      var2 = Math.max(0.0F, Math.min(1.0F, var2));
      return 1.0F - this.easeOutCubic(var2);
   }

   public void render(class_332 var1, float var2, float var3, float var4, int var5, float var6) {
      this.panelAlpha = var6;
      this.currentScale = 1.0F;
      long var7 = System.currentTimeMillis();
      float var9 = Math.min((float)(var7 - this.lastUpdateTime) / 1000.0F, 0.1F);
      this.lastUpdateTime = var7;
      float var10 = this.targetSlideOffsetX - this.slideOffsetX;
      if (Math.abs(var10) > 0.5F) {
         float var11 = 1.0F - Math.abs(var10) / (this.width + 100.0F);
         float var12 = 20.0F * (0.5F + 0.5F * this.easeOutCubic(var11));
         this.slideOffsetX += var10 * var12 * var9;
      } else {
         this.slideOffsetX = this.targetSlideOffsetX;
      }

      float var30 = this.calculateSlideAlpha();
      this.updateAnimations(var9);
      this.cursorBlink += var9 * 2.0F;
      if (this.cursorBlink > 1.0F) {
         this.cursorBlink--;
      }

      this.hoveredItem = null;
      this.pendingIcons.clear();
      this.pendingContextIcons.clear();
      float var31 = this.calculateContentHeight();
      float var13 = Math.max(0.0F, var31 - this.height + 10.0F);
      this.targetScroll = this.clamp(this.targetScroll, -var13, 0.0F);
      float var14 = this.targetScroll - this.smoothScroll;
      this.smoothScroll += var14 * 0.3F;
      if (Math.abs(var14) < 0.1F) {
         this.smoothScroll = this.targetScroll;
      }

      this.renderPanelBackground(var6, var30);
      float var15 = this.x + 3.0F;
      float var16 = this.y + 1.0F;
      float var17 = this.width - 6.0F;
      float var18 = this.height - 3.0F;
      Scissor.enable(var15, var16, var17, var18, 2.0F);
      float var19 = this.slideOffsetX;
      float var20 = var6 * var30;
      float var21 = this.y + 5.0F + this.smoothScroll;

      for (AutoBuyGuiComponent.CategoryItems var24 : this.getCategorizedItems()) {
         if (!var24.items.isEmpty()) {
            if (this.isInView(var21, 18.0F, var16, var18)) {
               this.renderCategoryHeader(this.x + 5.0F + var19, var21, this.width - 10.0F, var24.name, var20);
            }

            var21 += 18.0F;

            for (AutoBuyableItem var26 : var24.items) {
               if (this.isInView(var21, 22.0F, var16, var18)) {
                  this.renderItem(var1, var26, this.x + 5.0F + var19, var21, this.width - 10.0F, var2, var3, var6, var30);
               }

               var21 += 25.0F;
            }

            var21 += 8.0F;
         }
      }

      for (AutoBuyGuiComponent.PendingIcon var35 : this.pendingIcons) {
         ItemRender.drawItem(var35.stack, var35.x, var35.y, 1.0F, 1.0F);
      }

      this.pendingIcons.clear();
      Scissor.disable();
      float var34 = this.getScaleFactor();
      int var36 = (int)(var15 * var34);
      int var37 = (int)(var16 * var34);
      int var38 = (int)((var15 + var17) * var34);
      int var27 = (int)((var16 + var18) * var34);
      var1.method_44379(var36, var37, var38, var27);

      for (AutoBuyGuiComponent.PendingContextIcon var29 : this.pendingContextIcons) {
         this.drawItemWithScaleCompensation(var1, var29.stack, var29.x, var29.y, var29.scale, 1.0F, var34);
      }

      this.pendingContextIcons.clear();
      var1.method_44380();
   }

   private void drawItemWithScaleCompensation(class_332 var1, class_1799 var2, float var3, float var4, float var5, float var6, float var7) {
      if (!var2.method_7960() && !(var6 <= 0.01F)) {
         float var8 = 16.0F * var5;
         float var9 = var3 + var8 / 2.0F;
         float var10 = var4 + var8 / 2.0F;
         Matrix3x2fStack var11 = var1.method_51448();
         var11.pushMatrix();
         var11.translate(var9, var10);
         var11.scale(var5 * var7, var5 * var7);
         var11.translate(-8.0F, -8.0F);
         var1.method_51427(var2, 0, 0);
         var11.popMatrix();
      }
   }

   private boolean isInView(float var1, float var2, float var3, float var4) {
      float var5 = var1 + var2;
      float var6 = var3 + var4;
      return var5 > var3 && var1 < var6;
   }

   private void updateAnimations(float var1) {
      for (AutoBuyableItem var3 : ItemRegistry.getAllItems()) {
         float var4 = var3.isEnabled() ? 1.0F : 0.0F;
         float var5 = this.toggleAnimations.getOrDefault(var3, var3.isEnabled() ? 1.0F : 0.0F);
         float var6 = this.smoothLerp(var5, var4, 11.0F * var1);
         this.toggleAnimations.put(var3, var6);
         float var7 = var3.isEnabled() ? 1.0F : 0.0F;
         float var8 = this.enabledAnimations.getOrDefault(var3, var3.isEnabled() ? 1.0F : 0.0F);
         float var9 = this.smoothLerp(var8, var7, 11.0F * var1);
         this.enabledAnimations.put(var3, var9);
         boolean var10 = var3 == this.hoveredItem;
         float var11 = var10 ? 1.0F : 0.0F;
         float var12 = this.hoverAnimations.getOrDefault(var3, 0.0F);
         this.hoverAnimations.put(var3, this.smoothLerp(var12, var11, 11.0F * var1));
      }
   }

   private float smoothLerp(float var1, float var2, float var3) {
      float var4 = var2 - var1;
      return Math.abs(var4) < 0.001F ? var2 : var1 + var4 * this.clamp(var3, 0.0F, 1.0F);
   }

   private float clamp(float var1, float var2, float var3) {
      return Math.max(var2, Math.min(var3, var1));
   }

   private float calculateContentHeight() {
      float var1 = 5.0F;

      for (AutoBuyGuiComponent.CategoryItems var4 : this.getCategorizedItems()) {
         if (!var4.items.isEmpty()) {
            var1 += 18.0F;
            var1 += var4.items.size() * 25.0F;
            var1 += 8.0F;
         }
      }

      return var1;
   }

   private void renderPanelBackground(float var1, float var2) {
      float var3 = this.slidingOut ? var2 : 1.0F;
      int var4 = this.clampAlpha((int)(15.0F * var1 * var3));
      int var5 = this.clampAlpha((int)(215.0F * var1 * var3));
      if (var4 > 0) {
         Render2D.rect(this.x, this.y, this.width, this.height, new Color(64, 64, 64, var4).getRGB(), 7.0F);
         Render2D.outline(this.x, this.y, this.width, this.height, 0.5F, new Color(55, 55, 55, var5).getRGB(), 7.0F);
      }
   }

   private void renderCategoryHeader(float var1, float var2, float var3, String var4, float var5) {
      int var6 = this.clampAlpha((int)(180.0F * var5));
      float var7 = Fonts.BOLD.getWidth(var4, 5.0F);
      float var8 = (var3 - var7 - 16.0F) / 2.0F;
      int var9 = this.clampAlpha((int)(60.0F * var5));
      Render2D.rect(var1, var2 + 6.0F, var8, 0.5F, new Color(100, 100, 100, var9).getRGB(), 0.0F);
      Fonts.BOLD.draw(var4, var1 + var8 + 8.0F, var2 + 3.0F, 5.0F, new Color(160, 160, 160, var6).getRGB());
      Render2D.rect(var1 + var8 + var7 + 16.0F, var2 + 6.0F, var8, 0.5F, new Color(100, 100, 100, var9).getRGB(), 0.0F);
   }

   private void renderItem(class_332 var1, AutoBuyableItem var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9) {
      if (!(var8 <= 0.01F)) {
         float var10 = var8 * var9;
         float var11 = var3 - this.slideOffsetX;
         boolean var12 = this.isHovered(var6, var7, var11, var4, var5, 22.0F);
         if (var12 && !this.slidingOut) {
            this.hoveredItem = var2;
         }

         float var13 = this.toggleAnimations.getOrDefault(var2, var2.isEnabled() ? 1.0F : 0.0F);
         float var14 = this.enabledAnimations.getOrDefault(var2, var2.isEnabled() ? 1.0F : 0.0F);
         float var15 = this.hoverAnimations.getOrDefault(var2, 0.0F);
         float var16 = 0.5F + 0.5F * var14;
         int var17 = 64 + (int)(var15 * 36.0F);
         int var18 = this.clampColor((int)(var17 * var16));
         int var19 = this.clampColor((int)(var17 * var16));
         int var20 = this.clampColor((int)(var17 * var16));
         int var21 = this.clampAlpha((int)((25.0F + var15 * 15.0F) * var10));
         Render2D.rect(var3, var4, var5, 22.0F, new Color(var18, var19, var20, var21).getRGB(), 5.0F);
         byte var22 = 60;
         byte var23 = 80;
         int var24 = (int)((var22 + (var23 - var22) * var14 + var15 * 30.0F) * var10);
         int var25 = (int)(50.0F + 30.0F * var14 + 20.0F * var15);
         Render2D.outline(var3, var4, var5, 22.0F, 0.5F, new Color(var25, var25, var25, this.clampAlpha(var24)).getRGB(), 5.0F);
         float var26 = 16.0F;
         float var27 = var4 + (22.0F - var26) / 2.0F;
         float var28 = var3 + 2.0F;
         this.queueItemIcon(var2, var28, var27, var26);
         String var29 = var2.getDisplayName();
         int var30 = this.clampColor((int)(120.0F + 135.0F * var14));
         int var31 = this.clampAlpha((int)((120.0F + 135.0F * var14) * var10));
         Color var32 = new Color(var30, var30, var30, var31);
         Fonts.BOLD.draw(var29, var3 + 20.0F, var4 + 5.0F, 5.0F, var32.getRGB());
         boolean var33 = this.editingItem == var2 && this.editingField == AutoBuyGuiComponent.EditField.PRICE;
         float var34 = var3 + 20.0F;
         float var35 = var4 + 13.0F;
         int var36 = this.clampColor((int)(80.0F + 60.0F * var14));
         int var37 = this.clampAlpha((int)((100.0F + 80.0F * var14) * var10));
         Color var38 = new Color(var36, var36, var36, var37);
         Fonts.BOLD.draw("Цена покупки: ", var34, var35, 4.0F, var38.getRGB());
         float var39 = Fonts.BOLD.getWidth("Цена покупки: ", 4.0F);
         float var40 = var34 + var39;
         String var41;
         if (var33) {
            var41 = this.inputText;
            float var42 = (float)(Math.sin(this.cursorBlink * Math.PI * 2.0) * 0.5 + 0.5);
            if (var42 > 0.5F) {
               var41 = var41 + "|";
            }
         } else {
            var41 = String.valueOf(var2.getSettings().getBuyBelow());
         }

         int var54 = this.clampAlpha(var33 ? (int)(220.0F * var10) : (int)((100.0F + 80.0F * var14) * var10));
         Color var43 = var33 ? new Color(100, 200, 100, var54) : new Color(var36, var36, var36, var54);
         Fonts.BOLD.draw(var41, var40, var35, 4.0F, var43.getRGB());
         if (var2.getSettings().isCanHaveQuantity()) {
            boolean var44 = this.editingItem == var2 && this.editingField == AutoBuyGuiComponent.EditField.QUANTITY;
            float var45 = var39 + Fonts.BOLD.getWidth(var41, 4.0F);
            float var46 = var34 + var45 + 8.0F;
            Fonts.BOLD.draw("Покупать от: ", var46, var35, 4.0F, var38.getRGB());
            float var47 = Fonts.BOLD.getWidth("Покупать от: ", 4.0F);
            float var48 = var46 + var47;
            String var49;
            if (var44) {
               var49 = this.inputText;
               float var50 = (float)(Math.sin(this.cursorBlink * Math.PI * 2.0) * 0.5 + 0.5);
               if (var50 > 0.5F) {
                  var49 = var49 + "|";
               }
            } else {
               var49 = String.valueOf(var2.getSettings().getMinQuantity());
            }

            int var61 = this.clampAlpha(var44 ? (int)(220.0F * var10) : (int)((100.0F + 80.0F * var14) * var10));
            Color var51 = var44 ? new Color(100, 200, 100, var61) : new Color(var36, var36, var36, var61);
            Fonts.BOLD.draw(var49, var48, var35, 4.0F, var51.getRGB());
         }

         float var55 = 14.0F;
         float var56 = 8.0F;
         float var57 = var3 + var5 - var55 - 4.0F;
         float var58 = var4 + 11.0F - var56 / 2.0F;
         this.renderToggle(var57, var58, var55, var56, var13, var14, var10);
         float var59 = var57 - 8.0F;
         float var60 = var4 + 11.0F - 2.0F;
         int var62 = this.clampColor((int)(70.0F + 30.0F * var14));
         int var63 = this.clampColor((int)(70.0F + 130.0F * var14));
         int var52 = this.clampColor((int)(70.0F + 30.0F * var14));
         int var53 = this.clampAlpha((int)((80.0F + 120.0F * var14) * var10));
         Render2D.rect(var59, var60, 4.0F, 4.0F, new Color(var62, var63, var52, var53).getRGB(), 2.0F);
      }
   }

   private void queueItemIcon(AutoBuyableItem var1, float var2, float var3, float var4) {
      class_1799 var5 = var1.createItemStack();
      float var6 = var4 / 16.0F;
      this.pendingContextIcons.add(new AutoBuyGuiComponent.PendingContextIcon(var5, var2, var3, var6));
   }

   private void renderToggle(float var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      int var8 = this.clampColor((int)(40.0F + var5 * 40.0F));
      int var9 = this.clampColor((int)(40.0F + var5 * 110.0F));
      int var10 = this.clampColor((int)(45.0F + var5 * 30.0F));
      var8 = this.clampColor((int)(var8 * (0.5F + 0.5F * var6)));
      var9 = this.clampColor((int)(var9 * (0.5F + 0.5F * var6)));
      var10 = this.clampColor((int)(var10 * (0.5F + 0.5F * var6)));
      int var11 = this.clampAlpha((int)(160.0F * var7));
      Render2D.rect(var1, var2, var3, var4, new Color(var8, var9, var10, var11).getRGB(), var4 / 2.0F);
      float var12 = var4 - 2.0F;
      float var13 = var1 + 1.0F + var5 * (var3 - var12 - 2.0F);
      float var14 = var2 + 1.0F;
      int var15 = this.clampColor((int)(120.0F + 80.0F * var6));
      int var16 = this.clampAlpha((int)(220.0F * var7));
      Render2D.rect(var13, var14, var12, var12, new Color(var15, var15, var15, var16).getRGB(), var12 / 2.0F);
   }

   public boolean mouseClicked(double var1, double var3, int var5, float var6, float var7, float var8, float var9) {
      if (this.slidingOut) {
         return false;
      }

      if (!this.isHovered(var1, var3, var6, var7, var8, var9)) {
         if (this.isEditing()) {
            this.applyEdit();
         }

         return false;
      } else if (var5 != 0) {
         if (this.isEditing()) {
            this.applyEdit();
         }

         return true;
      } else {
         float var10 = var7 + 3.0F;
         float var11 = var9 - 6.0F;
         float var12 = var7 + 5.0F + this.smoothScroll;
         float var13 = var6 + 5.0F;
         float var14 = var8 - 10.0F;

         for (AutoBuyGuiComponent.CategoryItems var17 : this.getCategorizedItems()) {
            if (!var17.items.isEmpty()) {
               var12 += 18.0F;

               for (AutoBuyableItem var19 : var17.items) {
                  float var20 = var12;
                  boolean var21 = var20 + 22.0F > var10 && var20 < var10 + var11;
                  if (var21 && this.isHovered(var1, var3, var13, var20, var14, 22.0F)) {
                     float var22 = 14.0F;
                     float var23 = 8.0F;
                     float var24 = var13 + var14 - var22 - 4.0F;
                     float var25 = var20 + 11.0F - var23 / 2.0F;
                     float var26 = var24 - 15.0F;
                     float var27 = var25 - 10.0F;
                     float var28 = var22 + 30.0F;
                     float var29 = var23 + 20.0F;
                     if (this.isHovered(var1, var3, var26, var27, var28, var29)) {
                        if (this.isEditing()) {
                           this.applyEdit();
                        }

                        ItemRegistry.saveItemState(var19);
                        return true;
                     }

                     float var30 = var13 + 20.0F;
                     float var31 = var20 + 11.0F;
                     float var32 = Fonts.BOLD.getWidth("Цена покупки: ", 4.0F);
                     String var33 = String.valueOf(var19.getSettings().getBuyBelow());
                     float var34 = Fonts.BOLD.getWidth(var33, 4.0F);
                     float var35 = var30 + var32 - 3.0F;
                     float var36 = var34 + 10.0F;
                     if (this.isHovered(var1, var3, var35, var31 - 3.0F, var36, 12.0F)) {
                        if (this.isEditing()) {
                           this.applyEdit();
                        }

                        this.startEditing(var19, AutoBuyGuiComponent.EditField.PRICE);
                        return true;
                     }

                     if (var19.getSettings().isCanHaveQuantity()) {
                        float var37 = var32 + var34;
                        float var38 = var30 + var37 + 8.0F;
                        float var39 = Fonts.BOLD.getWidth("Покупать от: ", 4.0F);
                        String var40 = String.valueOf(var19.getSettings().getMinQuantity());
                        float var41 = Fonts.BOLD.getWidth(var40, 4.0F);
                        float var42 = var38 + var39 - 3.0F;
                        float var43 = var41 + 10.0F;
                        if (this.isHovered(var1, var3, var42, var31 - 3.0F, var43, 12.0F)) {
                           if (this.isEditing()) {
                              this.applyEdit();
                           }

                           this.startEditing(var19, AutoBuyGuiComponent.EditField.QUANTITY);
                           return true;
                        }
                     }

                     if (this.isEditing()) {
                        this.applyEdit();
                     }

                     return true;
                  }

                  var12 += 25.0F;
               }

               var12 += 8.0F;
            }
         }

         if (this.isEditing()) {
            this.applyEdit();
         }

         return true;
      }
   }

   private void startEditing(AutoBuyableItem var1, AutoBuyGuiComponent.EditField var2) {
      this.editingItem = var1;
      this.editingField = var2;
      this.cursorBlink = 0.0F;
      if (var2 == AutoBuyGuiComponent.EditField.PRICE) {
         this.inputText = String.valueOf(var1.getSettings().getBuyBelow());
      } else if (var2 == AutoBuyGuiComponent.EditField.QUANTITY) {
         this.inputText = String.valueOf(var1.getSettings().getMinQuantity());
      }
   }

   private void applyEdit() {
      if (this.editingItem != null && this.editingField != AutoBuyGuiComponent.EditField.NONE) {
         try {
            int var1 = Integer.parseInt(this.inputText);
            if (this.editingField == AutoBuyGuiComponent.EditField.PRICE) {
               this.editingItem.getSettings().setBuyBelow(Math.max(1, var1));
            } else if (this.editingField == AutoBuyGuiComponent.EditField.QUANTITY) {
               this.editingItem.getSettings().setMinQuantity(Math.max(1, Math.min(64, var1)));
            }

            ItemRegistry.saveItemSettings(this.editingItem);
         } catch (NumberFormatException var2) {
         }

         this.editingItem = null;
         this.editingField = AutoBuyGuiComponent.EditField.NONE;
         this.inputText = "";
      }
   }

   private void cancelEdit() {
      this.editingItem = null;
      this.editingField = AutoBuyGuiComponent.EditField.NONE;
      this.inputText = "";
   }

   public boolean keyPressed(int var1, int var2, int var3) {
      if (!this.isEditing()) {
         return false;
      } else if (var1 == 257 || var1 == 335) {
         this.applyEdit();
         return true;
      } else if (var1 == 256) {
         this.cancelEdit();
         return true;
      } else if (var1 == 259 && !this.inputText.isEmpty()) {
         this.inputText = this.inputText.substring(0, this.inputText.length() - 1);
         return true;
      } else {
         return true;
      }
   }

   public boolean charTyped(char var1, int var2) {
      if (!this.isEditing()) {
         return false;
      }

      if (Character.isDigit(var1)) {
         int var3 = this.editingField == AutoBuyGuiComponent.EditField.PRICE ? 9 : 2;
         if (this.inputText.length() < var3) {
            this.inputText = this.inputText + var1;
         }

         return true;
      } else {
         return true;
      }
   }

   public boolean mouseScrolled(double var1, double var3, double var5, float var7, float var8, float var9, float var10) {
      if (this.slidingOut) {
         return false;
      } else if (this.isHovered(var1, var3, var7, var8, var9, var10)) {
         this.targetScroll += (float)var5 * 25.0F;
         return true;
      } else {
         return false;
      }
   }

   public void resetHover() {
      this.hoveredItem = null;
   }

   public void resetPositions() {
      this.smoothScroll = this.targetScroll;
   }

   private List<AutoBuyGuiComponent.CategoryItems> getCategorizedItems() {
      ArrayList var1 = new ArrayList();
      var1.add(new AutoBuyGuiComponent.CategoryItems("Крушитель", ItemRegistry.getKrush()));
      var1.add(new AutoBuyGuiComponent.CategoryItems("Талисманы", ItemRegistry.getTalismans()));
      var1.add(new AutoBuyGuiComponent.CategoryItems("Сферы", ItemRegistry.getSpheres()));
      var1.add(new AutoBuyGuiComponent.CategoryItems("Разное", ItemRegistry.getMisc()));
      var1.add(new AutoBuyGuiComponent.CategoryItems("Донаторские", ItemRegistry.getDonator()));
      var1.add(new AutoBuyGuiComponent.CategoryItems("Зелья", ItemRegistry.getPotions()));
      return var1;
   }

   public float getX() {
      return this.x;
   }

   public float getY() {
      return this.y;
   }

   public float getWidth() {
      return this.width;
   }

   public float getHeight() {
      return this.height;
   }

   public float getTargetScroll() {
      return this.targetScroll;
   }

   public float getSmoothScroll() {
      return this.smoothScroll;
   }

   public float getSlideOffsetX() {
      return this.slideOffsetX;
   }

   public float getTargetSlideOffsetX() {
      return this.targetSlideOffsetX;
   }

   public boolean isSlidingOut() {
      return this.slidingOut;
   }

   public Map<AutoBuyableItem, Float> getToggleAnimations() {
      return this.toggleAnimations;
   }

   public Map<AutoBuyableItem, Float> getHoverAnimations() {
      return this.hoverAnimations;
   }

   public Map<AutoBuyableItem, Float> getEnabledAnimations() {
      return this.enabledAnimations;
   }

   public AutoBuyManager getAutoBuyManager() {
      return this.autoBuyManager;
   }

   public AutoBuyableItem getHoveredItem() {
      return this.hoveredItem;
   }

   public AutoBuyableItem getEditingItem() {
      return this.editingItem;
   }

   public AutoBuyGuiComponent.EditField getEditingField() {
      return this.editingField;
   }

   public String getInputText() {
      return this.inputText;
   }

   public float getCursorBlink() {
      return this.cursorBlink;
   }

   public long getLastUpdateTime() {
      return this.lastUpdateTime;
   }

   public float getPanelAlpha() {
      return this.panelAlpha;
   }

   public float getCurrentScale() {
      return this.currentScale;
   }

   public List<AutoBuyGuiComponent.PendingIcon> getPendingIcons() {
      return this.pendingIcons;
   }

   public List<AutoBuyGuiComponent.PendingContextIcon> getPendingContextIcons() {
      return this.pendingContextIcons;
   }

   public void setX(float var1) {
      this.x = var1;
   }

   public void setY(float var1) {
      this.y = var1;
   }

   public void setWidth(float var1) {
      this.width = var1;
   }

   public void setHeight(float var1) {
      this.height = var1;
   }

   public void setTargetScroll(float var1) {
      this.targetScroll = var1;
   }

   public void setSmoothScroll(float var1) {
      this.smoothScroll = var1;
   }

   public void setSlideOffsetX(float var1) {
      this.slideOffsetX = var1;
   }

   public void setTargetSlideOffsetX(float var1) {
      this.targetSlideOffsetX = var1;
   }

   public void setSlidingOut(boolean var1) {
      this.slidingOut = var1;
   }

   public void setHoveredItem(AutoBuyableItem var1) {
      this.hoveredItem = var1;
   }

   public void setEditingItem(AutoBuyableItem var1) {
      this.editingItem = var1;
   }

   public void setEditingField(AutoBuyGuiComponent.EditField var1) {
      this.editingField = var1;
   }

   public void setInputText(String var1) {
      this.inputText = var1;
   }

   public void setCursorBlink(float var1) {
      this.cursorBlink = var1;
   }

   public void setLastUpdateTime(long var1) {
      this.lastUpdateTime = var1;
   }

   public void setPanelAlpha(float var1) {
      this.panelAlpha = var1;
   }

   public void setCurrentScale(float var1) {
      this.currentScale = var1;
   }

   private static class CategoryItems {
      String name;
      List<AutoBuyableItem> items;

      CategoryItems(String var1, List<AutoBuyableItem> var2) {
         this.name = var1;
         this.items = var2 != null ? var2 : new ArrayList<>();
      }
   }

   public enum EditField {
      NONE,
      PRICE,
      QUANTITY;
   }

   private static class PendingContextIcon {
      class_1799 stack;
      float x;
      float y;
      float scale;

      PendingContextIcon(class_1799 var1, float var2, float var3, float var4) {
         this.stack = var1;
         this.x = var2;
         this.y = var3;
         this.scale = var4;
      }
   }

   private static class PendingIcon {
      class_1799 stack;
      float x;
      float y;

      PendingIcon(class_1799 var1, float var2, float var3) {
         this.stack = var1;
         this.x = var2;
         this.y = var3;
      }
   }
}
