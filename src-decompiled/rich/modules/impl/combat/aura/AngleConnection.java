package rich.modules.impl.combat.aura;

import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_2708;
import net.minecraft.class_2828;
import net.minecraft.class_3532;
import rich.IMinecraft;
import rich.Initialization;
import rich.events.api.EventHandler;
import rich.events.api.EventManager;
import rich.events.impl.PacketEvent;
import rich.events.impl.PlayerVelocityStrafeEvent;
import rich.events.impl.RotationUpdateEvent;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.util.math.TaskPriority;
import rich.util.math.TaskProcessor;

public class AngleConnection implements IMinecraft {
   public static AngleConnection INSTANCE = new AngleConnection();
   private AngleConstructor lastRotationPlan;
   private final TaskProcessor<AngleConstructor> rotationPlanTaskProcessor = new TaskProcessor<>();
   public Angle currentAngle;
   private Angle previousAngle;
   private Angle serverAngle = Angle.DEFAULT;
   private Angle fakeAngle;
   private boolean returning = false;

   public AngleConnection() {
      Initialization.getInstance().getManager().getEventManager();
      EventManager.register(this);
   }

   public void setRotation(Angle var1) {
      if (var1 == null) {
         this.previousAngle = this.currentAngle != null ? this.currentAngle : MathAngle.cameraAngle();
      } else {
         this.previousAngle = this.currentAngle;
      }

      this.currentAngle = var1;
   }

   public Angle getRotation() {
      return this.currentAngle != null ? this.currentAngle : MathAngle.cameraAngle();
   }

   public Angle getFakeRotation() {
      if (this.fakeAngle != null) {
         return this.fakeAngle;
      } else {
         return this.currentAngle != null ? this.currentAngle : (this.previousAngle != null ? this.previousAngle : MathAngle.cameraAngle());
      }
   }

   public void setFakeRotation(Angle var1) {
      this.fakeAngle = var1;
   }

   public Angle getPreviousRotation() {
      return this.currentAngle != null && this.previousAngle != null ? this.previousAngle : new Angle(mc.field_1724.field_5982, mc.field_1724.field_6004);
   }

   public Angle getMoveRotation() {
      AngleConstructor var1 = this.getCurrentRotationPlan();
      return this.currentAngle != null && var1 != null && var1.isMoveCorrection() ? this.currentAngle : MathAngle.cameraAngle();
   }

   public AngleConstructor getCurrentRotationPlan() {
      return this.rotationPlanTaskProcessor.fetchActiveTaskValue() != null ? this.rotationPlanTaskProcessor.fetchActiveTaskValue() : this.lastRotationPlan;
   }

   public void rotateTo(Angle.VecRotation var1, class_1309 var2, int var3, AngleConfig var4, TaskPriority var5, ModuleStructure var6) {
      this.rotateTo(var4.createRotationPlan(var1.getAngle(), var1.getVec(), var2, var3), var5, var6);
   }

   public void rotateTo(Angle var1, int var2, AngleConfig var3, TaskPriority var4, ModuleStructure var5) {
      this.rotateTo(var3.createRotationPlan(var1, var1.toVector(), null, var2), var4, var5);
   }

   public void rotateTo(Angle var1, AngleConfig var2, TaskPriority var3, ModuleStructure var4) {
      this.rotateTo(var2.createRotationPlan(var1, var1.toVector(), null, 1), var3, var4);
   }

   public void rotateTo(AngleConstructor var1, TaskPriority var2, ModuleStructure var3) {
      this.returning = false;
      this.rotationPlanTaskProcessor.addTask(new TaskProcessor.Task<>(1, var2.getPriority(), var3, var1));
   }

