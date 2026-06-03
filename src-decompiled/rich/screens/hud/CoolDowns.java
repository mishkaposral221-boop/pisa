package rich.screens.hud;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.class_1792;
import net.minecraft.class_1796;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_332;
import rich.client.draggables.AbstractHudElement;
import rich.util.a;
import rich.util.animations.Direction;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.item.ItemRender;
import rich.util.render.shader.Scissor;

public class CoolDowns extends AbstractHudElement {
   private static final int FORCED_GUI_SCALE = 2;
   private final Map<class_1792, CoolDowns.CoolDownInfo> cooldownMap = new LinkedHashMap<>();
   private final Map<class_1792, Float> cooldownAnimations = new LinkedHashMap<>();
   private final Set<class_1792> activeCooldowns = new HashSet<>();
   private float animatedWidth = 80.0F;
   private float animatedHeight = 23.0F;
   private long lastRenderTime = System.currentTimeMillis();
   private long lastItemChange = 0L;
   private int currentItemIndex = 0;
   private static final float ANIMATION_SPEED = 8.0F;
   private static final float ITEM_SCALE = 0.5F;
   private static final String TIMER_TEMPLATE = "00:00";
   private static final class_1792[] EXAMPLE_ITEMS = new class_1792[]{
      class_1802.field_8449,
      class_1802.field_8634,
      class_1802.field_8479,
      class_1802.field_49814,
      class_1802.field_8367,
      class_1802.field_8547,
      class_1802.field_8399,
      class_1802.field_8551,
      class_1802.field_22021
   };

   public CoolDowns() {
      super("CoolDowns", 10, 40, 80, 23, true);
      this.stopAnimation();
   }

   @Override
   public boolean visible() {
      return !this.scaleAnimation.isFinished(Direction.BACKWARDS);
   }

   @Override
   public void tick() {
      if (this.mc.field_1724 == null) {
         this.cooldownMap.clear();
         this.activeCooldowns.clear();
         this.cooldownAnimations.clear();
         this.stopAnimation();
      } else {
         this.activeCooldowns.clear();
         HashSet var1 = new HashSet();

         for (int var2 = 0; var2 < this.mc.field_1724.method_31548().method_5439(); var2++) {
            class_1799 var3 = this.mc.field_1724.method_31548().method_5438(var2);
            if (!var3.method_7960() && !var1.contains(var3.method_7909())) {
               var1.add(var3.method_7909());
               this.checkAndUpdateCooldown(var3.method_7909());
            }
         }

         class_1799 var7 = this.mc.field_1724.method_6047();
         if (!var7.method_7960() && !var1.contains(var7.method_7909())) {
            this.checkAndUpdateCooldown(var7.method_7909());
         }

         class_1799 var8 = this.mc.field_1724.method_6079();
         if (!var8.method_7960() && !var1.contains(var8.method_7909())) {
            this.checkAndUpdateCooldown(var8.method_7909());
         }

         boolean var4 = !this.cooldownAnimations.isEmpty() || this.isChat(this.mc.field_1755);
         if (var4) {
            this.startAnimation();
         } else {
            this.stopAnimation();
         }

         if (this.cooldownAnimations.isEmpty() && this.isChat(this.mc.field_1755)) {
            long var5 = System.currentTimeMillis();
            if (var5 - this.lastItemChange >= 1000L) {
               this.currentItemIndex = (this.currentItemIndex + 1) % EXAMPLE_ITEMS.length;
               this.lastItemChange = var5;
            }
         }
      }
   }

   private void checkAndUpdateCooldown(class_1792 var1) {
      if (this.mc.field_1724 != null) {
         class_1796 var2 = this.mc.field_1724.method_7357();
         class_1799 var3 = var1.method_7854();
         if (var2.method_7904(var3)) {
            float var4 = var2.method_7905(var3, 0.0F);
            this.activeCooldowns.add(var1);
            CoolDowns.CoolDownInfo var5 = this.cooldownMap.get(var1);
            if (var5 == null) {
               var5 = new CoolDowns.CoolDownInfo(var1, var4);
               this.cooldownMap.put(var1, var5);
            } else {
               var5.updateEstimate(var4);
            }

            if (!this.cooldownAnimations.containsKey(var1)) {
               this.cooldownAnimations.put(var1, 0.0F);
            }
         }
      }
   }

