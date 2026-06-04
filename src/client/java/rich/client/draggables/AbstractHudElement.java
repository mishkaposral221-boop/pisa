package rich.client.draggables;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import rich.modules.impl.render.ClickGuiSettings;
import rich.util.animations.Animation;
import rich.util.animations.Decelerate;
import rich.util.animations.Direction;
import rich.util.render.Render2D;

public abstract class AbstractHudElement implements HudElement {
   protected int x;
   protected int y;
   protected int width;
   protected int height;
   protected String name;
   protected boolean enabled = true;
   protected boolean draggable = true;
   protected float scale = 1.0F;
   protected final MinecraftClient mc = MinecraftClient.getInstance();
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
   public void render(DrawContext var1, float var2) {
      if (this.visible()) {
         this.lastTickDelta = var2;
         this.scaleAnimation.update();
         int var3 = (int)(this.scaleAnimation.getOutput().floatValue() * 255.0F);
         ClickGuiSettings var4 = ClickGuiSettings.getInstance();
         if (var4 != null) {
            var3 = (int)(var3 * var4.opacity.getValue());
         }

         if (var3 > 0) {
            boolean var5 = this.scale != 1.0F;
            if (var5) {
               Render2D.pushScale((float)this.x, (float)this.y, this.scale);
               var1.getMatrices().pushMatrix();
               var1.getMatrices().translate((float)this.x, (float)this.y);
               var1.getMatrices().scale(this.scale, this.scale);
               var1.getMatrices().translate((float)(-this.x), (float)(-this.y));
            }

            try {
               this.drawDraggable(var1, var3);
            } finally {
               if (var5) {
                  var1.getMatrices().popMatrix();
                  Render2D.popScale();
               }
            }
         }
      }
   }

   public abstract void drawDraggable(DrawContext var1, int var2);

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

   protected boolean isChat(Screen var1) {
      return var1 instanceof ChatScreen;
   }

   public boolean isDraggable() {
      return this.draggable;
   }

   public float getLastTickDelta() {
      return this.lastTickDelta;
   }

   @Override
   public float getScale() {
      return this.scale;
   }

   public void setScale(float var1) {
      this.scale = Math.max(0.5F, Math.min(3.0F, var1));
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
