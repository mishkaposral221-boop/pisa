package rich.modules.impl.combat;

import java.util.Locale;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import rich.Initialization;
import rich.events.api.EventHandler;
import rich.events.impl.ClickSlotEvent;
import rich.events.impl.DrawEvent;
import rich.events.impl.KeyEvent;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BindSetting;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.ButtonSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.TextSetting;
import rich.util.c;
import rich.util.profiler.FrameProfiler;
import rich.util.render.pipeline.WheelPipeline;

public class AutoSwap extends ModuleStructure {
   public static volatile boolean SUPPRESS_SPRINT = false;
   public static volatile boolean SUPPRESS_INPUT = false;
   public static volatile boolean LOCK_ROTATION = false;
   public static volatile float LOCK_YAW = 0.0F;
   public static volatile float LOCK_PITCH = 0.0F;

   private static final int PHASE_IDLE = 0;
   private static final int PHASE_SAFE_WAIT_SCROLL = 10;
   private static final int PHASE_SAFE_SWAP = 11;
   private static final int PHASE_SAFE_RESTORE = 12;
   private static final int PHASE_INV_REACTION = 20;
   private static final int PHASE_INV_AFTER_CLICK = 21;
   private static final int PHASE_INV_AFTER_CLOSE = 22;
   // ClickSlot SWAP button=40 = ванильное «F в инвентаре». PlayerInventory.OFF_HAND_SLOT.
   private static final int OFFHAND_SWAP_BUTTON = 40;

   public final SelectSetting mode = new SelectSetting(
         "Режим",
         "Legit — ванильные пакеты, мгновенно, может свапать из инвентаря. Rage — то же самое, но только из хотбара.")
      .value("Legit", "Rage")
      .selected("Legit");

   public final SelectSetting triggerMode = new SelectSetting(
         "Триггер",
         "Колесо — выбор через радиальное меню. Без колеса — авто-свап по содержимому offhand.")
      .value("Колесо", "Без колеса")
      .selected("Без колеса");
   public final BindSetting wheelBind = new BindSetting("Бинд колеса", "Зажми, наведи на предмет и отпусти.")
      .visible(() -> this.triggerMode.isSelected("Колесо"));
   public final BindSetting swapBind = new BindSetting("Бинд свапа", "Ручной триггер.")
      .visible(() -> this.triggerMode.isSelected("Без колеса"));

   public final BooleanSetting legitInvSwap = new BooleanSetting(
         "Свап из инвентаря",
         "Открываем инвентарь, жмём F на предмете — он уходит в offhand. Хотбар не затрагивается. Только Legit.")
      .setValue(true).visible(() -> this.mode.isSelected("Legit"));

   public final BooleanSetting keepOriginalSlot = new BooleanSetting("Возвращать слот", "Вернуть выделение на оригинальный слот после свапа.").setValue(true);
   public final BooleanSetting lockRotation = new BooleanSetting("Lock rotation", "Замораживать поворот во время свапа.").setValue(false);
   public final BooleanSetting antiInventoryMove = new BooleanSetting("Анти InventoryMove", "Гасит инпут во время свапа.").setValue(false);

   public final TextSetting slot1 = new TextSetting("Предмет 1", "ID или алиас");
   public final ButtonSetting pick1 = new ButtonSetting("Выбрать 1", "Открыть инвентарь")
      .setButtonName("Выбрать").setRunnable(() -> this.openPickerFor(0));
   public final TextSetting slot2 = new TextSetting("Предмет 2", "ID или алиас");
   public final ButtonSetting pick2 = new ButtonSetting("Выбрать 2", "Открыть инвентарь")
      .setButtonName("Выбрать").setRunnable(() -> this.openPickerFor(1));
   public final TextSetting slot3 = new TextSetting("Предмет 3", "ID или алиас");
   public final ButtonSetting pick3 = new ButtonSetting("Выбрать 3", "Открыть инвентарь")
      .setButtonName("Выбрать").setRunnable(() -> this.openPickerFor(2));

