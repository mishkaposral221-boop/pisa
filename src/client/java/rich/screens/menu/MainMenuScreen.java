package rich.screens.menu;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.Click;
import net.minecraft.util.Util;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerWarningScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.SkinTextures;
import rich.Initialization;
import rich.online.OnlineTracker;
import rich.screens.account.AccountEntry;
import rich.screens.account.AccountRenderer;
import rich.update.UpdateChecker;
import rich.update.UpdateNotification;
import rich.util.b;
import rich.util.config.impl.account.AccountConfig;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.session.SessionChanger;
import rich.util.sounds.SoundManager;

public class MainMenuScreen extends Screen {
   private static final Identifier BACKGROUND_TEXTURE = Identifier.of("rich", "textures/menu/backmenu.png");
   private static final Identifier STEVE_SKIN = Identifier.of("minecraft", "textures/entity/player/wide/steve.png");
   private static final float FIXED_GUI_SCALE = 2.0F;
   private static final int BUTTON_SIZE = 42;
   private static final int BUTTON_SPACING = 16;
   private static final float BLUR_RADIUS = 15.0F;
   private static final float OUTLINE_THICKNESS = 1.0F;
   private static final String[] BUTTON_ICONS = new String[]{"a", "b", "x", "s", "i"};
   private static final float LEFT_PANEL_WIDTH = 100.0F;
   private static final float LEFT_PANEL_TOP_HEIGHT = 100.0F;
   private static final float LEFT_PANEL_BOTTOM_HEIGHT = 58.0F;
   private static final float RIGHT_PANEL_WIDTH = 300.0F;
   private static final float RIGHT_PANEL_HEIGHT = 165.0F;
   private static final float GAP = 5.0F;
   private static final long UNLOCK_FADE_DURATION = 300L;
   private static final long MENU_APPEAR_DURATION = 800L;
   private static final long MENU_APPEAR_DELAY = 200L;
   private static final long VIEW_FADE_OUT_DURATION = 200L;
   private static final long VIEW_FADE_IN_DURATION = 250L;
   private static final float SLIDE_DISTANCE = 40.0F;
   private static final float ZOOM_INITIAL = 1.08F;
   private static final float ZOOM_NORMAL = 1.0F;
   private static final float ZOOM_SPEED = 3.0F;
   private MainMenuScreen.View currentView = MainMenuScreen.View.MAIN_MENU;
   private MainMenuScreen.View targetView = MainMenuScreen.View.MAIN_MENU;
   private MainMenuScreen.TransitionPhase transitionPhase = MainMenuScreen.TransitionPhase.NONE;
   private long transitionStart = 0L;
   private long screenStartTime = 0L;
   private boolean initialized = false;
   private long lastRenderTime = 0L;
   private float[] buttonScales = new float[5];
   private float[] buttonHoverProgress = new float[5];
   private int hoveredButton = -1;
   private float exitButtonRedProgress = 0.0F;
   private boolean welcomeSoundPlayed = false;
   private boolean isUnlocked = false;
   private long unlockTime = 0L;
   private float unlockTextPulse = 0.0F;
   private float currentZoom = 1.08F;
   private float targetZoom = 1.08F;
   private final AccountRenderer accountRenderer;
   private final AccountConfig accountConfig;
   private final UpdateNotification updateNotification = new UpdateNotification();
   private String nicknameText = "";
   private boolean nicknameFieldFocused = false;
   private float scrollOffset = 0.0F;
   private float targetScrollOffset = 0.0F;
   private long hwidCopiedTime = 0L;
   private boolean hwidHovered = false;

   public MainMenuScreen() {
      super(Text.literal("Main Menu"));

      for (int var1 = 0; var1 < 5; var1++) {
         this.buttonScales[var1] = 1.0F;
         this.buttonHoverProgress[var1] = 0.0F;
      }

      this.accountRenderer = new AccountRenderer();
      this.accountConfig = AccountConfig.getInstance();
      this.accountConfig.load();
   }

   protected void init() {
      OnlineTracker.getInstance().start();
      this.initialized = false;
      this.unlock();
   }

   private int getFixedScaledWidth() {
      return (int)Math.ceil(this.client.getWindow().getFramebufferWidth() / 2.0);
   }

   private int getFixedScaledHeight() {
      return (int)Math.ceil(this.client.getWindow().getFramebufferHeight() / 2.0);
   }

   private float getScaleMultiplier() {
      float var1 = this.client.getWindow().getScaleFactor();
      return var1 / 2.0F;
   }

   private float toFixedCoord(double var1) {
      float var3 = this.client.getWindow().getScaleFactor();
      return (float)(var1 * var3 / 2.0);
   }

   private void unlock() {
      if (!this.isUnlocked) {
         this.isUnlocked = true;
         this.unlockTime = Util.getMeasuringTimeMs();
         this.targetZoom = 1.0F;
      }
   }

   private float getUnlockTextAlpha(long var1) {
      if (!this.isUnlocked) {
         return 1.0F;
      }

      long var3 = var1 - this.unlockTime;
      return 1.0F - MathHelper.clamp((float)var3 / 300.0F, 0.0F, 1.0F);
   }

   private float getMenuProgress(long var1) {
      if (!this.isUnlocked) {
         return 0.0F;
      }

      long var3 = var1 - this.unlockTime - 200L;
      return var3 < 0L ? 0.0F : MathHelper.clamp((float)var3 / 800.0F, 0.0F, 1.0F);
   }

