package rich.screens.clickgui.impl.background.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lwjgl.glfw.GLFW;
import rich.IMinecraft;
import rich.Initialization;
import rich.modules.module.ModuleRepository;
import rich.modules.module.ModuleStructure;

public class SearchHandler implements IMinecraft {
   private boolean searchActive = false;
   private String searchText = "";
   private int searchCursorPosition = 0;
   private int searchSelectionStart = -1;
   private int searchSelectionEnd = -1;
   private float searchCursorBlink = 0.0F;
   private float searchBoxAnimation = 0.0F;
   private float searchFocusAnimation = 0.0F;
   private float searchPanelAlpha = 0.0F;
   private float normalPanelAlpha = 1.0F;
   private float searchSelectionAnimation = 0.0F;
   private List<ModuleStructure> searchResults = new ArrayList<>();
   private Map<ModuleStructure, Float> searchResultAnimations = new HashMap<>();
   private Map<ModuleStructure, Long> searchResultAnimStartTimes = new HashMap<>();
   private float searchScrollOffset = 0.0F;
   private float searchTargetScroll = 0.0F;
   private int hoveredSearchIndex = -1;
   private ModuleStructure selectedSearchModule = null;
   private static final float SEARCH_ANIM_SPEED = 8.0F;
   private static final float PANEL_FADE_SPEED = 15.0F;
   private static final float SEARCH_RESULT_HEIGHT = 18.0F;
   private static final float SEARCH_RESULT_ANIM_DURATION = 200.0F;

   public void setSearchActive(boolean var1) {
      if (var1 && !this.searchActive) {
         this.searchText = "";
         this.searchCursorPosition = 0;
         this.searchSelectionStart = -1;
         this.searchSelectionEnd = -1;
         this.searchResults.clear();
         this.searchResultAnimations.clear();
         this.searchResultAnimStartTimes.clear();
         this.searchScrollOffset = 0.0F;
         this.searchTargetScroll = 0.0F;
         this.hoveredSearchIndex = -1;
         this.selectedSearchModule = null;
      }

      this.searchActive = var1;
   }

   public void updateAnimations(float var1) {
      float var2 = this.searchActive ? 1.0F : 0.0F;
      this.searchBoxAnimation = this.updateAnimation(this.searchBoxAnimation, var2, 8.0F, var1);
      this.searchFocusAnimation = this.updateAnimation(this.searchFocusAnimation, var2, 8.0F, var1);
      this.searchPanelAlpha = this.updateAnimation(this.searchPanelAlpha, var2, 15.0F, var1);
      this.normalPanelAlpha = this.updateAnimation(this.normalPanelAlpha, this.searchActive ? 0.0F : 1.0F, 15.0F, var1);
      this.searchSelectionAnimation = this.updateAnimation(this.searchSelectionAnimation, this.hasSearchSelection() ? 1.0F : 0.0F, 8.0F, var1);
      if (this.searchActive) {
         this.searchCursorBlink += var1 * 2.0F;
         if (this.searchCursorBlink > 1.0F) {
            this.searchCursorBlink--;
         }
      }

      this.updateResultAnimations();
      this.updateScrollAnimation(var1);
   }

   private float updateAnimation(float var1, float var2, float var3, float var4) {
      float var5 = var2 - var1;
      return Math.abs(var5) < 0.001F ? var2 : var1 + var5 * var3 * var4;
   }

   private void updateResultAnimations() {
      long var1 = System.currentTimeMillis();

      for (ModuleStructure var4 : this.searchResults) {
         Long var5 = this.searchResultAnimStartTimes.get(var4);
         if (var5 != null) {
            float var6 = (float)(var1 - var5);
            float var7 = Math.min(1.0F, Math.max(0.0F, var6 / 200.0F));
            var7 = this.easeOutCubic(var7);
            this.searchResultAnimations.put(var4, var7);
         }
      }
   }

