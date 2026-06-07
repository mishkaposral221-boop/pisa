package rich.screens.clickgui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.Click;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderTickCounter;
import org.lwjgl.glfw.GLFW;
import rich.IMinecraft;
import rich.Initialization;
import rich.modules.module.ModuleRepository;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.screens.clickgui.impl.DragHandler;
import rich.screens.clickgui.impl.background.BackgroundComponent;
import rich.screens.clickgui.impl.configs.ConfigsRenderer;
import rich.screens.clickgui.impl.module.ModuleComponent;
import rich.screens.clickgui.impl.settingsrender.BindComponent;
import rich.screens.clickgui.impl.settingsrender.TextComponent;
import rich.theme.ClientTheme;
import rich.theme.ThemeRenderer;
import rich.util.animations.Direction;
import rich.util.animations.GuiAnimation;
import rich.util.config.impl.drag.GuiLayoutConfig;
import rich.util.interfaces.AbstractSettingComponent;
import rich.util.math.FrameRateCounter;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.shader.Scissor;

public class ClickGui extends Screen implements IMinecraft {
   public static ClickGui INSTANCE = new ClickGui();
   private static final int FIXED_GUI_SCALE = 2;
   private final BackgroundComponent background = new BackgroundComponent();
   private final ModuleComponent moduleComponent = new ModuleComponent();
   private final ConfigsRenderer configsRenderer = new ConfigsRenderer();
   private final ThemeRenderer themeRenderer = new ThemeRenderer();
   private final DragHandler dragHandler = new DragHandler();
   private ModuleCategory selectedCategory = ModuleCategory.VISUALS;
   private final GuiAnimation openAnimation = new GuiAnimation();
   private boolean closing = false;
   private boolean waitingForSlide = false;
   private boolean slideTriggered = false;
   private float hintAlphaAnimation = 0.0F;
   private long lastHintUpdateTime = System.currentTimeMillis();
   private static final float HINT_ANIM_SPEED = 6.0F;
   private static final float OFFSET_THRESHOLD = 5.0F;
   private int lastMouseX;
   private int lastMouseY;
   private float lastDelta;
   private float settingsPanelOffsetX = 0.0F;
   private float settingsPanelOffsetY = 0.0F;
   private boolean settingsPanelDragging = false;
   private double settingsDragStartX;
   private double settingsDragStartY;
   private float settingsDragStartOffX;
   private float settingsDragStartOffY;
   private static final float SP_W = 200.0F;
   private static final float SP_H = 125.0F;
   private static final float SP_HEADER_H = 14.0F;
   private static final float SP_MARGIN = 8.0F;
   private float catBarOffsetX = 0.0F;
   private float catBarOffsetY = 0.0F;
   private boolean catBarDragging = false;
   private double catBarDragStartX;
   private double catBarDragStartY;
   private float catBarDragStartOffX;
   private float catBarDragStartOffY;
   private int catBarSide = 0;
   private final Map<ModuleCategory, Float> catBarAnims = new HashMap<>();
   private static final ModuleCategory[] ALL_CATEGORIES = new ModuleCategory[]{
      ModuleCategory.VISUALS, ModuleCategory.HUD, ModuleCategory.UTILITIES, ModuleCategory.THEMES, ModuleCategory.CONFIGS
   };
   private static final String[] CAT_ICONS = new String[]{"c", "d", "e", "f", "f"};
   private static final float BTN_W = 26.0F;
   private static final float BTN_H = 22.0F;
   private static final float BTN_GAP = 4.0F;
   private static final float BAR_PADDING = 6.0F;
   private static final float BAR_MARGIN = 6.0F;

   public ClickGui() {
      super(Text.of("MenuScreen"));
   }

   public boolean isClosing() {
      return this.closing;
   }

   protected void init() {
      super.init();
      this.closing = false;
      this.waitingForSlide = false;
      this.slideTriggered = false;
      this.openAnimation.setMs(250).setValue(1.0).setDirection(Direction.FORWARDS).reset();
      this.hintAlphaAnimation = 0.0F;
      this.lastHintUpdateTime = System.currentTimeMillis();
      long var1 = mc.getWindow().getHandle();
      double var3 = mc.getWindow().getWidth() / 2.0;
      double var5 = mc.getWindow().getHeight() / 2.0;
      GLFW.glfwSetCursorPos(var1, var3, var5);
      this.background.setSearchActive(false);
      this.loadGuiLayout();
      this.updateModules();
   }

   private void updateModules() {
      ArrayList var1 = new ArrayList();

      try {
         ModuleRepository var2 = Initialization.getInstance().getManager().getModuleRepository();
         if (var2 != null) {
            for (ModuleStructure var4 : var2.modules()) {
               if (var4.getCategory() == this.selectedCategory) {
                  var1.add(var4);
               }
            }
         }
      } catch (Exception var5) {
      }

      this.moduleComponent.updateModules(var1, this.selectedCategory);
   }

