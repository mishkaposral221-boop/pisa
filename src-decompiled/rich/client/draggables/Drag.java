package rich.client.draggables;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_11909;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_408;
import rich.Initialization;
import rich.modules.impl.render.Hud;
import rich.util.a;
import rich.util.animations.SweepAnim;
import rich.util.config.impl.drag.DragConfig;
import rich.util.render.Render2D;

public class Drag {
   private static final float OUTLINE_OFFSET = 0.0F;
   private static final float OUTLINE_THICKNESS = 1.0F;
   private static final int OUTLINE_COLOR = a.b(255, 255, 255, 255);
   private static final Set<String> EXCLUDED_ELEMENTS = Set.of("Notifications");
   private static HudElement draggingElement;
   private static int startX;
   private static int startY;
   private static final Map<HudElement, SweepAnim> sweepAnimations = new HashMap<>();
   private static final Map<HudElement, Boolean> wasHovered = new HashMap<>();

   public static void onDraw(class_332 var0, int var1, int var2, float var3, boolean var4) {
      HudManager var5 = getHudManager();
      if (var5 != null) {
         Hud var6 = Hud.getInstance();
         if (var6 != null) {
            if (!var4) {
               if (draggingElement != null) {
                  DragConfig.getInstance().save();
                  draggingElement = null;
               }

               sweepAnimations.clear();
               wasHovered.clear();
            }

            if (var4 && draggingElement != null) {
               class_310 var7 = class_310.method_1551();
               int var8 = var7.method_22683().method_4486();
               int var9 = var7.method_22683().method_4502();
               int var10 = var1 - startX;
               int var11 = var2 - startY;
               var10 = Math.max(0, Math.min(var8 - draggingElement.getWidth(), var10));
               var11 = Math.max(0, Math.min(var9 - draggingElement.getHeight(), var11));
               draggingElement.setX(var10);
               draggingElement.setY(var11);
            }

            var5.render(var0, var3, var1, var2);
            if (var4) {
               for (HudElement var22 : var5.getEnabledElements()) {
                  if (!var22.visible()) {
                     sweepAnimations.remove(var22);
                     wasHovered.remove(var22);
                  } else if (!EXCLUDED_ELEMENTS.contains(var22.getName())) {
                     boolean var23 = isHovered(var22, var1, var2);
                     boolean var25 = wasHovered.getOrDefault(var22, false);
                     float var27 = var22.getRoundingRadius();
                     float var12 = 0.0F;
                     float var13 = var22.getX() - var12;
                     float var14 = var22.getY() - var12;
                     float var15 = var22.getWidth() + var12 * 2.0F;
                     float var16 = var22.getHeight() + var12 * 2.0F;
                     float var17 = Math.max(0.0F, var27 + var12);
                     SweepAnim var18 = sweepAnimations.computeIfAbsent(var22, var0x -> new SweepAnim(0.05F));
                     if (var23 && !var25) {
                        var18.start();
                     } else if (!var23 && var25) {
                        var18.reset();
                     }

                     wasHovered.put(var22, var23);
                     var18.update();
                     float var19 = var18.getProgress();
                     if (var23 || var18.isActive()) {
                        float var20 = 0.3F;
                        Render2D.glowOutline(var13, var14, var15, var16, 1.0F, OUTLINE_COLOR, var17, var19, var20);
                     }

                     if (!var23 && var18.isCompleted()) {
                        sweepAnimations.remove(var22);
                        wasHovered.remove(var22);
                     }
                  }
               }
            }
         }
      }
   }

   public static void onMouseClick(class_11909 var0) {
      class_310 var1 = class_310.method_1551();
      if (var1.field_1755 instanceof class_408) {
         if (var0.method_74245() == 0) {
            HudManager var2 = getHudManager();
            if (var2 == null) {
               return;
            }

            double var3 = var0.comp_4798();
            double var5 = var0.comp_4799();
            HudElement var7 = var2.getElementAt(var3, var5);
            if (var7 != null && var7 instanceof AbstractHudElement var8 && var8.isDraggable()) {
               draggingElement = var7;
               startX = (int)var3 - var7.getX();
               startY = (int)var5 - var7.getY();
            }
         }
      }
   }

   public static void onMouseRelease(class_11909 var0) {
      if (var0.method_74245() == 0 && draggingElement != null) {
         DragConfig.getInstance().save();
         draggingElement = null;
      }
   }

   public static void resetDragging() {
      if (draggingElement != null) {
         DragConfig.getInstance().save();
         draggingElement = null;
      }

      sweepAnimations.clear();
      wasHovered.clear();
   }

   public static boolean isDragging() {
      return draggingElement != null;
   }

   private static boolean isHovered(HudElement var0, double var1, double var3) {
      int var5 = var0.getX();
      int var6 = var0.getY();
      int var7 = var0.getWidth();
      int var8 = var0.getHeight();
      return var1 >= var5 && var1 <= var5 + var7 && var3 >= var6 && var3 <= var6 + var8;
   }

   private static HudManager getHudManager() {
      if (Initialization.getInstance() == null) {
         return null;
      } else {
         return Initialization.getInstance().getManager() == null ? null : Initialization.getInstance().getManager().getHudManager();
      }
   }

   public static void tick() {
      HudManager var0 = getHudManager();
      if (var0 != null) {
         var0.tick();
      }
   }
}