   public void update() {
      AngleConstructor var1 = this.getCurrentRotationPlan();
      if (var1 == null) {
         if (this.currentAngle != null && this.returning) {
            Angle var10 = MathAngle.cameraAngle();
            double var12 = computeRotationDifference(this.currentAngle, var10);
            if (var12 < 0.5) {
               this.setRotation(null);
               this.lastRotationPlan = null;
               this.returning = false;
            } else {
               float var5 = 0.25F;
               float var6 = Math.min(1.0F, (float)var12 / 30.0F);
               var5 += 0.4F * var6;
               float var7 = class_3532.method_15393(var10.getYaw() - this.currentAngle.getYaw());
               float var8 = this.currentAngle.getYaw() + var7 * var5;
               float var9 = class_3532.method_16439(var5, this.currentAngle.getPitch(), var10.getPitch());
               this.setRotation(new Angle(var8, var9).adjustSensitivity());
            }
         }
      } else {
         this.returning = false;
         Angle var2 = MathAngle.cameraAngle();
         if (this.lastRotationPlan != null) {
            double var3 = computeRotationDifference(this.serverAngle, var2);
            if (var1.getTicksUntilReset() <= this.rotationPlanTaskProcessor.tickCounter && var3 < var1.getResetThreshold()) {
               this.setRotation(null);
               this.lastRotationPlan = null;
               this.rotationPlanTaskProcessor.tickCounter = 0;
               return;
            }
         }

         Angle var11 = var1.nextRotation(this.currentAngle != null ? this.currentAngle : var2, this.rotationPlanTaskProcessor.fetchActiveTaskValue() == null)
            .adjustSensitivity();
         this.setRotation(var11);
         this.lastRotationPlan = var1;
         this.rotationPlanTaskProcessor.tick(1);
      }
   }

   public static double computeRotationDifference(Angle var0, Angle var1) {
      return Math.hypot(Math.abs(computeAngleDifference(var0.getYaw(), var1.getYaw())), Math.abs(var0.getPitch() - var1.getPitch()));
   }

   public static float computeAngleDifference(float var0, float var1) {
      return class_3532.method_15393(var0 - var1);
   }

   private class_243 fixVelocity(class_243 var1, class_243 var2, float var3) {
      if (this.currentAngle != null) {
         float var4 = this.currentAngle.getYaw();
         double var5 = var2.method_1027();
         if (var5 < 1.0E-7) {
            return class_243.field_1353;
         }

         class_243 var7 = (var5 > 1.0 ? var2.method_1029() : var2).method_1021(var3);
         float var8 = class_3532.method_15374(var4 * (float) (Math.PI / 180.0));
         float var9 = class_3532.method_15362(var4 * (float) (Math.PI / 180.0));
         return new class_243(
            var7.method_10216() * var9 - var7.method_10215() * var8, var7.method_10214(), var7.method_10215() * var9 + var7.method_10216() * var8
         );
      } else {
         return var1;
      }
   }

   public void clear() {
      this.rotationPlanTaskProcessor.activeTasks.clear();
   }

   public void startReturning() {
   }

   public void reset() {
      this.currentAngle = null;
      this.previousAngle = null;
      this.fakeAngle = null;
      this.lastRotationPlan = null;
      this.rotationPlanTaskProcessor.tickCounter = 0;
   }

   @EventHandler
   public void onPlayerVelocityStrafe(PlayerVelocityStrafeEvent var1) {
      AngleConstructor var2 = this.getCurrentRotationPlan();
      if (var2 != null && var2.isMoveCorrection()) {
         var1.setVelocity(this.fixVelocity(var1.getVelocity(), var1.getMovementInput(), var1.getSpeed()));
      }
   }

   @EventHandler
   public void onTick(TickEvent var1) {
      EventManager.callEvent(new RotationUpdateEvent((byte)0));
      this.update();
      EventManager.callEvent(new RotationUpdateEvent((byte)2));
   }

   @EventHandler
   public void onPacket(PacketEvent var1) {
      if (!var1.isCancelled()) {
         Object var4 = var1.getPacket();
         switch (var4) {
            case class_2828 var2 when var2.method_36172():
               this.serverAngle = new Angle(var2.method_12271(1.0F), var2.method_12270(1.0F));
               break;
            case class_2708 var6:
               class_2708 var3 = (class_2708)var4;
               this.serverAngle = new Angle(var3.comp_3228().comp_3150(), var3.comp_3228().comp_3151());
               break;
            default:
         }
      }
   }

   public AngleConstructor getLastRotationPlan() {
      return this.lastRotationPlan;
   }

   public TaskProcessor<AngleConstructor> getRotationPlanTaskProcessor() {
      return this.rotationPlanTaskProcessor;
   }

   public Angle getCurrentAngle() {
      return this.currentAngle;
   }

   public Angle getPreviousAngle() {
      return this.previousAngle;
   }

   public Angle getServerAngle() {
      return this.serverAngle;
   }

   public Angle getFakeAngle() {
      return this.fakeAngle;
   }

   public boolean isReturning() {
      return this.returning;
   }
}
