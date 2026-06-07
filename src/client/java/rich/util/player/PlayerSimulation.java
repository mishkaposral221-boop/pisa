package rich.util.player;

import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import net.minecraft.util.PlayerInput;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.block.LadderBlock;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluid;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos.Mutable;
import rich.IMinecraft;
import rich.util.move.MoveUtil;

public class PlayerSimulation implements IMinecraft, Simulation {
   public final PlayerEntity player;
   public final PlayerSimulation.SimulatedPlayerInput input;
   public Vec3d pos;
   public Vec3d velocity;
   public Box boundingBox;
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
   private final Object2DoubleMap<TagKey<Fluid>> fluidHeight;
   private final HashSet<TagKey<Fluid>> submergedFluidTag;
   private int simulatedTicks = 0;
   private boolean clipLedged = false;
   private static final double STEP_HEIGHT = 0.5;

   public PlayerSimulation(
      PlayerEntity var1,
      PlayerSimulation.SimulatedPlayerInput var2,
      Vec3d var3,
      Vec3d var4,
      Box var5,
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
      Object2DoubleMap<TagKey<Fluid>> var19,
      HashSet<TagKey<Fluid>> var20
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
      PlayerSimulation var1 = fromClientPlayer(PlayerSimulation.SimulatedPlayerInput.fromClientPlayer(mc.player.input.playerInput));

      for (int var2 = 0; var2 < var0; var2++) {
         var1.tick();
      }

      return var1;
   }

   public static PlayerSimulation simulateOtherPlayer(PlayerEntity var0, int var1) {
      PlayerSimulation var2 = fromOtherPlayer(var0, PlayerSimulation.SimulatedPlayerInput.guessInput(var0));

      for (int var3 = 0; var3 < var1; var3++) {
         var2.tick();
      }

      return var2;
   }

   public static PlayerSimulation fromClientPlayer(PlayerSimulation.SimulatedPlayerInput var0) {
      ClientPlayerEntity var1 = mc.player;
      return new PlayerSimulation(
         var1,
         var0,
         var1.getEntityPos(),
         var1.getVelocity(),
         var1.getBoundingBox(),
         var1.getYaw(),
         var1.getPitch(),
         var1.isSprinting(),
         (float)var1.fallDistance,
         var1.jumpingCooldown,
         var1.jumping,
         var1.isGliding(),
         var1.isOnGround(),
         var1.horizontalCollision,
         var1.verticalCollision,
         var1.isTouchingWater(),
         var1.isSwimming(),
         var1.isSubmergedInWater(),
         new Object2DoubleArrayMap(var1.fluidHeight),
         new HashSet<>(var1.submergedFluidTag)
      );
   }

   public static PlayerSimulation fromOtherPlayer(PlayerEntity var0, PlayerSimulation.SimulatedPlayerInput var1) {
      return new PlayerSimulation(
         var0,
         var1,
         var0.getEntityPos(),
         var0.getEntityPos().subtract(new Vec3d(var0.lastX, var0.lastY, var0.lastZ)),
         var0.getBoundingBox(),
         var0.getYaw(),
         var0.getPitch(),
         var0.isSprinting(),
         (float)var0.fallDistance,
         var0.jumpingCooldown,
         var0.jumping,
         var0.isGliding(),
         var0.isOnGround(),
         var0.horizontalCollision,
         var0.verticalCollision,
         var0.isTouchingWater(),
         var0.isSwimming(),
         var0.isSubmergedInWater(),
         new Object2DoubleArrayMap(var0.fluidHeight),
         new HashSet<>(var0.submergedFluidTag)
      );
   }

   @Override
   public Vec3d pos() {
      return this.player.getEntityPos();
   }

