package rich.modules.impl.combat;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import rich.events.api.EventHandler;
import rich.events.impl.ClickSlotEvent;
import rich.events.impl.KeyEvent;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BindSetting;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.ButtonSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.modules.module.setting.implement.TextSetting;
import rich.util.c;

public class AutoSwap extends ModuleStructure {
   public static volatile boolean SUPPRESS_SPRINT = false;

   private static final int PHASE_IDLE = 0;
   private static final int PHASE_PRE_OPEN = 1;
   private static final int PHASE_AFTER_OPEN = 2;
   private static final int PHASE_BEFORE_CLICK = 3;
   private static final int PHASE_CLOSING = 4;
   private static final int OFFHAND_BUTTON = 40;
   private static final int OFFHAND_SLOT = 45;

   public final BindSetting swapBind = new BindSetting("\u0411\u0438\u043d\u0434 \u0441\u0432\u0430\u043f\u0430", "\u041e\u043f\u0446\u0438\u043e\u043d\u0430\u043b\u044c\u043d\u044b\u0439 \u0440\u0443\u0447\u043d\u043e\u0439 \u0442\u0440\u0438\u0433\u0433\u0435\u0440 \u0441\u0432\u0430\u043f\u0430");
   public final BooleanSetting autoSwap = new BooleanSetting("\u0410\u0432\u0442\u043e-\u0441\u0432\u0430\u043f", "\u0421\u0432\u0430\u043f\u0430\u0442\u044c \u0430\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438, \u043f\u043e\u043a\u0430 \u043c\u043e\u0434\u0443\u043b\u044c \u0432\u043a\u043b\u044e\u0447\u0451\u043d")
      .setValue(true);
   public final SelectSetting swapMode = new SelectSetting("\u0420\u0435\u0436\u0438\u043c \u0441\u0432\u0430\u043f\u0430", "Legit \u043e\u0442\u043a\u0440\u044b\u0432\u0430\u0435\u0442 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044c, Packet \u0441\u0432\u0430\u043f\u0430\u0435\u0442 \u0431\u0435\u0437 \u044d\u043a\u0440\u0430\u043d\u0430")
      .value("Legit", "Packet")
      .selected("Legit");
   public final SliderSettings preOpenDelay = new SliderSettings("\u0414\u043e \u043e\u0442\u043a\u0440\u044b\u0442\u0438\u044f", "\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430 \u0434\u043e \u043e\u0442\u043a\u0440\u044b\u0442\u0438\u044f \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044f \u0432 \u0442\u0438\u043a\u0430\u0445")
      .setValue(1.0F)
      .range(0, 20)
      .visible(() -> this.swapMode.isSelected("Legit"));
   public final SliderSettings afterOpenDelay = new SliderSettings("\u041f\u043e\u0441\u043b\u0435 \u043e\u0442\u043a\u0440\u044b\u0442\u0438\u044f", "\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430 \u043f\u043e\u0441\u043b\u0435 \u043e\u0442\u043a\u0440\u044b\u0442\u0438\u044f \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044f \u0432 \u0442\u0438\u043a\u0430\u0445")
      .setValue(3.0F)
      .range(0, 20)
      .visible(() -> this.swapMode.isSelected("Legit"));
   public final SliderSettings beforeClickDelay = new SliderSettings("\u041f\u0435\u0440\u0435\u0434 F", "\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430 \u043f\u0435\u0440\u0435\u0434 \u043d\u0430\u0436\u0430\u0442\u0438\u0435\u043c F \u043f\u043e \u0441\u043b\u043e\u0442\u0443")
      .setValue(1.0F)
      .range(0, 20)
      .visible(() -> this.swapMode.isSelected("Legit"));
   public final SliderSettings closeDelay = new SliderSettings("\u041f\u0435\u0440\u0435\u0434 \u0437\u0430\u043a\u0440\u044b\u0442\u0438\u0435\u043c", "\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430 \u043f\u0435\u0440\u0435\u0434 \u0437\u0430\u043a\u0440\u044b\u0442\u0438\u0435\u043c \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044f \u0432 \u0442\u0438\u043a\u0430\u0445")
      .setValue(1.0F)
      .range(0, 20)
      .visible(() -> this.swapMode.isSelected("Legit"));
   public final SliderSettings randomDelay = new SliderSettings("\u0420\u0430\u043d\u0434\u043e\u043c \u0437\u0430\u0434\u0435\u0440\u0436\u043a\u0438", "\u0414\u043e\u043f\u043e\u043b\u043d\u0438\u0442\u0435\u043b\u044c\u043d\u044b\u0439 \u0441\u043b\u0443\u0447\u0430\u0439\u043d\u044b\u0439 \u0440\u0430\u0437\u0431\u0440\u043e\u0441 \u0432 \u0442\u0438\u043a\u0430\u0445")
      .setValue(1.0F)
      .range(0, 10)
      .visible(() -> this.swapMode.isSelected("Legit"));
   public final SliderSettings cooldown = new SliderSettings("Cooldown", "\u041c\u0438\u043d\u0438\u043c\u0430\u043b\u044c\u043d\u0430\u044f \u043f\u0430\u0443\u0437\u0430 \u043c\u0435\u0436\u0434\u0443 \u0441\u0432\u0430\u043f\u0430\u043c\u0438 \u0432 \u043c\u0438\u043b\u043b\u0438\u0441\u0435\u043a\u0443\u043d\u0434\u0430\u0445")
      .setValue(250.0F)
      .range(0, 3000);
   public final BooleanSetting stopMovement = new BooleanSetting("\u041e\u0441\u0442\u0430\u043d\u043e\u0432\u043a\u0430", "\u041e\u0441\u0442\u0430\u043d\u0430\u0432\u043b\u0438\u0432\u0430\u0442\u044c \u0438\u0433\u0440\u043e\u043a\u0430 \u0432\u043e \u0432\u0440\u0435\u043c\u044f legit-\u0441\u0432\u0430\u043f\u0430")
      .setValue(true)
      .visible(() -> this.swapMode.isSelected("Legit"));

