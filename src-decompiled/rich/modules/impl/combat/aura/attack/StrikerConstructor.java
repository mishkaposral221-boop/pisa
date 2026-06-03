package rich.modules.impl.combat.aura.attack;

import java.util.List;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import rich.IMinecraft;
import rich.events.impl.PacketEvent;
import rich.events.impl.UsingItemEvent;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.module.setting.implement.SelectSetting;

public class StrikerConstructor implements IMinecraft {
   StrikeManager attackHandler = new StrikeManager();

   public void tick() {
      this.attackHandler.tick();
   }

   public void onPacket(PacketEvent var1) {
      this.attackHandler.onPacket(var1);
   }

   public void performAttack(StrikerConstructor.AttackPerpetratorConfigurable var1) {
      this.attackHandler.handleAttack(var1);
   }

   public void performTriggerAttack(StrikerConstructor.AttackPerpetratorConfigurable var1) {
      this.attackHandler.handleTriggerAttack(var1);
   }

   public void onUsingItem(UsingItemEvent var1) {
      this.attackHandler.onUsingItem(var1);
   }

   public StrikeManager getAttackHandler() {
      return this.attackHandler;
   }

   public static class AttackPerpetratorConfigurable {
      private final class_1309 target;
      private final Angle angle;
      private final float maximumRange;
      private final boolean onlyCritical;
      private final boolean shouldBreakShield;
      private final boolean shouldUnPressShield;
      private final boolean eatAndAttack;
      private final boolean multiPoints;
      private final boolean ignoreWalls;
      private final class_238 box;
      private final SelectSetting aimMode;

      public AttackPerpetratorConfigurable(class_1309 var1, Angle var2, float var3, List<String> var4, SelectSetting var5, class_238 var6) {
         this.target = var1;
         this.angle = var2;
         this.maximumRange = var3;
         this.onlyCritical = var4.contains("Только криты") || var4.contains("Only Critical") || var4.contains("Crits with space");
         this.shouldBreakShield = var4.contains("Break Shield");
         this.shouldUnPressShield = var4.contains("UnPress Shield");
         this.multiPoints = var4.contains("Multi Points");
         this.eatAndAttack = var4.contains("No Attack When Eat");
         this.ignoreWalls = var4.contains("Бить сквозь стены") || var4.contains("Ignore The Walls");
         this.box = var6;
         this.aimMode = var5;
      }

      public class_1309 getTarget() {
         return this.target;
      }

      public Angle getAngle() {
         return this.angle;
      }

      public float getMaximumRange() {
         return this.maximumRange;
      }

      public boolean isOnlyCritical() {
         return this.onlyCritical;
      }

      public boolean isShouldBreakShield() {
         return this.shouldBreakShield;
      }

      public boolean isShouldUnPressShield() {
         return this.shouldUnPressShield;
      }

      public boolean isEatAndAttack() {
         return this.eatAndAttack;
      }

      public boolean isMultiPoints() {
         return this.multiPoints;
      }

      public boolean isIgnoreWalls() {
         return this.ignoreWalls;
      }

      public class_238 getBox() {
         return this.box;
      }

      public SelectSetting getAimMode() {
         return this.aimMode;
      }
   }
}
