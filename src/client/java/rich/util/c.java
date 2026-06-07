package rich.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import rich.Initialization;
import rich.modules.module.ModuleStructure;

public final class c {
   private static final ConcurrentMap<Class<? extends ModuleStructure>, ModuleStructure> a = new ConcurrentHashMap<>();

   public static <T extends ModuleStructure> T a(Class<T> var0) {
      return var0.cast(
         a.computeIfAbsent(var0, var0x -> Initialization.getInstance().getManager().getModuleProvider().get((Class<? extends ModuleStructure>)var0x))
      );
   }

   public static <T extends ModuleStructure> T a(String var0) {
      return Initialization.getInstance().getManager().getModuleProvider().get(var0);
   }

   private c() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