   public final TextSetting slot1 = new TextSetting("\u041f\u0440\u0435\u0434\u043c\u0435\u0442 1", "ID \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430");
   public final ButtonSetting pick1 = new ButtonSetting("\u0412\u044b\u0431\u0440\u0430\u0442\u044c \u043f\u0440\u0435\u0434\u043c\u0435\u0442 1", "\u041e\u0442\u043a\u0440\u044b\u0442\u044c \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044c")
      .setButtonName("\u0412\u044b\u0431\u0440\u0430\u0442\u044c")
      .setRunnable(() -> this.openPickerFor(0));
   public final TextSetting slot2 = new TextSetting("\u041f\u0440\u0435\u0434\u043c\u0435\u0442 2", "ID \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430");
   public final ButtonSetting pick2 = new ButtonSetting("\u0412\u044b\u0431\u0440\u0430\u0442\u044c \u043f\u0440\u0435\u0434\u043c\u0435\u0442 2", "\u041e\u0442\u043a\u0440\u044b\u0442\u044c \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044c")
      .setButtonName("\u0412\u044b\u0431\u0440\u0430\u0442\u044c")
      .setRunnable(() -> this.openPickerFor(1));
   public final TextSetting slot3 = new TextSetting("\u041f\u0440\u0435\u0434\u043c\u0435\u0442 3", "ID \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430");
   public final ButtonSetting pick3 = new ButtonSetting("\u0412\u044b\u0431\u0440\u0430\u0442\u044c \u043f\u0440\u0435\u0434\u043c\u0435\u0442 3", "\u041e\u0442\u043a\u0440\u044b\u0442\u044c \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044c")
      .setButtonName("\u0412\u044b\u0431\u0440\u0430\u0442\u044c")
      .setRunnable(() -> this.openPickerFor(2));

   private int pickingForSlot = -1;
   private Item pendingItem = null;
   private Item targetItem = null;
   private int swapPhase = PHASE_IDLE;
   private int phaseTimer = 0;
   private boolean sentSprintStop = false;
   private boolean openedInventoryScreen = false;
   private long lastSwapMs = 0L;

   public static AutoSwap getInstance() {
      return c.a(AutoSwap.class);
   }

