package rich.mixin;

import net.minecraft.class_5223;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import rich.events.api.EventManager;
import rich.events.api.events.render.TextFactoryEvent;

@Mixin(class_5223.class)
public class TextVisitFactoryMixin {
   @ModifyArg(
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_5223;method_27473(Ljava/lang/String;ILnet/minecraft/class_2583;Lnet/minecraft/class_2583;Lnet/minecraft/class_5224;)Z",
         ordinal = 0
      ),
      method = "method_27472(Ljava/lang/String;ILnet/minecraft/class_2583;Lnet/minecraft/class_5224;)Z",
      index = 0
   )
   private static String adjustText(String var0) {
      TextFactoryEvent var1 = new TextFactoryEvent(var0);
      EventManager.callEvent(var1);
      return var1.getText();
   }
}