   private void updateScrollAnimation(float var1) {
      float var2 = this.searchTargetScroll - this.searchScrollOffset;
      if (Math.abs(var2) < 0.5F) {
         this.searchScrollOffset = this.searchTargetScroll;
      } else {
         this.searchScrollOffset += var2 * 12.0F * var1;
      }
   }

   private float easeOutCubic(float var1) {
      return 1.0F - (float)Math.pow(1.0F - var1, 3.0);
   }

   public boolean hasSearchSelection() {
      return this.searchSelectionStart != -1 && this.searchSelectionEnd != -1 && this.searchSelectionStart != this.searchSelectionEnd;
   }

   public int getSearchSelectionStart() {
      return Math.min(this.searchSelectionStart, this.searchSelectionEnd);
   }

   public int getSearchSelectionEnd() {
      return Math.max(this.searchSelectionStart, this.searchSelectionEnd);
   }

   private void clearSearchSelection() {
      this.searchSelectionStart = -1;
      this.searchSelectionEnd = -1;
   }

   private void selectAllSearchText() {
      this.searchSelectionStart = 0;
      this.searchSelectionEnd = this.searchText.length();
      this.searchCursorPosition = this.searchText.length();
   }

   private void deleteSelectedSearchText() {
      if (this.hasSearchSelection()) {
         int var1 = this.getSearchSelectionStart();
         int var2 = this.getSearchSelectionEnd();
         this.searchText = this.searchText.substring(0, var1) + this.searchText.substring(var2);
         this.searchCursorPosition = var1;
         this.clearSearchSelection();
         this.updateSearchResults();
      }
   }

   private String getSelectedSearchText() {
      return !this.hasSearchSelection() ? "" : this.searchText.substring(this.getSearchSelectionStart(), this.getSearchSelectionEnd());
   }

   private void copySearchToClipboard() {
      if (this.hasSearchSelection()) {
         GLFW.glfwSetClipboardString(mc.getWindow().getHandle(), this.getSelectedSearchText());
      }
   }

   private void pasteToSearch() {
      String var1 = GLFW.glfwGetClipboardString(mc.getWindow().getHandle());
      if (var1 != null && !var1.isEmpty()) {
         var1 = var1.replaceAll("[\n\r\t]", "");
         if (this.hasSearchSelection()) {
            this.deleteSelectedSearchText();
         }

         this.searchText = this.searchText.substring(0, this.searchCursorPosition) + var1 + this.searchText.substring(this.searchCursorPosition);
         this.searchCursorPosition = this.searchCursorPosition + var1.length();
         this.updateSearchResults();
      }
   }

   private boolean isControlDown() {
      long var1 = mc.getWindow().getHandle();
      return GLFW.glfwGetKey(var1, 341) == 1 || GLFW.glfwGetKey(var1, 345) == 1;
   }

   private boolean isShiftDown() {
      long var1 = mc.getWindow().getHandle();
      return GLFW.glfwGetKey(var1, 340) == 1 || GLFW.glfwGetKey(var1, 344) == 1;
   }

   public void updateSearchResults() {
      if (this.searchText.isEmpty()) {
         this.searchResults.clear();
         this.searchResultAnimations.clear();
         this.searchResultAnimStartTimes.clear();
         this.searchScrollOffset = 0.0F;
         this.searchTargetScroll = 0.0F;
         this.selectedSearchModule = null;
      } else {
         String var1 = this.searchText.toLowerCase();
         ArrayList var2 = new ArrayList();
         HashMap var3 = new HashMap<>(this.searchResultAnimations);

         try {
            ModuleRepository var4 = Initialization.getInstance().getManager().getModuleRepository();
            if (var4 != null) {
               for (ModuleStructure var6 : var4.modules()) {
                  if (var6.getName().toLowerCase().contains(var1)) {
                     var2.add(var6);
                  }
               }
            }
         } catch (Exception var10) {
         }

         this.searchResultAnimations.clear();
         this.searchResultAnimStartTimes.clear();
         long var11 = System.currentTimeMillis();
         int var12 = 0;

         for (int var7 = 0; var7 < var2.size(); var7++) {
            ModuleStructure var8 = (ModuleStructure)var2.get(var7);
            if (var3.containsKey(var8)) {
               float var9 = (Float)var3.get(var8);
               this.searchResultAnimations.put(var8, Math.max(var9, 0.5F));
               this.searchResultAnimStartTimes.put(var8, var11 - 170L);
            } else {
               this.searchResultAnimations.put(var8, 0.0F);
               this.searchResultAnimStartTimes.put(var8, var11 + var12 * 40L);
               var12++;
            }
         }

         this.searchResults = var2;
         if (!this.searchResults.isEmpty()) {
            if (this.selectedSearchModule == null || !this.searchResults.contains(this.selectedSearchModule)) {
               this.selectedSearchModule = this.searchResults.get(0);
            }
         } else {
            this.selectedSearchModule = null;
         }
      }
   }

