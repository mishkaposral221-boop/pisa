package rich.util.render.shader;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.class_310;
import org.lwjgl.opengl.GL11;

public class Scissor {
   private static final class_310 mc = class_310.method_1551();
   private static final Deque<int[]> scissorStack = new ArrayDeque<>();

   public static void enable(float var0, float var1, float var2, float var3, float var4) {
      int var5 = mc.method_22683().method_4507();
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
      GL11.glEnable(3089);
      GL11.glScissor(var6, var7, var8, var9);
   }

   public static void enable(float var0, float var1, float var2, float var3) {
      int var4 = mc.method_22683().method_4476((Integer)mc.field_1690.method_42474().method_41753(), mc.method_1573());
      enable(var0, var1, var2, var3, var4);
   }

   public static void disable() {
      if (!scissorStack.isEmpty()) {
         scissorStack.pop();
      }

      if (scissorStack.isEmpty()) {
         GL11.glDisable(3089);
      } else {
         int[] var0 = scissorStack.peek();
         GL11.glScissor(var0[0], var0[1], var0[2], var0[3]);
      }
   }

   public static void reset() {
      scissorStack.clear();
      GL11.glDisable(3089);
   }
}
