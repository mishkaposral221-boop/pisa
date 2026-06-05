package rich.modules.impl.combat;

import java.util.List;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import rich.Initialization;
import rich.events.api.EventHandler;
import rich.events.impl.ClickSlotEvent;
import rich.events.impl.DrawEvent;
import rich.events.impl.KeyEvent;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BindSetting;
import rich.modules.module.setting.implement.ButtonSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.modules.module.setting.implement.TextSetting;
import rich.util.c;
import rich.util.inventory.InventoryUtils;
import rich.util.render.pipeline.WheelPipeline;

public class AutoSwap extends ModuleStructure {
   public final BindSetting wheelBind = new BindSetting("Бинд колеса", "Клавиша открытия колеса");
   public final SelectSetting mode = new SelectSetting("Метод свапа", "F = имитация нажатия (без инвентарных пакетов, только из хотбара); Инвентарь = свап из любого слота").value("F", "Инвентарь");
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
   public final SliderSettings openDelay = new SliderSettings("Задержка открытия", "Тиков от открытия инвентаря до свапа (режим Инвентарь)").range(0, 40).setValue(8.0F);
   public final SliderSettings closeDelay = new SliderSettings("Задержка закрытия", "Тиков от свапа до закрытия инвентаря (режим Инвентарь)").range(0, 40).setValue(6.0F);
   private boolean wheelOpen = false;
   private boolean cursorUnlocked = false;
   private int lastHover = -1;
   private int pickingForSlot = -1;
   private boolean swapViaInv = false;
   private int swapStage = 0;
   private int swapTicks = 0;
   private int swapSlotId = -1;
   private int swapHotbar = -1;
   private int swapPrevSlot = -1;

   public static AutoSwap getInstance() {
      return c.a(AutoSwap.class);
   }

   public AutoSwap() {
      super("AutoSwap", "Свап предметов", ModuleCategory.UTILITIES);
      this.settings(this.wheelBind, this.mode, this.slot1, this.pick1, this.slot2, this.pick2, this.slot3, this.pick3, this.openDelay, this.closeDelay);
      this.slot1.setText("minecraft:totem_of_undying");
      this.slot2.setText("minecraft:golden_apple");
      this.slot3.setText("minecraft:shield");
   }

   private void openPickerFor(int var1) {
      if (mc.player != null) {
         this.pickingForSlot = var1;
         mc.setScreen(new InventoryScreen(mc.player));
      }
   }

