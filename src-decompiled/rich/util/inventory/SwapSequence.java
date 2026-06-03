package rich.util.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public class SwapSequence {
   private final List<SwapSequence.SwapStep> steps = new ArrayList<>();
   private int currentIndex;
   private int tickCounter;
   private boolean running;

   public SwapSequence step(int var1, Runnable var2) {
      return this.step(var1, var2, () -> true);
   }

   public SwapSequence step(int var1, Runnable var2, BooleanSupplier var3) {
      this.steps.add(new SwapSequence.SwapStep(var1, var2, var3));
      return this;
   }

   public SwapSequence start() {
      this.currentIndex = 0;
      this.tickCounter = 0;
      this.running = true;
      return this;
   }

   public void tick() {
      if (this.running && this.currentIndex < this.steps.size()) {
         SwapSequence.SwapStep var1 = this.steps.get(this.currentIndex);
         if (var1.condition.getAsBoolean()) {
            if (this.tickCounter >= var1.delayTicks) {
               var1.action.run();
               this.currentIndex++;
               this.tickCounter = 0;
            } else {
               this.tickCounter++;
            }
         }
      } else {
         this.running = false;
      }
   }

   public boolean isFinished() {
      return !this.running || this.currentIndex >= this.steps.size();
   }

   public void reset() {
      this.steps.clear();
      this.currentIndex = 0;
      this.tickCounter = 0;
      this.running = false;
   }

   public void cancel() {
      this.running = false;
   }

   private record SwapStep() {
      private final int delayTicks;
      private final Runnable action;
      private final BooleanSupplier condition;

      private SwapStep(int var1, Runnable var2, BooleanSupplier var3) {
         this.delayTicks = var1;
         this.action = var2;
         this.condition = var3;
      }
   }
}