   private int pickingForSlot = -1;
   private Item pendingItem = null;
   private Item targetItem = null;
   private int swapPhase = PHASE_IDLE;
   private int phaseTimer = 0;
   private int swapHotbarSlot = -1;
   private int originalSlot = -1;
   private int invPickupSlot = -1;
   private boolean weOpenedScreen = false;
   private boolean sentSprintStop = false;
   private long lastWarnMs = 0L;
   private boolean wheelOpen = false;
   private boolean cursorUnlocked = false;
   private int lastHover = -1;
   private final String[] cachedIds = new String[]{"", "", ""};
   private final ItemStack[] cachedStacks = new ItemStack[]{ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};

   public static AutoSwap getInstance() { return c.a(AutoSwap.class); }

   public AutoSwap() {
      super("AutoSwap", "Свап во вторую руку (Legit / Rage)", ModuleCategory.UTILITIES);
      this.settings(
         this.mode, this.triggerMode, this.wheelBind, this.swapBind,
         this.legitInvSwap,
         this.keepOriginalSlot, this.lockRotation, this.antiInventoryMove,
         this.slot1, this.pick1, this.slot2, this.pick2, this.slot3, this.pick3
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
      if (mc.player == null || this.pickingForSlot == -1 || !(mc.currentScreen instanceof InventoryScreen)) return;
      if (var1.getActionType() == SlotActionType.PICKUP) {
         DefaultedList slots = mc.player.playerScreenHandler.slots;
         if (var1.getSlotId() >= 0 && var1.getSlotId() < slots.size()) {
            ItemStack stack = ((Slot)slots.get(var1.getSlotId())).getStack();
            if (!stack.isEmpty()) {
               TextSetting setting = this.getSlotSetting(this.pickingForSlot);
               if (setting != null) {
                  setting.setText(Registries.ITEM.getId(stack.getItem()).toString());
                  this.invalidateCachedStack(this.pickingForSlot);
               }
               var1.cancel();
               this.pickingForSlot = -1;
               mc.setScreen(null);
            }
         }
      }
   }

   @EventHandler
   public void onKey(KeyEvent var1) {
      if (mc.player == null) return;
      if (this.swapBind.getKey() != -1 && this.triggerMode.isSelected("Без колеса")
            && var1.isKeyDown(this.swapBind.getKey(), true)) {
         this.pendingItem = this.resolveTargetItem();
      }
      if (this.triggerMode.isSelected("Колесо") && this.wheelBind.getKey() != -1) {
         if (var1.isKeyDown(this.wheelBind.getKey(), true)) {
            this.wheelOpen = true;
            this.lastHover = -1;
            this.pendingItem = null;
            this.setCursorUnlocked(true);
         } else if (var1.isKeyReleased(this.wheelBind.getKey(), true) && this.wheelOpen) {
            if (this.lastHover != -1) {
               TextSetting s = this.getSlotSetting(this.lastHover);
               Item picked = this.parseItem(s != null ? s.getText() : null);
               if (picked != null) this.pendingItem = picked;
            }
            this.wheelOpen = false;
            this.lastHover = -1;
            this.setCursorUnlocked(false);
         }
      }
   }

   @EventHandler
   public void onTick(TickEvent var1) {
      if (mc.player == null || mc.world == null || mc.interactionManager == null) {
         this.resetSwap();
         return;
      }
      if (this.wheelOpen) return;
      if (this.swapPhase == PHASE_IDLE) {
         if (this.pendingItem != null) {
            Item item = this.pendingItem;
            this.pendingItem = null;
            this.beginSwap(item);
            return;
         }
         if (this.triggerMode.isSelected("Без колеса") && mc.currentScreen == null) {
            Item desired = this.resolveDesiredOffhandItem();
            if (desired != null && mc.player.getOffHandStack().getItem() != desired) {
               this.beginSwap(desired);
            }
         }
         return;
      }
      if (this.swapPhase == PHASE_INV_REACTION || this.swapPhase == PHASE_INV_AFTER_CLICK || this.swapPhase == PHASE_INV_AFTER_CLOSE) {
         this.tickInvSwapPhase();
         return;
      }
      this.tickSafePhase();
   }

   private void beginSwap(Item item) {
      if (mc.player == null || item == null || item == Items.AIR) return;
      if (mc.currentScreen != null && !(mc.currentScreen instanceof InventoryScreen)) return;
      int hotbar = this.findHotbarSlotForItem(item);
      if (hotbar != -1) { this.beginSafeSwap(item, hotbar); return; }
      if (this.mode.isSelected("Legit") && this.legitInvSwap.isValue()) {
         int inv = this.findInvSlotForItem(item);
         if (inv == -1) { this.warnTargetNotInHotbar(item); return; }
         this.beginLegitInvSwap(item, inv);
         return;
      }
      this.warnTargetNotInHotbar(item);
   }

   private void beginSafeSwap(Item item, int hotbar) {
      this.targetItem = item;
      this.originalSlot = this.getSelectedSlot();
      this.swapHotbarSlot = hotbar;
      this.sentSprintStop = false;
      if (this.originalSlot != hotbar && this.originalSlot >= 0) {
         this.sendScrollPacket(hotbar);
         this.swapPhase = PHASE_SAFE_WAIT_SCROLL;
         this.phaseTimer = 0;
      } else {
         this.swapPhase = PHASE_SAFE_SWAP;
         this.phaseTimer = 0;
      }
   }

   private void tickSafePhase() {
      if (this.antiInventoryMove.isValue()) {
         if (!this.sentSprintStop) { this.stopServerSprint(); this.sentSprintStop = true; }
         SUPPRESS_SPRINT = true; SUPPRESS_INPUT = true;
      }
      if (this.lockRotation.isValue()) this.refreshRotationLock();
      if (this.phaseTimer > 0) { this.phaseTimer--; return; }
      if (this.swapPhase == PHASE_SAFE_WAIT_SCROLL) {
         this.swapPhase = PHASE_SAFE_SWAP; this.phaseTimer = 0; return;
      }
      if (this.swapPhase == PHASE_SAFE_SWAP) {
         this.sendSwapWithOffhandPacket();
         this.mirrorOffhandSwap(this.swapHotbarSlot);
         if (this.keepOriginalSlot.isValue() && this.originalSlot != this.swapHotbarSlot && this.originalSlot >= 0) {
            this.swapPhase = PHASE_SAFE_RESTORE; this.phaseTimer = 0;
         } else { this.finishSwap(); }
         return;
      }
      if (this.swapPhase == PHASE_SAFE_RESTORE) {
         this.sendScrollPacket(this.originalSlot);
         this.finishSwap();
         return;
      }
   }

   /**
    * Legit inv-свап: открываем инв, посылаем ClickSlot SWAP button=40 (ванильный F-в-инвентаре), закрываем.
    */
   private void beginLegitInvSwap(Item item, int invSlot) {
      this.targetItem = item;
      this.originalSlot = this.getSelectedSlot();
      this.swapHotbarSlot = -1;
      this.invPickupSlot = invSlot;
      this.sentSprintStop = false;
      this.weOpenedScreen = false;
      if (mc.currentScreen == null) {
         mc.setScreen(new InventoryScreen(mc.player));
         this.weOpenedScreen = true;
      }
      this.swapPhase = PHASE_INV_REACTION;
      this.phaseTimer = 0;
   }

   private void tickInvSwapPhase() {
      if (this.antiInventoryMove.isValue()) {
         if (!this.sentSprintStop) { this.stopServerSprint(); this.sentSprintStop = true; }
         SUPPRESS_SPRINT = true; SUPPRESS_INPUT = true;
      }
      if (this.lockRotation.isValue()) this.refreshRotationLock();
      if (this.phaseTimer > 0) { this.phaseTimer--; return; }

      if (this.swapPhase == PHASE_INV_REACTION) {
         if (mc.player == null || mc.interactionManager == null) { this.finishSwap(); return; }
         int syncId = mc.player.playerScreenHandler.syncId;
         mc.interactionManager.clickSlot(syncId, this.invPickupSlot, OFFHAND_SWAP_BUTTON, SlotActionType.SWAP, mc.player);
         this.swapPhase = PHASE_INV_AFTER_CLICK;
         this.phaseTimer = 0;
         return;
      }

      if (this.swapPhase == PHASE_INV_AFTER_CLICK) {
         if (this.weOpenedScreen) {
            if (mc.getNetworkHandler() != null && mc.player != null) {
               mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(mc.player.playerScreenHandler.syncId));
            }
            if (mc.currentScreen instanceof InventoryScreen) mc.setScreen(null);
            this.weOpenedScreen = false;
         }
         this.swapPhase = PHASE_INV_AFTER_CLOSE;
         this.phaseTimer = 0;
         return;
      }

      if (this.swapPhase == PHASE_INV_AFTER_CLOSE) {
         this.finishSwap();
         return;
      }
   }

