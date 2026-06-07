package rich.util.color;

import it.unimi.dsi.fastutil.chars.Char2IntArrayMap;
import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.regex.Pattern;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.ColorHelper;
import org.joml.Vector4i;
import org.lwjgl.opengl.GL11;
import rich.util.math.MathUtils;

public final class ColorUtil {
   public static final int a = new Color(64, 255, 64).getRGB();
   public static final int b = new Color(255, 255, 64).getRGB();
   public static final int c = new Color(255, 128, 32).getRGB();
   public static final int d = new Color(255, 64, 64).getRGB();
   private static final long o = 60000L;
   public static final Pattern e = Pattern.compile("(?i)§[0-9a-f-or]");
   public static Char2IntArrayMap f = new Char2IntArrayMap() {
      {
         this.put('0', 0);
         this.put('1', 170);
         this.put('2', 43520);
         this.put('3', 43690);
         this.put('4', 11141120);
         this.put('5', 11141290);
         this.put('6', 16755200);
         this.put('7', 11184810);
         this.put('8', 5592405);
         this.put('9', 5592575);
         this.put('A', 5635925);
         this.put('B', 5636095);
         this.put('C', 16733525);
         this.put('D', 16733695);
         this.put('E', 16777045);
         this.put('F', 16777215);
      }
   };
   public static final int g = c(255, 0, 0);
   public static final int h = c(0, 255, 0);
   public static final int i = c(0, 0, 255);
   public static final int j = c(255, 255, 0);
   public static final int k = m(255);
   public static final int l = m(0);
   public static final int m = b(0, 0.5F);
   public static final int n = c(255, 85, 85);

   public static int a() {
      return new Color(91, 63, 212, 255).getRGB();
   }

   public static int b() {
      return new Color(26, 26, 26, 255).getRGB();
   }

   public static int c() {
      return new Color(255, 255, 255, 255).getRGB();
   }

   public static int d() {
      return new Color(130, 100, 210, 255).getRGB();
   }

   public static int a(int var0, int var1) {
      int var2 = var0 >> 16 & 0xFF;
      int var3 = var0 >> 8 & 0xFF;
      int var4 = var0 & 0xFF;
      int var5 = var0 >> 24 & 0xFF;
      int var6 = var5 * var1 / 255;
      return var6 << 24 | var2 << 16 | var3 << 8 | var4;
   }

   public static int a(int var0, float var1) {
      return a(var0, (int)(var1 * 255.0F));
   }

   public static int a(int var0) {
      return var0 >> 16 & 0xFF;
   }

   public static int b(int var0) {
      return var0 >> 8 & 0xFF;
   }

   public static int c(int var0) {
      return var0 & 0xFF;
   }

   public static int d(int var0) {
      return var0 >> 24 & 0xFF;
   }

   public static float e(int var0) {
      return a(var0) / 255.0F;
   }

   public static float f(int var0) {
      return b(var0) / 255.0F;
   }

   public static float g(int var0) {
      return c(var0) / 255.0F;
   }

   public static float h(int var0) {
      return d(var0) / 255.0F;
   }

   public static int[] i(int var0) {
      return new int[]{a(var0), b(var0), c(var0), d(var0)};
   }

   public static int[] j(int var0) {
      return new int[]{a(var0), b(var0), c(var0)};
   }

   public static float[] k(int var0) {
      return new float[]{e(var0), f(var0), g(var0), h(var0)};
   }

   public static float[] l(int var0) {
      return new float[]{e(var0), f(var0), g(var0)};
   }

   public static int a(float var0, float var1, float var2, float var3) {
      return d(Math.round(var0 * 255.0F), Math.round(var1 * 255.0F), Math.round(var2 * 255.0F), Math.round(var3 * 255.0F));
   }

   public static int a(int var0, int var1, int var2, float var3) {
      return d(var0, var1, var2, Math.round(var3 * 255.0F));
   }

   public static int a(float var0, float var1, float var2) {
      return a(var0, var1, var2, 1.0F);
   }

   public static int b(int var0, int var1) {
      return d(var0, var0, var0, var1);
   }

