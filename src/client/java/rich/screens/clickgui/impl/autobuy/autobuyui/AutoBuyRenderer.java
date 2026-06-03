package rich.screens.clickgui.impl.autobuy.autobuyui;

import net.minecraft.client.gui.DrawContext;
import rich.IMinecraft;
import rich.modules.module.category.ModuleCategory;

public class AutoBuyRenderer implements IMinecraft {
   private final AutoBuyGuiComponent autoBuyComponent = new AutoBuyGuiComponent();
   private ModuleCategory lastCategory = null;
   private float categoryAlpha = 0.0F;
   private long lastUpdateTime = System.currentTimeMillis();
   private boolean wasActive = false;
   private boolean pendingSlideOut = false;
   private boolean slideOutComplete = false;
   private static final float FADE_SPEED = 14.0F;

   public void render(DrawContext var1, float var2, float var3, float var4, float var5, float var6, int var7, float var8, ModuleCategory var9) {
      long var10 = System.currentTimeMillis();
      float var12 = Math.min((float)(var10 - this.lastUpdateTime) / 1000.0F, 0.1F);
      this.lastUpdateTime = var10;
      boolean var13 = var9 == ModuleCategory.UTILITIES;
      if (this.wasActive && !var13 && !this.pendingSlideOut) {
         this.pendingSlideOut = true;
         this.slideOutComplete = false;
         this.autoBuyComponent.startSlideOut();
      }

      if (var13 && !this.wasActive) {
         this.pendingSlideOut = false;
         this.slideOutComplete = false;
         this.autoBuyComponent.resetSlide();
         this.autoBuyComponent.resetPositions();
      }

      if (this.pendingSlideOut && this.autoBuyComponent.isSlidOut()) {
         this.slideOutComplete = true;
         this.pendingSlideOut = false;
      }

      this.wasActive = var13;
      float var14;
      if (var13) {
         var14 = 1.0F;
      } else if (this.pendingSlideOut) {
         var14 = 1.0F;
      } else {
         var14 = 0.0F;
      }

      float var15 = var14 - this.categoryAlpha;
      if (Math.abs(var15) < 0.01F) {
         this.categoryAlpha = var14;
      } else {
         this.categoryAlpha += var15 * 14.0F * var12;
      }

      this.categoryAlpha = Math.max(0.0F, Math.min(1.0F, this.categoryAlpha));
      if (!(this.categoryAlpha <= 0.01F) || this.pendingSlideOut) {
         float var16 = var2 + 92.0F;
         float var17 = var3 + 38.0F;
         float var18 = 300.0F;
         float var19 = 204.0F;
         this.autoBuyComponent.position(var16, var17);
         this.autoBuyComponent.size(var18, var19);
         this.autoBuyComponent.resetHover();
         this.autoBuyComponent.render(var1, var4, var5, var6, var7, var8 * this.categoryAlpha);
      }
   }

   public boolean isSliding() {
      return this.pendingSlideOut && !this.slideOutComplete;
   }

   public void triggerSlideOut() {
      if (!this.autoBuyComponent.isSlidingOut()) {
         this.pendingSlideOut = true;
         this.slideOutComplete = false;
         this.autoBuyComponent.startSlideOut();
      }
   }

   public boolean isSlideOutComplete() {
      return this.slideOutComplete || this.autoBuyComponent.isSlidOut();
   }

   public void resetForClose() {
      this.pendingSlideOut = false;
      this.slideOutComplete = false;
      this.autoBuyComponent.resetSlide();
      this.categoryAlpha = 0.0F;
      this.wasActive = false;
   }

   public boolean mouseClicked(double var1, double var3, int var5, float var6, float var7, ModuleCategory var8) {
      if (var8 != ModuleCategory.UTILITIES) {
         return false;
      }

      if (this.categoryAlpha < 0.5F) {
         return false;
      }

      if (this.autoBuyComponent.isSlidingOut()) {
         return false;
      }

      float var9 = var6 + 92.0F;
      float var10 = var7 + 38.0F;
      float var11 = 300.0F;
      float var12 = 204.0F;
      return this.autoBuyComponent.mouseClicked(var1, var3, var5, var9, var10, var11, var12);
   }

   public boolean mouseReleased(double var1, double var3, int var5) {
      return false;
   }

   public boolean mouseScrolled(double var1, double var3, double var5, float var7, float var8, ModuleCategory var9) {
      if (var9 != ModuleCategory.UTILITIES) {
         return false;
      }

      if (this.categoryAlpha < 0.5F) {
         return false;
      }

      if (this.autoBuyComponent.isSlidingOut()) {
         return false;
      }

      float var10 = var7 + 92.0F;
      float var11 = var8 + 38.0F;
      float var12 = 300.0F;
      float var13 = 204.0F;
      return this.autoBuyComponent.mouseScrolled(var1, var3, var5, var10, var11, var12, var13);
   }

   public boolean keyPressed(int var1, int var2, int var3) {
      return this.categoryAlpha < 0.5F ? false : this.autoBuyComponent.keyPressed(var1, var2, var3);
   }

   public boolean charTyped(char var1, int var2) {
      return this.categoryAlpha < 0.5F ? false : this.autoBuyComponent.charTyped(var1, var2);
   }

   public boolean isEditing() {
      return this.autoBuyComponent.isEditing();
   }

   public float getCategoryAlpha() {
      return this.categoryAlpha;
   }

   public boolean isOnAutoBuy() {
      return this.wasActive;
   }
}
