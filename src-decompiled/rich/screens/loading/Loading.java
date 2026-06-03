package rich.screens.loading;

import net.minecraft.class_156;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;

public class Loading {
   private static Loading instance;
   private static final float FIXED_GUI_SCALE = 2.0F;
   private static final String[] LOADING_TEXTS = new String[]{"Запуск паста клиент", "Панчан ответь", "Панчан где связь", "Панчан молодец"};
   private static final long TEXT_DISPLAY_DURATION = 2200L;
   private static final long LAST_TEXT_DISPLAY_DURATION = 2500L;
   private static final long TEXT_TRANSITION_DURATION = 400L;
   private static final float ZOOM_LEVEL = 1.08F;
   private float animatedProgress = 0.0F;
   private float targetProgress = 0.0F;
   private float pulseTime = 0.0F;
   private long lastRenderTime = 0L;
   private long startTime = 0L;
   private boolean initialized = false;
   private int currentTextIndex = 0;
   private float currentTextOffsetY = 0.0F;
   private float currentTextAlpha = 1.0F;
   private float newTextOffsetY = -12.0F;
   private float newTextAlpha = 0.0F;
   private long lastTextChangeTime = 0L;
   private boolean isTransitioning = false;
   private long transitionStartTime = 0L;
   private float backgroundAlpha = 0.0F;
   private float contentAlpha = 0.0F;
   private boolean isFadingOut = false;
   private boolean readyToClose = false;
   private boolean resourcesLoaded = false;
   private boolean allTextsShown = false;
   private long lastTextShownTime = 0L;
   private final Loading.Particle[] particles = new Loading.Particle[40];
   private float progressGlow = 0.0F;

   public Loading() {
      instance = this;
      this.startTime = class_156.method_658();
      this.lastTextChangeTime = this.startTime;
      this.initParticles();
   }

   public static Loading getInstance() {
      if (instance == null) {
         instance = new Loading();
      }

      return instance;
   }

   private void initParticles() {
      for (int var1 = 0; var1 < this.particles.length; var1++) {
         this.particles[var1] = new Loading.Particle();
      }
   }

   private int getFixedScaledWidth() {
      class_310 var1 = class_310.method_1551();
      return var1 != null && var1.method_22683() != null ? (int)Math.ceil(var1.method_22683().method_4489() / 2.0) : 960;
   }

   private int getFixedScaledHeight() {
      class_310 var1 = class_310.method_1551();
      return var1 != null && var1.method_22683() != null ? (int)Math.ceil(var1.method_22683().method_4506() / 2.0) : 540;
   }

   public void render(int var1, int var2, float var3) {
      long var4 = class_156.method_658();
      if (!this.initialized) {
         this.lastRenderTime = var4;
         this.initialized = true;
      }

      float var6 = (float)(var4 - this.lastRenderTime) / 1000.0F;
      this.lastRenderTime = var4;
      var6 = class_3532.method_15363(var6, 0.001F, 0.1F);
      this.updateAnimations(var6, var4);
      int var7 = this.getFixedScaledWidth();
      int var8 = this.getFixedScaledHeight();
      Render2D.beginOverlay();
      Render2D.backgroundImage(this.backgroundAlpha * var3, 1.08F);
      float var9 = this.backgroundAlpha * var3 * 0.3F;
      Render2D.rect(0.0F, 0.0F, var7, var8, this.withAlpha(-16777216, (int)(var9 * 255.0F)), 0.0F);
      float var10 = this.contentAlpha * var3;
      if (var10 > 0.001F) {
         this.renderParticles(var7, var8, var10, var6);
         this.renderLogo(var7, var8, var10);
         this.renderSubtitle(var7, var8, var10);
         this.renderProgressBar(var7, var8, var10);
         this.renderLoadingText(var7, var8, var10, var4);
         this.renderBottomInfo(var7, var8, var10);
      }

      Render2D.endOverlay();
   }