   public static int b(int var0, float var1) {
      return b(var0, Math.round(var1 * 255.0F));
   }

   public static int m(int var0) {
      return c(var0, var0, var0);
   }

   public static int c(int var0, int var1) {
      return d(a(var0), b(var0), c(var0), var1);
   }

   public static int c(int var0, float var1) {
      return a(a(var0), b(var0), c(var0), var1);
   }

   public static int d(int var0, float var1) {
      return d(a(var0), b(var0), c(var0), Math.round(d(var0) * var1));
   }

   public static int d(int var0, int var1) {
      return e(var0, 2.55F * Math.min(var1, 100));
   }

   public static int e(int var0, float var1) {
      return ColorHelper.getArgb(
         (int)(ColorHelper.getAlpha(var0) * (var1 / 255.0F)), ColorHelper.getRed(var0), ColorHelper.getGreen(var0), ColorHelper.getBlue(var0)
      );
   }

   public static int a(float var0, int var1, int var2) {
      return ColorHelper.lerp(var0, var1, var2);
   }

   public static int e(int var0, int var1) {
      ByteBuffer var2 = ByteBuffer.allocateDirect(4);
      GL11.glReadPixels(var0, var1, 1, 1, 6408, 5121, var2);
      return ColorHelper.getArgb(n(var2.get()), o(var2.get()), p(var2.get()));
   }

   public static int f(int var0, int var1) {
      long var2 = System.currentTimeMillis();
      long var4 = (var2 / var0 + var1) % 360L;
      return (int)var4;
   }

   public static int g(int var0, int var1) {
      return Math.clamp(var1, 0, 255) << 24 | var0 & 16777215;
   }

   public static int a(int var0, int var1, float var2, float var3, float var4) {
      float var5 = 90.0F;
      float var6 = f(var0, var1);
      float var7 = (var6 + var1 * var5) % 360.0F;
      var7 /= 360.0F;
      var2 = Math.clamp(var2, 0.0F, 1.0F);
      var3 = Math.clamp(var3, 0.0F, 1.0F);
      int var8 = Color.HSBtoRGB(var7, var2, var3);
      int var9 = Math.max(0, Math.min(255, (int)(var4 * 255.0F)));
      return g(var8, var9);
   }

   public static int a(int var0, int var1, int var2) {
      return 0xFF000000 | var0 << 16 | var1 << 8 | var2;
   }

   public static int a(String var0) {
      int var1 = Integer.parseInt(var0.substring(1), 16);
      return h(var1, 255);
   }

   public static int h(int var0, int var1) {
      return var0 & 16777215 | var1 << 24;
   }

   public static int a(int var0, int var1, int var2, int var3) {
      int var4 = (int)((System.currentTimeMillis() / var3 + var2) % 360L);
      var4 = (var4 > 180 ? 360 - var4 : var4) + 180;
      int var5 = f(var0, var1, Math.clamp(var4 / 180.0F - 1.0F, 0.0F, 1.0F));
      float[] var6 = r(var5);
      float[] var7 = Color.RGBtoHSB((int)(var6[0] * 255.0F), (int)(var6[1] * 255.0F), (int)(var6[2] * 255.0F), null);
      var7[1] *= 1.5F;
      var7[1] = Math.min(var7[1], 1.0F);
      return Color.HSBtoRGB(var7[0], var7[1], var7[2]);
   }

   public static int a(int var0, int var1, int... var2) {
      int var3 = (int)((System.currentTimeMillis() / var0 + var1) % 360L);
      var3 = (var3 > 180 ? 360 - var3 : var3) + 180;
      int var4 = (int)(var3 / 360.0F * var2.length);
      if (var4 == var2.length) {
         var4--;
      }

      int var5 = var2[var4];
      int var6 = var2[var4 == var2.length - 1 ? 0 : var4 + 1];
      return f(var5, var6, var3 / 360.0F * var2.length - var4);
   }

