package rich.util.player;

import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import net.minecraft.class_10185;
import net.minecraft.class_10255;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1320;
import net.minecraft.class_1657;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_2399;
import net.minecraft.class_243;
import net.minecraft.class_2533;
import net.minecraft.class_2680;
import net.minecraft.class_3481;
import net.minecraft.class_3486;
import net.minecraft.class_3532;
import net.minecraft.class_3610;
import net.minecraft.class_3611;
import net.minecraft.class_5134;
import net.minecraft.class_5635;
import net.minecraft.class_6862;
import net.minecraft.class_6880;
import net.minecraft.class_744;
import net.minecraft.class_746;
import net.minecraft.class_2338.class_2339;
import rich.IMinecraft;
import rich.util.move.MoveUtil;

public class PlayerSimulation implements IMinecraft, Simulation {
   public final class_1657 player;
   public final PlayerSimulation.SimulatedPlayerInput input;
   public class_243 pos;
   public class_243 velocity;
   public class_238 boundingBox;
   public float yaw;
   public float pitch;
   public boolean sprinting;
   public float fallDistance;
   public int jumpingCooldown;
   public boolean isJumping;
   public boolean isFallFlying;
   public boolean onGround;
   public boolean horizontalCollision;
   public boolean verticalCollision;
   public boolean touchingWater;
   public boolean isSwimming;
   public boolean submergedInWater;
   private final Object2DoubleMap<class_6862<class_3611>> fluidHeight;
   private final HashSet<class_6862<class_3611>> submergedFluidTag;
   private int simulatedTicks = 0;
   private boolean clipLedged = false;
   private static final double STEP_HEIGHT = 0.5;

   public PlayerSimulation(
      class_1657 var1,
      PlayerSimulation.SimulatedPlayerInput var2,
      class_243 var3,
      class_243 var4,
      class_238 var5,
      float var6,
      float var7,
      boolean var8,
      float var9,
      int var10,
      boolean var11,
      boolean var12,
      boolean var13,
      boolean var14,
      boolean var15,
      boolean var16,
      boolean var17,
      boolean var18,
      Object2DoubleMap<class_6862<class_3611>> var19,
      HashSet<class_6862<class_3611>> var20
   ) {
      this.player = var1;
      this.input = var2;
      this.pos = var3;
      this.velocity = var4;
      this.boundingBox = var5;
      this.yaw = var6;
      this.pitch = var7;
      this.sprinting = var8;
      this.fallDistance = var9;
      this.jumpingCooldown = var10;
      this.isJumping = var11;
      this.isFallFlying = var12;
      this.onGround = var13;
      this.horizontalCollision = var14;
      this.verticalCollision = var15;
      this.touchingWater = var16;
      this.isSwimming = var17;
      this.submergedInWater = var18;
      this.fluidHeight = var19;
      this.submergedFluidTag = var20;
   }

   public static PlayerSimulation simulateLocalPlayer(int var0) {
      PlayerSimulation var1 = fromClientPlayer(PlayerSimulation.SimulatedPlayerInput.fromClientPlayer(mc.field_1724.field_3913.field_54155));

      for (int var2 = 0; var2 < var0; var2++) {
         var1.tick();
      }

      return var1;
   }

   public static PlayerSimulation simulateOtherPlayer(class_1657 var0, int var1) {
      PlayerSimulation var2 = fromOtherPlayer(var0, PlayerSimulation.SimulatedPlayerInput.guessInput(var0));

      for (int var3 = 0; var3 < var1; var3++) {
         var2.tick();
      }

      return var2;
   }

   public static PlayerSimulation fromClientPlayer(PlayerSimulation.SimulatedPlayerInput var0) {
      class_746 var1 = mc.field_1724;
      return new PlayerSimulation(
         var1,
         var0,
         var1.method_73189(),
         var1.method_18798(),
         var1.method_5829(),
         var1.method_36454(),
         var1.method_36455(),
         var1.method_5624(),
         (float)var1.field_6017,
         var1.field_6228,
         var1.field_6282,
         var1.method_6128(),
         var1.method_24828(),
         var1.field_5976,
         var1.field_5992,
         var1.method_5799(),
         var1.method_5681(),
         var1.method_5869(),
         new Object2DoubleArrayMap(var1.field_5964),
         new HashSet<>(var1.field_25599)
      );
   }

