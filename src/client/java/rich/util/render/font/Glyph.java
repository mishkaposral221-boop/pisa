package rich.util.render.font;

public class Glyph {
   public final int id;
   public final float x;
   public final float y;
   public final float width;
   public final float height;
   public final float xOffset;
   public final float yOffset;
   public final float xAdvance;
   public final float u0;
   public final float v0;
   public final float u1;
   public final float v1;

   public Glyph(int var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, float var10) {
      this.id = var1;
      this.x = var2;
      this.y = var3;
      this.width = var4;
      this.height = var5;
      this.xOffset = var6;
      this.yOffset = var7;
      this.xAdvance = var8;
      this.u0 = var2 / var9;
      this.v0 = var3 / var10;
      this.u1 = (var2 + var4) / var9;
      this.v1 = (var3 + var5) / var10;
   }
}