   public static int a(int var0, int var1, float var2) {
      var2 = Math.min(1.0F, Math.max(0.0F, var2));
      int var3 = n(var0);
      int var4 = o(var0);
      int var5 = p(var0);
      int var6 = q(var0);
      int var7 = n(var1);
      int var8 = o(var1);
      int var9 = p(var1);
      int var10 = q(var1);
      int var11 = a(var3, var7, (double)var2);
      int var12 = a(var4, var8, (double)var2);
      int var13 = a(var5, var9, (double)var2);
      int var14 = a(var6, var10, (double)var2);
      return var14 << 24 | var11 << 16 | var12 << 8 | var13;
   }

   public static Double a(double var0, double var2, double var4) {
      return var0 + (var2 - var0) * var4;
   }

   public static int a(int var0, int var1, double var2) {
      return a((double)var0, (double)var1, (double)((float)var2)).intValue();
   }

   public static int b(int var0, int var1, float var2) {
      int var3 = var0 >> 24 & 0xFF;
      int var4 = var0 >> 16 & 0xFF;
      int var5 = var0 >> 8 & 0xFF;
      int var6 = var0 & 0xFF;
      int var7 = var1 >> 24 & 0xFF;
      int var8 = var1 >> 16 & 0xFF;
      int var9 = var1 >> 8 & 0xFF;
      int var10 = var1 & 0xFF;
      int var11 = (int)(var3 + (var7 - var3) * var2);
      int var12 = (int)(var4 + (var8 - var4) * var2);
      int var13 = (int)(var5 + (var9 - var5) * var2);
      int var14 = (int)(var6 + (var10 - var6) * var2);
      return var11 << 24 | var12 << 16 | var13 << 8 | var14;
   }

   public static int i(int var0, int var1) {
      var1 = Math.max(0, Math.min(255, var1));
      return var0 & 16777215 | var1 << 24;
   }

   public static int n(int var0) {
      return var0 >> 16 & 0xFF;
   }

   public static int j(int var0, int var1) {
      return var1 << 24 | var0 & 16777215;
   }

   public static int f(int var0, float var1) {
      return j(var0, (int)(var1 * 255.0F));
   }

   public static int o(int var0) {
      return var0 >> 8 & 0xFF;
   }

   public static int p(int var0) {
      return var0 & 0xFF;
   }

   public static int q(int var0) {
      return var0 >> 24 & 0xFF;
   }

   public static int c(int var0, int var1, float var2) {
      return d(
         Math.round(a(var0) * (e(var1) * var2)),
         Math.round(b(var0) * (f(var1) * var2)),
         Math.round(c(var0) * (g(var1) * var2)),
         Math.round(d(var0) * (h(var1) * var2))
      );
   }

   public static int d(int var0, int var1, float var2) {
      return d(
         Math.round(a(var0) * (e(var1) * var2)),
         Math.round(b(var0) * (f(var1) * var2)),
         Math.round(c(var0) * (g(var1) * var2)),
         Math.round(d(var0) * (h(var1) * var2))
      );
   }

   public static int g(int var0, float var1) {
      return d(Math.round(a(var0) * var1), Math.round(b(var0) * var1), Math.round(c(var0) * var1), d(var0));
   }

   public static int h(int var0, float var1) {
      return d(Math.min(255, Math.round(a(var0) / var1)), Math.min(255, Math.round(b(var0) / var1)), Math.min(255, Math.round(c(var0) / var1)), d(var0));
   }

   public static int e(int var0, int var1, float var2) {
      float var3 = MathHelper.clamp(var2, 0.0F, 1.0F);
      return d(
         MathHelper.lerp(var3, a(var0), a(var1)),
         MathHelper.lerp(var3, b(var0), b(var1)),
         MathHelper.lerp(var3, c(var0), c(var1)),
         MathHelper.lerp(var3, d(var0), d(var1))
      );
   }

   public static Vector4i a(Vector4i var0, float var1, float var2) {
      return new Vector4i(a(var0.x, var1, var2), a(var0.y, var1, var2), a(var0.w, var1, var2), a(var0.z, var1, var2));
   }

   public static int a(int var0, float var1, float var2) {
      return d(a(var0), Math.min(255, Math.round(b(var0) / var1)), Math.min(255, Math.round(c(var0) / var1)), Math.round(d(var0) * var2));
   }

