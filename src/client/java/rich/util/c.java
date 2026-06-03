package rich.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import rich.Initialization;
import rich.modules.module.ModuleStructure;

public final class c {
   private static final ConcurrentMap<Class<? extends ModuleStructure>, ModuleStructure> keyCodec = new ConcurrentHashMap<>();

   public static <T extends ModuleStructure> T keyCodec(Class<T> var0) {
      return (T)var0.cast(
         ColorUtil.computeIfAbsent(var0, var0x -> Initialization.getInstance().getManager().getModuleProvider().getName((Class<? extends ModuleStructure>)var0x))
      );
   }

   public static <T extends ModuleStructure> T keyCodec(String var0) {
      return Initialization.getInstance().getManager().getModuleProvider().getName(var0);
   }

   private c() {
      throw new UnsupportedOperationException("This is keyCodec utility class and cannot be instantiated");
   }
}
