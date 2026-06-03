package rich.screens.clickgui.impl.module;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_332;
import rich.IMinecraft;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.SettingComponentAdder;
import rich.screens.clickgui.impl.module.handler.ModuleAnimationHandler;
import rich.screens.clickgui.impl.module.handler.ModuleBindHandler;
import rich.screens.clickgui.impl.module.handler.ModuleFavoriteHandler;
import rich.screens.clickgui.impl.module.handler.ModuleScrollHandler;
import rich.screens.clickgui.impl.module.render.ModuleListRenderer;
import rich.screens.clickgui.impl.module.render.SettingsPanelRenderer;
import rich.screens.clickgui.impl.module.util.ModuleDisplayHelper;
import rich.util.interfaces.AbstractComponent;
import rich.util.interfaces.AbstractSettingComponent;

public class ModuleComponent implements IMinecraft {
   private List<ModuleStructure> modules = new ArrayList<>();
   private List<ModuleStructure> displayModules = new ArrayList<>();
   private ModuleStructure selectedModule = null;
   private ModuleStructure bindingModule = null;
   private List<AbstractSettingComponent> settingComponents = new ArrayList<>();
   private ModuleCategory currentCategory = null;
   private final ModuleAnimationHandler animationHandler;
   private final ModuleScrollHandler scrollHandler;
   private final ModuleFavoriteHandler favoriteHandler;
   private final ModuleBindHandler bindHandler;
   private final ModuleListRenderer listRenderer;
   private final SettingsPanelRenderer settingsRenderer;
   private final ModuleDisplayHelper displayHelper;
   private int savedGuiScale = 1;
   private float lastMouseX = 0.0F;
   private float lastMouseY = 0.0F;
   private float lastListX = 0.0F;
   private float lastListY = 0.0F;
   private float lastListWidth = 0.0F;
   private float lastListHeight = 0.0F;

   public ModuleComponent() {
      this.animationHandler = new ModuleAnimationHandler();
      this.scrollHandler = new ModuleScrollHandler();
      this.favoriteHandler = new ModuleFavoriteHandler();
      this.bindHandler = new ModuleBindHandler();
      this.displayHelper = new ModuleDisplayHelper();
      this.listRenderer = new ModuleListRenderer(this.animationHandler, this.bindHandler, this.displayHelper);
      this.settingsRenderer = new SettingsPanelRenderer(this.animationHandler);
   }

   public void updateModules(List<ModuleStructure> var1, ModuleCategory var2) {
      if (var2 != this.currentCategory) {
         this.animationHandler.prepareTransition(this.modules, this.displayModules);
         this.currentCategory = var2;
         this.modules = var1;
         this.rebuildDisplayList();
         this.scrollHandler.resetModuleScroll();
         this.animationHandler.initModuleAnimations(this.displayModules);
         this.displayHelper.updateModulesWithSettings(this.displayModules);
         if (this.animationHandler.shouldScrollToModule() && this.displayModules.contains(this.animationHandler.getScrollTargetModule())) {
            this.scrollToModuleAndHighlight(this.animationHandler.getScrollTargetModule());
            this.animationHandler.clearScrollTarget();
         } else if (this.displayModules.isEmpty() || this.selectedModule != null && this.displayModules.contains(this.selectedModule)) {
            if (this.displayModules.isEmpty()) {
               this.selectedModule = null;
               this.settingComponents.clear();
            }
         } else {
            this.selectModule(this.displayModules.get(0));
         }
      }
   }

   private void rebuildDisplayList() {
      this.displayModules.clear();
      ArrayList var1 = new ArrayList();
      ArrayList var2 = new ArrayList();

      for (ModuleStructure var4 : this.modules) {
         if (var4.isFavorite()) {
            var1.add(var4);
         } else {
            var2.add(var4);
         }
      }

      this.displayModules.addAll(var1);
      this.displayModules.addAll(var2);
   }

   public void toggleFavorite(ModuleStructure var1) {
      this.favoriteHandler.toggleFavorite(var1, this.displayModules, this.animationHandler);
      this.rebuildDisplayList();
   }

   public void selectModuleFromSearch(ModuleStructure var1) {
      this.animationHandler.setScrollTarget(var1);
   }

   public void scrollToModuleAndHighlight(ModuleStructure var1) {
      if (var1 != null && this.displayModules.contains(var1)) {
         this.selectModule(var1);
         int var2 = this.displayModules.indexOf(var1);
         if (var2 >= 0 && this.scrollHandler.getLastModuleListHeight() > 0.0F) {
            this.scrollHandler.scrollToModule(var2, this.displayModules.size());
         }

         this.animationHandler.startHighlight(var1);
      }
   }

   public void selectModule(ModuleStructure var1) {
      if (var1 != this.selectedModule) {
         this.selectedModule = var1;
         this.scrollHandler.resetSettingScroll();
         this.settingComponents.clear();
         this.animationHandler.clearSettingAnimations();
         if (var1 != null) {
            new SettingComponentAdder().addSettingComponent(var1.settings(), this.settingComponents);
            this.animationHandler.initSettingAnimations(this.settingComponents);
         }
      }
   }

   public void renderModuleList(class_332 var1, float var2, float var3, float var4, float var5, float var6, float var7, int var8, float var9) {
      this.lastMouseX = var6;
      this.lastMouseY = var7;
      this.lastListX = var2;
      this.lastListY = var3;
      this.lastListWidth = var4;
      this.lastListHeight = var5;
      this.animationHandler
         .updateAll(this.displayModules, this.selectedModule, var6, var7, var2, var3, var4, var5, (float)this.scrollHandler.getModuleDisplayScroll());
      this.listRenderer
         .render(
            var1,
            this.displayModules,
            this.selectedModule,
            this.bindingModule,
            var2,
            var3,
            var4,
            var5,
            var6,
            var7,
            var8,
            var9,
            this.animationHandler,
            this.scrollHandler
         );
   }

