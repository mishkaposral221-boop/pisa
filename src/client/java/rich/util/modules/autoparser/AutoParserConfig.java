package rich.util.modules.autoparser;

public class AutoParserConfig {
   private static AutoParserConfig instance;
   private boolean enabled = false;
   private int discountPercent = 60;
   private volatile boolean isRunning = false;
   private boolean debugMode = false;

   private AutoParserConfig() {
   }

   public static AutoParserConfig getInstance() {
      if (instance == null) {
         instance = new AutoParserConfig();
      }

      return instance;
   }

   public void toggle() {
      this.enabled = !this.enabled;
   }

   public void reset() {
      this.isRunning = false;
   }

   public boolean isRunning() {
      return this.isRunning;
   }

   public void setRunning(boolean var1) {
      this.isRunning = var1;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public int getDiscountPercent() {
      return this.discountPercent;
   }

   public boolean isDebugMode() {
      return this.debugMode;
   }

   public void setEnabled(boolean var1) {
      this.enabled = var1;
   }

   public void setDiscountPercent(int var1) {
      this.discountPercent = var1;
   }

   public void setDebugMode(boolean var1) {
      this.debugMode = var1;
   }
}
