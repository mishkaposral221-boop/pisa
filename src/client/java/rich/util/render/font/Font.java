package rich.util.render.font;

import rich.Initialization;
import rich.util.render.Render2D;

public class Font {
   private final String name;

   public Font(String var1) {
      this.name = var1;
   }

   public void draw(String var1, float var2, float var3, float var4, int var5) {
      Initialization.getInstance()
         .getManager()
         .getRenderCore()
         .getFontRenderer()
         .drawText(this.name, var1, Render2D.scaleX(var2), Render2D.scaleY(var3), Render2D.scaleSize(var4), var5);
   }

   public void drawCentered(String var1, float var2, float var3, float var4, int var5) {
      Initialization.getInstance()
         .getManager()
         .getRenderCore()
         .getFontRenderer()
         .drawCenteredText(this.name, var1, Render2D.scaleX(var2), Render2D.scaleY(var3), Render2D.scaleSize(var4), var5);
   }

   public float getWidth(String var1, float var2) {
      return Initialization.getInstance().getManager().getRenderCore().getFontRenderer().getTextWidth(this.name, var1, var2);
   }

   public float getHeight(float var1) {
      return Initialization.getInstance().getManager().getRenderCore().getFontRenderer().getLineHeight(this.name, var1);
   }

   public String getName() {
      return this.name;
   }
}