   private void updateAnimations(float var1, long var2) {
      this.pulseTime += var1;
      this.animatedProgress = class_3532.method_16439(var1 * 5.0F, this.animatedProgress, this.targetProgress);
      this.progressGlow += var1 * 3.0F;
      this.backgroundAlpha = class_3532.method_16439(var1 * 4.0F, this.backgroundAlpha, 1.0F);
      if (this.backgroundAlpha > 0.99F) {
         this.backgroundAlpha = 1.0F;
      }

      if (!this.isFadingOut) {
         this.contentAlpha = class_3532.method_16439(var1 * 2.5F, this.contentAlpha, 1.0F);
         if (this.contentAlpha > 0.99F) {
            this.contentAlpha = 1.0F;
         }
      } else {
         this.contentAlpha -= var1 * 1.8F;
         if (this.contentAlpha < 0.0F) {
            this.contentAlpha = 0.0F;
            this.readyToClose = true;
         }
      }

      if (!this.isFadingOut) {
         this.updateTextAnimation(var2, var1);
      }

      if (this.allTextsShown && this.resourcesLoaded && !this.isFadingOut) {
         long var4 = var2 - this.lastTextShownTime;
         if (var4 >= 2500L) {
            this.isFadingOut = true;
         }
      }
   }

   private void updateTextAnimation(long var1, float var3) {
      if (!this.allTextsShown) {
         if (!this.isTransitioning) {
            long var4 = var1 - this.lastTextChangeTime;
            if (this.currentTextIndex >= LOADING_TEXTS.length - 1) {
               if (!this.allTextsShown) {
                  this.allTextsShown = true;
                  this.lastTextShownTime = var1;
               }

               return;
            }

            if (var4 >= 2200L) {
               this.isTransitioning = true;
               this.transitionStartTime = var1;
            }
         }

         if (this.isTransitioning) {
            long var8 = var1 - this.transitionStartTime;
            float var6 = class_3532.method_15363((float)var8 / 400.0F, 0.0F, 1.0F);
            float var7 = this.easeOutCubic(var6);
            this.currentTextOffsetY = 14.0F * var7;
            this.currentTextAlpha = class_3532.method_15363(1.0F - var7 * 1.5F, 0.0F, 1.0F);
            this.newTextOffsetY = -12.0F * (1.0F - var7);
            this.newTextAlpha = class_3532.method_15363(var7 * 1.3F, 0.0F, 1.0F);
            if (var6 >= 1.0F) {
               this.isTransitioning = false;
               this.currentTextIndex++;
               this.currentTextOffsetY = 0.0F;
               this.currentTextAlpha = 1.0F;
               this.newTextOffsetY = -12.0F;
               this.newTextAlpha = 0.0F;
               this.lastTextChangeTime = var1;
               if (this.currentTextIndex >= LOADING_TEXTS.length - 1) {
                  this.allTextsShown = true;
                  this.lastTextShownTime = var1;
               }
            }
         }
      }
   }

   private void renderParticles(int var1, int var2, float var3, float var4) {
      for (Loading.Particle var8 : this.particles) {
         var8.update(var4);
         int var9 = (int)(var8.alpha * var3 * 255.0F);
         if (var9 > 0) {
            int var10 = this.withAlpha(-1, var9);
            Render2D.rect(var8.x, var8.y, var8.size, var8.size, var10, var8.size / 2.0F);
         }
      }
   }

   private void renderLogo(int var1, int var2, float var3) {
      float var4 = var1 / 2.0F;
      float var5 = var2 / 2.0F - 40.0F;
      int var6 = (int)(var3 * 255.0F);
      float var7 = 44.0F;
      float var8 = (float)Math.sin(this.pulseTime * 1.2F) * 1.5F;
      String var9 = "A";
      float var10 = Fonts.ICONS.getWidth(var9, var7);
      float var11 = Fonts.ICONS.getHeight(var7);
      float var12 = var4 - var10 / 2.0F;
      float var13 = var5 - var11 / 2.0F + var8;
      float var14 = 0.4F + 0.2F * (float)Math.sin(this.pulseTime * 1.5F);
      int var15 = (int)(var6 * var14);
      int var16 = this.withAlpha(-36234, var15);
      Fonts.ICONS.draw(var9, var12, var13 + 3.0F, var7, var16);
      int var17 = this.withAlpha(-16777216, var6 / 3);
      Fonts.ICONS.draw(var9, var12 + 2.0F, var13 + 2.0F, var7, var17);
      int var18 = this.withAlpha(-1, var6);
      Fonts.ICONS.draw(var9, var12, var13, var7, var18);
   }

