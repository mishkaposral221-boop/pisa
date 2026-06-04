package rich.util.render;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.util.Window;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import rich.Initialization;
import rich.util.render.pipeline.Arc2D;
import rich.util.render.pipeline.ArcOutline2D;

public class Render2D {
   private static boolean inOverlayMode = false;
   private static boolean savedDepthTest = false;
   private static boolean savedDepthMask = false;
   private static boolean savedBlend = false;
   private static final Identifier BACKGROUND_TEXTURE = Identifier.of("rich", "textures/menu/backmenu.png");
   private static final List<Runnable> OVERRIDE_TASKS = new ArrayList<>();
   private static final float Z_OVERRIDE = 0.0F;
   private static final float FIXED_GUI_SCALE = 2.0F;
   private static final int[] COLORS_4 = new int[4];
   private static final int[] COLORS_8 = new int[8];
   private static final int[] COLORS_9 = new int[9];
   private static final float[] RADII_4 = new float[4];
   private static final float[] THICKNESS_8 = new float[8];
   private static final int[] TEX_COLORS_4 = new int[4];
   private static final float[] TEX_RADII_4 = new float[4];
   private static final int[] FB_COLORS_4 = new int[4];
   private static final float[] FB_RADII_4 = new float[4];
   private static final float[] BLUR_RADII_4 = new float[4];
   private static final float[] GLOW_RADII_4 = new float[4];

   private static boolean scaleActive = false;
   private static float scalePivotX = 0.0F;
   private static float scalePivotY = 0.0F;
   private static float scaleAmount = 1.0F;

   public static void pushScale(float var0, float var1, float var2) {
      scaleActive = true;
      scalePivotX = var0;
      scalePivotY = var1;
      scaleAmount = var2;
   }

   public static void popScale() {
      scaleActive = false;
      scalePivotX = 0.0F;
      scalePivotY = 0.0F;
      scaleAmount = 1.0F;
   }

   private static float tx(float var0) {
      return scaleActive ? scalePivotX + (var0 - scalePivotX) * scaleAmount : var0;
   }

   private static float ty(float var0) {
      return scaleActive ? scalePivotY + (var0 - scalePivotY) * scaleAmount : var0;
   }

   private static float td(float var0) {
      return scaleActive ? var0 * scaleAmount : var0;
   }

   public static float scaleX(float var0) {
      return tx(var0);
   }

   public static float scaleY(float var0) {
      return ty(var0);
   }

   public static float scaleSize(float var0) {
      return td(var0);
   }

   public static int getFixedScaledWidth() {
      Window var0 = MinecraftClient.getInstance().getWindow();
      return (int)Math.ceil(var0.getFramebufferWidth() / 2.0);
   }

   public static int getFixedScaledHeight() {
      Window var0 = MinecraftClient.getInstance().getWindow();
      return (int)Math.ceil(var0.getFramebufferHeight() / 2.0);
   }

   public static float getFixedGuiScale() {
      return 2.0F;
   }

   public static float getScaleMultiplier() {
      MinecraftClient var0 = MinecraftClient.getInstance();
      float var1 = var0.getWindow().getScaleFactor();
      return 2.0F / var1;
   }

   public static void beginOverlay() {
      inOverlayMode = true;
      savedDepthTest = GL11.glIsEnabled(2929);
      savedDepthMask = GL11.glGetBoolean(2930);
      savedBlend = GL11.glIsEnabled(3042);
      GL11.glDisable(2929);
      GL11.glDepthMask(false);
      GL11.glEnable(3042);
      GL14.glBlendFuncSeparate(770, 771, 1, 771);
   }

   public static void endOverlay() {
      if (savedDepthMask) {
         GL11.glDepthMask(true);
      }

      if (savedDepthTest) {
         GL11.glEnable(2929);
      } else {
         GL11.glDisable(2929);
      }

      if (!savedBlend) {
         GL11.glDisable(3042);
      }

      inOverlayMode = false;
   }

