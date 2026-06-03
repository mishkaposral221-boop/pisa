package rich.modules.impl.player;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_746;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import rich.events.api.EventHandler;
import rich.events.impl.WorldRenderEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.MultiSelectSetting;
import rich.util.c;
import rich.util.render.Render3D;

public class RadiusHelper extends ModuleStructure {
   private static final String ITEM_DEZKA = "Дезка";
   private static final String ITEM_YAVKA = "Явка";
   private static final String ITEM_FIRE_CHARGE = "Огненный Заряд";
   private static final String ITEM_GOD_AURA = "Божья Аура";
   private static final String ITEM_TRAPKA = "Трапка";
   private static final String ITEM_PLAST = "Пласт";
   private static final String ITEM_SNOWBALL = "Снежок";
   private static final int IDX_DEZKA = 0;
   private static final int IDX_YAVKA = 1;
   private static final int IDX_FIRE_CHARGE = 2;
   private static final int IDX_GOD_AURA = 3;
   private static final int IDX_TRAPKA = 4;
   private static final int IDX_PLAST = 5;
   private static final int IDX_SNOWBALL = 6;
   private static final int FILL_ALPHA = 85;
   private static final int OUTLINE_ALPHA = 255;
   private static final float TRANSITION_DURATION = 0.5F;
   private static final float CIRCLE_STEP_DEGREES = 5.0F;
   private static final float OUTLINE_WIDTH = 3.0F;
   private static final double PLAST_EXTRA_EXPAND = 0.5;
   private static final double PLAST_SURFACE_OFFSET = 0.01;
   private static final double DEZKA_RADIUS = 10.0;
   private static final double YAVKA_RADIUS = 10.0;
   private static final double GOD_AURA_RADIUS = 2.0;
   private static final double DRAGON_SKIN_SIZE = 7.0;
   private static final double DRAGON_SKIN_HALF_SIZE = 3.5;
   private static final double DRAGON_PLAST_DEPTH = 2.0;
   private static final double DRAGON_PLAST_HALF_DEPTH = 1.0;
   private static final float TRAJECTORY_WIDTH = 2.25F;
   private static final double SNOWBALL_RADIUS = 7.0;
   private static final double SNOWBALL_SPEED = 1.5;
   private static final double SNOWBALL_GRAVITY = 0.03;
   private static final double SNOWBALL_DRAG = 0.99;
   private static final int SNOWBALL_MAX_STEPS = 160;
   private static final int SNOWBALL_SUBSTEPS = 6;
   private final MultiSelectSetting items = new MultiSelectSetting("Предметы", "Выберите предметы для отображения")
      .value("Дезка", "Явка", "Огненный Заряд", "Божья Аура", "Трапка", "Пласт", "Снежок")
      .selected("Дезка", "Явка", "Огненный Заряд", "Божья Аура", "Трапка", "Пласт", "Снежок");
   private final ColorSetting dezkaColor = new ColorSetting("Цвет Дезки", "Цвет радиуса дезки").value(-16755456);
   private final ColorSetting yavkaColor = new ColorSetting("Цвет Явки", "Цвет радиуса явки").value(-6710887);
   private final ColorSetting fireChargeColor = new ColorSetting("Цвет Огненного Заряда", "Цвет радиуса огненного заряда").value(-11206656);
   private final ColorSetting godAuraColor = new ColorSetting("Цвет Божьей Ауры", "Цвет радиуса божьей ауры").value(-16737895);
   private final ColorSetting trapkaColor = new ColorSetting("Цвет Трапки", "Цвет радиуса трапки").value(-7650029);
   private final ColorSetting plastColor = new ColorSetting("Цвет Пласта", "Цвет радиуса пластика").value(-13421773);
   private final ColorSetting snowballColor = new ColorSetting("Цвет Снежка", "Цвет траектории снежка").value(-6234881);
   private final BooleanSetting hitIndicator = new BooleanSetting("Подсвечивать при попадании", "Подсвечивать когда игроки в радиусе").setValue(true);
   private final ColorSetting hitColor = new ColorSetting("Цвет Подсвечивания", "Цвет при попадании в игроков").value(-16742145);
   private final BooleanSetting fillEnabled = new BooleanSetting("Заполнение", "Заливать область").setValue(true);
   private final BooleanSetting dragonSkin = new BooleanSetting("Драконий скин", "Увеличить размер для драконьего скина").setValue(false);
   private int currentFillColor = withAlpha(-16755456, 85);
   private int currentOutlineColor = withAlpha(-16755456, 255);
   private int targetFillColor = this.currentFillColor;
   private int targetOutlineColor = this.currentOutlineColor;
   private float transitionTimer = 0.0F;
   private boolean lastPlayersInRadius = false;
   private int activeTransitionItem = -1;