   public void openGui() {
      if (mc.currentScreen == null) {
         this.closing = false;
         this.waitingForSlide = false;
         this.slideTriggered = false;
         this.openAnimation.setMs(250).setValue(1.0).setDirection(Direction.FORWARDS).reset();
         mc.setScreen(this);
      }
   }

   public void tick() {
      this.moduleComponent.tick();
      super.tick();
   }

   private float[] calculateBackground(float var1) {
      int var2 = mc.getWindow().getWidth() / 2;
      int var3 = mc.getWindow().getHeight() / 2;
      float var4 = (var2 - 400) / 2.0F + this.dragHandler.getOffsetX();
      float var5 = (var3 - 250) / 2.0F + this.dragHandler.getOffsetY();
      return new float[]{var4, var5, var2, var3};
   }

   private boolean isAnyBindListening() {
      for (AbstractSettingComponent var2 : this.moduleComponent.getSettingComponents()) {
         if (var2 instanceof BindComponent var3 && var3.isListening()) {
            return true;
         }
      }

      return false;
   }

   private void updateHintAnimation() {
      long var1 = System.currentTimeMillis();
      float var3 = Math.min((float)(var1 - this.lastHintUpdateTime) / 1000.0F, 0.1F);
      this.lastHintUpdateTime = var1;
      float var4 = Math.abs(this.dragHandler.getOffsetX());
      float var5 = Math.abs(this.dragHandler.getOffsetY());
      boolean var6 = var4 > 5.0F || var5 > 5.0F;
      float var7 = var6 ? 1.0F : 0.0F;
      float var8 = var7 - this.hintAlphaAnimation;
      if (Math.abs(var8) < 0.001F) {
         this.hintAlphaAnimation = var7;
      } else {
         this.hintAlphaAnimation += var8 * 6.0F * var3;
         this.hintAlphaAnimation = Math.max(0.0F, Math.min(1.0F, this.hintAlphaAnimation));
      }
   }

   private boolean isModuleCategory(ModuleCategory var1) {
      return true;
   }

   public void render(DrawContext var1, int var2, int var3, float var4) {
      this.lastMouseX = var2;
      this.lastMouseY = var3;
      this.lastDelta = var4;
      FrameRateCounter.INSTANCE.recordFrame();
      if (this.waitingForSlide) {
         this.waitingForSlide = false;
         this.startActualClose();
      }

      if (this.closing && !this.waitingForSlide && this.openAnimation.isFinished(Direction.BACKWARDS)) {
         this.closing = false;
         TextComponent.typing = false;
         this.moduleComponent.setBindingModule(null);
         this.dragHandler.stopDrag();
         mc.currentScreen = null;
      }
   }

