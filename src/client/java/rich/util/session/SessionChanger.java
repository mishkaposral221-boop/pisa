package rich.util.session;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

public class SessionChanger {
   private static Consumer<Session> sessionSetter;

   public static void setSessionSetter(Consumer<Session> var0) {
      sessionSetter = var0;
   }

   public static void changeUsername(String var0) {
      if (sessionSetter != null && var0 != null && !var0.isEmpty()) {
         UUID var1 = UUID.nameUUIDFromBytes(("OfflinePlayer:" + var0).getBytes());
         Session var2 = new Session(var0, var1, "", Optional.empty(), Optional.empty());
         sessionSetter.accept(var2);
      }
   }

   public static String getCurrentUsername() {
      MinecraftClient var0 = MinecraftClient.getInstance();
      return var0 != null && var0.getSession() != null ? var0.getSession().getUsername() : "";
   }
}