   public static PlayerSimulation fromOtherPlayer(class_1657 var0, PlayerSimulation.SimulatedPlayerInput var1) {
      return new PlayerSimulation(
         var0,
         var1,
         var0.method_73189(),
         var0.method_73189().method_1020(new class_243(var0.field_6014, var0.field_6036, var0.field_5969)),
         var0.method_5829(),
         var0.method_36454(),
         var0.method_36455(),
         var0.method_5624(),
         (float)var0.field_6017,
         var0.field_6228,
         var0.field_6282,
         var0.method_6128(),
         var0.method_24828(),
         var0.field_5976,
         var0.field_5992,
         var0.method_5799(),
         var0.method_5681(),
         var0.method_5869(),
         new Object2DoubleArrayMap(var0.field_5964),
         new HashSet<>(var0.field_25599)
      );
   }

   @Override
   public class_243 pos() {
      return this.player.method_73189();
   }

   @Override
   public void tick() {
      this.simulatedTicks++;
      this.clipLedged = false;
      if (!(this.pos.field_1351 <= -70.0)) {
         this.input.update();
         this.checkWaterState();
         this.updateSubmergedInWaterState();
         this.updateSwimming();
         if (this.jumpingCooldown > 0) {
            this.jumpingCooldown--;
         }

         this.isJumping = this.input.playerInput.comp_3163();
         double var1 = this.velocity.field_1352;
         double var3 = this.velocity.field_1351;
         double var5 = this.velocity.field_1350;
         if (Math.abs(this.velocity.field_1352) < 0.003) {
            var1 = 0.0;
         }

         if (Math.abs(this.velocity.field_1351) < 0.003) {
            var3 = 0.0;
         }

         if (Math.abs(this.velocity.field_1350) < 0.003) {
            var5 = 0.0;
         }

         if (this.onGround) {
            this.isFallFlying = false;
         }

         this.velocity = new class_243(var1, var3, var5);
         if (this.isJumping) {
            double var7 = this.isInLava() ? this.getFluidHeight(class_3486.field_15518) : this.getFluidHeight(class_3486.field_15517);
            boolean var9 = this.isTouchingWater() && var7 > 0.0;
            double var10 = this.getSwimHeight();
            if (!var9 || this.onGround && !(var7 > var10)) {
               if (!this.isInLava() || this.onGround && !(var7 > var10)) {
                  if ((this.onGround || var9 && var7 <= var10) && this.jumpingCooldown == 0) {
                     this.jump();
                     if (this.player.equals(mc.field_1724)) {
                        this.jumpingCooldown = 10;
                     }
                  }
               } else {
                  this.swimUpward(class_3486.field_15518);
               }
            } else {
               this.swimUpward(class_3486.field_15517);
            }
         }

         float var12 = this.input.movementSideways * 0.98F;
         float var8 = this.input.movementForward * 0.98F;
         float var13 = 0.0F;
         if (this.hasStatusEffect(class_1294.field_5906) || this.hasStatusEffect(class_1294.field_5902)) {
            this.onLanding();
         }

         this.travel(new class_243(var12, var13, var8));
      }
   }