   @Override
   public void tick() {
      this.simulatedTicks++;
      this.clipLedged = false;
      if (!(this.pos.y <= -70.0)) {
         this.input.update();
         this.checkWaterState();
         this.updateSubmergedInWaterState();
         this.updateSwimming();
         if (this.jumpingCooldown > 0) {
            this.jumpingCooldown--;
         }

         this.isJumping = this.input.playerInput.jump();
         double var1 = this.velocity.x;
         double var3 = this.velocity.y;
         double var5 = this.velocity.z;
         if (Math.abs(this.velocity.x) < 0.003) {
            var1 = 0.0;
         }

         if (Math.abs(this.velocity.y) < 0.003) {
            var3 = 0.0;
         }

         if (Math.abs(this.velocity.z) < 0.003) {
            var5 = 0.0;
         }

         if (this.onGround) {
            this.isFallFlying = false;
         }

         this.velocity = new Vec3d(var1, var3, var5);
         if (this.isJumping) {
            double var7 = this.isInLava() ? this.getFluidHeight(FluidTags.LAVA) : this.getFluidHeight(FluidTags.WATER);
            boolean var9 = this.isTouchingWater() && var7 > 0.0;
            double var10 = this.getSwimHeight();
            if (!var9 || this.onGround && !(var7 > var10)) {
               if (!this.isInLava() || this.onGround && !(var7 > var10)) {
                  if ((this.onGround || var9 && var7 <= var10) && this.jumpingCooldown == 0) {
                     this.jump();
                     if (this.player.equals(mc.player)) {
                        this.jumpingCooldown = 10;
                     }
                  }
               } else {
                  this.swimUpward(FluidTags.LAVA);
               }
            } else {
               this.swimUpward(FluidTags.WATER);
            }
         }

         float var12 = this.input.movementSideways * 0.98F;
         float var8 = this.input.movementForward * 0.98F;
         float var13 = 0.0F;
         if (this.hasStatusEffect(StatusEffects.SLOW_FALLING) || this.hasStatusEffect(StatusEffects.LEVITATION)) {
            this.onLanding();
         }

         this.travel(new Vec3d(var12, var13, var8));
      }
   }

