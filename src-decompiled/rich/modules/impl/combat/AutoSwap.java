package rich.modules.impl.combat;

import java.util.List;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2371;
import net.minecraft.class_2960;
import net.minecraft.class_490;
import net.minecraft.class_7923;
import rich.Initialization;
import rich.events.api.EventHandler;
import rich.events.impl.ClickSlotEvent;
import rich.events.impl.DrawEvent;
import rich.events.impl.KeyEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BindSetting;
import rich.modules.module.setting.implement.ButtonSetting;
import rich.modules.module.setting.implement.TextSetting;
import rich.util.c;
import rich.util.inventory.InventoryUtils;
import rich.util.render.pipeline.WheelPipeline;

public class AutoSwap extends ModuleStructure {
   public final BindSetting wheelBind = new BindSetting("Бинд колеса", "Клавиша открытия колеса");
   public final TextSetting slot1 = new TextSetting("Слот 1", "ID предмета");
   public final ButtonSetting pick1 = new ButtonSetting("Выбрать слот 1", "Открыть инвентарь")
      .setButtonName("Выбрать")
      .setRunnable(() -> this.openPickerFor(0));
   public final TextSetting slot2 = new TextSetting("Слот 2", "ID предмета");
   public final ButtonSetting pick2 = new ButtonSetting("Выбрать слот 2", "Открыть инвентарь")
      .setButtonName("Выбрать")
      .setRunnable(() -> this.openPickerFor(1));
   public final TextSetting slot3 = new TextSetting("Слот 3", "ID предмета");
   public final ButtonSetting pick3 = new ButtonSetting("Выбрать слот 3", "Открыть инвентарь")
      .setButtonName("Выбрать")
      .setRunnable(() -> this.openPickerFor(2));
   private boolean wheelOpen = false;
   private boolean cursorUnlocked = false;
   private int lastHover = -1;
   private int pickingForSlot = -1;

   public static AutoSwap getInstance() {
      return c.a(AutoSwap.class);
   }

   public AutoSwap() {
      super("AutoSwap", "Свап предметов", ModuleCategory.UTILITIES);
      this.settings(this.wheelBind, this.slot1, this.pick1, this.slot2, this.pick2, this.slot3, this.pick3);
      this.slot1.setText("minecraft:totem_of_undying");
      this.slot2.setText("minecraft:golden_apple");
      this.slot3.setText("minecraft:shield");
   }

   private void openPickerFor(int var1) {
      if (mc.field_1724 != null) {
         this.pickingForSlot = var1;
         mc.method_1507(new class_490(mc.field_1724));
      }
   }

