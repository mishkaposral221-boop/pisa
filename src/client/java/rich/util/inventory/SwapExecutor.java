package rich.util.inventory;

import net.minecraft.client.MinecraftClient;

public class SwapExecutor {
   private static final MinecraftClient mc = MinecraftClient.getInstance();
   private SwapExecutor.Phase phase = SwapExecutor.Phase.IDLE;
   private final MovementController movement = new MovementController();
   private SwapSettings settings = SwapSettings.defaults();
   private Runnable swapAction;
   private Runnable onComplete;
   private long phaseStartTime;
   private int currentDelay;

   public void execute(Runnable var1, SwapSettings var2) {
      this.execute(var1, var2, null);
   }

   public void execute(Runnable var1, SwapSettings var2, Runnable var3) {
      if (this.phase == SwapExecutor.Phase.IDLE) {
         this.swapAction = var1;
         this.settings = var2 != null ? var2 : SwapSettings.defaults();
         this.onComplete = var3;
         if (this.settings.shouldStopMovement()) {
            this.movement.saveState();
            this.movement.block();
            if (this.settings.shouldStopSprint()) {
               this.movement.stopSprint();
            }

            this.startPhase(SwapExecutor.Phase.PRE_STOP, this.settings.randomPreStopDelay());
         } else {
            this.startPhase(SwapExecutor.Phase.SWAPPING, 0);
         }
      }
   }

   public void tick() {
      if (this.phase != SwapExecutor.Phase.IDLE && this.phase != SwapExecutor.Phase.FINISHED) {
         if (mc.player == null) {
            this.reset();
         } else {
            if (this.settings.shouldStopMovement() && this.phase != SwapExecutor.Phase.RESUMING && this.phase != SwapExecutor.Phase.FINISHED) {
               this.movement.block();
               if (this.settings.shouldStopSprint()) {
                  this.movement.stopSprint();
               }
            }

            boolean var1 = true;
            byte var2 = 10;

            for (int var3 = 0; var1 && var3 < var2; var1 = this.processPhase()) {
               var3++;
            }
         }
      }
   }

   private boolean processPhase() {
      long var1 = System.currentTimeMillis() - this.phaseStartTime;
      switch (this.phase) {
         case PRE_STOP:
            this.movement.block();
            if (this.settings.shouldStopSprint()) {
               this.movement.stopSprint();
            }

            if (var1 >= this.currentDelay) {
               this.startPhase(SwapExecutor.Phase.STOPPING, 0);
               return true;
            }
            break;
         case STOPPING:
            this.movement.block();
            if (this.settings.shouldStopSprint()) {
               this.movement.stopSprint();
            }

            this.startPhase(SwapExecutor.Phase.WAIT_STOP, this.settings.randomWaitStopDelay());
            return this.currentDelay == 0;
         case WAIT_STOP:
            this.movement.block();
            if (this.settings.shouldStopSprint()) {
               this.movement.stopSprint();
            }

            boolean var3 = this.movement.isPlayerStopped(this.settings.getVelocityThreshold());
            boolean var4 = var1 >= this.currentDelay;
            if (!var3 && !var4) {
               break;
            }

            this.startPhase(SwapExecutor.Phase.PRE_SWAP, this.settings.randomPreSwapDelay());
            return this.currentDelay == 0;
         case PRE_SWAP:
            this.movement.block();
            if (this.settings.shouldStopSprint()) {
               this.movement.stopSprint();
            }

            if (var1 >= this.currentDelay) {
               this.startPhase(SwapExecutor.Phase.SWAPPING, 0);
               return true;
            }
            break;
         case SWAPPING:
            this.movement.block();
            if (this.settings.shouldStopSprint()) {
               this.movement.stopSprint();
            }

            if (this.swapAction != null) {
               this.swapAction.run();
            }

            this.startPhase(SwapExecutor.Phase.POST_SWAP, this.settings.randomPostSwapDelay());
            return this.currentDelay == 0;
         case POST_SWAP:
            this.movement.block();
            if (var1 >= this.currentDelay) {
               if (this.settings.shouldCloseInventory()) {
                  InventoryUtils.closeScreen();
               }

               this.startPhase(SwapExecutor.Phase.RESUMING, this.settings.randomResumeDelay());
               return this.currentDelay == 0;
            }
            break;
         case RESUMING:
            if (var1 >= this.currentDelay) {
               if (this.settings.shouldStopMovement()) {
                  this.movement.restoreFromCurrent();
               }

               this.phase = SwapExecutor.Phase.FINISHED;
               if (this.onComplete != null) {
                  this.onComplete.run();
               }

               this.reset();
               return false;
            }
      }

      return false;
   }

   private void startPhase(SwapExecutor.Phase var1, int var2) {
      this.phase = var1;
      this.phaseStartTime = System.currentTimeMillis();
      this.currentDelay = var2;
   }

   public void cancel() {
      if (this.movement.isBlocked()) {
         this.movement.restoreFromCurrent();
      }

      this.reset();
   }

   public void reset() {
      this.phase = SwapExecutor.Phase.IDLE;
      this.swapAction = null;
      this.onComplete = null;
      this.movement.reset();
   }

   public boolean isRunning() {
      return this.phase != SwapExecutor.Phase.IDLE && this.phase != SwapExecutor.Phase.FINISHED;
   }

   public boolean isBlocking() {
      return this.movement.isBlocked() || this.isRunning() && this.settings.shouldStopMovement();
   }

   public SwapExecutor.Phase getPhase() {
      return this.phase;
   }

   public enum Phase {
      IDLE,
      PRE_STOP,
      STOPPING,
      WAIT_STOP,
      PRE_SWAP,
      SWAPPING,
      POST_SWAP,
      RESUMING,
      FINISHED;
   }
}
