package rich.util.repository.staff;

public record Staff() {
   private final String name;

   public Staff(String var1) {
      this.name = var1;
   }

   public String getName() {
      return this.name;
   }
}
