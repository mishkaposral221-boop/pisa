package rich.util.inventory.script;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import rich.util.timer.StopWatch;

public class Script {
   private final StopWatch time = new StopWatch();
   private final List<Script.ScriptStep> scriptSteps = Lists.newCopyOnWriteArrayList();
   private final List<Script.ScriptTickStep> scriptTickSteps = Lists.newCopyOnWriteArrayList();
   private int currentStepIndex;
   private int currentTickStepIndex;
   private boolean interrupt;
   private Script.LoopStrategy loopStrategy = new Script.FiniteLoopStrategy(1);

   public Script() {
      this.cleanup();
   }

   public Script addStep(int var1, ScriptAction var2) {
      return this.addStep(var1, var2, () -> true, 0);
   }

   public Script addStep(int var1, ScriptAction var2, BooleanSupplier var3) {
      return this.addStep(var1, var2, var3, 0);
   }

   public Script addStep(int var1, ScriptAction var2, int var3) {
      return this.addStep(var1, var2, () -> true, var3);
   }

   public Script addStep(int var1, ScriptAction var2, BooleanSupplier var3, int var4) {
      this.scriptSteps.add(new Script.ScriptStep(var1, var2, var3, var4));
      Collections.sort(this.scriptSteps);
      return this;
   }

   public Script addTickStep(int var1, ScriptAction var2) {
      return this.addTickStep(var1, var2, () -> true, 0);
   }

   public Script addTickStep(int var1, ScriptAction var2, BooleanSupplier var3) {
      return this.addTickStep(var1, var2, var3, 0);
   }

   public Script addTickStep(int var1, ScriptAction var2, int var3) {
      return this.addTickStep(var1, var2, () -> true, var3);
   }

   public Script addTickStep(int var1, ScriptAction var2, BooleanSupplier var3, int var4) {
      this.scriptTickSteps.add(new Script.ScriptTickStep(var1, var2, var3, var4));
      Collections.sort(this.scriptTickSteps);
      return this;
   }

   public void resetTime() {
      this.time.reset();
   }

   public void resetStepIndex() {
      this.currentStepIndex = 0;
      this.currentTickStepIndex = 0;
   }

   public Script cleanupIfFinished() {
      if (this.isFinished()) {
         this.cleanup();
      }

      return this;
   }

   public Script cleanup() {
      this.scriptSteps.clear();
      this.scriptTickSteps.clear();
      this.resetTime();
      this.resetStepIndex();
      return this;
   }

   public void update() {
      if ((!this.scriptSteps.isEmpty() || !this.scriptTickSteps.isEmpty()) && !this.interrupt) {
         this.scriptSteps.forEach(var1 -> {
            if (this.currentStepIndex < this.scriptSteps.size()) {
               Script.ScriptStep var2 = this.scriptSteps.get(this.currentStepIndex);
               if (var2.condition().getAsBoolean() && this.time.finished(var2.delay())) {
                  var2.action().perform();
                  this.currentStepIndex++;
                  this.resetTime();
                  if (this.loopStrategy.shouldLoop(this.currentStepIndex, this.scriptSteps.size())) {
                     this.resetStepIndex();
                     this.loopStrategy.onLoop();
                  }
               }
            }
         });
         this.scriptTickSteps.forEach(var1 -> {
            if (this.currentTickStepIndex < this.scriptTickSteps.size()) {
               Script.ScriptTickStep var2 = this.scriptTickSteps.get(this.currentTickStepIndex);
               if (var2.condition().getAsBoolean() && var2.ticks() <= 0) {
                  var2.action().perform();
                  this.currentTickStepIndex++;
                  this.resetTime();
                  if (this.loopStrategy.shouldLoop(this.currentTickStepIndex, this.scriptTickSteps.size())) {
                     this.resetStepIndex();
                     this.loopStrategy.onLoop();
                  }
               }

               var2.decrementTicks();
            }
         });
         this.currentStepIndex = Math.min(this.currentStepIndex, this.scriptSteps.size());
         this.currentTickStepIndex = Math.min(this.currentTickStepIndex, this.scriptTickSteps.size());
      }
   }

   public Script setLoopStrategy(Script.LoopStrategy var1) {
      this.loopStrategy = var1;
      return this;
   }