   private void travel(class_243 var1) {
      if (this.isSwimming && !this.player.method_5765()) {
         double var2 = this.getRotationVector().field_1351;
         double var4 = var2 < -0.2 ? 0.085 : 0.06;
         class_2338 var6 = new class_2338(
            class_3532.method_15357(this.pos.field_1352),
            class_3532.method_15357(this.pos.field_1351 + 1.0 - 0.1),
            class_3532.method_15357(this.pos.field_1350)
         );
         if (var2 <= 0.0 || this.input.playerInput.comp_3163() || !this.player.method_73183().method_8320(var6).method_26227().method_15769()) {
            this.velocity = this.velocity.method_1031(0.0, (var2 - this.velocity.field_1351) * var4, 0.0);
         }
      }

      double var19 = this.velocity.field_1351;
      double var20 = 0.08;
      boolean var21 = this.velocity.field_1351 <= 0.0;
      if (this.velocity.field_1351 <= 0.0 && this.hasStatusEffect(class_1294.field_5906)) {
         var20 = 0.01;
         this.onLanding();
      }

      if (this.isTouchingWater() && this.player.method_29920()) {
         double var25 = this.pos.field_1351;
         float var28 = this.isSprinting() ? 0.9F : 0.8F;
         float var30 = 0.02F;
         float var32 = (float)this.getAttributeValue(class_5134.field_51578);
         if (!this.onGround) {
            var32 *= 0.5F;
         }

         if (var32 > 0.0F) {
            var28 += (0.54600006F - var28) * var32 / 3.0F;
            var30 += (this.getMovementSpeed() - var30) * var32 / 3.0F;
         }

         if (this.hasStatusEffect(class_1294.field_5900)) {
            var28 = 0.96F;
         }

         this.updateVelocity(var30, var1);
         this.move(this.velocity);
         class_243 var33 = this.velocity;
         if (this.horizontalCollision && this.isClimbing()) {
            var33 = new class_243(var33.field_1352, 0.2, var33.field_1350);
         }

         this.velocity = var33.method_18805(var28, 0.8, var28);
         class_243 var34 = this.player.method_26317(var20, var21, this.velocity);
         this.velocity = var34;
         if (this.horizontalCollision && this.doesNotCollide(var34.field_1352, var34.field_1351 + 0.6 - this.pos.field_1351 + var25, var34.field_1350)) {
            this.velocity = new class_243(var34.field_1352, 0.3, var34.field_1350);
         }
      } else if (this.isInLava() && this.player.method_29920()) {
         double var24 = this.pos.field_1351;
         this.updateVelocity(0.02F, var1);
         this.move(this.velocity);
         if (this.getFluidHeight(class_3486.field_15518) <= this.getSwimHeight()) {
            this.velocity = this.velocity.method_18805(0.5, 0.8, 0.5);
            this.velocity = this.player.method_26317(var20, var21, this.velocity);
         } else {
            this.velocity = this.velocity.method_1021(0.5);
         }

         if (!this.player.method_5740()) {
            this.velocity = this.velocity.method_1031(0.0, -var20 / 4.0, 0.0);
         }

         if (this.horizontalCollision
            && this.doesNotCollide(this.velocity.field_1352, this.velocity.field_1351 + 0.6 - this.pos.field_1351 + var24, this.velocity.field_1350)) {
            this.velocity = new class_243(this.velocity.field_1352, 0.3, this.velocity.field_1350);
         }
      } else if (this.isFallFlying) {
         class_243 var9 = this.velocity;
         if (var9.field_1351 > -0.5) {
            this.fallDistance = 1.0F;
         }

         class_243 var10 = this.getRotationVector();
         float var11 = this.pitch * (float) (Math.PI / 180.0);
         double var12 = Math.sqrt(var10.field_1352 * var10.field_1352 + var10.field_1350 * var10.field_1350);
         double var14 = this.velocity.method_37267();
         double var16 = var10.method_1033();
         float var18 = class_3532.method_15362(var11);
         var18 = (float)(var18 * (var18 * Math.min(1.0, var16 / 0.4)));
         var9 = this.velocity.method_1031(0.0, var20 * (-1.0 + var18 * 0.75), 0.0);
         if (var9.field_1351 < 0.0 && var12 > 0.0) {
            double var7 = var9.field_1351 * -0.1 * var18;
            var9 = var9.method_1031(var10.field_1352 * var7 / var12, var7, var10.field_1350 * var7 / var12);
         }

         if (var11 < 0.0F && var12 > 0.0) {
            double var22 = var14 * -class_3532.method_15374(var11) * 0.04;
            var9 = var9.method_1031(-var10.field_1352 * var22 / var12, var22 * 3.2, -var10.field_1350 * var22 / var12);
         }

         if (var12 > 0.0) {
            var9 = var9.method_1031((var10.field_1352 / var12 * var14 - var9.field_1352) * 0.1, 0.0, (var10.field_1350 / var12 * var14 - var9.field_1350) * 0.1);
         }

         this.velocity = var9.method_18805(0.99, 0.98, 0.99);
         this.move(this.velocity);
      } else {
         class_2338 var23 = this.getVelocityAffectingPos();
         float var8 = this.player.method_73183().method_8320(var23).method_26204().method_9499();
         float var27 = this.onGround ? var8 * 0.91F : 0.91F;
         class_243 var29 = this.applyMovementInput(var1, var8);
         double var31 = var29.field_1351;
         if (this.hasStatusEffect(class_1294.field_5902)) {
            class_1293 var13 = this.getStatusEffect(class_1294.field_5902);
            if (var13 != null) {
               var31 += (0.05 * (var13.method_5578() + 1) - var29.field_1351) * 0.2;
            }
         } else if (this.player.method_73183().method_8608() && !this.player.method_73183().method_22340(var23)) {
            var31 = this.pos.field_1351 > this.player.method_73183().method_31607() ? -0.1 : 0.0;
         } else if (!this.player.method_5740()) {
            var31 -= var20;
         }

         if (this.player.method_35053()) {
            this.velocity = new class_243(var29.field_1352, var31, var29.field_1350);
         } else {
            this.velocity = new class_243(var29.field_1352 * var27, var31 * 0.98F, var29.field_1350 * var27);
         }
      }

      if (this.player.method_31549().field_7479 && !this.player.method_5765()) {
         this.velocity = new class_243(this.velocity.field_1352, var19 * 0.6, this.velocity.field_1350);
         this.onLanding();
      }
   }

