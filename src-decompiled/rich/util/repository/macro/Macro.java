package rich.util.repository.macro;

public record Macro() {
   private final String name;
   private final String message;
   private final int key;

   public Macro(String var1, String var2, int var3) {
      this.name = var1;
      this.message = var2;
      this.key = var3;
   }
}
