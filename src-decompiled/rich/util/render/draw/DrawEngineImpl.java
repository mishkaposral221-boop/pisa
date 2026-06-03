package rich.util.render.draw;

import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_4587.class_4665;
import org.joml.Matrix4f;
import org.joml.Vector4i;
import rich.IMinecraft;

public class DrawEngineImpl implements IMinecraft, DrawEngine {
   @Override
   public void quad(Matrix4f var1, class_287 var2, float var3, float var4, float var5, float var6) {
      var2.method_22918(var1, var3, var4, 0.0F);
      var2.method_22918(var1, var3, var4 + var6, 0.0F);
      var2.method_22918(var1, var3 + var5, var4 + var6, 0.0F);
      var2.method_22918(var1, var3 + var5, var4, 0.0F);
   }

   @Override
   public void quad(Matrix4f var1, class_287 var2, float var3, float var4, float var5, float var6, int var7) {
      var2.method_22918(var1, var3, var4, 0.0F).method_39415(var7);
      var2.method_22918(var1, var3, var4 + var6, 0.0F).method_39415(var7);
      var2.method_22918(var1, var3 + var5, var4 + var6, 0.0F).method_39415(var7);
      var2.method_22918(var1, var3 + var5, var4, 0.0F).method_39415(var7);
   }

   @Override
   public void quad(Matrix4f var1, float var2, float var3, float var4, float var5, int var6) {
      class_287 var7 = tessellator.method_60827(class_5596.field_27382, class_290.field_1575);
      var7.method_22918(var1, var2, var3 + var5, 0.0F).method_22913(0.0F, 0.0F).method_39415(var6);
      var7.method_22918(var1, var2 + var4, var3 + var5, 0.0F).method_22913(0.0F, 1.0F).method_39415(var6);
      var7.method_22918(var1, var2 + var4, var3, 0.0F).method_22913(1.0F, 1.0F).method_39415(var6);
      var7.method_22918(var1, var2, var3, 0.0F).method_22913(1.0F, 0.0F).method_39415(var6);
   }

   @Override
   public void quadTexture(class_4665 var1, class_287 var2, float var3, float var4, float var5, float var6, Vector4i var7) {
      var2.method_56824(var1, var3, var4 + var6, 0.0F).method_22913(0.0F, 0.0F).method_39415(var7.x);
      var2.method_56824(var1, var3 + var5, var4 + var6, 0.0F).method_22913(0.0F, 1.0F).method_39415(var7.y);
      var2.method_56824(var1, var3 + var5, var4, 0.0F).method_22913(1.0F, 1.0F).method_39415(var7.w);
      var2.method_56824(var1, var3, var4, 0.0F).method_22913(1.0F, 0.0F).method_39415(var7.z);
   }
}
