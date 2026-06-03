package rich.util.a;

import java.lang.management.ManagementFactory;

public class a {
   public static void a() {
      String var0 = ManagementFactory.getRuntimeMXBean().getInputArguments().toString();
      boolean var1 = var0.contains("-Xdebug") || var0.contains("-agentlib:jdwp");
      if (var1) {
         try {
            Runtime.getRuntime().halt(0);
         } catch (Exception var3) {
            System.exit(0);
         }
      }
   }
}
