package rich.screens.clickgui.impl.module.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import rich.modules.module.ModuleStructure;
import rich.util.interfaces.AbstractSettingComponent;

public class ModuleAnimationHandler {
   private Map<ModuleStructure, Float> moduleAnimations = new HashMap<>();
   private Map<ModuleStructure, Long> moduleAnimStartTimes = new HashMap<>();
   private Map<ModuleStructure, Float> oldModuleAnimations = new HashMap<>();
   private Map<AbstractSettingComponent, Float> settingAnimations = new HashMap<>();
   private Map<AbstractSettingComponent, Long> settingAnimStartTimes = new HashMap<>();
   private Map<AbstractSettingComponent, Float> visibilityAnimations = new HashMap<>();
   private Map<AbstractSettingComponent, Float> heightAnimations = new HashMap<>();
   private Map<ModuleStructure, Float> hoverAnimations = new HashMap<>();
   private Map<ModuleStructure, Float> stateAnimations = new HashMap<>();
   private Map<ModuleStructure, Float> selectedIconAnimations = new HashMap<>();
   private Map<ModuleStructure, Float> favoriteAnimations = new HashMap<>();
   private Map<ModuleStructure, Float> positionAnimations = new HashMap<>();
   private Map<ModuleStructure, Float> moduleAlphaAnimations = new HashMap<>();
   private Map<ModuleStructure, Float> bindBoxWidthAnimations = new HashMap<>();
   private Map<ModuleStructure, Float> bindBoxAlphaAnimations = new HashMap<>();
   private List<ModuleStructure> oldModules = new ArrayList<>();
   private double oldModuleDisplayScroll = 0.0;
   private float selectedPulseAnimation = 0.0F;
   private long lastHoverUpdateTime = System.currentTimeMillis();
   private long lastStateUpdateTime = System.currentTimeMillis();
   private long lastIconUpdateTime = System.currentTimeMillis();
   private long lastFavoriteUpdateTime = System.currentTimeMillis();
   private long lastBindUpdateTime = System.currentTimeMillis();
   private long lastVisibilityUpdateTime = System.currentTimeMillis();
   private ModuleStructure highlightedModule = null;
   private long highlightStartTime = 0L;
   private float highlightAnimation = 0.0F;
   private boolean scrollToModule = false;
   private ModuleStructure scrollTargetModule = null;
   private boolean isCategoryTransitioning = false;
   private float categoryTransitionProgress = 1.0F;
   private long categoryTransitionStartTime = 0L;
   private static final float MODULE_ANIM_DURATION = 300.0F;
   private static final float SETTING_ANIM_DURATION = 450.0F;
   private static final float CATEGORY_TRANSITION_DURATION = 280.0F;
   private static final float HIGHLIGHT_DURATION = 2000.0F;
   private static final float HOVER_ANIM_SPEED = 8.0F;
   private static final float STATE_ANIM_SPEED = 10.0F;
   private static final float ICON_ANIM_SPEED = 10.0F;
   private static final float FAVORITE_ANIM_SPEED = 8.0F;
   private static final float POSITION_ANIM_SPEED = 6.0F;
   private static final float BIND_WIDTH_ANIM_SPEED = 12.0F;
   private static final float PULSE_SPEED = 5.5F;
   private static final float VISIBILITY_ANIM_SPEED = 8.0F;
   private static final float HEIGHT_ANIM_SPEED = 10.0F;
   private static final float CORNER_INSET = 3.0F;
   private static final float MODULE_ITEM_HEIGHT = 22.0F;
   private float cachedDeltaTime = 0.016F;
   private long lastUpdateMs = System.currentTimeMillis();

   public void prepareTransition(List<ModuleStructure> var1, List<ModuleStructure> var2) {
      if (!var1.isEmpty()) {
         this.oldModules = new ArrayList<>(var1);
         this.oldModuleAnimations = new HashMap<>(this.moduleAnimations);
         this.isCategoryTransitioning = true;
         this.categoryTransitionStartTime = System.currentTimeMillis();
         this.categoryTransitionProgress = 0.0F;
      }
   }

