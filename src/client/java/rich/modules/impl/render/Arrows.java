package rich.modules.impl.render;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.RaycastContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import rich.events.api.EventHandler;
import rich.events.impl.DrawEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.ColorSetting;
import rich.util.c;
import rich.util.animations.Animation;
import rich.util.animations.Direction;
import rich.util.animations.EaseInOutQuad;
import rich.util.animations.Easings;
import rich.util.animations.SmoothAnimation;

public class Arrows extends ModuleStructure {
   private static final Identifier ARROW_TEXTURE = Identifier.of("rich", "textures/world/arrow.png");
   public ColorSetting arrowColor = new ColorSetting("Цвет", "Цвет стрелок").value(-7773880);
   private final SmoothAnimation animationStep = new SmoothAnimation();
   private final SmoothAnimation animatedYaw = new SmoothAnimation();
   private final SmoothAnimation animatedPitch = new SmoothAnimation();
   private final SmoothAnimation animatedCameraYaw = new SmoothAnimation();
   private final Map<PlayerEntity, Arrows.Arrow> playerArrows = new ConcurrentHashMap<>();

   public static Arrows getInstance() {
      return c.keyCodec(Arrows.class);
   }

   public Arrows() {
      super("Arrows", "Показывает стрелки в сторону игроков", ModuleCategory.VISUALS);
      this.settings(this.arrowColor);
   }

   @Override
   public void deactivate() {
      this.playerArrows.clear();
   }

   @EventHandler
   public void onDraw(DrawEvent var1) {
      if (mc.player != null && mc.world != null) {
         if (mc.options.getPerspective() == Perspective.FIRST_PERSON) {
            DrawContext var2 = var1.getDrawContext();
            float var3 = var1.getPartialTicks();
            this.updateAnimations(var3);
            float var4 = 70.0F;
            if (mc.currentScreen instanceof InventoryScreen) {
               var4 += 80.0F;
            }

            if (mc.player.isSneaking()) {
               var4 -= 20.0F;
            }

            if (this.isMoving()) {
               var4 += 10.0F;
            }

            this.animationStep.run(var4, 1.0, Easings.EXPO_OUT, false);
            this.updatePlayerArrows();
            float var5 = mc.getWindow().getScaledWidth() / 2.0F;
            float var6 = mc.getWindow().getScaledHeight() / 2.0F;

            for (Arrows.Arrow var8 : this.playerArrows.values()) {
               this.renderArrow(var2, var3, var8, var5, var6);
            }
         }
      }
   }

   private void updateAnimations(float var1) {
      this.animationStep.update();
      this.animatedYaw.update();
      this.animatedPitch.update();
      this.animatedCameraYaw.update();
      float var2 = mc.player.input.getMovementInput().x;
      float var3 = mc.player.input.getMovementInput().y;
      this.animatedYaw.run(var2 * 5.0F, 0.75, Easings.EXPO_OUT);
      this.animatedPitch.run(var3 * 5.0F, 0.75, Easings.EXPO_OUT);
      this.animatedCameraYaw.run(mc.gameRenderer.getCamera().getYaw(), 0.75, Easings.EXPO_OUT, true);
   }

   private void updatePlayerArrows() {
      this.playerArrows.entrySet().removeIf(var1 -> !this.isValidPlayer(var1.getKey()) || var1.getKey().isRemoved());

      for (AbstractClientPlayerEntity var2 : mc.world.getPlayers()) {
         if (this.isValidPlayer(var2)) {
            this.playerArrows.computeIfAbsent(var2, var1 -> new Arrows.Arrow(var1, this.createFadeAnimation()));
         }
      }

      this.playerArrows.values().forEach(var1 -> {
         if (!this.isValidPlayer(var1.player)) {
            var1.fadeAnimation.setDirection(Direction.BACKWARDS);
         } else {
            var1.fadeAnimation.setDirection(Direction.FORWARDS);
         }
      });
   }

