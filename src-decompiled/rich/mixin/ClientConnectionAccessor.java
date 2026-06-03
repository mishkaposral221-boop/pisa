package rich.mixin;

import net.minecraft.class_2535;
import net.minecraft.class_2547;
import net.minecraft.class_2596;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(class_2535.class)
public interface ClientConnectionAccessor {
   @Accessor("field_11652")
   class_2547 client$listener();

   @Invoker("method_10759")
   static <T extends class_2547> void handlePacket(class_2596<T> var0, class_2547 var1) {
      throw new UnsupportedOperationException();
   }
}