   public static void clearDepth() {
      MinecraftClient var0 = MinecraftClient.getInstance();
      if (var0.getFramebuffer() != null) {
         GL11.glClear(256);
      }
   }

   public static void enableBlend() {
      GL11.glEnable(3042);
      GL14.glBlendFuncSeparate(770, 771, 1, 771);
   }

   public static void disableBlend() {
      GL11.glDisable(3042);
   }

   public static void enableDepthTest() {
      GL11.glEnable(2929);
   }

   public static void disableDepthTest() {
      GL11.glDisable(2929);
   }

   public static void depthMask(boolean var0) {
      GL11.glDepthMask(var0);
   }

   public static void backgroundImage(float var0) {
      backgroundImage(var0, 1.0F);
   }

   public static void backgroundImage(float var0, float var1) {
      int var2 = getFixedScaledWidth();
      int var3 = getFixedScaledHeight();
      float var4 = var2 * var1;
      float var5 = var3 * var1;
      float var6 = (var2 - var4) / 2.0F;
      float var7 = (var3 - var5) / 2.0F;
      int var8 = (int)(var0 * 255.0F);
      int var9 = var8 << 24 | 16777215;
      texture(BACKGROUND_TEXTURE, var6, var7, var4, var5, var9);
   }

   public static void backgroundImage(float var0, float var1, float var2, float var3, float var4) {
      int var5 = (int)(var4 * 255.0F);
      int var6 = var5 << 24 | 16777215;
      texture(BACKGROUND_TEXTURE, var0, var1, var2, var3, var6);
   }

   public static void rect(float var0, float var1, float var2, float var3, int var4) {
      COLORS_4[0] = COLORS_4[1] = COLORS_4[2] = COLORS_4[3] = var4;
      RADII_4[0] = RADII_4[1] = RADII_4[2] = RADII_4[3] = 0.0F;
      Initialization.getInstance().getManager().getRenderCore().getRectPipeline().drawRect(tx(var0), ty(var1), td(var2), td(var3), COLORS_4, RADII_4);
   }

   public static void rect(float var0, float var1, float var2, float var3, int var4, float var5) {
      COLORS_4[0] = COLORS_4[1] = COLORS_4[2] = COLORS_4[3] = var4;
      RADII_4[0] = RADII_4[1] = RADII_4[2] = RADII_4[3] = var5;
      Initialization.getInstance().getManager().getRenderCore().getRectPipeline().drawRect(tx(var0), ty(var1), td(var2), td(var3), COLORS_4, RADII_4);
   }

   public static void rect(float var0, float var1, float var2, float var3, int var4, float var5, float var6, float var7, float var8) {
      COLORS_4[0] = COLORS_4[1] = COLORS_4[2] = COLORS_4[3] = var4;
      RADII_4[0] = var5;
      RADII_4[1] = var6;
      RADII_4[2] = var7;
      RADII_4[3] = var8;
      Initialization.getInstance().getManager().getRenderCore().getRectPipeline().drawRect(tx(var0), ty(var1), td(var2), td(var3), COLORS_4, RADII_4);
   }

   public static void gradientRect(float var0, float var1, float var2, float var3, int[] var4, float var5) {
      RADII_4[0] = RADII_4[1] = RADII_4[2] = RADII_4[3] = var5;
      Initialization.getInstance().getManager().getRenderCore().getRectPipeline().drawRect(tx(var0), ty(var1), td(var2), td(var3), var4, RADII_4);
   }

   public static void gradientRect(float var0, float var1, float var2, float var3, int[] var4, float var5, float var6, float var7, float var8) {
      RADII_4[0] = var5;
      RADII_4[1] = var6;
      RADII_4[2] = var7;
      RADII_4[3] = var8;
      Initialization.getInstance().getManager().getRenderCore().getRectPipeline().drawRect(tx(var0), ty(var1), td(var2), td(var3), var4, RADII_4);
   }

