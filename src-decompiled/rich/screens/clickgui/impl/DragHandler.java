package rich.screens.clickgui.impl;

import org.lwjgl.glfw.GLFW;
import rich.IMinecraft;

public class DragHandler implements IMinecraft {
   private float offsetX = 0.0F;
   private float offsetY = 0.0F;
   private float targetOffsetX = 0.0F;
   private float targetOffsetY = 0.0F;
   private boolean dragging = false;
   private double dragStartX = 0.0;
   private double dragStartY = 0.0;
   private float dragStartOffsetX = 0.0F;
   private float dragStartOffsetY = 0.0F;
   private static final float ANIMATION_SPEED = 10.0F;
   private long lastUpdateTime = System.currentTimeMillis();

   public void update(double var1, double var3) {
      long var5 = System.currentTimeMillis();
      float var7 = Math.min((float)(var5 - this.lastUpdateTime) / 1000.0F, 0.1F);
      this.lastUpdateTime = var5;
      if (this.dragging) {
         if (GLFW.glfwGetMouseButton(mc.method_22683().method_4490(), 2) != 1) {
            this.dragging = false;
         } else {
            this.targetOffsetX = this.dragStartOffsetX + (float)(var1 - this.dragStartX);
            this.targetOffsetY = this.dragStartOffsetY + (float)(var3 - this.dragStartY);
            this.offsetX = this.targetOffsetX;
            this.offsetY = this.targetOffsetY;
         }
      }

      float var8 = this.targetOffsetX - this.offsetX;
      float var9 = this.targetOffsetY - this.offsetY;
      if (Math.abs(var8) > 0.01F) {
         this.offsetX += var8 * 10.0F * var7;
      } else {
         this.offsetX = this.targetOffsetX;
      }

      if (Math.abs(var9) > 0.01F) {
         this.offsetY += var9 * 10.0F * var7;
      } else {
         this.offsetY = this.targetOffsetY;
      }
   }

   public boolean startDrag(double var1, double var3, float var5, float var6, int var7, int var8) {
      if (var1 >= var5 && var1 <= var5 + var7 && var3 >= var6 && var3 <= var6 + var8) {
         this.dragging = true;
         this.dragStartX = var1;
         this.dragStartY = var3;
         this.dragStartOffsetX = this.targetOffsetX;
         this.dragStartOffsetY = this.targetOffsetY;
         return true;
      } else {
         return false;
      }
   }

   public void reset() {
      this.targetOffsetX = 0.0F;
      this.targetOffsetY = 0.0F;
   }

   public void stopDrag() {
      this.dragging = false;
   }

   public boolean isResetNeeded(int var1, int var2) {
      boolean var3 = (var2 & 2) != 0;
      boolean var4 = (var2 & 4) != 0;
      boolean var5 = var1 == 341 || var1 == 345;
      boolean var6 = var1 == 342 || var1 == 346;
      return var5 && var4 || var6 && var3;
   }

   public float getOffsetX() {
      return this.offsetX;
   }

   public float getOffsetY() {
      return this.offsetY;
   }

   public float getTargetOffsetX() {
      return this.targetOffsetX;
   }

   public float getTargetOffsetY() {
      return this.targetOffsetY;
   }

   public boolean isDragging() {
      return this.dragging;
   }

   public double getDragStartX() {
      return this.dragStartX;
   }

   public double getDragStartY() {
      return this.dragStartY;
   }

   public float getDragStartOffsetX() {
      return this.dragStartOffsetX;
   }

   public float getDragStartOffsetY() {
      return this.dragStartOffsetY;
   }

   public long getLastUpdateTime() {
      return this.lastUpdateTime;
   }

   public void setOffsetX(float var1) {
      this.offsetX = var1;
   }

   public void setOffsetY(float var1) {
      this.offsetY = var1;
   }

   public void setTargetOffsetX(float var1) {
      this.targetOffsetX = var1;
   }

   public void setTargetOffsetY(float var1) {
      this.targetOffsetY = var1;
   }

   public void setDragging(boolean var1) {
      this.dragging = var1;
   }

   public void setDragStartX(double var1) {
      this.dragStartX = var1;
   }

   public void setDragStartY(double var1) {
      this.dragStartY = var1;
   }

   public void setDragStartOffsetX(float var1) {
      this.dragStartOffsetX = var1;
   }

   public void setDragStartOffsetY(float var1) {
      this.dragStartOffsetY = var1;
   }

   public void setLastUpdateTime(long var1) {
      this.lastUpdateTime = var1;
   }
}
