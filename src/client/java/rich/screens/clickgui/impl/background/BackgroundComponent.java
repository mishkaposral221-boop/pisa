package rich.screens.clickgui.impl.background;

import java.util.List;
import net.minecraft.client.gui.DrawContext;
import rich.IMinecraft;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.screens.clickgui.impl.background.render.AvatarRenderer;
import rich.screens.clickgui.impl.background.render.BackgroundRenderer;
import rich.screens.clickgui.impl.background.render.CategoryRenderer;
import rich.screens.clickgui.impl.background.render.HeaderRenderer;
import rich.screens.clickgui.impl.background.search.SearchHandler;
import rich.screens.clickgui.impl.background.search.SearchRenderer;

public class BackgroundComponent implements IMinecraft {
   public static final int BG_WIDTH = 400;
   public static final int BG_HEIGHT = 250;
   private final BackgroundRenderer backgroundRenderer;
   private final CategoryRenderer categoryRenderer;
   private final HeaderRenderer headerRenderer;
   private final AvatarRenderer avatarRenderer;
   private final SearchHandler searchHandler;
   private final SearchRenderer searchRenderer;
   private ModuleCategory previousCategory = null;
   private ModuleCategory currentCategory = null;
   private float headerTransition = 1.0F;
   private static final float HEADER_SPEED = 3.0F;
   private long lastUpdateTime = System.currentTimeMillis();

   public BackgroundComponent() {
      this.backgroundRenderer = new BackgroundRenderer();
      this.categoryRenderer = new CategoryRenderer();
      this.headerRenderer = new HeaderRenderer();
      this.avatarRenderer = new AvatarRenderer();
      this.searchHandler = new SearchHandler();
      this.searchRenderer = new SearchRenderer(this.searchHandler);
   }

   public boolean isSearchActive() {
      return this.searchHandler.isSearchActive();
   }

   public float getSearchPanelAlpha() {
      return this.searchHandler.getSearchPanelAlpha();
   }

   public float getNormalPanelAlpha() {
      return this.searchHandler.getNormalPanelAlpha();
   }

   public void setSearchActive(boolean var1) {
      this.searchHandler.setSearchActive(var1);
   }

   public String getSearchText() {
      return this.searchHandler.getSearchText();
   }

   public List<ModuleStructure> getSearchResults() {
      return this.searchHandler.getSearchResults();
   }

   public ModuleStructure getSelectedSearchModule() {
      return this.searchHandler.getSelectedSearchModule();
   }

   public void updateAnimations(ModuleCategory var1, float var2) {
      long var3 = System.currentTimeMillis();
      float var5 = Math.min((float)(var3 - this.lastUpdateTime) / 1000.0F, 0.1F);
      this.lastUpdateTime = var3;
      if (this.currentCategory != var1) {
         this.previousCategory = this.currentCategory;
         this.currentCategory = var1;
         this.headerTransition = 0.0F;
      }

      if (this.headerTransition < 1.0F) {
         this.headerTransition += 3.0F * var5;
         if (this.headerTransition > 1.0F) {
            this.headerTransition = 1.0F;
         }
      }

      this.categoryRenderer.updateAnimations(var1, var5);
      this.searchHandler.updateAnimations(var5);
   }

   public void render(DrawContext var1, float var2, float var3, ModuleCategory var4, float var5, float var6) {
      this.updateAnimations(var4, var5);
      this.backgroundRenderer.render(var1, var2, var3, var6);
   }

   public void renderCategoryPanel(float var1, float var2, float var3) {
      this.backgroundRenderer.renderCategoryPanel(var1, var2, 250.0F, var3);
   }

   public void renderHeader(float var1, float var2, ModuleCategory var3, float var4) {
      this.headerRenderer.render(var1, var2, 400.0F, var3, this.previousCategory, this.currentCategory, this.headerTransition, this.searchHandler, var4);
   }

   public void renderCategoryNames(float var1, float var2, ModuleCategory var3, float var4) {
      this.categoryRenderer.render(var1, var2, var3, var4);
   }

   public void renderSearchResults(DrawContext var1, float var2, float var3, float var4, float var5, int var6, float var7) {
      this.searchRenderer.render(var1, var2, var3, 400.0F, 250.0F, var4, var5, var6, var7);
   }

   public boolean handleSearchChar(char var1) {
      return this.searchHandler.handleSearchChar(var1);
   }

   public boolean handleSearchKey(int var1) {
      return this.searchHandler.handleSearchKey(var1);
   }

   public void handleSearchScroll(double var1, float var3) {
      this.searchHandler.handleSearchScroll(var1, var3);
   }

   public boolean isSearchBoxHovered(double var1, double var3, float var5, float var6) {
      return this.headerRenderer.isSearchBoxHovered(var1, var3, var5, var6);
   }

   public ModuleStructure getSearchModuleAtPosition(double var1, double var3, float var5, float var6) {
      return this.searchRenderer.getModuleAtPosition(var1, var3, var5, var6, 400.0F, 250.0F, this.searchHandler);
   }

   public ModuleCategory getCategoryAtPosition(double var1, double var3, float var5, float var6) {
      return this.categoryRenderer.getCategoryAtPosition(var1, var3, var5, var6);
   }
}
