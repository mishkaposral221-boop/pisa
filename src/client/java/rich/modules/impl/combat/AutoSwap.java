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

   public static volatile boolean LOCK_ROTATION = false;
   public static volatile float LOCK_YAW = 0.0F;
   public static volatile float LOCK_PITCH = 0.0F;

   private static final int PHASE_IDLE = 0;
   private static final int PHASE_RELOCATE = 1;
   private static final int PHASE_SCROLL_TO = 2;
   private static final int PHASE_DO_SWAP = 3;
   private static final int PHASE_RESTORE = 4;
   private static final int PHASE_RESTORE_RELOCATE = 5;
   private static final int POST_SWAP_PAUSE_TICKS = 10;

   public final SelectSetting triggerMode = new SelectSetting("Триггер", "Колесо — выбор через радиальное меню, Без колеса — авто-свап")
      .value("Колесо", "Без колеса")
      .selected("Без колеса");
   public final BindSetting wheelBind = new BindSetting("Бинд колеса", "Зажми, наведи на предмет и отпусти для выбора")
      .visible(() -> this.triggerMode.isSelected("Колесо"));
   public final BindSetting swapBind = new BindSetting("Бинд свапа", "Ручной триггер свапа (работает только в режиме «Без колеса»)")
      .visible(() -> this.triggerMode.isSelected("Без колеса"));
   public final SelectSetting swapMode = new SelectSetting("Режим свапа", "Legit — растягивает F-пакеты во времени (рекомендуется), Packet — всё в один тик (быстро, но ловится строгими AC)")
      .value("Legit", "Packet")
      .selected("Legit");
   public final SliderSettings preOpenDelay = new SliderSettings("До скролла", "Тиков перед первым UpdateSelectedSlot. Минимум 2 рекомендуется во избежание badpackets-чека Post HeldItemSlot.")
      .setValue(3.0F).range(0, 20)
      .visible(() -> this.swapMode.isSelected("Legit"));
   public final SliderSettings afterOpenDelay = new SliderSettings("Перед F", "Тиков между UpdateSelectedSlot и PlayerAction(SWAP)")
      .setValue(3.0F).range(0, 20)
      .visible(() -> this.swapMode.isSelected("Legit"));
   public final SliderSettings closeDelay = new SliderSettings("Перед возвратом", "Тиков между F и восстановительным UpdateSelectedSlot")
      .setValue(3.0F).range(0, 20)
      .visible(() -> this.swapMode.isSelected("Legit"));
   public final SliderSettings restoreGap = new SliderSettings("Гэп HeldItemSlot", "Тиков между восстановительным UpdateSelectedSlot и финальным ClickSlot. Должен быть >=2, иначе AC поймает Post HeldItemSlot (badpackets #3).")
      .setValue(3.0F).range(0, 20)
      .visible(() -> this.swapMode.isSelected("Legit"));
   public final SliderSettings randomDelay = new SliderSettings("Рандом задержки", "Дополнительный случайный разброс в тиках")
      .setValue(2.0F).range(0, 10)
      .visible(() -> this.swapMode.isSelected("Legit"));
   public final SliderSettings relocateHaltTicks = new SliderSettings("Стоп перед ClickSlot", "Тиков «стационарного» состояния перед каждым ClickSlot для перемещения из инвентаря. Минимум 2 рекомендуется для Polar/Spacetime InventoryMove-чека.")
      .setValue(3.0F).range(1, 10);
   public final SliderSettings cooldown = new SliderSettings("Cooldown", "Минимальная пауза между свапами в миллисекундах")
      .setValue(1000.0F).range(0, 3000);
   public final BooleanSetting stopMovement = new BooleanSetting("Остановка", "Останавливать игрока во время свапа")
      .setValue(false);
   public final BooleanSetting lockRotation = new BooleanSetting("Lock rotation", "Замораживать поворот головы во время свапа")
      .setValue(false);
   public final BooleanSetting restoreAfterRelocate = new BooleanSetting("Возврат после перекидывания", "Если предмет был в основной части инвентаря — вернуть содержимое хотбара на место (ванильным 1-9 пакетом)")
      .setValue(true);
   public final BooleanSetting maskInventory = new BooleanSetting("Маскировка ClickSlot", "После каждого SWAP-клика шлём CloseHandledScreen(0). Обходит InventoryMove-проверки (Grim/Polar/Spacetime).")
      .setValue(true);
   public final BooleanSetting antiInventoryMove = new BooleanSetting("Анти InventoryMove", "Перед каждым ClickSlot принудительно шлём STOP_SPRINTING + обнуляем скорость + держим игрока в стационарном состоянии N тиков. Обязательно для свапа из основного инвентаря во время движения.")
      .setValue(true);

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

   private boolean wheelOpen = false;
   private boolean cursorUnlocked = false;
   private int lastHover = -1;
   private final String[] cachedIds = new String[]{"", "", ""};
   private final ItemStack[] cachedStacks = new ItemStack[]{ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};

   public static AutoSwap getInstance() {
      return c.a(AutoSwap.class);
   }

   public AutoSwap() {
      super("AutoSwap", "Свап во вторую руку ванильными F-пакетами", ModuleCategory.UTILITIES);
      this.settings(
         this.triggerMode,
         this.wheelBind,
         this.swapBind,
         this.swapMode,
         this.preOpenDelay,
         this.afterOpenDelay,
         this.closeDelay,
         this.restoreGap,
         this.randomDelay,
         this.relocateHaltTicks,
         this.cooldown,
         this.stopMovement,
         this.lockRotation,
         this.restoreAfterRelocate,
         this.maskInventory,
         this.antiInventoryMove,
         this.slot1, this.pick1,
         this.slot2, this.pick2,
         this.slot3, this.pick3
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
      if (mc.player == null || this.pickingForSlot == -1 || !(mc.currentScreen instanceof InventoryScreen)) {
         return;
      }

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

      if (this.swapBind.getKey() != -1
            && this.triggerMode.isSelected("Без колеса")
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

      if (this.postSwapPauseTicks > 0) {
         this.postSwapPauseTicks--;
         if (this.postSwapPauseTicks == 0) {
            LOCK_ROTATION = false;
         }
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

      // While any swap phase is active, force player stationary on the server.
      // Mandatory during PHASE_RELOCATE — that's exactly what AC inspects for InventoryMove.
      boolean forceHalt = this.swapPhase == PHASE_RELOCATE
            || this.swapPhase == PHASE_RESTORE_RELOCATE
            || (this.relocateFromInvSlot != -1 && this.antiInventoryMove.isValue())
            || this.stopMovement.isValue();
      if (forceHalt) {
         if (!this.sentSprintStop) {
            this.stopServerSprint();
            this.sentSprintStop = true;
         }
         this.haltMovement();
         SUPPRESS_SPRINT = true;
      }
      if (this.lockRotation.isValue()) this.refreshRotationLock();

      if (this.phaseTimer > 0) {
         this.phaseTimer--;
         return;
      }

      if (this.swapPhase == PHASE_RELOCATE) {
         this.sendVanillaSwapClick(this.pendingRelocateInvSlot, this.pendingRelocateStash);
         this.relocateFromInvSlot = this.pendingRelocateInvSlot;
         this.swapHotbarSlot = this.pendingRelocateStash;
         this.pendingRelocateInvSlot = -1;
         this.pendingRelocateStash = -1;

         if (this.originalSlot == this.swapHotbarSlot) {
            this.swapPhase = PHASE_DO_SWAP;
            this.phaseTimer = this.delayTicks(this.afterOpenDelay);
         } else {
            this.swapPhase = PHASE_SCROLL_TO;
            // Гэп ClickSlot -> UpdateSelectedSlot: тоже Post HeldItemSlot чек ловит
            this.phaseTimer = this.delayTicks(this.preOpenDelay);
         }
         return;
      }

      if (this.swapPhase == PHASE_SCROLL_TO) {
         this.sendScrollPacket(this.swapHotbarSlot);
         this.swapPhase = PHASE_DO_SWAP;
         this.phaseTimer = this.delayTicks(this.afterOpenDelay);
         return;
      }

      if (this.swapPhase == PHASE_DO_SWAP) {
         this.sendSwapWithOffhandPacket();
         this.mirrorOffhandSwap(this.swapHotbarSlot);
         this.swapPhase = PHASE_RESTORE;
         this.phaseTimer = this.delayTicks(this.closeDelay);
         return;
      }

      if (this.swapPhase == PHASE_RESTORE) {
         // Шаг 1 восстановления: только UpdateSelectedSlot (если нужно).
         // Финальный ClickSlot перенесён в отдельную фазу PHASE_RESTORE_RELOCATE,
         // чтобы между UpdateSelectedSlot и ClickSlot был тиковый гэп (иначе
         // ловится badpackets #3 / Post HeldItemSlot).
         if (this.originalSlot != this.swapHotbarSlot && this.originalSlot >= 0) {
            this.sendScrollPacket(this.originalSlot);
         }
         if (this.relocateFromInvSlot != -1 && this.restoreAfterRelocate.isValue()) {
            this.swapPhase = PHASE_RESTORE_RELOCATE;
            this.phaseTimer = this.delayTicks(this.restoreGap);
         } else {
            this.finishSwap();
         }
         return;
      }

      if (this.swapPhase == PHASE_RESTORE_RELOCATE) {
         // Шаг 2 восстановления: финальный ClickSlot(SWAP) ВОЗВРАЩАЕТ
         // первоначальное содержимое хотбар-слота обратно в исходную
         // ячейку инвентаря. Выполняется в отдельном тике с предварительным
         // halt-комбо для антиInventoryMove.
         if (this.relocateFromInvSlot != -1 && this.restoreAfterRelocate.isValue()) {
            this.sendVanillaSwapClick(this.relocateFromInvSlot, this.swapHotbarSlot);
         }
         this.finishSwap();
         return;
      }
   }

   private void beginSwap(Item item) {
      if (mc.player == null || item == null || item == Items.AIR) return;
      long now = System.currentTimeMillis();
      if (now - this.lastSwapMs < (long)this.cooldown.getInt()) return;
      if (mc.currentScreen != null && !(mc.currentScreen instanceof InventoryScreen)) return;

      int hotbar = this.findHotbarSlotForItem(item);
      int invSlot = -1;
      if (hotbar == -1) {
         invSlot = this.findInvSlotForItem(item);
         if (invSlot == -1) return;
      }

      this.lastSwapMs = now;
      this.targetItem = item;
      this.originalSlot = this.getSelectedSlot();
      this.relocateFromInvSlot = -1;
      this.sentSprintStop = false;

      if (this.swapMode.isSelected("Packet")) {
         // Packet mode: всё в один тик (быстро, но строгие AC могут поймать badpackets).
         if (hotbar == -1) {
            int stash = this.findEmptyHotbarSlot();
            if (stash == -1) stash = this.originalSlot >= 0 ? this.originalSlot : 0;
            this.sendVanillaSwapClick(invSlot, stash);
            this.relocateFromInvSlot = invSlot;
            hotbar = stash;
         }
         this.swapHotbarSlot = hotbar;
         if (this.lockRotation.isValue()) this.refreshRotationLock();
         if (this.stopMovement.isValue() || this.antiInventoryMove.isValue()) {
            this.stopServerSprint();
            this.haltMovement();
         }
         if (this.originalSlot != this.swapHotbarSlot) {
            this.sendScrollPacket(this.swapHotbarSlot);
         }
         this.sendSwapWithOffhandPacket();
         this.mirrorOffhandSwap(this.swapHotbarSlot);
         if (this.originalSlot != this.swapHotbarSlot && this.originalSlot >= 0) {
            this.sendScrollPacket(this.originalSlot);
         }
         if (this.relocateFromInvSlot != -1 && this.restoreAfterRelocate.isValue()) {
            this.sendVanillaSwapClick(this.relocateFromInvSlot, this.swapHotbarSlot);
         }
         this.finishSwap();
         return;
      }

      // Legit mode.
      if (this.lockRotation.isValue()) this.refreshRotationLock();
      SUPPRESS_SPRINT = this.stopMovement.isValue() || this.antiInventoryMove.isValue();

      if (hotbar == -1) {
         // Inv-area item: enter PHASE_RELOCATE first to give server N ticks of stationary
         // state BEFORE the ClickSlot fires. This is what fixes the InventoryMove kick
         // when player is moving/sprinting at the moment of swap trigger.
         int stash = this.findEmptyHotbarSlot();
         if (stash == -1) stash = this.originalSlot >= 0 ? this.originalSlot : 0;
         this.pendingRelocateInvSlot = invSlot;
         this.pendingRelocateStash = stash;
         this.swapPhase = PHASE_RELOCATE;
         int haltTicks = this.antiInventoryMove.isValue()
               ? Math.max(1, this.relocateHaltTicks.getInt())
               : Math.max(0, this.preOpenDelay.getInt());
         int rnd = Math.max(0, this.randomDelay.getInt());
         this.phaseTimer = haltTicks + (rnd <= 0 ? 0 : ThreadLocalRandom.current().nextInt(rnd + 1));
         return;
      }

      this.swapHotbarSlot = hotbar;
      if (this.originalSlot == this.swapHotbarSlot) {
         this.swapPhase = PHASE_DO_SWAP;
         this.phaseTimer = this.delayTicks(this.afterOpenDelay);
      } else {
         this.swapPhase = PHASE_SCROLL_TO;
         this.phaseTimer = this.delayTicks(this.preOpenDelay);
      }
   }

   private void finishSwap() {
      this.targetItem = null;
      this.swapHotbarSlot = -1;
      this.originalSlot = -1;
      this.relocateFromInvSlot = -1;
      this.pendingRelocateInvSlot = -1;
      this.pendingRelocateStash = -1;
      this.swapPhase = PHASE_IDLE;
      this.phaseTimer = 0;
      this.sentSprintStop = false;
      SUPPRESS_SPRINT = false;
      this.postSwapPauseTicks = POST_SWAP_PAUSE_TICKS;
      if (this.lockRotation.isValue()) this.refreshRotationLock();
   }

   private void sendScrollPacket(int hotbarSlot) {
      if (mc.getNetworkHandler() == null) return;
      mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(hotbarSlot));
   }

   private void sendSwapWithOffhandPacket() {
      if (mc.getNetworkHandler() == null) return;
      mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
         PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND,
         BlockPos.ORIGIN,
         Direction.DOWN));
   }

   /**
    * Ванильный ClickSlot формы «цифры 1-9 над слотом в инвентаре».
    *
    * Анти-кик-комбо вокруг каждого клика:
    *  1. STOP_SPRINTING — сервер сразу видит, что игрок «остановился».
    *  2. haltMovement — обнуляем форвард/сайд + xz-velocity. Следующий PlayerMoveC2SPacket
    *     (отсылается ваниллой в конце тика) покажет нулевой дельта-шаг, и InventoryMove-чек
    *     Polar/Spacetime/Grim не зафлажится.
    *  3. ClickSlot(SWAP).
    *  4. CloseHandledScreen(0) — сбрасывает эвристику «открыт инвентарь» (GrimAC #1829).
    */
   private void sendVanillaSwapClick(int screenSlotId, int hotbarButton) {
      if (mc.player == null || mc.interactionManager == null) return;
      if (this.antiInventoryMove.isValue()) {
         this.stopServerSprint();
         this.haltMovement();
      }
      mc.interactionManager.clickSlot(
         mc.player.playerScreenHandler.syncId,
         screenSlotId,
         hotbarButton,
         SlotActionType.SWAP,
         mc.player
      );
      if (this.maskInventory.isValue()) {
         this.sendInventoryCloseMask();
      }
   }

   private void sendInventoryCloseMask() {
      if (mc.getNetworkHandler() == null || mc.player == null) return;
      mc.getNetworkHandler().sendPacket(
         new CloseHandledScreenC2SPacket(mc.player.playerScreenHandler.syncId));
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
      this.swapHotbarSlot = -1;
      this.originalSlot = -1;
      this.relocateFromInvSlot = -1;
      this.pendingRelocateInvSlot = -1;
      this.pendingRelocateStash = -1;
      this.sentSprintStop = false;
      SUPPRESS_SPRINT = false;
      if (this.postSwapPauseTicks == 0) {
         LOCK_ROTATION = false;
      }
   }

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