   @EventHandler
   public void onClickSlot(ClickSlotEvent var1) {
      if (mc.player != null && this.pickingForSlot != -1) {
         if (mc.currentScreen instanceof InventoryScreen) {
            if (var1.getActionType() == SlotActionType.PICKUP) {
               DefaultedList var2 = mc.player.playerScreenHandler.slots;
               if (var1.getSlotId() >= 0 && var1.getSlotId() < var2.size()) {
                  ItemStack var3 = ((Slot)var2.get(var1.getSlotId())).getStack();
                  if (!var3.isEmpty()) {
                     Identifier var4 = Registries.ITEM.getId(var3.getItem());
                     List var5 = List.of(this.slot1, this.slot2, this.slot3);
                     if (this.pickingForSlot < var5.size()) {
                        ((TextSetting)var5.get(this.pickingForSlot)).setText(var4.toString());
                     }

                     var1.cancel();
                     this.pickingForSlot = -1;
                     mc.setScreen(null);
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onKey(KeyEvent var1) {
      if (mc.player != null) {
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

   // Two ways to put the chosen item into the offhand:
   //  * "F"        -> imitate the vanilla swap-hands key press (KeyBinding.onKeyPressed). The game itself
   //                  emits the SWAP_ITEM_WITH_OFFHAND PlayerAction - no inventory click packets at all,
   //                  so the "(Inventory)" check never fires. Slot select/restore is done with
   //                  setSelectedSlot (vanilla auto-syncs it like a normal scroll). Hotbar items only.
   //                  Each step lives on its own tick so it never looks like a "multi action".
   //  * "Inventory"-> open the inventory GUI and do a single clickSlot SWAP (works for any slot, but
   //                  uses an inventory packet that the server may flag).
   @EventHandler
   public void onTick(TickEvent var1) {
      if (mc.player == null || this.swapStage == 0) {
         return;
      }

      if (this.swapViaInv) {
         this.tickInventorySwap();
      } else {
         this.tickKeySwap();
      }
   }

   private void tickKeySwap() {
      switch (this.swapStage) {
         case 1:
            if (this.swapHotbar >= 0 && this.swapHotbar <= 8) {
               mc.player.getInventory().setSelectedSlot(this.swapHotbar);
            }

            this.swapTicks = 0;
            this.swapStage = 2;
            break;
         case 2:
            if (++this.swapTicks >= 1) {
               this.pressSwapHands();
               this.swapTicks = 0;
               this.swapStage = 3;
            }
            break;
         case 3:
            if (++this.swapTicks >= 1) {
               if (this.swapPrevSlot >= 0 && this.swapPrevSlot <= 8) {
                  mc.player.getInventory().setSelectedSlot(this.swapPrevSlot);
               }

               this.resetSwap();
            }
            break;
         default:
            this.resetSwap();
      }
   }

   private void tickInventorySwap() {
      if (mc.interactionManager == null) {
         return;
      }

      switch (this.swapStage) {
         case 1:
            if (!(mc.currentScreen instanceof InventoryScreen)) {
               mc.setScreen(new InventoryScreen(mc.player));
            }

            this.swapTicks = 0;
            this.swapStage = 2;
            break;
         case 2:
            if (++this.swapTicks >= this.openDelay.getInt()) {
               if (mc.currentScreen instanceof InventoryScreen && this.swapSlotId >= 0) {
                  mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, this.swapSlotId, 40, SlotActionType.SWAP, mc.player);
               }

               this.swapTicks = 0;
               this.swapStage = 3;
            }
            break;
         case 3:
            if (++this.swapTicks >= this.closeDelay.getInt()) {
               if (mc.currentScreen instanceof InventoryScreen) {
                  mc.setScreen(null);
               }

               this.resetSwap();
            }
            break;
         default:
            this.resetSwap();
      }
   }

   private void pressSwapHands() {
      if (mc.options != null) {
         InputUtil.Key var1 = KeyBindingHelper.getBoundKeyOf(mc.options.swapHandsKey);
         KeyBinding.onKeyPressed(var1);
      }
   }

   private boolean requestSwap(ItemStack var1) {
      if (mc.player == null || this.swapStage != 0 || var1.isEmpty()) {
         return false;
      }

      if (this.mode.isSelected("Инвентарь")) {
         Slot var2 = InventoryUtils.findSlotAnywhere(var1.getItem());
         if (var2 != null) {
            this.swapViaInv = true;
            this.swapSlotId = var2.id;
            this.swapTicks = 0;
            this.swapStage = 1;
            return true;
         }
      } else {
         int var3 = InventoryUtils.findItemInHotbar(var1.getItem());
         if (var3 != -1) {
            this.swapViaInv = false;
            this.swapHotbar = var3;
            this.swapPrevSlot = mc.player.getInventory().getSelectedSlot();
            this.swapTicks = 0;
            this.swapStage = 1;
            return true;
         }
      }

      return false;
   }

   private void resetSwap() {
      this.swapViaInv = false;
      this.swapStage = 0;
      this.swapTicks = 0;
      this.swapSlotId = -1;
      this.swapHotbar = -1;
      this.swapPrevSlot = -1;
   }

   @EventHandler
   public void onDraw(DrawEvent var1) {
      if (mc.player != null) {
         if (this.wheelOpen) {
            if (mc.currentScreen == null) {
               this.setCursorUnlocked(true);
               int var2 = var1.getDrawContext().getScaledWindowWidth();
               int var3 = var1.getDrawContext().getScaledWindowHeight();
               float var4 = var2 / 2.0F;
               float var5 = var3 / 2.0F;
               float var6 = 92.0F;
               float var7 = 54.0F;
               float var8 = (float)(mc.mouse.getX() * var2 / mc.getWindow().getWidth());
               float var9 = (float)(mc.mouse.getY() * var3 / mc.getWindow().getHeight());
               byte var10 = 3;
               int var11 = this.getHoverIndex(var8, var9, var4, var5, var7, var6, var10);
               if (var11 != -1 && var11 != this.lastHover) {
                  this.lastHover = var11;
                  ItemStack var12 = this.getStackForIndex(var11);
                  if (!var12.isEmpty() && this.requestSwap(var12)) {
                     this.wheelOpen = false;
                     this.lastHover = -1;
                     this.setCursorUnlocked(false);
                     return;
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
                  ItemStack var24 = this.getStackForIndex(var23);
                  if (!var24.isEmpty()) {
                     float var25 = (float)Math.toRadians(-90.0F + var21 * var23 + var21 / 2.0F);
                     float var26 = (var7 + var6) / 2.0F;
                     float var19 = var4 + (float)Math.cos(var25) * var26;
                     float var20 = var5 + (float)Math.sin(var25) * var26;
                     var1.getDrawContext().drawItem(var24, (int)(var19 - 8.0F), (int)(var20 - 8.0F));
                  }
               }
            }
         }
      }
   }

   private void setCursorUnlocked(boolean var1) {
      if (mc.mouse != null) {
         if (var1 && !this.cursorUnlocked) {
            mc.mouse.unlockCursor();
            this.cursorUnlocked = true;
         } else if (!var1 && this.cursorUnlocked) {
            if (mc.currentScreen == null) {
               mc.mouse.lockCursor();
            }

            this.cursorUnlocked = false;
         }
      }
   }

   @Override
   public void deactivate() {
      this.wheelOpen = false;
      this.pickingForSlot = -1;
      this.resetSwap();
      this.setCursorUnlocked(false);
   }

   private ItemStack getStackForIndex(int var1) {
      List var2 = List.of(this.slot1, this.slot2, this.slot3);
      if (var1 >= 0 && var1 < var2.size()) {
         String var3 = ((TextSetting)var2.get(var1)).getText();
         if (var3 != null && !var3.isBlank()) {
            Identifier var4 = Identifier.tryParse(var3);
            if (var4 == null) {
               return ItemStack.EMPTY;
            }

            Item var5 = (Item)Registries.ITEM.get(var4);
            return var5 != null && var5 != Items.AIR ? var5.getDefaultStack() : ItemStack.EMPTY;
         } else {
            return ItemStack.EMPTY;
         }
      } else {
         return ItemStack.EMPTY;
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
