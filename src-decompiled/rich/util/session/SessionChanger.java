package rich.util.session;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.class_310;
import net.minecraft.class_320;

public class SessionChanger {
   private static Consumer<class_320> sessionSetter;

   public static void setSessionSetter(Consumer<class_320> var0) {
      sessionSetter = var0;
   }

   public static void changeUsername(String var0) {
      if (sessionSetter != null && var0 != null && !var0.isEmpty()) {
         UUID var1 = UUID.nameUUIDFromBytes(("OfflinePlayer:" + var0).getBytes());
         class_320 var2 = new class_320(var0, var1, "", Optional.empty(), Optional.empty());
         sessionSetter.accept(var2);
      }
   }

   public static String getCurrentUsername() {
      class_310 var0 = class_310.method_1551();
      return var0 != null && var0.method_1548() != null ? var0.method_1548().method_1676() : "";
   }
}
