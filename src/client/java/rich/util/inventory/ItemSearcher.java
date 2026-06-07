package rich.util.inventory;

import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface ItemSearcher {
   boolean matches(ItemStack var1);
}
