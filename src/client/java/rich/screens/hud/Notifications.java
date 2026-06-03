package rich.screens.hud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import rich.client.draggables.AbstractHudElement;
import rich.theme.ClientTheme;
import rich.util.animations.Animation;
import rich.util.animations.Direction;
import rich.util.animations.OutBack;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;

public class Notifications extends AbstractHudElement {
   private static final int FORCED_GUI_SCALE = 2;
   private static Notifications instance;
   private final List<Notifications.Notification> list = new ArrayList<>();
   private static final float NOTIFICATION_HEIGHT = 16.0F;
   private static final float NOTIFICATION_GAP = 3.0F;

   public static Notifications getInstance() {
      return instance;
   }

   public Notifications() {
      super("Notifications", 0, 0, 110, 16, false);
      instance = this;
   }

   private int getCurrentGuiScale() {
      int var1 = (Integer)this.mc.options.getGuiScale().getValue();
      if (var1 == 0) {
         var1 = this.mc.getWindow().calculateScaleFactor(0, this.mc.forcesUnicodeFont());
      }

      return var1;
   }

   private float getScaleFactor() {
      return this.getCurrentGuiScale() / 2.0F;
   }

   private float getVirtualWidth() {
      return this.mc.getWindow().getFramebufferWidth() / 2.0F;
   }

   private float getVirtualHeight() {
      return this.mc.getWindow().getFramebufferHeight() / 2.0F;
   }

   @Override
   public boolean visible() {
      return !this.list.isEmpty();
   }

   @Override
   public void tick() {
      this.list.forEach(var0 -> {
         if (System.currentTimeMillis() > var0.removeTime) {
            var0.anim.setDirection(Direction.BACKWARDS);
         }
      });
      this.list.removeIf(var0 -> var0.anim.isFinished(Direction.BACKWARDS));
      this.updatePosition();
   }

   private void updatePosition() {
      if (this.mc.getWindow() != null) {
         float var1 = this.getVirtualWidth();
         float var2 = this.getVirtualHeight();
         float var3 = var1 / 2.0F;
         float var4 = var2 / 2.0F;
         this.setX((int)(var3 - 60.0F));
         this.setY((int)(var4 + 100.0F));
      }
   }

   public void addNotification(String var1, long var2) {
      Animation var4 = new OutBack().setMs(700).setValue(1.0);
      var4.setDirection(Direction.FORWARDS);
      int var5 = this.list.size();
      float var6 = var5 * 19.0F;
      Notifications.Notification var7 = new Notifications.Notification(var1, var4, System.currentTimeMillis(), System.currentTimeMillis() + var2);
      var7.currentY = var6;
      var7.targetY = var6;
      var7.velocityY = 0.0F;
      this.list.add(var7);
      if (this.list.size() > 12) {
         this.list.removeFirst();
      }

      this.list.sort(Comparator.comparingDouble(var0 -> -var0.removeTime));
      this.updateTargetPositions();
   }

   private void updateTargetPositions() {
      float var1 = 0.0F;

      for (int var2 = 0; var2 < this.list.size(); var2++) {
         Notifications.Notification var3 = this.list.getName(var2);
         float var4 = var3.anim.getOutput().floatValue();
         var3.targetY = var1;
         var1 += 19.0F * var4;
      }
   }

   private int clampAlpha(int var1) {
      return Math.max(0, Math.min(255, var1));
   }

   private int clampAlpha(float var1) {
      return Math.max(0, Math.min(255, (int)var1));
   }

   @Override
   public void drawDraggable(DrawContext var1, int var2) {
      var2 = this.clampAlpha(var2);
      if (var2 > 0) {
         float var3 = var2 / 255.0F;
         this.updatePosition();
         this.updateTargetPositions();
         float var4 = 180.0F;
         float var5 = 12.0F;
         float var6 = 0.016F;

         for (Notifications.Notification var8 : this.list) {
            float var9 = var8.targetY - var8.currentY;
            float var10 = var9 * var4;
            float var11 = var8.velocityY * var5;
            float var12 = var10 - var11;
            var8.velocityY += var12 * var6;
            var8.currentY = var8.currentY + var8.velocityY * var6;
            if (Math.abs(var9) < 0.01F && Math.abs(var8.velocityY) < 0.01F) {
               var8.currentY = var8.targetY;
               var8.velocityY = 0.0F;
            }
         }

         float var20 = 5.0F;
         float var21 = 0.0F;
         float var22 = 0.0F;

         for (Notifications.Notification var24 : this.list) {
            float var25 = var24.anim.getOutput().floatValue();
            if (!(var25 <= 0.01F)) {
               var25 = Math.max(0.0F, Math.min(1.0F, var25));
               float var13 = Fonts.BOLD.getWidth(var24.text, 6.0F);
               float var14 = var13 + var20 * 2.0F + 22.0F;
               var21 = Math.max(var21, var14);
               float var15 = this.getY() + var24.currentY;
               float var16 = this.getX() + (120.0F - var14) / 2.0F;
               int var17 = this.clampAlpha(225.0F * var25 * var3);
               int var18 = this.clampAlpha(155.0F * var25 * var3);
               if (var17 > 0) {
                  Render2D.gradientRect(var16, var15, var14, 16.0F, ClientTheme.bgGradient(var17), 4.0F);
                  Render2D.outline(var16, var15, var14, 16.0F, 0.35F, ClientTheme.outline(var17), 4.0F);
                  Render2D.outline(var16 + 2.75F, var15 + 2.0F, 12.0F, 12.0F, 0.35F, ClientTheme.outline(var17), 4.0F);
                  Fonts.BOLD.draw(var24.text, var16 + var20 + 16.0F, var15 + 4.5F, 6.0F, ClientTheme.text(var17));
                  Fonts.GUI_ICONS.draw("C", var16 + 5.0F, var15 + 4.0F, 8.0F, ClientTheme.text(var18));
               }

               var22 = Math.max(var22, var24.currentY + 16.0F);
            }
         }

         if (var21 > 0.0F) {
            this.setWidth((int)Math.ceil(var21));
         }

         this.setHeight((int)Math.ceil(Math.max(16.0F, var22)));
      }
   }

   public static class Notification {
      String text;
      Animation anim;
      long startTime;
      long removeTime;
      float currentY;
      float targetY;
      float velocityY;

      Notification(String var1, Animation var2, long var3, long var5) {
         this.text = var1;
         this.anim = var2;
         this.startTime = var3;
         this.removeTime = var5;
         this.currentY = 0.0F;
         this.targetY = 0.0F;
         this.velocityY = 0.0F;
      }

      boolean isExpired() {
         return System.currentTimeMillis() > this.removeTime;
      }
   }
}
