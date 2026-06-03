package rich.modules.impl.combat.aura.attack;

import net.minecraft.util.Hand;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.entity.EntityPose;
import rich.IMinecraft;
import rich.events.impl.PacketEvent;
import rich.events.impl.UsingItemEvent;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.impl.combat.aura.target.RaycastAngle;
import rich.util.player.PlayerSimulation;
import rich.util.string.PlayerInteractionHelper;
import rich.util.timer.StopWatch;

public class StrikeManager implements IMinecraft {
   private final Pressing clickScheduler = new Pressing();
   private final StopWatch attackTimer = new StopWatch();
   private final StopWatch shieldWatch = new StopWatch();
   private int count = 0;
   private int ticksOnBlock = 0;

   public void tick() {
      if (mc.player != null && mc.player.isOnGround()) {
         this.ticksOnBlock++;
      } else {
         this.ticksOnBlock = 0;
      }
   }

   public void onUsingItem(UsingItemEvent var1) {
      if (var1.getType() == -1 && !this.shieldWatch.finished(50.0)) {
         var1.cancel();
      }
   }

   public void onPacket(PacketEvent var1) {
      Packet var2 = var1.getPacket();
      if (var2 instanceof HandSwingC2SPacket || var2 instanceof UpdateSelectedSlotC2SPacket) {
         this.clickScheduler.recalculate();
      }
   }

   public void resetPendingState() {
   }

   private boolean hasAnyMovementInput() {
      return mc.player == null
         ? false
         : mc.player.input.playerInput.forward()
            || mc.player.input.playerInput.backward()
            || mc.player.input.playerInput.left()
            || mc.player.input.playerInput.right();
   }

   private boolean isHoldingMace() {
      return this.clickScheduler.isHoldingMace();
   }

   private boolean isPlayerEating() {
      if (mc.player == null) {
         return false;
      }

      if (!mc.player.isUsingItem()) {
         return false;
      }

      ItemStack var1 = mc.player.getActiveItem();
      if (var1.isEmpty()) {
         return false;
      }

      UseAction var2 = var1.getUseAction();
      return var2 == UseAction.EAT || var2 == UseAction.DRINK;
   }

   private boolean shouldWaitForEating() {
      return this.isPlayerEating();
   }

   private boolean isInWater() {
      return mc.player != null && (mc.player.isTouchingWater() || mc.player.isSubmergedInWater() || mc.player.isSwimming());
   }

   private boolean hasLowCeiling() {
      if (mc.player != null && mc.world != null) {
         BlockPos var1 = mc.player.getBlockPos();
         BlockPos var2 = var1.up(2);
         BlockPos var3 = var1.up(3);
         BlockState var4 = mc.world.getBlockState(var2);
         BlockState var5 = mc.world.getBlockState(var3);
         boolean var6 = !var4.isAir() && !var4.getCollisionShape(mc.world, var2).isEmpty();
         boolean var7 = !var5.isAir() && !var5.getCollisionShape(mc.world, var3).isEmpty();
         return var6 || var7;
      } else {
         return false;
      }
   }

   private boolean isPerfectCrit() {
      return mc.player == null
         ? false
         : mc.player.fallDistance > 0.0
            && !mc.player.isOnGround()
            && !mc.player.isClimbing()
            && !mc.player.isTouchingWater()
            && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
            && !mc.player.hasVehicle()
            && !mc.player.getAbilities().flying;
   }

   private boolean isAscending() {
      return mc.player == null ? false : !mc.player.isOnGround() && mc.player.getVelocity().y > 0.0;
   }

   private boolean isDescending() {
      return mc.player == null ? false : !mc.player.isOnGround() && mc.player.getVelocity().y <= 0.0;
   }

   private boolean willBeCritInTicks(int var1) {
      if (var1 == 0) {
         return this.isPerfectCrit();
      }

      PlayerSimulation var2 = PlayerSimulation.simulateLocalPlayer(var1);
      return var2.fallDistance > 0.0F
         && !var2.onGround
         && var2.velocity.y <= 0.0
         && !var2.isClimbing()
         && !var2.player.isTouchingWater()
         && !var2.hasStatusEffect(StatusEffects.BLINDNESS)
         && !var2.player.hasVehicle()
         && !var2.player.getAbilities().flying;
   }

