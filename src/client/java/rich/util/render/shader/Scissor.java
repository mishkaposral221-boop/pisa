package rich.util.render.shader;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.GL11;
import rich.util.profiler.FrameProfiler;

public class Scissor {
   private static final MinecraftClient mc = MinecraftClient.getInstance();
   private static final Deque<int[]> scissorStack = new ArrayDeque<>();
   private static boolean glScissorEnabled = false;
   private static int lastX = Integer.MIN_VALUE;
   private static int lastY = Integer.MIN_VALUE;
   private static int lastW = Integer.MIN_VALUE;
   private static int lastH = Integer.MIN_VALUE;

   public static void enable(float var0, float var1, float var2, float var3, float var4) {
      FrameProfiler profiler = FrameProfiler.getInstance();
      boolean prof = profiler.isEnabled();
      if (prof) {
         profiler.begin("Scissor/enable");
      }

      try {
         int var5 = mc.getWindow().getHeight();
         int var6 = (int)(var0 * var4);
         int var7 = (int)(var5 - (var1 + var3) * var4);
         int var8 = (int)(var2 * var4);
         int var9 = (int)(var3 * var4);
         var6 = Math.max(0, var6);
         var7 = Math.max(0, var7);
         var8 = Math.max(0, var8);
         var9 = Math.max(0, var9);
         if (!scissorStack.isEmpty()) {
            int[] var10 = scissorStack.peek();
            int var11 = var10[0];
            int var12 = var10[1];
            int var13 = var11 + var10[2];
            int var14 = var12 + var10[3];
            int var15 = var6 + var8;
            int var16 = var7 + var9;
            var6 = Math.max(var6, var11);
            var7 = Math.max(var7, var12);
            var15 = Math.min(var15, var13);
            var16 = Math.min(var16, var14);
            var8 = Math.max(0, var15 - var6);
            var9 = Math.max(0, var16 - var7);
         }

         scissorStack.push(new int[]{var6, var7, var8, var9});
         applyScissor(var6, var7, var8, var9);
      } finally {
         if (prof) {
            profiler.end();
         }
      }
   }

   public static void enable(float var0, float var1, float var2, float var3) {
      int var4 = mc.getWindow().calculateScaleFactor((Integer)mc.options.getGuiScale().getValue(), mc.forcesUnicodeFont());
      enable(var0, var1, var2, var3, var4);
   }

   public static void disable() {
      FrameProfiler profiler = FrameProfiler.getInstance();
      boolean prof = profiler.isEnabled();
      if (prof) {
         profiler.begin("Scissor/disable");
      }

      try {
         if (!scissorStack.isEmpty()) {
            scissorStack.pop();
         }

         if (scissorStack.isEmpty()) {
            if (glScissorEnabled) {
               GL11.glDisable(3089);
               glScissorEnabled = false;
            }

            invalidateCache();
         } else {
            int[] var0 = scissorStack.peek();
            applyScissor(var0[0], var0[1], var0[2], var0[3]);
         }
      } finally {
         if (prof) {
            profiler.end();
         }
      }
   }

   public static void reset() {
      scissorStack.clear();
      if (glScissorEnabled) {
         GL11.glDisable(3089);
         glScissorEnabled = false;
      }

      invalidateCache();
   }

   private static void invalidateCache() {
      lastX = Integer.MIN_VALUE;
      lastY = Integer.MIN_VALUE;
      lastW = Integer.MIN_VALUE;
      lastH = Integer.MIN_VALUE;
   }

   private static void applyScissor(int x, int y, int w, int h) {
      if (!glScissorEnabled) {
         GL11.glEnable(3089);
         glScissorEnabled = true;
         // The managed render backend (and Minecraft's own render passes) can
         // change the GL scissor box while our scissor test is disabled. Our
         // cached last* values are therefore stale whenever we re-enable, so we
         // must force glScissor to be re-uploaded for the first box of a session.
         invalidateCache();
      }

      if (x != lastX || y != lastY || w != lastW || h != lastH) {
         GL11.glScissor(x, y, w, h);
         lastX = x;
         lastY = y;
         lastW = w;
         lastH = h;
      }
   }
}