   private void travel(Vec3d var1) {
      if (this.isSwimming && !this.player.hasVehicle()) {
         double var2 = this.getRotationVector().y;
         double var4 = var2 < -0.2 ? 0.085 : 0.06;
         BlockPos var6 = new BlockPos(
            MathHelper.floor(this.pos.x),
            MathHelper.floor(this.pos.y + 1.0 - 0.1),
            MathHelper.floor(this.pos.z)
         );
         if (var2 <= 0.0 || this.input.playerInput.jump() || !this.player.getEntityWorld().getBlockState(var6).getFluidState().isEmpty()) {
            this.velocity = this.velocity.add(0.0, (var2 - this.velocity.y) * var4, 0.0);
         }
      }

      double var19 = this.velocity.y;
      double var20 = 0.08;
      boolean var21 = this.velocity.y <= 0.0;
      if (this.velocity.y <= 0.0 && this.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
         var20 = 0.01;
         this.onLanding();
      }

      if (this.isTouchingWater() && this.player.shouldSwimInFluids()) {
         double var25 = this.pos.y;
         float var28 = this.isSprinting() ? 0.9F : 0.8F;
         float var30 = 0.02F;
         float var32 = (float)this.getAttributeValue(EntityAttributes.WATER_MOVEMENT_EFFICIENCY);
         if (!this.onGround) {
            var32 *= 0.5F;
         }

         if (var32 > 0.0F) {
            var28 += (0.54600006F - var28) * var32 / 3.0F;
            var30 += (this.getMovementSpeed() - var30) * var32 / 3.0F;
         }

         if (this.hasStatusEffect(StatusEffects.DOLPHINS_GRACE)) {
            var28 = 0.96F;
         }

         this.updateVelocity(var30, var1);
         this.move(this.velocity);
         Vec3d var33 = this.velocity;
         if (this.horizontalCollision && this.isClimbing()) {
            var33 = new Vec3d(var33.x, 0.2, var33.z);
         }

         this.velocity = var33.multiply(var28, 0.8, var28);
         Vec3d var34 = this.player.applyFluidMovingSpeed(var20, var21, this.velocity);
         this.velocity = var34;
         if (this.horizontalCollision && this.doesNotCollide(var34.x, var34.y + 0.6 - this.pos.y + var25, var34.z)) {
            this.velocity = new Vec3d(var34.x, 0.3, var34.z);
         }
      } else if (this.isInLava() && this.player.shouldSwimInFluids()) {
         double var24 = this.pos.y;
         this.updateVelocity(0.02F, var1);
         this.move(this.velocity);
         if (this.getFluidHeight(FluidTags.LAVA) <= this.getSwimHeight()) {
            this.velocity = this.velocity.multiply(0.5, 0.8, 0.5);
            this.velocity = this.player.applyFluidMovingSpeed(var20, var21, this.velocity);
         } else {
            this.velocity = this.velocity.multiply(0.5);
         }

         if (!this.player.hasNoGravity()) {
            this.velocity = this.velocity.add(0.0, -var20 / 4.0, 0.0);
         }

         if (this.horizontalCollision
            && this.doesNotCollide(this.velocity.x, this.velocity.y + 0.6 - this.pos.y + var24, this.velocity.z)) {
            this.velocity = new Vec3d(this.velocity.x, 0.3, this.velocity.z);
         }
      } else if (this.isFallFlying) {
         Vec3d var9 = this.velocity;
         if (var9.y > -0.5) {
            this.fallDistance = 1.0F;
         }

         Vec3d var10 = this.getRotationVector();
         float var11 = this.pitch * (float) (Math.PI / 180.0);
         double var12 = Math.sqrt(var10.x * var10.x + var10.z * var10.z);
         double var14 = this.velocity.horizontalLength();
         double var16 = var10.length();
         float var18 = MathHelper.cos(var11);
         var18 = (float)(var18 * (var18 * Math.min(1.0, var16 / 0.4)));
         var9 = this.velocity.add(0.0, var20 * (-1.0 + var18 * 0.75), 0.0);
         if (var9.y < 0.0 && var12 > 0.0) {
            double var7 = var9.y * -0.1 * var18;
            var9 = var9.add(var10.x * var7 / var12, var7, var10.z * var7 / var12);
         }

         if (var11 < 0.0F && var12 > 0.0) {
            double var22 = var14 * -MathHelper.sin(var11) * 0.04;
            var9 = var9.add(-var10.x * var22 / var12, var22 * 3.2, -var10.z * var22 / var12);
         }

         if (var12 > 0.0) {
            var9 = var9.add((var10.x / var12 * var14 - var9.x) * 0.1, 0.0, (var10.z / var12 * var14 - var9.z) * 0.1);
         }

         this.velocity = var9.multiply(0.99, 0.98, 0.99);
         this.move(this.velocity);
      } else {
         BlockPos var23 = this.getVelocityAffectingPos();
         float var8 = this.player.getEntityWorld().getBlockState(var23).getBlock().getSlipperiness();
         float var27 = this.onGround ? var8 * 0.91F : 0.91F;
         Vec3d var29 = this.applyMovementInput(var1, var8);
         double var31 = var29.y;
         if (this.hasStatusEffect(StatusEffects.LEVITATION)) {
            StatusEffectInstance var13 = this.getStatusEffect(StatusEffects.LEVITATION);
            if (var13 != null) {
               var31 += (0.05 * (var13.getAmplifier() + 1) - var29.y) * 0.2;
            }
         } else if (this.player.getEntityWorld().isClient() && !this.player.getEntityWorld().isChunkLoaded(var23)) {
            var31 = this.pos.y > this.player.getEntityWorld().getBottomY() ? -0.1 : 0.0;
         } else if (!this.player.hasNoGravity()) {
            var31 -= var20;
         }

         if (this.player.hasNoDrag()) {
            this.velocity = new Vec3d(var29.x, var31, var29.z);
         } else {
            this.velocity = new Vec3d(var29.x * var27, var31 * 0.98F, var29.z * var27);
         }
      }

      if (this.player.getAbilities().flying && !this.player.hasVehicle()) {
         this.velocity = new Vec3d(this.velocity.x, var19 * 0.6, this.velocity.z);
         this.onLanding();
      }
   }

