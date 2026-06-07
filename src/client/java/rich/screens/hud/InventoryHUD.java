package rich.screens.hud;

import java.awt.Color;
import net.minecraft.item.ItemStack;
import net.minecraft.client.gui.DrawContext;
import rich.client.draggables.AbstractHudElement;
import rich.screens.clickgui.ClickGui;
import rich.theme.ClientTheme;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;

public class InventoryHUD extends AbstractHudElement {
   private static final int SLOT_SIZE = 18;
   private static final int SLOT_SPACING = 2;
   private static final int ROWS = 3;
   private static final int COLS = 9;
   private static final int TOTAL_WIDTH = 178;
   private static final int TOTAL_HEIGHT = 58;

   public InventoryHUD() {
      super("InventoryHUD", 10, 220, 182, 62, true);
   }

   @Override
   public boolean visible() {
      return !(this.mc.currentScreen instanceof ClickGui);
   }

   @Override
   public void drawDraggable(DrawContext var1, int var2) {
      if (var2 > 0) {
         this.render(var1, 1.0F);
      }
   }

   @Override
   public void render(DrawContext var1, float var2) {
      if (this.mc.player != null && this.mc.world != null) {
         float var3 = this.getX();
         float var4 = this.getY();
         this.drawBackground(var1, var3, var4);

         for (int var5 = 0; var5 < 3; var5++) {
            for (int var6 = 0; var6 < 9; var6++) {
               int var7 = var5 * 9 + var6 + 9;
               float var8 = var3 + 2.0F + var6 * 20;
               float var9 = var4 + 2.0F + var5 * 20;
               this.drawInventorySlot(var1, var8, var9, var7);
            }
         }
      }
   }

   private void drawBackground(DrawContext var1, float var2, float var3) {
      Render2D.gradientRect(var2 + 2.0F, var3 + 2.0F, 178.0F, 58.0F, ClientTheme.bgGradient(180), 6.0F);
      Render2D.outline(var2 + 2.0F, var3 + 2.0F, 178.0F, 58.0F, 0.35F, ClientTheme.outline(255), 5.0F);
   }

   private void drawInventorySlot(DrawContext var1, float var2, float var3, int var4) {
      ItemStack var5 = this.mc.player.getInventory().getStack(var4);
      int var6 = var5.isEmpty() ? new Color(40, 40, 40, 150).getRGB() : new Color(60, 60, 60, 180).getRGB();
      Render2D.rect(var2, var3, 18.0F, 18.0F, var6, 3.0F);
      Render2D.outline(var2, var3, 18.0F, 18.0F, 0.35F, new Color(100, 100, 100, 200).getRGB(), 3.0F);
      if (!var5.isEmpty()) {
         this.drawItem(var1, var2, var3, var5);
         this.drawItemCount(var1, var2, var3, var5);
      }
   }

   private void drawItem(DrawContext var1, float var2, float var3, ItemStack var4) {
      var1.drawItem(var4, (int)var2 + 1, (int)var3 + 1);
   }

   private void drawItemCount(DrawContext var1, float var2, float var3, ItemStack var4) {
      if (var4.getCount() > 1) {
         String var5 = String.valueOf(var4.getCount());
         int var6 = var4.getCount() > 64 ? new Color(255, 100, 100).getRGB() : new Color(255, 255, 255).getRGB();
         float var7 = var2 + 18.0F - 4.0F - Fonts.REGULAR.getWidth(var5, 6.0F);
         float var8 = var3 + 18.0F - 8.0F;
         Fonts.REGULAR.draw(var5, var7, var8, 6.0F, var6);
         Fonts.REGULAR.draw(var5, var7 + 1.0F, var8 + 1.0F, 6.0F, new Color(0, 0, 0, 150).getRGB());
      }
   }

   @Override
   public void tick() {
   }
}
