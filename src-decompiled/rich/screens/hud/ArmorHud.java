package rich.screens.hud;

import java.awt.Color;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_332;
import rich.client.draggables.AbstractHudElement;
import rich.screens.clickgui.ClickGui;
import rich.theme.ClientTheme;
import rich.util.render.Render2D;

public class ArmorHud extends AbstractHudElement {
   private static final int ITEM_SIZE = 16;
   private static final int PAD = 4;
   private static final int BAR_H = 3;
   private static final int BAR_GAP = 2;
   private static final int SLOT_W = 24;
   private static final int SLOT_H = 29;
   private static final int SLOTS = 4;
   private static final int SLOT_GAP = 3;
   private static final int TOTAL_W = 105;
   private static final int TOTAL_H = 29;
   private static final class_1304[] SLOTS_ORDER = new class_1304[]{class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166};

   public ArmorHud() {
      super("ArmorHud", 10, 200, 105, 29, true);
   }

   @Override
   public boolean visible() {
      return !(this.mc.field_1755 instanceof ClickGui);
   }

   @Override
   public void drawDraggable(class_332 var1, int var2) {
      if (this.mc.field_1724 != null) {
         float var3 = this.getX();
         float var4 = this.getY();
         Render2D.gradientRect(var3, var4, 105.0F, 29.0F, ClientTheme.bgGradient((int)(200 * var2 / 255.0F)), 6.0F);
         Render2D.outline(var3, var4, 105.0F, 29.0F, 0.5F, ClientTheme.outline((int)(200 * var2 / 255.0F)), 6.0F);

         for (int var5 = 0; var5 < 4; var5++) {
            float var6 = var3 + var5 * 27;
            float var7 = var4;
            this.drawSlot(var1, var6, var7, SLOTS_ORDER[var5], var2);
         }

         this.setWidth(105);
         this.setHeight(29);
      }
   }

   private void drawSlot(class_332 var1, float var2, float var3, class_1304 var4, int var5) {
      class_1799 var6 = this.mc.field_1724.method_6118(var4);
      int var7 = var6.method_7960() ? new Color(30, 30, 35, (int)(140 * var5 / 255.0F)).getRGB() : new Color(45, 45, 55, (int)(160 * var5 / 255.0F)).getRGB();
      Render2D.rect(var2 + 1.0F, var3 + 1.0F, 22.0F, 27.0F, var7, 5.0F);
      if (!var6.method_7960()) {
         var1.method_51427(var6, (int)(var2 + 4.0F), (int)(var3 + 4.0F));
         if (var6.method_7963() && var6.method_7936() > 0) {
            float var8 = 1.0F - (float)var6.method_7919() / var6.method_7936();
            float var9 = var3 + 4.0F + 16.0F + 2.0F;
            float var10 = 16.0F;
            Render2D.rect(var2 + 4.0F, var9, var10, 3.0F, new Color(20, 20, 25, (int)(180 * var5 / 255.0F)).getRGB(), 1.5F);
            int var11 = this.getDurabilityColor(var8, var5);
            if (var8 > 0.0F) {
               Render2D.rect(var2 + 4.0F, var9, var10 * var8, 3.0F, var11, 1.5F);
            }
         }
      } else {
         float var12 = var2 + 12.0F - 1.5F;
         float var13 = var3 + 14.5F - 1.5F;
         Render2D.rect(var12, var13, 3.0F, 3.0F, new Color(80, 80, 100, (int)(120 * var5 / 255.0F)).getRGB(), 1.5F);
      }
   }

   private int getDurabilityColor(float var1, int var2) {
      int var3 = (int)(200 * var2 / 255.0F);
      if (var1 > 0.75F) {
         return new Color(85, 220, 85, var3).getRGB();
      } else if (var1 > 0.5F) {
         return new Color(220, 220, 85, var3).getRGB();
      } else {
         return var1 > 0.25F ? new Color(220, 150, 85, var3).getRGB() : new Color(220, 85, 85, var3).getRGB();
      }
   }

   @Override
   public void tick() {
   }
}