   private Vec3d applyMovementInput(Vec3d var1, float var2) {
      this.updateVelocity(this.getMovementSpeed(var2), var1);
      this.velocity = this.applyClimbingSpeed(this.velocity);
      this.move(this.velocity);
      Vec3d var3 = this.velocity;
      BlockPos var4 = this.posToBlockPos(this.pos);
      BlockState var5 = this.getState(var4);
      if ((this.horizontalCollision || this.isJumping)
         && (this.isClimbing() || var5 != null && var5.isOf(Blocks.POWDER_SNOW) && PowderSnowBlock.canWalkOnPowderSnow(this.player))) {
         var3 = new Vec3d(var3.x, 0.2, var3.z);
      }

      return var3;
   }

   private void updateVelocity(float var1, Vec3d var2) {
      Vec3d var3 = Entity.movementInputToVelocity(var2, var1, this.yaw);
      this.velocity = this.velocity.add(var3);
   }

   private float getMovementSpeed(float var1) {
      return this.onGround ? this.getMovementSpeed() * (0.21600002F / (var1 * var1 * var1)) : this.getAirStrafingSpeed();
   }

   private float getAirStrafingSpeed() {
      float var1 = 0.02F;
      return this.input.playerInput.sprint() ? var1 + 0.006F : var1;
   }

   private float getMovementSpeed() {
      return 0.1F;
   }

   private void move(Vec3d var1) {
      Vec3d var2 = var1;
      var2 = this.adjustMovementForSneaking(var2);
      Vec3d var3 = this.adjustMovementForCollisions(var2);
      if (var3.lengthSquared() > 1.0E-7) {
         this.pos = this.pos.add(var3);
         this.boundingBox = this.player.getDimensions(this.player.getPose()).getBoxAt(this.pos);
      }

      boolean var4 = !MathHelper.approximatelyEquals(var1.x, var3.x);
      boolean var5 = !MathHelper.approximatelyEquals(var1.z, var3.z);
      this.horizontalCollision = var4 || var5;
      this.verticalCollision = var1.y != var3.y;
      this.onGround = this.verticalCollision && var1.y < 0.0;
      if (!this.isTouchingWater()) {
         this.checkWaterState();
      }

      if (this.onGround) {
         this.onLanding();
      } else if (var1.y < 0.0) {
         this.fallDistance = this.fallDistance - (float)var1.y;
      }

      Vec3d var6 = this.velocity;
      if (this.horizontalCollision || this.verticalCollision) {
         this.velocity = new Vec3d(var4 ? 0.0 : var6.x, this.onGround ? 0.0 : var6.y, var5 ? 0.0 : var6.z);
      }
   }