   private String formatDuration(int var1) {
      if (var1 < 0) {
         return "...";
      }

      if (var1 == 0) {
         return "0:00";
      }

      int var2 = var1 / 60;
      int var3 = var1 % 60;
      return String.format("%d:%02d", var2, var3);
   }

   @Override
   public void drawDraggable(class_332 var1, int var2) {
      if (var2 > 0) {
         float var3 = var2 / 255.0F;
         long var4 = System.currentTimeMillis();
         float var6 = (float)(var4 - this.lastRenderTime) / 1000.0F;
         this.lastRenderTime = var4;
         var6 = Math.min(var6, 0.1F);
         ArrayList var7 = new ArrayList();

         for (Entry var9 : this.cooldownAnimations.entrySet()) {
            class_1792 var10 = (class_1792)var9.getKey();
            float var11 = (Float)var9.getValue();
            float var12 = this.activeCooldowns.contains(var10) ? 1.0F : 0.0F;
            float var13 = var11 + (var12 - var11) * Math.min(1.0F, var6 * 8.0F);
            if (Math.abs(var13 - var12) < 0.01F) {
               var13 = var12;
            }

            if (var13 <= 0.01F && var12 == 0.0F) {
               var7.add(var10);
            } else {
               this.cooldownAnimations.put(var10, var13);
            }
         }

         for (class_1792 var40 : var7) {
            this.cooldownAnimations.remove(var40);
            this.cooldownMap.remove(var40);
         }

         float var39 = this.getX();
         float var41 = this.getY();
         int var42 = 23;
         float var43 = 80.0F;
         boolean var44 = !this.cooldownAnimations.isEmpty();
         int var45 = a.b(0, 0, 0, 0);
         Render2D.blur(var39, var41, 1.0F, 1.0F, 0.0F, 0.0F, var45);
         float var14 = Fonts.BOLD.getWidth("00:00", 6.0F);
         if (!var44) {
            var42 += 11;
            String var15 = "Example CoolDown";
            float var16 = Fonts.BOLD.getWidth(var15, 6.0F);
            var43 = Math.max(var16 + var14 + 55.0F, var43);
         } else {
            for (Entry var48 : this.cooldownAnimations.entrySet()) {
               class_1792 var17 = (class_1792)var48.getKey();
               float var18 = (Float)var48.getValue();
               if (!(var18 <= 0.0F)) {
                  CoolDowns.CoolDownInfo var19 = this.cooldownMap.get(var17);
                  if (var19 != null) {
                     var42 += (int)(var18 * 11.0F);
                     String var20 = var17.method_7854().method_7964().getString();
                     float var21 = Fonts.BOLD.getWidth(var20, 6.0F);
                     var43 = Math.max(var21 + var14 + 55.0F, var43);
                  }
               }
            }
         }

         float var47 = var42 + 2;
         this.animatedWidth = this.animatedWidth + (var43 - this.animatedWidth) * Math.min(1.0F, var6 * 8.0F);
         this.animatedHeight = this.animatedHeight + (var47 - this.animatedHeight) * Math.min(1.0F, var6 * 8.0F);
         if (Math.abs(this.animatedWidth - var43) < 0.3F) {
            this.animatedWidth = var43;
         }

         if (Math.abs(this.animatedHeight - var47) < 0.3F) {
            this.animatedHeight = var47;
         }

         this.setWidth((int)Math.ceil(this.animatedWidth));
         this.setHeight((int)Math.ceil(this.animatedHeight));
         float var49 = this.animatedHeight;
         int var50 = (int)(255.0F * var3);
         if (var49 > 0.0F) {
            Render2D.gradientRect(
               var39,
               var41,
               this.getWidth(),
               var49,
               new int[]{
                  new Color(52, 52, 52, var50).getRGB(),
                  new Color(32, 32, 32, var50).getRGB(),
                  new Color(52, 52, 52, var50).getRGB(),
                  new Color(32, 32, 32, var50).getRGB()
               },
               5.0F
            );
            Render2D.outline(var39, var41, this.getWidth(), var49, 0.35F, new Color(90, 90, 90, var50).getRGB(), 5.0F);
         }

         Scissor.enable(var39, var41, this.getWidth(), var49, 2.0F);
         Render2D.gradientRect(
            var39 + this.getWidth() - 22.5F,
            var41 + 5.0F,
            14.0F,
            12.0F,
            new int[]{
               new Color(52, 52, 52, var50).getRGB(),
               new Color(52, 52, 52, var50).getRGB(),
               new Color(52, 52, 52, var50).getRGB(),
               new Color(52, 52, 52, var50).getRGB()
            },
            3.0F
         );
         Fonts.ICONS.draw("D", var39 + this.getWidth() - 20.0F, var41 + 6.5F, 9.0F, new Color(165, 165, 165, var50).getRGB());
         Fonts.BOLD.draw("CoolDowns", var39 + 8.0F, var41 + 6.5F, 6.0F, new Color(255, 255, 255, var50).getRGB());
         int var51 = 23;
         float var52 = var14 + 4.0F;
         float var53 = var39 + this.getWidth() - var52 - 9.5F;
         if (!var44) {
            class_1792 var54 = EXAMPLE_ITEMS[this.currentItemIndex];
            String var22 = "Example CoolDown";
            String var23 = "0:00";
            Render2D.gradientRect(
               var53 + 1.0F,
               var41 + var51 - 1.0F,
               var52,
               9.0F,
               new int[]{
                  new Color(52, 52, 52, var50).getRGB(),
                  new Color(52, 52, 52, var50).getRGB(),
                  new Color(52, 52, 52, var50).getRGB(),
                  new Color(52, 52, 52, var50).getRGB()
               },
               3.0F
            );
            Render2D.blur(var39, var41, 1.0F, 1.0F, 0.0F, 0.0F, var45);
            Render2D.outline(var53 + 1.0F, var41 + var51 - 1.0F, var52, 9.0F, 0.05F, new Color(132, 132, 132, var50).getRGB(), 2.0F);
            float var24 = var39 + 8.0F;
            float var25 = var41 + var51 - 1.0F;
            if (ItemRender.needsContextRender(var54.method_7854())) {
               ItemRender.drawItemWithContext(var1, var54.method_7854(), var24, var25, 0.5F, var3);
            } else {
               ItemRender.drawItem(var54.method_7854(), var24, var25, 0.5F, var3);
            }

            float var26 = var39 + 20.0F;
            Fonts.BOLD.draw(var22, var26, var41 + var51 - 1.0F, 6.0F, new Color(255, 255, 255, var50).getRGB());
            float var27 = Fonts.BOLD.getWidth(var23, 6.0F);
            float var28 = var53 + (var52 - var27) / 2.0F;
            Fonts.BOLD.draw(var23, var28 + 1.0F, var41 + var51, 6.0F, new Color(165, 165, 165, var50).getRGB());
         } else {
            for (Entry var56 : this.cooldownAnimations.entrySet()) {
               class_1792 var57 = (class_1792)var56.getKey();
               float var58 = (Float)var56.getValue();
               if (!(var58 <= 0.0F)) {
                  CoolDowns.CoolDownInfo var59 = this.cooldownMap.get(var57);
                  if (var59 != null) {
                     class_1796 var60 = this.mc.field_1724.method_7357();
                     float var61 = var60.method_7905(var57.method_7854(), 0.0F);
                     String var62 = var57.method_7854().method_7964().getString();
                     int var29 = var59.getDisplaySeconds(var61);
                     String var30 = this.formatDuration(var29);
                     int var31 = (int)(255.0F * var58 * var3);
                     Render2D.gradientRect(
                        var53 + 1.0F,
                        var41 + var51 - 1.0F,
                        var52,
                        9.0F,
                        new int[]{
                           new Color(52, 52, 52, var31).getRGB(),
                           new Color(52, 52, 52, var31).getRGB(),
                           new Color(52, 52, 52, var31).getRGB(),
                           new Color(52, 52, 52, var31).getRGB()
                        },
                        3.0F
                     );
                     Render2D.blur(var39, var41, 1.0F, 1.0F, 0.0F, 0.0F, var45);
                     Render2D.outline(var53 + 1.0F, var41 + var51 - 1.0F, var52, 9.0F, 0.05F, new Color(132, 132, 132, var31).getRGB(), 2.0F);
                     float var32 = var39 + 8.0F;
                     float var33 = var41 + var51 - 1.0F;
                     if (ItemRender.needsContextRender(var57.method_7854())) {
                        ItemRender.drawItemWithContext(var1, var57.method_7854(), var32, var33, 0.5F, var58 * var3);
                     } else {
                        ItemRender.drawItem(var57.method_7854(), var32, var33, 0.5F, var58 * var3);
                     }

                     float var34 = var39 + 20.0F;
                     Fonts.BOLD.draw(var62, var34, var41 + var51 - 0.5F, 6.0F, new Color(255, 255, 255, var31).getRGB());
                     float var35 = Fonts.BOLD.getWidth(var30, 6.0F);
                     float var36 = var53 + (var52 - var35) / 2.0F;
                     Fonts.BOLD.draw(var30, var36 + 1.0F, var41 + var51, 6.0F, new Color(165, 165, 165, var31).getRGB());
                     var51 += (int)(var58 * 11.0F);
                  }
               }
            }
         }

         Scissor.disable();
      }
   }

