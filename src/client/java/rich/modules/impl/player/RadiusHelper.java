package rich.modules.impl.player;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
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
      if (mc.player != null && mc.world != null) {
         ClientPlayerEntity var2 = mc.player;
         ItemStack var3 = var2.getMainHandStack();
         ItemStack var4 = var2.getOffHandStack();
         int var5 = this.resolveActiveItemIndex(var3, var4);
         Vec3d var6 = var2.getLerpedPos(var1.getPartialTicks()).add(0.0, -1.4, 0.0);
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
            Box var12 = this.getTrapkaBox(var2);
            int var13 = this.trapkaColor.getColor();
            boolean var14 = this.hasPlayersInBox(var2, var12);
            boolean var15 = this.hitIndicator.isValue() && var14;
            int var16 = this.hitIndicator.isValue() ? this.hitColor.getColor() : var13;
            this.updateTransition(var1, 4, var15, var13, var16);
            this.renderCubeOutline(var12, this.currentOutlineColor);
         } else if (var5 != 5) {
            this.activeTransitionItem = -1;
         } else {
            Box var7 = this.getPlastBox(var1, var2);
            int var8 = this.plastColor.getColor();
            boolean var9 = this.hasPlayersInPlastBox(var2, var7);
            boolean var10 = this.hitIndicator.isValue() && var9;
            int var11 = this.hitIndicator.isValue() ? this.hitColor.getColor() : var8;
            this.updateTransition(var1, 5, var10, var8, var11);
            this.renderPlane(var7, this.currentFillColor, this.currentOutlineColor);
         }
      }
   }

   private int resolveActiveItemIndex(ItemStack var1, ItemStack var2) {
      int var3 = this.getEnabledItemIndex(var1);
      return var3 != -1 ? var3 : this.getEnabledItemIndex(var2);
   }

   private int getEnabledItemIndex(ItemStack var1) {
      if (var1 != null && !var1.isEmpty()) {
         Item var2 = var1.getItem();
         if (var2 == Items.ENDER_EYE) {
            return this.items.isSelected("Дезка") ? 0 : -1;
         } else if (var2 == Items.SUGAR) {
            return this.items.isSelected("Явка") ? 1 : -1;
         } else if (var2 == Items.FIRE_CHARGE) {
            return this.items.isSelected("Огненный Заряд") ? 2 : -1;
         } else if (var2 == Items.PHANTOM_MEMBRANE) {
            return this.items.isSelected("Божья Аура") ? 3 : -1;
         } else if (var2 == Items.NETHERITE_SCRAP) {
            return this.items.isSelected("Трапка") ? 4 : -1;
         } else if (var2 == Items.DRIED_KELP) {
            return this.items.isSelected("Пласт") ? 5 : -1;
         } else if (var2 == Items.SNOWBALL) {
            return this.items.isSelected("Снежок") ? 6 : -1;
         } else {
            return -1;
         }
      } else {
         return -1;
      }
   }

   private void renderRadiusPreset(WorldRenderEvent var1, ClientPlayerEntity var2, Vec3d var3, double var4, int var6, int var7) {
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

   private void renderSnowballPrediction(WorldRenderEvent var1, ClientPlayerEntity var2, int var3, int var4) {
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

         Vec3d var6 = var5.landingPos().add(0.0, 0.03, 0.0);
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

   private RadiusHelper.SnowballPrediction predictSnowballPrediction(ClientPlayerEntity var1, float var2) {
      if (mc.world == null) {
         return null;
      }

      ArrayList var3 = new ArrayList();
      Vec3d var4 = this.getInterpolatedEyePos(var1, var2);
      Vec3d var5 = var1.getRotationVec(var2).normalize().multiply(1.5);
      var3.add(var4);
      Vec3d var6 = null;

      label38:
      for (int var7 = 0; var7 < 160; var7++) {
         for (int var8 = 0; var8 < 6; var8++) {
            Vec3d var9 = var4;
            Vec3d var10 = var5.multiply(0.16666666666666666);
            var4 = var4.add(var10);
            BlockHitResult var11 = mc.world.raycast(new RaycastContext(var9, var4, net.minecraft.world.RaycastContext.ShapeType.COLLIDER, net.minecraft.world.RaycastContext.FluidHandling.ANY, var1));
            if (var11.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
               BlockHitResult var14 = var11;
               var6 = var14.getPos();
               var3.add(var6);
               break label38;
            }

            var3.add(var4);
            if (var4.y < mc.world.getBottomY() - 8.0) {
               var6 = var4;
               break label38;
            }

            double var12 = Math.pow(0.99, 0.16666666666666666);
            var5 = var5.subtract(0.0, 0.005, 0.0).multiply(var12);
         }
      }

      if (var3.size() < 2) {
         return null;
      }

      if (var6 == null) {
         var6 = (Vec3d)var3.get(var3.size() - 1);
      }

      return new RadiusHelper.SnowballPrediction(var3, var6);
   }

   private void renderTrajectory(List<Vec3d> var1, int var2) {
      for (int var3 = 1; var3 < var1.size(); var3++) {
         Render3D.drawLine((Vec3d)var1.get(var3 - 1), (Vec3d)var1.get(var3), var2, 2.25F, false);
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

   private void renderPlane(Box var1, int var2, int var3) {
      if (this.fillEnabled.isValue()) {
         Vec3d var4 = new Vec3d(var1.minX, var1.minY, var1.minZ);
         Vec3d var5 = new Vec3d(var1.maxX, var1.maxY, var1.maxZ);
         Render3D.drawQuad(
            new Vec3d(var4.x, var4.y, var4.z),
            new Vec3d(var5.x, var4.y, var4.z),
            new Vec3d(var5.x, var4.y, var5.z),
            new Vec3d(var4.x, var4.y, var5.z),
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

   private boolean hasPlayersInRadius(ClientPlayerEntity var1, Vec3d var2, double var3) {
      double var5 = var3 * var3;

      for (PlayerEntity var8 : mc.world.getPlayers()) {
         if (!this.shouldIgnoreHitHighlightTarget(var1, var8)
            && this.canSeeTarget(var1, var8)
            && var8.getLerpedPos(mc.getRenderTickCounter().getTickProgress(true)).squaredDistanceTo(var2) <= var5) {
            return true;
         }
      }

      return false;
   }

   private boolean hasPlayersInBox(ClientPlayerEntity var1, Box var2) {
      for (PlayerEntity var4 : mc.world.getPlayers()) {
         if (!this.shouldIgnoreHitHighlightTarget(var1, var4) && this.canSeeTarget(var1, var4) && var4.getBoundingBox().intersects(var2)) {
            return true;
         }
      }

      return false;
   }

   private boolean hasPlayersInPlastBox(ClientPlayerEntity var1, Box var2) {
      for (PlayerEntity var4 : mc.world.getPlayers()) {
         if (!this.shouldIgnoreHitHighlightTarget(var1, var4) && this.canSeeTarget(var1, var4) && var4.getBoundingBox().intersects(var2)) {
            return true;
         }
      }

      return false;
   }

   private boolean canSeeTarget(ClientPlayerEntity var1, PlayerEntity var2) {
      if (mc.world == null) {
         return false;
      }

      Vec3d var3 = var1.getEyePos();
      Box var4 = var2.getBoundingBox();
      Vec3d[] var5 = new Vec3d[]{
         var2.getEyePos(),
         var4.getCenter(),
         new Vec3d(var4.minX, var4.getCenter().y, var4.minZ),
         new Vec3d(var4.maxX, var4.getCenter().y, var4.maxZ)
      };

      for (Vec3d var9 : var5) {
         BlockHitResult var10 = mc.world.raycast(new RaycastContext(var3, var9, net.minecraft.world.RaycastContext.ShapeType.COLLIDER, net.minecraft.world.RaycastContext.FluidHandling.NONE, var1));
         if (var10.getType() == net.minecraft.util.hit.HitResult.Type.MISS) {
            return true;
         }
      }

      return false;
   }

   private boolean shouldIgnoreHitHighlightTarget(ClientPlayerEntity var1, PlayerEntity var2) {
      return var2 == var1 || !var2.isAlive() || var2.isSpectator() || var2.isInvisible() || var2.isInvisibleTo(var1);
   }

   private void renderRadiusCircle(ClientPlayerEntity var1, double var2, int var4, int var5) {
      Vec3d var6 = var1.getLerpedPos(mc.getRenderTickCounter().getTickProgress(true));
      double var7 = var6.y + var1.getHeight() - 1.4;
      Vec3d var9 = new Vec3d(var6.x, var7, var6.z);
      this.renderRadiusCircle(var9, var2, var4, var5);
   }

   private void renderRadiusCircle(Vec3d var1, double var2, int var4, int var5) {
      Vec3d var6 = null;
      Vec3d var7 = null;

      for (float var8 = 0.0F; var8 <= 360.0F; var8 += 5.0F) {
         double var9 = Math.toRadians(var8);
         Vec3d var11 = new Vec3d(var1.x + Math.sin(var9) * var2, var1.y, var1.z - Math.cos(var9) * var2);
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

   private void renderCubeOutline(Box var1, int var2) {
      Render3D.drawBox(var1, var2, 3.0F, true, false, false);
   }

   private Box getTrapkaBox(ClientPlayerEntity var1) {
      Vec3d var2 = var1.getLerpedPos(mc.getRenderTickCounter().getTickProgress(true));
      double var3 = Math.floor(var2.x) + 0.5;
      double var5 = Math.floor(var2.y) + 0.5 + 1.625;
      double var7 = Math.floor(var2.z) + 0.5;
      float var9 = this.dragonSkin.isValue() ? 3.5F : 2.0F;
      return new Box(var3 - var9, var5 - var9, var7 - var9, var3 + var9, var5 + var9, var7 + var9);
   }

   private Box getPlastBox(WorldRenderEvent var1, ClientPlayerEntity var2) {
      if (mc.world == null) {
         return new Box(var2.getLerpedPos(mc.getRenderTickCounter().getTickProgress(true)), var2.getLerpedPos(mc.getRenderTickCounter().getTickProgress(true)));
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
      Vec3d var9 = var2.getRotationVec(var1.getPartialTicks());
      Vec3d var10 = this.getInterpolatedEyePos(var2, var1.getPartialTicks());
      Direction var11 = this.getDominantLookDirection(var9);
      double var12 = var8 + 0.01;
      Vec3d var14 = var10.add(var9.multiply(4.0))
         .add(var11.getOffsetX() * var12, var11.getOffsetY() * var12, var11.getOffsetZ() * var12);

      Box var15 = switch (var11.getAxis()) {
         case X -> new Box(
            var14.x - var8,
            var14.y - var7,
            var14.z - var6,
            var14.x + var8,
            var14.y + var7,
            var14.z + var6
         );
         case Y -> new Box(
            var14.x - var6,
            var14.y - var8,
            var14.z - var7,
            var14.x + var6,
            var14.y + var8,
            var14.z + var7
         );
         case Z -> new Box(
            var14.x - var6,
            var14.y - var7,
            var14.z - var8,
            var14.x + var6,
            var14.y + var7,
            var14.z + var8
         );
         default -> throw new MatchException(null, null);
      };

      return switch (var11.getAxis()) {
         case X -> new Box(
            var15.minX, var15.minY - 0.5, var15.minZ - 0.5, var15.maxX, var15.maxY + 0.5, var15.maxZ + 0.5
         );
         case Y -> new Box(
            var15.minX - 0.5, var15.minY, var15.minZ - 0.5, var15.maxX + 0.5, var15.maxY, var15.maxZ + 0.5
         );
         case Z -> new Box(
            var15.minX - 0.5, var15.minY - 0.5, var15.minZ, var15.maxX + 0.5, var15.maxY + 0.5, var15.maxZ
         );
         default -> throw new MatchException(null, null);
      };
   }

   private Box getDragonPlastBox(ClientPlayerEntity var1, float var2) {
      Vec3d var3 = var1.getRotationVec(var2);
      Vec3d var4 = this.getInterpolatedEyePos(var1, var2);
      Direction var5 = this.getDominantLookDirection(var3);
      double var6 = 1.01;
      Vec3d var8 = var4.add(var3.multiply(4.0)).add(var5.getOffsetX() * var6, var5.getOffsetY() * var6, var5.getOffsetZ() * var6);

      return switch (var5.getAxis()) {
         case X -> new Box(
            var8.x - 1.0, var8.y - 3.5, var8.z - 3.5, var8.x + 1.0, var8.y + 3.5, var8.z + 3.5
         );
         case Y -> new Box(
            var8.x - 3.5, var8.y - 1.0, var8.z - 3.5, var8.x + 3.5, var8.y + 1.0, var8.z + 3.5
         );
         case Z -> new Box(
            var8.x - 3.5, var8.y - 3.5, var8.z - 1.0, var8.x + 3.5, var8.y + 3.5, var8.z + 1.0
         );
         default -> throw new MatchException(null, null);
      };
   }

   private Direction getDominantLookDirection(Vec3d var1) {
      double var2 = Math.abs(var1.x);
      double var4 = Math.abs(var1.y);
      double var6 = Math.abs(var1.z);
      if (var4 >= var2 && var4 >= var6) {
         return var1.y >= 0.0 ? Direction.UP : Direction.DOWN;
      } else if (var2 >= var6) {
         return var1.x >= 0.0 ? Direction.EAST : Direction.WEST;
      } else {
         return var1.z >= 0.0 ? Direction.SOUTH : Direction.NORTH;
      }
   }

   private static int withAlpha(int var0, int var1) {
      return var1 << 24 | var0 & 16777215;
   }

   private Vec3d getInterpolatedEyePos(ClientPlayerEntity var1, float var2) {
      Vec3d var3 = var1.getLerpedPos(var2);
      return new Vec3d(var3.x, var3.y + var1.getEyeHeight(var1.getPose()), var3.z);
   }

   private record SnowballPrediction(List<Vec3d> trajectory, Vec3d landingPos) {
   }
}