   private class_243 applyMovementInput(class_243 var1, float var2) {
      this.updateVelocity(this.getMovementSpeed(var2), var1);
      this.velocity = this.applyClimbingSpeed(this.velocity);
      this.move(this.velocity);
      class_243 var3 = this.velocity;
      class_2338 var4 = this.posToBlockPos(this.pos);
      class_2680 var5 = this.getState(var4);
      if ((this.horizontalCollision || this.isJumping)
         && (this.isClimbing() || var5 != null && var5.method_27852(class_2246.field_27879) && class_5635.method_32355(this.player))) {
         var3 = new class_243(var3.field_1352, 0.2, var3.field_1350);
      }

      return var3;
   }

   private void updateVelocity(float var1, class_243 var2) {
      class_243 var3 = class_1297.method_18795(var2, var1, this.yaw);
      this.velocity = this.velocity.method_1019(var3);
   }

   private float getMovementSpeed(float var1) {
      return this.onGround ? this.getMovementSpeed() * (0.21600002F / (var1 * var1 * var1)) : this.getAirStrafingSpeed();
   }

   private float getAirStrafingSpeed() {
      float var1 = 0.02F;
      return this.input.playerInput.comp_3165() ? var1 + 0.006F : var1;
   }

   private float getMovementSpeed() {
      return 0.1F;
   }

   private void move(class_243 var1) {
      class_243 var2 = var1;
      var2 = this.adjustMovementForSneaking(var2);
      class_243 var3 = this.adjustMovementForCollisions(var2);
      if (var3.method_1027() > 1.0E-7) {
         this.pos = this.pos.method_1019(var3);
         this.boundingBox = this.player.method_18377(this.player.method_18376()).method_30757(this.pos);
      }

      boolean var4 = !class_3532.method_20390(var1.field_1352, var3.field_1352);
      boolean var5 = !class_3532.method_20390(var1.field_1350, var3.field_1350);
      this.horizontalCollision = var4 || var5;
      this.verticalCollision = var1.field_1351 != var3.field_1351;
      this.onGround = this.verticalCollision && var1.field_1351 < 0.0;
      if (!this.isTouchingWater()) {
         this.checkWaterState();
      }

      if (this.onGround) {
         this.onLanding();
      } else if (var1.field_1351 < 0.0) {
         this.fallDistance = this.fallDistance - (float)var1.field_1351;
      }

      class_243 var6 = this.velocity;
      if (this.horizontalCollision || this.verticalCollision) {
         this.velocity = new class_243(var4 ? 0.0 : var6.field_1352, this.onGround ? 0.0 : var6.field_1351, var5 ? 0.0 : var6.field_1350);
      }
   }