   private void renderArrow(DrawContext var1, float var2, Arrows.Arrow var3, float var4, float var5) {
      var3.updateAlpha();
      float var6 = var3.getAlpha();
      if (!(var6 <= 0.001F)) {
         PlayerEntity var7 = var3.player;
         Vec3d var8 = mc.gameRenderer.getCamera().getCameraPos();
         Vec3d var9 = new Vec3d(
            MathHelper.lerp(var2, var7.lastRenderX, var7.getX()),
            MathHelper.lerp(var2, var7.lastRenderY, var7.getY()),
            MathHelper.lerp(var2, var7.lastRenderZ, var7.getZ())
         );
         if (!(var9.subtract(var8).normalize().dotProduct(mc.player.getRotationVec(var2)) < 0.0)) {
            if (var3.shouldUpdateRaycast()) {
               var3.isBehindWall = mc.world
                     .raycast(new RaycastContext(var8, var9, net.minecraft.world.RaycastContext.ShapeType.COLLIDER, net.minecraft.world.RaycastContext.FluidHandling.NONE, mc.player))
                     .getType()
                  == net.minecraft.util.hit.HitResult.Type.BLOCK;
            }

            if (!var3.isBehindWall) {
               double var10 = var9.x - var8.x;
               double var12 = var9.z - var8.z;
               double var14 = this.animatedCameraYaw.getValue();
               double var16 = MathHelper.cos((float)(var14 * (Math.PI / 180.0)));
               double var18 = MathHelper.sin((float)(var14 * (Math.PI / 180.0)));
               double var20 = -(var12 * var16 - var10 * var18);
               double var22 = -(var10 * var16 + var12 * var18);
               float var24 = (float)(Math.atan2(var20, var22) * 180.0 / Math.PI);
               double var25 = this.animationStep.getValue() * var6 * MathHelper.cos((float)Math.toRadians(var24)) + var4;
               double var27 = this.animationStep.getValue() * var6 * MathHelper.sin((float)Math.toRadians(var24)) + var5;
               var25 += this.animatedYaw.getValue();
               var27 += this.animatedPitch.getValue();
               int var29 = this.applyAlpha(this.arrowColor.getColor(), var6);
               this.drawArrow(var1, (float)var25, (float)var27, var24, var29, 1.0F);
            }
         }
      }
   }

   private Animation createFadeAnimation() {
      Animation var1 = new EaseInOutQuad().setMs(200).setValue(1.0);
      var1.setDirection(Direction.FORWARDS);
      return var1;
   }

   private void drawArrow(DrawContext var1, float var2, float var3, float var4, int var5, float var6) {
      float var7 = 17.0F * var6;
      float var8 = var7 / 2.0F;
      var1.getMatrices().pushMatrix();
      var1.getMatrices().translate(var2, var3);
      var1.getMatrices().rotate((float)Math.toRadians(var4));
      var1.getMatrices().rotate((float)Math.toRadians(90.0));
      int var9 = (int)var7;
      var1.drawTexture(RenderPipelines.GUI_TEXTURED, ARROW_TEXTURE, (int)(1.0F - var8), -5, 0.0F, 0.0F, var9, var9, var9, var9, var5);
      var1.getMatrices().popMatrix();
   }

   private int applyAlpha(int var1, float var2) {
      int var3 = var1 >> 16 & 0xFF;
      int var4 = var1 >> 8 & 0xFF;
      int var5 = var1 & 0xFF;
      int var6 = (int)(var2 * 255.0F);
      return var6 << 24 | var3 << 16 | var4 << 8 | var5;
   }

   private boolean isValidPlayer(PlayerEntity var1) {
      return var1 != mc.player && !var1.isRemoved() && !var1.isInvisible();
   }

   private boolean isMoving() {
      return mc.player.input.getMovementInput().y != 0.0F || mc.player.input.getMovementInput().x != 0.0F;
   }

   private static class Arrow {
      final PlayerEntity player;
      final Animation fadeAnimation;
      float cachedAlpha = 0.0F;
      long lastAlphaUpdate = 0L;
      boolean isBehindWall = false;
      long lastRaycastTime = 0L;

      Arrow(PlayerEntity var1, Animation var2) {
         this.player = var1;
         this.fadeAnimation = var2;
      }

      void updateAlpha() {
         long var1 = System.currentTimeMillis();
         if (var1 - this.lastAlphaUpdate > 16L) {
            this.cachedAlpha = this.fadeAnimation.getOutput().floatValue();
            this.lastAlphaUpdate = var1;
         }
      }

      float getAlpha() {
         return this.cachedAlpha;
      }

      boolean shouldUpdateRaycast() {
         long var1 = System.currentTimeMillis();
         if (var1 - this.lastRaycastTime > 500L) {
            this.lastRaycastTime = var1;
            return true;
         } else {
            return false;
         }
      }

      boolean isDead() {
         return this.fadeAnimation.isDirection(Direction.BACKWARDS) && this.fadeAnimation.isDone() && this.cachedAlpha <= 0.0F;
      }
   }
}