   private float easeOutCubic(float var1) {
      return 1.0F - (float)Math.pow(1.0F - var1, 3.0);
   }

   private float easeInCubic(float var1) {
      return var1 * var1 * var1;
   }

   private float easeOutQuart(float var1) {
      return 1.0F - (float)Math.pow(1.0F - var1, 4.0);
   }

   private void switchToView(MainMenuScreen.View var1) {
      if (this.currentView != var1 && this.transitionPhase == MainMenuScreen.TransitionPhase.NONE) {
         this.targetView = var1;
         this.transitionPhase = MainMenuScreen.TransitionPhase.FADE_OUT;
         this.transitionStart = Util.getMeasuringTimeMs();
      }
   }

   private void updateTransition(long var1) {
      if (this.transitionPhase != MainMenuScreen.TransitionPhase.NONE) {
         long var3 = var1 - this.transitionStart;
         if (this.transitionPhase == MainMenuScreen.TransitionPhase.FADE_OUT) {
            if (var3 >= 200L) {
               this.currentView = this.targetView;
               this.transitionPhase = MainMenuScreen.TransitionPhase.FADE_IN;
               this.transitionStart = var1;
            }
         } else if (this.transitionPhase == MainMenuScreen.TransitionPhase.FADE_IN && var3 >= 250L) {
            this.transitionPhase = MainMenuScreen.TransitionPhase.NONE;
         }
      }
   }

   private float getMainMenuAlpha(long var1) {
      if (this.currentView == MainMenuScreen.View.ALT_SCREEN && this.transitionPhase == MainMenuScreen.TransitionPhase.NONE) {
         return 0.0F;
      } else if (this.currentView == MainMenuScreen.View.MAIN_MENU && this.transitionPhase == MainMenuScreen.TransitionPhase.NONE) {
         return 1.0F;
      } else {
         long var3 = var1 - this.transitionStart;
         if (this.transitionPhase == MainMenuScreen.TransitionPhase.FADE_OUT) {
            return this.currentView == MainMenuScreen.View.MAIN_MENU
               ? 1.0F - this.easeInCubic(MathHelper.clamp((float)var3 / 200.0F, 0.0F, 1.0F))
               : 0.0F;
         } else if (this.transitionPhase == MainMenuScreen.TransitionPhase.FADE_IN) {
            return this.currentView == MainMenuScreen.View.MAIN_MENU ? this.easeOutCubic(MathHelper.clamp((float)var3 / 250.0F, 0.0F, 1.0F)) : 0.0F;
         } else {
            return this.currentView == MainMenuScreen.View.MAIN_MENU ? 1.0F : 0.0F;
         }
      }
   }

   private float getAltScreenAlpha(long var1) {
      if (this.currentView == MainMenuScreen.View.MAIN_MENU && this.transitionPhase == MainMenuScreen.TransitionPhase.NONE) {
         return 0.0F;
      } else if (this.currentView == MainMenuScreen.View.ALT_SCREEN && this.transitionPhase == MainMenuScreen.TransitionPhase.NONE) {
         return 1.0F;
      } else {
         long var3 = var1 - this.transitionStart;
         if (this.transitionPhase == MainMenuScreen.TransitionPhase.FADE_OUT) {
            return this.currentView == MainMenuScreen.View.ALT_SCREEN
               ? 1.0F - this.easeInCubic(MathHelper.clamp((float)var3 / 200.0F, 0.0F, 1.0F))
               : 0.0F;
         } else if (this.transitionPhase == MainMenuScreen.TransitionPhase.FADE_IN) {
            return this.currentView == MainMenuScreen.View.ALT_SCREEN ? this.easeOutCubic(MathHelper.clamp((float)var3 / 250.0F, 0.0F, 1.0F)) : 0.0F;
         } else {
            return this.currentView == MainMenuScreen.View.ALT_SCREEN ? 1.0F : 0.0F;
         }
      }
   }

   private void drawBackground(float var1) {
      int var2 = this.getFixedScaledWidth();
      int var3 = this.getFixedScaledHeight();
      float var4 = var2 * var1;
      float var5 = var3 * var1;
      float var6 = (var2 - var4) / 2.0F;
      float var7 = (var3 - var5) / 2.0F;
      int[] var8 = new int[]{-1, -1, -1, -1};
      float[] var9 = new float[]{0.0F, 0.0F, 0.0F, 0.0F};
      Initialization.getInstance()
         .getManager()
         .getRenderCore()
         .getTexturePipeline()
         .drawTexture(BACKGROUND_TEXTURE, var6, var7, var4, var5, 0.0F, 0.0F, 1.0F, 1.0F, var8, var9, 1.0F);
   }

