package rich.modules.impl.render;

import rich.events.api.EventHandler;
import rich.events.impl.GlassHandsRenderEvent;
import rich.events.impl.WorldChangeEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.render.shader.GlassHandsRenderer;

public class GlassHands extends ModuleStructure {
   private static GlassHands instance;
   private final SliderSettings blurRadius = new SliderSettings("Сила размытия", "Сила эффекта размытия стекла").setValue(2.5F).range(1.0F, 5.0F);
   private final SliderSettings blurIterations = new SliderSettings("Качество", "Количество итераций размытия").setValue(3.0F).range(1, 5);
   private final SliderSettings saturation = new SliderSettings("Насыщенность", "Насыщенность цвета").setValue(0.0F).range(0.0F, 2.0F);
   private final BooleanSetting enableTint = new BooleanSetting("Оттенок", "Включить цветной оттенок стекла").setValue(false);
   private final SliderSettings tintIntensity = new SliderSettings("Сила оттенка", "Интенсивность оттенка")
      .setValue(0.2F)
      .range(0.0F, 0.5F)
      .visible(this.enableTint::isValue);
   private final ColorSetting tintColor = new ColorSetting("Цвет оттенка", "Цвет оттенка стекла").value(-16711681).visible(this.enableTint::isValue);
   private final BooleanSetting enableEdgeGlow = new BooleanSetting("Свечение краёв", "Свечение по краям стекла").setValue(true);
   private final SliderSettings edgeGlowIntensity = new SliderSettings("Сила свечения", "Интенсивность свечения краёв")
      .setValue(0.2F)
      .range(0.0F, 1.0F)
      .visible(this.enableEdgeGlow::isValue);

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
         this.updateRendererSettings();
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
            this.updateRendererSettings();
         }
      }
   }

   @EventHandler
   public void onGlassHandsRender(GlassHandsRenderEvent var1) {
      if (this.isState()) {
         GlassHandsRenderer var2 = GlassHandsRenderer.getInstance();
         if (var2 != null) {
            this.updateRendererSettings();
            if (var1.getPhase() == GlassHandsRenderEvent.Phase.PRE) {
               var2.captureSceneBeforeHands();
            } else if (var1.getPhase() == GlassHandsRenderEvent.Phase.POST) {
               var2.captureSceneAfterHands();
               var2.renderGlassEffect();
            }
         }
      }
   }

   private void updateRendererSettings() {
      GlassHandsRenderer var1 = GlassHandsRenderer.getInstance();
      if (var1 != null) {
         var1.setBlurRadius(this.blurRadius.getValue());
         var1.setBlurIterations(this.blurIterations.getInt());
         var1.setSaturation(this.saturation.getValue());
         var1.setReflect(true);
         if (this.enableTint.isValue()) {
            var1.setTintColor(this.tintColor.getColor());
            var1.setTintIntensity(this.tintIntensity.getValue());
         } else {
            var1.setTintColor(0);
            var1.setTintIntensity(0.0F);
         }

         if (this.enableEdgeGlow.isValue()) {
            var1.setEdgeGlowIntensity(this.edgeGlowIntensity.getValue());
         } else {
            var1.setEdgeGlowIntensity(0.0F);
         }
      }
   }

   public SliderSettings getBlurRadius() {
      return this.blurRadius;
   }

   public SliderSettings getBlurIterations() {
      return this.blurIterations;
   }

   public SliderSettings getSaturation() {
      return this.saturation;
   }

   public BooleanSetting getEnableTint() {
      return this.enableTint;
   }

   public SliderSettings getTintIntensity() {
      return this.tintIntensity;
   }

   public ColorSetting getTintColor() {
      return this.tintColor;
   }

   public BooleanSetting getEnableEdgeGlow() {
      return this.enableEdgeGlow;
   }

   public SliderSettings getEdgeGlowIntensity() {
      return this.edgeGlowIntensity;
   }
}