   public static void gradientRect9(
      float var0, float var1, float var2, float var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, int var12, float var13
   ) {
      COLORS_9[0] = var4;
      COLORS_9[1] = var5;
      COLORS_9[2] = var6;
      COLORS_9[3] = var7;
      COLORS_9[4] = var8;
      COLORS_9[5] = var9;
      COLORS_9[6] = var10;
      COLORS_9[7] = var11;
      COLORS_9[8] = var12;
      RADII_4[0] = RADII_4[1] = RADII_4[2] = RADII_4[3] = var13;
      Initialization.getInstance().getManager().getRenderCore().getRectPipeline().drawRect(tx(var0), ty(var1), td(var2), td(var3), COLORS_9, RADII_4);
   }

   public static void gradientRect9(float var0, float var1, float var2, float var3, int[] var4, float var5) {
      RADII_4[0] = RADII_4[1] = RADII_4[2] = RADII_4[3] = var5;
      Initialization.getInstance().getManager().getRenderCore().getRectPipeline().drawRect(tx(var0), ty(var1), td(var2), td(var3), var4, RADII_4);
   }

   public static void gradientRect9(float var0, float var1, float var2, float var3, int[] var4, float var5, float var6, float var7, float var8) {
      RADII_4[0] = var5;
      RADII_4[1] = var6;
      RADII_4[2] = var7;
      RADII_4[3] = var8;
      Initialization.getInstance().getManager().getRenderCore().getRectPipeline().drawRect(tx(var0), ty(var1), td(var2), td(var3), var4, RADII_4);
   }

   public static void gradientRect9(
      float var0,
      float var1,
      float var2,
      float var3,
      int var4,
      int var5,
      int var6,
      int var7,
      int var8,
      int var9,
      int var10,
      int var11,
      int var12,
      float var13,
      float var14
   ) {
      COLORS_9[0] = var4;
      COLORS_9[1] = var5;
      COLORS_9[2] = var6;
      COLORS_9[3] = var7;
      COLORS_9[4] = var8;
      COLORS_9[5] = var9;
      COLORS_9[6] = var10;
      COLORS_9[7] = var11;
      COLORS_9[8] = var12;
      RADII_4[0] = RADII_4[1] = RADII_4[2] = RADII_4[3] = var13;
      Initialization.getInstance().getManager().getRenderCore().getRectPipeline().drawRect(tx(var0), ty(var1), td(var2), td(var3), COLORS_9, RADII_4, var14);
   }

   public static void gradientRect9(float var0, float var1, float var2, float var3, int[] var4, float var5, float var6) {
      RADII_4[0] = RADII_4[1] = RADII_4[2] = RADII_4[3] = var5;
      Initialization.getInstance().getManager().getRenderCore().getRectPipeline().drawRect(tx(var0), ty(var1), td(var2), td(var3), var4, RADII_4, var6);
   }

   public static void outline(float var0, float var1, float var2, float var3, float var4, int var5) {
      for (int var6 = 0; var6 < 8; var6++) {
         COLORS_8[var6] = var5;
      }

      for (int var7 = 0; var7 < 8; var7++) {
         THICKNESS_8[var7] = var4;
      }

      RADII_4[0] = RADII_4[1] = RADII_4[2] = RADII_4[3] = 0.0F;
      Initialization.getInstance().getManager().getRenderCore().getOutlinePipeline().drawOutline(tx(var0), ty(var1), td(var2), td(var3), COLORS_8, THICKNESS_8, RADII_4, 1.0F);
   }

   public static void outline(float var0, float var1, float var2, float var3, float var4, int var5, float var6) {
      for (int var7 = 0; var7 < 8; var7++) {
         COLORS_8[var7] = var5;
      }

      for (int var8 = 0; var8 < 8; var8++) {
         THICKNESS_8[var8] = var4;
      }

      RADII_4[0] = RADII_4[1] = RADII_4[2] = RADII_4[3] = var6;
      Initialization.getInstance().getManager().getRenderCore().getOutlinePipeline().drawOutline(tx(var0), ty(var1), td(var2), td(var3), COLORS_8, THICKNESS_8, RADII_4, 1.0F);
   }

