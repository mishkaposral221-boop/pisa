package rich.util.move;

import java.util.Objects;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.input.Input;
import rich.IMinecraft;
import rich.modules.impl.combat.aura.AngleConnection;

public class MoveUtil implements IMinecraft {
   public static boolean hasPlayerMovement() {
      Input var0 = mc.player.input;
      if (var0.hasForwardMovement()) {
         return true;
      }

      Vec2f var1 = var0.getMovementInput();
      return var1.x != 0.0F || var1.y != 0.0F;
   }

   public static double getDistanceToGround() {
      for (double var0 = mc.player.getY(); var0 > 0.0; var0 -= 0.1) {
         if (!mc.world.getBlockState(mc.player.getBlockPos().down((int)(mc.player.getY() - var0 + 1.0))).isAir()) {
            return mc.player.getY() - var0;
         }
      }

      return 256.0;
   }

   public static double getDegreesRelativeToView(Vec3d var0, float var1) {
      float var2 = (float)Math.atan2(-var0.x, var0.z);
      double var3 = Math.toRadians(MathHelper.wrapDegrees(var1));
      return Math.toDegrees(MathHelper.wrapDegrees(var2 - var3));
   }

   public static void setVelocity(double var0) {
      double[] var2 = calculateDirection(var0);
      Objects.requireNonNull(mc.player).setVelocity(var2[0], mc.player.getVelocity().getY(), var2[1]);
   }

   public static double[] forward(double var0) {
      Vec2f var2 = mc.player.input.getMovementInput();
      float var3 = var2.y;
      float var4 = var2.x;
      float var5 = AngleConnection.INSTANCE.getRotation().getYaw();
      if (var3 != 0.0F) {
         if (var4 > 0.0F) {
            var5 += var3 > 0.0F ? -45 : 45;
         } else if (var4 < 0.0F) {
            var5 += var3 > 0.0F ? 45 : -45;
         }

         var4 = 0.0F;
         if (var3 > 0.0F) {
            var3 = 1.0F;
         } else if (var3 < 0.0F) {
            var3 = -1.0F;
         }
      }

      double var6 = Math.sin(Math.toRadians(var5 + 90.0F));
      double var8 = Math.cos(Math.toRadians(var5 + 90.0F));
      double var10 = var3 * var0 * var8 + var4 * var0 * var6;
      double var12 = var3 * var0 * var6 - var4 * var0 * var8;
      return new double[]{var10, var12};
   }

   public static double[] calculateDirection(double var0) {
      Vec2f var2 = mc.player.input.getMovementInput();
      float var3 = var2.y;
      float var4 = var2.x;
      return calculateDirection(var3, var4, var0);
   }

   public static double[] calculateDirection(float var0, float var1, double var2) {
      float var4 = AngleConnection.INSTANCE.getRotation().getYaw();
      if (var0 != 0.0F) {
         if (var1 > 0.0F) {
            var4 += var0 > 0.0F ? -45.0F : 45.0F;
         } else if (var1 < 0.0F) {
            var4 += var0 > 0.0F ? 45.0F : -45.0F;
         }

         var1 = 0.0F;
         var0 = var0 > 0.0F ? 1.0F : -1.0F;
      }

      double var5 = Math.sin(Math.toRadians(var4 + 90.0F));
      double var7 = Math.cos(Math.toRadians(var4 + 90.0F));
      double var9 = var0 * var2 * var7 + var1 * var2 * var5;
      double var11 = var0 * var2 * var5 - var1 * var2 * var7;
      return new double[]{var9, var11};
   }

   public static final boolean moveKeyPressed(int var0) {
      boolean var1 = mc.options.forwardKey.isPressed();
      boolean var2 = mc.options.leftKey.isPressed();
      boolean var3 = mc.options.backKey.isPressed();
      boolean var4 = mc.options.rightKey.isPressed();
      return var0 == 0 ? var1 : (var0 == 1 ? var2 : (var0 == 2 ? var3 : var0 == 3 && var4));
   }

   public static final boolean w() {
      return moveKeyPressed(0);
   }

   public static final boolean a() {
      return moveKeyPressed(1);
   }

   public static final boolean s() {
      return moveKeyPressed(2);
   }

   public static final boolean d() {
      return moveKeyPressed(3);
   }

   public static final float moveYaw(float var0) {
      return var0
         + (
            !a() || !d() || w() && s() || !w() && !s()
               ? (
                  !w() || !s() || a() && d() || !a() && !d()
                     ? (
                        (!a() || !d() || w() && s()) && (!w() || !s() || a() && d())
                           ? (
                              !a() && !d() && !s()
                                 ? 0
                                 : (w() && !s() ? 45 : (!s() || w() ? (!w() && !s() || w() && s() ? 90 : 0) : (!a() && !d() ? 180 : 135))) * (a() ? -1 : 1)
                           )
                           : 0
                     )
                     : (a() ? -90 : (d() ? 90 : 0))
               )
               : (w() ? 0 : (s() ? 180 : 0))
         );
   }

   public static float calculateBodyYaw(float var0, float var1, double var2, double var4, double var6, double var8, float var10) {
      double var11 = var6 - var2;
      double var13 = var8 - var4;
      float var15 = (float)(var11 * var11 + var13 * var13);
      float var16 = var1;
      float var17 = mc.player.handSwingProgress;
      if (var15 > 0.0025000002F) {
         float var18 = (float)MathHelper.atan2(var13, var11) * (180.0F / (float)Math.PI) - 90.0F;
         float var19 = MathHelper.abs(MathHelper.wrapDegrees(var0) - var18);
         if (95.0F < var19 && var19 < 265.0F) {
            var16 = var18 - 180.0F;
         } else {
            var16 = var18;
         }
      }

      if (mc.player != null && mc.player.handSwingProgress - 0.2F > 0.0F) {
         var16 = var0;
      }

      float var22 = MathHelper.wrapDegrees(var16 - var1);
      var16 = var1 + var22 * 0.3F;
      float var23 = MathHelper.wrapDegrees(var0 - var16);
      float var20 = 52.0F;
      if (Math.abs(var23) > var20) {
         var16 += var23 - MathHelper.sign(var23) * var20;
      }

      return var16;
   }

   public static PlayerInput getDirectionalInputForDegrees(PlayerInput var0, double var1, float var3) {
      boolean var4 = var0.forward();
      boolean var5 = var0.backward();
      boolean var6 = var0.left();
      boolean var7 = var0.right();
      if (var1 >= -90.0F + var3 && var1 <= 90.0F - var3) {
         var4 = true;
      } else if (var1 < -90.0F - var3 || var1 > 90.0F + var3) {
         var5 = true;
      }

      if (var1 >= 0.0F + var3 && var1 <= 180.0F - var3) {
         var7 = true;
      } else if (var1 >= -180.0F + var3 && var1 <= 0.0F - var3) {
         var6 = true;
      }

      return new PlayerInput(var4, var5, var6, var7, var0.jump(), var0.sneak(), var0.sprint());
   }

   public static PlayerInput getDirectionalInputForDegrees(PlayerInput var0, double var1) {
      return getDirectionalInputForDegrees(var0, var1, 20.0F);
   }
}
