package rich.modules.impl.render.chinahat;

import net.minecraft.class_10055;
import net.minecraft.class_11659;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_591;
import net.minecraft.class_7833;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import rich.modules.impl.render.ChinaHat;
import rich.util.a;
import rich.util.render.clientpipeline.ClientPipelines;

public class ChinaHatFeatureRenderer extends class_3887<class_10055, class_591> {
   private static final float PI2 = (float) (Math.PI * 2);
   private static final int CIRCLE_SEGMENTS = 720;
   private static final int OUTLINE_SEGMENTS = 360;

   public ChinaHatFeatureRenderer(class_3883<class_10055, class_591> var1) {
      super(var1);
   }

   public void render(class_4587 var1, class_11659 var2, int var3, class_10055 var4, float var5, float var6) {
      class_310 var7 = class_310.method_1551();
      ChinaHat var8 = ChinaHat.getInstance();
      if (var8 != null && var8.isState()) {
         if (var7.field_1724 != null) {
            if (this.isLocalPlayer(var4, var7)) {
               var1.method_22903();
               ((class_591)this.method_17165()).field_3398.method_22703(var1);
               var1.method_22907(class_7833.field_40718.rotationDegrees(180.0F));
               var1.method_22907(class_7833.field_40716.rotationDegrees(90.0F));
               var1.method_46416(0.0F, 0.5F, 0.0F);
               class_4598 var9 = var7.method_22940().method_23000();
               boolean var10 = var8.style.getSelected().equals("Сеточный");
               if (var10) {
                  this.renderGrid(var1, var9, var8);
                  this.renderOutline(var1, var9, var8);
               } else {
                  this.renderFlatHat(var1, var9, var8);
                  this.renderOutline(var1, var9, var8);
               }

               var9.method_22993();
               var1.method_22909();
            }
         }
      }
   }

   private boolean isLocalPlayer(class_10055 var1, class_310 var2) {
      try {
         if (var1.field_53528 == var2.field_1724.method_5628()) {
            return true;
         }
      } catch (Exception var5) {
      }

      try {
         if (var1.field_53525 != null && var2.field_1724.method_5477() != null) {
            return var1.field_53525.getString().equals(var2.field_1724.method_5477().getString());
         }
      } catch (Exception var4) {
      }

      return false;
   }

   private void renderGrid(class_4587 var1, class_4597 var2, ChinaHat var3) {
      class_4588 var4 = var2.method_73477(ClientPipelines.CHINA_HAT_GRID);
      Matrix4f var5 = var1.method_23760().method_23761();
      float var6 = 0.55F;
      float var7 = 0.31F;
      float var8 = 5.0F;
      short var9 = 220;
      byte var10 = 32;

      for (int var11 = 0; var11 < var10; var11++) {
         float var12 = var11 * (float) (Math.PI * 2) / var10;
         float var13 = -class_3532.method_15374(var12) * var6;
         float var14 = class_3532.method_15362(var12) * var6;
         int var15 = this.getGradientColor(var11 * (720 / var10), 720, var3, var8);
         var15 = a.c(var15, var9);
         var4.method_22918(var5, 0.0F, var7, 0.0F).method_39415(var15);
         var4.method_22918(var5, var13, 0.0F, var14).method_39415(var15);
      }

      byte var25 = 10;
      byte var26 = 64;

      for (int var27 = 1; var27 <= var25; var27++) {
         float var28 = (float)var27 / var25;
         float var30 = var6 * var28;
         float var16 = var7 * (1.0F - var28);

         for (int var17 = 0; var17 < var26; var17++) {
            float var18 = var17 * (float) (Math.PI * 2) / var26;
            float var19 = (var17 + 1) * (float) (Math.PI * 2) / var26;
            int var20 = this.getGradientColor(var17 * (720 / var26), 720, var3, var8);
            var20 = a.c(var20, var9);
            float var21 = -class_3532.method_15374(var18) * var30;
            float var22 = class_3532.method_15362(var18) * var30;
            float var23 = -class_3532.method_15374(var19) * var30;
            float var24 = class_3532.method_15362(var19) * var30;
            var4.method_22918(var5, var21, var16, var22).method_39415(var20);
            var4.method_22918(var5, var23, var16, var24).method_39415(var20);
         }
      }
   }