   private boolean hasMovementRestrictions() {
      if (mc.player == null) {
         return true;
      } else if (this.isInWater()) {
         return false;
      } else if (this.hasLowCeiling()) {
         return true;
      } else if (mc.player.hasStatusEffect(StatusEffects.BLINDNESS)) {
         return true;
      } else if (mc.player.hasStatusEffect(StatusEffects.LEVITATION)) {
         return true;
      } else if (PlayerInteractionHelper.isBoxInBlock(mc.player.getBoundingBox().expand(-0.001), Blocks.COBWEB)) {
         return true;
      } else if (mc.player.isInLava()) {
         return true;
      } else if (mc.player.isClimbing()) {
         return true;
      } else {
         return !PlayerInteractionHelper.canChangeIntoPose(EntityPose.STANDING, mc.player.getEntityPos())
            ? true
            : mc.player.getAbilities().flying;
      }
   }

   private boolean shouldResetSprintForCrit() {
      if (mc.player == null) {
         return false;
      } else if (this.isInWater()) {
         return false;
      } else {
         return mc.player.isGliding() ? false : mc.player.isSprinting();
      }
   }

   private boolean canCritNow() {
      if (this.isInWater() || this.hasLowCeiling() || this.hasMovementRestrictions()) {
         return true;
      } else if (this.isAscending()) {
         return false;
      } else {
         return mc.player.isOnGround() ? true : this.isDescending() && mc.player.fallDistance > 0.0;
      }
   }

