package rich.mixin;

import net.minecraft.class_638;
import net.minecraft.class_7202;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_638.class)
public interface ClientWorldAccessor {
   @Accessor("field_37951")
   class_7202 getPendingUpdateManager();
}