   private static class CoolDownInfo {
      class_1792 item;
      long startTime;
      float startProgress;
      long estimatedTotalMs;
      int displaySeconds = -1;
      long nextTickTime = 0L;
      boolean estimateReady = false;

      CoolDownInfo(class_1792 var1, float var2) {
         this.item = var1;
         this.startTime = System.currentTimeMillis();
         this.startProgress = var2;
         this.estimatedTotalMs = 0L;
         this.nextTickTime = 0L;
         this.estimateReady = false;
      }

      void updateEstimate(float var1) {
         if (!this.estimateReady) {
            long var2 = System.currentTimeMillis();
            long var4 = var2 - this.startTime;
            if (var4 >= 200L) {
               if (this.startProgress > var1 && this.startProgress > 0.01F) {
                  float var6 = this.startProgress - var1;
                  if (var6 > 0.01F) {
                     this.estimatedTotalMs = (long)((float)var4 / var6);
                     long var7 = (long)(var1 * (float)this.estimatedTotalMs);
                     this.displaySeconds = (int)Math.ceil(var7 / 1000.0);
                     this.nextTickTime = var2 + 1000L;
                     this.estimateReady = true;
                  }
               }
            }
         }
      }

      int getDisplaySeconds(float var1) {
         if (var1 <= 0.0F) {
            this.displaySeconds = 0;
            return 0;
         }

         if (!this.estimateReady) {
            return -1;
         }

         long var2 = System.currentTimeMillis();
         if (var2 >= this.nextTickTime && this.nextTickTime > 0L) {
            this.displaySeconds = Math.max(0, this.displaySeconds - 1);
            this.nextTickTime = var2 + 1000L;
            int var4;
            if (this.estimatedTotalMs > 0L) {
               long var5 = (long)(var1 * (float)this.estimatedTotalMs);
               var4 = (int)Math.ceil(var5 / 1000.0);
            } else {
               var4 = this.displaySeconds;
            }

            if (Math.abs(this.displaySeconds - var4) > 2) {
               this.displaySeconds = var4;
            }
         }

         return Math.max(0, this.displaySeconds);
      }
   }
}