   void handleAttack(StrikerConstructor.AttackPerpetratorConfigurable var1) {
      if (var1.getTarget() != null && var1.getTarget().isAlive()) {
         if (!this.shouldWaitForEating()) {
            if (this.isHoldingMace()) {
               this.handleMaceAttack(var1);
            } else {
               boolean var2 = this.checkElytraMode(var1);
               if (!var2 || this.checkElytraRaycast(var1)) {
                  if (RaycastAngle.rayTrace(var1)) {
                     if (this.isLookingAtTarget(var1)) {
                        if (this.clickScheduler.isCooldownComplete(0)) {
                           if (this.canCritNow()) {
                              this.preAttackEntity(var1);
                              boolean var3 = mc.player.isSprinting();
                              boolean var4 = var3 && this.shouldResetSprintForCrit();
                              if (var4) {
                                 mc.player.setSprinting(false);
                              }

                              this.executeAttack(var1);
                              if (var4) {
                                 mc.player.setSprinting(true);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void preAttackEntity(StrikerConstructor.AttackPerpetratorConfigurable var1) {
      if (var1.isShouldUnPressShield() && mc.player.isUsingItem() && mc.player.getActiveItem().getItem().equals(Items.SHIELD)) {
         mc.interactionManager.stopUsingItem(mc.player);
         this.shieldWatch.reset();
      }
   }

   private void handleMaceAttack(StrikerConstructor.AttackPerpetratorConfigurable var1) {
      if (!this.shouldWaitForEating()) {
         if (!(mc.player.distanceTo(var1.getTarget()) > 3.0F)) {
            if (RaycastAngle.rayTrace(var1)) {
               if (this.isLookingAtTarget(var1)) {
                  if (this.clickScheduler.isMaceFastAttack()) {
                     if (this.attackTimer.finished(25.0)) {
                        this.preAttackEntity(var1);
                        boolean var2 = mc.player.isSprinting();
                        boolean var3 = var2 && this.shouldResetSprintForCrit();
                        if (var3) {
                           mc.player.setSprinting(false);
                        }

                        this.executeAttack(var1);
                        if (var3) {
                           mc.player.setSprinting(true);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean checkElytraMode(StrikerConstructor.AttackPerpetratorConfigurable var1) {
      return false;
   }

   private boolean checkElytraRaycast(StrikerConstructor.AttackPerpetratorConfigurable var1) {
      return false;
   }

   private void executeAttack(StrikerConstructor.AttackPerpetratorConfigurable var1) {
      mc.interactionManager.attackEntity(mc.player, var1.getTarget());
      mc.player.swingHand(Hand.MAIN_HAND);
      this.attackTimer.reset();
      this.count++;
   }

   void handleTriggerAttack(StrikerConstructor.AttackPerpetratorConfigurable var1) {
      if (!this.shouldWaitForEating()) {
         if (RaycastAngle.rayTrace(var1)) {
            if (this.isLookingAtTarget(var1)) {
               if (this.clickScheduler.isCooldownComplete(0)) {
                  this.preAttackEntity(var1);
                  boolean var2 = mc.player.isSprinting();
                  boolean var3 = var2 && this.shouldResetSprintForCrit();
                  if (var3) {
                     mc.player.setSprinting(false);
                  }

                  this.executeAttack(var1);
                  if (var3) {
                     mc.player.setSprinting(true);
                  }
               }
            }
         }
      }
   }

   private boolean canAttackTrigger(StrikerConstructor.AttackPerpetratorConfigurable var1) {
      if (this.shouldWaitForEating()) {
         return false;
      } else if (!this.clickScheduler.isCooldownComplete(0)) {
         return false;
      } else {
         return !this.isInWater() && !this.hasLowCeiling() && !this.hasMovementRestrictions() ? this.isPerfectCrit() : true;
      }
   }

   public boolean shouldResetSprinting(StrikerConstructor.AttackPerpetratorConfigurable var1) {
      if (this.shouldWaitForEating()) {
         return false;
      } else {
         return this.isHoldingMace() ? true : this.shouldResetSprintForCrit();
      }
   }

   public boolean shouldResetSprintingForTrigger(StrikerConstructor.AttackPerpetratorConfigurable var1) {
      return this.shouldWaitForEating() ? false : this.shouldResetSprintForCrit();
   }

   public boolean canAttack(StrikerConstructor.AttackPerpetratorConfigurable var1, int var2) {
      if (this.shouldWaitForEating()) {
         return false;
      }

      if (this.isHoldingMace()) {
         return this.attackTimer.finished(25.0) && this.clickScheduler.isMaceFastAttack();
      }

      if (!this.clickScheduler.isCooldownComplete(0)) {
         return false;
      }

      if (var2 > 0) {
         if (!this.isInWater() && !this.hasLowCeiling() && !this.hasMovementRestrictions()) {
            for (int var3 = 0; var3 <= var2; var3++) {
               if (this.willBeCritInTicks(var3)) {
                  return true;
               }

               PlayerSimulation var4 = PlayerSimulation.simulateLocalPlayer(var3);
               if (var4.onGround) {
                  return true;
               }
            }

            return false;
         } else {
            return true;
         }
      } else {
         return this.clickScheduler.isCooldownComplete(0) && this.canCritNow();
      }
   }

   public boolean canCrit(StrikerConstructor.AttackPerpetratorConfigurable var1, int var2) {
      if (this.isHoldingMace()) {
         return true;
      }

      if (mc.player.isUsingItem() && !mc.player.getActiveItem().getItem().equals(Items.SHIELD) && var1.isEatAndAttack()) {
         return false;
      }

      if (!this.isInWater() && !this.hasLowCeiling() && !this.hasMovementRestrictions()) {
         if (var2 > 0) {
            for (int var3 = 0; var3 <= var2; var3++) {
               if (this.willBeCritInTicks(var3)) {
                  return true;
               }

               PlayerSimulation var4 = PlayerSimulation.simulateLocalPlayer(var3);
               if (var4.onGround) {
                  return true;
               }
            }

            return false;
         } else {
            return this.canCritNow();
         }
      } else {
         return true;
      }
   }

   private boolean isLookingAtTarget(StrikerConstructor.AttackPerpetratorConfigurable var1) {
      Vec3d var2 = mc.player.getEyePos();
      Vec3d var3 = AngleConnection.INSTANCE.getRotation().toVector();
      Vec3d var4 = var2.add(var3.multiply(var1.getMaximumRange()));
      return var1.getBox().raycast(var2, var4).isPresent();
   }

   public void setCount(int var1) {
      this.count = var1;
   }

   public void setTicksOnBlock(int var1) {
      this.ticksOnBlock = var1;
   }

   public Pressing getClickScheduler() {
      return this.clickScheduler;
   }

   public StopWatch getAttackTimer() {
      return this.attackTimer;
   }

   public StopWatch getShieldWatch() {
      return this.shieldWatch;
   }

   public int getCount() {
      return this.count;
   }

   public int getTicksOnBlock() {
      return this.ticksOnBlock;
   }
}