   public static int i(int var0, float var1) {
      return d(a(var0), Math.min(255, Math.round(b(var0) / var1)), Math.min(255, Math.round(c(var0) / var1)), d(var0));
   }

   public static int j(int var0, float var1) {
      return d(Math.min(255, Math.round(b(var0) / var1)), b(var0), Math.min(255, Math.round(c(var0) / var1)), d(var0));
   }

   public static int b(int var0, int var1, int var2, int var3) {
      return d(var0, var1, var2, var3);
   }

   public static int[] b(int var0, int var1, int var2) {
      int[] var3 = new int[var2];

      for (int var4 = 0; var4 < var2; var4++) {
         float var5 = (float)var4 / (var2 - 1);
         var3[var4] = e(var0, var1, var5);
      }

      return var3;
   }

   public static float[] r(int var0) {
      return new float[]{(var0 >> 16 & 0xFF) / 255.0F, (var0 >> 8 & 0xFF) / 255.0F, (var0 & 0xFF) / 255.0F, (var0 >> 24 & 0xFF) / 255.0F};
   }

   public static int f(int var0, int var1, float var2) {
      float[] var3 = r(var0);
      float[] var4 = r(var1);
      return b(
         (int)MathUtils.interpolate(var3[0] * 255.0F, var4[0] * 255.0F, var2),
         (int)MathUtils.interpolate(var3[1] * 255.0F, var4[1] * 255.0F, var2),
         (int)MathUtils.interpolate(var3[2] * 255.0F, var4[2] * 255.0F, var2),
         (int)MathUtils.interpolate(var3[3] * 255.0F, var4[3] * 255.0F, var2)
      );
   }

   public static int[] s(int var0) {
      return new int[]{var0, var0, var0, var0, var0, var0, var0, var0, var0};
   }

   public static int[] t(int var0) {
      return new int[]{var0, var0, var0, var0, var0, var0, var0, var0};
   }

   public static int b(int var0, int var1, float var2, float var3, float var4) {
      int var5 = (int)((System.currentTimeMillis() / var0 + var1) % 360L);
      float var6 = var5 / 360.0F;
      int var7 = Color.HSBtoRGB(var6, var2, var3);
      return d(a(var7), b(var7), c(var7), Math.round(var4 * 255.0F));
   }

   public static int c(int var0, int var1, int var2, int var3) {
      int var4 = (int)((System.currentTimeMillis() / var0 + var1) % 360L);
      var4 = var4 >= 180 ? 360 - var4 : var4;
      return e(var2, var3, var4 / 180.0F);
   }

   public static int b(float var0, float var1, float var2, float var3) {
      return (int)(MathHelper.clamp(var3, 0.0F, 1.0F) * 255.0F) << 24
         | (int)(MathHelper.clamp(var0, 0.0F, 1.0F) * 255.0F) << 16
         | (int)(MathHelper.clamp(var1, 0.0F, 1.0F) * 255.0F) << 8
         | (int)(MathHelper.clamp(var2, 0.0F, 1.0F) * 255.0F);
   }

   public static int d(int var0, int var1, int var2, int var3) {
      return e(var0, var1, var2, var3);
   }

   public static int c(int var0, int var1, int var2) {
      return e(var0, var1, var2, 255);
   }

   private static int e(int var0, int var1, int var2, int var3) {
      return MathHelper.clamp(var3, 0, 255) << 24
         | MathHelper.clamp(var0, 0, 255) << 16
         | MathHelper.clamp(var1, 0, 255) << 8
         | MathHelper.clamp(var2, 0, 255);
   }

   private static String f(int var0, int var1, int var2, int var3) {
      return var0 + "," + var1 + "," + var2 + "," + var3;
   }

   public static String u(int var0) {
      return "⏏" + var0 + "⏏";
   }

   public static int k(int var0, float var1) {
      int var2 = var0 >> 24 & 0xFF;
      int var3 = (int)((var0 >> 16 & 0xFF) * var1);
      int var4 = (int)((var0 >> 8 & 0xFF) * var1);
      int var5 = (int)((var0 & 0xFF) * var1);
      return var2 << 24 | var3 << 16 | var4 << 8 | var5;
   }

