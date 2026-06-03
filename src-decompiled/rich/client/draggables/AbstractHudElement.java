package rich.client.draggables;

import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_408;
import net.minecraft.class_437;
import rich.modules.impl.render.ClickGuiSettings;
import rich.util.animations.Animation;
import rich.util.animations.Decelerate;
import rich.util.animations.Direction;

public abstract class AbstractHudElement implements HudElement {
   protected int x;
   protected int y;
   protected int width;
   protected int height;
   protected String name;
   protected boolean enabled = true;
   protected boolean draggable = true;
   protected final class_310 mc = class_310.method_1551();
   protected final Animation scaleAnimation = new Decelerate().setMs(300).setValue(1.0);
   protected float lastTickDelta = 0.0F;

   public AbstractHudElement(String var1, int var2, int var3, int var4, int var5, boolean var6) {
      this.name = var1;
      this.x = var2;
      this.y = var3;
      this.width = var4;
      this.height = var5;
      this.draggable = var6;
   }

   @Override
   public void render(class_332 var1, float var2) {
      if (this.visible()) {
         this.lastTickDelta = var2;
         this.scaleAnimation.update();
         int var3 = (int)(this.scaleAnimation.getOutput().floatValue() * 255.0F);
         ClickGuiSettings var4 = ClickGuiSettings.getInstance();
         if (var4 != null) {
            var3 = (int)(var3 * var4.opacity.getValue());
         }

         if (var3 > 0) {
            this.drawDraggable(var1, var3);
         }
      }
   }

   public abstract void drawDraggable(class_332 var1, int var2);

   @Override
   public void tick() {
   }

   @Override
   public boolean visible() {
      return true;
   }

   public void startAnimation() {
      this.scaleAnimation.setDirection(Direction.FORWARDS);
   }

   public void stopAnimation() {
      this.scaleAnimation.setDirection(Direction.BACKWARDS);
   }

   protected boolean isChat(class_437 var1) {
      return var1 instanceof class_408;
   }

   public boolean isDraggable() {
      return this.draggable;
   }

   public float getLastTickDelta() {
      return this.lastTickDelta;
   }

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   @Override
   public void setEnabled(boolean var1) {
      this.enabled = var1;
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public int getX() {
      return this.x;
   }

   @Override
   public int getY() {
      return this.y;
   }

   @Override
   public void setX(int var1) {
      this.x = var1;
   }

   @Override
   public void setY(int var1) {
      this.y = var1;
   }

   @Override
   public int getWidth() {
      return this.width;
   }

   @Override
   public int getHeight() {
      return this.height;
   }

   @Override
   public void setWidth(int var1) {
      this.width = var1;
   }

   @Override
   public void setHeight(int var1) {
      this.height = var1;
   }
}