   public static void outline(float var0, float var1, float var2, float var3, float var4, int var5, float var6, float var7, float var8, float var9) {
      for (int var10 = 0; var10 < 8; var10++) {
         COLORS_8[var10] = var5;
      }

      for (int var11 = 0; var11 < 8; var11++) {
         THICKNESS_8[var11] = var4;
      }

      RADII_4[0] = var6;
      RADII_4[1] = var7;
      RADII_4[2] = var8;
      RADII_4[3] = var9;
      Initialization.getInstance().getManager().getRenderCore().getOutlinePipeline().drawOutline(tx(var0), ty(var1), td(var2), td(var3), COLORS_8, THICKNESS_8, RADII_4, 1.0F);
   }

   public static void gradientOutline(float var0, float var1, float var2, float var3, float var4, int[] var5, float var6) {
      for (int var7 = 0; var7 < 8; var7++) {
         THICKNESS_8[var7] = var4;
      }

      RADII_4[0] = RADII_4[1] = RADII_4[2] = RADII_4[3] = var6;
      Initialization.getInstance().getManager().getRenderCore().getOutlinePipeline().drawOutline(tx(var0), ty(var1), td(var2), td(var3), var5, THICKNESS_8, RADII_4, 1.0F);
   }

   public static void blur(float var0, float var1, float var2, float var3, float var4, int var5) {
      BLUR_RADII_4[0] = BLUR_RADII_4[1] = BLUR_RADII_4[2] = BLUR_RADII_4[3] = 0.0F;
      Initialization.getInstance().getManager().getRenderCore().getBlurPipeline().drawBlur(tx(var0), ty(var1), td(var2), td(var3), var4, BLUR_RADII_4, var5);
   }

   public static void blur(float var0, float var1, float var2, float var3, float var4, float var5, int var6) {
      BLUR_RADII_4[0] = BLUR_RADII_4[1] = BLUR_RADII_4[2] = BLUR_RADII_4[3] = var5;
      Initialization.getInstance().getManager().getRenderCore().getBlurPipeline().drawBlur(tx(var0), ty(var1), td(var2), td(var3), var4, BLUR_RADII_4, var6);
   }

   public static void blur(float var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, int var9) {
      BLUR_RADII_4[0] = var5;
      BLUR_RADII_4[1] = var6;
      BLUR_RADII_4[2] = var7;
      BLUR_RADII_4[3] = var8;
      Initialization.getInstance().getManager().getRenderCore().getBlurPipeline().drawBlur(tx(var0), ty(var1), td(var2), td(var3), var4, BLUR_RADII_4, var9);
   }

   public static void texture(Identifier var0, float var1, float var2, float var3, float var4, int var5) {
      texture(var0, var1, var2, var3, var4, 0.0F, 0.0F, 1.0F, 1.0F, var5, 1.0F, 0.0F);
   }

   public static void texture(Identifier var0, float var1, float var2, float var3, float var4, float var5, int var6) {
      texture(var0, var1, var2, var3, var4, 0.0F, 0.0F, 1.0F, 1.0F, var6, var5, 0.0F);
   }

   public static void texture(Identifier var0, float var1, float var2, float var3, float var4, float var5, float var6, int var7) {
      texture(var0, var1, var2, var3, var4, 0.0F, 0.0F, 1.0F, 1.0F, var7, var5, var6);
   }

   public static void texture(Identifier var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, int var9) {
      texture(var0, var1, var2, var3, var4, var5, var6, var7, var8, var9, 1.0F, 0.0F);
   }

   public static void texture(
      Identifier var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, int var9, float var10
   ) {
      texture(var0, var1, var2, var3, var4, var5, var6, var7, var8, var9, 1.0F, var10);
   }

