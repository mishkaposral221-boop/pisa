package rich.modules.impl.util;

import java.util.ArrayList;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import rich.events.api.EventHandler;
import rich.events.impl.WorldRenderEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.ColorSetting;
import rich.util.c;
import rich.util.math.MathUtils;
import rich.util.render.Render3D;

public class RadiusHelper extends ModuleStructure {
   private static final int IDX_DEZKA = 0;
   private static final int IDX_YAVKA = 1;
   private static final int IDX_FIRE_CHARGE = 2;
   private static final int IDX_GOD_AURA = 3;
   private static final int IDX_TRAPKA = 4;
   private static final int IDX_PLAST = 5;
   private static final int IDX_SNOWBALL = 6;
   private static final float OUTLINE_WIDTH = 3.0F;
   private static final float TRAJECTORY_WIDTH = 2.25F;
   private static final float CIRCLE_STEP = 5.0F;
   private static final float TRANSITION_DURATION = 0.5F;
   private static final int FILL_ALPHA = 85;
   private static final int OUTLINE_ALPHA = 255;
   private static final double DEZKA_RADIUS = 10.0;
   private static final double YAVKA_RADIUS = 10.0;
   private static final double GOD_AURA_RADIUS = 2.0;
   private static final double SNOWBALL_RADIUS = 7.0;
   private static final double SNOWBALL_SPEED = 1.5;
   private static final double SNOWBALL_GRAVITY = 0.03;
   private static final double SNOWBALL_DRAG = 0.99;
   private static final int SNOWBALL_MAX_STEPS = 160;
   private static final int SNOWBALL_SUBSTEPS = 6;
   private static final double PLAST_EXTRA_EXPAND = 0.5;
   private static final double PLAST_SURFACE_OFFSET = 0.01;
   private static final double DRAGON_SKIN_HALF = 3.5;
   private static final double DRAGON_PLAST_HALF = 1.0;
   private final ColorSetting dezkaColor = new ColorSetting("Цвет Дезки", "").value(-16755456);
   private final ColorSetting yavkaColor = new ColorSetting("Цвет Явки", "").value(-6710887);
   private final ColorSetting fireChargeColor = new ColorSetting("Цвет Огн. Заряда", "").value(-11206656);
   private final ColorSetting godAuraColor = new ColorSetting("Цвет Ауры", "").value(-16737895);
   private final ColorSetting trapkaColor = new ColorSetting("Цвет Трапки", "").value(-7650029);
   private final ColorSetting plastColor = new ColorSetting("Цвет Пласта", "").value(-13421773);
   private final ColorSetting snowballColor = new ColorSetting("Цвет Снежка", "").value(-6234881);
   private final BooleanSetting hitIndicator = new BooleanSetting("Подсветка попадания", "Подсвечивать при игроке в радиусе").setValue(true);
   private final ColorSetting hitColor = new ColorSetting("Цвет попадания", "").value(-16711800).visible(() -> this.hitIndicator.isValue());
   private final BooleanSetting fillEnabled = new BooleanSetting("Заполнение", "Заполнять зону цветом").setValue(true);
   private final BooleanSetting dragonSkin = new BooleanSetting("Драконий скин", "Размер 7x7 для трапки/пласта").setValue(false);
   private int currentFillColor;
   private int currentOutlineColor;
   private int targetFillColor;
   private int targetOutlineColor;
   private float transitionTimer = 0.0F;
   private boolean lastPlayersInRadius = false;
   private int activeTransitionItem = -1;

   public static RadiusHelper getInstance() {
      return c.a(RadiusHelper.class);
   }

   public RadiusHelper() {
      super("Radius Helper", "Показывает радиус/зону действия предметов", ModuleCategory.UTILITIES);
      this.settings(
         this.dezkaColor,
         this.yavkaColor,
         this.fireChargeColor,
         this.godAuraColor,
         this.trapkaColor,
         this.plastColor,
         this.snowballColor,
         this.hitIndicator,
         this.hitColor,
         this.fillEnabled,
         this.dragonSkin
      );
   }

   @Override
   public void deactivate() {
      this.activeTransitionItem = -1;
      super.deactivate();
   }