   public void initModuleAnimations(List<ModuleStructure> var1) {
      this.moduleAnimations.clear();
      this.moduleAnimStartTimes.clear();
      this.hoverAnimations.clear();
      this.stateAnimations.clear();
      this.selectedIconAnimations.clear();
      this.bindBoxWidthAnimations.clear();
      this.bindBoxAlphaAnimations.clear();
      long var2 = System.currentTimeMillis();
      long var4 = 84L;

      for (int var6 = 0; var6 < var1.size(); var6++) {
         ModuleStructure var7 = (ModuleStructure)var1.get(var6);
         this.moduleAnimations.put(var7, 0.0F);
         this.moduleAnimStartTimes.put(var7, var2 + var4 + var6 * 25L);
         this.hoverAnimations.put(var7, 0.0F);
         this.stateAnimations.put(var7, var7.isState() ? 1.0F : 0.0F);
         this.selectedIconAnimations.put(var7, 0.0F);
         this.favoriteAnimations.put(var7, var7.isFavorite() ? 1.0F : 0.0F);
         this.positionAnimations.put(var7, 1.0F);
         this.moduleAlphaAnimations.put(var7, 1.0F);
      }
   }

   public void initSettingAnimations(List<AbstractSettingComponent> var1) {
      long var2 = System.currentTimeMillis();

      for (int var4 = 0; var4 < var1.size(); var4++) {
         AbstractSettingComponent var5 = (AbstractSettingComponent)var1.get(var4);
         this.settingAnimations.put(var5, 0.0F);
         this.settingAnimStartTimes.put(var5, var2 + var4 * 25L);
         boolean var6 = var5.getSetting().isVisible();
         this.visibilityAnimations.put(var5, var6 ? 1.0F : 0.0F);
         this.heightAnimations.put(var5, var6 ? 1.0F : 0.0F);
      }
   }

   public void clearSettingAnimations() {
      this.settingAnimations.clear();
      this.settingAnimStartTimes.clear();
      this.visibilityAnimations.clear();
      this.heightAnimations.clear();
   }

   public void updateAll(List<ModuleStructure> var1, ModuleStructure var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9) {
      long var10 = System.currentTimeMillis();
      this.cachedDeltaTime = Math.min((float)(var10 - this.lastUpdateMs) / 1000.0F, 0.05F);
      this.lastUpdateMs = var10;
      this.lastHoverUpdateTime = var10;
      this.lastStateUpdateTime = var10;
      this.lastIconUpdateTime = var10;
      this.lastFavoriteUpdateTime = var10;
      this.lastBindUpdateTime = var10;
      this.updateCategoryTransition();
      this.updateModuleAnimations(var1, var10);
      this.updateStateAnimations(var1);
      this.updateSelectedIconAnimations(var1, var2);
      this.updateFavoriteAnimations(var1);
      this.updateBindAnimations(var1);
      this.updateHighlightAnimation();
      this.updateHoverAnimations(var1, var3, var4, var5, var6, var7, var8, var9);
   }

   private void updateCategoryTransition() {
      if (this.isCategoryTransitioning) {
         long var1 = System.currentTimeMillis() - this.categoryTransitionStartTime;
         float var3 = Math.min(1.0F, (float)var1 / 280.0F);
         this.categoryTransitionProgress = this.easeOutCubic(var3);
         if (var3 >= 1.0F) {
            this.isCategoryTransitioning = false;
            this.oldModules.clear();
            this.oldModuleAnimations.clear();
            this.categoryTransitionProgress = 1.0F;
         }
      }
   }

   private void updateModuleAnimations(List<ModuleStructure> var1, long var2) {
      for (ModuleStructure var5 : var1) {
         Long var6 = this.moduleAnimStartTimes.get(var5);
         if (var6 != null) {
            float var7 = (float)(var2 - var6);
            float var8 = this.easeOutCubic(Math.min(1.0F, Math.max(0.0F, var7 / 300.0F)));
            this.moduleAnimations.put(var5, var8);
         }
      }
   }

   private void updateStateAnimations(List<ModuleStructure> var1) {
      float var2 = this.cachedDeltaTime;

      for (ModuleStructure var4 : var1) {
         float var5 = this.stateAnimations.getOrDefault(var4, var4.isState() ? 1.0F : 0.0F);
         this.stateAnimations.put(var4, this.animateTowards(var5, var4.isState() ? 1.0F : 0.0F, 10.0F, var2));
      }
   }

   private void updateSelectedIconAnimations(List<ModuleStructure> var1, ModuleStructure var2) {
      float var3 = this.cachedDeltaTime;

      for (ModuleStructure var5 : var1) {
         float var6 = this.selectedIconAnimations.getOrDefault(var5, 0.0F);
         this.selectedIconAnimations.put(var5, this.animateTowards(var6, var5 == var2 ? 1.0F : 0.0F, 10.0F, var3));
      }
   }