   private void renderSubtitle(int var1, int var2, float var3) {
      float var4 = var1 / 2.0F;
      float var5 = var2 / 2.0F - 8.0F;
      int var6 = (int)(var3 * 180.0F);
      float var7 = 9.0F;
      String var8 = "R I C H  C L I E N T";
      float var9 = Fonts.BOLD.getWidth(var8, var7);
      float var10 = 30.0F;
      float var11 = var5 + var7 / 2.0F;
      float var12 = 8.0F;
      int var13 = (int)(var3 * 40.0F);
      int var14 = this.withAlpha(-36234, var13);
      Render2D.rect(var4 - var9 / 2.0F - var12 - var10, var11, var10, 0.5F, var14, 0.0F);
      Render2D.rect(var4 + var9 / 2.0F + var12, var11, var10, 0.5F, var14, 0.0F);
      Fonts.BOLD.draw(var8, var4 - var9 / 2.0F, var5, var7, this.withAlpha(-1, var6));
   }

   private void renderProgressBar(int var1, int var2, float var3) {
      float var4 = var1 / 2.0F;
      float var5 = var2 / 2.0F + 12.0F;
      float var6 = 180.0F;
      float var7 = 3.0F;
      float var8 = var4 - var6 / 2.0F;
      int var9 = (int)(var3 * 60.0F);
      Render2D.rect(var8, var5, var6, var7, this.withAlpha(-1, var9), 1.5F);
      if (this.animatedProgress > 0.0F) {
         float var10 = var6 * class_3532.method_15363(this.animatedProgress, 0.0F, 1.0F);
         int var11 = (int)(var3 * 200.0F);
         int var12 = (int)(var3 * 140.0F);
         Render2D.gradientRect(
            var8,
            var5,
            var10,
            var7,
            new int[]{this.withAlpha(-36234, var11), this.withAlpha(-25994, var12), this.withAlpha(-36234, var11), this.withAlpha(-25994, var12)},
            1.5F
         );
         float var13 = (float)(this.progressGlow * 0.3F % 1.0);
         float var14 = var8 + var10 * var13;
         float var15 = 20.0F;
         if (var14 + var15 > var8 + var10) {
            var15 = var8 + var10 - var14;
         }

         if (var15 > 0.0F) {
            int var16 = (int)(var3 * 80.0F);
            Render2D.rect(var14, var5, var15, var7, this.withAlpha(-1, var16), 1.5F);
         }
      }

      String var17 = (int)(this.animatedProgress * 100.0F) + "%";
      float var18 = Fonts.BOLD.getWidth(var17, 6.0F);
      int var19 = (int)(var3 * 120.0F);
      Fonts.BOLD.draw(var17, var8 + var6 + 8.0F, var5 - 1.0F, 6.0F, this.withAlpha(-1, var19));
   }

   private void renderLoadingText(int var1, int var2, float var3, long var4) {
      float var6 = 10.0F;
      float var7 = var2 / 2.0F + 30.0F;
      float var8 = var1 / 2.0F;
      if (this.currentTextAlpha > 0.01F && this.currentTextIndex < LOADING_TEXTS.length) {
         String var9 = LOADING_TEXTS[this.currentTextIndex];
         float var10 = Fonts.REGULARNEW.getWidth(var9, var6);
         int var11 = (int)(var3 * this.currentTextAlpha * 220.0F);
         Fonts.REGULARNEW.draw(var9, var8 - var10 / 2.0F, var7 + this.currentTextOffsetY, var6, this.withAlpha(-1, var11));
      }

      if (this.isTransitioning && this.newTextAlpha > 0.01F) {
         int var13 = this.currentTextIndex + 1;
         if (var13 < LOADING_TEXTS.length) {
            String var14 = LOADING_TEXTS[var13];
            float var15 = Fonts.REGULARNEW.getWidth(var14, var6);
            int var12 = (int)(var3 * this.newTextAlpha * 220.0F);
            Fonts.REGULARNEW.draw(var14, var8 - var15 / 2.0F, var7 + this.newTextOffsetY, var6, this.withAlpha(-1, var12));
         }
      }
   }

