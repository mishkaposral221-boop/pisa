package rich.util.repository.way;

import net.minecraft.class_2338;

public record Way() {
   private final String name;
   private final class_2338 pos;
   private final String server;

   public Way(String var1, class_2338 var2, String var3) {
      this.name = var1;
      this.pos = var2;
      this.server = var3;
   }
}
