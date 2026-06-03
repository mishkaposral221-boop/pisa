package rich.util.a;

public final class b {
   private static final long a = 5941776592210322721L;

   public static String a(long[] var0) {
      byte[] var1 = a(5941776592210322721L);
      byte[] var2 = new byte[var0.length * 8];
      int var3 = 0;

      for (int var4 = 0; var4 < var0.length; var4++) {
         byte[] var5 = a(var0[var4]);

         for (int var6 = 0; var6 < 8; var6++) {
            byte var7 = (byte)(var5[var6] ^ var1[(var4 * 8 + var6) % 8]);
            if (var7 == 0) {
               break;
            }

            var2[var3++] = var7;
         }
      }

      return new String(var2, 0, var3);
   }

   private static byte[] a(long var0) {
      return new byte[]{
         (byte)(var0 >> 56), (byte)(var0 >> 48), (byte)(var0 >> 40), (byte)(var0 >> 32), (byte)(var0 >> 24), (byte)(var0 >> 16), (byte)(var0 >> 8), (byte)var0
      };
   }

   public static long[] a(String var0) {
      byte[] var1 = a(5941776592210322721L);
      byte[] var2 = var0.getBytes();
      int var3 = (var2.length + 7) / 8;
      long[] var4 = new long[var3];

      for (int var5 = 0; var5 < var3; var5++) {
         long var6 = 0L;

         for (int var8 = 0; var8 < 8; var8++) {
            int var9 = var5 * 8 + var8;
            byte var10 = var9 < var2.length ? var2[var9] : 0;
            byte var11 = (byte)(var10 ^ var1[(var5 * 8 + var8) % 8]);
            var6 |= (long)(var11 & 0xFF) << 56 - var8 * 8;
         }

         var4[var5] = var6;
      }

      return var4;
   }
}