   public boolean handleSearchChar(char var1) {
      if (!this.searchActive) {
         return false;
      }

      if (Character.isISOControl(var1)) {
         return false;
      }

      if (this.hasSearchSelection()) {
         this.deleteSelectedSearchText();
      }

      this.searchText = this.searchText.substring(0, this.searchCursorPosition) + var1 + this.searchText.substring(this.searchCursorPosition);
      this.searchCursorPosition++;
      this.clearSearchSelection();
      this.updateSearchResults();
      return true;
   }

   public boolean handleSearchKey(int var1) {
      if (!this.searchActive) {
         return false;
      }

      if (this.isControlDown()) {
         switch (var1) {
            case 65:
               this.selectAllSearchText();
               return true;
            case 67:
               this.copySearchToClipboard();
               return true;
            case 86:
               this.pasteToSearch();
               return true;
            case 88:
               if (this.hasSearchSelection()) {
                  this.copySearchToClipboard();
                  this.deleteSelectedSearchText();
               }

               return true;
         }
      }

      switch (var1) {
         case 256:
            this.setSearchActive(false);
            return true;
         case 257:
            if (this.selectedSearchModule != null) {
               this.selectedSearchModule.switchState();
            }

            return true;
         case 258:
         case 260:
         case 266:
         case 267:
         default:
            return false;
         case 259:
            if (this.hasSearchSelection()) {
               this.deleteSelectedSearchText();
            } else if (this.searchCursorPosition > 0) {
               this.searchText = this.searchText.substring(0, this.searchCursorPosition - 1) + this.searchText.substring(this.searchCursorPosition);
               this.searchCursorPosition--;
               this.updateSearchResults();
            }

            return true;
         case 261:
            if (this.hasSearchSelection()) {
               this.deleteSelectedSearchText();
            } else if (this.searchCursorPosition < this.searchText.length()) {
               this.searchText = this.searchText.substring(0, this.searchCursorPosition) + this.searchText.substring(this.searchCursorPosition + 1);
               this.updateSearchResults();
            }

            return true;
         case 262:
            this.handleRightKey();
            return true;
         case 263:
            this.handleLeftKey();
            return true;
         case 264:
            if (!this.searchResults.isEmpty() && this.selectedSearchModule != null) {
               int var3 = this.searchResults.indexOf(this.selectedSearchModule);
               if (var3 < this.searchResults.size() - 1) {
                  this.selectedSearchModule = this.searchResults.get(var3 + 1);
               }
            }

            return true;
         case 265:
            if (!this.searchResults.isEmpty() && this.selectedSearchModule != null) {
               int var2 = this.searchResults.indexOf(this.selectedSearchModule);
               if (var2 > 0) {
                  this.selectedSearchModule = this.searchResults.get(var2 - 1);
               }
            }

            return true;
         case 268:
            this.handleHomeKey();
            return true;
         case 269:
            this.handleEndKey();
            return true;
      }
   }

