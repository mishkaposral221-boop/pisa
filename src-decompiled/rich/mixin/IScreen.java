package rich.mixin;

import java.util.List;
import net.minecraft.class_364;
import net.minecraft.class_4068;
import net.minecraft.class_437;
import net.minecraft.class_6379;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_437.class)
public interface IScreen {
   @Accessor("field_33816")
   List<class_4068> getDrawables();

   @Accessor("field_22786")
   List<class_364> getChildren();

   @Accessor("field_33815")
   List<class_6379> getSelectables();
}
