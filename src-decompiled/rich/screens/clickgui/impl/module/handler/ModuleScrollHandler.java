package rich.screens.clickgui.impl.module.handler;

public class ModuleScrollHandler {
   private double moduleTargetScroll = 0.0;
   private double moduleDisplayScroll = 0.0;
   private double settingTargetScroll = 0.0;
   private double settingDisplayScroll = 0.0;
   private float moduleScrollTopFade = 0.0F;
   private float moduleScrollBottomFade = 0.0F;
   private float settingScrollTopFade = 0.0F;
   private float settingScrollBottomFade = 0.0F;
   private float lastSettingsPanelHeight = 0.0F;
   private float lastModuleListHeight = 0.0F;
   private long lastScrollUpdateTime = System.currentTimeMillis();
   private static final float SCROLL_SPEED = 12.0F;
   private static final float FADE_SPEED = 8.0F;
   private static final float CORNER_INSET = 3.0F;
   private static final float MODULE_ITEM_HEIGHT = 22.0F;

   public void resetModuleScroll() {
      this.moduleTargetScroll = this.moduleDisplayScroll = 0.0;
   }

   public void resetSettingScroll() {
      this.settingTargetScroll = this.settingDisplayScroll = 0.0;
   }

   public void update(float var1) {
      long var2 = System.currentTimeMillis();
      float var4 = Math.min((float)(var2 - this.lastScrollUpdateTime) / 1000.0F, 0.1F);
      this.lastScrollUpdateTime = var2;
      this.moduleDisplayScroll = this.smoothScroll(this.moduleDisplayScroll, this.moduleTargetScroll, var4);
      this.settingDisplayScroll = this.smoothScroll(this.settingDisplayScroll, this.settingTargetScroll, var4);
   }

   private double smoothScroll(double var1, double var3, float var5) {
      double var6 = var3 - var1;
      return Math.abs(var6) < 0.5 ? var3 : var1 + var6 * 12.0 * var5;
   }

   public void updateFades(int var1, float var2, float var3, float var4) {
      this.lastSettingsPanelHeight = var4;
      this.lastModuleListHeight = var3;
      long var5 = System.currentTimeMillis();
      float var7 = Math.min((float)(var5 - this.lastScrollUpdateTime) / 1000.0F, 0.1F);
      float var8 = Math.max(0.0F, (var1 / 2 + var1 % 2) * 24.0F - var3 + 10.0F);
      float var9 = Math.max(0.0F, var2 - var4 + 45.0F);
      this.moduleScrollTopFade = this.updateFade(this.moduleScrollTopFade, this.moduleDisplayScroll < -0.5, var7);
      this.moduleScrollBottomFade = this.updateFade(this.moduleScrollBottomFade, this.moduleDisplayScroll > -var8 + 0.5F && var8 > 0.0F, var7);
      this.settingScrollTopFade = this.updateFade(this.settingScrollTopFade, this.settingDisplayScroll < -0.5, var7);
      this.settingScrollBottomFade = this.updateFade(this.settingScrollBottomFade, this.settingDisplayScroll > -var9 + 0.5F && var9 > 0.0F, var7);
   }

   private float updateFade(float var1, boolean var2, float var3) {
      float var4 = var2 ? 1.0F : 0.0F;
      float var5 = var4 - var1;
      return Math.abs(var5) < 0.01F ? var4 : var1 + var5 * 8.0F * var3;
   }

   public void handleModuleScroll(double var1, float var3, int var4) {
      float var5 = var3 - 6.0F - 2.0F;
      int var6 = var4 / 2 + var4 % 2;
      float var7 = Math.max(0.0F, var6 * 24.0F - var5 + 10.0F);
      this.moduleTargetScroll = Math.max(-var7, Math.min(0.0, this.moduleTargetScroll + var1 * 25.0));
   }

   public void handleSettingScroll(double var1, float var3, float var4) {
      float var5 = var3 - 31.0F - 3.0F - 3.0F;
      float var6 = Math.max(0.0F, var4 - var5 + 10.0F);
      this.settingTargetScroll = Math.max(-var6, Math.min(0.0, this.settingTargetScroll + var1 * 25.0));
   }

   public void scrollToModule(int var1, int var2) {
      float var3 = var1 * 24.0F;
      float var4 = this.lastModuleListHeight - 6.0F - 4.0F;
      float var5 = (var4 - 22.0F) / 2.0F;
      float var6 = -(var3 - var5);
      float var7 = Math.max(0.0F, var2 * 24.0F - var4);
      var6 = Math.max(-var7, Math.min(0.0F, var6));
      this.moduleTargetScroll = var6;
   }

   public void correctSettingScrollPosition(float var1) {
      if (!(this.lastSettingsPanelHeight <= 0.0F)) {
         float var2 = Math.max(0.0F, var1 - this.lastSettingsPanelHeight + 45.0F);
         if (this.settingTargetScroll < -var2) {
            this.settingTargetScroll = -var2;
         }

         if (this.settingDisplayScroll < -var2) {
            this.settingDisplayScroll = -var2;
         }
      }
   }

   public double getModuleTargetScroll() {
      return this.moduleTargetScroll;
   }

   public double getModuleDisplayScroll() {
      return this.moduleDisplayScroll;
   }

   public double getSettingTargetScroll() {
      return this.settingTargetScroll;
   }

   public double getSettingDisplayScroll() {
      return this.settingDisplayScroll;
   }

   public float getModuleScrollTopFade() {
      return this.moduleScrollTopFade;
   }

   public float getModuleScrollBottomFade() {
      return this.moduleScrollBottomFade;
   }

   public float getSettingScrollTopFade() {
      return this.settingScrollTopFade;
   }

   public float getSettingScrollBottomFade() {
      return this.settingScrollBottomFade;
   }

   public float getLastSettingsPanelHeight() {
      return this.lastSettingsPanelHeight;
   }

   public float getLastModuleListHeight() {
      return this.lastModuleListHeight;
   }

   public long getLastScrollUpdateTime() {
      return this.lastScrollUpdateTime;
   }
}