   private void renderFlatHat(class_4587 var1, class_4597 var2, ChinaHat var3) {
      class_4588 var4 = var2.method_73477(ClientPipelines.CHINA_HAT);
      Matrix4f var5 = var1.method_23760().method_23761();
      float var6 = 0.55F;
      float var7 = 0.31F;
      short var8 = 185;
      float var9 = 5.0F;
      int var10 = this.getGradientColor(0, 720, var3, var9);
      var10 = a.c(var10, var8);
      var4.method_22918(var5, 0.0F, var7, 0.0F).method_39415(var10);

      for (int var11 = 0; var11 <= 720; var11++) {
         int var12 = this.getGradientColor(var11, 720, var3, var9);
         var12 = a.c(var12, var8);
         float var13 = var11 * (float) (Math.PI * 2) / 720.0F;
         float var14 = -class_3532.method_15374(var13) * var6;
         float var15 = class_3532.method_15362(var13) * var6;
         var4.method_22918(var5, var14, 0.0F, var15).method_39415(var12);
      }

      for (int var17 = 720; var17 >= 0; var17--) {
         int var19 = this.getGradientColor(var17, 720, var3, var9);
         var19 = a.c(var19, var8);
         float var21 = var17 * (float) (Math.PI * 2) / 720.0F;
         float var22 = -class_3532.method_15374(var21) * var6;
         float var23 = class_3532.method_15362(var21) * var6;
         var4.method_22918(var5, var22, 0.0F, var23).method_39415(var19);
      }

      var4.method_22918(var5, 0.0F, var7, 0.0F).method_39415(var10);
   }

   private void renderOutline(class_4587 var1, class_4597 var2, ChinaHat var3) {
      class_4588 var4 = var2.method_73477(ClientPipelines.CHINA_HAT_OUTLINE);
      Matrix4f var5 = var1.method_23760().method_23761();
      float var6 = 0.55F;
      float var7 = 5.0F;
      short var8 = 255;

      for (int var9 = 0; var9 <= 360; var9++) {
         int var10 = this.getGradientColor(var9 * 2, 720, var3, var7);
         var10 = a.c(var10, var8);
         float var11 = var9 * (float) (Math.PI * 2) / 360.0F;
         float var12 = -class_3532.method_15374(var11) * var6;
         float var13 = class_3532.method_15362(var11) * var6;
         var4.method_22918(var5, var12, 0.0F, var13).method_39415(var10);
      }
   }

   private int getGradientColor(int var1, int var2, ChinaHat var3, float var4) {
      long var5 = System.currentTimeMillis();
      float var7 = (float)var5 / (1000.0F / var4) % var2;
      int var8 = (int)((var1 + var7) % var2);
      float var9 = (float)var8 / var2;
      String var10 = var3.colorMode.getSelected();
      if (var10.equals("1 Color")) {
         return var3.color1.getColor();
      }

      if (var10.equals("2 Colors")) {
         int var16 = var3.color1.getColor();
         int var18 = var3.color2.getColor();
         return var9 < 0.5F ? a.a(var16, var18, var9 * 2.0F) : a.a(var18, var16, (var9 - 0.5F) * 2.0F);
      }

      if (var10.equals("3 Colors")) {
         int var15 = var3.color1.getColor();
         int var17 = var3.color2.getColor();
         int var19 = var3.color3.getColor();
         if (var9 < 0.333F) {
            return a.a(var15, var17, var9 * 3.0F);
         } else {
            return var9 < 0.666F ? a.a(var17, var19, (var9 - 0.333F) * 3.0F) : a.a(var19, var15, (var9 - 0.666F) * 3.0F);
         }
      } else if (var10.equals("4 Colors")) {
         int var11 = var3.color1.getColor();
         int var12 = var3.color2.getColor();
         int var13 = var3.color3.getColor();
         int var14 = var3.color4.getColor();
         if (var9 < 0.25F) {
            return a.a(var11, var12, var9 * 4.0F);
         } else if (var9 < 0.5F) {
            return a.a(var12, var13, (var9 - 0.25F) * 4.0F);
         } else {
            return var9 < 0.75F ? a.a(var13, var14, (var9 - 0.5F) * 4.0F) : a.a(var14, var11, (var9 - 0.75F) * 4.0F);
         }
      } else {
         return var3.color1.getColor();
      }
   }
}
