package rich.util.inventory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.Hand;
import net.minecraft.item.Item;
import net.minecraft.client.MinecraftClient;

public final class SwapManager {
   private static final MinecraftClient mc = MinecraftClient.getInstance();
   private static final Map<String, SwapSequence> sequences = new ConcurrentHashMap<>();
   private static SwapSequence activeSequence;

   private SwapManager() {
   }

   public static void tick() {
      if (activeSequence != null) {
         activeSequence.tick();
         if (activeSequence.isFinished()) {
            activeSequence = null;
         }
      }

      sequences.values().removeIf(SwapSequence::isFinished);
      sequences.values().forEach(SwapSequence::tick);
   }

   public static void execute(SwapSequence var0) {
      if (activeSequence != null) {
         activeSequence.cancel();
      }

      activeSequence = var0.start();
   }

   public static void execute(String var0, SwapSequence var1) {
      SwapSequence var2 = sequences.get(var0);
      if (var2 != null) {
         var2.cancel();
      }

      sequences.put(var0, var1.start());
   }

   public static void cancel() {
      if (activeSequence != null) {
         activeSequence.cancel();
         activeSequence = null;
      }
   }

   public static void cancel(String var0) {
      SwapSequence var1 = sequences.remove(var0);
      if (var1 != null) {
         var1.cancel();
      }
   }

   public static void cancelAll() {
      cancel();
      sequences.values().forEach(SwapSequence::cancel);
      sequences.clear();
   }

   public static boolean isRunning() {
      return activeSequence != null && !activeSequence.isFinished();
   }

   public static boolean isRunning(String var0) {
      SwapSequence var1 = sequences.get(var0);
      return var1 != null && !var1.isFinished();
   }

   public static void swapAndUse(Item var0) {
      InventoryResult var1 = InventoryUtils.find(var0);
      if (var1.found()) {
         if (var1.isHotbar()) {
            execute(
               new SwapSequence()
                  .step(0, InventoryUtils::saveSlot)
                  .step(0, () -> InventoryUtils.selectSlot(var1.slot()))
                  .step(1, () -> InventoryUtils.use(Hand.MAIN_HAND))
                  .step(1, InventoryUtils::restoreSlot)
            );
         } else {
            int var2 = InventoryUtils.currentSlot();
            execute(
               new SwapSequence()
                  .step(0, () -> InventoryUtils.swapHotbar(var1.slot(), var2))
                  .step(1, () -> InventoryUtils.use(Hand.MAIN_HAND))
                  .step(1, () -> InventoryUtils.swapHotbar(var1.slot(), var2))
                  .step(0, InventoryUtils::closeScreen)
            );
         }
      }
   }

   public static void swapAndUseSilent(Item var0) {
      InventoryResult var1 = InventoryUtils.find(var0);
      if (var1.found()) {
         if (var1.isHotbar()) {
            execute(
               new SwapSequence()
                  .step(0, InventoryUtils::saveSlot)
                  .step(0, () -> InventoryUtils.selectSlotSilent(var1.slot()))
                  .step(0, () -> InventoryUtils.use(Hand.MAIN_HAND))
                  .step(0, InventoryUtils::restoreSlotSilent)
            );
         } else {
            int var2 = InventoryUtils.currentSlot();
            execute(
               new SwapSequence()
                  .step(0, () -> InventoryUtils.swapHotbar(var1.slot(), var2))
                  .step(0, () -> InventoryUtils.use(Hand.MAIN_HAND))
                  .step(0, () -> InventoryUtils.swapHotbar(var1.slot(), var2))
                  .step(0, InventoryUtils::closeScreen)
            );
         }
      }
   }

   public static void moveToHotbar(Item var0, int var1) {
      InventoryResult var2 = InventoryUtils.find(var0);
      if (var2.found() && !var2.isHotbar()) {
         execute(new SwapSequence().step(0, () -> InventoryUtils.swapHotbar(var2.slot(), var1)));
      }
   }

   public static void swapSlots(int var0, int var1) {
      execute(new SwapSequence().step(0, () -> InventoryUtils.swap(var0, var1)));
   }
}