   private class_243 adjustMovementForCollisions(class_243 var1) {
      class_238 var2 = new class_238(-0.3, 0.0, -0.3, 0.3, 1.8, 0.3).method_997(this.pos);
      List var3 = Collections.emptyList();
      class_243 var4;
      if (var1.method_1027() == 0.0) {
         var4 = var1;
      } else {
         var4 = class_1297.method_20736(this.player, var1, var2, this.player.method_73183(), var3);
      }

      boolean var5 = var1.field_1352 != var4.field_1352;
      boolean var6 = var1.field_1351 != var4.field_1351;
      boolean var7 = var1.field_1350 != var4.field_1350;
      boolean var8 = this.onGround || var6 && var1.field_1351 < 0.0;
      if (this.player.method_49476() > 0.0F && var8 && (var5 || var7)) {
         class_243 var9 = class_1297.method_20736(
            this.player, new class_243(var1.field_1352, this.player.method_49476(), var1.field_1350), var2, this.player.method_73183(), var3
         );
         class_243 var10 = class_1297.method_20736(
            this.player,
            new class_243(0.0, this.player.method_49476(), 0.0),
            var2.method_1012(var1.field_1352, 0.0, var1.field_1350),
            this.player.method_73183(),
            var3
         );
         class_243 var11 = class_1297.method_20736(
               this.player, new class_243(var1.field_1352, 0.0, var1.field_1350), var2.method_997(var10), this.player.method_73183(), var3
            )
            .method_1019(var10);
         if (var10.field_1351 < this.player.method_49476() && var11.method_37268() > var9.method_37268()) {
            var9 = var11;
         }

         if (var9.method_37268() > var4.method_37268()) {
            return var9.method_1019(
               class_1297.method_20736(
                  this.player, new class_243(0.0, -var9.field_1351 + var1.field_1351, 0.0), var2.method_997(var9), this.player.method_73183(), var3
               )
            );
         }
      }

      return var4;
   }

   private void onLanding() {
      this.fallDistance = 0.0F;
   }

   public void jump() {
      this.velocity = this.velocity.method_1031(0.0, this.getJumpVelocity() - this.velocity.field_1351, 0.0);
      if (this.isSprinting()) {
         float var1 = (float)Math.toRadians(this.yaw);
         this.velocity = this.velocity.method_1031(-class_3532.method_15374(var1) * 0.2, 0.0, class_3532.method_15362(var1) * 0.2);
      }
   }

   private class_243 applyClimbingSpeed(class_243 var1) {
      if (!this.isClimbing()) {
         return var1;
      }

      this.onLanding();
      double var2 = class_3532.method_15350(var1.field_1352, -0.15F, 0.15F);
      double var4 = class_3532.method_15350(var1.field_1350, -0.15F, 0.15F);
      double var6 = Math.max(var1.field_1351, -0.15F);
      if (var6 < 0.0 && !this.getState(this.posToBlockPos(this.pos)).method_27852(class_2246.field_16492) && this.player.method_21754()) {
         var6 = 0.0;
      }

      return new class_243(var2, var6, var4);
   }

   public boolean isClimbing() {
      class_2338 var1 = this.posToBlockPos(this.pos);
      class_2680 var2 = this.getState(var1);
      return var2.method_26164(class_3481.field_22414) ? true : var2.method_26204() instanceof class_2533 && this.canEnterTrapdoor(var1, var2);
   }

   private boolean canEnterTrapdoor(class_2338 var1, class_2680 var2) {
      if (!(Boolean)var2.method_11654(class_2533.field_11631)) {
         return false;
      }

      class_2680 var3 = this.player.method_73183().method_8320(var1.method_10074());
      return var3.method_27852(class_2246.field_9983)
         && ((class_2350)var3.method_11654(class_2399.field_11253)).equals(var2.method_11654(class_2533.field_11177));
   }