   public AutoSwap() {
      super("AutoSwap", "\u0421\u0432\u0430\u043f \u0442\u0430\u043b\u0438\u043a\u0430 \u0432\u043e \u0432\u0442\u043e\u0440\u0443\u044e \u0440\u0443\u043a\u0443", ModuleCategory.UTILITIES);
      this.settings(
         this.swapBind,
         this.autoSwap,
         this.swapMode,
         this.preOpenDelay,
         this.afterOpenDelay,
         this.beforeClickDelay,
         this.closeDelay,
         this.randomDelay,
         this.cooldown,
         this.stopMovement,
         this.slot1,
         this.pick1,
         this.slot2,
         this.pick2,
         this.slot3,
         this.pick3
      );
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
      if (mc.player != null && this.pickingForSlot != -1 && mc.currentScreen instanceof InventoryScreen) {
         if (var1.getActionType() == SlotActionType.PICKUP) {
            DefaultedList var2 = mc.player.playerScreenHandler.slots;
            if (var1.getSlotId() >= 0 && var1.getSlotId() < var2.size()) {
               ItemStack var3 = ((Slot)var2.get(var1.getSlotId())).getStack();
               if (!var3.isEmpty()) {
                  Identifier var4 = Registries.ITEM.getId(var3.getItem());
                  TextSetting setting = this.getSlotSetting(this.pickingForSlot);
                  if (setting != null) {
                     setting.setText(var4.toString());
                  }

                  var1.cancel();
                  this.pickingForSlot = -1;
                  mc.setScreen(null);
               }
            }
         }
      }
   }

   @EventHandler
   public void onKey(KeyEvent var1) {
      if (mc.player != null && this.swapBind.getKey() != -1) {
         if (var1.isKeyDown(this.swapBind.getKey(), true)) {
            this.pendingItem = this.resolveTargetItem();
         }
      }
   }

   @EventHandler
   public void onTick(TickEvent var1) {
      if (mc.player == null || mc.world == null || mc.interactionManager == null) {
         this.resetSwap();
         return;
      }

      if (this.swapPhase == PHASE_IDLE) {
         if (this.pendingItem != null) {
            Item var2 = this.pendingItem;
            this.pendingItem = null;
            this.beginSwap(var2);
            return;
         }

         // \u0410\u0432\u0442\u043e-\u0442\u0440\u0438\u0433\u0433\u0435\u0440: \u0434\u0435\u0440\u0436\u0438\u043c \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u044b\u0439 \u043f\u0440\u0435\u0434\u043c\u0435\u0442 \u0432\u043e \u0432\u0442\u043e\u0440\u043e\u0439 \u0440\u0443\u043a\u0435, \u043f\u043e\u043a\u0430 \u043c\u043e\u0434\u0443\u043b\u044c \u0432\u043a\u043b\u044e\u0447\u0451\u043d.
         if (this.autoSwap.isValue() && mc.currentScreen == null) {
            Item offhand = mc.player.getOffHandStack().getItem();
            if (!this.isConfiguredItem(offhand)) {
               Item auto = this.resolveTargetItem();
               if (auto != null && offhand != auto) {
                  this.beginSwap(auto);
               }
            }
         }

         return;
      }

      if (mc.currentScreen != null && !(mc.currentScreen instanceof InventoryScreen)) {
         this.resetSwap();
         return;
      }

      if (!this.sentSprintStop) {
         this.stopServerSprint();
         this.sentSprintStop = true;
      }

      if (this.stopMovement.isValue()) {
         this.haltMovement();
      }

      if (this.phaseTimer > 0) {
         this.phaseTimer--;
         return;
      }

      if (this.swapPhase == PHASE_PRE_OPEN) {
         if (!(mc.currentScreen instanceof InventoryScreen)) {
            this.openedInventoryScreen = true;
            mc.setScreen(new InventoryScreen(mc.player));
         }

         this.swapPhase = PHASE_AFTER_OPEN;
         this.phaseTimer = this.delayTicks(this.afterOpenDelay);
         return;
      }

      if (this.swapPhase == PHASE_AFTER_OPEN) {
         if (!(mc.currentScreen instanceof InventoryScreen)) {
            this.resetSwap();
            return;
         }

         this.swapPhase = PHASE_BEFORE_CLICK;
         this.phaseTimer = this.delayTicks(this.beforeClickDelay);
         return;
      }

      if (this.swapPhase == PHASE_BEFORE_CLICK) {
         if (!(mc.currentScreen instanceof InventoryScreen)) {
            this.resetSwap();
            return;
         }

         this.executeOffhandSwap(this.targetItem);
         this.swapPhase = PHASE_CLOSING;
         this.phaseTimer = this.delayTicks(this.closeDelay);
         return;
      }

      if (this.swapPhase == PHASE_CLOSING) {
         if (mc.currentScreen instanceof InventoryScreen && this.openedInventoryScreen) {
            mc.setScreen(null);
         }

         this.resetSwap();
      }
   }

