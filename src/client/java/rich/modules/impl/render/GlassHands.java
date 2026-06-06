package rich.modules.impl.render;

import rich.events.api.EventHandler;
import rich.events.impl.GlassHandsRenderEvent;
import rich.events.impl.WorldChangeEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.profiler.FrameProfiler;
import rich.util.render.shader.GlassHandsRenderer;

public class GlassHands extends ModuleStructure {
   private static GlassHands instance;
   private final SliderSettings blurRadius = new SliderSettings("Сила размытия", "Сила эффекта размытия стекла").setValue(1.5F).range(1.0F, 5.0F);
   private final SliderSettings blurIterations = new SliderSettings("Качество", "Количество итераций размытия").setValue(1.0F).range(1, 5);
   private final SliderSettings saturation = new SliderSettings("Насыщенность", "Насыщенность цвета").setValue(0.0F).range(0.0F, 2.0F);
   private final BooleanSetting enableTint = new BooleanSetting("Оттенок", "Включить цветной оттенок стекла").setValue(false);
   private final SliderSettings tintIntensity = new SliderSettings("Сила оттенка", "Интенсивность оттенка")
      .setValue(0.2F)
      .range(0.0F, 0.5F)
      .visible(this.enableTint::isValue);
   private final ColorSetting tintColor = new ColorSetting("Цвет оттенка", "Цвет оттенка стекла").value(-16711681).visible(this.enableTint::isValue);
   private final BooleanSetting enableEdgeGlow = new BooleanSetting("Свечение краёв", "Свечение по краям стекла").setValue(false);
   private final SliderSettings edgeGlowIntensity = new SliderSettings("Сила свечения", "Интенсивность свечения краёв")
      .setValue(0.1F)
      .range(0.0F, 1.0F)
      .visible(this.enableEdgeGlow::isValue);

   private long lastSettingsPush = 0L;
   private float lastBlurRadius = -1.0F;
   private int lastBlurIterations = -1;
   private float lastSaturation = -999.0F;
   private int lastTintColor = Integer.MIN_VALUE;
   private float lastTintIntensity = -999.0F;
   private float lastEdgeGlowIntensity = -999.0F;

   public GlassHands() {
      super("GlassHands", "Делает руки и предметы стеклянными", ModuleCategory.VISUALS);
      this.settings(
         this.blurRadius,
         this.blurIterations,
         this.saturation,
         this.enableTint,
         this.tintIntensity,
         this.tintColor,
         this.enableEdgeGlow,
         this.edgeGlowIntensity
      );
      instance = this;
   }

   public static GlassHands getInstance() {
      return instance;
   }

   @Override
   public void activate() {
      GlassHandsRenderer var1 = GlassHandsRenderer.getInstance();
      if (var1 != null) {
         var1.invalidate();
         var1.setEnabled(true);
         this.forceRendererSettings();
      }
   }

   @Override
   public void deactivate() {
      GlassHandsRenderer var1 = GlassHandsRenderer.getInstance();
      if (var1 != null) {
         var1.setEnabled(false);
      }
   }

   @EventHandler
   public void onWorldChange(WorldChangeEvent var1) {
      if (this.isState()) {
         GlassHandsRenderer var2 = GlassHandsRenderer.getInstance();
         if (var2 != null) {
            var2.invalidate();
            var2.setEnabled(true);
            this.forceRendererSettings();
         }
      }
   }

   @EventHandler
   public void onGlassHandsRender(GlassHandsRenderEvent var1) {
      if (!this.isState()) {
         return;
      }

      FrameProfiler profiler = FrameProfiler.getInstance();
      boolean prof = profiler.isEnabled();
      String section = var1.getPhase() == GlassHandsRenderEvent.Phase.PRE ? "GlassHands/PRE" : "GlassHands/POST";
      if (prof) profiler.begin(section);
      try {
         GlassHandsRenderer var2 = GlassHandsRenderer.getInstance();
         if (var2 != null) {
            this.updateRendererSettingsThrottled();
            if (var1.getPhase() == GlassHandsRenderEvent.Phase.PRE) {
               var2.captureSceneBeforeHands();
            } else if (var1.getPhase() == GlassHandsRenderEvent.Phase.POST) {
               var2.captureSceneAfterHands();
               var2.renderGlassEffect();
            }
         }
      } finally {
         if (prof) profiler.end();
      }
   }

   private void updateRendererSettingsThrottled() {
      long now = System.currentTimeMillis();
      if (now - this.lastSettingsPush < 250L) {
         return;
      }
      this.lastSettingsPush = now;
      this.forceRendererSettings();
   }

   private void forceRendererSettings() {
      GlassHandsRenderer var1 = GlassHandsRenderer.getInstance();
      if (var1 == null) {
         return;
      }

      // Жёсткий FPS-safe clamp: старые конфиги с Quality=5 больше не будут убивать кадр.
      float radius = Math.min(this.blurRadius.getValue(), 2.5F);
      int iterations = Math.min(this.blurIterations.getInt(), 2);
      float sat = this.saturation.getValue();
      int tint = this.enableTint.isValue() ? this.tintColor.getColor() : 0;
      float tintPower = this.enableTint.isValue() ? this.tintIntensity.getValue() : 0.0F;
      float edgePower = this.enableEdgeGlow.isValue() ? Math.min(this.edgeGlowIntensity.getValue(), 0.25F) : 0.0F;

      if (radius == this.lastBlurRadius
         && iterations == this.lastBlurIterations
         && sat == this.lastSaturation
         && tint == this.lastTintColor
         && tintPower == this.lastTintIntensity
         && edgePower == this.lastEdgeGlowIntensity) {
         return;
      }

      this.lastBlurRadius = radius;
      this.lastBlurIterations = iterations;
      this.lastSaturation = sat;
      this.lastTintColor = tint;
      this.lastTintIntensity = tintPower;
      this.lastEdgeGlowIntensity = edgePower;

      var1.setBlurRadius(radius);
      var1.setBlurIterations(iterations);
      var1.setSaturation(sat);
      var1.setReflect(true);
      var1.setTintColor(tint);
      var1.setTintIntensity(tintPower);
      var1.setEdgeGlowIntensity(edgePower);
   }

   public SliderSettings getBlurRadius() { return this.blurRadius; }
   public SliderSettings getBlurIterations() { return this.blurIterations; }
   public SliderSettings getSaturation() { return this.saturation; }
   public BooleanSetting getEnableTint() { return this.enableTint; }
   public SliderSettings getTintIntensity() { return this.tintIntensity; }
   public ColorSetting getTintColor() { return this.tintColor; }
   public BooleanSetting getEnableEdgeGlow() { return this.enableEdgeGlow; }
   public SliderSettings getEdgeGlowIntensity() { return this.edgeGlowIntensity; }
}