   public void renderOverlay(DrawContext var1, RenderTickCounter var2) {
      if (mc.getWindow() != null) {
         float var3 = this.lastDelta;
         int var4 = this.lastMouseX;
         int var5 = this.lastMouseY;
         float var6 = Math.min(1.0F, 60.0F / Math.max(FrameRateCounter.INSTANCE.getFps(), 1));
         float var7 = this.openAnimation.getOutput().floatValue();
         int var8 = mc.getWindow().getScaledWidth();
         int var9 = mc.getWindow().getScaledHeight();
         var1.createNewRootLayer();
         int var10 = (int)(125.0F * var7);
         if (var10 > 0) {
            Render2D.rect(0.0F, 0.0F, 5000.0F, 5000.0F, var10 << 24, 0.0F);
         }

         int var11 = mc.getWindow().calculateScaleFactor((Integer)mc.options.getGuiScale().getValue(), mc.forcesUnicodeFont());
         float var12 = 2.0F / var11;
         float var13 = var4 / var12;
         float var14 = var5 / var12;
         if (!this.closing || this.waitingForSlide) {
            this.dragHandler.update(var13, var14);
         }

         this.updateHintAnimation();
         var1.getMatrices().pushMatrix();
         var1.getMatrices().scale(var12, var12);
         float[] var15 = this.calculateBackground(var12);
         float var16 = var15[0];
         float var17 = var15[1];
         int var18 = (int)var15[2];
         int var19 = (int)var15[3];
         float var20;
         if (this.closing && !this.waitingForSlide) {
            var20 = (1.0F - var7) * 30.0F;
         } else {
            var20 = (1.0F - var7) * -15.0F;
         }

         var17 += var20;
         float var21 = var7;
         var1.getMatrices().pushMatrix();
         this.background.render(var1, var16, var17, this.selectedCategory, var3, var21);
         float var22 = var16 + 6.0F;
         float var23 = var17 + 6.0F;
         float var24 = 388.0F;
         float var25 = 238.0F;
         float var26 = var25;
         float var27 = this.background.getNormalPanelAlpha();
         float var28 = this.background.getSearchPanelAlpha();
         if (var27 > 0.01F) {
            if (this.selectedCategory == ModuleCategory.THEMES) {
               this.themeRenderer.render(var1, var22, var23, var24, var25, var13, var14, var21 * var27);
            } else if (this.selectedCategory == ModuleCategory.CONFIGS) {
               this.configsRenderer.render(var1, var16, var17, var13, var14, var3, 2, var21 * var27, this.selectedCategory);
            } else {
               this.moduleComponent.updateScroll(var3, var6);
               this.moduleComponent.updateScrollFades(var3, var6, var25, var26);
               this.moduleComponent.renderModuleList(var1, var22, var23, var24, var25, var13, var14, 2, var21 * var27);
            }
         }

         if (var28 > 0.01F) {
            this.background.renderSearchResults(var1, var16, var17, var13, var14, 2, var21);
         }

         this.renderCategoryBar(var16, var17, this.selectedCategory, var21, var13, var14);
         this.renderSearchBar(var16, var17, var21, var13, var14);
         this.renderSettingsPanelExternal(var1, var16, var17, var13, var14, var3, var21);
         Scissor.reset();
         var1.getMatrices().popMatrix();
         float var29 = this.hintAlphaAnimation * var21;
         if (var29 > 0.01F) {
            int var30 = (int)(255.0F * var29);
            float var31 = var18 / 2.0F;
            float var32 = var19 / 2.0F;
            float var33 = var32 + 125.0F + 10.0F;
         }

         var1.getMatrices().popMatrix();
      }
   }

   public boolean mouseClicked(Click var1, boolean var2) {
      if (this.closing) {
         return false;
      }

      int var3 = mc.getWindow().calculateScaleFactor((Integer)mc.options.getGuiScale().getValue(), mc.forcesUnicodeFont());
      float var4 = 2.0F / var3;
      double var5 = var1.x() / var4;
      double var7 = var1.y() / var4;
      float[] var9 = this.calculateBackground(var4);
      float var10 = var9[0];
      float var11 = this.openAnimation.getOutput().floatValue();
      float var12 = this.closing ? (1.0F - var11) * 30.0F : (1.0F - var11) * -15.0F;
      float var13 = var9[1] + var12;
      if (this.background.isSearchActive()) {
         if (var1.button() == 0) {
            ModuleStructure var21 = this.background.getSearchModuleAtPosition(var5, var7, var10, var13);
            if (var21 != null) {
               var21.switchState();
               return true;
            }

            float var23 = var10 + 6.0F;
            float var24 = var13 + 6.0F;
            float var25 = 388.0F;
            float var28 = 238.0F;
            if (var5 >= var23 && var5 <= var23 + var25 && var7 >= var24 && var7 <= var24 + var28) {
               return true;
            }

            if (!this.background.isSearchBoxHovered(var5, var7, var10, var13)) {
               this.background.setSearchActive(false);
            }
         } else if (var1.button() == 1) {
            ModuleStructure var22 = this.background.getSearchModuleAtPosition(var5, var7, var10, var13);
            if (var22 != null) {
               this.background.setSearchActive(false);
               this.selectedCategory = var22.getCategory();
               this.moduleComponent.selectModuleFromSearch(var22);
               this.updateModules();
               return true;
            }
         }

         return true;
      } else {
         float var14 = var10 + 6.0F;
         float var15 = var13 + 6.0F;
         float var16 = 388.0F;
         float var17 = 238.0F;
         if (var1.button() == 2) {
            if (this.isAnyBindListening()) {
               for (AbstractSettingComponent var19 : this.moduleComponent.getSettingComponents()) {
                  if (var19 instanceof BindComponent var20 && var20.isListening()) {
                     var20.handleMiddleMouseBind();
                     return true;
                  }
               }
            }

            if (this.moduleComponent.getBindingModule() != null) {
               return true;
            }

            ModuleStructure var26 = this.moduleComponent.getModuleAtPosition(var5, var7, var14, var15, var16, var17);
            if (var26 != null) {
               this.moduleComponent.setBindingModule(var26);
               return true;
            }

            if (this.dragHandler.startDrag(var5, var7, var10, var13, 400, 250)) {
               return true;
            }
         }

         ModuleCategory var27 = this.getCategoryBarClick(var5, var7, var10, var13);
         if (var27 != null) {
            this.selectedCategory = var27;
            this.updateModules();
            return true;
         }

         if (var1.button() == 0 && this.isSearchBarClick(var5, var7, var10, var13)) {
            this.background.setSearchActive(!this.background.isSearchActive());
            return true;
         }

         if (this.isModuleCategory(this.selectedCategory)) {
            if (this.selectedCategory == ModuleCategory.THEMES) {
               float var29 = var10 + 6.0F;
               float var31 = var13 + 6.0F;
               if (this.themeRenderer.mouseClicked((float)var5, (float)var7, var29, var31, var1.button())) {
                  return true;
               }
            } else if (this.selectedCategory == ModuleCategory.CONFIGS) {
               if (this.configsRenderer.mouseClicked((float)var5, (float)var7, var1.button(), var10, var13)) {
                  return true;
               }
            } else {
               ModuleStructure var30 = this.moduleComponent.getModuleForStarClick(var5, var7, var14, var15, var16, var17);
               if (var30 != null && var1.button() == 0) {
                  this.moduleComponent.toggleFavorite(var30);
                  return true;
               }

               ModuleStructure var32 = this.moduleComponent.getModuleAtPosition(var5, var7, var14, var15, var16, var17);
               if (var32 != null) {
                  if (var1.button() == 0) {
                     var32.switchState();
                  } else if (var1.button() == 1) {
                     this.moduleComponent.selectModule(var32);
                  }

                  return true;
               }
            }
         }

         if (var1.button() == 0 && this.tryStartSettingsPanelDrag(var5, var7, var10, var13)) {
            return true;
         } else {
            return this.tryClickSettingsPanel(var5, var7, var10, var13, var1.button()) ? true : super.mouseClicked(var1, var2);
         }
      }
   }