   private void beginSwap(Item var1) {
      if (mc.player == null || var1 == null || var1 == Items.AIR) {
         return;
      }

      long now = System.currentTimeMillis();
      if (now - this.lastSwapMs < (long)this.cooldown.getInt()) {
         return;
      }

      if (mc.player.getOffHandStack().getItem() == var1) {
         return;
      }

      if (this.findSlotForItem(var1) == null) {
         return;
      }

      if (mc.currentScreen != null && !(mc.currentScreen instanceof InventoryScreen)) {
         return;
      }

      this.targetItem = var1;

      if (this.swapMode.isSelected("Packet")) {
         this.executeOffhandSwap(var1);
         this.targetItem = null;
         return;
      }

      this.sentSprintStop = false;
      SUPPRESS_SPRINT = true;
      if (this.stopMovement.isValue()) {
         this.haltMovement();
      }

      if (mc.currentScreen instanceof InventoryScreen) {
         this.openedInventoryScreen = false;
         this.swapPhase = PHASE_AFTER_OPEN;
         this.phaseTimer = this.delayTicks(this.afterOpenDelay);
      } else {
         this.openedInventoryScreen = false;
         this.swapPhase = PHASE_PRE_OPEN;
         this.phaseTimer = this.delayTicks(this.preOpenDelay);
      }
   }

   private Item resolveTargetItem() {
      Item var1 = this.parseItem(this.slot1.getText());
      if (var1 != null && this.findSlotForItem(var1) != null) {
         return var1;
      }

      Item var2 = this.parseItem(this.slot2.getText());
      if (var2 != null && this.findSlotForItem(var2) != null) {
         return var2;
      }

      Item var3 = this.parseItem(this.slot3.getText());
      if (var3 != null && this.findSlotForItem(var3) != null) {
         return var3;
      }

      // \u041d\u0438\u0447\u0435\u0433\u043e \u043d\u0435\u0442 \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435 \u2014 \u0432\u043e\u0437\u0432\u0440\u0430\u0449\u0430\u0435\u043c \u043f\u0435\u0440\u0432\u044b\u0439 \u0432\u0430\u043b\u0438\u0434\u043d\u044b\u0439, \u0447\u0442\u043e\u0431\u044b beginSwap \u043a\u043e\u0440\u0440\u0435\u043a\u0442\u043d\u043e \u043d\u0438\u0447\u0435\u0433\u043e \u043d\u0435 \u0441\u0434\u0435\u043b\u0430\u043b.
      return var1 != null ? var1 : (var2 != null ? var2 : var3);
   }

   private boolean isConfiguredItem(Item var1) {
      if (var1 == null || var1 == Items.AIR) {
         return false;
      }

      Item var2 = this.parseItem(this.slot1.getText());
      Item var3 = this.parseItem(this.slot2.getText());
      Item var4 = this.parseItem(this.slot3.getText());
      return var1 == var2 || var1 == var3 || var1 == var4;
   }

   private Item parseItem(String var1) {
      if (var1 == null || var1.isBlank()) {
         return null;
      }

      Identifier var2 = Identifier.tryParse(var1.trim());
      if (var2 == null) {
         return null;
      }

      Item var3 = (Item)Registries.ITEM.get(var2);
      return var3 != null && var3 != Items.AIR ? var3 : null;
   }

