package rich.modules.impl.combat;

import java.util.Locale;
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
import org.lwjgl.glfw.GLFW;
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

   // Anti-BadPacket rotation lock. Mixin в ClientPlayerEntity подменяет yaw/pitch в
   // sendMovementPackets/tick на эти значения, пока LOCK_ROTATION = true. Это нужно,
   // потому что современные AC (Polar/Spacetime) кикают за любое изменение поворота
   // в одном тике с ClickContainer-пакетом.
   public static volatile boolean LOCK_ROTATION = false;
   public static volatile float LOCK_YAW = 0.0F;
   public static volatile float LOCK_PITCH = 0.0F;

   private static final int PHASE_IDLE = 0;
   private static final int PHASE_PRE_OPEN = 1;
   private static final int PHASE_AFTER_OPEN = 2;
   private static final int PHASE_BEFORE_CLICK = 3;
   private static final int PHASE_CLOSING = 4;
   private static final int OFFHAND_BUTTON = 40;
   private static final int INVENTORY_WIDTH = 176;
   private static final int INVENTORY_HEIGHT = 166;
   // Количество тиков паузы после любого свапа (помогает от двойного свапа
   // и держит rotation lock ещё немного после клика).
   private static final int POST_SWAP_PAUSE_TICKS = 10;

   public final SelectSetting triggerMode = new SelectSetting("Триггер", "Колесо — выбор через радиальное меню, Без колеса — авто-свап")
      .value("Колесо", "Без колеса")
      .selected("Без колеса");
   public final BindSetting wheelBind = new BindSetting("Бинд колеса", "Зажми, наведи на предмет и отпусти для выбора")
      .visible(() -> this.triggerMode.isSelected("Колесо"));
   public final BindSetting swapBind = new BindSetting("Бинд свапа", "Ручной триггер свапа (работает только в режиме «Без колеса»)")
      .visible(() -> this.triggerMode.isSelected("Без колеса"));
   public final SelectSetting swapMode = new SelectSetting("Режим свапа", "Legit открывает инвентарь, Packet свапает без экрана")
      .value("Legit", "Packet")
      .selected("Legit");
   public final SliderSettings preOpenDelay = new SliderSettings("До открытия", "Задержка до открытия инвентаря в тиках")
      .setValue(2.0F).range(0, 20)
      .visible(() -> this.swapMode.isSelected("Legit"));
   public final SliderSettings afterOpenDelay = new SliderSettings("После открытия", "Задержка после открытия инвентаря в тиках")
      .setValue(5.0F).range(0, 20)
      .visible(() -> this.swapMode.isSelected("Legit"));
   public final SliderSettings beforeClickDelay = new SliderSettings("Перед F", "Задержка перед нажатием F по слоту")
      .setValue(3.0F).range(0, 20)
      .visible(() -> this.swapMode.isSelected("Legit"));
   public final SliderSettings closeDelay = new SliderSettings("Перед закрытием", "Задержка перед закрытием инвентаря в тиках")
      .setValue(3.0F).range(0, 20)
      .visible(() -> this.swapMode.isSelected("Legit"));
   public final SliderSettings randomDelay = new SliderSettings("Рандом задержки", "Дополнительный случайный разброс в тиках")
      .setValue(2.0F).range(0, 10)
      .visible(() -> this.swapMode.isSelected("Legit"));
   public final SliderSettings cooldown = new SliderSettings("Cooldown", "Минимальная пауза между свапами в миллисекундах")
      .setValue(1000.0F).range(0, 3000);
   public final BooleanSetting stopMovement = new BooleanSetting("Остановка", "Останавливать игрока во время свапа (важно для обхода BadPacket)")
      .setValue(true);
   public final BooleanSetting lockRotation = new BooleanSetting("Lock rotation", "Замораживать поворот головы во время свапа. Нужно для Polar/Spacetime, иначе BadPacket.")
      .setValue(true);

   public final TextSetting slot1 = new TextSetting("Предмет 1", "ID или алиас: талик/тотем/totem");
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
   private boolean sentSprintStop = false;
   private boolean openedInventoryScreen = false;
   // ms-кулдаун: выставляется в начале beginSwap, не только после фактического клика
   private long lastSwapMs = 0L;
   // Тик-кулдаун после свапа (защита от двойного свапа пока клиент обновляет offhand)
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
      super("AutoSwap", "Свап талика во вторую руку", ModuleCategory.UTILITIES);
      this.settings(
         this.triggerMode,
         this.wheelBind,
         this.swapBind,
         this.swapMode,
         this.preOpenDelay,
         this.afterOpenDelay,
         this.beforeClickDelay,
         this.closeDelay,
         this.randomDelay,
         this.cooldown,
         this.stopMovement,
         this.lockRotation,
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
               // Закрываем через ванильный путь — шлём CloseHandledScreenC2SPacket,
               // иначе у сервера остаётся «открытый» контейнер.
               mc.player.closeHandledScreen();
            }
         }
      }
   }

   @EventHandler
   public void onKey(KeyEvent var1) {
      if (mc.player == null) return;

      // swapBind работает ТОЛЬКО в режиме «Без колеса».
      // В режиме «Колесо» свап идёт ИСКЛЮЧИТЕЛЬНО через выбор в колесе,
      // иначе при совпадении/наложении клавиш получается двойной свап:
      //   keyDown swapBind → pendingItem (свап #1) + keyRelease wheelBind → picked (свап #2).
      if (this.swapBind.getKey() != -1
            && this.triggerMode.isSelected("Без колеса")
            && var1.isKeyDown(this.swapBind.getKey(), true)) {
         this.pendingItem = this.resolveTargetItem();
      }

      if (this.triggerMode.isSelected("Колесо") && this.wheelBind.getKey() != -1) {
         if (var1.isKeyDown(this.wheelBind.getKey(), true)) {
            this.wheelOpen = true;
            this.lastHover = -1;
            // Сбрасываем любой pendingItem, который мог остаться от предыдущих событий
            // — в режиме «Колесо» выбор фиксируется ТОЛЬКО на release.
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
            // Пауза кончилась — снимаем rotation lock, дальше клиент шлёт свой настоящий поворот.
            LOCK_ROTATION = false;
         }
         return;
      }

      // Пока колесо открыто — никаких свапов, ждём выбор пользователя
      if (this.wheelOpen) {
         return;
      }

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

      if (mc.currentScreen != null && !(mc.currentScreen instanceof InventoryScreen)) {
         this.resetSwap();
         return;
      }

      if (!this.sentSprintStop) {
         this.stopServerSprint();
         this.sentSprintStop = true;
      }

      if (this.stopMovement.isValue()) this.haltMovement();
      // Каждый тик в фазе свапа подновляем замороженный yaw/pitch на текущие,
      // чтобы lock был "живым" против медленного drift'а до момента click-тика.
      if (this.lockRotation.isValue()) this.refreshRotationLock();

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
         if (!(mc.currentScreen instanceof InventoryScreen)) { this.resetSwap(); return; }
         this.swapPhase = PHASE_BEFORE_CLICK;
         this.phaseTimer = this.delayTicks(this.beforeClickDelay);
         return;
      }

      if (this.swapPhase == PHASE_BEFORE_CLICK) {
         if (!(mc.currentScreen instanceof InventoryScreen)) { this.resetSwap(); return; }
         // На тике клика ещё раз гасим спринт и движение — анти-BadPacket страховка.
         this.stopServerSprint();
         if (this.stopMovement.isValue()) this.haltMovement();
         // И финально фиксируем rotation именно перед ClickContainer-пакетом.
         if (this.lockRotation.isValue()) this.refreshRotationLock();
         this.executeOffhandSwap(this.targetItem);
         this.swapPhase = PHASE_CLOSING;
         this.phaseTimer = this.delayTicks(this.closeDelay);
         return;
      }

      if (this.swapPhase == PHASE_CLOSING) {
         if (mc.currentScreen instanceof InventoryScreen && this.openedInventoryScreen) {
            // Корректное ванильное закрытие: шлём CloseHandledScreenC2SPacket + сбрасываем экран.
            // Без этого пакета сервер думает, что инвентарь всё ещё «открыт» — и следующий
            // ClickContainer ловится как BadPacket.
            mc.player.closeHandledScreen();
         }
         this.resetSwap();
         // После legit-свапа выставляем tick-паузу. Rotation lock держим всё это время,
         // чтобы сервер не увидел резкий скачок поворота сразу после ClickContainer.
         this.postSwapPauseTicks = POST_SWAP_PAUSE_TICKS;
         if (this.lockRotation.isValue()) this.refreshRotationLock();
      }
   }

   private void beginSwap(Item item) {
      if (mc.player == null || item == null || item == Items.AIR) return;

      long now = System.currentTimeMillis();
      if (now - this.lastSwapMs < (long)this.cooldown.getInt()) return;

      // Не блокируем «тотем→тотем» / «талисман→талисман»: если в инвентаре есть подходящий стак,
      // позволяем заменить оффхенд свежим. Автотриггер сам зовёт beginSwap только когда offhand != desired
      // (см. onTick), так что бесконечного цикла не будет.
      Slot slot = this.findSlotForItem(item);
      if (slot == null) return;

      if (mc.currentScreen != null && !(mc.currentScreen instanceof InventoryScreen)) return;

      // Ставим метку времени сразу, чтобы двойной триггер не прошёл даже если legit занимает несколько тиков
      this.lastSwapMs = now;
      this.targetItem = item;

      // Сразу включаем rotation lock — даже если до самого click-тика ещё несколько фаз.
      if (this.lockRotation.isValue()) this.refreshRotationLock();

      if (this.swapMode.isSelected("Packet")) {
         // Анти-BadPacket: перед сырым clickSlot обязательно гасим спринт, движение и фиксируем поворот,
         // иначе сервер видит «click + sprint + forward + rotation delta» в одном тике и кидает BadPacket.
         this.stopServerSprint();
         if (this.stopMovement.isValue()) this.haltMovement();
         if (this.lockRotation.isValue()) this.refreshRotationLock();
         this.executeOffhandSwap(item);
         this.targetItem = null;
         this.postSwapPauseTicks = POST_SWAP_PAUSE_TICKS;
         // LOCK_ROTATION останется true ещё POST_SWAP_PAUSE_TICKS тиков (снимется в onTick).
         return;
      }

      this.sentSprintStop = false;
      SUPPRESS_SPRINT = true;
      if (this.stopMovement.isValue()) this.haltMovement();

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

   private void refreshRotationLock() {
      if (mc.player == null) return;
      LOCK_YAW = mc.player.getYaw();
      LOCK_PITCH = mc.player.getPitch();
      LOCK_ROTATION = true;
   }

   // Ручной/колёсный: первый найденный в инвентаре
   private Item resolveTargetItem() {
      for (int i = 0; i < 3; i++) {
         Item it = this.parseItem(this.getSlotSetting(i) != null ? this.getSlotSetting(i).getText() : null);
         if (it != null && this.findSlotForItem(it) != null) return it;
      }
      return null;
   }

   // Авто: предмет с наивысшим приоритетом, который есть в инвентаре ИЛИ во второй руке
   private Item resolveDesiredOffhandItem() {
      if (mc.player == null) return null;
      Item offhand = mc.player.getOffHandStack().getItem();
      for (int i = 0; i < 3; i++) {
         Item it = this.parseItem(this.getSlotSetting(i) != null ? this.getSlotSetting(i).getText() : null);
         if (it != null && (offhand == it || this.findSlotForItem(it) != null)) return it;
      }
      return null;
   }

   private Item parseItem(String text) {
      if (text == null || text.isBlank()) return null;
      String id = text.trim().toLowerCase(Locale.ROOT);
      // Алиасы для талика — проверяем если строка содержит одно из ключевых слов
      if (id.contains("талик") || id.contains("тотем") || id.contains("talik") || id.equals("totem") || id.equals("totem_of_undying")) {
         return Items.TOTEM_OF_UNDYING;
      }
      // Если нет namespace — добавляем minecraft:
      if (!id.contains(":")) id = "minecraft:" + id;
      // totem в ID (например minecraft:totem_of_undying или minecraft:totem)
      if (id.contains(":totem")) return Items.TOTEM_OF_UNDYING;
      Identifier ident = Identifier.tryParse(id);
      if (ident == null) return null;
      Item item = Registries.ITEM.get(ident);
      return item != null && item != Items.AIR ? item : null;
   }

   private Slot findSlotForItem(Item item) {
      if (mc.player == null || item == null || item == Items.AIR) return null;
      ItemStack mainHand = mc.player.getMainHandStack();
      // 1) Сначала ищем в основной части инвентаря (9..35) — не трогаем хотбар,
      //    чтобы свап не утаскивал предмет, который сейчас в руке (это давало эффект «обратного» свапа).
      for (int i = 9; i <= 35; i++) {
         Slot s = this.getPlayerSlot(i);
         if (this.slotContains(s, item)) return s;
      }
      // 2) Затем — хотбар (36..44), но слот основной руки откладываем на потом.
      Slot heldFallback = null;
      for (int i = 36; i <= 44; i++) {
         Slot s = this.getPlayerSlot(i);
         if (!this.slotContains(s, item)) continue;
         if (s.getStack() == mainHand) { if (heldFallback == null) heldFallback = s; continue; }
         return s;
      }
      // 3) Крайний случай: предмет есть только в основной руке.
      return heldFallback;
   }

   private Slot getPlayerSlot(int id) {
      if (mc.player == null || id < 0 || id >= mc.player.playerScreenHandler.slots.size()) return null;
      return mc.player.playerScreenHandler.getSlot(id);
   }

   private boolean slotContains(Slot slot, Item item) {
      return slot != null && !slot.getStack().isEmpty() && slot.getStack().getItem() == item;
   }

   private boolean executeOffhandSwap(Item item) {
      if (mc.player == null || mc.world == null || mc.interactionManager == null || item == null) return false;
      Slot slot = this.findSlotForItem(item);
      if (slot == null) return false;
      if (mc.currentScreen instanceof InventoryScreen) {
         this.moveCursorToSlot(slot);
      }
      mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot.id, OFFHAND_BUTTON, SlotActionType.SWAP, mc.player);
      return true;
   }

   private void moveCursorToSlot(Slot slot) {
      if (mc.getWindow() == null || slot == null) return;
      int sw = mc.getWindow().getScaledWidth();
      int sh = mc.getWindow().getScaledHeight();
      double guiLeft = (sw - INVENTORY_WIDTH) / 2.0;
      double guiTop = (sh - INVENTORY_HEIGHT) / 2.0;
      double sx = guiLeft + slot.x + 8.0;
      double sy = guiTop + slot.y + 8.0;
      GLFW.glfwSetCursorPos(mc.getWindow().getHandle(),
         sx * mc.getWindow().getWidth() / (double)sw,
         sy * mc.getWindow().getHeight() / (double)sh);
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
      // Если нет активной post-swap паузы — снимаем rotation lock тоже.
      // Если postSwapPauseTicks > 0 (вызвали resetSwap в фазе CLOSING прямо перед паузой),
      // lock останется на эту паузу и снимется в onTick по завершении.
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
      boolean weOpened = this.openedInventoryScreen;
      this.resetSwap();
      if (weOpened && mc.currentScreen instanceof InventoryScreen && mc.player != null) {
         // Ванильное закрытие со close-пакетом, чтобы не оставить сервер в "open container" state.
         mc.player.closeHandledScreen();
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