   public boolean mouseReleased(Click var1) {
      if (this.closing) {
         return false;
      }

      this.settingsPanelDragging = false;
      if (this.catBarDragging) {
         int var2 = mc.getWindow().calculateScaleFactor((Integer)mc.options.getGuiScale().getValue(), mc.forcesUnicodeFont());
         float var3 = 2.0F / var2;
         float[] var4 = this.calculateBackground(var3);
         float var5 = this.openAnimation.getOutput().floatValue();
         float var6 = this.closing ? (1.0F - var5) * 30.0F : (1.0F - var5) * -15.0F;
         this.snapCatBar(var4[0], var4[1] + var6);
         this.catBarDragging = false;
      }

      for (AbstractSettingComponent var8 : this.moduleComponent.getSettingComponents()) {
         if (var8.getSetting().isVisible() && var8.mouseReleased(var1.x(), var1.y(), var1.button())) {
            return true;
         }
      }

      return super.mouseReleased(var1);
   }

   public boolean mouseScrolled(double var1, double var3, double var5, double var7) {
      if (this.closing) {
         return false;
      }

      if (this.isAnyBindListening()) {
         for (AbstractSettingComponent var10 : this.moduleComponent.getSettingComponents()) {
            if (var10 instanceof BindComponent var11 && var11.isListening()) {
               var11.handleScrollBind(var7);
               return true;
            }
         }
      }

      if (this.moduleComponent.getBindingModule() != null) {
         return true;
      } else {
         int var22 = mc.getWindow().calculateScaleFactor((Integer)mc.options.getGuiScale().getValue(), mc.forcesUnicodeFont());
         float var23 = 2.0F / var22;
         double var24 = var1 / var23;
         double var13 = var3 / var23;
         float[] var15 = this.calculateBackground(var23);
         float var16 = var15[0];
         float var17 = var15[1];
         float var18 = var16 + 6.0F;
         float var19 = var17 + 6.0F;
         float var20 = 388.0F;
         float var21 = 238.0F;
         if (this.background.isSearchActive() && var24 >= var18 && var24 <= var18 + var20 && var13 >= var19 && var13 <= var19 + var21) {
            this.background.handleSearchScroll(var7, var21);
            return true;
         } else if (this.selectedCategory == ModuleCategory.THEMES && var24 >= var18 && var24 <= var18 + var20 && var13 >= var19 && var13 <= var19 + var21) {
            this.themeRenderer.handleScroll(var7);
            return true;
         } else if (this.selectedCategory == ModuleCategory.CONFIGS && this.configsRenderer.mouseScrolled(var24, var13, var7, var16, var17)) {
            return true;
         } else if (var24 >= var18 && var24 <= var18 + var20 && var13 >= var19 && var13 <= var19 + var21) {
            this.moduleComponent.handleModuleScroll(var7, var21);
            return true;
         } else {
            return this.tryScrollSettingsPanel(var24, var13, var16, var17, var7) ? true : super.mouseScrolled(var1, var3, var5, var7);
         }
      }
   }