   private Slot findSlotForItem(Item var1) {
      if (mc.player == null || var1 == null || var1 == Items.AIR) {
         return null;
      }

      for (int var2 = 36; var2 <= 44; var2++) {
         Slot var3 = this.getPlayerSlot(var2);
         if (this.slotContains(var3, var1)) {
            return var3;
         }
      }

      for (int var4 = 9; var4 <= 35; var4++) {
         Slot var5 = this.getPlayerSlot(var4);
         if (this.slotContains(var5, var1)) {
            return var5;
         }
      }

      return null;
   }

   private Slot getPlayerSlot(int var1) {
      if (mc.player == null || var1 < 0 || var1 >= mc.player.playerScreenHandler.slots.size()) {
         return null;
      }

      return mc.player.playerScreenHandler.getSlot(var1);
   }

   private boolean slotContains(Slot var1, Item var2) {
      return var1 != null && !var1.getStack().isEmpty() && var1.getStack().getItem() == var2;
   }

   private boolean executeOffhandSwap(Item var1) {
      if (mc.player == null || mc.world == null || mc.interactionManager == null || var1 == null) {
         return false;
      }

      Slot var2 = this.findSlotForItem(var1);
      if (var2 == null) {
         return false;
      }

      int var3 = mc.player.currentScreenHandler.syncId;

      // \u041e\u0441\u043d\u043e\u0432\u043d\u043e\u0439 \u043f\u0443\u0442\u044c \u2014 \u043a\u0430\u043a F \u043f\u043e \u0441\u043b\u043e\u0442\u0443: SlotActionType.SWAP + button 40.
      mc.interactionManager.clickSlot(var3, var2.id, OFFHAND_BUTTON, SlotActionType.SWAP, mc.player);

      // Fallback, \u0435\u0441\u043b\u0438 SWAP \u043d\u0435 \u043f\u0440\u0438\u043c\u0435\u043d\u0438\u043b\u0441\u044f: \u043e\u0431\u044b\u0447\u043d\u044b\u0435 PICKUP-\u043a\u043b\u0438\u043a\u0438 \u0432\u0437\u044f\u0442\u044c/\u043f\u043e\u043b\u043e\u0436\u0438\u0442\u044c/\u0432\u0435\u0440\u043d\u0443\u0442\u044c.
      if (mc.player.getOffHandStack().getItem() != var1) {
         mc.interactionManager.clickSlot(var3, var2.id, 0, SlotActionType.PICKUP, mc.player);
         mc.interactionManager.clickSlot(var3, OFFHAND_SLOT, 0, SlotActionType.PICKUP, mc.player);
         mc.interactionManager.clickSlot(var3, var2.id, 0, SlotActionType.PICKUP, mc.player);
      }

      this.lastSwapMs = System.currentTimeMillis();
      return true;
   }

   private void stopServerSprint() {
      if (mc.player != null && mc.getNetworkHandler() != null) {
         mc.player.setSprinting(false);
         mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
      }
   }

   private void haltMovement() {
      if (mc.player != null) {
         mc.player.setSprinting(false);
         mc.player.setVelocity(0.0, mc.player.getVelocity().y, 0.0);
         mc.player.forwardSpeed = 0.0F;
         mc.player.sidewaysSpeed = 0.0F;
      }
   }

   private void resetSwap() {
      this.swapPhase = PHASE_IDLE;
      this.phaseTimer = 0;
      this.targetItem = null;
      this.sentSprintStop = false;
      this.openedInventoryScreen = false;
      SUPPRESS_SPRINT = false;
   }

   private int delayTicks(SliderSettings var1) {
      int var2 = Math.max(0, var1.getInt());
      int var3 = Math.max(0, this.randomDelay.getInt());
      return var2 + (var3 <= 0 ? 0 : ThreadLocalRandom.current().nextInt(var3 + 1));
   }

   @Override
   public void deactivate() {
      this.pickingForSlot = -1;
      this.pendingItem = null;
      this.resetSwap();
      if (mc.currentScreen instanceof InventoryScreen && this.openedInventoryScreen) {
         mc.setScreen(null);
      }
   }

   private TextSetting getSlotSetting(int index) {
      if (index == 0) return this.slot1;
      if (index == 1) return this.slot2;
      if (index == 2) return this.slot3;
      return null;
   }
}