   public void render(DrawContext var1, int var2, int var3, float var4) {
      long var5 = Util.getMeasuringTimeMs();
      if (!this.initialized) {
         this.screenStartTime = var5;
         this.lastRenderTime = var5;
         this.initialized = true;
      }

      float var7 = (float)(var5 - this.lastRenderTime) / 1000.0F;
      this.lastRenderTime = var5;
      var7 = MathHelper.clamp(var7, 0.0F, 0.1F);
      this.updateTransition(var5);
      this.unlockTextPulse += var7 * 3.0F;
      this.currentZoom = MathHelper.lerp(var7 * 3.0F, this.currentZoom, this.targetZoom);
      float var8 = 12.0F;
      float var9 = this.targetScrollOffset - this.scrollOffset;
      this.scrollOffset = this.scrollOffset + var9 * Math.min(1.0F, var7 * var8);
      if (Math.abs(var9) < 0.1F) {
         this.scrollOffset = this.targetScrollOffset;
      }

      float var10 = this.getUnlockTextAlpha(var5);
      float var11 = this.easeOutQuart(this.getMenuProgress(var5));
      float var12 = this.getMainMenuAlpha(var5);
      float var13 = this.getAltScreenAlpha(var5);
      if (!this.welcomeSoundPlayed && var11 > 0.1F) {
         SoundManager.playSoundDirect(SoundManager.WELCOME, 1.0F, 1.0F);
         this.welcomeSoundPlayed = true;
      }

      float var14 = this.toFixedCoord(var2);
      float var15 = this.toFixedCoord(var3);
      int var16 = this.getFixedScaledWidth();
      int var17 = this.getFixedScaledHeight();
      boolean var18 = this.currentView == MainMenuScreen.View.MAIN_MENU && this.transitionPhase == MainMenuScreen.TransitionPhase.NONE && var11 > 0.8F;
      if (this.currentView == MainMenuScreen.View.ALT_SCREEN && this.transitionPhase == MainMenuScreen.TransitionPhase.NONE) {
         boolean var33 = true;
      } else {
         boolean var10000 = false;
      }

      this.hoveredButton = var18 ? this.getHoveredButton(var14, var15, var16, var17, var11) : -1;
      this.updateButtonAnimations(var7);
      if (this.currentView == MainMenuScreen.View.MAIN_MENU && var11 > 0.3F) {
         float var20 = this.getHwidCopyBtnX(var17);
         float var21 = this.getHwidCopyBtnY(var17);
         String var22 = b.a();
         float var23 = 5.5F;
         float var24 = 8.0F;
         float var25 = 5.0F;
         float var26 = Fonts.BOLD.getHeight(var23);
         float var27 = var26 + var25 * 2.0F;
         String var28 = Util.getMeasuringTimeMs() - this.hwidCopiedTime < 1500L ? "✓ Скопировано" : "[копировать]";
         float var29 = Fonts.BOLD.getWidth(var28, var23) + var24 * 2.0F;
         this.hwidHovered = var14 >= var20 && var14 <= var20 + var29 && var15 >= var21 && var15 <= var21 + var27;
      } else {
         this.hwidHovered = false;
      }

      Render2D.beginOverlay();
      this.drawBackground(this.currentZoom);
      if (var12 > 0.01F) {
         this.renderMainMenuContent(var16, var17, var14, var15, var11, var12, var10, var5);
      }

      if (var13 > 0.01F) {
         this.renderAltScreenContent(var16, var17, var14, var15, var13, var5);
      }

      Render2D.blur(var14, var15, 1.0F, 1.0F, 15.0F, 1.0F, new Color(128, 128, 128, 0).getRGB());
      Fonts.TEST.drawCentered("RunTime Visuals © All Rights Reserved", var16 / 2.0F, var17 - 6, 5.0F, new Color(128, 128, 128, 128).getRGB());
      if (var11 > 0.3F) {
         this.renderOnlineWidget(var16, var11 * var12);
         this.renderHwidWidget(var16, var17, var11 * var12);
      }

      UpdateChecker var31 = UpdateChecker.getInstance();
      UpdateChecker.UpdateInfo var32 = var31.getPendingUpdate();
      if (var32 != null && !var31.isNotified() && !this.updateNotification.isVisible()) {
         this.updateNotification.show();
         var31.markNotified();
      }

      if (this.updateNotification.isVisible() && var32 != null) {
         this.updateNotification.render(var16, var17, var14, var15, var32);
      }

      Render2D.blur(var14, var15, 1.0F, 1.0F, 15.0F, 1.0F, new Color(128, 128, 128, 0).getRGB());
      Render2D.endOverlay();
   }

   private void renderMainMenuContent(int var1, int var2, float var3, float var4, float var5, float var6, float var7, long var8) {
      float var10 = (1.0F - var6) * 20.0F;
      if (var5 > 0.01F) {
         this.renderTime(var5 * var6, var1, var2, var5, var10);
         this.renderButtons(var3, var4, var5 * var6, var1, var2, var5, var10);
      }
   }

   private void renderAltScreenContent(int var1, int var2, float var3, float var4, float var5, long var6) {
      float var8 = 405.0F;
      float var9 = 163.0F;
      float var10 = var1 / 2.0F;
      float var11 = var2 / 2.0F;
      float var12 = var10 - var8 / 2.0F;
      float var13 = var11 - var9 / 2.0F;
      float var14 = var12;
      float var15 = var13;
      float var16 = (1.0F - var5) * -40.0F;
      if (var5 > 0.01F) {
         this.accountRenderer
            .renderLeftPanelTop(var14 + var16, var15, 100.0F, 100.0F, var5, this.nicknameText, this.nicknameFieldFocused, var3 - var16, var4, var6);
      }

      float var17 = var13 + 100.0F + 5.0F;
      float var18 = (1.0F - var5) * 40.0F;
      if (var5 > 0.01F) {
         this.accountRenderer
            .renderLeftPanelBottom(
               var14,
               var17 + var18,
               100.0F,
               58.0F,
               var5,
               this.accountConfig.getActiveAccountName(),
               this.accountConfig.getActiveAccountDate(),
               this.accountConfig.getActiveAccountSkin()
            );
      }

      float var19 = var12 + 100.0F + 5.0F;
      float var20 = var13;
      byte var21 = 2;
      List var22 = this.accountConfig.getSortedAccounts();
      float var23 = (1.0F - var5) * 40.0F;
      if (var5 > 0.01F) {
         this.accountRenderer.renderRightPanel(var19 + var23, var20, 300.0F, 165.0F, var5, var22, this.scrollOffset, var3 - var23, var4, 1.0F, var21);
      }
   }