   private Vec3d adjustMovementForCollisions(Vec3d var1) {
      Box var2 = new Box(-0.3, 0.0, -0.3, 0.3, 1.8, 0.3).offset(this.pos);
      List var3 = Collections.emptyList();
      Vec3d var4;
      if (var1.lengthSquared() == 0.0) {
         var4 = var1;
      } else {
         var4 = Entity.adjustMovementForCollisions(this.player, var1, var2, this.player.getEntityWorld(), var3);
      }

      boolean var5 = var1.x != var4.x;
      boolean var6 = var1.y != var4.y;
      boolean var7 = var1.z != var4.z;
      boolean var8 = this.onGround || var6 && var1.y < 0.0;
      if (this.player.getStepHeight() > 0.0F && var8 && (var5 || var7)) {
         Vec3d var9 = Entity.adjustMovementForCollisions(
            this.player, new Vec3d(var1.x, this.player.getStepHeight(), var1.z), var2, this.player.getEntityWorld(), var3
         );
         Vec3d var10 = Entity.adjustMovementForCollisions(
            this.player,
            new Vec3d(0.0, this.player.getStepHeight(), 0.0),
            var2.stretch(var1.x, 0.0, var1.z),
            this.player.getEntityWorld(),
            var3
         );
         Vec3d var11 = Entity.adjustMovementForCollisions(
               this.player, new Vec3d(var1.x, 0.0, var1.z), var2.offset(var10), this.player.getEntityWorld(), var3
            )
            .add(var10);
         if (var10.y < this.player.getStepHeight() && var11.horizontalLengthSquared() > var9.horizontalLengthSquared()) {
            var9 = var11;
         }

         if (var9.horizontalLengthSquared() > var4.horizontalLengthSquared()) {
            return var9.add(
               Entity.adjustMovementForCollisions(
                  this.player, new Vec3d(0.0, -var9.y + var1.y, 0.0), var2.offset(var9), this.player.getEntityWorld(), var3
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
      this.velocity = this.velocity.add(0.0, this.getJumpVelocity() - this.velocity.y, 0.0);
      if (this.isSprinting()) {
         float var1 = (float)Math.toRadians(this.yaw);
         this.velocity = this.velocity.add(-MathHelper.sin(var1) * 0.2, 0.0, MathHelper.cos(var1) * 0.2);
      }
   }

   private Vec3d applyClimbingSpeed(Vec3d var1) {
      if (!this.isClimbing()) {
         return var1;
      }

      this.onLanding();
      double var2 = MathHelper.clamp(var1.x, -0.15F, 0.15F);
      double var4 = MathHelper.clamp(var1.z, -0.15F, 0.15F);
      double var6 = Math.max(var1.y, -0.15F);
      if (var6 < 0.0 && !this.getState(this.posToBlockPos(this.pos)).isOf(Blocks.SCAFFOLDING) && this.player.isHoldingOntoLadder()) {
         var6 = 0.0;
      }

      return new Vec3d(var2, var6, var4);
   }

   public boolean isClimbing() {
      BlockPos var1 = this.posToBlockPos(this.pos);
      BlockState var2 = this.getState(var1);
      return var2.isIn(BlockTags.CLIMBABLE) ? true : var2.getBlock() instanceof TrapdoorBlock && this.canEnterTrapdoor(var1, var2);
   }

   private boolean canEnterTrapdoor(BlockPos var1, BlockState var2) {
      if (!(Boolean)var2.get(TrapdoorBlock.OPEN)) {
         return false;
      }

      BlockState var3 = this.player.getEntityWorld().getBlockState(var1.down());
      return var3.isOf(Blocks.LADDER)
         && ((Direction)var3.get(LadderBlock.FACING)).equals(var2.get(TrapdoorBlock.FACING));
   }

   private Vec3d adjustMovementForSneaking(Vec3d var1) {
      if (var1.y <= 0.0 && this.isStandingOnSurface()) {
         double var2 = var1.x;
         double var4 = var1.z;

         double var6;
         for (var6 = 0.05;
            var2 != 0.0 && this.player.getEntityWorld().isSpaceEmpty(this.player, this.boundingBox.offset(var2, -0.5, 0.0));
            var2 += var2 > 0.0 ? -var6 : var6
         ) {
            if (var2 < var6 && var2 >= -var6) {
               var2 = 0.0;
               break;
            }
         }

         while (var4 != 0.0 && this.player.getEntityWorld().isSpaceEmpty(this.player, this.boundingBox.offset(0.0, -0.5, var4))) {
            if (var4 < var6 && var4 >= -var6) {
               var4 = 0.0;
               break;
            }

            var4 += var4 > 0.0 ? -var6 : var6;
         }

         while (var2 != 0.0 && var4 != 0.0 && this.player.getEntityWorld().isSpaceEmpty(this.player, this.boundingBox.offset(var2, -0.5, var4))) {
            var2 = var2 < var6 && var2 >= -var6 ? 0.0 : (var2 > 0.0 ? var2 - var6 : var2 + var6);
            if (var4 < var6 && var4 >= -var6) {
               var4 = 0.0;
               break;
            }

            var4 += var4 > 0.0 ? -var6 : var6;
         }

         if (var1.x != var2 || var1.z != var4) {
            this.clipLedged = true;
         }

         if (this.shouldClipAtLedge()) {
            var1 = new Vec3d(var2, var1.y, var4);
         }
      }

      return var1;
   }

   protected boolean shouldClipAtLedge() {
      return this.input.playerInput.sneak() || this.input.forceSafeWalk;
   }

   private boolean isStandingOnSurface() {
      return this.onGround
         || this.fallDistance < 0.5 && !this.player.getEntityWorld().isSpaceEmpty(this.player, this.boundingBox.offset(0.0, this.fallDistance - 0.5, 0.0));
   }

   private boolean isSprinting() {
      return this.sprinting;
   }

   private float getJumpVelocity() {
      return 0.42F * this.getJumpVelocityMultiplier() + this.getJumpBoostVelocityModifier();
   }

   private float getJumpBoostVelocityModifier() {
      if (this.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
         StatusEffectInstance var1 = this.getStatusEffect(StatusEffects.JUMP_BOOST);
         return 0.1F * (var1.getAmplifier() + 1);
      } else {
         return 0.0F;
      }
   }

   private float getJumpVelocityMultiplier() {
      float var1 = 0.0F;
      Block var2 = this.getState(this.posToBlockPos(this.pos)).getBlock();
      if (var2 != null) {
         var1 = var2.getJumpVelocityMultiplier();
      }

      float var3 = 0.0F;
      Block var4 = this.getState(this.getVelocityAffectingPos()).getBlock();
      if (var4 != null) {
         var3 = var4.getJumpVelocityMultiplier();
      }

      return var1 == 1.0F ? var3 : var1;
   }

   private boolean doesNotCollide(double var1, double var3, double var5) {
      return this.doesNotCollide(this.boundingBox.offset(var1, var3, var5));
   }

   private boolean doesNotCollide(Box var1) {
      return this.player.getEntityWorld().isSpaceEmpty(this.player, var1) && !this.player.getEntityWorld().containsFluid(var1);
   }

   private void swimUpward(TagKey<Fluid> var1) {
      this.velocity = this.velocity.add(0.0, 0.04F, 0.0);
   }

   private BlockPos getVelocityAffectingPos() {
      return BlockPos.ofFloored(this.pos.x, this.boundingBox.minY - 0.5000001, this.pos.z);
   }

   private double getSwimHeight() {
      return this.player.getStandingEyeHeight() < 0.4 ? 0.0 : 0.4;
   }

   private boolean isTouchingWater() {
      return this.touchingWater;
   }

   public boolean isInLava() {
      return this.fluidHeight.getDouble(FluidTags.LAVA) > 0.0;
   }

   private void checkWaterState() {
      if (this.player.getVehicle() instanceof AbstractBoatEntity var2 && !var2.isSubmergedInWater()) {
         this.touchingWater = false;
      } else {
         if (this.updateMovementInFluid(FluidTags.WATER, 0.014)) {
            this.onLanding();
            this.touchingWater = true;
         } else {
            this.touchingWater = false;
         }
      }
   }

   private void updateSwimming() {
      if (this.isSwimming) {
         this.isSwimming = this.isSprinting() && this.isTouchingWater() && !this.player.hasVehicle();
      } else {
         this.isSwimming = this.isSprinting()
            && this.isSubmergedInWater()
            && !this.player.hasVehicle()
            && this.player.getEntityWorld().getFluidState(this.posToBlockPos(this.pos)).isIn(FluidTags.WATER);
      }
   }

   private void updateSubmergedInWaterState() {
      this.submergedInWater = this.submergedFluidTag.contains(FluidTags.WATER);
      this.submergedFluidTag.clear();
      double var1 = this.getEyeY() - 0.11111111F;
      if (!(
         this.player.getVehicle() instanceof AbstractBoatEntity var4
            && !var4.isSubmergedInWater()
            && var4.getBoundingBox().maxY >= var1
            && var4.getBoundingBox().minY <= var1
      )) {
         BlockPos var8 = BlockPos.ofFloored(this.pos.x, var1, this.pos.z);
         FluidState var5 = this.player.getEntityWorld().getFluidState(var8);
         double var6 = var8.getY() + var5.getHeight(this.player.getEntityWorld(), var8);
         if (var6 > var1) {
            this.submergedFluidTag.addAll(var5.streamTags().toList());
         }
      }
   }

   private double getEyeY() {
      return this.pos.y + this.player.getStandingEyeHeight();
   }

   public boolean isSubmergedInWater() {
      return this.submergedInWater && this.isTouchingWater();
   }

   private double getFluidHeight(TagKey<Fluid> var1) {
      return this.fluidHeight.getDouble(var1);
   }

   private boolean updateMovementInFluid(TagKey<Fluid> var1, double var2) {
      if (this.isRegionUnloaded()) {
         return false;
      }

      Box var4 = this.boundingBox.contract(0.001);
      int var5 = MathHelper.floor(var4.minX);
      int var6 = MathHelper.ceil(var4.maxX);
      int var7 = MathHelper.floor(var4.minY);
      int var8 = MathHelper.ceil(var4.maxY);
      int var9 = MathHelper.floor(var4.minZ);
      int var10 = MathHelper.ceil(var4.maxZ);
      double var11 = 0.0;
      boolean var13 = true;
      boolean var14 = false;
      Vec3d var15 = Vec3d.ZERO;
      int var16 = 0;
      net.minecraft.util.math.BlockPos.Mutable var17 = new net.minecraft.util.math.BlockPos.Mutable();

      for (int var18 = var5; var18 < var6; var18++) {
         for (int var19 = var7; var19 < var8; var19++) {
            for (int var20 = var9; var20 < var10; var20++) {
               var17.set(var18, var19, var20);
               FluidState var21 = this.player.getEntityWorld().getFluidState(var17);
               if (var21.isIn(var1)) {
                  double var22 = var19 + var21.getHeight(this.player.getEntityWorld(), var17);
                  if (var22 >= var4.minY) {
                     var14 = true;
                     var11 = Math.max(var22 - var4.minY, var11);
                     if (var13) {
                        Vec3d var24 = var21.getVelocity(this.player.getEntityWorld(), var17);
                        if (var11 < 0.4) {
                           var24 = var24.multiply(var11);
                        }

                        var15 = var15.add(var24);
                        var16++;
                     }
                  }
               }
            }
         }
      }

      if (var15.length() > 0.0) {
         if (var16 > 0) {
            var15 = var15.multiply(1.0 / var16);
         }

         var15 = var15.multiply(var2);
         if (Math.abs(this.velocity.x) < 0.003 && Math.abs(this.velocity.z) < 0.003 && var15.length() < 0.0045) {
            var15 = var15.normalize().multiply(0.0045);
         }

         this.velocity = this.velocity.add(var15);
      }

      this.fluidHeight.put(var1, var11);
      return var14;
   }

   private boolean isRegionUnloaded() {
      Box var1 = this.boundingBox.expand(1.0);
      int var2 = MathHelper.floor(var1.minX);
      int var3 = MathHelper.ceil(var1.maxX);
      int var4 = MathHelper.floor(var1.minZ);
      int var5 = MathHelper.ceil(var1.maxZ);
      return !this.player.getEntityWorld().isRegionLoaded(var2, var4, var3, var5);
   }

   private Vec3d getRotationVector() {
      return this.getRotationVector(this.pitch, this.yaw);
   }

   private Vec3d getRotationVector(float var1, float var2) {
      float var3 = (float)(var1 * Math.PI / 180.0);
      float var4 = (float)(-var2 * Math.PI / 180.0);
      float var5 = MathHelper.cos(var4);
      float var6 = MathHelper.sin(var4);
      float var7 = MathHelper.cos(var3);
      float var8 = MathHelper.sin(var3);
      return new Vec3d(var6 * var7, -var8, var5 * var7);
   }

   public boolean hasStatusEffect(RegistryEntry<StatusEffect> var1) {
      StatusEffectInstance var2 = this.player.getStatusEffect(var1);
      return var2 != null && var2.getDuration() >= this.simulatedTicks;
   }

   private StatusEffectInstance getStatusEffect(RegistryEntry<StatusEffect> var1) {
      StatusEffectInstance var2 = this.player.getStatusEffect(var1);
      return var2 != null && var2.getDuration() >= this.simulatedTicks ? var2 : null;
   }

   public double getAttributeValue(RegistryEntry<EntityAttribute> var1) {
      return this.player.getAttributes().getValue(var1);
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

   public BlockPos posToBlockPos(Vec3d var1) {
      return new BlockPos(MathHelper.floor(var1.x), MathHelper.floor(var1.y), MathHelper.floor(var1.z));
   }

   public BlockState getState(BlockPos var1) {
      return this.player.getEntityWorld().getBlockState(var1);
   }

   public static class SimulatedPlayerInput extends Input {
      public boolean forceSafeWalk = false;
      public float movementForward;
      public float movementSideways;
      public PlayerInput playerInput;
      public static final double MAX_WALKING_SPEED = 0.121;

      public SimulatedPlayerInput(PlayerInput var1) {
         this.playerInput = var1;
      }

      public void update() {
         if (this.playerInput.forward() != this.playerInput.backward()) {
            this.movementForward = this.playerInput.forward() ? 1.0F : -1.0F;
         } else {
            this.movementForward = 0.0F;
         }

         if (this.playerInput.left() == this.playerInput.right()) {
            this.movementSideways = 0.0F;
         } else {
            this.movementSideways = this.playerInput.left() ? 1.0F : -1.0F;
         }

         if (this.playerInput.sneak()) {
            this.movementSideways *= 0.3F;
            this.movementForward *= 0.3F;
         }
      }

      public String toString() {
         return "SimulatedPlayerInput(forwards={"
            + this.playerInput.forward()
            + "}, backwards={"
            + this.playerInput.backward()
            + "}, left={"
            + this.playerInput.left()
            + "}, right={"
            + this.playerInput.right()
            + "}, jumping={"
            + this.playerInput.jump()
            + "}, sprinting="
            + this.playerInput.sprint()
            + ", slowDown="
            + this.playerInput.sneak()
            + ")";
      }

      public static PlayerSimulation.SimulatedPlayerInput fromClientPlayer(PlayerInput var0) {
         return new PlayerSimulation.SimulatedPlayerInput(var0);
      }

      public static PlayerSimulation.SimulatedPlayerInput guessInput(PlayerEntity var0) {
         Vec3d var1 = var0.getEntityPos().subtract(new Vec3d(var0.lastX, var0.lastY, var0.lastZ));
         double var2 = var1.horizontalLengthSquared();
         PlayerInput var4 = new PlayerInput(false, false, false, false, !var0.isOnGround(), var0.isSneaking(), var2 >= 0.014641);
         if (var2 > 0.0025000000000000005) {
            double var5 = MoveUtil.getDegreesRelativeToView(var1, var0.getYaw());
            double var7 = MathHelper.wrapDegrees(var5);
            var4 = MoveUtil.getDirectionalInputForDegrees(var4, var7);
         }

         return new PlayerSimulation.SimulatedPlayerInput(var4);
      }
   }
}