   private class_243 adjustMovementForSneaking(class_243 var1) {
      if (var1.field_1351 <= 0.0 && this.method_30263()) {
         double var2 = var1.field_1352;
         double var4 = var1.field_1350;

         double var6;
         for (var6 = 0.05;
            var2 != 0.0 && this.player.method_73183().method_8587(this.player, this.boundingBox.method_989(var2, -0.5, 0.0));
            var2 += var2 > 0.0 ? -var6 : var6
         ) {
            if (var2 < var6 && var2 >= -var6) {
               var2 = 0.0;
               break;
            }
         }

         while (var4 != 0.0 && this.player.method_73183().method_8587(this.player, this.boundingBox.method_989(0.0, -0.5, var4))) {
            if (var4 < var6 && var4 >= -var6) {
               var4 = 0.0;
               break;
            }

            var4 += var4 > 0.0 ? -var6 : var6;
         }

         while (var2 != 0.0 && var4 != 0.0 && this.player.method_73183().method_8587(this.player, this.boundingBox.method_989(var2, -0.5, var4))) {
            var2 = var2 < var6 && var2 >= -var6 ? 0.0 : (var2 > 0.0 ? var2 - var6 : var2 + var6);
            if (var4 < var6 && var4 >= -var6) {
               var4 = 0.0;
               break;
            }

            var4 += var4 > 0.0 ? -var6 : var6;
         }

         if (var1.field_1352 != var2 || var1.field_1350 != var4) {
            this.clipLedged = true;
         }

         if (this.shouldClipAtLedge()) {
            var1 = new class_243(var2, var1.field_1351, var4);
         }
      }

      return var1;
   }

   protected boolean shouldClipAtLedge() {
      return this.input.playerInput.comp_3164() || this.input.forceSafeWalk;
   }

   private boolean method_30263() {
      return this.onGround
         || this.fallDistance < 0.5 && !this.player.method_73183().method_8587(this.player, this.boundingBox.method_989(0.0, this.fallDistance - 0.5, 0.0));
   }

   private boolean isSprinting() {
      return this.sprinting;
   }

   private float getJumpVelocity() {
      return 0.42F * this.getJumpVelocityMultiplier() + this.getJumpBoostVelocityModifier();
   }

   private float getJumpBoostVelocityModifier() {
      if (this.hasStatusEffect(class_1294.field_5913)) {
         class_1293 var1 = this.getStatusEffect(class_1294.field_5913);
         return 0.1F * (var1.method_5578() + 1);
      } else {
         return 0.0F;
      }
   }

   private float getJumpVelocityMultiplier() {
      float var1 = 0.0F;
      class_2248 var2 = this.getState(this.posToBlockPos(this.pos)).method_26204();
      if (var2 != null) {
         var1 = var2.method_23350();
      }

      float var3 = 0.0F;
      class_2248 var4 = this.getState(this.getVelocityAffectingPos()).method_26204();
      if (var4 != null) {
         var3 = var4.method_23350();
      }

      return var1 == 1.0F ? var3 : var1;
   }

   private boolean doesNotCollide(double var1, double var3, double var5) {
      return this.doesNotCollide(this.boundingBox.method_989(var1, var3, var5));
   }

   private boolean doesNotCollide(class_238 var1) {
      return this.player.method_73183().method_8587(this.player, var1) && !this.player.method_73183().method_22345(var1);
   }

   private void swimUpward(class_6862<class_3611> var1) {
      this.velocity = this.velocity.method_1031(0.0, 0.04F, 0.0);
   }

   private class_2338 getVelocityAffectingPos() {
      return class_2338.method_49637(this.pos.field_1352, this.boundingBox.field_1322 - 0.5000001, this.pos.field_1350);
   }

   private double getSwimHeight() {
      return this.player.method_5751() < 0.4 ? 0.0 : 0.4;
   }

   private boolean isTouchingWater() {
      return this.touchingWater;
   }

   public boolean isInLava() {
      return this.fluidHeight.getDouble(class_3486.field_15518) > 0.0;
   }

   private void checkWaterState() {
      if (this.player.method_5854() instanceof class_10255 var2 && !var2.method_5869()) {
         this.touchingWater = false;
      } else {
         if (this.updateMovementInFluid(class_3486.field_15517, 0.014)) {
            this.onLanding();
            this.touchingWater = true;
         } else {
            this.touchingWater = false;
         }
      }
   }

   private void updateSwimming() {
      if (this.isSwimming) {
         this.isSwimming = this.isSprinting() && this.isTouchingWater() && !this.player.method_5765();
      } else {
         this.isSwimming = this.isSprinting()
            && this.isSubmergedInWater()
            && !this.player.method_5765()
            && this.player.method_73183().method_8316(this.posToBlockPos(this.pos)).method_15767(class_3486.field_15517);
      }
   }