   private void updateButtonAnimations(float var1) {
      for (int var2 = 0; var2 < 5; var2++) {
         float var3 = this.hoveredButton == var2 ? 1.0F : 0.0F;
         this.buttonHoverProgress[var2] = MathHelper.lerp(var1 * 10.0F, this.buttonHoverProgress[var2], var3);
         float var4 = this.hoveredButton == var2 ? 1.08F : 1.0F;
         this.buttonScales[var2] = MathHelper.lerp(var1 * 12.0F, this.buttonScales[var2], var4);
      }

      float var5 = this.hoveredButton == 4 ? 1.0F : 0.0F;
      this.exitButtonRedProgress = MathHelper.lerp(var1 * 8.0F, this.exitButtonRedProgress, var5);
   }

   private void renderTime(float var1, int var2, int var3, float var4, float var5) {
      float var6 = var2 / 2.0F;
      float var7 = (1.0F - var4) * 40.0F + var5;
      float var8 = var3 / 2.0F - 55.0F + var7;
      LocalTime var9 = LocalTime.now();
      String var10 = var9.format(DateTimeFormatter.ofPattern("HH:mm"));
      int var11 = (int)(var1 * 255.0F);
      float var12 = 48.0F;
      float var13 = Fonts.BOLD.getHeight(var12);
      Fonts.BOLD.drawCentered(var10, var6, var8 - var13 / 2.0F, var12, this.withAlpha(16777215, var11));
      String var14 = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH));
      int var15 = (int)(var1 * 200.0F);
      Fonts.BOLD.drawCentered(var14, var6, var8 + var13 / 2.0F + 4.0F, 12.0F, this.withAlpha(16777215, var15));
   }

   private void renderButtons(float var1, float var2, float var3, int var4, int var5, float var6, float var7) {
      float var8 = 274.0F;
      float var9 = (var4 - var8) / 2.0F;
      float var10 = (1.0F - var6) * 60.0F + var7;
      float var11 = var5 / 2.0F + 30.0F + var10;

      for (int var12 = 0; var12 < 5; var12++) {
         float var13 = var12 * 0.12F;
         float var14 = MathHelper.clamp((var6 - var13) / (1.0F - var13 * 0.5F), 0.0F, 1.0F);
         float var15 = this.easeOutCubic(var14);
         float var16 = var9 + var12 * 58;
         float var17 = var3 * var15;
         this.renderCircleButton(var12, var16, var11, var17);
      }
   }

   private void renderCircleButton(int var1, float var2, float var3, float var4) {
      if (!(var4 < 0.01F)) {
         float var5 = this.buttonScales[var1];
         float var6 = this.buttonHoverProgress[var1];
         float var7 = 42.0F * var5;
         float var8 = var7 / 2.0F;
         float var9 = var2 + 21.0F;
         float var10 = var3 + 21.0F;
         float var11 = var9 - var8;
         float var12 = var10 - var8;
         float var13 = var7 / 2.0F;
         int var14 = (int)(var4 * 120.0F);
         int var15 = (int)(var4 * (150.0F + var6 * 50.0F));
         int var16 = (int)(var4 * (150.0F + var6 * 80.0F));
         int var17 = (int)(var4 * 80.0F);
         int var18;
         int var19;
         int var20;
         int var21;
         int var22;
         int var23;
         if (var1 == 4) {
            float var24 = this.exitButtonRedProgress;
            int var25 = MathHelper.lerp(var24, 20, 42);
            int var26 = MathHelper.lerp(var24, 23, 20);
            int var27 = MathHelper.lerp(var24, 31, 20);
            var18 = this.withAlpha(var25 << 16 | var26 << 8 | var27, var15);
            var19 = this.withAlpha(var25 + 4 << 16 | var26 + 4 << 8 | var27 + 5, var15);
            var20 = this.withAlpha(var25 - 4 << 16 | var26 - 4 << 8 | var27 - 5, var15);
            var21 = this.withAlpha(var25 << 16 | var26 << 8 | var27, var15);
            int var28 = MathHelper.lerp(var24, 37, 90);
            int var29 = MathHelper.lerp(var24, 42, 58);
            int var30 = MathHelper.lerp(var24, 54, 58);
            var22 = this.withAlpha(var28 << 16 | var29 << 8 | var30, var16);
            short var31 = 255;
            int var32 = MathHelper.lerp(var24, 255, 140);
            int var33 = MathHelper.lerp(var24, 255, 140);
            var23 = this.withAlpha(var31 << 16 | var32 << 8 | var33, (int)(var4 * 255.0F));
         } else {
            var18 = this.withAlpha(1316639, var15);
            var19 = this.withAlpha(1579812, var15);
            var20 = this.withAlpha(1053466, var15);
            var21 = this.withAlpha(1316639, var15);
            var22 = this.withAlpha(2435638, var16);
            var23 = this.withAlpha(16777215, (int)(var4 * 255.0F));
         }

         int var34 = this.withAlpha(395280, var17);
         Render2D.blur(var11, var12, var7, var7, 15.0F, var13, var34);
         int[] var35 = new int[]{var18, var19, var21, var20};
         Render2D.gradientRect(var11, var12, var7, var7, var35, var13);
         Render2D.outline(var11, var12, var7, var7, 1.0F, var22, var13);
         float var36 = 17.0F * var5;
         String var37 = BUTTON_ICONS[var1];
         float var38 = Fonts.MAINMENUSCREEN.getWidth(var37, var36);
         float var39 = Fonts.MAINMENUSCREEN.getHeight(var36);
         Fonts.MAINMENUSCREEN.draw(var37, var9 - var38 / 2.0F + 0.5F, var10 - var39 / 2.0F, var36, var23);
      }
   }

   private int getHoveredButton(float var1, float var2, int var3, int var4, float var5) {
      float var6 = 274.0F;
      float var7 = (var3 - var6) / 2.0F;
      float var8 = (1.0F - var5) * 60.0F;
      float var9 = var4 / 2.0F + 30.0F + var8;

      for (int var10 = 0; var10 < 5; var10++) {
         float var11 = var7 + var10 * 58;
         float var12 = var11 + 21.0F;
         float var13 = var9 + 21.0F;
         float var14 = var1 - var12;
         float var15 = var2 - var13;
         if (var14 * var14 + var15 * var15 <= 441.0F) {
            return var10;
         }
      }

      return -1;
   }

   public boolean mouseClicked(Click var1, boolean var2) {
      if (this.transitionPhase != MainMenuScreen.TransitionPhase.NONE) {
         return false;
      }

      float var3 = this.toFixedCoord(var1.x());
      float var4 = this.toFixedCoord(var1.y());
      UpdateChecker.UpdateInfo var5 = UpdateChecker.getInstance().getPendingUpdate();
      if (this.updateNotification.isVisible()
         && var5 != null
         && this.updateNotification.mouseClicked(var3, var4, this.getFixedScaledWidth(), this.getFixedScaledHeight(), var5)) {
         return true;
      }

      if (this.currentView == MainMenuScreen.View.MAIN_MENU) {
         if (var1.button() == 0 && this.hwidHovered) {
            String var6 = b.a();
            if (!var6.isEmpty()) {
               try {
                  StringSelection var7 = new StringSelection(var6);
                  Toolkit.getDefaultToolkit().getSystemClipboard().setContents(var7, var7);
                  this.hwidCopiedTime = Util.getMeasuringTimeMs();
               } catch (Exception var8) {
               }
            }

            return true;
         }

         if (var1.button() == 0 && this.hoveredButton >= 0) {
            this.handleMainMenuButtonClick(this.hoveredButton);
            return true;
         }
      } else if (this.currentView == MainMenuScreen.View.ALT_SCREEN) {
         return this.handleAltScreenClick(var3, var4, var1);
      }

      return super.mouseClicked(var1, var2);
   }

   private void handleMainMenuButtonClick(int var1) {
      if (var1 == 0) {
         this.client.setScreen(new SelectWorldScreen(this));
      } else if (var1 == 1) {
         if (this.client.options.skipMultiplayerWarning) {
            this.client.setScreen(new MultiplayerScreen(this));
         } else {
            this.client.setScreen(new MultiplayerWarningScreen(this));
         }
      } else if (var1 == 2) {
         this.switchToView(MainMenuScreen.View.ALT_SCREEN);
      } else if (var1 == 3) {
         this.client.setScreen(new OptionsScreen(this, this.client.options));
      } else if (var1 == 4) {
         this.client.scheduleStop();
      }
   }

   private boolean handleAltScreenClick(float var1, float var2, Click var3) {
      int var4 = this.getFixedScaledWidth();
      int var5 = this.getFixedScaledHeight();
      float var6 = 405.0F;
      float var7 = 163.0F;
      float var8 = var4 / 2.0F;
      float var9 = var5 / 2.0F;
      float var10 = var8 - var6 / 2.0F;
      float var11 = var9 - var7 / 2.0F;
      float var12 = var10;
      float var13 = var11;
      float var14 = var10 + 100.0F + 5.0F;
      float var15 = var11;
      float var16 = var12 + 5.0F;
      float var17 = var13 + 38.0F;
      float var18 = 14.0F;
      float var19 = 14.0F;
      float var20 = 3.0F;
      float var21 = 90.0F - var19 - var20;
      if (this.accountRenderer.isMouseOver(var1, var2, var16, var17, var21, var18)) {
         this.nicknameFieldFocused = true;
         return true;
      }

      this.nicknameFieldFocused = false;
      float var22 = var16 + var21 + var20;
      float var23 = var17;
      if (this.accountRenderer.isMouseOver(var1, var2, var22, var23, var19, var19)) {
         if (!this.nicknameText.isEmpty()) {
            this.addAccount(this.nicknameText);
            this.nicknameText = "";
         }

         return true;
      } else {
         float var24 = 90.0F;
         float var25 = 16.0F;
         float var26 = var12 + 5.0F;
         float var27 = var17 + var18 + 6.0F;
         if (this.accountRenderer.isMouseOver(var1, var2, var26, var27, var24, var25)) {
            String var48 = this.generateRandomNickname();
            this.addAccount(var48);
            this.nicknameText = "";
            return true;
         }

         float var28 = var12 + 5.0F;
         float var29 = var27 + var25 + 5.0F;
         if (this.accountRenderer.isMouseOver(var1, var2, var28, var29, var24, var25)) {
            this.accountConfig.clearAllAccounts();
            this.targetScrollOffset = 0.0F;
            this.scrollOffset = 0.0F;
            return true;
         }

         float var30 = var14 + 5.0F;
         float var31 = var15 + 26.0F;
         float var32 = 290.0F;
         float var33 = 134.0F;
         if (!this.accountRenderer.isMouseOver(var1, var2, var30, var31, var32, var33)) {
            return false;
         }

         float var34 = (var32 - 5.0F) / 2.0F;
         float var35 = 40.0F;
         float var36 = 5.0F;
         List var37 = this.accountConfig.getSortedAccounts();

         for (int var38 = 0; var38 < var37.size(); var38++) {
            int var39 = var38 % 2;
            int var40 = var38 / 2;
            float var41 = var30 + var39 * (var34 + var36);
            float var42 = var31 + var40 * (var35 + var36) - this.scrollOffset;
            if (!(var42 + var35 < var31) && !(var42 > var31 + var33)) {
               float var43 = 12.0F;
               float var44 = var42 + var35 - var43 - 5.0F;
               float var45 = var41 + var34 - var43 * 2.0F - 8.0F;
               float var46 = var41 + var34 - var43 - 5.0F;
               if (this.accountRenderer.isMouseOver(var1, var2, var45, var44, var43, var43)) {
                  AccountEntry var47 = (AccountEntry)var37.get(var38);
                  var47.togglePinned();
                  if (var47.isPinned()) {
                     this.setActiveAccount(var47);
                  }

                  this.accountConfig.save();
                  return true;
               }

               if (this.accountRenderer.isMouseOver(var1, var2, var46, var44, var43, var43)) {
                  this.accountConfig.removeAccountByIndex(var38);
                  return true;
               }

               if (this.accountRenderer.isMouseOver(var1, var2, var41, var42, var34, var35)) {
                  this.setActiveAccount((AccountEntry)var37.get(var38));
                  return true;
               }
            }
         }

         return false;
      }
   }

   public boolean mouseScrolled(double var1, double var3, double var5, double var7) {
      if (this.currentView == MainMenuScreen.View.ALT_SCREEN && this.transitionPhase == MainMenuScreen.TransitionPhase.NONE) {
         float var9 = this.toFixedCoord(var1);
         float var10 = this.toFixedCoord(var3);
         int var11 = this.getFixedScaledWidth();
         int var12 = this.getFixedScaledHeight();
         float var13 = 405.0F;
         float var14 = 163.0F;
         float var15 = var11 / 2.0F;
         float var16 = var12 / 2.0F;
         float var17 = var15 - var13 / 2.0F;
         float var18 = var16 - var14 / 2.0F;
         float var19 = var17 + 100.0F + 5.0F;
         float var20 = var18;
         if (this.accountRenderer.isMouseOver(var9, var10, var19, var20, 300.0F, 165.0F)) {
            float var21 = 40.0F;
            float var22 = 5.0F;
            float var23 = 134.0F;
            int var24 = (int)Math.ceil(this.accountConfig.getSortedAccounts().size() / 2.0);
            float var25 = Math.max(0.0F, var24 * (var21 + var22) - var23);
            this.targetScrollOffset -= (float)var7 * 25.0F;
            this.targetScrollOffset = MathHelper.clamp(this.targetScrollOffset, 0.0F, var25);
            return true;
         } else {
            return super.mouseScrolled(var1, var3, var5, var7);
         }
      } else {
         return false;
      }
   }

   public boolean keyPressed(KeyInput var1) {
      if (this.transitionPhase != MainMenuScreen.TransitionPhase.NONE) {
         return false;
      }

      if (var1.key() == 256 && this.updateNotification.isVisible()) {
         return this.updateNotification.keyPressed(var1.key());
      }

      if (this.currentView != MainMenuScreen.View.MAIN_MENU && this.currentView == MainMenuScreen.View.ALT_SCREEN) {
         if (this.nicknameFieldFocused) {
            int var2 = var1.key();
            if (var2 == 259) {
               if (!this.nicknameText.isEmpty()) {
                  this.nicknameText = this.nicknameText.substring(0, this.nicknameText.length() - 1);
               }

               return true;
            }

            if (var2 == 256) {
               this.nicknameFieldFocused = false;
               return true;
            }

            if (var2 == 257 || var2 == 335) {
               if (!this.nicknameText.isEmpty()) {
                  this.addAccount(this.nicknameText);
                  this.nicknameText = "";
               }

               this.nicknameFieldFocused = false;
               return true;
            }
         }

         if (var1.key() == 256) {
            this.switchToView(MainMenuScreen.View.MAIN_MENU);
            this.accountConfig.save();
            return true;
         }
      }

      return super.keyPressed(var1);
   }

   public boolean charTyped(CharInput var1) {
      if (this.currentView == MainMenuScreen.View.ALT_SCREEN && this.nicknameFieldFocused && this.transitionPhase == MainMenuScreen.TransitionPhase.NONE) {
         int var2 = var1.codepoint();
         if (Character.isLetterOrDigit(var2) || var2 == 95) {
            if (this.nicknameText.length() < 16) {
               this.nicknameText = this.nicknameText + Character.toString(var2);
            }

            return true;
         }
      }

      return super.charTyped(var1);
   }

   private void setActiveAccount(AccountEntry var1) {
      this.accountConfig.setActiveAccount(var1.getName(), var1.getDate(), var1.getSkin());
      SessionChanger.changeUsername(var1.getName());
   }

   private void addAccount(String var1) {
      LocalDateTime var2 = LocalDateTime.now();
      DateTimeFormatter var3 = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
      String var4 = var2.format(var3);
      AccountEntry var5 = new AccountEntry(var1, var4, null);
      this.accountConfig.addAccount(var5);
      this.setActiveAccount(var5);
      SessionChanger.changeUsername(var1);
   }

   private String generateRandomNickname() {
      Random var1 = new Random();
      StringBuilder var2 = new StringBuilder();
      char[] var3 = new char[]{'a', 'e', 'i', 'o', 'u'};
      char[] var4 = new char[]{'b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'p', 'r', 's', 't', 'v', 'w', 'x', 'y', 'z'};
      String var5 = null;
      int var6 = 0;
      byte var7 = 10;
      List<AccountEntry> var8 = this.accountConfig.getAccounts();

      do {
         var2.setLength(0);
         int var9 = 6 + var1.nextInt(5);
         boolean var10 = var1.nextBoolean();

         for (int var11 = 0; var11 < var9; var11++) {
            if (var11 % 2 == 0) {
               var2.append(var10 ? var3[var1.nextInt(var3.length)] : var4[var1.nextInt(var4.length)]);
            } else {
               var2.append(var10 ? var4[var1.nextInt(var4.length)] : var3[var1.nextInt(var3.length)]);
            }
         }

         if (var1.nextInt(100) < 30) {
            var2.append(var1.nextInt(100));
         }

         String var15 = var2.substring(0, 1).toUpperCase() + var2.substring(1);
         var6++;
         boolean var12 = false;

         for (AccountEntry var14 : var8) {
            if (var14.getName().equalsIgnoreCase(var15)) {
               var12 = true;
               break;
            }
         }

         if (!var12) {
            var5 = var15;
            break;
         }
      } while (var6 < 10);

      if (var5 == null) {
         var5 = var2.substring(0, 1).toUpperCase() + var2.substring(1) + System.currentTimeMillis() % 1000L;
      }

      return var5;
   }

   private Identifier getSkinTexturePath(SkinTextures var1) {
      if (var1 != null && var1.body() != null) {
         try {
            return var1.body().texturePath();
         } catch (Exception var3) {
            return STEVE_SKIN;
         }
      } else {
         return STEVE_SKIN;
      }
   }

   private Identifier getSkinForPlayer(String var1) {
      if (this.client != null && this.client.player != null && this.client.player.networkHandler != null) {
         for (PlayerListEntry var3 : this.client.player.networkHandler.getPlayerList()) {
            if (var3.getProfile() != null && var3.getProfile().name().equalsIgnoreCase(var1)) {
               try {
                  SkinTextures var4 = var3.getSkinTextures();
                  Identifier var5 = this.getSkinTexturePath(var4);
                  if (var5 != null && !var5.equals(STEVE_SKIN)) {
                     return var5;
                  }
               } catch (Exception var6) {
               }
            }
         }

         return STEVE_SKIN;
      } else {
         return STEVE_SKIN;
      }
   }

   private Identifier getLocalPlayerSkin() {
      if (this.client != null && this.client.player != null && this.client.player.networkHandler != null) {
         try {
            PlayerListEntry var1 = this.client.player.networkHandler.getPlayerListEntry(this.client.player.getUuid());
            if (var1 != null) {
               SkinTextures var2 = var1.getSkinTextures();
               return this.getSkinTexturePath(var2);
            }
         } catch (Exception var3) {
         }

         return STEVE_SKIN;
      } else {
         return STEVE_SKIN;
      }
   }

   public void renderBackground(DrawContext var1, int var2, int var3, float var4) {
      this.drawBackground(this.currentZoom);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public boolean shouldPause() {
      return false;
   }

   private void renderOnlineWidget(int var1, float var2) {
      if (!(var2 < 0.01F)) {
         int var3 = (int)(var2 * 255.0F);
         String var4 = "Онлайн: ";
         String var5 = String.valueOf(OnlineTracker.getInstance().getOnline());
         float var6 = 6.5F;
         float var7 = Fonts.BOLD.getWidth(var4, var6);
         float var8 = Fonts.BOLD.getWidth(var5, var6);
         float var9 = Fonts.BOLD.getHeight(var6);
         float var10 = 8.0F;
         float var11 = 5.0F;
         float var12 = 5.0F;
         float var13 = 4.0F;
         float var14 = var12 + var13 + var7 + var8;
         float var15 = var14 + var10 * 2.0F;
         float var16 = var9 + var11 * 2.0F;
         float var17 = var1 - var15 - 8.0F;
         float var18 = 8.0F;
         Render2D.blur(var17, var18, var15, var16, 12.0F, 5.0F, this.withAlpha(395280, (int)(60.0F * var2)));
         int var19 = this.withAlpha(1316639, (int)(200.0F * var2));
         int var20 = this.withAlpha(1053466, (int)(200.0F * var2));
         Render2D.gradientRect(var17, var18, var15, var16, new int[]{var19, var19, var20, var20}, 5.0F);
         Render2D.outline(var17, var18, var15, var16, 0.5F, this.withAlpha(2435638, (int)(180.0F * var2)), 5.0F);
         float var21 = var17 + var10;
         float var22 = var18 + var11 + (var9 - var12) / 2.0F;
         Render2D.rect(var21, var22, var12, var12, this.withAlpha(5025616, var3), var12 / 2.0F);
         float var23 = var21 + var12 + var13;
         float var24 = var18 + var11;
         Fonts.BOLD.draw(var4, var23, var24, var6, this.withAlpha(11186368, var3));
         Fonts.BOLD.draw(var5, var23 + var7, var24, var6, this.withAlpha(16777215, var3));
      }
   }

   private void renderHwidWidget(int var1, int var2, float var3) {
      if (!(var3 < 0.01F)) {
         String var4 = b.a();
         if (var4 != null && !var4.isEmpty()) {
            int var5 = (int)(var3 * 255.0F);
            float var6 = 5.5F;
            float var7 = Fonts.BOLD.getHeight(var6);
            float var8 = 8.0F;
            float var9 = 5.0F;
            float var10 = Fonts.BOLD.getWidth(var4, var6);
            float var11 = Fonts.BOLD.getWidth("[copy]", var6);
            float var12 = 5.0F;
            float var13 = var10 + var8 * 2.0F;
            float var14 = var7 + var9 * 2.0F;
            float var15 = 8.0F;
            float var16 = var2 - var14 - 14.0F;
            Render2D.blur(var15, var16, var13, var14, 12.0F, 5.0F, this.withAlpha(395280, (int)(60.0F * var3)));
            int var17 = this.withAlpha(1316639, (int)(200.0F * var3));
            int var18 = this.withAlpha(1053466, (int)(200.0F * var3));
            Render2D.gradientRect(var15, var16, var13, var14, new int[]{var17, var17, var18, var18}, 5.0F);
            Render2D.outline(var15, var16, var13, var14, 0.5F, this.withAlpha(2435638, (int)(180.0F * var3)), 5.0F);
            Fonts.BOLD.draw(var4, var15 + var8, var16 + var9, var6, this.withAlpha(11186368, var5));
            long var19 = Util.getMeasuringTimeMs();
            boolean var21 = var19 - this.hwidCopiedTime < 1500L;
            String var22 = var21 ? "✓ Скопировано" : "[копировать]";
            float var23 = Fonts.BOLD.getWidth(var22, var6) + var8 * 2.0F;
            float var24 = var15 + var13 + var12;
            float var25 = var16;
            int var26 = var21
               ? this.withAlpha(1715738, (int)(200.0F * var3))
               : (this.hwidHovered ? this.withAlpha(1974832, (int)(220.0F * var3)) : this.withAlpha(1316639, (int)(180.0F * var3)));
            int var27 = var21 ? this.withAlpha(1451540, (int)(200.0F * var3)) : this.withAlpha(1053466, (int)(180.0F * var3));
            Render2D.blur(var24, var25, var23, var14, 12.0F, 5.0F, this.withAlpha(395280, (int)(50.0F * var3)));
            Render2D.gradientRect(var24, var25, var23, var14, new int[]{var26, var26, var27, var27}, 5.0F);
            Render2D.outline(
               var24, var25, var23, var14, 0.5F, this.withAlpha(var21 ? 3038254 : (this.hwidHovered ? 3817568 : 2435638), (int)(180.0F * var3)), 5.0F
            );
            int var28 = var21 ? this.withAlpha(5025616, var5) : (this.hwidHovered ? this.withAlpha(16777215, var5) : this.withAlpha(8028313, var5));
            Fonts.BOLD.draw(var22, var24 + var8, var25 + var9, var6, var28);
         }
      }
   }

   private float getHwidCopyBtnX(int var1) {
      String var2 = b.a();
      float var3 = 5.5F;
      float var4 = 8.0F;
      float var5 = 5.0F;
      float var6 = Fonts.BOLD.getHeight(var3);
      float var7 = Fonts.BOLD.getWidth(var2, var3);
      float var8 = var7 + var4 * 2.0F;
      float var9 = var6 + var5 * 2.0F;
      float var10 = 8.0F;
      return var10 + var8 + 5.0F;
   }

   private float getHwidCopyBtnY(int var1) {
      float var2 = 5.5F;
      float var3 = 5.0F;
      float var4 = Fonts.BOLD.getHeight(var2);
      float var5 = var4 + var3 * 2.0F;
      return var1 - var5 - 14.0F;
   }

   private int withAlpha(int var1, int var2) {
      return var1 & 16777215 | MathHelper.clamp(var2, 0, 255) << 24;
   }

   private enum TransitionPhase {
      NONE,
      FADE_OUT,
      FADE_IN;
   }

   private enum View {
      MAIN_MENU,
      ALT_SCREEN;
   }
}
