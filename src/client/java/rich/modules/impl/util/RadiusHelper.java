package rich.modules.impl.util;

import java.util.ArrayList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
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
      if (mc.player != null && mc.world != null) {
         int var2 = this.resolveIndex(mc.player.getMainHandStack(), mc.player.getOffHandStack());
         Vec3d var3 = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ()).add(0.0, -1.4, 0.0);
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
               Box var5 = this.getTrapkaBox();
               this.updateTransition(var1, 4, this.hasPlayersInBox(var5), this.trapkaColor.getColor());
               if (this.fillEnabled.isValue()) {
                  this.drawFilledBox(var5, this.currentFillColor);
               }

               Render3D.drawBox(var5, this.currentOutlineColor, 3.0F, true, false, false);
               break;
            case 5:
               Box var4 = this.getPlastBox(var1);
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

   private int resolveIndex(ItemStack var1, ItemStack var2) {
      int var3 = this.itemIndex(var1);
      return var3 != -1 ? var3 : this.itemIndex(var2);
   }

   private int itemIndex(ItemStack var1) {
      if (var1 != null && !var1.isEmpty()) {
         Item var2 = var1.getItem();
         if (var2 == Items.ENDER_EYE) {
            return 0;
         } else if (var2 == Items.SUGAR) {
            return 1;
         } else if (var2 == Items.FIRE_CHARGE) {
            return 2;
         } else if (var2 == Items.PHANTOM_MEMBRANE) {
            return 3;
         } else if (var2 == Items.NETHERITE_SCRAP) {
            return 4;
         } else if (var2 == Items.DRIED_KELP) {
            return 5;
         } else {
            return var2 == Items.SNOWBALL ? 6 : -1;
         }
      } else {
         return -1;
      }
   }

   private void renderRadius(WorldRenderEvent var1, Vec3d var2, double var3, int var5, int var6) {
      this.initTransition(var5, var6);
      boolean var7 = this.hitIndicator.isValue() && this.hasPlayersInRadius(var2, var3);
      int var8 = this.hitIndicator.isValue() ? this.hitColor.getColor() : var6;
      this.updateColors(var7, withAlpha(var6, 85), withAlpha(var6, 255), withAlpha(var8, 85), withAlpha(var8, 255), var1.getPartialTicks());
      this.drawCircle(var2, var3);
   }

   private void drawCircle(Vec3d var1, double var2) {
      Vec3d var4 = null;
      Vec3d var5 = null;

      for (float var6 = 0.0F; var6 <= 360.0F; var6 += 5.0F) {
         double var7 = Math.toRadians(var6);
         Vec3d var9 = new Vec3d(var1.x + Math.sin(var7) * var2, var1.y, var1.z - Math.cos(var7) * var2);
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
      Vec3d var4 = this.eyePos(var2);
      Vec3d var5 = mc.player.getRotationVec(var2).normalize().multiply(1.5);
      var3.add(var4);
      Vec3d var6 = null;

      label57:
      for (int var7 = 0; var7 < 160; var7++) {
         for (int var8 = 0; var8 < 6; var8++) {
            Vec3d var9 = var4;
            var4 = var4.add(var5.multiply(0.16666666666666666));
            BlockHitResult var10 = mc.world.raycast(new RaycastContext(var9, var4, net.minecraft.world.RaycastContext.ShapeType.COLLIDER, net.minecraft.world.RaycastContext.FluidHandling.ANY, mc.player));
            if (var10.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
               var6 = var10.getPos();
               var3.add(var6);
               break label57;
            }

            var3.add(var4);
            if (var4.y < mc.world.getBottomY() - 8) {
               var6 = var4;
               break label57;
            }

            double var11 = Math.pow(0.99, 0.16666666666666666);
            var5 = var5.subtract(0.0, 0.005, 0.0).multiply(var11);
         }
      }

      if (var3.size() < 2) {
         this.activeTransitionItem = -1;
      } else {
         if (var6 == null) {
            var6 = (Vec3d)var3.get(var3.size() - 1);
         }

         int var13 = this.snowballColor.getColor();
         this.initTransition(6, var13);
         Vec3d var14 = var6.add(0.0, 0.03, 0.0);
         boolean var15 = this.hitIndicator.isValue() && this.hasPlayersInRadius(var14, 7.0);
         int var16 = this.hitIndicator.isValue() ? this.hitColor.getColor() : var13;
         this.updateColors(var15, withAlpha(var13, 85), withAlpha(var13, 255), withAlpha(var16, 85), withAlpha(var16, 255), var2);

         for (int var17 = 1; var17 < var3.size(); var17++) {
            Render3D.drawLine((Vec3d)var3.get(var17 - 1), (Vec3d)var3.get(var17), this.currentOutlineColor, 2.25F, false);
         }

         this.drawCircle(var14, 7.0);
      }
   }

   private Box getTrapkaBox() {
      Vec3d var1 = MathUtils.interpolate(mc.player);
      double var2 = Math.floor(var1.x) + 0.5;
      double var4 = Math.floor(var1.y) + 0.5 + 1.625;
      double var6 = Math.floor(var1.z) + 0.5;
      double var8 = this.dragonSkin.isValue() ? 3.5 : 2.0;
      return new Box(var2 - var8, var4 - var8, var6 - var8, var2 + var8, var4 + var8, var6 + var8);
   }

   private Box getPlastBox(WorldRenderEvent var1) {
      float var2 = var1.getPartialTicks();
      if (this.dragonSkin.isValue()) {
         return this.getDragonPlastBox(var2);
      }

      float var3 = 2.0F;
      float var4 = 2.0F;
      float var5 = 0.75F;
      Vec3d var6 = mc.player.getRotationVec(var2);
      Vec3d var7 = this.eyePos(var2);
      Direction var8 = this.dominantDir(var6);
      double var9 = var5 + 0.01;
      Vec3d var11 = var7.add(var6.multiply(4.0)).add(var8.getOffsetX() * var9, var8.getOffsetY() * var9, var8.getOffsetZ() * var9);

      Box var12 = switch (var8.getAxis()) {
         case X -> new Box(
            var11.x - var5,
            var11.y - var4,
            var11.z - var3,
            var11.x + var5,
            var11.y + var4,
            var11.z + var3
         );
         case Y -> new Box(
            var11.x - var3,
            var11.y - var5,
            var11.z - var4,
            var11.x + var3,
            var11.y + var5,
            var11.z + var4
         );
         case Z -> new Box(
            var11.x - var3,
            var11.y - var4,
            var11.z - var5,
            var11.x + var3,
            var11.y + var4,
            var11.z + var5
         );
         default -> throw new MatchException(null, null);
      };

      return switch (var8.getAxis()) {
         case X -> new Box(
            var12.minX, var12.minY - 0.5, var12.minZ - 0.5, var12.maxX, var12.maxY + 0.5, var12.maxZ + 0.5
         );
         case Y -> new Box(
            var12.minX - 0.5, var12.minY, var12.minZ - 0.5, var12.maxX + 0.5, var12.maxY, var12.maxZ + 0.5
         );
         case Z -> new Box(
            var12.minX - 0.5, var12.minY - 0.5, var12.minZ, var12.maxX + 0.5, var12.maxY + 0.5, var12.maxZ
         );
         default -> throw new MatchException(null, null);
      };
   }

   private Box getDragonPlastBox(float var1) {
      Vec3d var2 = mc.player.getRotationVec(var1);
      Vec3d var3 = this.eyePos(var1);
      Direction var4 = this.dominantDir(var2);
      double var5 = 1.01;
      Vec3d var7 = var3.add(var2.multiply(4.0)).add(var4.getOffsetX() * var5, var4.getOffsetY() * var5, var4.getOffsetZ() * var5);

      return switch (var4.getAxis()) {
         case X -> new Box(
            var7.x - 1.0, var7.y - 3.5, var7.z - 3.5, var7.x + 1.0, var7.y + 3.5, var7.z + 3.5
         );
         case Y -> new Box(
            var7.x - 3.5, var7.y - 1.0, var7.z - 3.5, var7.x + 3.5, var7.y + 1.0, var7.z + 3.5
         );
         case Z -> new Box(
            var7.x - 3.5, var7.y - 3.5, var7.z - 1.0, var7.x + 3.5, var7.y + 3.5, var7.z + 1.0
         );
         default -> throw new MatchException(null, null);
      };
   }

   private void renderPlane(Box var1) {
      if (this.fillEnabled.isValue()) {
         this.drawFilledBox(var1, this.currentFillColor);
      }

      Render3D.drawBox(var1, this.currentOutlineColor, 3.0F, true, false, false);
   }

   private boolean hasPlayersInRadius(Vec3d var1, double var2) {
      double var4 = var2 * var2;

      for (PlayerEntity var7 : mc.world.getPlayers()) {
         if (!this.shouldSkip(var7)
            && this.canSee(var7)
            && new Vec3d(var7.getX(), var7.getY(), var7.getZ()).squaredDistanceTo(var1) <= var4) {
            return true;
         }
      }

      return false;
   }

   private boolean hasPlayersInBox(Box var1) {
      for (PlayerEntity var3 : mc.world.getPlayers()) {
         if (!this.shouldSkip(var3) && this.canSee(var3) && var3.getBoundingBox().intersects(var1)) {
            return true;
         }
      }

      return false;
   }

   private boolean shouldSkip(PlayerEntity var1) {
      return var1 == mc.player || !var1.isAlive() || var1.isSpectator() || var1.isInvisible() || var1.isInvisibleTo(mc.player);
   }

   private boolean canSee(PlayerEntity var1) {
      Vec3d var2 = mc.player.getEyePos();
      Box var3 = var1.getBoundingBox();
      Vec3d[] var4 = new Vec3d[]{
         var1.getEyePos(),
         var3.getCenter(),
         new Vec3d(var3.minX, var3.getCenter().y, var3.minZ),
         new Vec3d(var3.maxX, var3.getCenter().y, var3.maxZ)
      };

      for (Vec3d var8 : var4) {
         BlockHitResult var9 = mc.world.raycast(new RaycastContext(var2, var8, net.minecraft.world.RaycastContext.ShapeType.COLLIDER, net.minecraft.world.RaycastContext.FluidHandling.NONE, mc.player));
         if (var9.getType() == net.minecraft.util.hit.HitResult.Type.MISS) {
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

   private void drawFilledBox(Box var1, int var2) {
      Render3D.drawQuad(
         new Vec3d(var1.minX, var1.minY, var1.minZ),
         new Vec3d(var1.maxX, var1.minY, var1.minZ),
         new Vec3d(var1.maxX, var1.minY, var1.maxZ),
         new Vec3d(var1.minX, var1.minY, var1.maxZ),
         var2,
         false
      );
      Render3D.drawQuad(
         new Vec3d(var1.minX, var1.minY, var1.minZ),
         new Vec3d(var1.minX, var1.maxY, var1.minZ),
         new Vec3d(var1.maxX, var1.maxY, var1.minZ),
         new Vec3d(var1.maxX, var1.minY, var1.minZ),
         var2,
         false
      );
      Render3D.drawQuad(
         new Vec3d(var1.maxX, var1.minY, var1.minZ),
         new Vec3d(var1.maxX, var1.maxY, var1.minZ),
         new Vec3d(var1.maxX, var1.maxY, var1.maxZ),
         new Vec3d(var1.maxX, var1.minY, var1.maxZ),
         var2,
         false
      );
      Render3D.drawQuad(
         new Vec3d(var1.minX, var1.minY, var1.maxZ),
         new Vec3d(var1.maxX, var1.minY, var1.maxZ),
         new Vec3d(var1.maxX, var1.maxY, var1.maxZ),
         new Vec3d(var1.minX, var1.maxY, var1.maxZ),
         var2,
         false
      );
      Render3D.drawQuad(
         new Vec3d(var1.minX, var1.minY, var1.minZ),
         new Vec3d(var1.minX, var1.minY, var1.maxZ),
         new Vec3d(var1.minX, var1.maxY, var1.maxZ),
         new Vec3d(var1.minX, var1.maxY, var1.minZ),
         var2,
         false
      );
      Render3D.drawQuad(
         new Vec3d(var1.minX, var1.maxY, var1.minZ),
         new Vec3d(var1.minX, var1.maxY, var1.maxZ),
         new Vec3d(var1.maxX, var1.maxY, var1.maxZ),
         new Vec3d(var1.maxX, var1.maxY, var1.minZ),
         var2,
         false
      );
   }

   private Vec3d eyePos(float var1) {
      double var2 = mc.player.lastX + (mc.player.getX() - mc.player.lastX) * var1;
      double var4 = mc.player.lastY
         + (mc.player.getY() - mc.player.lastY) * var1
         + mc.player.getEyeHeight(mc.player.getPose());
      double var6 = mc.player.lastZ + (mc.player.getZ() - mc.player.lastZ) * var1;
      return new Vec3d(var2, var4, var6);
   }

   private Direction dominantDir(Vec3d var1) {
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
      return MathHelper.clamp(var1, 0, 255) << 24 | var0 & 16777215;
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