   private void updateFavoriteAnimations(List<ModuleStructure> var1) {
      float var2 = this.cachedDeltaTime;

      for (ModuleStructure var4 : var1) {
         float var5 = this.favoriteAnimations.getOrDefault(var4, 0.0F);
         this.favoriteAnimations.put(var4, this.animateTowards(var5, var4.isFavorite() ? 1.0F : 0.0F, 8.0F, var2));
         float var6 = this.positionAnimations.getOrDefault(var4, 1.0F);
         if (var6 < 1.0F) {
            this.positionAnimations.put(var4, Math.min(1.0F, var6 + 6.0F * var2));
         }

         float var7 = this.moduleAlphaAnimations.getOrDefault(var4, 1.0F);
         if (var7 < 1.0F) {
            this.moduleAlphaAnimations.put(var4, Math.min(1.0F, var7 + 6.0F * var2));
         }
      }
   }

   private void updateBindAnimations(List<ModuleStructure> var1) {
      float var2 = this.cachedDeltaTime;

      for (ModuleStructure var4 : var1) {
         int var5 = var4.getKey();
         boolean var6 = var5 != -1 && var5 != -1;
         float var7 = this.bindBoxAlphaAnimations.getOrDefault(var4, 0.0F);
         this.bindBoxAlphaAnimations.put(var4, this.animateTowards(var7, var6 ? 1.0F : 0.0F, 12.0F, var2));
      }
   }

   private void updateHoverAnimations(List<ModuleStructure> var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      float var9 = this.cachedDeltaTime;
      this.selectedPulseAnimation += var9 * 5.5F;
      if (this.selectedPulseAnimation > Math.PI * 2) {
         this.selectedPulseAnimation -= (float) (Math.PI * 2);
      }

      float var10 = 3.0F;
      float var11 = (var6 - var10 * 2.0F - 4.0F) / 2.0F;
      float var12 = var5 + var10 + 2.0F + var8;
      float var13 = var5 + var10;
      float var14 = var5 + var7 - var10;
      boolean var15 = var2 >= var4 && var2 <= var4 + var6;

      for (int var16 = 0; var16 < var1.size(); var16++) {
         ModuleStructure var17 = (ModuleStructure)var1.get(var16);
         int var18 = var16 % 2;
         int var19 = var16 / 2;
         float var20 = var4 + var10 + var18 * (var11 + 4.0F);
         float var21 = var12 + var19 * 24.0F;
         boolean var22 = !this.isCategoryTransitioning
            && var15
            && var2 >= var20
            && var2 <= var20 + var11
            && var3 >= var21
            && var3 <= var21 + 22.0F
            && var21 >= var13
            && var21 + 22.0F <= var14 + 22.0F;
         float var23 = this.hoverAnimations.getOrDefault(var17, 0.0F);
         this.hoverAnimations.put(var17, this.animateTowards(var23, var22 ? 1.0F : 0.0F, 8.0F, var9));
      }
   }

   private void updateHighlightAnimation() {
      if (this.highlightedModule != null) {
         long var1 = System.currentTimeMillis() - this.highlightStartTime;
         if ((float)var1 >= 2000.0F) {
            long var3 = var1 - 2000L;
            float var5 = (float)var3 / 500.0F;
            if (var5 >= 1.0F) {
               this.highlightedModule = null;
               this.highlightAnimation = 0.0F;
            } else {
               this.highlightAnimation = 1.0F - var5;
            }
         } else {
            this.highlightAnimation = 1.0F;
         }
      }
   }

   public void updateSettingAnimations(List<AbstractSettingComponent> var1) {
      long var2 = System.currentTimeMillis();

      for (AbstractSettingComponent var5 : var1) {
         Long var6 = this.settingAnimStartTimes.get(var5);
         if (var6 != null) {
            float var7 = this.easeOutCubic(Math.min(1.0F, Math.max(0.0F, (float)(var2 - var6) / 450.0F)));
            this.settingAnimations.put(var5, var7);
         }
      }
   }

   public void updateVisibilityAnimations(List<AbstractSettingComponent> var1) {
      float var2 = this.cachedDeltaTime;

      for (AbstractSettingComponent var4 : var1) {
         boolean var5 = var4.getSetting().isVisible();
         float var6 = var5 ? 1.0F : 0.0F;
         this.visibilityAnimations.put(var4, this.animateTowards(this.visibilityAnimations.getOrDefault(var4, var6), var6, 8.0F, var2));
         this.heightAnimations.put(var4, this.animateTowards(this.heightAnimations.getOrDefault(var4, var6), var6, 10.0F, var2));
      }
   }