   public void renderSettingsPanel(class_332 var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, int var9, float var10) {
      this.savedGuiScale = var9;
      this.settingsRenderer
         .render(
            var1, this.selectedModule, this.settingComponents, var2, var3, var4, var5, var6, var7, var8, var9, var10, this.scrollHandler, this.animationHandler
         );
   }

   public void updateScroll(float var1, float var2) {
      this.scrollHandler.update(var1);
   }

   public void updateScrollFades(float var1, float var2, float var3, float var4) {
      this.scrollHandler.updateFades(this.displayModules.size(), this.calculateTotalSettingHeight(), var3, var4);
   }

   public float calculateTotalSettingHeight() {
      return this.settingsRenderer.calculateTotalHeight(this.settingComponents, this.animationHandler);
   }

   public ModuleStructure getModuleAtPosition(double var1, double var3, float var5, float var6, float var7, float var8) {
      return this.listRenderer
         .getModuleAtPosition(
            this.displayModules,
            var1,
            var3,
            var5,
            var6,
            var7,
            var8,
            this.scrollHandler.getModuleDisplayScroll(),
            this.animationHandler.isCategoryTransitioning()
         );
   }

   public boolean isStarClicked(double var1, double var3, float var5, float var6, float var7, float var8) {
      return this.listRenderer
         .isStarClicked(
            this.displayModules,
            var1,
            var3,
            var5,
            var6,
            var7,
            var8,
            this.scrollHandler.getModuleDisplayScroll(),
            this.displayHelper,
            this.animationHandler.isCategoryTransitioning()
         );
   }

   public ModuleStructure getModuleForStarClick(double var1, double var3, float var5, float var6, float var7, float var8) {
      return this.listRenderer
         .getModuleForStarClick(
            this.displayModules,
            var1,
            var3,
            var5,
            var6,
            var7,
            var8,
            this.scrollHandler.getModuleDisplayScroll(),
            this.displayHelper,
            this.animationHandler.isCategoryTransitioning()
         );
   }

   public void handleModuleScroll(double var1, float var3) {
      if (!this.animationHandler.isCategoryTransitioning()) {
         this.scrollHandler.handleModuleScroll(var1, var3, this.displayModules.size());
      }
   }

   public void handleSettingScroll(double var1, float var3) {
      this.scrollHandler.handleSettingScroll(var1, var3, this.calculateTotalSettingHeight());
   }

   public void tick() {
      this.settingComponents.forEach(AbstractComponent::tick);
   }

   public boolean isTransitioning() {
      return this.animationHandler.isCategoryTransitioning();
   }

   public List<ModuleStructure> getModules() {
      return this.modules;
   }

   public List<ModuleStructure> getDisplayModules() {
      return this.displayModules;
   }

   public ModuleStructure getSelectedModule() {
      return this.selectedModule;
   }

   public ModuleStructure getBindingModule() {
      return this.bindingModule;
   }

   public List<AbstractSettingComponent> getSettingComponents() {
      return this.settingComponents;
   }

   public ModuleCategory getCurrentCategory() {
      return this.currentCategory;
   }

   public ModuleAnimationHandler getAnimationHandler() {
      return this.animationHandler;
   }

   public ModuleScrollHandler getScrollHandler() {
      return this.scrollHandler;
   }

   public ModuleFavoriteHandler getFavoriteHandler() {
      return this.favoriteHandler;
   }

   public ModuleBindHandler getBindHandler() {
      return this.bindHandler;
   }

   public ModuleListRenderer getListRenderer() {
      return this.listRenderer;
   }

   public SettingsPanelRenderer getSettingsRenderer() {
      return this.settingsRenderer;
   }

   public ModuleDisplayHelper getDisplayHelper() {
      return this.displayHelper;
   }

   public int getSavedGuiScale() {
      return this.savedGuiScale;
   }

   public float getLastMouseX() {
      return this.lastMouseX;
   }

   public float getLastMouseY() {
      return this.lastMouseY;
   }

   public float getLastListX() {
      return this.lastListX;
   }

   public float getLastListY() {
      return this.lastListY;
   }

   public float getLastListWidth() {
      return this.lastListWidth;
   }

   public float getLastListHeight() {
      return this.lastListHeight;
   }

   public void setModules(List<ModuleStructure> var1) {
      this.modules = var1;
   }

   public void setDisplayModules(List<ModuleStructure> var1) {
      this.displayModules = var1;
   }

   public void setSelectedModule(ModuleStructure var1) {
      this.selectedModule = var1;
   }

   public void setBindingModule(ModuleStructure var1) {
      this.bindingModule = var1;
   }

   public void setSettingComponents(List<AbstractSettingComponent> var1) {
      this.settingComponents = var1;
   }

   public void setCurrentCategory(ModuleCategory var1) {
      this.currentCategory = var1;
   }

   public void setSavedGuiScale(int var1) {
      this.savedGuiScale = var1;
   }

   public void setLastMouseX(float var1) {
      this.lastMouseX = var1;
   }

   public void setLastMouseY(float var1) {
      this.lastMouseY = var1;
   }

   public void setLastListX(float var1) {
      this.lastListX = var1;
   }

   public void setLastListY(float var1) {
      this.lastListY = var1;
   }

   public void setLastListWidth(float var1) {
      this.lastListWidth = var1;
   }

   public void setLastListHeight(float var1) {
      this.lastListHeight = var1;
   }
}
