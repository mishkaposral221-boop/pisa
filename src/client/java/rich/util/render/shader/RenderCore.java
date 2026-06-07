package rich.util.render.shader;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import rich.util.render.font.FontRenderer;
import rich.util.render.font.Fonts;
import rich.util.render.pipeline.Arc2D;
import rich.util.render.pipeline.ArcOutline2D;
import rich.util.render.pipeline.BlurPipeline;
import rich.util.render.pipeline.GlassCompositePipeline;
import rich.util.render.pipeline.GlowOutlinePipeline;
import rich.util.render.pipeline.KawaseBlurPipeline;
import rich.util.render.pipeline.MaskDiffPipeline;
import rich.util.render.pipeline.OutlinePipeline;
import rich.util.render.pipeline.RectPipeline;
import rich.util.render.pipeline.TexturePipeline;
import rich.util.render.pipeline.WheelPipeline;

public class RenderCore {
   private final RectPipeline rectPipeline;
   private final OutlinePipeline outlinePipeline;
   private final GlowOutlinePipeline glowOutlinePipeline;
   private final TexturePipeline texturePipeline;
   private final BlurPipeline blurPipeline;
   private final KawaseBlurPipeline kawaseBlurPipeline;
   private final GlassCompositePipeline glassCompositePipeline;
   private final GlassHandsRenderer glassHandsRenderer;
   private final FontRenderer fontRenderer;
   private final MaskDiffPipeline maskDiffPipeline;
   private final WheelPipeline wheelPipeline;
   private boolean fontsLoaded = false;
   private boolean arcInitialized = false;
   private boolean arcOutlineInitialized = false;

   public RenderCore() {
      this.rectPipeline = new RectPipeline();
      this.outlinePipeline = new OutlinePipeline();
      this.glowOutlinePipeline = new GlowOutlinePipeline();
      this.texturePipeline = new TexturePipeline();
      this.blurPipeline = new BlurPipeline();
      this.kawaseBlurPipeline = new KawaseBlurPipeline();
      this.glassCompositePipeline = new GlassCompositePipeline();
      this.glassHandsRenderer = new GlassHandsRenderer();
      this.maskDiffPipeline = new MaskDiffPipeline();
      this.wheelPipeline = new WheelPipeline();
      this.fontRenderer = new FontRenderer();
   }

   private void ensureFontsLoaded() {
      if (!this.fontsLoaded) {
         this.fontsLoaded = true;
         this.fontRenderer.loadAllFonts(Fonts.getRegistry());
      }
   }

   private void ensureArcInitialized() {
      if (!this.arcInitialized) {
         this.arcInitialized = true;
         Arc2D.init();
      }
   }

   private void ensureArcOutlineInitialized() {
      if (!this.arcOutlineInitialized) {
         this.arcOutlineInitialized = true;
         ArcOutline2D.init();
      }
   }

   public void setupOverlayState() {
      GL11.glDisable(2929);
      GL11.glDepthMask(false);
      GL11.glEnable(3042);
      GL14.glBlendFuncSeparate(770, 771, 1, 771);
   }

   public void restoreState() {
      GL11.glDepthMask(true);
      GL11.glEnable(2929);
   }

   public void clearDepthBuffer() {
      GL11.glClear(256);
   }

   public void initArc() {
      this.ensureArcInitialized();
   }

   public void initArcOutline() {
      this.ensureArcOutlineInitialized();
   }

   public RectPipeline getRectPipeline() {
      return this.rectPipeline;
   }

   public OutlinePipeline getOutlinePipeline() {
      return this.outlinePipeline;
   }

   public GlowOutlinePipeline getGlowOutlinePipeline() {
      return this.glowOutlinePipeline;
   }

   public TexturePipeline getTexturePipeline() {
      return this.texturePipeline;
   }

   public BlurPipeline getBlurPipeline() {
      return this.blurPipeline;
   }

   public KawaseBlurPipeline getKawaseBlurPipeline() {
      return this.kawaseBlurPipeline;
   }

   public GlassCompositePipeline getGlassCompositePipeline() {
      return this.glassCompositePipeline;
   }

   public GlassHandsRenderer getGlassHandsRenderer() {
      return this.glassHandsRenderer;
   }

   public FontRenderer getFontRenderer() {
      this.ensureFontsLoaded();
      return this.fontRenderer;
   }

   public MaskDiffPipeline getMaskDiffPipeline() {
      return this.maskDiffPipeline;
   }

   public WheelPipeline getWheelPipeline() {
      return this.wheelPipeline;
   }

   public MinecraftClient getClient() {
      return MinecraftClient.getInstance();
   }

   public void close() {
      this.rectPipeline.close();
      this.outlinePipeline.close();
      this.glowOutlinePipeline.close();
      this.texturePipeline.close();
      this.blurPipeline.close();
      this.kawaseBlurPipeline.close();
      this.glassCompositePipeline.close();
      this.glassHandsRenderer.close();
      this.maskDiffPipeline.close();
      this.wheelPipeline.close();
      this.fontRenderer.close();
      Arc2D.shutdown();
      ArcOutline2D.shutdown();
   }
}
