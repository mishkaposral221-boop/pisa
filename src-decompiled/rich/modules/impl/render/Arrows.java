package rich.modules.impl.render;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_10799;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_490;
import net.minecraft.class_5498;
import net.minecraft.class_742;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
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
   private static final class_2960 ARROW_TEXTURE = class_2960.method_60655("rich", "textures/world/arrow.png");
   public ColorSetting arrowColor = new ColorSetting("Цвет", "Цвет стрелок").value(-7773880);
   private final SmoothAnimation animationStep = new SmoothAnimation();
   private final SmoothAnimation animatedYaw = new SmoothAnimation();
   private final SmoothAnimation animatedPitch = new SmoothAnimation();
   private final SmoothAnimation animatedCameraYaw = new SmoothAnimation();
   private final Map<class_1657, Arrows.Arrow> playerArrows = new ConcurrentHashMap<>();

   public static Arrows getInstance() {
      return c.a(Arrows.class);
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
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (mc.field_1690.method_31044() == class_5498.field_26664) {
            class_332 var2 = var1.getDrawContext();
            float var3 = var1.getPartialTicks();
            this.updateAnimations(var3);
            float var4 = 70.0F;
            if (mc.field_1755 instanceof class_490) {
               var4 += 80.0F;
            }

            if (mc.field_1724.method_5715()) {
               var4 -= 20.0F;
            }

            if (this.isMoving()) {
               var4 += 10.0F;
            }

            this.animationStep.run(var4, 1.0, Easings.EXPO_OUT, false);
            this.updatePlayerArrows();
            float var5 = mc.method_22683().method_4486() / 2.0F;
            float var6 = mc.method_22683().method_4502() / 2.0F;

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
      float var2 = mc.field_1724.field_3913.method_3128().field_1343;
      float var3 = mc.field_1724.field_3913.method_3128().field_1342;
      this.animatedYaw.run(var2 * 5.0F, 0.75, Easings.EXPO_OUT);
      this.animatedPitch.run(var3 * 5.0F, 0.75, Easings.EXPO_OUT);
      this.animatedCameraYaw.run(mc.field_1773.method_19418().method_19330(), 0.75, Easings.EXPO_OUT, true);
   }

   private void updatePlayerArrows() {
      this.playerArrows.entrySet().removeIf(var1 -> !this.isValidPlayer(var1.getKey()) || var1.getKey().method_31481());

      for (class_742 var2 : mc.field_1687.method_18456()) {
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

   private void renderArrow(class_332 var1, float var2, Arrows.Arrow var3, float var4, float var5) {
      var3.updateAlpha();
      float var6 = var3.getAlpha();
      if (!(var6 <= 0.001F)) {
         class_1657 var7 = var3.player;
         class_243 var8 = mc.field_1773.method_19418().method_71156();
         class_243 var9 = new class_243(
            class_3532.method_16436(var2, var7.field_6038, var7.method_23317()),
            class_3532.method_16436(var2, var7.field_5971, var7.method_23318()),
            class_3532.method_16436(var2, var7.field_5989, var7.method_23321())
         );
         if (!(var9.method_1020(var8).method_1029().method_1026(mc.field_1724.method_5828(var2)) < 0.0)) {
            if (var3.shouldUpdateRaycast()) {
               var3.isBehindWall = mc.field_1687
                     .method_17742(new class_3959(var8, var9, class_3960.field_17558, class_242.field_1348, mc.field_1724))
                     .method_17783()
                  == class_240.field_1332;
            }

            if (!var3.isBehindWall) {
               double var10 = var9.field_1352 - var8.field_1352;
               double var12 = var9.field_1350 - var8.field_1350;
               double var14 = this.animatedCameraYaw.getValue();
               double var16 = class_3532.method_15362((float)(var14 * (Math.PI / 180.0)));
               double var18 = class_3532.method_15374((float)(var14 * (Math.PI / 180.0)));
               double var20 = -(var12 * var16 - var10 * var18);
               double var22 = -(var10 * var16 + var12 * var18);
               float var24 = (float)(Math.atan2(var20, var22) * 180.0 / Math.PI);
               double var25 = this.animationStep.getValue() * var6 * class_3532.method_15362((float)Math.toRadians(var24)) + var4;
               double var27 = this.animationStep.getValue() * var6 * class_3532.method_15374((float)Math.toRadians(var24)) + var5;
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

   private void drawArrow(class_332 var1, float var2, float var3, float var4, int var5, float var6) {
      float var7 = 17.0F * var6;
      float var8 = var7 / 2.0F;
      var1.method_51448().pushMatrix();
      var1.method_51448().translate(var2, var3);
      var1.method_51448().rotate((float)Math.toRadians(var4));
      var1.method_51448().rotate((float)Math.toRadians(90.0));
      int var9 = (int)var7;
      var1.method_25291(class_10799.field_56883, ARROW_TEXTURE, (int)(1.0F - var8), -5, 0.0F, 0.0F, var9, var9, var9, var9, var5);
      var1.method_51448().popMatrix();
   }

   private int applyAlpha(int var1, float var2) {
      int var3 = var1 >> 16 & 0xFF;
      int var4 = var1 >> 8 & 0xFF;
      int var5 = var1 & 0xFF;
      int var6 = (int)(var2 * 255.0F);
      return var6 << 24 | var3 << 16 | var4 << 8 | var5;
   }

   private boolean isValidPlayer(class_1657 var1) {
      return var1 != mc.field_1724 && !var1.method_31481() && !var1.method_5767();
   }

   private boolean isMoving() {
      return mc.field_1724.field_3913.method_3128().field_1342 != 0.0F || mc.field_1724.field_3913.method_3128().field_1343 != 0.0F;
   }

   private static class Arrow {
      final class_1657 player;
      final Animation fadeAnimation;
      float cachedAlpha = 0.0F;
      long lastAlphaUpdate = 0L;
      boolean isBehindWall = false;
      long lastRaycastTime = 0L;

      Arrow(class_1657 var1, Animation var2) {
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
