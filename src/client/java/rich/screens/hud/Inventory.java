package rich.screens.hud;

import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.item.ItemStack;
import net.minecraft.client.gui.DrawContext;
import rich.client.draggables.AbstractHudElement;
import rich.util.animations.Direction;
import rich.util.render.Render2D;
import rich.util.render.item.ItemRender;

public class Inventory extends AbstractHudElement {
   private static final int SLOT_SIZE = 12;
   private static final int SLOTS_PER_ROW = 9;
   private static final int INVENTORY_ROWS = 3;
   private static final float ITEM_SCALE = 0.5F;
   private int filledSlots = 0;

   public Inventory() {
      super("Inventory", 20, 60, 200, 80, true);
      this.stopAnimation();
   }

   @Override
   public boolean visible() {
      return !this.scaleAnimation.isFinished(Direction.BACKWARDS);
   }

   @Override
   public void tick() {
      if (this.mc.player == null) {
         this.filledSlots = 0;
         this.stopAnimation();
      } else {
         this.filledSlots = 0;

         for (int var1 = 9; var1 < 36; var1++) {
            ItemStack var2 = this.mc.player.getInventory().getStack(var1);
            if (!var2.isEmpty()) {
               this.filledSlots++;
            }
         }

         boolean var3 = this.filledSlots > 0;
         boolean var4 = this.isChat(this.mc.currentScreen);
         if (!var3 && !var4) {
            this.stopAnimation();
         } else {
            this.startAnimation();
         }
      }
   }

   @Override
   public void drawDraggable(DrawContext var1, int var2) {
      if (var2 > 0) {
         if (this.mc.player != null) {
            float var3 = var2 / 255.0F;
            float var4 = this.getX();
            float var5 = this.getY();
            float var6 = 6.0F;
            float var7 = 1.0F;
            float var8 = 108.0F + 8.0F * var7;
            float var9 = 36.0F + 2.0F * var7;
            float var10 = var8 + var6 * 2.0F;
            float var11 = var9 + var6 * 2.0F;
            this.setWidth((int)var10);
            this.setHeight((int)(var11 + 4.0F));
            float var12 = var5;
            int var13 = (int)(255.0F * var3);
            Render2D.gradientRect(
               var4 + 2.0F,
               var12 + 2.0F,
               var10 - 4.0F,
               var11 - 4.0F,
               new int[]{
                  new Color(52, 52, 52, var13).getRGB(),
                  new Color(32, 32, 32, var13).getRGB(),
                  new Color(52, 52, 52, var13).getRGB(),
                  new Color(32, 32, 32, var13).getRGB()
               },
               5.0F
            );
            Render2D.outline(var4 + 2.0F, var12 + 2.0F, var10 - 4.0F, var11 - 4.0F, 0.35F, new Color(90, 90, 90, var13).getRGB(), 5.0F);
            float var14 = var4 + var6;
            float var15 = var12 + var6;
            ArrayList<Inventory.CountLabel> var16 = new ArrayList<>();

            for (int var17 = 0; var17 < 3; var17++) {
               for (int var18 = 0; var18 < 9; var18++) {
                  int var19 = 9 + var17 * 9 + var18;
                  float var20 = var14 + var18 * (12.0F + var7);
                  float var21 = var15 + var17 * (12.0F + var7);
                  ItemStack var22 = this.mc.player.getInventory().getStack(var19);
                  Render2D.rect(var20, var21, 12.0F, 12.0F, new Color(28, 28, 28, var13).getRGB(), 2.0F);
                  if (!var22.isEmpty()) {
                     float var23 = 8.0F;
                     float var24 = var20 + (12.0F - var23) / 2.0F;
                     float var25 = var21 + (12.0F - var23) / 2.0F;
                     if (ItemRender.needsContextRender(var22)) {
                        ItemRender.drawItemWithContext(var1, var22, var24, var25, 0.5F, var3);
                     } else {
                        ItemRender.drawItem(var22, var24, var25, 0.5F, var3);
                     }

                     int var26 = var22.getCount();
                     if (var26 > 1) {
                        var16.add(new Inventory.CountLabel(var20, var21, var26));
                     }
                  }
               }
            }

            int var27 = (int)(255.0F * var3);
            int var28 = var27 << 24 | 16777215;

            for (Inventory.CountLabel var30 : var16) {
               String var31 = String.valueOf(var30.count);
               int var32 = this.mc.textRenderer.getWidth(var31);
               int var33 = (int)(var30.slotX + 12.0F - var32);
               int var34 = (int)(var30.slotY + 12.0F - 9.0F + 1.0F);
               var1.drawText(this.mc.textRenderer, var31, var33, var34, var28, true);
            }
         }
      }
   }

   private record CountLabel(float slotX, float slotY, int count) {
   }
}
