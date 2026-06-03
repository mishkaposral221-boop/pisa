package rich.mixin.accessor;

import net.minecraft.class_1661;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_1661.class)
public interface PlayerInventoryAccessor {
   @Accessor("field_7545")
   int getSelectedSlot();

   @Accessor("field_7545")
   void setSelectedSlot(int var1);
}