   public static RadiusHelper getInstance() {
      return c.a(RadiusHelper.class);
   }

   public RadiusHelper() {
      super("RadiusHelper", "Показывает радиус/зону действия предметов", ModuleCategory.UTILITIES);
      this.settings(
         this.items,
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

   @EventHandler
   public void onRender3D(WorldRenderEvent var1) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         class_746 var2 = mc.field_1724;
         class_1799 var3 = var2.method_6047();
         class_1799 var4 = var2.method_6079();
         int var5 = this.resolveActiveItemIndex(var3, var4);
         class_243 var6 = var2.method_30950(var1.getPartialTicks()).method_1031(0.0, -1.4, 0.0);
         if (var5 == 0) {
            this.renderRadiusPreset(var1, var2, var6, 10.0, 0, this.dezkaColor.getColor());
         } else if (var5 == 1) {
            this.renderRadiusPreset(var1, var2, var6, 10.0, 1, this.yavkaColor.getColor());
         } else if (var5 == 2) {
            this.renderRadiusPreset(var1, var2, var6, 10.0, 2, this.fireChargeColor.getColor());
         } else if (var5 == 3) {
            this.renderRadiusPreset(var1, var2, var6, 2.0, 3, this.godAuraColor.getColor());
         } else if (var5 == 6) {
            this.renderSnowballPrediction(var1, var2, 6, this.snowballColor.getColor());
         } else if (var5 == 4) {
            class_238 var12 = this.getTrapkaBox(var2);
            int var13 = this.trapkaColor.getColor();
            boolean var14 = this.hasPlayersInBox(var2, var12);
            boolean var15 = this.hitIndicator.isValue() && var14;
            int var16 = this.hitIndicator.isValue() ? this.hitColor.getColor() : var13;
            this.updateTransition(var1, 4, var15, var13, var16);
            this.renderCubeOutline(var12, this.currentOutlineColor);
         } else if (var5 != 5) {
            this.activeTransitionItem = -1;
         } else {
            class_238 var7 = this.getPlastBox(var1, var2);
            int var8 = this.plastColor.getColor();
            boolean var9 = this.hasPlayersInPlastBox(var2, var7);
            boolean var10 = this.hitIndicator.isValue() && var9;
            int var11 = this.hitIndicator.isValue() ? this.hitColor.getColor() : var8;
            this.updateTransition(var1, 5, var10, var8, var11);
            this.renderPlane(var7, this.currentFillColor, this.currentOutlineColor);
         }
      }
   }

   private int resolveActiveItemIndex(class_1799 var1, class_1799 var2) {
      int var3 = this.getEnabledItemIndex(var1);
      return var3 != -1 ? var3 : this.getEnabledItemIndex(var2);
   }

   private int getEnabledItemIndex(class_1799 var1) {
      if (var1 != null && !var1.method_7960()) {
         class_1792 var2 = var1.method_7909();
         if (var2 == class_1802.field_8449) {
            return this.items.isSelected("Дезка") ? 0 : -1;
         } else if (var2 == class_1802.field_8479) {
            return this.items.isSelected("Явка") ? 1 : -1;
         } else if (var2 == class_1802.field_8814) {
            return this.items.isSelected("Огненный Заряд") ? 2 : -1;
         } else if (var2 == class_1802.field_8614) {
            return this.items.isSelected("Божья Аура") ? 3 : -1;
         } else if (var2 == class_1802.field_22021) {
            return this.items.isSelected("Трапка") ? 4 : -1;
         } else if (var2 == class_1802.field_8551) {
            return this.items.isSelected("Пласт") ? 5 : -1;
         } else if (var2 == class_1802.field_8543) {
            return this.items.isSelected("Снежок") ? 6 : -1;
         } else {
            return -1;
         }
      } else {
         return -1;
      }
   }