   private void finishSwap() {
      this.targetItem = null;
      this.swapHotbarSlot = -1; this.originalSlot = -1;
      this.invPickupSlot = -1;
      if (this.weOpenedScreen) {
         if (mc.getNetworkHandler() != null && mc.player != null) {
            mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(mc.player.playerScreenHandler.syncId));
         }
         if (mc.currentScreen instanceof InventoryScreen) mc.setScreen(null);
         this.weOpenedScreen = false;
      }
      this.swapPhase = PHASE_IDLE; this.phaseTimer = 0;
      this.sentSprintStop = false;
      SUPPRESS_SPRINT = false; SUPPRESS_INPUT = false;
      LOCK_ROTATION = false;
   }

   private void resetSwap() {
      this.swapPhase = PHASE_IDLE; this.phaseTimer = 0;
      this.targetItem = null;
      this.swapHotbarSlot = -1; this.originalSlot = -1;
      this.invPickupSlot = -1;
      this.weOpenedScreen = false;
      this.sentSprintStop = false;
      SUPPRESS_SPRINT = false; SUPPRESS_INPUT = false;
      LOCK_ROTATION = false;
   }

   private void sendScrollPacket(int hotbarSlot) {
      if (mc.getNetworkHandler() == null) return;
      mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(hotbarSlot));
      if (mc.player != null) {
         try { mc.player.getInventory().setSelectedSlot(hotbarSlot); } catch (Throwable ignored) {}
      }
   }

   private void sendSwapWithOffhandPacket() {
      if (mc.getNetworkHandler() == null) return;
      mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
         PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
   }

   private void mirrorOffhandSwap(int hotbarSlot) {
      if (mc.player == null) return;
      PlayerInventory inv = mc.player.getInventory();
      ItemStack inHotbar = inv.getStack(hotbarSlot).copy();
      ItemStack inOffhand = mc.player.getStackInHand(Hand.OFF_HAND).copy();
      inv.setStack(hotbarSlot, inOffhand);
      mc.player.setStackInHand(Hand.OFF_HAND, inHotbar);
   }

   private int getSelectedSlot() {
      if (mc.player == null) return -1;
      return mc.player.getInventory().getSelectedSlot();
   }

   private int findHotbarSlotForItem(Item item) {
      if (mc.player == null || item == null || item == Items.AIR) return -1;
      PlayerInventory inv = mc.player.getInventory();
      for (int i = 0; i < 9; i++) {
         ItemStack s = inv.getStack(i);
         if (!s.isEmpty() && s.getItem() == item) return i;
      }
      return -1;
   }

   private int findInvSlotForItem(Item item) {
      if (mc.player == null || item == null || item == Items.AIR) return -1;
      PlayerInventory inv = mc.player.getInventory();
      for (int i = 9; i <= 35; i++) {
         ItemStack s = inv.getStack(i);
         if (!s.isEmpty() && s.getItem() == item) return i;
      }
      return -1;
   }

   private Item resolveTargetItem() {
      for (int i = 0; i < 3; i++) {
         Item it = this.parseItem(this.getSlotSetting(i) != null ? this.getSlotSetting(i).getText() : null);
         if (it == null) continue;
         if (this.findHotbarSlotForItem(it) != -1 || this.findInvSlotForItem(it) != -1) return it;
      }
      return null;
   }

   private Item resolveDesiredOffhandItem() {
      if (mc.player == null) return null;
      Item offhand = mc.player.getOffHandStack().getItem();
      for (int i = 0; i < 3; i++) {
         Item it = this.parseItem(this.getSlotSetting(i) != null ? this.getSlotSetting(i).getText() : null);
         if (it == null) continue;
         if (offhand == it) return it;
         if (this.findHotbarSlotForItem(it) != -1 || this.findInvSlotForItem(it) != -1) return it;
      }
      return null;
   }

   private Item parseItem(String text) {
      if (text == null || text.isBlank()) return null;
      String id = text.trim().toLowerCase(Locale.ROOT);
      if (id.contains("талик") || id.contains("тотем") || id.contains("talik") || id.equals("totem") || id.equals("totem_of_undying")) {
         return Items.TOTEM_OF_UNDYING;
      }
      if (!id.contains(":")) id = "minecraft:" + id;
      if (id.contains(":totem")) return Items.TOTEM_OF_UNDYING;
      Identifier ident = Identifier.tryParse(id);
      if (ident == null) return null;
      Item item = Registries.ITEM.get(ident);
      return item != null && item != Items.AIR ? item : null;
   }

   private void warnTargetNotInHotbar(Item item) {
      long now = System.currentTimeMillis();
      if (now - this.lastWarnMs < 4000L) return;
      this.lastWarnMs = now;
      if (mc.player == null || item == null) return;
      String name = item.getName().getString();
      String msg;
      if (this.mode.isSelected("Rage")) {
         msg = "Rage свапает только из хотбара. Положи «" + name + "» в хотбар или включи Legit.";
      } else if (!this.legitInvSwap.isValue()) {
         msg = "Нет «" + name + "» в хотбаре. Включи «Свап из инвентаря» или положи в хотбар.";
      } else {
         msg = "«" + name + "» не найден ни в хотбаре, ни в инвентаре.";
      }
      mc.player.sendMessage(
         Text.literal("[AutoSwap] ").formatted(Formatting.YELLOW).append(Text.literal(msg).formatted(Formatting.GRAY)),
         true);
   }

   private void refreshRotationLock() {
      if (mc.player == null) return;
      LOCK_YAW = mc.player.getYaw();
      LOCK_PITCH = mc.player.getPitch();
      LOCK_ROTATION = true;
   }

   private void stopServerSprint() {
      if (mc.player != null && mc.getNetworkHandler() != null) {
         mc.player.setSprinting(false);
         mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
      }
   }

   @EventHandler
   public void onDraw(DrawEvent var1) {
      if (mc.player == null || !this.triggerMode.isSelected("Колесо") || !this.wheelOpen || mc.currentScreen != null) return;
      FrameProfiler profiler = FrameProfiler.getInstance();
      boolean prof = profiler.isEnabled();
      if (prof) profiler.begin("AutoSwap/wheelDraw");
      try {
         this.setCursorUnlocked(true);
         int sw = var1.getDrawContext().getScaledWindowWidth();
         int sh = var1.getDrawContext().getScaledWindowHeight();
         float cx = sw / 2.0F, cy = sh / 2.0F;
         float outerR = 92.0F, innerR = 54.0F;
         float mx = (float)(mc.mouse.getX() * sw / mc.getWindow().getWidth());
         float my = (float)(mc.mouse.getY() * sh / mc.getWindow().getHeight());
         int count = 3;
         int hover = this.getHoverIndex(mx, my, cx, cy, innerR, outerR, count);
         this.lastHover = hover;
         float segAngle = 360.0F / count;
         float gap = 2.0F;
         WheelPipeline wheel = Initialization.getInstance().getManager().getRenderCore().getWheelPipeline();
         for (int i = 0; i < count; i++) {
            int color = i == hover ? -1593847505 : 1624100301;
            float a = -90.0F + segAngle * i + gap / 2.0F;
            float b = a + segAngle - gap;
            wheel.drawSegment(cx, cy, innerR, outerR, (float)Math.toRadians(a), (float)Math.toRadians(b), color);
         }
         for (int i = 0; i < count; i++) {
            ItemStack stack = this.getStackForIndex(i);
            if (!stack.isEmpty()) {
               float angle = (float)Math.toRadians(-90.0F + segAngle * i + segAngle / 2.0F);
               float mid = (innerR + outerR) / 2.0F;
               float ix = cx + (float)Math.cos(angle) * mid;
               float iy = cy + (float)Math.sin(angle) * mid;
               var1.getDrawContext().drawItem(stack, (int)(ix - 8.0F), (int)(iy - 8.0F));
            }
         }
      } finally {
         if (prof) profiler.end();
      }
   }

   private void setCursorUnlocked(boolean unlock) {
      if (mc.mouse == null) return;
      if (unlock && !this.cursorUnlocked) {
         mc.mouse.unlockCursor();
         this.cursorUnlocked = true;
      } else if (!unlock && this.cursorUnlocked) {
         if (mc.currentScreen == null) mc.mouse.lockCursor();
         this.cursorUnlocked = false;
      }
   }

   private ItemStack getStackForIndex(int idx) {
      TextSetting setting = this.getSlotSetting(idx);
      if (setting == null) return ItemStack.EMPTY;
      String text = setting.getText();
      if (text == null || text.isBlank()) {
         this.cachedIds[idx] = text == null ? "" : text;
         this.cachedStacks[idx] = ItemStack.EMPTY;
         return ItemStack.EMPTY;
      }
      if (text.equals(this.cachedIds[idx])) return this.cachedStacks[idx];
      this.cachedIds[idx] = text;
      Item item = this.parseItem(text);
      this.cachedStacks[idx] = item != null && item != Items.AIR ? item.getDefaultStack() : ItemStack.EMPTY;
      return this.cachedStacks[idx];
   }

   private void invalidateCachedStack(int idx) {
      if (idx >= 0 && idx < this.cachedIds.length) {
         this.cachedIds[idx] = "";
         this.cachedStacks[idx] = ItemStack.EMPTY;
      }
   }

   private int getHoverIndex(float mx, float my, float cx, float cy, float inner, float outer, int count) {
      float dx = mx - cx, dy = my - cy;
      float dist = (float)Math.sqrt(dx * dx + dy * dy);
      if (dist < inner || dist > outer) return -1;
      double angle = Math.atan2(dy, dx) + (Math.PI / 2);
      if (angle < 0) angle += Math.PI * 2;
      return Math.max(0, Math.min((int)Math.floor(angle / (Math.PI * 2) * count), count - 1));
   }

   @Override
   public void deactivate() {
      this.pickingForSlot = -1;
      this.pendingItem = null;
      this.wheelOpen = false;
      this.lastHover = -1;
      LOCK_ROTATION = false;
      if (this.weOpenedScreen) {
         if (mc.getNetworkHandler() != null && mc.player != null) {
            mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(mc.player.playerScreenHandler.syncId));
         }
         if (mc.currentScreen instanceof InventoryScreen) mc.setScreen(null);
         this.weOpenedScreen = false;
      }
      this.resetSwap();
      this.setCursorUnlocked(false);
   }

   private TextSetting getSlotSetting(int idx) {
      if (idx == 0) return this.slot1;
      if (idx == 1) return this.slot2;
      if (idx == 2) return this.slot3;
      return null;
   }
}