   public static int l(int var0, float var1) {
      int var2 = var0 >> 24 & 0xFF;
      int var3 = Math.min(255, (int)((var0 >> 16 & 0xFF) * var1));
      int var4 = Math.min(255, (int)((var0 >> 8 & 0xFF) * var1));
      int var5 = Math.min(255, (int)((var0 & 0xFF) * var1));
      return var2 << 24 | var3 << 16 | var4 << 8 | var5;
   }

   public static int b(float var0, float var1, float var2) {
      float var3 = var2 * var1;
      float var4 = var3 * (1.0F - Math.abs(var0 * 6.0F % 2.0F - 1.0F));
      float var5 = var2 - var3;
      float var6;
      float var7;
      float var8;
      if (var0 < 0.16666667F) {
         var6 = var3;
         var7 = var4;
         var8 = 0.0F;
      } else if (var0 < 0.33333334F) {
         var6 = var4;
         var7 = var3;
         var8 = 0.0F;
      } else if (var0 < 0.5F) {
         var6 = 0.0F;
         var7 = var3;
         var8 = var4;
      } else if (var0 < 0.6666667F) {
         var6 = 0.0F;
         var7 = var4;
         var8 = var3;
      } else if (var0 < 0.8333333F) {
         var6 = var4;
         var7 = 0.0F;
         var8 = var3;
      } else {
         var6 = var3;
         var7 = 0.0F;
         var8 = var4;
      }

      int var9 = (int)((var6 + var5) * 255.0F);
      int var10 = (int)((var7 + var5) * 255.0F);
      int var11 = (int)((var8 + var5) * 255.0F);
      return 0xFF000000 | var9 << 16 | var10 << 8 | var11;
   }

   public static int c(float var0, float var1, float var2, float var3) {
      int var4 = b(var0, var1, var2);
      int var5 = (int)(var3 * 255.0F);
      return var5 << 24 | var4 & 16777215;
   }

   public static String b(String var0) {
      return var0 != null && !var0.isEmpty() ? e.matcher(var0).replaceAll("") : null;
   }

   public static int e() {
      return new Color(20, 20, 24, 255).getRGB();
   }

   public static int a(float var0) {
      return d(new Color(1710623).getRGB(), var0);
   }

   public static int b(float var0) {
      return d(new Color(1973798).getRGB(), var0);
   }

   public static int c(float var0) {
      return d(new Color(0, 0, 0, 228).getRGB(), var0);
   }

   public static int d(float var0) {
      return d(new Color(1579038).getRGB(), var0);
   }

   public static int e(float var0) {
      return d(f(), var0);
   }

   public static int f() {
      return new Color(255, 255, 255, 255).getRGB();
   }

   public static int g() {
      return new Color(175, 175, 175, 255).getRGB();
   }

   public static int h() {
      return new Color(5635925).getRGB();
   }

   public static int a(float var0, float var1) {
      return h(d(i(), var0), var1);
   }

   public static int f(float var0) {
      return d(i(), var0);
   }

   public static int i() {
      return new Color(3618630).getRGB();
   }

   private ColorUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static class a {
      public static float[] a(int var0) {
         return new float[]{(var0 >> 16 & 0xFF) / 255.0F, (var0 >> 8 & 0xFF) / 255.0F, (var0 & 0xFF) / 255.0F, (var0 >> 24 & 0xFF) / 255.0F};
      }

      public static int a(int var0, int var1, int var2, int var3) {
         return var3 << 24 | var0 << 16 | var1 << 8 | var2;
      }

      public static int a(int var0, int var1, int var2) {
         return 0xFF000000 | var0 << 16 | var1 << 8 | var2;
      }

      public static int b(int var0) {
         return var0 >> 16 & 0xFF;
      }

      public static int c(int var0) {
         return var0 >> 8 & 0xFF;
      }

      public static int d(int var0) {
         return var0 & 0xFF;
      }

      public static int e(int var0) {
         return var0 >> 24 & 0xFF;
      }
   }
}