   public boolean keyPressed(KeyInput var1) {
      if (var1.key() == 256) {
         if (this.configsRenderer.isEditing()) {
            this.configsRenderer.keyPressed(256);
            return true;
         } else if (this.background.isSearchActive()) {
            this.background.setSearchActive(false);
            return true;
         } else {
            this.close();
            return true;
         }
      } else {
         if (this.closing) {
            return false;
         }

         if (var1.key() == 70 && (var1.modifiers() & 2) != 0) {
            this.background.setSearchActive(!this.background.isSearchActive());
            return true;
         }

         if (this.background.isSearchActive() && this.background.handleSearchKey(var1.key())) {
            return true;
         }

         if (this.dragHandler.isResetNeeded(var1.key(), var1.modifiers())) {
            this.dragHandler.reset();
            return true;
         }

         ModuleStructure var2 = this.moduleComponent.getBindingModule();
         if (var2 != null) {
            var2.setKey(var1.key() == 261 ? -1 : var1.key());
            this.moduleComponent.setBindingModule(null);
            return true;
         }

         for (AbstractSettingComponent var4 : this.moduleComponent.getSettingComponents()) {
            if (var4.getSetting().isVisible() && var4.keyPressed(var1.key(), var1.scancode(), var1.modifiers())) {
               return true;
            }
         }

         return this.selectedCategory == ModuleCategory.CONFIGS && this.configsRenderer.keyPressed(var1.key()) ? true : super.keyPressed(var1);
      }
   }

   public boolean charTyped(CharInput var1) {
      if (this.closing) {
         return false;
      }

      if (this.background.isSearchActive() && this.background.handleSearchChar((char)var1.codepoint())) {
         return true;
      }

      for (AbstractSettingComponent var3 : this.moduleComponent.getSettingComponents()) {
         if (var3.getSetting().isVisible() && var3.charTyped((char)var1.codepoint(), var1.modifiers())) {
            return true;
         }
      }

      return this.selectedCategory == ModuleCategory.CONFIGS && this.configsRenderer.charTyped((char)var1.codepoint()) ? true : super.charTyped(var1);
   }

   public boolean shouldPause() {
      return false;
   }

   private float[] getSettingsPanelBounds(float var1, float var2) {
      float var3 = var1 + 400.0F + 8.0F + this.settingsPanelOffsetX;
      float var4 = var2 + 62.5F + this.settingsPanelOffsetY;
      return new float[]{var3, var4};
   }