   private void updateSubmergedInWaterState() {
      this.submergedInWater = this.submergedFluidTag.contains(class_3486.field_15517);
      this.submergedFluidTag.clear();
      double var1 = this.getEyeY() - 0.11111111F;
      if (!(
         this.player.method_5854() instanceof class_10255 var4
            && !var4.method_5869()
            && var4.method_5829().field_1325 >= var1
            && var4.method_5829().field_1322 <= var1
      )) {
         class_2338 var8 = class_2338.method_49637(this.pos.field_1352, var1, this.pos.field_1350);
         class_3610 var5 = this.player.method_73183().method_8316(var8);
         double var6 = var8.method_10264() + var5.method_15763(this.player.method_73183(), var8);
         if (var6 > var1) {
            this.submergedFluidTag.addAll(var5.method_40181().toList());
         }
      }
   }

   private double getEyeY() {
      return this.pos.field_1351 + this.player.method_5751();
   }

   public boolean isSubmergedInWater() {
      return this.submergedInWater && this.isTouchingWater();
   }

   private double getFluidHeight(class_6862<class_3611> var1) {
      return this.fluidHeight.getDouble(var1);
   }

   private boolean updateMovementInFluid(class_6862<class_3611> var1, double var2) {
      if (this.isRegionUnloaded()) {
         return false;
      }

      class_238 var4 = this.boundingBox.method_1011(0.001);
      int var5 = class_3532.method_15357(var4.field_1323);
      int var6 = class_3532.method_15384(var4.field_1320);
      int var7 = class_3532.method_15357(var4.field_1322);
      int var8 = class_3532.method_15384(var4.field_1325);
      int var9 = class_3532.method_15357(var4.field_1321);
      int var10 = class_3532.method_15384(var4.field_1324);
      double var11 = 0.0;
      boolean var13 = true;
      boolean var14 = false;
      class_243 var15 = class_243.field_1353;
      int var16 = 0;
      class_2339 var17 = new class_2339();

      for (int var18 = var5; var18 < var6; var18++) {
         for (int var19 = var7; var19 < var8; var19++) {
            for (int var20 = var9; var20 < var10; var20++) {
               var17.method_10103(var18, var19, var20);
               class_3610 var21 = this.player.method_73183().method_8316(var17);
               if (var21.method_15767(var1)) {
                  double var22 = var19 + var21.method_15763(this.player.method_73183(), var17);
                  if (var22 >= var4.field_1322) {
                     var14 = true;
                     var11 = Math.max(var22 - var4.field_1322, var11);
                     if (var13) {
                        class_243 var24 = var21.method_15758(this.player.method_73183(), var17);
                        if (var11 < 0.4) {
                           var24 = var24.method_1021(var11);
                        }

                        var15 = var15.method_1019(var24);
                        var16++;
                     }
                  }
               }
            }
         }
      }

      if (var15.method_1033() > 0.0) {
         if (var16 > 0) {
            var15 = var15.method_1021(1.0 / var16);
         }

         var15 = var15.method_1021(var2);
         if (Math.abs(this.velocity.field_1352) < 0.003 && Math.abs(this.velocity.field_1350) < 0.003 && var15.method_1033() < 0.0045) {
            var15 = var15.method_1029().method_1021(0.0045);
         }

         this.velocity = this.velocity.method_1019(var15);
      }

      this.fluidHeight.put(var1, var11);
      return var14;
   }

   private boolean isRegionUnloaded() {
      class_238 var1 = this.boundingBox.method_1014(1.0);
      int var2 = class_3532.method_15357(var1.field_1323);
      int var3 = class_3532.method_15384(var1.field_1320);
      int var4 = class_3532.method_15357(var1.field_1321);
      int var5 = class_3532.method_15384(var1.field_1324);
      return !this.player.method_73183().method_33597(var2, var4, var3, var5);
   }

   private class_243 getRotationVector() {
      return this.getRotationVector(this.pitch, this.yaw);
   }

   private class_243 getRotationVector(float var1, float var2) {
      float var3 = (float)(var1 * Math.PI / 180.0);
      float var4 = (float)(-var2 * Math.PI / 180.0);
      float var5 = class_3532.method_15362(var4);
      float var6 = class_3532.method_15374(var4);
      float var7 = class_3532.method_15362(var3);
      float var8 = class_3532.method_15374(var3);
      return new class_243(var6 * var7, -var8, var5 * var7);
   }