   private void renderRadiusPreset(WorldRenderEvent var1, class_746 var2, class_243 var3, double var4, int var6, int var7) {
      if (this.activeTransitionItem != var6) {
         this.activeTransitionItem = var6;
         this.lastPlayersInRadius = false;
         this.transitionTimer = 0.0F;
         this.currentFillColor = withAlpha(var7, 85);
         this.currentOutlineColor = withAlpha(var7, 255);
         this.targetFillColor = this.currentFillColor;
         this.targetOutlineColor = this.currentOutlineColor;
      }

      boolean var8 = this.hasPlayersInRadius(var2, var3, var4);
      boolean var9 = this.hitIndicator.isValue() && var8;
      int var10 = this.hitIndicator.isValue() ? this.hitColor.getColor() : var7;
      this.updateColors(var9, withAlpha(var7, 85), withAlpha(var7, 255), withAlpha(var10, 85), withAlpha(var10, 255), var1.getPartialTicks());
      this.renderRadiusCircle(var2, var4, this.currentFillColor, this.currentOutlineColor);
   }

   private void renderSnowballPrediction(WorldRenderEvent var1, class_746 var2, int var3, int var4) {
      RadiusHelper.SnowballPrediction var5 = this.predictSnowballPrediction(var2, var1.getPartialTicks());
      if (var5 != null && var5.trajectory().size() >= 2) {
         if (this.activeTransitionItem != var3) {
            this.activeTransitionItem = var3;
            this.lastPlayersInRadius = false;
            this.transitionTimer = 0.0F;
            this.currentFillColor = withAlpha(var4, 85);
            this.currentOutlineColor = withAlpha(var4, 255);
            this.targetFillColor = this.currentFillColor;
            this.targetOutlineColor = this.currentOutlineColor;
         }

         class_243 var6 = var5.landingPos().method_1031(0.0, 0.03, 0.0);
         boolean var7 = this.hasPlayersInRadius(var2, var6, 7.0);
         boolean var8 = this.hitIndicator.isValue() && var7;
         int var9 = this.hitIndicator.isValue() ? this.hitColor.getColor() : var4;
         this.updateColors(var8, withAlpha(var4, 85), withAlpha(var4, 255), withAlpha(var9, 85), withAlpha(var9, 255), var1.getPartialTicks());
         this.renderTrajectory(var5.trajectory(), this.currentOutlineColor);
         this.renderRadiusCircle(var6, 7.0, this.currentFillColor, this.currentOutlineColor);
      } else {
         this.activeTransitionItem = -1;
      }
   }

   private RadiusHelper.SnowballPrediction predictSnowballPrediction(class_746 var1, float var2) {
      if (mc.field_1687 == null) {
         return null;
      }

      ArrayList var3 = new ArrayList();
      class_243 var4 = this.getInterpolatedEyePos(var1, var2);
      class_243 var5 = var1.method_5828(var2).method_1029().method_1021(1.5);
      var3.add(var4);
      class_243 var6 = null;

      label38:
      for (int var7 = 0; var7 < 160; var7++) {
         for (int var8 = 0; var8 < 6; var8++) {
            class_243 var9 = var4;
            class_243 var10 = var5.method_1021(0.16666666666666666);
            var4 = var4.method_1019(var10);
            class_3965 var11 = mc.field_1687.method_17742(new class_3959(var9, var4, class_3960.field_17558, class_242.field_1347, var1));
            if (var11.method_17783() == class_240.field_1332) {
               class_3965 var14 = var11;
               var6 = var14.method_17784();
               var3.add(var6);
               break label38;
            }

            var3.add(var4);
            if (var4.field_1351 < mc.field_1687.method_31607() - 8.0) {
               var6 = var4;
               break label38;
            }

            double var12 = Math.pow(0.99, 0.16666666666666666);
            var5 = var5.method_1023(0.0, 0.005, 0.0).method_1021(var12);
         }
      }

      if (var3.size() < 2) {
         return null;
      }

      if (var6 == null) {
         var6 = (class_243)var3.get(var3.size() - 1);
      }

      return new RadiusHelper.SnowballPrediction(var3, var6);
   }