   private void renderSettingsPanelExternal(DrawContext var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      float[] var8 = this.getSettingsPanelBounds(var2, var3);
      float var9 = var8[0];
      float var10 = var8[1];
      if (this.settingsPanelDragging) {
         if (GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), 0) != 1) {
            this.settingsPanelDragging = false;
         } else {
            this.settingsPanelOffsetX = this.settingsDragStartOffX + (float)(var4 - this.settingsDragStartX);
            this.settingsPanelOffsetY = this.settingsDragStartOffY + (float)(var5 - this.settingsDragStartY);
         }
      }

      int var11 = (int)(220.0F * var7);
      int var12 = (int)(255.0F * var7);
      Render2D.gradientRect(var9, var10, 200.0F, 125.0F, ClientTheme.bgGradient(var11), 10.0F);
      Render2D.outline(var9, var10, 200.0F, 125.0F, 0.5F, ClientTheme.outline(var12), 10.0F);
      int var13 = (int)(30.0F * var7);
      Render2D.rect(var9 + 1.0F, var10 + 1.0F, 198.0F, 14.0F, ClientTheme.panel(var13), 9.0F);
      String var14 = this.moduleComponent.getSelectedModule() != null ? this.moduleComponent.getSelectedModule().getName() : "Settings";
      float var15 = Fonts.BOLD.getWidth(var14, 6.0F);
      Fonts.BOLD.draw(var14, var9 + (200.0F - var15) / 2.0F, var10 + 4.0F + 1.0F, 6.0F, (int)(200.0F * var7) << 24 | 13158600);
      Render2D.rect(var9 + 6.0F, var10 + 14.0F + 1.0F, 188.0F, 0.5F, (int)(80.0F * var7) << 24 | 5263440, 1.0F);
      float var16 = var9;
      float var17 = var10 + 14.0F + 3.0F;
      float var18 = 200.0F;
      float var19 = 107.0F;
      this.moduleComponent.renderSettingsPanel(var1, var16, var17, var18, var19, var4, var5, var6, 2, var7);
   }

   private boolean tryStartSettingsPanelDrag(double var1, double var3, float var5, float var6) {
      float[] var7 = this.getSettingsPanelBounds(var5, var6);
      float var8 = var7[0];
      float var9 = var7[1];
      if (var1 >= var8 && var1 <= var8 + 200.0F && var3 >= var9 && var3 <= var9 + 14.0F) {
         this.settingsPanelDragging = true;
         this.settingsDragStartX = var1;
         this.settingsDragStartY = var3;
         this.settingsDragStartOffX = this.settingsPanelOffsetX;
         this.settingsDragStartOffY = this.settingsPanelOffsetY;
         return true;
      } else {
         return false;
      }
   }

   private boolean tryClickSettingsPanel(double var1, double var3, float var5, float var6, int var7) {
      float[] var8 = this.getSettingsPanelBounds(var5, var6);
      float var9 = var8[0];
      float var10 = var8[1];
      float var11 = var9;
      float var12 = var10 + 14.0F + 3.0F;
      float var13 = 200.0F;
      float var14 = 107.0F;
      if (var1 >= var11 && var1 <= var11 + var13 && var3 >= var12 && var3 <= var12 + var14) {
         for (AbstractSettingComponent var16 : this.moduleComponent.getSettingComponents()) {
            if (var16.getSetting().isVisible() && var16.mouseClicked(var1, var3, var7)) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean tryScrollSettingsPanel(double var1, double var3, float var5, float var6, double var7) {
      float[] var9 = this.getSettingsPanelBounds(var5, var6);
      float var10 = var9[0];
      float var11 = var9[1];
      if (var1 >= var10 && var1 <= var10 + 200.0F && var3 >= var11 && var3 <= var11 + 125.0F) {
         this.moduleComponent.handleSettingScroll(var7, 107.0F);
         return true;
      } else {
         return false;
      }
   }

   private float[] getCatBarLayout(float var1, float var2) {
      int var3 = ALL_CATEGORIES.length;
      float var4 = var3 * 26.0F + (var3 - 1) * 4.0F + 12.0F;
      float var5 = 34.0F;
      float var6 = 38.0F;
      float var7 = var3 * 22.0F + (var3 - 1) * 4.0F + 12.0F;
      boolean var12 = this.catBarSide == 2 || this.catBarSide == 3;
      float var10;
      float var11;
      if (var12) {
         var10 = var6;
         var11 = var7;
      } else {
         var10 = var4;
         var11 = var5;
      }

      float var8;
      float var9;
      switch (this.catBarSide) {
         case 0:
            var8 = var1 + (400.0F - var10) / 2.0F;
            var9 = var2 + 250.0F + 6.0F;
            break;
         case 1:
            var8 = var1 + (400.0F - var10) / 2.0F;
            var9 = var2 - var11 - 6.0F;
            break;
         case 2:
            var8 = var1 - var10 - 6.0F;
            var9 = var2 + (250.0F - var11) / 2.0F;
            break;
         default:
            var8 = var1 + 400.0F + 6.0F;
            var9 = var2 + (250.0F - var11) / 2.0F;
      }

      var8 += this.catBarOffsetX;
      var9 += this.catBarOffsetY;
      return new float[]{var8, var9, var10, var11, var12 ? 1.0F : 0.0F};
   }

   private void snapCatBar(float var1, float var2) {
      int var3 = ALL_CATEGORIES.length;
      float var4 = var3 * 26.0F + (var3 - 1) * 4.0F + 12.0F;
      float var5 = 34.0F;
      float var6 = 38.0F;
      float var7 = var3 * 22.0F + (var3 - 1) * 4.0F + 12.0F;
      float[] var8 = this.getCatBarLayout(var1, var2);
      float var9 = var8[0] + var8[2] / 2.0F;
      float var10 = var8[1] + var8[3] / 2.0F;
      float var11 = var1 + 200.0F;
      float var12 = var2 + 125.0F;
      float var13 = var9 - var11;
      float var14 = var10 - var12;
      int var15;
      if (Math.abs(var13) > Math.abs(var14)) {
         var15 = var13 > 0.0F ? 3 : 2;
      } else {
         var15 = var14 > 0.0F ? 0 : 1;
      }

      this.catBarSide = var15;
      this.catBarOffsetX = 0.0F;
      this.catBarOffsetY = 0.0F;
   }

   private void renderCategoryBar(float var1, float var2, ModuleCategory var3, float var4, float var5, float var6) {
      if (this.catBarDragging) {
         if (GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), 0) != 1) {
            this.catBarDragging = false;
            this.snapCatBar(var1, var2);
         } else {
            this.catBarOffsetX = this.catBarDragStartOffX + (float)(var5 - this.catBarDragStartX);
            this.catBarOffsetY = this.catBarDragStartOffY + (float)(var6 - this.catBarDragStartY);
         }
      }

      float[] var7 = this.getCatBarLayout(var1, var2);
      float var8 = var7[0];
      float var9 = var7[1];
      float var10 = var7[2];
      float var11 = var7[3];
      boolean var12 = var7[4] > 0.5F;
      int var13 = ALL_CATEGORIES.length;
      int var14 = (int)(200.0F * var4);
      int var15 = (int)(255.0F * var4);
      Render2D.gradientRect(var8, var9, var10, var11, ClientTheme.bgGradient(var14), 10.0F);
      Render2D.outline(var8, var9, var10, var11, 0.5F, ClientTheme.outline(var15), 10.0F);
      float var16 = 0.016F;

      for (int var17 = 0; var17 < var13; var17++) {
         ModuleCategory var18 = ALL_CATEGORIES[var17];
         float var19 = this.catBarAnims.getOrDefault(var18, 0.0F);
         float var20 = var18 == var3 ? 1.0F : 0.0F;
         var19 += (var20 - var19) * 10.0F * var16;
         if (Math.abs(var20 - var19) < 0.005F) {
            var19 = var20;
         }

         this.catBarAnims.put(var18, var19);
         float var21;
         float var22;
         if (var12) {
            var21 = var8 + 6.0F;
            var22 = var9 + 6.0F + var17 * 26.0F;
         } else {
            var21 = var8 + 6.0F + var17 * 30.0F;
            var22 = var9 + 6.0F;
         }

         boolean var23 = var5 >= var21 && var5 <= var21 + 26.0F && var6 >= var22 && var6 <= var22 + 22.0F;
         float var24 = var23 ? 1.0F : 0.0F;
         int var25 = (int)((var19 * 50.0F + var24 * 20.0F) * var4);
         if (var25 > 0) {
            Render2D.rect(var21, var22, 26.0F, 22.0F, var25 << 24 | ClientTheme.panel(255), 6.0F);
         }

         if (var19 > 0.01F) {
            float var26 = 2.5F * var19;
            float var27;
            float var28;
            if (var12) {
               if (this.catBarSide == 2) {
                  var27 = var21 + 26.0F - 4.0F;
                  var28 = var22 + 11.0F - var26 / 2.0F;
               } else {
                  var27 = var21 + 1.5F;
                  var28 = var22 + 11.0F - var26 / 2.0F;
               }
            } else if (this.catBarSide == 1) {
               var27 = var21 + 13.0F - var26 / 2.0F;
               var28 = var22 + 22.0F - 4.0F;
            } else {
               var27 = var21 + 13.0F - var26 / 2.0F;
               var28 = var22 + 1.5F;
            }

            int var29 = (int)(200.0F * var19 * var4);
            Render2D.rect(var27, var28, var26, var26, var29 << 24 | 16777215, var26 / 2.0F);
            Render2D.outline(var21, var22, 26.0F, 22.0F, 0.5F, ClientTheme.outline((int)(120.0F * var19 * var4)), 6.0F);
         }

         String var35 = CAT_ICONS[var17];
         float var36 = 9.0F;
         float var37 = Fonts.CATEGORY_ICONS.getWidth(var35, var36);
         float var38 = Fonts.CATEGORY_ICONS.getHeight(var36);
         float var30 = var21 + (26.0F - var37) / 2.0F;
         float var31 = var22 + (22.0F - var38) / 2.0F - 1.0F;
         int var32 = (int)(140.0F + 115.0F * Math.max(var19, var24));
         int var33 = (int)((160.0F + 95.0F * Math.max(var19, var24)) * var4);
         var32 = Math.min(255, var32);
         var33 = Math.min(255, var33);
         Fonts.CATEGORY_ICONS.draw(var35, var30, var31, var36, var33 << 24 | var32 << 16 | var32 << 8 | var32);
      }
   }

   private ModuleCategory getCategoryBarClick(double var1, double var3, float var5, float var6) {
      float[] var7 = this.getCatBarLayout(var5, var6);
      float var8 = var7[0];
      float var9 = var7[1];
      float var10 = var7[2];
      float var11 = var7[3];
      boolean var12 = var7[4] > 0.5F;
      int var13 = ALL_CATEGORIES.length;
      if (var1 >= var8 && var1 <= var8 + var10 && var3 >= var9 && var3 <= var9 + var11) {
         boolean var14 = false;

         for (int var15 = 0; var15 < var13; var15++) {
            float var16 = var12 ? var8 + 6.0F : var8 + 6.0F + var15 * 30.0F;
            float var17 = var12 ? var9 + 6.0F + var15 * 26.0F : var9 + 6.0F;
            if (var1 >= var16 && var1 <= var16 + 26.0F && var3 >= var17 && var3 <= var17 + 22.0F) {
               var14 = true;
               break;
            }
         }

         if (!var14) {
            this.catBarDragging = true;
            this.catBarDragStartX = var1;
            this.catBarDragStartY = var3;
            this.catBarDragStartOffX = this.catBarOffsetX;
            this.catBarDragStartOffY = this.catBarOffsetY;
            return null;
         }
      }

      for (int var18 = 0; var18 < var13; var18++) {
         float var19 = var12 ? var8 + 6.0F : var8 + 6.0F + var18 * 30.0F;
         float var20 = var12 ? var9 + 6.0F + var18 * 26.0F : var9 + 6.0F;
         if (var1 >= var19 && var1 <= var19 + 26.0F && var3 >= var20 && var3 <= var20 + 22.0F) {
            return ALL_CATEGORIES[var18];
         }
      }

      return null;
   }

   private void saveGuiLayout() {
      GuiLayoutConfig.getInstance().save(this.settingsPanelOffsetX, this.settingsPanelOffsetY, this.catBarOffsetX, this.catBarOffsetY, this.catBarSide);
   }

   private void loadGuiLayout() {
      float[] var1 = GuiLayoutConfig.getInstance().load();
      this.settingsPanelOffsetX = var1[0];
      this.settingsPanelOffsetY = var1[1];
      this.catBarOffsetX = var1[2];
      this.catBarOffsetY = var1[3];
      this.catBarSide = (int)var1[4];
   }

   private void startActualClose() {
      this.saveGuiLayout();
      this.openAnimation.setDirection(Direction.BACKWARDS);
      this.openAnimation.reset();
      long var1 = mc.getWindow().getHandle();
      double var3 = mc.getWindow().getWidth() / 2.0;
      double var5 = mc.getWindow().getHeight() / 2.0;
      GLFW.glfwSetInputMode(var1, 208897, 212995);
      GLFW.glfwSetCursorPos(var1, var3, var5);
      TextComponent.typing = false;
      this.moduleComponent.setBindingModule(null);
      this.background.setSearchActive(false);
      this.dragHandler.stopDrag();
   }

   private boolean isSearchBarClick(double var1, double var3, float var5, float var6) {
      float[] var7 = this.getCatBarLayout(var5, var6);
      float var8 = var7[0];
      float var9 = var7[1];
      float var10 = var7[2];
      float var11 = var7[3];
      boolean var12 = var7[4] > 0.5F;
      float var13 = 80.0F;
      float var14 = 16.0F;
      float var15;
      float var16;
      if (var12) {
         var15 = var8;
         var16 = var9 + var11 + 4.0F;
         var13 = var10;
      } else {
         var15 = var8 + (var10 - var13) / 2.0F;
         var16 = var9 + var11 + 4.0F;
      }

      return var1 >= var15 && var1 <= var15 + var13 && var3 >= var16 && var3 <= var16 + var14;
   }

   private void renderSearchBar(float var1, float var2, float var3, float var4, float var5) {
      float[] var6 = this.getCatBarLayout(var1, var2);
      float var7 = var6[0];
      float var8 = var6[1];
      float var9 = var6[2];
      float var10 = var6[3];
      boolean var11 = var6[4] > 0.5F;
      float var12 = 80.0F;
      float var13 = 16.0F;
      float var14;
      float var15;
      if (var11) {
         var14 = var7;
         var15 = var8 + var10 + 4.0F;
         var12 = var9;
      } else {
         var14 = var7 + (var9 - var12) / 2.0F;
         var15 = var8 + var10 + 4.0F;
      }

      boolean var16 = var4 >= var14 && var4 <= var14 + var12 && var5 >= var15 && var5 <= var15 + var13;
      boolean var17 = this.background.isSearchActive();
      int var18 = (int)((var17 ? 220 : 180) * var3);
      Render2D.gradientRect(var14, var15, var12, var13, ClientTheme.bgGradient(var18), 6.0F);
      Render2D.outline(var14, var15, var12, var13, 0.5F, ClientTheme.outline((int)((!var17 && !var16 ? 120 : 255) * var3)), 6.0F);
      String var19 = var17 && !this.background.getSearchText().isEmpty() ? this.background.getSearchText() : (var17 ? "|" : "Стрелка • Ctrl+F");
      int var20 = var17 ? (int)(220.0F * var3) << 24 | 16777215 : (int)(180.0F * var3) << 24 | 9211020;
      float var21 = Fonts.BOLD.getWidth(var19, 5.0F);
      Fonts.BOLD.draw(var19, var14 + (var12 - var21) / 2.0F, var15 + (var13 - 5.0F) / 2.0F, 5.0F, var20);
   }

   public void close() {
      if (!this.closing) {
         this.closing = true;
         this.waitingForSlide = false;
         this.startActualClose();
      }
   }
}
