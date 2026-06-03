package rich.util.inventory;

public class SwapSettings {
   private boolean stopMovement = true;
   private boolean stopSprint = true;
   private boolean closeInventory = true;
   private int preStopDelayMin = 0;
   private int preStopDelayMax = 50;
   private int waitStopDelayMin = 50;
   private int waitStopDelayMax = 150;
   private int preSwapDelayMin = 20;
   private int preSwapDelayMax = 100;
   private int postSwapDelayMin = 20;
   private int postSwapDelayMax = 80;
   private int resumeDelayMin = 50;
   private int resumeDelayMax = 150;
   private double velocityThreshold = 0.001;

   public static SwapSettings defaults() {
      return new SwapSettings();
   }

   public static SwapSettings instant() {
      return new SwapSettings()
         .stopMovement(false)
         .stopSprint(false)
         .preStopDelay(0, 0)
         .waitStopDelay(0, 0)
         .preSwapDelay(0, 0)
         .postSwapDelay(0, 0)
         .resumeDelay(0, 0);
   }

   public static SwapSettings instantWithStop() {
      return new SwapSettings()
         .stopMovement(true)
         .stopSprint(true)
         .preStopDelay(0, 0)
         .waitStopDelay(15, 30)
         .preSwapDelay(0, 5)
         .postSwapDelay(0, 5)
         .resumeDelay(10, 20);
   }

   public static SwapSettings legit() {
      return new SwapSettings()
         .stopMovement(true)
         .stopSprint(true)
         .preStopDelay(0, 10)
         .waitStopDelay(40, 80)
         .preSwapDelay(15, 40)
         .postSwapDelay(15, 30)
         .resumeDelay(25, 50);
   }

   public SwapSettings stopMovement(boolean var1) {
      this.stopMovement = var1;
      return this;
   }

   public SwapSettings stopSprint(boolean var1) {
      this.stopSprint = var1;
      return this;
   }

   public SwapSettings closeInventory(boolean var1) {
      this.closeInventory = var1;
      return this;
   }

   public SwapSettings preStopDelay(int var1, int var2) {
      this.preStopDelayMin = var1;
      this.preStopDelayMax = var2;
      return this;
   }

   public SwapSettings waitStopDelay(int var1, int var2) {
      this.waitStopDelayMin = var1;
      this.waitStopDelayMax = var2;
      return this;
   }

   public SwapSettings preSwapDelay(int var1, int var2) {
      this.preSwapDelayMin = var1;
      this.preSwapDelayMax = var2;
      return this;
   }

   public SwapSettings postSwapDelay(int var1, int var2) {
      this.postSwapDelayMin = var1;
      this.postSwapDelayMax = var2;
      return this;
   }

   public SwapSettings resumeDelay(int var1, int var2) {
      this.resumeDelayMin = var1;
      this.resumeDelayMax = var2;
      return this;
   }

   public SwapSettings velocityThreshold(double var1) {
      this.velocityThreshold = var1;
      return this;
   }

   public boolean shouldStopMovement() {
      return this.stopMovement;
   }

   public boolean shouldStopSprint() {
      return this.stopSprint;
   }

   public boolean shouldCloseInventory() {
      return this.closeInventory;
   }

   public double getVelocityThreshold() {
      return this.velocityThreshold;
   }

   public int randomPreStopDelay() {
      return this.random(this.preStopDelayMin, this.preStopDelayMax);
   }

   public int randomWaitStopDelay() {
      return this.random(this.waitStopDelayMin, this.waitStopDelayMax);
   }

   public int randomPreSwapDelay() {
      return this.random(this.preSwapDelayMin, this.preSwapDelayMax);
   }

   public int randomPostSwapDelay() {
      return this.random(this.postSwapDelayMin, this.postSwapDelayMax);
   }

   public int randomResumeDelay() {
      return this.random(this.resumeDelayMin, this.resumeDelayMax);
   }

   private int random(int var1, int var2) {
      return var1 >= var2 ? var1 : var1 + (int)(Math.random() * (var2 - var1 + 1));
   }
}
