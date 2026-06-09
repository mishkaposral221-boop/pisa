package rich.modules.impl.combat;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
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
import rich.modules.module.setting.implement.SliderSettings;
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
   private static final int PHASE_PRE_HALT = 1;
   private static final int PHASE_RELOCATE = 2;
   private static final int PHASE_SCROLL_TO = 3;
   private static final int PHASE_DO_SWAP = 4;
   private static final int PHASE_RESTORE = 5;
   private static final int PHASE_RESTORE_RELOCATE = 6;
   private static final int POST_SWAP_PAUSE_TICKS = 10;

   public final SelectSetting mode = new SelectSetting(
         "Режим",
         "Legit — ванильные пакеты + гуманизация, обход TotemGuard/Vulcan. Rage — те же пакеты, но без задержек. Custom — полная фазовая машина со слайдерами.")
      .value("Legit", "Rage", "Custom")
      .selected("Legit");

   public final SelectSetting triggerMode = new SelectSetting(
         "Триггер",
         "Колесо — выбор через радиальное меню, Без колеса — авто-свап по содержимому offhand")
      .value("Колесо", "Без колеса")
      .selected("Без колеса");
   public final BindSetting wheelBind = new BindSetting("Бинд колеса", "Зажми, наведи на предмет и отпусти")
      .visible(() -> this.triggerMode.isSelected("Колесо"));
   public final BindSetting swapBind = new BindSetting("Бинд свапа", "Ручной триггер свапа")
      .visible(() -> this.triggerMode.isSelected("Без колеса"));

   public final SliderSettings legitMeanMs = new SliderSettings("Legit: среднее (мс)", "Гаусс-распределение.")
      .setValue(200.0F).range(80, 600).visible(() -> this.mode.isSelected("Legit"));
   public final SliderSettings legitStdMs = new SliderSettings("Legit: разброс (мс)", "Стандартное отклонение. Ломает AutoTotemA/B.")
      .setValue(80.0F).range(20, 250).visible(() -> this.mode.isSelected("Legit"));
   public final SliderSettings reactionFloorMs = new SliderSettings("Legit: реакция (мс)", "Мин. реакция от потери offhand. Ломает AutoTotemE.")
      .setValue(150.0F).range(0, 500).visible(() -> this.mode.isSelected("Legit"));
   public final SliderSettings outlierChance = new SliderSettings("Legit: outlier %", "Шанс медленного свапа. Ломает AutoTotemB/C.")
      .setValue(10.0F).range(0, 30).visible(() -> this.mode.isSelected("Legit"));

   public final SliderSettings preOpenDelay = new SliderSettings("До скролла", "Custom")
      .setValue(3.0F).range(0, 20).visible(() -> this.mode.isSelected("Custom"));
   public final SliderSettings afterOpenDelay = new SliderSettings("Перед F", "Custom")
      .setValue(3.0F).range(0, 20).visible(() -> this.mode.isSelected("Custom"));
   public final SliderSettings closeDelay = new SliderSettings("Перед возвратом", "Custom")
      .setValue(3.0F).range(0, 20).visible(() -> this.mode.isSelected("Custom"));
   public final SliderSettings restoreGap = new SliderSettings("Гэп restore", "Custom")
      .setValue(3.0F).range(0, 20).visible(() -> this.mode.isSelected("Custom"));
   public final SliderSettings randomDelay = new SliderSettings("Рандом", "Custom")
      .setValue(2.0F).range(0, 10).visible(() -> this.mode.isSelected("Custom"));
   public final SliderSettings relocateHaltTicks = new SliderSettings("Стоп перед свапом", "Custom")
      .setValue(8.0F).range(2, 15).visible(() -> this.mode.isSelected("Custom"));
   public final SliderSettings maskDelay = new SliderSettings("Задержка маски", "Custom")
      .setValue(3.0F).range(2, 10).visible(() -> this.mode.isSelected("Custom"));

   public final SliderSettings cooldown = new SliderSettings("Cooldown (мс)", "Мин. пауза между свапами. В Rage флор 50мс.")
      .setValue(300.0F).range(0, 3000);

   public final BooleanSetting keepOriginalSlot = new BooleanSetting("Возвращать слот", "Вернуть выделение на оригинальный слот после свапа.")
      .setValue(true);
   public final BooleanSetting allowRelocate = new BooleanSetting("Custom: разрешить relocate", "ОПАСНО: в Custom перекладывать из инвентаря в хотбар через ClickSlot. Ловится как Multi-Action.")
      .setValue(false).visible(() -> this.mode.isSelected("Custom"));
   public final BooleanSetting stopMovement = new BooleanSetting("Остановка", "Custom")
      .setValue(false).visible(() -> this.mode.isSelected("Custom"));
   public final BooleanSetting positionLock = new BooleanSetting("Жёсткая фиксация XZ", "ОПАСНО на Vulcan/Grim. Custom.")
      .setValue(false).visible(() -> this.mode.isSelected("Custom"));
   public final BooleanSetting lockRotation = new BooleanSetting("Lock rotation", "Замораживать поворот головы").setValue(false);
   public final BooleanSetting restoreAfterRelocate = new BooleanSetting("Возврат после перекидывания", "Custom")
      .setValue(true).visible(() -> this.mode.isSelected("Custom") && this.allowRelocate.isValue());
   public final BooleanSetting maskInventory = new BooleanSetting("Маскировка ClickSlot", "Custom. AutoTotemF.")
      .setValue(false).visible(() -> this.mode.isSelected("Custom") && this.allowRelocate.isValue());
   public final BooleanSetting antiInventoryMove = new BooleanSetting("Анти InventoryMove", "Гасит инпут во время свапа. В Legit — только последний тик.")
      .setValue(false);

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
   private int relocateFromInvSlot = -1;
   private int pendingRelocateInvSlot = -1;
   private int pendingRelocateStash = -1;
   private boolean sentSprintStop = false;
   private long lastSwapMs = 0L;
   private int postSwapPauseTicks = 0;
   private int pendingMaskTicks = -1;
   private double lockX = Double.NaN, lockY = Double.NaN, lockZ = Double.NaN;
   private boolean posLockActive = false;
   private long offhandLostMs = 0L;
   private Item lastDesiredItem = null;
   private boolean lastHadDesired = false;
   private long lastWarnMs = 0L;
   private boolean wheelOpen = false;
   private boolean cursorUnlocked = false;
   private int lastHover = -1;
   private final String[] cachedIds = new String[]{"", "", ""};
   private final ItemStack[] cachedStacks = new ItemStack[]{ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};

   public static AutoSwap getInstance() { return c.a(AutoSwap.class); }

   public AutoSwap() {
      super("AutoSwap", "Свап во вторую руку (Legit / Rage / Custom)", ModuleCategory.UTILITIES);
      this.settings(
         this.mode, this.triggerMode, this.wheelBind, this.swapBind,
         this.legitMeanMs, this.legitStdMs, this.reactionFloorMs, this.outlierChance,
         this.preOpenDelay, this.afterOpenDelay, this.closeDelay, this.restoreGap,
         this.randomDelay, this.relocateHaltTicks, this.maskDelay,
         this.cooldown,
         this.keepOriginalSlot, this.allowRelocate,
         this.stopMovement, this.positionLock, this.lockRotation,
         this.restoreAfterRelocate, this.maskInventory, this.antiInventoryMove,
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
      this.trackOffhandLoss();
      if (this.pendingMaskTicks > 0) {
         this.pendingMaskTicks--;
         if (this.pendingMaskTicks == 0) { this.flushMaskNow(); return; }
      }
      if (this.postSwapPauseTicks > 0) {
         this.postSwapPauseTicks--;
         if (this.postSwapPauseTicks == 0) LOCK_ROTATION = false;
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
      if (this.swapPhase == PHASE_SAFE_WAIT_SCROLL || this.swapPhase == PHASE_SAFE_SWAP || this.swapPhase == PHASE_SAFE_RESTORE) {
         this.tickSafePhase();
         return;
      }
      this.tickCustomPhase();
   }

   private void beginSwap(Item item) {
      if (mc.player == null || item == null || item == Items.AIR) return;
      long now = System.currentTimeMillis();
      long cd = this.mode.isSelected("Rage") ? Math.max(50L, (long)this.cooldown.getInt()) : (long)this.cooldown.getInt();
      if (now - this.lastSwapMs < cd) return;
      if (mc.currentScreen != null && !(mc.currentScreen instanceof InventoryScreen)) return;
      int hotbar = this.findHotbarSlotForItem(item);
      if (this.mode.isSelected("Rage") || this.mode.isSelected("Legit")) {
         if (hotbar == -1) { this.warnTargetNotInHotbar(item); return; }
         this.beginSafeSwap(item, hotbar, now);
         return;
      }
      if (hotbar == -1) {
         if (!this.allowRelocate.isValue()) { this.warnTargetNotInHotbar(item); return; }
         int inv = this.findInvSlotForItem(item);
         if (inv == -1) return;
         this.beginCustomSwap(item, -1, inv, now);
         return;
      }
      this.beginCustomSwap(item, hotbar, -1, now);
   }

   private void beginSafeSwap(Item item, int hotbar, long now) {
      this.targetItem = item;
      this.originalSlot = this.getSelectedSlot();
      this.swapHotbarSlot = hotbar;
      this.lastSwapMs = now;
      this.sentSprintStop = false;
      int waitTicksBeforeSwap;
      if (this.mode.isSelected("Rage")) {
         waitTicksBeforeSwap = 0;
      } else {
         long reactNeeded = this.reactionFloorMs.getInt();
         long sinceLoss = this.offhandLostMs == 0L ? Long.MAX_VALUE / 4 : (now - this.offhandLostMs);
         long waitMs = this.computeLegitDelayMs();
         if (sinceLoss < reactNeeded) waitMs = Math.max(waitMs, reactNeeded - sinceLoss);
         int outlier = this.outlierChance.getInt();
         if (outlier > 0 && ThreadLocalRandom.current().nextInt(100) < outlier) {
            waitMs += 300L + ThreadLocalRandom.current().nextInt(500);
         }
         waitTicksBeforeSwap = Math.max(1, (int)Math.ceil(waitMs / 50.0));
      }
      if (this.originalSlot != hotbar && this.originalSlot >= 0) {
         this.sendScrollPacket(hotbar);
         this.swapPhase = PHASE_SAFE_WAIT_SCROLL;
         this.phaseTimer = this.mode.isSelected("Rage") ? 1 : Math.max(1, waitTicksBeforeSwap);
      } else {
         this.swapPhase = PHASE_SAFE_SWAP;
         this.phaseTimer = waitTicksBeforeSwap;
      }
   }

   private void tickSafePhase() {
      if (this.mode.isSelected("Legit") && this.antiInventoryMove.isValue()
            && this.swapPhase == PHASE_SAFE_SWAP && this.phaseTimer <= 1) {
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
            this.swapPhase = PHASE_SAFE_RESTORE;
            this.phaseTimer = this.mode.isSelected("Rage") ? 1 : Math.max(1, (int)Math.ceil(this.computeLegitDelayMs() / 50.0));
         } else {
            this.finishSwap();
         }
         return;
      }
      if (this.swapPhase == PHASE_SAFE_RESTORE) {
         this.sendScrollPacket(this.originalSlot);
         this.finishSwap();
         return;
      }
   }

   private void beginCustomSwap(Item item, int hotbar, int invSlot, long now) {
      this.targetItem = item;
      this.originalSlot = this.getSelectedSlot();
      this.relocateFromInvSlot = -1;
      this.sentSprintStop = false;
      this.lastSwapMs = now;
      if (this.positionLock.isValue()) {
         this.lockX = mc.player.getX(); this.lockY = mc.player.getY(); this.lockZ = mc.player.getZ();
         this.posLockActive = true;
      }
      if (this.lockRotation.isValue()) this.refreshRotationLock();
      SUPPRESS_SPRINT = this.stopMovement.isValue() || this.antiInventoryMove.isValue();
      SUPPRESS_INPUT = SUPPRESS_SPRINT;
      if (hotbar == -1) {
         int stash = this.findEmptyHotbarSlot();
         if (stash == -1) stash = this.originalSlot >= 0 ? this.originalSlot : 0;
         this.pendingRelocateInvSlot = invSlot;
         this.pendingRelocateStash = stash;
         this.swapHotbarSlot = stash;
      } else {
         this.swapHotbarSlot = hotbar;
      }
      int preHaltTicks = this.antiInventoryMove.isValue() ? Math.max(2, this.relocateHaltTicks.getInt()) : 0;
      if (preHaltTicks > 0) {
         this.swapPhase = PHASE_PRE_HALT;
         int rnd = Math.max(0, this.randomDelay.getInt());
         this.phaseTimer = preHaltTicks + (rnd <= 0 ? 0 : ThreadLocalRandom.current().nextInt(rnd + 1));
         return;
      }
      if (this.pendingRelocateInvSlot != -1) {
         this.swapPhase = PHASE_RELOCATE; this.phaseTimer = 0;
      } else if (this.originalSlot == this.swapHotbarSlot) {
         this.swapPhase = PHASE_DO_SWAP; this.phaseTimer = this.tickGap(this.afterOpenDelay);
      } else {
         this.swapPhase = PHASE_SCROLL_TO; this.phaseTimer = this.tickGap(this.preOpenDelay);
      }
   }

   private void tickCustomPhase() {
      boolean forceHalt = this.swapPhase != PHASE_IDLE && (this.antiInventoryMove.isValue() || this.stopMovement.isValue());
      if (forceHalt) {
         if (!this.sentSprintStop) { this.stopServerSprint(); this.sentSprintStop = true; }
         this.haltMovement();
         SUPPRESS_SPRINT = true; SUPPRESS_INPUT = true;
      }
      if (this.lockRotation.isValue()) this.refreshRotationLock();
      if (this.phaseTimer > 0) { this.phaseTimer--; return; }
      if (this.swapPhase == PHASE_PRE_HALT) {
         if (this.pendingRelocateInvSlot != -1) { this.swapPhase = PHASE_RELOCATE; this.phaseTimer = 0; }
         else if (this.originalSlot == this.swapHotbarSlot) { this.swapPhase = PHASE_DO_SWAP; this.phaseTimer = this.tickGap(this.afterOpenDelay); }
         else { this.swapPhase = PHASE_SCROLL_TO; this.phaseTimer = this.tickGap(this.preOpenDelay); }
         return;
      }
      if (this.swapPhase == PHASE_RELOCATE) {
         this.sendVanillaSwapClick(this.pendingRelocateInvSlot, this.pendingRelocateStash);
         this.relocateFromInvSlot = this.pendingRelocateInvSlot;
         this.swapHotbarSlot = this.pendingRelocateStash;
         this.pendingRelocateInvSlot = -1; this.pendingRelocateStash = -1;
         if (this.originalSlot == this.swapHotbarSlot) { this.swapPhase = PHASE_DO_SWAP; this.phaseTimer = this.tickGap(this.afterOpenDelay); }
         else { this.swapPhase = PHASE_SCROLL_TO; this.phaseTimer = this.tickGap(this.preOpenDelay); }
         return;
      }
      if (this.swapPhase == PHASE_SCROLL_TO) {
         this.sendScrollPacket(this.swapHotbarSlot);
         this.swapPhase = PHASE_DO_SWAP;
         this.phaseTimer = this.tickGap(this.afterOpenDelay);
         return;
      }
      if (this.swapPhase == PHASE_DO_SWAP) {
         this.sendSwapWithOffhandPacket();
         this.mirrorOffhandSwap(this.swapHotbarSlot);
         this.swapPhase = PHASE_RESTORE;
         this.phaseTimer = this.tickGap(this.closeDelay);
         return;
      }
      if (this.swapPhase == PHASE_RESTORE) {
         if (this.originalSlot != this.swapHotbarSlot && this.originalSlot >= 0) this.sendScrollPacket(this.originalSlot);
         if (this.relocateFromInvSlot != -1 && this.restoreAfterRelocate.isValue()) {
            this.swapPhase = PHASE_RESTORE_RELOCATE;
            this.phaseTimer = this.tickGap(this.restoreGap);
         } else { this.finishSwap(); }
         return;
      }
      if (this.swapPhase == PHASE_RESTORE_RELOCATE) {
         if (this.relocateFromInvSlot != -1 && this.restoreAfterRelocate.isValue()) {
            this.sendVanillaSwapClick(this.relocateFromInvSlot, this.swapHotbarSlot);
         }
         this.finishSwap();
         return;
      }
   }

   private void finishSwap() {
      this.targetItem = null;
      this.swapHotbarSlot = -1; this.originalSlot = -1;
      this.relocateFromInvSlot = -1; this.pendingRelocateInvSlot = -1; this.pendingRelocateStash = -1;
      this.swapPhase = PHASE_IDLE; this.phaseTimer = 0;
      this.sentSprintStop = false;
      SUPPRESS_SPRINT = false; SUPPRESS_INPUT = false;
      this.posLockActive = false;
      this.lockX = Double.NaN; this.lockY = Double.NaN; this.lockZ = Double.NaN;
      this.postSwapPauseTicks = this.mode.isSelected("Rage") ? 1 : POST_SWAP_PAUSE_TICKS;
      if (this.lockRotation.isValue()) this.refreshRotationLock();
   }

   private void resetSwap() {
      this.swapPhase = PHASE_IDLE; this.phaseTimer = 0;
      this.targetItem = null;
      this.swapHotbarSlot = -1; this.originalSlot = -1;
      this.relocateFromInvSlot = -1; this.pendingRelocateInvSlot = -1; this.pendingRelocateStash = -1;
      this.sentSprintStop = false;
      this.pendingMaskTicks = -1;
      this.posLockActive = false;
      this.lockX = Double.NaN; this.lockY = Double.NaN; this.lockZ = Double.NaN;
      SUPPRESS_SPRINT = false; SUPPRESS_INPUT = false;
      if (this.postSwapPauseTicks == 0) LOCK_ROTATION = false;
   }

   private void sendScrollPacket(int hotbarSlot) {
      if (mc.getNetworkHandler() == null) return;
      mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(hotbarSlot));
   }

   private void sendSwapWithOffhandPacket() {
      if (mc.getNetworkHandler() == null) return;
      mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
         PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
   }

   private void sendVanillaSwapClick(int screenSlotId, int hotbarButton) {
      if (mc.player == null || mc.interactionManager == null) return;
      if (this.pendingMaskTicks > 0) this.flushMaskNow();
      mc.interactionManager.clickSlot(
         mc.player.playerScreenHandler.syncId, screenSlotId, hotbarButton, SlotActionType.SWAP, mc.player);
      if (this.maskInventory.isValue()) {
         this.pendingMaskTicks = Math.max(2, this.maskDelay.getInt());
      }
   }

   private void flushMaskNow() {
      if (mc.getNetworkHandler() != null && mc.player != null) {
         mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(mc.player.playerScreenHandler.syncId));
      }
      this.pendingMaskTicks = -1;
   }

   private void mirrorOffhandSwap(int hotbarSlot) {
      if (mc.player == null) return;
      PlayerInventory inv = mc.player.getInventory();
      ItemStack inHotbar = inv.getStack(hotbarSlot).copy();
      ItemStack inOffhand = mc.player.getStackInHand(Hand.OFF_HAND).copy();
      inv.setStack(hotbarSlot, inOffhand);
      mc.player.setStackInHand(Hand.OFF_HAND, inHotbar);
   }

   private void trackOffhandLoss() {
      if (mc.player == null) return;
      Item desired = this.resolveDesiredOffhandItem();
      if (desired == null) {
         this.lastDesiredItem = null; this.lastHadDesired = false; this.offhandLostMs = 0L;
         return;
      }
      Item current = mc.player.getOffHandStack().getItem();
      boolean has = (current == desired);
      if (this.lastDesiredItem != desired) {
         this.lastDesiredItem = desired;
         this.lastHadDesired = has;
         this.offhandLostMs = has ? 0L : System.currentTimeMillis();
         return;
      }
      if (has) { this.lastHadDesired = true; this.offhandLostMs = 0L; }
      else {
         if (this.lastHadDesired || this.offhandLostMs == 0L) this.offhandLostMs = System.currentTimeMillis();
         this.lastHadDesired = false;
      }
   }

   private long computeLegitDelayMs() {
      double mean = this.legitMeanMs.getInt();
      double std = this.legitStdMs.getInt();
      double g = ThreadLocalRandom.current().nextGaussian() * std + mean;
      double minMs = Math.max(50.0, mean - std * 1.5);
      double maxMs = mean + std * 3.0 + 100.0;
      return (long) Math.max(minMs, Math.min(maxMs, g));
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

   private int findEmptyHotbarSlot() {
      if (mc.player == null) return -1;
      PlayerInventory inv = mc.player.getInventory();
      int sel = inv.getSelectedSlot();
      for (int i = 8; i >= 0; i--) {
         if (i == sel) continue;
         if (inv.getStack(i).isEmpty()) return i;
      }
      if (inv.getStack(sel).isEmpty()) return sel;
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
      if (mc.player != null && item != null) {
         String name = item.getName().getString();
         mc.player.sendMessage(
            Text.literal("[AutoSwap] ").formatted(Formatting.YELLOW)
               .append(Text.literal("Положи «" + name + "» в хотбар — в Rage/Legit relocate отключён ради обхода multi-action.").formatted(Formatting.GRAY)),
            true);
      }
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

   private void haltMovement() {
      if (mc.player == null) return;
      mc.player.setSprinting(false);
      mc.player.setVelocity(0.0, mc.player.getVelocity().y, 0.0);
      mc.player.forwardSpeed = 0.0F;
      mc.player.sidewaysSpeed = 0.0F;
      mc.player.upwardSpeed = 0.0F;
      if (this.posLockActive && !Double.isNaN(this.lockX) && !Double.isNaN(this.lockZ)) {
         double curY = mc.player.getY();
         mc.player.setPosition(this.lockX, curY, this.lockZ);
         mc.player.lastX = this.lockX; mc.player.lastZ = this.lockZ;
         mc.player.lastRenderX = this.lockX; mc.player.lastRenderZ = this.lockZ;
      }
   }

   private int tickGap(SliderSettings s) { return this.delayTicks(s); }

   private int delayTicks(SliderSettings s) {
      int base = Math.max(0, s.getInt());
      int rnd = Math.max(0, this.randomDelay.getInt());
      return base + (rnd <= 0 ? 0 : ThreadLocalRandom.current().nextInt(rnd + 1));
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
      this.postSwapPauseTicks = 0;
      LOCK_ROTATION = false;
      this.resetSwap();
      if (mc.currentScreen instanceof InventoryScreen && mc.player != null) {
         mc.setScreen(null);
      }
      this.setCursorUnlocked(false);
   }

   private TextSetting getSlotSetting(int idx) {
      if (idx == 0) return this.slot1;
      if (idx == 1) return this.slot2;
      if (idx == 2) return this.slot3;
      return null;
   }
}
