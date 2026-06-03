package rich.util.render.font;

import rich.Initialization;

public class Font {
   private final String name;

   public Font(String var1) {
      this.name = var1;
   }

   public void draw(String var1, float var2, float var3, float var4, int var5) {
      Initialization.getInstance().getManager().getRenderCore().getFontRenderer().drawText(this.name, var1, var2, var3, var4, var5);
   }

   public void drawCentered(String var1, float var2, float var3, float var4, int var5) {
      Initialization.getInstance().getManager().getRenderCore().getFontRenderer().drawCenteredText(this.name, var1, var2, var3, var4, var5);
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
