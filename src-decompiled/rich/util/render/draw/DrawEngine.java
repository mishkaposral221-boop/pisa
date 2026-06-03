package rich.util.render.draw;

import net.minecraft.class_287;
import net.minecraft.class_4587.class_4665;
import org.joml.Matrix4f;
import org.joml.Vector4i;

public interface DrawEngine {
   void quad(Matrix4f var1, class_287 var2, float var3, float var4, float var5, float var6);

   void quad(Matrix4f var1, class_287 var2, float var3, float var4, float var5, float var6, int var7);

   void quadTexture(class_4665 var1, class_287 var2, float var3, float var4, float var5, float var6, Vector4i var7);

   void quad(Matrix4f var1, float var2, float var3, float var4, float var5, int var6);
}