   @EventHandler
   public void onClickSlot(ClickSlotEvent var1) {
      if (mc.field_1724 != null && this.pickingForSlot != -1) {
         if (mc.field_1755 instanceof class_490) {
            if (var1.getActionType() == class_1713.field_7790) {
               class_2371 var2 = mc.field_1724.field_7498.field_7761;
               if (var1.getSlotId() >= 0 && var1.getSlotId() < var2.size()) {
                  class_1799 var3 = ((class_1735)var2.get(var1.getSlotId())).method_7677();
                  if (!var3.method_7960()) {
                     class_2960 var4 = class_7923.field_41178.method_10221(var3.method_7909());
                     List var5 = List.of(this.slot1, this.slot2, this.slot3);
                     if (this.pickingForSlot < var5.size()) {
                        ((TextSetting)var5.get(this.pickingForSlot)).setText(var4.toString());
                     }

                     var1.cancel();
                     this.pickingForSlot = -1;
                     mc.method_1507(null);
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onKey(KeyEvent var1) {
      if (mc.field_1724 != null) {
         if (var1.isKeyDown(this.wheelBind.getKey(), true)) {
            this.wheelOpen = !this.wheelOpen;
            if (this.wheelOpen) {
               this.lastHover = -1;
               this.setCursorUnlocked(true);
            } else {
               this.setCursorUnlocked(false);
            }
         }
      }
   }

   @EventHandler
   public void onDraw(DrawEvent var1) {
      if (mc.field_1724 != null) {
         if (this.wheelOpen) {
            if (mc.field_1755 == null) {
               this.setCursorUnlocked(true);
               int var2 = var1.getDrawContext().method_51421();
               int var3 = var1.getDrawContext().method_51443();
               float var4 = var2 / 2.0F;
               float var5 = var3 / 2.0F;
               float var6 = 92.0F;
               float var7 = 54.0F;
               float var8 = (float)(mc.field_1729.method_1603() * var2 / mc.method_22683().method_4480());
               float var9 = (float)(mc.field_1729.method_1604() * var3 / mc.method_22683().method_4507());
               byte var10 = 3;
               int var11 = this.getHoverIndex(var8, var9, var4, var5, var7, var6, var10);
               if (var11 != -1 && var11 != this.lastHover) {
                  this.lastHover = var11;
                  class_1799 var12 = this.getStackForIndex(var11);
                  if (!var12.method_7960()) {
                     int var13 = InventoryUtils.findItemInHotbar(var12.method_7909());
                     if (var13 != -1) {
                        InventoryUtils.swapOffhandWithSlot(36 + var13);
                        this.wheelOpen = false;
                        this.lastHover = -1;
                        this.setCursorUnlocked(false);
                        return;
                     }
                  }
               }

               float var21 = 360.0F / var10;
               float var22 = 2.0F;
               WheelPipeline var14 = Initialization.getInstance().getManager().getRenderCore().getWheelPipeline();

               for (int var15 = 0; var15 < var10; var15++) {
                  int var16 = var15 == var11 ? -1593847505 : 1624100301;
                  float var17 = -90.0F + var21 * var15 + var22 / 2.0F;
                  float var18 = var17 + var21 - var22;
                  var14.drawSegment(var4, var5, var7, var6, (float)Math.toRadians(var17), (float)Math.toRadians(var18), var16);
               }

               for (int var23 = 0; var23 < var10; var23++) {
                  class_1799 var24 = this.getStackForIndex(var23);
                  if (!var24.method_7960()) {
                     float var25 = (float)Math.toRadians(-90.0F + var21 * var23 + var21 / 2.0F);
                     float var26 = (var7 + var6) / 2.0F;
                     float var19 = var4 + (float)Math.cos(var25) * var26;
                     float var20 = var5 + (float)Math.sin(var25) * var26;
                     var1.getDrawContext().method_51427(var24, (int)(var19 - 8.0F), (int)(var20 - 8.0F));
                  }
               }
            }
         }
      }
   }

   private void setCursorUnlocked(boolean var1) {
      if (mc.field_1729 != null) {
         if (var1 && !this.cursorUnlocked) {
            mc.field_1729.method_1610();
            this.cursorUnlocked = true;
         } else if (!var1 && this.cursorUnlocked) {
            if (mc.field_1755 == null) {
               mc.field_1729.method_1612();
            }

            this.cursorUnlocked = false;
         }
      }
   }

   @Override
   public void deactivate() {
      this.wheelOpen = false;
      this.pickingForSlot = -1;
      this.setCursorUnlocked(false);
   }

   private class_1799 getStackForIndex(int var1) {
      List var2 = List.of(this.slot1, this.slot2, this.slot3);
      if (var1 >= 0 && var1 < var2.size()) {
         String var3 = ((TextSetting)var2.get(var1)).getText();
         if (var3 != null && !var3.isBlank()) {
            class_2960 var4 = class_2960.method_12829(var3);
            if (var4 == null) {
               return class_1799.field_8037;
            }

            class_1792 var5 = (class_1792)class_7923.field_41178.method_63535(var4);
            return var5 != null && var5 != class_1802.field_8162 ? var5.method_7854() : class_1799.field_8037;
         } else {
            return class_1799.field_8037;
         }
      } else {
         return class_1799.field_8037;
      }
   }

   private int getHoverIndex(float var1, float var2, float var3, float var4, float var5, float var6, int var7) {
      float var8 = var1 - var3;
      float var9 = var2 - var4;
      float var10 = (float)Math.sqrt(var8 * var8 + var9 * var9);
      if (!(var10 < var5) && !(var10 > var6)) {
         double var11 = Math.atan2(var9, var8) + (Math.PI / 2);
         if (var11 < 0.0) {
            var11 += Math.PI * 2;
         }

         int var13 = (int)Math.floor(var11 / (Math.PI * 2) * var7);
         return Math.max(0, Math.min(var13, var7 - 1));
      } else {
         return -1;
      }
   }
}