   public static void texture(
      Identifier var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, int var9, float var10, float var11
   ) {
      TEX_COLORS_4[0] = TEX_COLORS_4[1] = TEX_COLORS_4[2] = TEX_COLORS_4[3] = var9;
      TEX_RADII_4[0] = TEX_RADII_4[1] = TEX_RADII_4[2] = TEX_RADII_4[3] = var11;
      Initialization.getInstance()
         .getManager()
         .getRenderCore()
         .getTexturePipeline()
         .drawTexture(var0, tx(var1), ty(var2), td(var3), td(var4), var5, var6, var7, var8, TEX_COLORS_4, TEX_RADII_4, var10);
   }

   public static void drawTexture(
      DrawContext var0,
      Identifier var1,
      float var2,
      float var3,
      float var4,
      float var5,
      float var6,
      float var7,
      float var8,
      float var9,
      float var10,
      float var11,
      int var12
   ) {
      float var13 = var6 / var10;
      float var14 = var7 / var11;
      float var15 = (var6 + var8) / var10;
      float var16 = (var7 + var9) / var11;
      texture(var1, var2, var3, var4, var5, var13, var14, var15, var16, var12, 1.0F, 0.0F);
   }

   public static void drawTexture(
      DrawContext var0,
      Identifier var1,
      float var2,
      float var3,
      float var4,
      float var5,
      float var6,
      float var7,
      float var8,
      float var9,
      float var10,
      float var11,
      int var12,
      float var13
   ) {
      float var14 = var6 / var10;
      float var15 = var7 / var11;
      float var16 = (var6 + var8) / var10;
      float var17 = (var7 + var9) / var11;
      texture(var1, var2, var3, var4, var5, var14, var15, var16, var17, var12, 1.0F, var13);
   }

   public static void drawSprite(Sprite var0, float var1, float var2, float var3, float var4, int var5) {
      drawSprite(var0, var1, var2, var3, var4, var5, true);
   }

   public static void drawSprite(Sprite var0, float var1, float var2, float var3, float var4, int var5, boolean var6) {
      if (var0 != null && var3 != 0.0F && var4 != 0.0F) {
         float var7 = var6 ? 1.0F : 0.0F;
         texture(var0.getAtlasId(), var1, var2, var3, var4, var0.getMinU(), var0.getMinV(), var0.getMaxU(), var0.getMaxV(), var5, var7, 0.0F);
      }
   }

   public static void drawSpriteSmooth(Sprite var0, float var1, float var2, float var3, float var4, int var5) {
      drawSprite(var0, var1, var2, var3, var4, var5, false);
   }

   public static void drawFramebufferTexture(int var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      int var9 = (int)(var8 * 255.0F) << 24 | (int)(var5 * 255.0F) << 16 | (int)(var6 * 255.0F) << 8 | (int)(var7 * 255.0F);
      FB_COLORS_4[0] = FB_COLORS_4[1] = FB_COLORS_4[2] = FB_COLORS_4[3] = var9;
      FB_RADII_4[0] = FB_RADII_4[1] = FB_RADII_4[2] = FB_RADII_4[3] = 0.0F;
      Initialization.getInstance().getManager().getRenderCore().getTexturePipeline().drawFramebufferTexture(var0, tx(var1), ty(var2), td(var3), td(var4), FB_COLORS_4, FB_RADII_4, var8);
   }

   public static void glowOutline(float var0, float var1, float var2, float var3, float var4, int var5, float var6, float var7, float var8) {
      GLOW_RADII_4[0] = GLOW_RADII_4[1] = GLOW_RADII_4[2] = GLOW_RADII_4[3] = var6;
      Initialization.getInstance().getManager().getRenderCore().getGlowOutlinePipeline().drawGlowOutline(tx(var0), ty(var1), td(var2), td(var3), var5, var4, GLOW_RADII_4, var7, var8);
   }