   private void handleLeftKey() {
      if (this.hasSearchSelection() && !this.isShiftDown()) {
         this.searchCursorPosition = this.getSearchSelectionStart();
         this.clearSearchSelection();
      } else if (this.searchCursorPosition > 0) {
         if (this.isShiftDown()) {
            if (this.searchSelectionStart == -1) {
               this.searchSelectionStart = this.searchCursorPosition;
            }

            this.searchCursorPosition--;
            this.searchSelectionEnd = this.searchCursorPosition;
         } else {
            this.searchCursorPosition--;
            this.clearSearchSelection();
         }
      }
   }

   private void handleRightKey() {
      if (this.hasSearchSelection() && !this.isShiftDown()) {
         this.searchCursorPosition = this.getSearchSelectionEnd();
         this.clearSearchSelection();
      } else if (this.searchCursorPosition < this.searchText.length()) {
         if (this.isShiftDown()) {
            if (this.searchSelectionStart == -1) {
               this.searchSelectionStart = this.searchCursorPosition;
            }

            this.searchCursorPosition++;
            this.searchSelectionEnd = this.searchCursorPosition;
         } else {
            this.searchCursorPosition++;
            this.clearSearchSelection();
         }
      }
   }

   private void handleHomeKey() {
      if (this.isShiftDown()) {
         if (this.searchSelectionStart == -1) {
            this.searchSelectionStart = this.searchCursorPosition;
         }

         this.searchCursorPosition = 0;
         this.searchSelectionEnd = this.searchCursorPosition;
      } else {
         this.searchCursorPosition = 0;
         this.clearSearchSelection();
      }
   }

   private void handleEndKey() {
      if (this.isShiftDown()) {
         if (this.searchSelectionStart == -1) {
            this.searchSelectionStart = this.searchCursorPosition;
         }

         this.searchCursorPosition = this.searchText.length();
         this.searchSelectionEnd = this.searchCursorPosition;
      } else {
         this.searchCursorPosition = this.searchText.length();
         this.clearSearchSelection();
      }
   }

   public void handleSearchScroll(double var1, float var3) {
      if (this.searchActive && !this.searchResults.isEmpty()) {
         float var4 = Math.max(0.0F, this.searchResults.size() * 20.0F - var3 + 10.0F);
         this.searchTargetScroll = (float)Math.max(-var4, Math.min(0.0, this.searchTargetScroll + var1 * 25.0));
      }
   }

   public float getSearchResultHeight() {
      return 18.0F;
   }

   public void setHoveredSearchIndex(int var1) {
      this.hoveredSearchIndex = var1;
   }

   public boolean isSearchActive() {
      return this.searchActive;
   }

   public String getSearchText() {
      return this.searchText;
   }

   public int getSearchCursorPosition() {
      return this.searchCursorPosition;
   }

   public float getSearchCursorBlink() {
      return this.searchCursorBlink;
   }

   public float getSearchBoxAnimation() {
      return this.searchBoxAnimation;
   }

   public float getSearchFocusAnimation() {
      return this.searchFocusAnimation;
   }

   public float getSearchPanelAlpha() {
      return this.searchPanelAlpha;
   }

   public float getNormalPanelAlpha() {
      return this.normalPanelAlpha;
   }

   public float getSearchSelectionAnimation() {
      return this.searchSelectionAnimation;
   }

   public List<ModuleStructure> getSearchResults() {
      return this.searchResults;
   }

   public Map<ModuleStructure, Float> getSearchResultAnimations() {
      return this.searchResultAnimations;
   }

   public Map<ModuleStructure, Long> getSearchResultAnimStartTimes() {
      return this.searchResultAnimStartTimes;
   }

   public float getSearchScrollOffset() {
      return this.searchScrollOffset;
   }

   public float getSearchTargetScroll() {
      return this.searchTargetScroll;
   }

   public int getHoveredSearchIndex() {
      return this.hoveredSearchIndex;
   }

   public ModuleStructure getSelectedSearchModule() {
      return this.selectedSearchModule;
   }
}