   public boolean hasStatusEffect(class_6880<class_1291> var1) {
      class_1293 var2 = this.player.method_6112(var1);
      return var2 != null && var2.method_5584() >= this.simulatedTicks;
   }

   private class_1293 getStatusEffect(class_6880<class_1291> var1) {
      class_1293 var2 = this.player.method_6112(var1);
      return var2 != null && var2.method_5584() >= this.simulatedTicks ? var2 : null;
   }

   public double getAttributeValue(class_6880<class_1320> var1) {
      return this.player.method_6127().method_26852(var1);
   }

   public PlayerSimulation clone() {
      return new PlayerSimulation(
         this.player,
         this.input,
         this.pos,
         this.velocity,
         this.boundingBox,
         this.yaw,
         this.pitch,
         this.sprinting,
         this.fallDistance,
         this.jumpingCooldown,
         this.isJumping,
         this.isFallFlying,
         this.onGround,
         this.horizontalCollision,
         this.verticalCollision,
         this.touchingWater,
         this.isSwimming,
         this.submergedInWater,
         new Object2DoubleArrayMap(this.fluidHeight),
         new HashSet<>(this.submergedFluidTag)
      );
   }

   public class_2338 posToBlockPos(class_243 var1) {
      return new class_2338(class_3532.method_15357(var1.field_1352), class_3532.method_15357(var1.field_1351), class_3532.method_15357(var1.field_1350));
   }

   public class_2680 getState(class_2338 var1) {
      return this.player.method_73183().method_8320(var1);
   }

   public static class SimulatedPlayerInput extends class_744 {
      public boolean forceSafeWalk = false;
      public float movementForward;
      public float movementSideways;
      public class_10185 playerInput;
      public static final double MAX_WALKING_SPEED = 0.121;

      public SimulatedPlayerInput(class_10185 var1) {
         this.playerInput = var1;
      }

      public void update() {
         if (this.playerInput.comp_3159() != this.playerInput.comp_3160()) {
            this.movementForward = this.playerInput.comp_3159() ? 1.0F : -1.0F;
         } else {
            this.movementForward = 0.0F;
         }

         if (this.playerInput.comp_3161() == this.playerInput.comp_3162()) {
            this.movementSideways = 0.0F;
         } else {
            this.movementSideways = this.playerInput.comp_3161() ? 1.0F : -1.0F;
         }

         if (this.playerInput.comp_3164()) {
            this.movementSideways *= 0.3F;
            this.movementForward *= 0.3F;
         }
      }

      public String toString() {
         return "SimulatedPlayerInput(forwards={"
            + this.playerInput.comp_3159()
            + "}, backwards={"
            + this.playerInput.comp_3160()
            + "}, left={"
            + this.playerInput.comp_3161()
            + "}, right={"
            + this.playerInput.comp_3162()
            + "}, jumping={"
            + this.playerInput.comp_3163()
            + "}, sprinting="
            + this.playerInput.comp_3165()
            + ", slowDown="
            + this.playerInput.comp_3164()
            + ")";
      }

      public static PlayerSimulation.SimulatedPlayerInput fromClientPlayer(class_10185 var0) {
         return new PlayerSimulation.SimulatedPlayerInput(var0);
      }

      public static PlayerSimulation.SimulatedPlayerInput guessInput(class_1657 var0) {
         class_243 var1 = var0.method_73189().method_1020(new class_243(var0.field_6014, var0.field_6036, var0.field_5969));
         double var2 = var1.method_37268();
         class_10185 var4 = new class_10185(false, false, false, false, !var0.method_24828(), var0.method_5715(), var2 >= 0.014641);
         if (var2 > 0.0025000000000000005) {
            double var5 = MoveUtil.getDegreesRelativeToView(var1, var0.method_36454());
            double var7 = class_3532.method_15338(var5);
            var4 = MoveUtil.getDirectionalInputForDegrees(var4, var7);
         }

         return new PlayerSimulation.SimulatedPlayerInput(var4);
      }
   }
}
