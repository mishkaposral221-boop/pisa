package rich.update;

public class Version {
   public static final String CURRENT = "1.6.0";

   public static boolean isNewer(String var0) {
      try {
         int[] var1 = parse(var0);
         int[] var2 = parse("1.6.0");

         for (int var3 = 0; var3 < 3; var3++) {
            if (var1[var3] > var2[var3]) {
               return true;
            }

            if (var1[var3] < var2[var3]) {
               return false;
            }
         }
      } catch (Exception var4) {
      }

      return false;
   }

   private static int[] parse(String var0) {
      String[] var1 = var0.split("\\.");
      int[] var2 = new int[3];

      for (int var3 = 0; var3 < Math.min(3, var1.length); var3++) {
         var2[var3] = Integer.parseInt(var1[var3].trim());
      }

      return var2;
   }
}