   private void renderBottomInfo(int var1, int var2, float var3) {
      float var4 = var2 - 20;
      int var5 = (int)(var3 * 50.0F);
      float var6 = 6.0F;
      String var7 = "RunTime Visuals · 1.21.11";
      Fonts.BOLD.draw(var7, 15.0F, var4, var6, this.withAlpha(-1, var5));
      String var8 = "RunTime Visuals";
      float var9 = Fonts.BOLD.getWidth(var8, var6);
      Fonts.BOLD.draw(var8, var1 - var9 - 15.0F, var4, var6, this.withAlpha(-1, var5));
      if (!this.allTextsShown) {
         int var10 = (int)(this.pulseTime * 2.0F) % 4;
         String var11 = ".".repeat(var10);
         float var12 = Fonts.BOLD.getWidth("...", var6);
         Fonts.BOLD.draw(var11, var1 / 2.0F - var12 / 2.0F, var4, var6, this.withAlpha(-36234, (int)(var3 * 80.0F)));
      }
   }

   private float easeOutCubic(float var1) {
      return 1.0F - (float)Math.pow(1.0F - var1, 3.0);
   }

   public void markComplete() {
      this.resourcesLoaded = true;
   }

   public boolean isContentFadedOut() {
      return this.isFadingOut && this.contentAlpha <= 0.01F;
   }

   public boolean isReadyToClose() {
      return this.readyToClose;
   }

   public boolean isComplete() {
      return this.allTextsShown && this.resourcesLoaded;
   }

   public boolean isFadingOut() {
      return this.isFadingOut;
   }

   public float getContentAlpha() {
      return this.contentAlpha;
   }

   public void setProgress(float var1) {
      this.targetProgress = class_3532.method_15363(var1, 0.0F, 1.0F);
   }

   public float getProgress() {
      return this.targetProgress;
   }

   public void reset() {
      this.animatedProgress = 0.0F;
      this.targetProgress = 0.0F;
      this.pulseTime = 0.0F;
      this.lastRenderTime = 0L;
      this.startTime = class_156.method_658();
      this.initialized = false;
      this.currentTextIndex = 0;
      this.currentTextOffsetY = 0.0F;
      this.currentTextAlpha = 1.0F;
      this.newTextOffsetY = -12.0F;
      this.newTextAlpha = 0.0F;
      this.lastTextChangeTime = this.startTime;
      this.isTransitioning = false;
      this.transitionStartTime = 0L;
      this.backgroundAlpha = 0.0F;
      this.contentAlpha = 0.0F;
      this.isFadingOut = false;
      this.readyToClose = false;
      this.resourcesLoaded = false;
      this.allTextsShown = false;
      this.lastTextShownTime = 0L;
      this.initParticles();
   }

   public long getStartTime() {
      return this.startTime;
   }

   private int withAlpha(int var1, int var2) {
      return var1 & 16777215 | class_3532.method_15340(var2, 0, 255) << 24;
   }

   private static class Particle {
      float x;
      float y;
      float speed;
      float size;
      float alpha;
      float drift;

      Particle() {
         this.reset(true);
      }

      void reset(boolean var1) {
         this.x = (float)(Math.random() * 2000.0);
         this.y = var1 ? (float)(Math.random() * 1200.0) : 1200.0F + (float)(Math.random() * 100.0);
         this.speed = 8.0F + (float)(Math.random() * 25.0);
         this.size = 1.0F + (float)(Math.random() * 2.5);
         this.alpha = 0.08F + (float)(Math.random() * 0.2F);
         this.drift = -15.0F + (float)(Math.random() * 30.0);
      }

      void update(float var1) {
         this.y = this.y - this.speed * var1;
         this.x = this.x + this.drift * var1;
         if (this.y < -20.0F) {
            this.reset(false);
         }
      }
   }
}