   private void renderTrajectory(List<class_243> var1, int var2) {
      for (int var3 = 1; var3 < var1.size(); var3++) {
         Render3D.drawLine((class_243)var1.get(var3 - 1), (class_243)var1.get(var3), var2, 2.25F, false);
      }
   }

   private void updateColors(boolean var1, int var2, int var3, int var4, int var5, float var6) {
      if (var1 != this.lastPlayersInRadius) {
         this.transitionTimer = 0.0F;
         this.lastPlayersInRadius = var1;
      }

      this.targetFillColor = var1 ? var4 : var2;
      this.targetOutlineColor = var1 ? var5 : var3;
      this.transitionTimer = Math.min(this.transitionTimer + var6 / 0.5F, 1.0F);
      this.currentFillColor = this.lerpColor(this.currentFillColor, this.targetFillColor, this.transitionTimer);
      this.currentOutlineColor = this.lerpColor(this.currentOutlineColor, this.targetOutlineColor, this.transitionTimer);
   }

   private void updateTransition(WorldRenderEvent var1, int var2, boolean var3, int var4, int var5) {
      if (this.activeTransitionItem != var2) {
         this.activeTransitionItem = var2;
         this.lastPlayersInRadius = false;
         this.transitionTimer = 0.0F;
         this.currentFillColor = withAlpha(var4, 85);
         this.currentOutlineColor = withAlpha(var4, 255);
         this.targetFillColor = this.currentFillColor;
         this.targetOutlineColor = this.currentOutlineColor;
      }

      this.updateColors(var3, withAlpha(var4, 85), withAlpha(var4, 255), withAlpha(var5, 85), withAlpha(var5, 255), var1.getPartialTicks());
   }

   private void renderPlane(class_238 var1, int var2, int var3) {
      if (this.fillEnabled.isValue()) {
         class_243 var4 = new class_243(var1.field_1323, var1.field_1322, var1.field_1321);
         class_243 var5 = new class_243(var1.field_1320, var1.field_1325, var1.field_1324);
         Render3D.drawQuad(
            new class_243(var4.field_1352, var4.field_1351, var4.field_1350),
            new class_243(var5.field_1352, var4.field_1351, var4.field_1350),
            new class_243(var5.field_1352, var4.field_1351, var5.field_1350),
            new class_243(var4.field_1352, var4.field_1351, var5.field_1350),
            var2,
            false
         );
      }

      Render3D.drawBox(var1, var3, 3.0F, true, false, false);
   }

   private int lerpColor(int var1, int var2, float var3) {
      int var4 = var1 >> 24 & 0xFF;
      int var5 = var1 >> 16 & 0xFF;
      int var6 = var1 >> 8 & 0xFF;
      int var7 = var1 & 0xFF;
      int var8 = var2 >> 24 & 0xFF;
      int var9 = var2 >> 16 & 0xFF;
      int var10 = var2 >> 8 & 0xFF;
      int var11 = var2 & 0xFF;
      int var12 = (int)(var4 + (var8 - var4) * var3);
      int var13 = (int)(var5 + (var9 - var5) * var3);
      int var14 = (int)(var6 + (var10 - var6) * var3);
      int var15 = (int)(var7 + (var11 - var7) * var3);
      return var12 << 24 | var13 << 16 | var14 << 8 | var15;
   }

   private boolean hasPlayersInRadius(class_746 var1, class_243 var2, double var3) {
      double var5 = var3 * var3;

      for (class_1657 var8 : mc.field_1687.method_18456()) {
         if (!this.shouldIgnoreHitHighlightTarget(var1, var8)
            && this.canSeeTarget(var1, var8)
            && var8.method_30950(mc.method_61966().method_60637(true)).method_1025(var2) <= var5) {
            return true;
         }
      }

      return false;
   }

   private boolean hasPlayersInBox(class_746 var1, class_238 var2) {
      for (class_1657 var4 : mc.field_1687.method_18456()) {
         if (!this.shouldIgnoreHitHighlightTarget(var1, var4) && this.canSeeTarget(var1, var4) && var4.method_5829().method_994(var2)) {
            return true;
         }
      }

      return false;
   }

