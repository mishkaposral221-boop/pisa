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
   private final SliderSettings blurRadius = new SliderSettings("Сила размытия", "Сила эффекта размытия стекла").setValue(1.25F).range(1.0F, 5.0F);
   private final SliderSettings blurIterations = new SliderSettings("Качество", "Количество итераций размытия").setValue(1.0F).range(1, 5);
   private final SliderSettings saturation = new SliderSettings("Насыщенность", "Насыщенность цвета").setValue(0.0F).range(0.0F, 2.0F);
   private final BooleanSetting enableTint = new BooleanSetting("Оттенок", "Включить цветной оттенок стекла").setValue(false);
   private final SliderSettings tintIntensity = new SliderSettings("Сила оттенка", "Интенсивность оттенка")
      .setValue(0.15F)
      .range(0.0F, 0.5F)
      .visible(this.enableTint::isValue);
   private final ColorSetting tintColor = new ColorSetting("Цвет оттенка", "Цвет оттенка стекла").value(-16711681).visible(this.enableTint::isValue);
   private final BooleanSetting enableEdgeGlow = new BooleanSetting("Свечение краёв", "Свечение по краям стекла").setValue(false);
   private final SliderSettings edgeGlowIntensity = new SliderSettings("Сила свечения", "Интенсивность свечения краёв")
      .setValue(0.0F)
      .range(0.0F, 1.0F)
      .visible(this.enableEdgeGlow::isValue);

   private long lastSettingsPush = 0L;
   private float lastBlurRadius = -1.0F;
   private int lastBlurIterations = -1;
   private float lastSaturation = -999.0F;
   private int lastTintColor = Integer.MIN_VALUE;
   private float lastTintIntensity = -999.0F;
   private float lastEdgeGlowIntensity = -999.0F;
   private int frameCounter = 0;
   private boolean processThisFrame = false;

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

      if (var1.getPhase() == GlassHandsRenderEvent.Phase.PRE) {
         this.frameCounter++;
         // Самая дорогая часть в логах. Рендерим эффект через кадр: визуально почти незаметно, FPS заметно выше.
         this.processThisFrame = (this.frameCounter & 1) == 0;
      }

      if (!this.processThisFrame) {
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
      if (now - this.lastSettingsPush < 500L) {
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

      // Агрессивный FPS-safe clamp: даже если в конфиге стояло Quality=5, в рантайме выше 1 не пускаем.
      float radius = Math.min(this.blurRadius.getValue(), 1.5F);
      int iterations = 1;
      float sat = this.saturation.getValue();
      int tint = this.enableTint.isValue() ? this.tintColor.getColor() : 0;
      float tintPower = this.enableTint.isValue() ? Math.min(this.tintIntensity.getValue(), 0.15F) : 0.0F;
      float edgePower = 0.0F;

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
      var1.setReflect(false);
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
