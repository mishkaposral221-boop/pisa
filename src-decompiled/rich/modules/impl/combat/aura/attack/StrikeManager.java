package rich.modules.impl.combat.aura.attack;

import net.minecraft.class_1268;
import net.minecraft.class_1294;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1839;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2680;
import net.minecraft.class_2868;
import net.minecraft.class_2879;
import net.minecraft.class_4050;
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
      if (mc.field_1724 != null && mc.field_1724.method_24828()) {
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
      class_2596 var2 = var1.getPacket();
      if (var2 instanceof class_2879 || var2 instanceof class_2868) {
         this.clickScheduler.recalculate();
      }
   }

   public void resetPendingState() {
   }

   private boolean hasAnyMovementInput() {
      return mc.field_1724 == null
         ? false
         : mc.field_1724.field_3913.field_54155.comp_3159()
            || mc.field_1724.field_3913.field_54155.comp_3160()
            || mc.field_1724.field_3913.field_54155.comp_3161()
            || mc.field_1724.field_3913.field_54155.comp_3162();
   }

   private boolean isHoldingMace() {
      return this.clickScheduler.isHoldingMace();
   }

   private boolean isPlayerEating() {
      if (mc.field_1724 == null) {
         return false;
      }

      if (!mc.field_1724.method_6115()) {
         return false;
      }

      class_1799 var1 = mc.field_1724.method_6030();
      if (var1.method_7960()) {
         return false;
      }

      class_1839 var2 = var1.method_7976();
      return var2 == class_1839.field_8950 || var2 == class_1839.field_8946;
   }

   private boolean shouldWaitForEating() {
      return this.isPlayerEating();
   }

   private boolean isInWater() {
      return mc.field_1724 != null && (mc.field_1724.method_5799() || mc.field_1724.method_5869() || mc.field_1724.method_5681());
   }

   private boolean hasLowCeiling() {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         class_2338 var1 = mc.field_1724.method_24515();
         class_2338 var2 = var1.method_10086(2);
         class_2338 var3 = var1.method_10086(3);
         class_2680 var4 = mc.field_1687.method_8320(var2);
         class_2680 var5 = mc.field_1687.method_8320(var3);
         boolean var6 = !var4.method_26215() && !var4.method_26220(mc.field_1687, var2).method_1110();
         boolean var7 = !var5.method_26215() && !var5.method_26220(mc.field_1687, var3).method_1110();
         return var6 || var7;
      } else {
         return false;
      }
   }

   private boolean isPerfectCrit() {
      return mc.field_1724 == null
         ? false
         : mc.field_1724.field_6017 > 0.0
            && !mc.field_1724.method_24828()
            && !mc.field_1724.method_6101()
            && !mc.field_1724.method_5799()
            && !mc.field_1724.method_6059(class_1294.field_5919)
            && !mc.field_1724.method_5765()
            && !mc.field_1724.method_31549().field_7479;
   }

   private boolean isAscending() {
      return mc.field_1724 == null ? false : !mc.field_1724.method_24828() && mc.field_1724.method_18798().field_1351 > 0.0;
   }

   private boolean isDescending() {
      return mc.field_1724 == null ? false : !mc.field_1724.method_24828() && mc.field_1724.method_18798().field_1351 <= 0.0;
   }

   private boolean willBeCritInTicks(int var1) {
      if (var1 == 0) {
         return this.isPerfectCrit();
      }

      PlayerSimulation var2 = PlayerSimulation.simulateLocalPlayer(var1);
      return var2.fallDistance > 0.0F
         && !var2.onGround
         && var2.velocity.field_1351 <= 0.0
         && !var2.isClimbing()
         && !var2.player.method_5799()
         && !var2.hasStatusEffect(class_1294.field_5919)
         && !var2.player.method_5765()
         && !var2.player.method_31549().field_7479;
   }

   private boolean hasMovementRestrictions() {
      if (mc.field_1724 == null) {
         return true;
      } else if (this.isInWater()) {
         return false;
      } else if (this.hasLowCeiling()) {
         return true;
      } else if (mc.field_1724.method_6059(class_1294.field_5919)) {
         return true;
      } else if (mc.field_1724.method_6059(class_1294.field_5902)) {
         return true;
      } else if (PlayerInteractionHelper.isBoxInBlock(mc.field_1724.method_5829().method_1014(-0.001), class_2246.field_10343)) {
         return true;
      } else if (mc.field_1724.method_5771()) {
         return true;
      } else if (mc.field_1724.method_6101()) {
         return true;
      } else {
         return !PlayerInteractionHelper.canChangeIntoPose(class_4050.field_18076, mc.field_1724.method_73189())
            ? true
            : mc.field_1724.method_31549().field_7479;
      }
   }

   private boolean shouldResetSprintForCrit() {
      if (mc.field_1724 == null) {
         return false;
      } else if (this.isInWater()) {
         return false;
      } else {
         return mc.field_1724.method_6128() ? false : mc.field_1724.method_5624();
      }
   }

   private boolean canCritNow() {
      if (this.isInWater() || this.hasLowCeiling() || this.hasMovementRestrictions()) {
         return true;
      } else if (this.isAscending()) {
         return false;
      } else {
         return mc.field_1724.method_24828() ? true : this.isDescending() && mc.field_1724.field_6017 > 0.0;
      }
   }

   void handleAttack(StrikerConstructor.AttackPerpetratorConfigurable var1) {
      if (var1.getTarget() != null && var1.getTarget().method_5805()) {
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
                              boolean var3 = mc.field_1724.method_5624();
                              boolean var4 = var3 && this.shouldResetSprintForCrit();
                              if (var4) {
                                 mc.field_1724.method_5728(false);
                              }

                              this.executeAttack(var1);
                              if (var4) {
                                 mc.field_1724.method_5728(true);
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
      if (var1.isShouldUnPressShield() && mc.field_1724.method_6115() && mc.field_1724.method_6030().method_7909().equals(class_1802.field_8255)) {
         mc.field_1761.method_2897(mc.field_1724);
         this.shieldWatch.reset();
      }
   }

   private void handleMaceAttack(StrikerConstructor.AttackPerpetratorConfigurable var1) {
      if (!this.shouldWaitForEating()) {
         if (!(mc.field_1724.method_5739(var1.getTarget()) > 3.0F)) {
            if (RaycastAngle.rayTrace(var1)) {
               if (this.isLookingAtTarget(var1)) {
                  if (this.clickScheduler.isMaceFastAttack()) {
                     if (this.attackTimer.finished(25.0)) {
                        this.preAttackEntity(var1);
                        boolean var2 = mc.field_1724.method_5624();
                        boolean var3 = var2 && this.shouldResetSprintForCrit();
                        if (var3) {
                           mc.field_1724.method_5728(false);
                        }

                        this.executeAttack(var1);
                        if (var3) {
                           mc.field_1724.method_5728(true);
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
      mc.field_1761.method_2918(mc.field_1724, var1.getTarget());
      mc.field_1724.method_6104(class_1268.field_5808);
      this.attackTimer.reset();
      this.count++;
   }

   void handleTriggerAttack(StrikerConstructor.AttackPerpetratorConfigurable var1) {
      if (!this.shouldWaitForEating()) {
         if (RaycastAngle.rayTrace(var1)) {
            if (this.isLookingAtTarget(var1)) {
               if (this.clickScheduler.isCooldownComplete(0)) {
                  this.preAttackEntity(var1);
                  boolean var2 = mc.field_1724.method_5624();
                  boolean var3 = var2 && this.shouldResetSprintForCrit();
                  if (var3) {
                     mc.field_1724.method_5728(false);
                  }

                  this.executeAttack(var1);
                  if (var3) {
                     mc.field_1724.method_5728(true);
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

      if (mc.field_1724.method_6115() && !mc.field_1724.method_6030().method_7909().equals(class_1802.field_8255) && var1.isEatAndAttack()) {
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
      class_243 var2 = mc.field_1724.method_33571();
      class_243 var3 = AngleConnection.INSTANCE.getRotation().toVector();
      class_243 var4 = var2.method_1019(var3.method_1021(var1.getMaximumRange()));
      return var1.getBox().method_992(var2, var4).isPresent();
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
