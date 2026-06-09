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

   private static final int PHASE_IDLE = 0;
   private static final int PHASE_PRE_OPEN = 1;
   private static final int PHASE_AFTER_OPEN = 2;
   private static final int PHASE_BEFORE_CLICK = 3;
   private static final int PHASE_CLOSING = 4;
   private static final int OFFHAND_BUTTON = 40;

   public final SelectSetting triggerMode = new SelectSetting("Триггер", "Колесо — выбор предмета через радиальное меню, Без колеса — авто-свап")
      .value("Колесо", "Без колеса")
      .selected("Без колеса");
   public final BindSetting wheelBind = new BindSetting("Бинд колеса", "Зажми, наведи на предмет и отпусти для выбора")
      .visible(() -> this.triggerMode.isSelected("Колесо"));
   public final BindSetting swapBind = new BindSetting("Бинд свапа", "Опциональный ручной триггер свапа");
   public final BooleanSetting autoSwap = new BooleanSetting("Авто-свап", "Свапать автоматически, пока модуль включён (режим Без колеса)")
      .setValue(true)
      .visible(() -> this.triggerMode.isSelected("Без колеса"));
   public final SelectSetting swapMode = new SelectSetting("Режим свапа", "Legit открывает инвентарь, Packet свапает без экрана")
      .value("Legit", "Packet")
      .selected("Legit");
   public final SliderSettings preOpenDelay = new SliderSettings("До открытия", "Задержка до открытия инвентаря в тиках")
      .setValue(1.0F)
      .range(0, 20)
      .visible(() -> this.swapMode.isSelected("Legit"));
   public final SliderSettings afterOpenDelay = new SliderSettings("После открытия", "Задержка после открытия инвентаря в тиках")
      .setValue(3.0F)
      .range(0, 20)
      .visible(() -> this.swapMode.isSelected("Legit"));
   public final SliderSettings beforeClickDelay = new SliderSettings("Перед F", "Задержка перед нажатием F по слоту")
      .setValue(1.0F)
      .range(0, 20)
      .visible(() -> this.swapMode.isSelected("Legit"));
   public final SliderSettings closeDelay = new SliderSettings("Перед закрытием", "Задержка перед закрытием инвентаря в тиках")
      .setValue(1.0F)
      .range(0, 20)
      .visible(() -> this.swapMode.isSelected("Legit"));
   public final SliderSettings randomDelay = new SliderSettings("Рандом задержки", "Дополнительный случайный разброс в тиках")
      .setValue(1.0F)
      .range(0, 10)
      .visible(() -> this.swapMode.isSelected("Legit"));
   public final SliderSettings cooldown = new SliderSettings("Cooldown", "Минимальная пауза между свапами в миллисекундах")
      .setValue(250.0F)
      .range(0, 3000);
   public final BooleanSetting stopMovement = new BooleanSetting("Остановка", "Останавливать игрока во время legit-свапа")
      .setValue(true)
      .visible(() -> this.swapMode.isSelected("Legit"));

   public final TextSetting slot1 = new TextSetting("Предмет 1", "ID предмета или алиас: талик/тотем/totem");
   public final ButtonSetting pick1 = new ButtonSetting("Выбрать предмет 1", "Открыть инвентарь")
      .setButtonName("Выбрать")
      .setRunnable(() -> this.openPickerFor(0));
   public final TextSetting slot2 = new TextSetting("Предмет 2", "ID предмета или алиас")
      ;
   public final ButtonSetting pick2 = new ButtonSetting("Выбрать предмет 2", "Открыть инвентарь")
      .setButtonName("Выбрать")
      .setRunnable(() -> this.openPickerFor(1));
   public final TextSetting slot3 = new TextSetting("Предмет 3", "ID предмета или алиас")
      ;
   public final ButtonSetting pick3 = new ButtonSetting("Выбрать предмет 3", "Открыть инвентарь")
      .setButtonName("Выбрать")
      .setRunnable(() -> this.openPickerFor(2));

   private int pickingForSlot = -1;
   private Item pendingItem = null;
   private Item targetItem = null;
   private int swapPhase = PHASE_IDLE;
   private int phaseTimer = 0;
   private boolean sentSprintStop = false;
   private boolean openedInventoryScreen = false;
   private long lastSwapMs = 0L;

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
                     this.invalidateCachedStack(this.pickingForSlot);
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
      if (mc.player == null) {
         return;
      }

      // Ручной бинд свапа — работает в любом режиме.
      if (this.swapBind.getKey() != -1 && var1.isKeyDown(this.swapBind.getKey(), true)) {
         this.pendingItem = this.resolveTargetItem();
      }

      // Колесо: зажал бинд -> навёл -> отпустил бинд = выбрал. Наведение само по себе больше не свапает.
      if (this.triggerMode.isSelected("Колесо") && this.wheelBind.getKey() != -1) {
         if (var1.isKeyDown(this.wheelBind.getKey(), true)) {
            this.wheelOpen = true;
            this.lastHover = -1;
            this.setCursorUnlocked(true);
         } else if (var1.isKeyReleased(this.wheelBind.getKey(), true) && this.wheelOpen) {
            if (this.lastHover != -1) {
               TextSetting setting = this.getSlotSetting(this.lastHover);
               Item picked = this.parseItem(setting != null ? setting.getText() : null);
               if (picked != null) {
                  this.pendingItem = picked;
               }
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

      if (this.swapPhase == PHASE_IDLE) {
         if (this.pendingItem != null) {
            Item var2 = this.pendingItem;
            this.pendingItem = null;
            this.beginSwap(var2);
            return;
         }

         // Авто-триггер (режим Без колеса): держим во второй руке предмет с наивысшим приоритетом из доступных.
         if (this.triggerMode.isSelected("Без колеса") && this.autoSwap.isValue() && mc.currentScreen == null) {
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

   // Ручной/колёсный путь: первый сконфигурированный предмет, присутствующий в инвентаре.
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

      return var1 != null ? var1 : (var2 != null ? var2 : var3);
   }

   // Авто-путь: предмет с наивысшим приоритетом, который есть во второй руке ИЛИ в инвентаре.
   // Если он уже во второй руке — desired совпадёт с offhand и свапа не будет.
   // Если предмет выше приоритетом лежит в инвентаре, а во второй руке предмет ниже приоритетом — он будет вытеснен.
   private Item resolveDesiredOffhandItem() {
      Item offhand = mc.player != null ? mc.player.getOffHandStack().getItem() : Items.AIR;

      Item var1 = this.parseItem(this.slot1.getText());
      if (var1 != null && (offhand == var1 || this.findSlotForItem(var1) != null)) {
         return var1;
      }

      Item var2 = this.parseItem(this.slot2.getText());
      if (var2 != null && (offhand == var2 || this.findSlotForItem(var2) != null)) {
         return var2;
      }

      Item var3 = this.parseItem(this.slot3.getText());
      if (var3 != null && (offhand == var3 || this.findSlotForItem(var3) != null)) {
         return var3;
      }

      return null;
   }

   private Item parseItem(String var1) {
      if (var1 == null || var1.isBlank()) {
         return null;
      }

      String id = var1.trim().toLowerCase(Locale.ROOT);
      if (id.equals("талик") || id.equals("тотем") || id.equals("totem") || id.equals("talik") || id.equals("totem_of_undying")) {
         return Items.TOTEM_OF_UNDYING;
      }

      if (!id.contains(":")) {
         id = "minecraft:" + id;
      }

      Identifier var2 = Identifier.tryParse(id);
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

      // Только один swap-клик как F по слоту. Старый fallback через PICKUP убран, потому что он мог свапать предмет два раза.
      mc.interactionManager.clickSlot(var3, var2.id, OFFHAND_BUTTON, SlotActionType.SWAP, mc.player);

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

   @EventHandler
   public void onDraw(DrawEvent var1) {
      if (mc.player == null || !this.triggerMode.isSelected("Колесо") || !this.wheelOpen || mc.currentScreen != null) {
         return;
      }

      FrameProfiler profiler = FrameProfiler.getInstance();
      boolean prof = profiler.isEnabled();
      if (prof) profiler.begin("AutoSwap/wheelDraw");
      try {
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
         this.lastHover = var11;

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
      } finally {
         if (prof) profiler.end();
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

   private ItemStack getStackForIndex(int var1) {
      TextSetting setting = this.getSlotSetting(var1);
      if (setting == null) {
         return ItemStack.EMPTY;
      }

      String var3 = setting.getText();
      if (var3 == null || var3.isBlank()) {
         this.cachedIds[var1] = var3 == null ? "" : var3;
         this.cachedStacks[var1] = ItemStack.EMPTY;
         return ItemStack.EMPTY;
      }

      if (var3.equals(this.cachedIds[var1])) {
         return this.cachedStacks[var1];
      }

      this.cachedIds[var1] = var3;
      Item var5 = this.parseItem(var3);
      this.cachedStacks[var1] = var5 != null && var5 != Items.AIR ? var5.getDefaultStack() : ItemStack.EMPTY;
      return this.cachedStacks[var1];
   }

   private void invalidateCachedStack(int index) {
      if (index >= 0 && index < this.cachedIds.length) {
         this.cachedIds[index] = "";
         this.cachedStacks[index] = ItemStack.EMPTY;
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

   @Override
   public void deactivate() {
      this.pickingForSlot = -1;
      this.pendingItem = null;
      this.wheelOpen = false;
      this.lastHover = -1;
      this.resetSwap();
      if (mc.currentScreen instanceof InventoryScreen && this.openedInventoryScreen) {
         mc.setScreen(null);
      }

      this.setCursorUnlocked(false);
   }

   private TextSetting getSlotSetting(int index) {
      if (index == 0) return this.slot1;
      if (index == 1) return this.slot2;
      if (index == 2) return this.slot3;
      return null;
   }
}