   private boolean hasPlayersInPlastBox(class_746 var1, class_238 var2) {
      for (class_1657 var4 : mc.field_1687.method_18456()) {
         if (!this.shouldIgnoreHitHighlightTarget(var1, var4) && this.canSeeTarget(var1, var4) && var4.method_5829().method_994(var2)) {
            return true;
         }
      }

      return false;
   }

   private boolean canSeeTarget(class_746 var1, class_1657 var2) {
      if (mc.field_1687 == null) {
         return false;
      }

      class_243 var3 = var1.method_33571();
      class_238 var4 = var2.method_5829();
      class_243[] var5 = new class_243[]{
         var2.method_33571(),
         var4.method_1005(),
         new class_243(var4.field_1323, var4.method_1005().field_1351, var4.field_1321),
         new class_243(var4.field_1320, var4.method_1005().field_1351, var4.field_1324)
      };

      for (class_243 var9 : var5) {
         class_3965 var10 = mc.field_1687.method_17742(new class_3959(var3, var9, class_3960.field_17558, class_242.field_1348, var1));
         if (var10.method_17783() == class_240.field_1333) {
            return true;
         }
      }

      return false;
   }

   private boolean shouldIgnoreHitHighlightTarget(class_746 var1, class_1657 var2) {
      return var2 == var1 || !var2.method_5805() || var2.method_7325() || var2.method_5767() || var2.method_5756(var1);
   }

   private void renderRadiusCircle(class_746 var1, double var2, int var4, int var5) {
      class_243 var6 = var1.method_30950(mc.method_61966().method_60637(true));
      double var7 = var6.field_1351 + var1.method_17682() - 1.4;
      class_243 var9 = new class_243(var6.field_1352, var7, var6.field_1350);
      this.renderRadiusCircle(var9, var2, var4, var5);
   }

   private void renderRadiusCircle(class_243 var1, double var2, int var4, int var5) {
      class_243 var6 = null;
      class_243 var7 = null;

      for (float var8 = 0.0F; var8 <= 360.0F; var8 += 5.0F) {
         double var9 = Math.toRadians(var8);
         class_243 var11 = new class_243(var1.field_1352 + Math.sin(var9) * var2, var1.field_1351, var1.field_1350 - Math.cos(var9) * var2);
         if (var7 != null) {
            Render3D.drawLine(var7, var11, var5, 3.0F, false);
            if (this.fillEnabled.isValue()) {
               Render3D.drawQuad(var1, var7, var11, var1, var4, false);
            }
         } else {
            var6 = var11;
         }

         var7 = var11;
      }

      if (var7 != null && var6 != null) {
         Render3D.drawLine(var7, var6, var5, 3.0F, false);
         if (this.fillEnabled.isValue()) {
            Render3D.drawQuad(var1, var7, var6, var1, var4, false);
         }
      }
   }

   private void renderCubeOutline(class_238 var1, int var2) {
      Render3D.drawBox(var1, var2, 3.0F, true, false, false);
   }

   private class_238 getTrapkaBox(class_746 var1) {
      class_243 var2 = var1.method_30950(mc.method_61966().method_60637(true));
      double var3 = Math.floor(var2.field_1352) + 0.5;
      double var5 = Math.floor(var2.field_1351) + 0.5 + 1.625;
      double var7 = Math.floor(var2.field_1350) + 0.5;
      float var9 = this.dragonSkin.isValue() ? 3.5F : 2.0F;
      return new class_238(var3 - var9, var5 - var9, var7 - var9, var3 + var9, var5 + var9, var7 + var9);
   }

