package rich.util.inventory;

import net.minecraft.class_1799;

public record InventoryResult() {
   private final int slot;
   private final boolean found;
   private final class_1799 stack;
   private static final InventoryResult NOT_FOUND = new InventoryResult(-1, false, class_1799.field_8037);

   public InventoryResult(int var1, boolean var2, class_1799 var3) {
      this.slot = var1;
      this.found = var2;
      this.stack = var3;
   }

   public static InventoryResult notFound() {
      return NOT_FOUND;
   }

   public static InventoryResult of(int var0, class_1799 var1) {
      return new InventoryResult(var0, true, var1);
   }

   public boolean isHotbar() {
      return this.slot >= 0 && this.slot < 9;
   }

   public int toScreenSlot() {
      return this.slot < 9 ? this.slot + 36 : this.slot;
   }
}
