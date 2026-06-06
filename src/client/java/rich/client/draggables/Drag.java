package rich.client.draggables;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.gui.Click;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import rich.Initialization;
import rich.modules.impl.render.Hud;
import rich.util.color.ColorUtil;
import rich.util.animations.SweepAnim;
import rich.util.config.impl.drag.DragConfig;
import rich.util.render.Render2D;

public class Drag {
   private static final float OUTLINE_OFFSET = 0.0F;
   private static final float OUTLINE_THICKNESS = 1.0F;
   private static final int OUTLINE_COLOR = ColorUtil.b(255, 255, 255, 255);
   private static final double RESIZE_HANDLE = 8.0;
   private static final Set<String> EXCLUDED_ELEMENTS = Set.of("Notifications");
   private static HudElement draggingElement;
   private static HudElement resizingElement;
   private static int resizeBaseW;
   private static int resizeBaseH;
   private static int startX;
   private static int startY;
   private static final Map<HudElement, SweepAnim> sweepAnimations = new HashMap<>();
   private static final Map<HudElement, Boolean> wasHovered = new HashMap<>();

   public static void onDraw(DrawContext var0, int var1, int var2, float var3, boolean var4) {
      HudManager var5 = getHudManager();
      if (var5 == null) {
         return;
      }

      Hud var6 = Hud.getInstance();
      if (var6 == null) {
         return;
      }

      // В обычной игре HUD уже рисуется в InGameHudMixin. Повторный рендер здесь давал
      // Rich/Drag.onDraw ~3-6ms каждый кадр и дублировал работу. Оставляем Drag.onDraw
      // только для ChatScreen, где нужны рамки/перетаскивание HUD-элементов.
      if (!var4) {
         if (draggingElement != null || resizingElement != null) {
            DragConfig.getInstance().save();
            draggingElement = null;
            resizingElement = null;
         }

         if (!sweepAnimations.isEmpty()) {
            sweepAnimations.clear();
         }
         if (!wasHovered.isEmpty()) {
            wasHovered.clear();
         }
         return;
      }

      if (resizingElement != null) {
         if (resizingElement instanceof AbstractHudElement var40) {
            float var41 = (float)(var1 - resizingElement.getX()) / (float)Math.max(1, resizeBaseW);
            float var42 = (float)(var2 - resizingElement.getY()) / (float)Math.max(1, resizeBaseH);
            var40.setScale((var41 + var42) / 2.0F);
         }
      } else if (draggingElement != null) {
         MinecraftClient var7 = MinecraftClient.getInstance();
         int var8 = var7.getWindow().getScaledWidth();
         int var9 = var7.getWindow().getScaledHeight();
         int var10 = var1 - startX;
         int var11 = var2 - startY;
         float var43 = draggingElement.getScale();
         int var44 = (int)(draggingElement.getWidth() * var43);
         int var45 = (int)(draggingElement.getHeight() * var43);
         var10 = Math.max(0, Math.min(var8 - var44, var10));
         var11 = Math.max(0, Math.min(var9 - var45, var11));
         draggingElement.setX(var10);
         draggingElement.setY(var11);
      }

      var5.render(var0, var3, var1, var2);
      for (HudElement var22 : var5.getEnabledElements()) {
         if (!var22.visible()) {
            sweepAnimations.remove(var22);
            wasHovered.remove(var22);
         } else if (!EXCLUDED_ELEMENTS.contains(var22.getName())) {
            boolean var23 = isHovered(var22, var1, var2);
            boolean var25 = wasHovered.getOrDefault(var22, false);
            float var27 = var22.getRoundingRadius();
            float var12 = 0.0F;
            float var46 = var22.getScale();
            float var13 = var22.getX() - var12;
            float var14 = var22.getY() - var12;
            float var15 = var22.getWidth() * var46 + var12 * 2.0F;
            float var16 = var22.getHeight() * var46 + var12 * 2.0F;
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

   public static void onMouseClick(Click var0) {
      MinecraftClient var1 = MinecraftClient.getInstance();
      if (var1.currentScreen instanceof ChatScreen) {
         if (var0.button() == 0) {
            HudManager var2 = getHudManager();
            if (var2 == null) {
               return;
            }

            double var3 = var0.x();
            double var5 = var0.y();
            HudElement var7 = var2.getElementAt(var3, var5);
            if (var7 != null && var7 instanceof AbstractHudElement var8 && var8.isDraggable()) {
               float var9 = var7.getScale();
               double var10 = var7.getX() + var7.getWidth() * var9;
               double var12 = var7.getY() + var7.getHeight() * var9;
               if (var3 >= var10 - RESIZE_HANDLE && var5 >= var12 - RESIZE_HANDLE) {
                  resizingElement = var7;
                  resizeBaseW = var7.getWidth();
                  resizeBaseH = var7.getHeight();
               } else {
                  draggingElement = var7;
                  startX = (int)var3 - var7.getX();
                  startY = (int)var5 - var7.getY();
               }
            }
         }
      }
   }

   public static void onMouseRelease(Click var0) {
      if (var0.button() == 0 && (draggingElement != null || resizingElement != null)) {
         DragConfig.getInstance().save();
         draggingElement = null;
         resizingElement = null;
      }
   }

   public static void resetDragging() {
      if (draggingElement != null || resizingElement != null) {
         DragConfig.getInstance().save();
         draggingElement = null;
         resizingElement = null;
      }

      sweepAnimations.clear();
      wasHovered.clear();
   }

   public static boolean isDragging() {
      return draggingElement != null || resizingElement != null;
   }

   private static boolean isHovered(HudElement var0, double var1, double var3) {
      int var5 = var0.getX();
      int var6 = var0.getY();
      float var7 = var0.getScale();
      double var8 = var0.getWidth() * var7;
      double var10 = var0.getHeight() * var7;
      return var1 >= var5 && var1 <= var5 + var8 && var3 >= var6 && var3 <= var6 + var10;
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