   public void startHighlight(ModuleStructure var1) {
      this.highlightedModule = var1;
      this.highlightStartTime = System.currentTimeMillis();
      this.highlightAnimation = 1.0F;
   }

   public void setScrollTarget(ModuleStructure var1) {
      this.scrollToModule = true;
      this.scrollTargetModule = var1;
   }

   public boolean shouldScrollToModule() {
      return this.scrollToModule;
   }

   public void clearScrollTarget() {
      this.scrollToModule = false;
      this.scrollTargetModule = null;
   }

   private float animateTowards(float var1, float var2, float var3, float var4) {
      float var5 = var2 - var1;
      return Math.abs(var5) < 0.001F ? var2 : var1 + var5 * var3 * var4;
   }

   private float easeOutCubic(float var1) {
      return 1.0F - (float)Math.pow(1.0F - var1, 3.0);
   }

   public float easeInCubic(float var1) {
      return var1 * var1 * var1;
   }

   public float easeOutQuart(float var1) {
      return 1.0F - (float)Math.pow(1.0F - var1, 4.0);
   }

   public float getCategorySlideDistance() {
      return 40.0F;
   }

   public Map<ModuleStructure, Float> getModuleAnimations() {
      return this.moduleAnimations;
   }

   public Map<ModuleStructure, Long> getModuleAnimStartTimes() {
      return this.moduleAnimStartTimes;
   }

   public Map<ModuleStructure, Float> getOldModuleAnimations() {
      return this.oldModuleAnimations;
   }

   public Map<AbstractSettingComponent, Float> getSettingAnimations() {
      return this.settingAnimations;
   }

   public Map<AbstractSettingComponent, Long> getSettingAnimStartTimes() {
      return this.settingAnimStartTimes;
   }

   public Map<AbstractSettingComponent, Float> getVisibilityAnimations() {
      return this.visibilityAnimations;
   }

   public Map<AbstractSettingComponent, Float> getHeightAnimations() {
      return this.heightAnimations;
   }

   public Map<ModuleStructure, Float> getHoverAnimations() {
      return this.hoverAnimations;
   }

   public Map<ModuleStructure, Float> getStateAnimations() {
      return this.stateAnimations;
   }

   public Map<ModuleStructure, Float> getSelectedIconAnimations() {
      return this.selectedIconAnimations;
   }

   public Map<ModuleStructure, Float> getFavoriteAnimations() {
      return this.favoriteAnimations;
   }

   public Map<ModuleStructure, Float> getPositionAnimations() {
      return this.positionAnimations;
   }

   public Map<ModuleStructure, Float> getModuleAlphaAnimations() {
      return this.moduleAlphaAnimations;
   }

   public Map<ModuleStructure, Float> getBindBoxWidthAnimations() {
      return this.bindBoxWidthAnimations;
   }

   public Map<ModuleStructure, Float> getBindBoxAlphaAnimations() {
      return this.bindBoxAlphaAnimations;
   }

   public List<ModuleStructure> getOldModules() {
      return this.oldModules;
   }

   public double getOldModuleDisplayScroll() {
      return this.oldModuleDisplayScroll;
   }

   public float getSelectedPulseAnimation() {
      return this.selectedPulseAnimation;
   }

   public long getLastHoverUpdateTime() {
      return this.lastHoverUpdateTime;
   }

   public long getLastStateUpdateTime() {
      return this.lastStateUpdateTime;
   }

   public long getLastIconUpdateTime() {
      return this.lastIconUpdateTime;
   }

   public long getLastFavoriteUpdateTime() {
      return this.lastFavoriteUpdateTime;
   }

   public long getLastBindUpdateTime() {
      return this.lastBindUpdateTime;
   }

   public long getLastVisibilityUpdateTime() {
      return this.lastVisibilityUpdateTime;
   }

   public ModuleStructure getHighlightedModule() {
      return this.highlightedModule;
   }

   public long getHighlightStartTime() {
      return this.highlightStartTime;
   }

   public float getHighlightAnimation() {
      return this.highlightAnimation;
   }

   public boolean isScrollToModule() {
      return this.scrollToModule;
   }

   public ModuleStructure getScrollTargetModule() {
      return this.scrollTargetModule;
   }