   public static void glowOutline(
      float var0, float var1, float var2, float var3, float var4, int var5, float var6, float var7, float var8, float var9, float var10, float var11
   ) {
      GLOW_RADII_4[0] = var6;
      GLOW_RADII_4[1] = var7;
      GLOW_RADII_4[2] = var8;
      GLOW_RADII_4[3] = var9;
      Initialization.getInstance()
         .getManager()
         .getRenderCore()
         .getGlowOutlinePipeline()
         .drawGlowOutline(tx(var0), ty(var1), td(var2), td(var3), var5, var4, GLOW_RADII_4, var10, var11);
   }

   public static Matrix4f createProjection() {
      int var0 = getFixedScaledWidth();
      int var1 = getFixedScaledHeight();
      return new Matrix4f().ortho(0.0F, var0, var1, 0.0F, -1000.0F, 1000.0F);
   }

   public static void arc(DrawContext var0, float var1, float var2, float var3, float var4, float var5, float var6, int var7, boolean var8) {
      arc(createProjection(), var1, var2, var3, var4, var5, var6, var7, var8);
   }

   public static void arc(DrawContext var0, float var1, float var2, float var3, float var4, float var5, float var6, boolean var7, int... var8) {
      arc(createProjection(), var1, var2, var3, var4, var5, var6, var7, var8);
   }

   public static void arc(Matrix4f var0, float var1, float var2, float var3, float var4, float var5, float var6, int var7, boolean var8) {
      if (var8) {
         OVERRIDE_TASKS.add(() -> Arc2D.draw(var0, var1, var2, var3, var4, var5, var6, 0.0F, var7));
      } else {
         Arc2D.draw(var0, var1, var2, var3, var4, var5, var6, 0.0F, var7);
      }
   }

   public static void arc(Matrix4f var0, float var1, float var2, float var3, float var4, float var5, float var6, boolean var7, int... var8) {
      if (var7) {
         OVERRIDE_TASKS.add(() -> Arc2D.draw(var0, var1, var2, var3, var4, var5, var6, 0.0F, var8));
      } else {
         Arc2D.draw(var0, var1, var2, var3, var4, var5, var6, 0.0F, var8);
      }
   }

   public static void arc(float var0, float var1, float var2, float var3, float var4, float var5, int var6) {
      Arc2D.draw(createProjection(), var0, var1, var2, var3, var4, var5, 0.0F, var6);
   }

   public static void arc(float var0, float var1, float var2, float var3, float var4, float var5, int... var6) {
      Arc2D.draw(createProjection(), var0, var1, var2, var3, var4, var5, 0.0F, var6);
   }

   public static void arcOutline(float var0, float var1, float var2, float var3, float var4, float var5, float var6, int var7, int var8) {
      ArcOutline2D.draw(createProjection(), var0, var1, var2, var3, var4, var5, var6, var7, var8, 0.0F);
   }

   public static void arcOutline(
      DrawContext var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7, int var8, int var9, boolean var10
   ) {
      Matrix4f var11 = createProjection();
      if (var10) {
         OVERRIDE_TASKS.add(() -> ArcOutline2D.draw(var11, var1, var2, var3, var4, var5, var6, var7, var8, var9, 0.0F));
      } else {
         ArcOutline2D.draw(var11, var1, var2, var3, var4, var5, var6, var7, var8, var9, 0.0F);
      }
   }

   public static void arcOutline(Matrix4f var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7, int var8, int var9) {
      ArcOutline2D.draw(var0, var1, var2, var3, var4, var5, var6, var7, var8, var9, 0.0F);
   }

   public static void flushOverrideTasks() {
      for (Runnable var1 : OVERRIDE_TASKS) {
         var1.run();
      }

      OVERRIDE_TASKS.clear();
   }

   public static boolean isInOverlayMode() {
      return inOverlayMode;
   }

   public static void cleanup() {
      OVERRIDE_TASKS.clear();
      Arc2D.shutdown();
      ArcOutline2D.shutdown();
   }
}
