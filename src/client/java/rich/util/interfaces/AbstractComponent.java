package rich.util.interfaces;

import rich.IMinecraft;

public abstract class AbstractComponent implements IMinecraft, Component, ResizableMovable {
   public float x;
   public float y;
   public float width;
   public float height;
   public double scroll = 0.0;
   public double smoothedScroll = 0.0;

   @Override
   public ResizableMovable position(float var1, float var2) {
      this.x = var1;
      this.y = var2;
      return this;
   }

   @Override
   public ResizableMovable size(float var1, float var2) {
      this.width = var1;
      this.height = var2;
      return this;
   }

   @Override
   public void tick() {
   }

   @Override
   public boolean mouseClicked(double var1, double var3, int var5) {
      return false;
   }

   @Override
   public boolean mouseReleased(double var1, double var3, int var5) {
      return false;
   }

   @Override
   public boolean mouseDragged(double var1, double var3, int var5, double var6, double var8) {
      return false;
   }

   @Override
   public boolean mouseScrolled(double var1, double var3, double var5) {
      return false;
   }

   @Override
   public boolean keyPressed(int var1, int var2, int var3) {
      return false;
   }

   @Override
   public boolean charTyped(char var1, int var2) {
      return false;
   }

   @Override
   public boolean isHover(double var1, double var3) {
      return false;
   }
}