   public boolean isCategoryTransitioning() {
      return this.isCategoryTransitioning;
   }

   public float getCategoryTransitionProgress() {
      return this.categoryTransitionProgress;
   }

   public long getCategoryTransitionStartTime() {
      return this.categoryTransitionStartTime;
   }

   public float getCachedDeltaTime() {
      return this.cachedDeltaTime;
   }

   public long getLastUpdateMs() {
      return this.lastUpdateMs;
   }

   public void setModuleAnimations(Map<ModuleStructure, Float> var1) {
      this.moduleAnimations = var1;
   }

   public void setModuleAnimStartTimes(Map<ModuleStructure, Long> var1) {
      this.moduleAnimStartTimes = var1;
   }

   public void setOldModuleAnimations(Map<ModuleStructure, Float> var1) {
      this.oldModuleAnimations = var1;
   }

   public void setSettingAnimations(Map<AbstractSettingComponent, Float> var1) {
      this.settingAnimations = var1;
   }

   public void setSettingAnimStartTimes(Map<AbstractSettingComponent, Long> var1) {
      this.settingAnimStartTimes = var1;
   }

   public void setVisibilityAnimations(Map<AbstractSettingComponent, Float> var1) {
      this.visibilityAnimations = var1;
   }

   public void setHeightAnimations(Map<AbstractSettingComponent, Float> var1) {
      this.heightAnimations = var1;
   }

   public void setHoverAnimations(Map<ModuleStructure, Float> var1) {
      this.hoverAnimations = var1;
   }

   public void setStateAnimations(Map<ModuleStructure, Float> var1) {
      this.stateAnimations = var1;
   }

   public void setSelectedIconAnimations(Map<ModuleStructure, Float> var1) {
      this.selectedIconAnimations = var1;
   }

   public void setFavoriteAnimations(Map<ModuleStructure, Float> var1) {
      this.favoriteAnimations = var1;
   }

   public void setPositionAnimations(Map<ModuleStructure, Float> var1) {
      this.positionAnimations = var1;
   }

   public void setModuleAlphaAnimations(Map<ModuleStructure, Float> var1) {
      this.moduleAlphaAnimations = var1;
   }

   public void setBindBoxWidthAnimations(Map<ModuleStructure, Float> var1) {
      this.bindBoxWidthAnimations = var1;
   }

   public void setBindBoxAlphaAnimations(Map<ModuleStructure, Float> var1) {
      this.bindBoxAlphaAnimations = var1;
   }

   public void setOldModules(List<ModuleStructure> var1) {
      this.oldModules = var1;
   }

   public void setOldModuleDisplayScroll(double var1) {
      this.oldModuleDisplayScroll = var1;
   }

   public void setSelectedPulseAnimation(float var1) {
      this.selectedPulseAnimation = var1;
   }

   public void setLastHoverUpdateTime(long var1) {
      this.lastHoverUpdateTime = var1;
   }

   public void setLastStateUpdateTime(long var1) {
      this.lastStateUpdateTime = var1;
   }

   public void setLastIconUpdateTime(long var1) {
      this.lastIconUpdateTime = var1;
   }

   public void setLastFavoriteUpdateTime(long var1) {
      this.lastFavoriteUpdateTime = var1;
   }

   public void setLastBindUpdateTime(long var1) {
      this.lastBindUpdateTime = var1;
   }

   public void setLastVisibilityUpdateTime(long var1) {
      this.lastVisibilityUpdateTime = var1;
   }

   public void setHighlightedModule(ModuleStructure var1) {
      this.highlightedModule = var1;
   }

   public void setHighlightStartTime(long var1) {
      this.highlightStartTime = var1;
   }

   public void setHighlightAnimation(float var1) {
      this.highlightAnimation = var1;
   }

   public void setScrollToModule(boolean var1) {
      this.scrollToModule = var1;
   }

   public void setScrollTargetModule(ModuleStructure var1) {
      this.scrollTargetModule = var1;
   }

   public void setCategoryTransitioning(boolean var1) {
      this.isCategoryTransitioning = var1;
   }

   public void setCategoryTransitionProgress(float var1) {
      this.categoryTransitionProgress = var1;
   }

   public void setCategoryTransitionStartTime(long var1) {
      this.categoryTransitionStartTime = var1;
   }

   public void setCachedDeltaTime(float var1) {
      this.cachedDeltaTime = var1;
   }

   public void setLastUpdateMs(long var1) {
      this.lastUpdateMs = var1;
   }
}