   public boolean isFinished() {
      return this.currentStepIndex >= this.scriptSteps.size()
         && this.currentTickStepIndex >= this.scriptTickSteps.size()
         && !this.interrupt
         && this.loopStrategy.isFinished();
   }

   public StopWatch getTime() {
      return this.time;
   }

   public List<Script.ScriptStep> getScriptSteps() {
      return this.scriptSteps;
   }

   public List<Script.ScriptTickStep> getScriptTickSteps() {
      return this.scriptTickSteps;
   }

   public int getCurrentStepIndex() {
      return this.currentStepIndex;
   }

   public int getCurrentTickStepIndex() {
      return this.currentTickStepIndex;
   }

   public boolean isInterrupt() {
      return this.interrupt;
   }

   public Script.LoopStrategy getLoopStrategy() {
      return this.loopStrategy;
   }

   public void setCurrentStepIndex(int var1) {
      this.currentStepIndex = var1;
   }

   public void setCurrentTickStepIndex(int var1) {
      this.currentTickStepIndex = var1;
   }

   public void setInterrupt(boolean var1) {
      this.interrupt = var1;
   }

   public static class FiniteLoopStrategy implements Script.LoopStrategy {
      private final int loopCount;
      private int currentLoop;

      public FiniteLoopStrategy(int var1) {
         this.loopCount = var1 - 1;
      }

      @Override
      public boolean shouldLoop(int var1, int var2) {
         return var1 >= var2 && this.currentLoop < this.loopCount;
      }

      @Override
      public void onLoop() {
         this.currentLoop++;
      }

      @Override
      public boolean isFinished() {
         return this.currentLoop >= this.loopCount;
      }
   }

   public static class InfiniteLoopStrategy implements Script.LoopStrategy {
      @Override
      public boolean shouldLoop(int var1, int var2) {
         return var1 >= var2;
      }

      @Override
      public void onLoop() {
      }

      @Override
      public boolean isFinished() {
         return false;
      }
   }

   public interface LoopStrategy {
      boolean shouldLoop(int var1, int var2);

      void onLoop();

      boolean isFinished();
   }

   public static final class ScriptStep implements Comparable<Script.ScriptStep> {
      private int delay;
      private ScriptAction action;
      private BooleanSupplier condition;
      private int priority;

      public ScriptStep(int var1, ScriptAction var2, BooleanSupplier var3, int var4) {
         this.delay = var1;
         this.action = var2;
         this.condition = var3;
         this.priority = var4;
      }

      public int compareTo(Script.ScriptStep var1) {
         return Integer.compare(var1.priority(), this.priority());
      }

      public int delay() {
         return this.delay;
      }

      public ScriptAction action() {
         return this.action;
      }

      public BooleanSupplier condition() {
         return this.condition;
      }

      public int priority() {
         return this.priority;
      }

      public Script.ScriptStep delay(int var1) {
         this.delay = var1;
         return this;
      }

      public Script.ScriptStep action(ScriptAction var1) {
         this.action = var1;
         return this;
      }

      public Script.ScriptStep condition(BooleanSupplier var1) {
         this.condition = var1;
         return this;
      }

      public Script.ScriptStep priority(int var1) {
         this.priority = var1;
         return this;
      }
   }

   public static final class ScriptTickStep implements Comparable<Script.ScriptTickStep> {
      private int ticks;
      private ScriptAction action;
      private BooleanSupplier condition;
      private int priority;

      public ScriptTickStep(int var1, ScriptAction var2, BooleanSupplier var3, int var4) {
         this.ticks = var1;
         this.action = var2;
         this.condition = var3;
         this.priority = var4;
      }

      public int compareTo(Script.ScriptTickStep var1) {
         return Integer.compare(var1.priority(), this.priority());
      }

      public void decrementTicks() {
         this.ticks--;
      }

      public int ticks() {
         return this.ticks;
      }

      public ScriptAction action() {
         return this.action;
      }

      public BooleanSupplier condition() {
         return this.condition;
      }

      public int priority() {
         return this.priority;
      }

      public Script.ScriptTickStep ticks(int var1) {
         this.ticks = var1;
         return this;
      }

      public Script.ScriptTickStep action(ScriptAction var1) {
         this.action = var1;
         return this;
      }

      public Script.ScriptTickStep condition(BooleanSupplier var1) {
         this.condition = var1;
         return this;
      }

      public Script.ScriptTickStep priority(int var1) {
         this.priority = var1;
         return this;
      }
   }
}
