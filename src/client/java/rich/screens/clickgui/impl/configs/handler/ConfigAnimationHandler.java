package rich.screens.clickgui.impl.configs.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigAnimationHandler {
   private final Map<String, Float> hoverAnimations = new HashMap<>();
   private final Map<String, Float> deleteHoverAnimations = new HashMap<>();
   private final Map<String, Float> loadHoverAnimations = new HashMap<>();
   private final Map<String, Float> refreshHoverAnimations = new HashMap<>();
   private final Map<String, Float> itemAppearAnimations = new HashMap<>();
   private float panelAlpha = 0.0F;
   private float panelSlide = 0.0F;
   private float createBoxAnimation = 0.0F;
   private float cursorBlink = 0.0F;
   private float selectedAnimation = 0.0F;
   private long lastUpdateTime = System.currentTimeMillis();

   public void reset() {
      this.panelAlpha = 0.0F;
      this.panelSlide = 0.0F;
      this.createBoxAnimation = 0.0F;
      this.itemAppearAnimations.clear();
      this.hoverAnimations.clear();
      this.deleteHoverAnimations.clear();
      this.loadHoverAnimations.clear();
      this.refreshHoverAnimations.clear();
   }

   public void initItemAnimations(List<String> var1) {
      for (String var3 : var1) {
         if (!this.itemAppearAnimations.containsKey(var3)) {
            this.itemAppearAnimations.put(var3, 0.0F);
         }
      }
   }

   public void initItemAnimationsFromEntries(List<ConfigDataHandler.ConfigEntry> var1) {
      for (ConfigDataHandler.ConfigEntry var3 : var1) {
         if (!this.itemAppearAnimations.containsKey(var3.name)) {
            this.itemAppearAnimations.put(var3.name, 0.0F);
         }
      }
   }

   public void update(boolean var1, List<String> var2, boolean var3) {
      long var4 = System.currentTimeMillis();
      float var6 = Math.min((float)(var4 - this.lastUpdateTime) / 1000.0F, 0.1F);
      this.lastUpdateTime = var4;
      this.updatePanelAnimations(var1, var6);
      this.updateCreateBoxAnimation(var3, var6);
      this.updateCursorBlink(var6);
      this.updateItemAnimations(var1, var2, var6);
      this.updateHoverAnimations(var2, var6);
   }

   private void updatePanelAnimations(boolean var1, float var2) {
      float var3 = var1 ? 1.0F : 0.0F;
      float var4 = var3 - this.panelAlpha;
      this.panelAlpha += var4 * 16.0F * var2;
      this.panelAlpha = Math.max(0.0F, Math.min(1.0F, this.panelAlpha));
      float var5 = var1 ? 1.0F : 0.0F;
      float var6 = var5 - this.panelSlide;
      this.panelSlide += var6 * 20.0F * var2;
      this.panelSlide = Math.max(0.0F, Math.min(1.0F, this.panelSlide));
   }

   private void updateCreateBoxAnimation(boolean var1, float var2) {
      float var3 = var1 ? 1.0F : 0.0F;
      this.createBoxAnimation = this.createBoxAnimation + (var3 - this.createBoxAnimation) * 14.0F * var2;
   }

   private void updateCursorBlink(float var1) {
      this.cursorBlink += var1 * 2.0F;
      if (this.cursorBlink > 1.0F) {
         this.cursorBlink--;
      }
   }

   private void updateItemAnimations(boolean var1, List<String> var2, float var3) {
      int var4 = 0;

      for (String var6 : var2) {
         float var7 = this.itemAppearAnimations.getOrDefault(var6, 0.0F);
         float var8;
         if (var1) {
            float var9 = var4 * 0.02F;
            if (this.panelAlpha > var9) {
               var8 = 1.0F;
            } else {
               var8 = 0.0F;
            }
         } else {
            var8 = 0.0F;
         }

         float var12 = var1 ? 20.0F : 16.0F;
         float var10 = var8 - var7;
         var7 += var10 * var12 * var3;
         this.itemAppearAnimations.put(var6, Math.max(0.0F, Math.min(1.0F, var7)));
         var4++;
      }
   }

   private void updateHoverAnimations(List<String> var1, float var2) {
      for (String var4 : var1) {
         float var5 = this.hoverAnimations.getOrDefault(var4, 0.0F);
         this.hoverAnimations.put(var4, var5 + (0.0F - var5) * 8.0F * var2);
         float var6 = this.deleteHoverAnimations.getOrDefault(var4, 0.0F);
         this.deleteHoverAnimations.put(var4, var6 + (0.0F - var6) * 8.0F * var2);
         float var7 = this.loadHoverAnimations.getOrDefault(var4, 0.0F);
         this.loadHoverAnimations.put(var4, var7 + (0.0F - var7) * 8.0F * var2);
         float var8 = this.refreshHoverAnimations.getOrDefault(var4, 0.0F);
         this.refreshHoverAnimations.put(var4, var8 + (0.0F - var8) * 8.0F * var2);
      }
   }

   public void updateSelectedAnimation(boolean var1, float var2) {
      float var3 = var1 ? 1.0F : 0.0F;
      this.selectedAnimation = this.selectedAnimation + (var3 - this.selectedAnimation) * 8.0F * var2;
   }

   public void setHoverAnimation(String var1, float var2) {
      this.hoverAnimations.put(var1, var2);
   }

   public void setDeleteHoverAnimation(String var1, float var2) {
      this.deleteHoverAnimations.put(var1, var2);
   }

   public void setLoadHoverAnimation(String var1, float var2) {
      this.loadHoverAnimations.put(var1, var2);
   }

   public void setRefreshHoverAnimation(String var1, float var2) {
      this.refreshHoverAnimations.put(var1, var2);
   }

   public float getItemAppearAnimation(String var1) {
      return this.itemAppearAnimations.getOrDefault(var1, 0.0F);
   }

   public float getHoverAnimation(String var1) {
      return this.hoverAnimations.getOrDefault(var1, 0.0F);
   }

   public float getDeleteHoverAnimation(String var1) {
      return this.deleteHoverAnimations.getOrDefault(var1, 0.0F);
   }

   public float getLoadHoverAnimation(String var1) {
      return this.loadHoverAnimations.getOrDefault(var1, 0.0F);
   }

   public float getRefreshHoverAnimation(String var1) {
      return this.refreshHoverAnimations.getOrDefault(var1, 0.0F);
   }

   public boolean isFullyHidden() {
      return this.panelAlpha < 0.01F && this.panelSlide < 0.01F;
   }

   public Map<String, Float> getHoverAnimations() {
      return this.hoverAnimations;
   }

   public Map<String, Float> getDeleteHoverAnimations() {
      return this.deleteHoverAnimations;
   }

   public Map<String, Float> getLoadHoverAnimations() {
      return this.loadHoverAnimations;
   }

   public Map<String, Float> getRefreshHoverAnimations() {
      return this.refreshHoverAnimations;
   }

   public Map<String, Float> getItemAppearAnimations() {
      return this.itemAppearAnimations;
   }

   public float getPanelAlpha() {
      return this.panelAlpha;
   }

   public float getPanelSlide() {
      return this.panelSlide;
   }

   public float getCreateBoxAnimation() {
      return this.createBoxAnimation;
   }

   public float getCursorBlink() {
      return this.cursorBlink;
   }

   public float getSelectedAnimation() {
      return this.selectedAnimation;
   }

   public long getLastUpdateTime() {
      return this.lastUpdateTime;
   }
}