   private class_238 getPlastBox(WorldRenderEvent var1, class_746 var2) {
      if (mc.field_1687 == null) {
         return new class_238(var2.method_30950(mc.method_61966().method_60637(true)), var2.method_30950(mc.method_61966().method_60637(true)));
      }

      if (this.dragonSkin.isValue()) {
         return this.getDragonPlastBox(var2, var1.getPartialTicks());
      }

      float var3 = 4.0F;
      float var4 = 4.0F;
      float var5 = 1.5F;
      float var6 = var3 / 2.0F;
      float var7 = var4 / 2.0F;
      float var8 = var5 / 2.0F;
      class_243 var9 = var2.method_5828(var1.getPartialTicks());
      class_243 var10 = this.getInterpolatedEyePos(var2, var1.getPartialTicks());
      class_2350 var11 = this.getDominantLookDirection(var9);
      double var12 = var8 + 0.01;
      class_243 var14 = var10.method_1019(var9.method_1021(4.0))
         .method_1031(var11.method_10148() * var12, var11.method_10164() * var12, var11.method_10165() * var12);

      class_238 var15 = switch (var11.method_10166()) {
         case field_11048 -> new class_238(
            var14.field_1352 - var8,
            var14.field_1351 - var7,
            var14.field_1350 - var6,
            var14.field_1352 + var8,
            var14.field_1351 + var7,
            var14.field_1350 + var6
         );
         case field_11052 -> new class_238(
            var14.field_1352 - var6,
            var14.field_1351 - var8,
            var14.field_1350 - var7,
            var14.field_1352 + var6,
            var14.field_1351 + var8,
            var14.field_1350 + var7
         );
         case field_11051 -> new class_238(
            var14.field_1352 - var6,
            var14.field_1351 - var7,
            var14.field_1350 - var8,
            var14.field_1352 + var6,
            var14.field_1351 + var7,
            var14.field_1350 + var8
         );
         default -> throw new MatchException(null, null);
      };

      return switch (var11.method_10166()) {
         case field_11048 -> new class_238(
            var15.field_1323, var15.field_1322 - 0.5, var15.field_1321 - 0.5, var15.field_1320, var15.field_1325 + 0.5, var15.field_1324 + 0.5
         );
         case field_11052 -> new class_238(
            var15.field_1323 - 0.5, var15.field_1322, var15.field_1321 - 0.5, var15.field_1320 + 0.5, var15.field_1325, var15.field_1324 + 0.5
         );
         case field_11051 -> new class_238(
            var15.field_1323 - 0.5, var15.field_1322 - 0.5, var15.field_1321, var15.field_1320 + 0.5, var15.field_1325 + 0.5, var15.field_1324
         );
         default -> throw new MatchException(null, null);
      };
   }

   private class_238 getDragonPlastBox(class_746 var1, float var2) {
      class_243 var3 = var1.method_5828(var2);
      class_243 var4 = this.getInterpolatedEyePos(var1, var2);
      class_2350 var5 = this.getDominantLookDirection(var3);
      double var6 = 1.01;
      class_243 var8 = var4.method_1019(var3.method_1021(4.0)).method_1031(var5.method_10148() * var6, var5.method_10164() * var6, var5.method_10165() * var6);

      return switch (var5.method_10166()) {
         case field_11048 -> new class_238(
            var8.field_1352 - 1.0, var8.field_1351 - 3.5, var8.field_1350 - 3.5, var8.field_1352 + 1.0, var8.field_1351 + 3.5, var8.field_1350 + 3.5
         );
         case field_11052 -> new class_238(
            var8.field_1352 - 3.5, var8.field_1351 - 1.0, var8.field_1350 - 3.5, var8.field_1352 + 3.5, var8.field_1351 + 1.0, var8.field_1350 + 3.5
         );
         case field_11051 -> new class_238(
            var8.field_1352 - 3.5, var8.field_1351 - 3.5, var8.field_1350 - 1.0, var8.field_1352 + 3.5, var8.field_1351 + 3.5, var8.field_1350 + 1.0
         );
         default -> throw new MatchException(null, null);
      };
   }

   private class_2350 getDominantLookDirection(class_243 var1) {
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
      return var1 << 24 | var0 & 16777215;
   }

   private class_243 getInterpolatedEyePos(class_746 var1, float var2) {
      class_243 var3 = var1.method_30950(var2);
      return new class_243(var3.field_1352, var3.field_1351 + var1.method_18381(var1.method_18376()), var3.field_1350);
   }

   private record SnowballPrediction() {
      private final List<class_243> trajectory;
      private final class_243 landingPos;

      private SnowballPrediction(List<class_243> var1, class_243 var2) {
         this.trajectory = var1;
         this.landingPos = var2;
      }
   }
}
