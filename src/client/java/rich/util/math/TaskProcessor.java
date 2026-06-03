package rich.util.math;

import java.util.PriorityQueue;
import rich.modules.module.ModuleStructure;

public class TaskProcessor<T> {
   public int tickCounter = 0;
   public PriorityQueue<TaskProcessor.Task<T>> activeTasks = new PriorityQueue<>((var0, var1) -> Integer.compare(var1.priority, var0.priority));

   public void tick(int var1) {
      this.tickCounter += var1;
   }

   public void addTask(TaskProcessor.Task<T> var1) {
      this.activeTasks.removeIf(var1x -> var1x.provider.equals(var1.provider));
      var1.expiresIn = var1.expiresIn + this.tickCounter;
      this.activeTasks.add(var1);
   }

   public T fetchActiveTaskValue() {
      while (
         !this.activeTasks.isEmpty()
            && this.activeTasks.peek() != null
            && (this.activeTasks.peek().expiresIn <= this.tickCounter || !this.activeTasks.peek().provider.isState())
      ) {
         this.activeTasks.poll();
      }

      if (this.activeTasks.isEmpty()) {
         return null;
      } else {
         return this.activeTasks.peek() != null ? this.activeTasks.peek().value : null;
      }
   }

   public static class Task<T> {
      private int expiresIn;
      private final int priority;
      private final ModuleStructure provider;
      private final T value;

      @Override
      public String toString() {
         return "TaskProcessor.Task(expiresIn="
            + this.expiresIn
            + ", priority="
            + this.priority
            + ", provider="
            + this.provider
            + ", value="
            + this.value
            + ")";
      }

      public Task(int var1, int var2, ModuleStructure var3, T var4) {
         this.expiresIn = var1;
         this.priority = var2;
         this.provider = var3;
         this.value = (T)var4;
      }
   }
}