   @EventHandler
   public void onWorldRender(WorldRenderEvent var1) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         int var2 = this.resolveIndex(mc.field_1724.method_6047(), mc.field_1724.method_6079());
         class_243 var3 = new class_243(mc.field_1724.method_23317(), mc.field_1724.method_23318(), mc.field_1724.method_23321()).method_1031(0.0, -1.4, 0.0);
         switch (var2) {
            case 0:
               this.renderRadius(var1, var3, 10.0, 0, this.dezkaColor.getColor());
               break;
            case 1:
               this.renderRadius(var1, var3, 10.0, 1, this.yavkaColor.getColor());
               break;
            case 2:
               this.renderRadius(var1, var3, 10.0, 2, this.fireChargeColor.getColor());
               break;
            case 3:
               this.renderRadius(var1, var3, 2.0, 3, this.godAuraColor.getColor());
               break;
            case 4:
               class_238 var5 = this.getTrapkaBox();
               this.updateTransition(var1, 4, this.hasPlayersInBox(var5), this.trapkaColor.getColor());
               if (this.fillEnabled.isValue()) {
                  this.drawFilledBox(var5, this.currentFillColor);
               }

               Render3D.drawBox(var5, this.currentOutlineColor, 3.0F, true, false, false);
               break;
            case 5:
               class_238 var4 = this.getPlastBox(var1);
               this.updateTransition(var1, 5, this.hasPlayersInBox(var4), this.plastColor.getColor());
               if (this.fillEnabled.isValue()) {
                  this.drawFilledBox(var4, this.currentFillColor);
               }

               Render3D.drawBox(var4, this.currentOutlineColor, 3.0F, true, false, false);
               break;
            case 6:
               this.renderSnowball(var1);
               break;
            default:
               this.activeTransitionItem = -1;
         }
      }
   }

   private int resolveIndex(class_1799 var1, class_1799 var2) {
      int var3 = this.itemIndex(var1);
      return var3 != -1 ? var3 : this.itemIndex(var2);
   }

   private int itemIndex(class_1799 var1) {
      if (var1 != null && !var1.method_7960()) {
         class_1792 var2 = var1.method_7909();
         if (var2 == class_1802.field_8449) {
            return 0;
         } else if (var2 == class_1802.field_8479) {
            return 1;
         } else if (var2 == class_1802.field_8814) {
            return 2;
         } else if (var2 == class_1802.field_8614) {
            return 3;
         } else if (var2 == class_1802.field_22021) {
            return 4;
         } else if (var2 == class_1802.field_8551) {
            return 5;
         } else {
            return var2 == class_1802.field_8543 ? 6 : -1;
         }
      } else {
         return -1;
      }
   }

   private void renderRadius(WorldRenderEvent var1, class_243 var2, double var3, int var5, int var6) {
      this.initTransition(var5, var6);
      boolean var7 = this.hitIndicator.isValue() && this.hasPlayersInRadius(var2, var3);
      int var8 = this.hitIndicator.isValue() ? this.hitColor.getColor() : var6;
      this.updateColors(var7, withAlpha(var6, 85), withAlpha(var6, 255), withAlpha(var8, 85), withAlpha(var8, 255), var1.getPartialTicks());
      this.drawCircle(var2, var3);
   }

   private void drawCircle(class_243 var1, double var2) {
      class_243 var4 = null;
      class_243 var5 = null;

      for (float var6 = 0.0F; var6 <= 360.0F; var6 += 5.0F) {
         double var7 = Math.toRadians(var6);
         class_243 var9 = new class_243(var1.field_1352 + Math.sin(var7) * var2, var1.field_1351, var1.field_1350 - Math.cos(var7) * var2);
         if (var5 != null) {
            Render3D.drawLine(var5, var9, this.currentOutlineColor, 3.0F, false);
            if (this.fillEnabled.isValue()) {
               Render3D.drawQuad(var1, var5, var9, var1, this.currentFillColor, false);
            }
         } else {
            var4 = var9;
         }

         var5 = var9;
      }

      if (var5 != null && var4 != null) {
         Render3D.drawLine(var5, var4, this.currentOutlineColor, 3.0F, false);
         if (this.fillEnabled.isValue()) {
            Render3D.drawQuad(var1, var5, var4, var1, this.currentFillColor, false);
         }
      }
   }

   private void renderSnowball(WorldRenderEvent var1) {
      float var2 = var1.getPartialTicks();
      ArrayList var3 = new ArrayList();
      class_243 var4 = this.eyePos(var2);
      class_243 var5 = mc.field_1724.method_5828(var2).method_1029().method_1021(1.5);
      var3.add(var4);
      class_243 var6 = null;

      label57:
      for (int var7 = 0; var7 < 160; var7++) {
         for (int var8 = 0; var8 < 6; var8++) {
            class_243 var9 = var4;
            var4 = var4.method_1019(var5.method_1021(0.16666666666666666));
            class_3965 var10 = mc.field_1687.method_17742(new class_3959(var9, var4, class_3960.field_17558, class_242.field_1347, mc.field_1724));
            if (var10.method_17783() == class_240.field_1332) {
               var6 = var10.method_17784();
               var3.add(var6);
               break label57;
            }

            var3.add(var4);
            if (var4.field_1351 < mc.field_1687.method_31607() - 8) {
               var6 = var4;
               break label57;
            }

            double var11 = Math.pow(0.99, 0.16666666666666666);
            var5 = var5.method_1023(0.0, 0.005, 0.0).method_1021(var11);
         }
      }

      if (var3.size() < 2) {
         this.activeTransitionItem = -1;
      } else {
         if (var6 == null) {
            var6 = (class_243)var3.get(var3.size() - 1);
         }

         int var13 = this.snowballColor.getColor();
         this.initTransition(6, var13);
         class_243 var14 = var6.method_1031(0.0, 0.03, 0.0);
         boolean var15 = this.hitIndicator.isValue() && this.hasPlayersInRadius(var14, 7.0);
         int var16 = this.hitIndicator.isValue() ? this.hitColor.getColor() : var13;
         this.updateColors(var15, withAlpha(var13, 85), withAlpha(var13, 255), withAlpha(var16, 85), withAlpha(var16, 255), var2);

         for (int var17 = 1; var17 < var3.size(); var17++) {
            Render3D.drawLine((class_243)var3.get(var17 - 1), (class_243)var3.get(var17), this.currentOutlineColor, 2.25F, false);
         }

         this.drawCircle(var14, 7.0);
      }
   }

   private class_238 getTrapkaBox() {
      class_243 var1 = MathUtils.interpolate(mc.field_1724);
      double var2 = Math.floor(var1.field_1352) + 0.5;
      double var4 = Math.floor(var1.field_1351) + 0.5 + 1.625;
      double var6 = Math.floor(var1.field_1350) + 0.5;
      double var8 = this.dragonSkin.isValue() ? 3.5 : 2.0;
      return new class_238(var2 - var8, var4 - var8, var6 - var8, var2 + var8, var4 + var8, var6 + var8);
   }

   private class_238 getPlastBox(WorldRenderEvent var1) {
      float var2 = var1.getPartialTicks();
      if (this.dragonSkin.isValue()) {
         return this.getDragonPlastBox(var2);
      }

      float var3 = 2.0F;
      float var4 = 2.0F;
      float var5 = 0.75F;
      class_243 var6 = mc.field_1724.method_5828(var2);
      class_243 var7 = this.eyePos(var2);
      class_2350 var8 = this.dominantDir(var6);
      double var9 = var5 + 0.01;
      class_243 var11 = var7.method_1019(var6.method_1021(4.0)).method_1031(var8.method_10148() * var9, var8.method_10164() * var9, var8.method_10165() * var9);

      class_238 var12 = switch (var8.method_10166()) {
         case field_11048 -> new class_238(
            var11.field_1352 - var5,
            var11.field_1351 - var4,
            var11.field_1350 - var3,
            var11.field_1352 + var5,
            var11.field_1351 + var4,
            var11.field_1350 + var3
         );
         case field_11052 -> new class_238(
            var11.field_1352 - var3,
            var11.field_1351 - var5,
            var11.field_1350 - var4,
            var11.field_1352 + var3,
            var11.field_1351 + var5,
            var11.field_1350 + var4
         );
         case field_11051 -> new class_238(
            var11.field_1352 - var3,
            var11.field_1351 - var4,
            var11.field_1350 - var5,
            var11.field_1352 + var3,
            var11.field_1351 + var4,
            var11.field_1350 + var5
         );
         default -> throw new MatchException(null, null);
      };

      return switch (var8.method_10166()) {
         case field_11048 -> new class_238(
            var12.field_1323, var12.field_1322 - 0.5, var12.field_1321 - 0.5, var12.field_1320, var12.field_1325 + 0.5, var12.field_1324 + 0.5
         );
         case field_11052 -> new class_238(
            var12.field_1323 - 0.5, var12.field_1322, var12.field_1321 - 0.5, var12.field_1320 + 0.5, var12.field_1325, var12.field_1324 + 0.5
         );
         case field_11051 -> new class_238(
            var12.field_1323 - 0.5, var12.field_1322 - 0.5, var12.field_1321, var12.field_1320 + 0.5, var12.field_1325 + 0.5, var12.field_1324
         );
         default -> throw new MatchException(null, null);
      };
   }

   private class_238 getDragonPlastBox(float var1) {
      class_243 var2 = mc.field_1724.method_5828(var1);
      class_243 var3 = this.eyePos(var1);
      class_2350 var4 = this.dominantDir(var2);
      double var5 = 1.01;
      class_243 var7 = var3.method_1019(var2.method_1021(4.0)).method_1031(var4.method_10148() * var5, var4.method_10164() * var5, var4.method_10165() * var5);

      return switch (var4.method_10166()) {
         case field_11048 -> new class_238(
            var7.field_1352 - 1.0, var7.field_1351 - 3.5, var7.field_1350 - 3.5, var7.field_1352 + 1.0, var7.field_1351 + 3.5, var7.field_1350 + 3.5
         );
         case field_11052 -> new class_238(
            var7.field_1352 - 3.5, var7.field_1351 - 1.0, var7.field_1350 - 3.5, var7.field_1352 + 3.5, var7.field_1351 + 1.0, var7.field_1350 + 3.5
         );
         case field_11051 -> new class_238(
            var7.field_1352 - 3.5, var7.field_1351 - 3.5, var7.field_1350 - 1.0, var7.field_1352 + 3.5, var7.field_1351 + 3.5, var7.field_1350 + 1.0
         );
         default -> throw new MatchException(null, null);
      };
   }

   private void renderPlane(class_238 var1) {
      if (this.fillEnabled.isValue()) {
         this.drawFilledBox(var1, this.currentFillColor);
      }

      Render3D.drawBox(var1, this.currentOutlineColor, 3.0F, true, false, false);
   }

   private boolean hasPlayersInRadius(class_243 var1, double var2) {
      double var4 = var2 * var2;

      for (class_1657 var7 : mc.field_1687.method_18456()) {
         if (!this.shouldSkip(var7)
            && this.canSee(var7)
            && new class_243(var7.method_23317(), var7.method_23318(), var7.method_23321()).method_1025(var1) <= var4) {
            return true;
         }
      }

      return false;
   }

   private boolean hasPlayersInBox(class_238 var1) {
      for (class_1657 var3 : mc.field_1687.method_18456()) {
         if (!this.shouldSkip(var3) && this.canSee(var3) && var3.method_5829().method_994(var1)) {
            return true;
         }
      }

      return false;
   }

   private boolean shouldSkip(class_1657 var1) {
      return var1 == mc.field_1724 || !var1.method_5805() || var1.method_7325() || var1.method_5767() || var1.method_5756(mc.field_1724);
   }

   private boolean canSee(class_1657 var1) {
      class_243 var2 = mc.field_1724.method_33571();
      class_238 var3 = var1.method_5829();
      class_243[] var4 = new class_243[]{
         var1.method_33571(),
         var3.method_1005(),
         new class_243(var3.field_1323, var3.method_1005().field_1351, var3.field_1321),
         new class_243(var3.field_1320, var3.method_1005().field_1351, var3.field_1324)
      };

      for (class_243 var8 : var4) {
         class_3965 var9 = mc.field_1687.method_17742(new class_3959(var2, var8, class_3960.field_17558, class_242.field_1348, mc.field_1724));
         if (var9.method_17783() == class_240.field_1333) {
            return true;
         }
      }

      return false;
   }

   private void initTransition(int var1, int var2) {
      if (this.activeTransitionItem != var1) {
         this.activeTransitionItem = var1;
         this.lastPlayersInRadius = false;
         this.transitionTimer = 0.0F;
         this.currentFillColor = withAlpha(var2, 85);
         this.currentOutlineColor = withAlpha(var2, 255);
         this.targetFillColor = this.currentFillColor;
         this.targetOutlineColor = this.currentOutlineColor;
      } else if (this.currentFillColor == 0) {
         this.currentFillColor = withAlpha(var2, 85);
         this.currentOutlineColor = withAlpha(var2, 255);
         this.targetFillColor = this.currentFillColor;
         this.targetOutlineColor = this.currentOutlineColor;
      }
   }

   private void updateTransition(WorldRenderEvent var1, int var2, boolean var3, int var4) {
      this.initTransition(var2, var4);
      int var5 = this.hitIndicator.isValue() ? this.hitColor.getColor() : var4;
      this.updateColors(
         this.hitIndicator.isValue() && var3, withAlpha(var4, 85), withAlpha(var4, 255), withAlpha(var5, 85), withAlpha(var5, 255), var1.getPartialTicks()
      );
   }

   private void updateColors(boolean var1, int var2, int var3, int var4, int var5, float var6) {
      if (var1 != this.lastPlayersInRadius) {
         this.transitionTimer = 0.0F;
         this.lastPlayersInRadius = var1;
      }

      this.targetFillColor = var1 ? var4 : var2;
      this.targetOutlineColor = var1 ? var5 : var3;
      this.transitionTimer = Math.min(this.transitionTimer + var6 / 0.5F, 1.0F);
      this.currentFillColor = lerpColor(this.currentFillColor, this.targetFillColor, this.transitionTimer);
      this.currentOutlineColor = lerpColor(this.currentOutlineColor, this.targetOutlineColor, this.transitionTimer);
   }

   private void drawFilledBox(class_238 var1, int var2) {
      Render3D.drawQuad(
         new class_243(var1.field_1323, var1.field_1322, var1.field_1321),
         new class_243(var1.field_1320, var1.field_1322, var1.field_1321),
         new class_243(var1.field_1320, var1.field_1322, var1.field_1324),
         new class_243(var1.field_1323, var1.field_1322, var1.field_1324),
         var2,
         false
      );
      Render3D.drawQuad(
         new class_243(var1.field_1323, var1.field_1322, var1.field_1321),
         new class_243(var1.field_1323, var1.field_1325, var1.field_1321),
         new class_243(var1.field_1320, var1.field_1325, var1.field_1321),
         new class_243(var1.field_1320, var1.field_1322, var1.field_1321),
         var2,
         false
      );
      Render3D.drawQuad(
         new class_243(var1.field_1320, var1.field_1322, var1.field_1321),
         new class_243(var1.field_1320, var1.field_1325, var1.field_1321),
         new class_243(var1.field_1320, var1.field_1325, var1.field_1324),
         new class_243(var1.field_1320, var1.field_1322, var1.field_1324),
         var2,
         false
      );
      Render3D.drawQuad(
         new class_243(var1.field_1323, var1.field_1322, var1.field_1324),
         new class_243(var1.field_1320, var1.field_1322, var1.field_1324),
         new class_243(var1.field_1320, var1.field_1325, var1.field_1324),
         new class_243(var1.field_1323, var1.field_1325, var1.field_1324),
         var2,
         false
      );
      Render3D.drawQuad(
         new class_243(var1.field_1323, var1.field_1322, var1.field_1321),
         new class_243(var1.field_1323, var1.field_1322, var1.field_1324),
         new class_243(var1.field_1323, var1.field_1325, var1.field_1324),
         new class_243(var1.field_1323, var1.field_1325, var1.field_1321),
         var2,
         false
      );
      Render3D.drawQuad(
         new class_243(var1.field_1323, var1.field_1325, var1.field_1321),
         new class_243(var1.field_1323, var1.field_1325, var1.field_1324),
         new class_243(var1.field_1320, var1.field_1325, var1.field_1324),
         new class_243(var1.field_1320, var1.field_1325, var1.field_1321),
         var2,
         false
      );
   }

   private class_243 eyePos(float var1) {
      double var2 = mc.field_1724.field_6014 + (mc.field_1724.method_23317() - mc.field_1724.field_6014) * var1;
      double var4 = mc.field_1724.field_6036
         + (mc.field_1724.method_23318() - mc.field_1724.field_6036) * var1
         + mc.field_1724.method_18381(mc.field_1724.method_18376());
      double var6 = mc.field_1724.field_5969 + (mc.field_1724.method_23321() - mc.field_1724.field_5969) * var1;
      return new class_243(var2, var4, var6);
   }

   private class_2350 dominantDir(class_243 var1) {
      double var2 = Math.abs(var1.field_1352);
      double var4 = Math.abs(var1.field_1351);
      double var6 = Math.abs(var1.field_1350);
      if (var4 >= var2 && var4 >= var6) {
         return var1.field_1351 >= 0.0 ? class_2350.field_11036 : class_2350.field_11033;
      } else if (var2 >= var6) {
         return var1.field_1352 >= 0.0 ? class_2350.field_11034 : class_2350.field_11039;
      } else {
         return var1.field_1350 >= 0.0 ? class_2350.field_11035 : class_2350.field_11043;
      }
   }

   private static int withAlpha(int var0, int var1) {
      return class_3532.method_15340(var1, 0, 255) << 24 | var0 & 16777215;
   }

   private static int lerpColor(int var0, int var1, float var2) {
      int var3 = var0 >> 24 & 0xFF;
      int var4 = var0 >> 16 & 0xFF;
      int var5 = var0 >> 8 & 0xFF;
      int var6 = var0 & 0xFF;
      int var7 = var1 >> 24 & 0xFF;
      int var8 = var1 >> 16 & 0xFF;
      int var9 = var1 >> 8 & 0xFF;
      int var10 = var1 & 0xFF;
      return (int)(var3 + (var7 - var3) * var2) << 24
         | (int)(var4 + (var8 - var4) * var2) << 16
         | (int)(var5 + (var9 - var5) * var2) << 8
         | (int)(var6 + (var10 - var6) * var2);
   }
}
